package com.thecoderscorner.embedcontrol.core.util;

import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/// This is a utility mapping class for TCC applications, it has some raw SQL functions and the smallest
/// possible set of ORM functionality for our own purposes. Note that it is not intended as a general purpose ORM and
/// is just enough of an ORM for our purposes. You are welcome to use within TcMenu applications for the most basic of
/// activities, but it is not intended for wider distribution as it is somewhat incomplete.
///
/// The ORM works by a class being annotated with `TableMapping` on the class, and `FieldMapping` on fields that should
/// be loaded/saved to the database. For example:
///
/// ```
///     @TableMapping(tableName = "MY_TABLE_NAME", uniqueKeyField = "LOCAL_ID")
///     public class MyPersistedType {
///         // LocalID is the primary key, it must be integer.
///         @FieldMapping(fieldName = "LOCAL_ID", primaryKey = true, fieldType = FieldType.INTEGER)
///         private int localId;
///         // Mapping a varchar field,
///         @FieldMapping(fieldName = "MY_NAME", fieldType = FieldType.VARCHAR)
///         private String myName;
///     }
/// ```
///
/// @see FieldMapping
/// @see TableMapping
public class TccDatabaseUtilities {
    private final System.Logger logger = System.getLogger(TccDatabaseUtilities.class.getSimpleName());
    private final String newLine = System.getProperty("line.separator");
    private final Connection connection;

    public TccDatabaseUtilities(Connection c) throws SQLException {
        connection = c;
    }

    /// close out the database connection
    public void close() throws Exception {
        if(connection != null) connection.close();
    }

    /// query for records and convert them into the database type provided, it must be annotated with @TableMapping
    /// and any @FieldMapping entries will be populated.
    /// @param databaseType the class with the mappings
    /// @param whereClause the where clause of the statement
    /// @param params the parameters for the query
    /// @return a list of elements of `databaseType`
    public <T> List<T> queryRecords(Class<T> databaseType, String whereClause, Object... params) throws DataException {
        var tableInfo = databaseType.getAnnotation(TableMapping.class);

        var list = new ArrayList<T>();
        String wherePart = "";
        if(!StringHelper.isStringEmptyOrNull(whereClause)) {
            wherePart = " WHERE " + whereClause;
        }
        var sql = "SELECT * FROM " + tableInfo.tableName() + wherePart;
        try(var stmt = connection.prepareStatement(sql)) {
            addParamsToStmt(params, stmt);
            var rs = stmt.executeQuery();
            while(rs.next()) {
                list.add(fromResultSet(rs, databaseType));
            }
            return list;
        } catch (Exception ex) {
            throw new DataException("queryObject", ex);
        }
    }

    /// Converts a result set row into the type provided if it is possible to do so. IE the class is annotated with
    /// `@TableMapping` and the fields are annotated with `@FieldMapping`.
    /// @param rs the result set
    /// @param databaseType the database value type that is correctly annotated
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T fromResultSet(ResultSet rs, Class<T> databaseType) throws Exception {
        var item = databaseType.getConstructor().newInstance();
        for (var field : databaseType.getDeclaredFields()) {
            field.setAccessible(true);

            if(field.isAnnotationPresent(ProvideStore.class)) {
                field.set(item, this);
            }

            if (!field.isAnnotationPresent(FieldMapping.class)) continue;
            var fm = field.getAnnotation(FieldMapping.class);

            switch (fm.fieldType()) {
                case BOOLEAN -> field.setBoolean(item, rs.getInt(fm.fieldName()) == 1);
                case INTEGER -> field.setInt(item, rs.getInt(fm.fieldName()));
                case ISO_DATE -> field.set(item, LocalDateTime.parse(rs.getString(fm.fieldName()), DateTimeFormatter.ISO_DATE_TIME));
                case VARCHAR, LARGE_TEXT -> field.set(item, rs.getString(fm.fieldName()));
                case ENUM -> field.set(item, Enum.valueOf((Class<Enum>) field.getType(), rs.getString(fm.fieldName())));
            }
        }

        return item;
    }

    /// Query by the primary key, this must only return one result, otherwise acts like `queryRecords`
    public <T> Optional<T> queryPrimaryKey(Class<T> databaseType, Object primaryKey) throws DataException {
        var tableInfo = databaseType.getAnnotation(TableMapping.class);

        var sql = "SELECT * FROM " + tableInfo.tableName() + " WHERE " + tableInfo.uniqueKeyField() + " = ?";
        try(var stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, primaryKey);
            var rs = stmt.executeQuery();
            if(rs.next()) {
                return Optional.of(fromResultSet(rs, databaseType));
            }
            return Optional.empty();
        } catch (Exception ex) {
            throw new DataException("queryObject", ex);
        }
    }

    /// Update a record in the database either by insert if it is new, or by update otherwise. It again can only
    /// persist classes that are annotated with `@TableMapping` with fields annotated with `@FieldMapping`
    /// @param databaseType the type that is correctly annotated
    /// @data the item to persist
    /// @return the record loaded from the database
    public <T> int updateRecord(Class<T> databaseType, T data) throws DataException {
        var tableInfo = databaseType.getAnnotation(TableMapping.class);

        try {
            String sql;
            var pkField = Arrays.stream(databaseType.getDeclaredFields()).filter(this::isPrimaryKey).findFirst().orElseThrow();
            pkField.setAccessible(true);
            int pkValue = (int) pkField.get(data);
            if(pkValue == -1) {
                pkValue = nextRecordForTable(tableInfo);
                pkField.set(data, pkValue);
            }
            var count = queryRawSqlSingleInt("SELECT COUNT(*) from " + tableInfo.tableName() + " where " + tableInfo.uniqueKeyField() + " = ?", pkValue);
            var names = new ArrayList<String>();
            var values = new ArrayList<Object>();
            if (count == 0) {
                for (var field : data.getClass().getDeclaredFields()) {
                    if (!field.isAnnotationPresent(FieldMapping.class)) continue;
                    var annotation = field.getAnnotation(FieldMapping.class);
                    field.setAccessible(true);
                    names.add(annotation.fieldName());
                    putValueInArray(data, field, annotation, values);
                }
                sql = "INSERT INTO " + tableInfo.tableName() + "(" + String.join(",", names)
                        + ") values(?" + ",?".repeat(values.size() - 1) + ");";
            } else {
                for (var field : data.getClass().getDeclaredFields()) {
                    if (!field.isAnnotationPresent(FieldMapping.class)) continue;
                    field.setAccessible(true);
                    var annotation = field.getAnnotation(FieldMapping.class);
                    names.add("  " + annotation.fieldName() + " = ?");
                    putValueInArray(data, field, annotation, values);
                }
                values.add(pkValue);
                sql = "UPDATE " + tableInfo.tableName() + newLine
                        + "SET " + newLine +
                        String.join("," + newLine, names) + newLine
                        + "WHERE " + tableInfo.uniqueKeyField() + "=?";
            }

            logger.log(System.Logger.Level.DEBUG, "Exec update record - " + sql);
            try (var stmt = connection.prepareStatement(sql)) {
                addParamsToStmt(values.toArray(), stmt);
                stmt.executeUpdate();
                return pkValue;
            }
        } catch (Exception e) {
            throw new DataException("Update record " + databaseType.getSimpleName(), e);
        }
    }

    private static <T> void putValueInArray(T data, Field field, FieldMapping annotation, ArrayList<Object> values) throws IllegalAccessException {
        if(annotation.fieldType() == FieldType.BOOLEAN) {
            values.add(((boolean) field.get(data)) ? 1 : 0);
        } else if(field.get(data) != null) {
            values.add(field.get(data).toString());
        } else values.add(null);
    }

    private int nextRecordForTable(TableMapping tableInfo) throws DataException {
        var res = queryRawSqlSingleInt("SELECT MAX("+tableInfo.uniqueKeyField()+") from " + tableInfo.tableName());
        return (res == 0) ? 1 : (res + 1);

    }

    private boolean isPrimaryKey(Field field) {
        if(!field.isAnnotationPresent(FieldMapping.class)) return false;
        return field.getAnnotation(FieldMapping.class).primaryKey();
    }

    /// This should be called for each database type during start up, it will ensure that the database is up-to-date
    /// with any changes in the schema, note that only small incremental changes can be handled by this utility.
    /// It is intended that only columns are added, they should not be renamed or deleted.
    public void ensureTableFormatCorrect(Class<?>... databaseTypes) throws DataException {
        for(var databaseType : databaseTypes) {
            var tableMapping = databaseType.getAnnotation(TableMapping.class);
            var fieldMappings = getAnnotationsByType(databaseType, FieldMapping.class);
            boolean changed = false;

            logger.log(System.Logger.Level.INFO, "Checking for table " + tableMapping.tableName());
            if(!checkTableExists(tableMapping.tableName())) {
                createTableFully(tableMapping, fieldMappings);
                changed = true;
            } else {
                logger.log(System.Logger.Level.INFO, "Checking format of " + tableMapping.tableName());
                try (var stmt = connection.createStatement()) {
                    var rs = stmt.executeQuery("SELECT * FROM " + tableMapping.tableName());
                    var meta = rs.getMetaData();
                    var columnsToAdd = new ArrayList<FieldMapping>();
                    for(var field : databaseType.getDeclaredFields()) {
                        var fieldMapping = field.getAnnotation(FieldMapping.class);
                        if(fieldMapping == null) continue;
                        boolean found = false;
                        for (int i = 1; i <= meta.getColumnCount(); i++) {
                            var colName = meta.getColumnName(i);
                            if(colName.equalsIgnoreCase(fieldMapping.fieldName())) {
                                found = true;
                                break;
                            }
                        }
                        if(!found) columnsToAdd.add(fieldMapping);
                    }
                    if (!columnsToAdd.isEmpty()) {
                        logger.log(System.Logger.Level.INFO, "Changes to be applied: " + columnsToAdd);
                        alterTableToNewSpec(tableMapping, columnsToAdd);
                        changed = true;
                    }
                } catch (Exception ex) {
                    throw new DataException("Checking table structure " + tableMapping.tableName(), ex);
                }
            }
            logger.log(System.Logger.Level.INFO, "Table - " + tableMapping.tableName() + ", changed= " + changed);
        }
    }

    private FieldMapping[] getAnnotationsByType(Class<?> databaseType, Class<?> ignoredFieldMappingClass) {
        var l = Arrays.stream(databaseType.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(FieldMapping.class))
                .map(f -> f.getAnnotation(FieldMapping.class))
                .toList();
        return l.toArray(FieldMapping[]::new);
    }

    private void alterTableToNewSpec(TableMapping tableMapping, ArrayList<FieldMapping> columnsToAdd) throws DataException {
        logger.log(System.Logger.Level.DEBUG, "Altering table " + tableMapping.tableName() + " by adding " + columnsToAdd);

        if(columnsToAdd.stream().anyMatch(FieldMapping::primaryKey)) {
            throw new DataException("Extra primary keys cannot be added later to " + tableMapping.tableName());
        }

        for(var col : columnsToAdd) {
            var sql = "ALTER TABLE " + tableMapping.tableName() + " ADD COLUMN " + col.fieldName() + " " + toTypeDecl(col);
            executeRaw(sql);
        }
    }

    private void createTableFully(TableMapping tableMapping, FieldMapping[] fieldMappings) throws DataException {
        logger.log(System.Logger.Level.DEBUG, "Creating table " + tableMapping.tableName());

        var sql = "CREATE CACHED TABLE " + tableMapping.tableName() + "(" + newLine;
        sql += Arrays.stream(fieldMappings).map(fm -> "  " + fm.fieldName() + " " + toTypeDecl(fm))
                .collect(Collectors.joining("," + newLine));
        sql += ");";
        executeRaw(sql);
    }

    /// Execute a raw query given SQL and parameters for the query
    /// @param sql the SQL to execute
    /// @param  params the parameters for the query
    public void executeRaw(String sql, Object... params) throws DataException {
        logger.log(System.Logger.Level.DEBUG, "Execute raw sql " + sql);
        try (var stmt = connection.prepareStatement(sql)) {
            addParamsToStmt(params, stmt);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataException("Execute raw " + sql, e);
        }
    }

    private String toTypeDecl(FieldMapping fm) {
        var strTy = switch (fm.fieldType()) {
            case ENUM, VARCHAR, ISO_DATE -> "VARCHAR(255)";
            case INTEGER, BOOLEAN -> "INTEGER";
            case LARGE_TEXT -> "CLOB(262143)";
        };
        return fm.primaryKey() ? strTy + " PRIMARY KEY" : strTy;
    }

    /// Query for a single raw integer value, the result must be able to return as an integer.
    /// @param sql the SQL to execute that returns a single integer value
    /// @param data any parameters required to execute.
    public int queryRawSqlSingleInt(String sql, Object... data) throws DataException {
        logger.log(System.Logger.Level.DEBUG, "Query for int " + sql);
        try (var stmt = connection.prepareStatement(sql)) {

            addParamsToStmt(data, stmt);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new DataException("Query had no result but was int query");
        } catch (Exception ex) {
            throw new DataException("Query raw SQL single " + sql, ex);
        }
    }

    /// Same as queryRawSqlSingleInt but without an exception returning 0 on failure.
    public int queryRawSqlSingleIntNoException(String sql, Object... data) {
        try {
            return queryRawSqlSingleInt(sql, data);
        } catch (Exception ex) {
            logger.log(System.Logger.Level.WARNING, "query single int caught exception returning 0 - " + ex.getMessage());
            return 0;
        }
    }

    private static void addParamsToStmt(Object[] data, PreparedStatement stmt) throws SQLException {
        int i=1;
        for(var d : data) {
            if(d instanceof LocalDateTime ldt) {
                stmt.setObject(i++, ldt.format(DateTimeFormatter.ISO_DATE_TIME));
            } else {
                stmt.setObject(i++, d);
            }
        }
    }

    /// Perform a raw select and handle the results using `ResultSetConsumer`
    /// @param s the sql to process
    /// @param resultConsumer will be called to process the results.
    public void rawSelect(String s, ResultSetConsumer resultConsumer, Object... args) throws Exception {
        try (var stmt = connection.createStatement()) {
            var rs = stmt.executeQuery(s);
            resultConsumer.processResults(rs);
        }
    }

    /// Query for a list of string items
    /// @param sql the SQL to execute
    /// @param params the parameters for the sql
    /// @return a list of strings. There must only be 1 column in the dataset.
    public List<String> queryStrings(String sql, Object... params) throws DataException {
        logger.log(System.Logger.Level.DEBUG, "Query for strings " + sql);
        try (var stmt = connection.prepareStatement(sql)) {
            addParamsToStmt(params, stmt);
            var rs = stmt.executeQuery();
            var list = new ArrayList<String>();
            while(rs.next()) {
                 list.add(rs.getString(1));
            }
            return list;
        } catch (Exception ex) {
            throw new DataException("Query raw SQL single " + sql, ex);
        }
    }

    public boolean checkTableExists(String table) throws DataException {
        ResultSet mapping = null;
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            mapping = dbm.getTables(null, null, "%", null);
            while(mapping.next()) {
                String tableName = mapping.getString(3);
                if(tableName.equals(table)) return true;
            }
            return false;
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Failed checking table", ex);
            return false;
        } finally {
            if(mapping != null) {
                try {
                    mapping.close();
                } catch (SQLException e) {
                    logger.log(System.Logger.Level.ERROR, "Could not close connection", e);
                }
            }
        }
    }

    public void ensureTableExists(String tableToCheck, String sqlForCreate) {
        try {
            if(!checkTableExists(tableToCheck)) {
                executeRaw(sqlForCreate);
            }
        } catch (DataException e) {
            logger.log(System.Logger.Level.ERROR, "Could not check table exists", e);
        }
    }
}
