FROM gradle:7.6-jdk AS build
WORKDIR /build
ENV GRADLE_USER_HOME=/gradle

COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew

RUN --mount=type=cache,target=/gradle \
    --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon -q help

COPY src ./src

RUN --mount=type=cache,target=/gradle \
    --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon --build-cache --configuration-cache \
      -x test bootJar

FROM eclipse-temurin:17-jre AS run
WORKDIR /app
COPY --from=build /build/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]