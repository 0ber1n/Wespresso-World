FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve
COPY src ./src
RUN ./mvnw package -DskipTests
EXPOSE 1337
CMD ["java", "-jar", "target/wespresso-world-0.0.1-SNAPSHOT.jar"]