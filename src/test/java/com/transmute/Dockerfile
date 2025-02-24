FROM openjdk:11-jdk-slim

# Print OS version
RUN  cat /etc/os-release

RUN apt-get update && apt-get install -y libreoffice procps && apt-get clean

# Set environment variable for LibreOffice path
ENV LIBREOFFICE_BIN="/usr/lib/libreoffice"

# Print a message at the start of the installation process
RUN echo "🚀 Starting LibreOffice installation process..."

# Ensure LibreOffice is installed
RUN apt-get update && apt-get install -y libreoffice && apt-get clean && rm -rf /var/lib/apt/lists/*

RUN apt-get update && apt-get install -y \
    libreoffice \
    fonts-noto \
    fonts-noto-cjk \
    fonts-arphic-ukai \
    fonts-arphic-uming \
    fonts-ipafont-mincho \
    fonts-ipafont-gothic \
    fonts-unfonts-core \
    && apt-get clean

# Verify LibreOffice installation
RUN echo "🛠 Checking LibreOffice version..." && command -v libreoffice && libreoffice --version


# Print LibreOffice version on container startup
RUN libreoffice --version

# Print LibreOffice location and version
RUN echo "LibreOffice installed at: $(which libreoffice)" && libreoffice --version

# Print installed LibreOffice version
RUN libreoffice --version

RUN libreoffice --version

RUN which soffice

RUN which libreoffice

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/*.jar /app/ldu.jar

# Expose the port your application listens on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "ldu.jar"]