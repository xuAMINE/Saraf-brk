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

EXPOSE 8088

CMD java -jar \
         -Dspring.profiles.active=prod \
         -Dspring.datasource.url=jdbc:postgresql://sarafdb.c302gk4uasl6.us-west-1.rds.amazonaws.com:5432/sarafdb \
         -Dspring.datasource.username=postgres \
         -Dspring.datasource.password=SarafBrkDataBase24$ \
         security-1.2.0.jar