# Lightweight Alpine Linux image with Eclipse Temurin OpenJDK 25 JRE
FROM eclipse-temurin:25-jre-alpine

# Set working directory
WORKDIR /opt/tcmenu

# Create application directories
RUN mkdir -p /opt/tcmenu/plugins /opt/tcmenu/web /opt/tcmenu/data/prod/logs

# Copy application artifacts
# 1. Spring Boot executable JAR
COPY web-designer/tcmenu-web-generator/target/tcmenu-web-generator-*.jar /opt/tcmenu/tcmenu-web-generator.jar

# 2. XML Plugins
COPY web-designer/tcmenu-xml-plugins/ /opt/tcmenu/plugins/

# 3. React Frontend Static Build Assets
COPY web-designer/tcmenugen/build/ /opt/tcmenu/web/

# Set Environment variables
ENV SPRING_PROFILES_ACTIVE=prod \
    HOME_DIR=/opt/tcmenu/data \
    PACKAGED_PLUGIN_DIR=/opt/tcmenu/plugins \
    TCMENU_WEB_STATIC_DIR=/opt/tcmenu/web \
    SERVER_PORT=8080

# Expose HTTP port
EXPOSE 8080

# Run as non-root user for security
RUN addgroup -S tcmenu && adduser -S tcmenu -G tcmenu && \
    chown -R tcmenu:tcmenu /opt/tcmenu
USER tcmenu

# Start Spring Boot application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/opt/tcmenu/tcmenu-web-generator.jar"]
