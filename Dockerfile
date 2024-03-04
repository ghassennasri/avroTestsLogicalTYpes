FROM openjdk:17-jdk-alpine

WORKDIR /app
COPY . /app

# Build the application (without running tests)
RUN ./gradlew build -x test

# Default command keeps the container running
CMD ["tail", "-f", "/dev/null"]