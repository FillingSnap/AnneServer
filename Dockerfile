FROM gradle:7.6-jdk AS build
WORKDIR /build

COPY src/main /build/src/main

COPY build.gradle.kts settings.gradle.kts /build/

RUN ls -R /build/src/main
RUN cat /build/src/main/diary.json
RUN cat /build/src/main/mbti.json

RUN gradle build -x test --parallel --continue > /dev/null 2>&1 || true

FROM openjdk:17.0.1-jdk-slim AS run
WORKDIR /app

RUN adduser --system --group app-api

COPY --from=build --chown=app-api:app-api /build/build/libs/*.jar ./app.jar
COPY --from=build --chown=app-api:app-api /build/build/src/main/resources/json/diary.json ./diary.json
COPY --from=build --chown=app-api:app-api /build/build/src/main/resources/json/mbti.json ./mbti.json

USER app-api

CMD ["java", "-Dspring.profiles.active=${SPRING_PROFILE}", "-jar", "app.jar"]