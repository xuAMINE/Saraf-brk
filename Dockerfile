FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /sarafBRK
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:17
WORKDIR /app

COPY --from=build /sarafBRK/target/security-*.jar /app/

RUN yum install -y aws-cli

ARG PRIVATE_KEY_PEM
ARG DOMAIN_CERT_PEM
ARG PROFILE
ARG APP_VERSION

RUN aws s3 cp s3://saraf-brk/domain/keystore.p12 /app/ssl/keystore.p12 \
    && chmod 644 /app/ssl/keystore.p12

RUN echo ${PRIVATE_KEY_PEM} > /app/ssl/private.key.pem
RUN echo ${DOMAIN_CERT_PEM} > /app/ssl/domain.cert.pem

EXPOSE 8088

CMD java -jar \
         -Dspring.profiles.active=prod \
         -Dspring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME} \
         -Dspring.datasource.username=${DB_USER} \
         -Dspring.datasource.password=${DB_PASSWORD} \
         -Djavax.net.ssl.keyStore=/app/ssl/keystore.p12 \
         -Djavax.net.ssl.keyStorePassword=${KEY_STORE_PASSWORD} \
         security-1.0.2.jar