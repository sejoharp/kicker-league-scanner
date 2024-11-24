# Stage 1: Build the Uberjar
FROM eclipse-temurin:23-jdk-alpine AS builder

WORKDIR /app

# Copy the project files
COPY . .

# install bash to run lein.sh
RUN apk add bash

# Build the Uberjar
RUN ./lein.sh uberjar

# Stage 2: Create the final image
FROM eclipse-temurin:23-jdk-alpine

WORKDIR /app

# Copy the Uberjar from the builder stage
COPY --from=builder /app/target/kicker-league-scanner-1.0.0-standalone.jar /app/kicker-league-scanner-1.0.0-standalone.jar

# Command to start the Clojure application
CMD ["java", "-jar", "/app/kicker-league-scanner-1.0.0-standalone.jar", "--match-directory-path", "/app/downloaded-matches"]