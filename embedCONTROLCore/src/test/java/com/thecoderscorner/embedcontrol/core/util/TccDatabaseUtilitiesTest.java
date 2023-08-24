package com.thecoderscorner.embedcontrol.core.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sqlite.SQLiteDataSource;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.thecoderscorner.embedcontrol.core.util.PersistenceTestObj.PersistType.*;
import static org.assertj.core.api.Assertions.assertThat;

class TccDatabaseUtilitiesTest {
    private TccDatabaseUtilities dbUtilities;
    private SQLiteDataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite::memory:");
        dbUtilities = new TccDatabaseUtilities(dataSource);
    }

    @AfterEach
    void tearDown() {
        dataSource = null;
        dbUtilities = null;
    }

    @Test
    void testCreateTableFromScratch() throws Exception {
        dbUtilities.ensureTableFormatCorrect(PersistenceTestObj.class);
        checkTableHasColumns(PersistenceTestObj.class);

        LocalDateTime then = LocalDateTime.now();
        var id = dbUtilities.updateRecord(PersistenceTestObj.class,
                new PersistenceTestObj(-1, "hello world", DISK_PERSIST, then));

        var persisted = dbUtilities.queryPrimaryKey(PersistenceTestObj.class, id).orElseThrow();
        Assertions.assertEquals(id, persisted.getPersistId());
        Assertions.assertEquals("hello world", persisted.getPersistName());
        Assertions.assertEquals(DISK_PERSIST, persisted.getPersistType());
        Assertions.assertEquals(then, persisted.getLastModified());
    }

    @Test
    void testAlterTableOnUpgrade() throws Exception {
        dbUtilities.executeRaw("CREATE TABLE PERSIST_CHECK(PERSIST_ID INTEGER PRIMARY KEY)");
        dbUtilities.ensureTableFormatCorrect(PersistenceTestObj.class);
        checkTableHasColumns(PersistenceTestObj.class);

        var persist1 = new PersistenceTestObj(-1, "hello", FLASH_PERSIST, LocalDateTime.now());
        var persist2 = new PersistenceTestObj(-1, "world", MEMORY_PERSIST, LocalDateTime.now());

        Assertions.assertEquals(1, dbUtilities.updateRecord(PersistenceTestObj.class, persist1));
        Assertions.assertEquals("hello", persist1.getPersistName());
        Assertions.assertEquals(1, persist1.getPersistId());
        Assertions.assertEquals(FLASH_PERSIST, persist1.getPersistType());

        Assertions.assertEquals(2, dbUtilities.updateRecord(PersistenceTestObj.class, persist2));
        Assertions.assertEquals("world", persist2.getPersistName());
        Assertions.assertEquals(2, persist2.getPersistId());
        Assertions.assertEquals(MEMORY_PERSIST, persist2.getPersistType());

        persist2.setPersistName("everyone");
        Assertions.assertEquals(2, dbUtilities.updateRecord(PersistenceTestObj.class, persist2));
        persist2 = dbUtilities.queryPrimaryKey(PersistenceTestObj.class, 2).orElseThrow();
        Assertions.assertEquals("everyone", persist2.getPersistName());
        Assertions.assertEquals(2, persist2.getPersistId());
        Assertions.assertEquals(MEMORY_PERSIST, persist2.getPersistType());

        var records = dbUtilities.queryRecords(PersistenceTestObj.class, "");
        Assertions.assertEquals(2, records.size());
    }

    @Test
    void testWhenSchemaMatches() throws Exception {
        dbUtilities.executeRaw("""
                CREATE TABLE PERSIST_CHECK(
                  PERSIST_ID INTEGER PRIMARY KEY,
                  PERSIST_NAME VARCHAR(255),
                  PERSIST_TYPE VARCHAR(255),
                  LAST_MODIFIED VARCHAR(255));""");

        dbUtilities.executeRaw("""
                INSERT INTO PERSIST_CHECK(PERSIST_ID, PERSIST_NAME, PERSIST_TYPE, LAST_MODIFIED)
                VALUES(1, 'hello', 'MEMORY_PERSIST', '2023-08-22T19:16:01.2562814');""");

        dbUtilities.ensureTableFormatCorrect(PersistenceTestObj.class);
        checkTableHasColumns(PersistenceTestObj.class);

        var util = dbUtilities.queryPrimaryKey(PersistenceTestObj.class, 1).orElseThrow();
        Assertions.assertEquals("hello", util.getPersistName());
        Assertions.assertEquals(1, util.getPersistId());
        Assertions.assertEquals(MEMORY_PERSIST, util.getPersistType());
        Assertions.assertEquals(LocalDateTime.parse("2023-08-22T19:16:01.2562814", DateTimeFormatter.ISO_DATE_TIME), util.getLastModified());
    }

    @Test
    public void testStringAndIntQuery() throws DataException {
        dbUtilities.executeRaw("CREATE TABLE STR_CHECK(STR_COL VARCHAR(255))");

        String sql = "SELECT STR_COL FROM STR_CHECK";
        assertThat(dbUtilities.queryStrings(sql)).isEmpty();
        dbUtilities.executeRaw("INSERT INTO STR_CHECK VALUES(?)", "ABC");
        assertThat(dbUtilities.queryStrings(sql)).containsExactlyInAnyOrder("ABC");
        dbUtilities.executeRaw("INSERT INTO STR_CHECK VALUES(?)", "DEF");
        assertThat(dbUtilities.queryStrings(sql)).containsExactlyInAnyOrder("ABC", "DEF");

        var count = dbUtilities.queryRawSqlSingleInt("SELECT COUNT(STR_COL) FROM STR_CHECK");
        Assertions.assertEquals(2, count);
    }

    private void checkTableHasColumns(Class<PersistenceTestObj> persistedType) throws Exception {
        var tableMapping = persistedType.getAnnotation(TableMapping.class);
        var sql = "SELECT COUNT(name) FROM sqlite_master WHERE type='table' AND name=?";
        var res = dbUtilities.queryRawSqlSingleInt(sql, tableMapping.tableName());
        Assertions.assertEquals(1, res);
        dbUtilities.rawSelect("SELECT * FROM " + tableMapping.tableName(), resultSet -> {
            var allFields = Arrays.stream(persistedType.getDeclaredFields())
                    .filter(pt -> pt.isAnnotationPresent(FieldMapping.class))
                    .map(pt -> pt.getAnnotation(FieldMapping.class))
                    .collect(Collectors.toList());
            var meta = resultSet.getMetaData();
            for (var field : allFields) {
                boolean found = false;
                for (var i = 1; i <= meta.getColumnCount(); i++) {
                    if (field.fieldName().equalsIgnoreCase(meta.getColumnName(i))) {
                        found = meta.getColumnTypeName(i).equalsIgnoreCase(toSqlType(field.fieldType()));
                        break;
                    }
                }
                Assertions.assertTrue(found, "Field " + field.fieldName());
            }
        });
    }

    private String toSqlType(FieldType s) {
        return switch (s) {
            case ENUM, ISO_DATE -> "VARCHAR";
            default -> s.toString();
        };
    }
}