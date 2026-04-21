# 1단계: 빌드
FROM gradle:8.7-jdk21 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon || true

COPY src ./src

RUN ./gradlew clean build -x test --no-daemon

# 2단계: 실행
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]