FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /sarafBRK
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:17
WORKDIR /app

COPY --from=build /sarafBRK/target/security-*.jar /app/
COPY keystore.p12 /app/keystore.p12
COPY private.key.pem /app/ssl/private.key.pem
COPY domain.cert.pem /app/ssl/domain.cert.pem

EXPOSE 8088

CMD java -jar \
         -Dspring.profiles.active=prod \
         -Dspring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME} \
         -Dspring.datasource.username=${DB_USER} \
         -Dspring.datasource.password=${DB_PASSWORD} \
         security-1.0.2.jar