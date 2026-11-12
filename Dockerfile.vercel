FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY . .
RUN mvn -pl seaandtea-application -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/seaandtea-application/target/seaandtea-backend.jar /app/app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
