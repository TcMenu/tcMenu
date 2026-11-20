# Alpine Linux image with Amazon Corretto OpenJDK 25
FROM amazoncorretto:25-alpine-jdk

RUN apk upgrade --no-cache zlib

# Set working directory
WORKDIR /opt/tcmenu

# Create application directories
RUN mkdir -p /opt/tcmenu/plugins /opt/tcmenu/web /opt/tcmenu/data/prod/logs

# Copy application artifacts
# 1. Spring Boot executable JAR
COPY web-designer/tcmenu-web-generator/target/tcMenuDesigner.jar /opt/tcmenu/tcmenu-web-generator.jar

# 2. XML Plugins
COPY web-designer/tcmenu-xml-plugins/ /opt/tcmenu/plugins/

# 3. React Frontend Static Build Assets
COPY web-designer/tcmenugen/build/ /opt/tcmenu/web/

# Set Environment variables
ENV HOME_DIR=/opt/tcmenu/data \
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
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dhosted.versions.path=/mnt/designer-versions.json", "-Dspring.profiles.active=prod", "-jar", "/opt/tcmenu/tcmenu-web-generator.jar"]
