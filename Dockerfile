FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /sarafBRK
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:17
WORKDIR /app

COPY --from=build /sarafBRK/target/security-*.jar /app/

# Install AWS CLI to access S3
RUN yum install -y aws-cli

# Expose the application port
EXPOSE 8088

# Create the /app/ssl directory for the keystore files
RUN mkdir -p /app/ssl

CMD aws s3 cp s3://your-bucket-name/private.key.pem /app/ssl/private.key.pem && \
    aws s3 cp s3://your-bucket-name/keystore.p12 /app/ssl/keystore.p12 && \
    java -jar \
    -Dspring.profiles.active=prod \
    -Dspring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME} \
    -Dspring.datasource.username=${DB_USER} \
    -Dspring.datasource.password=${DB_PASSWORD} \
    -Djavax.net.ssl.keyStore=/app/ssl/keystore.p12 \
    -Djavax.net.ssl.keyStorePassword=${KEY_STORE_PASSWORD} \
    security-1.0.2.jar
