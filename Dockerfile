FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /sarafBRK
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests



FROM amazoncorretto:17
WORKDIR /app
COPY --from=build /sarafBRK/target/security-*.jar /app/
EXPOSE 8088

CMD java -jar \
         -Dspring.profiles.active=${ACTIVE_PROFILE} \
         -Dspring.datasource.url=${DB_URL} \
         -Dspring.datasource.username=${DB_USERNAME} \
         -Dspring.datasource.password=${DB_PASSWORD} \
         security-${APP_VERSION}.jar