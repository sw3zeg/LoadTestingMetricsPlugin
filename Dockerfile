
FROM docker:dind AS docker

# Основной этап сборки
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app

COPY LoadTestingMetricsPlugin/pom.xml .
RUN mvn dependency:resolve
RUN mvn dependency:resolve-plugins

COPY LoadTestingMetricsPlugin/src ./src
RUN mvn clean package -DskipTests


# Финальный образ
FROM docker:latest

# Устанавливаем всё необходимое
RUN apk add --no-cache bash openjdk17 docker-cli

WORKDIR /app

# Копируем собранный JAR из предыдущего этапа
COPY --from=build /app/target/*.jar /opt/app.jar
#COPY --from=docker /usr/local/bin/docker /usr/local/bin/docker

CMD sh -c '\
  docker stop k6 > /dev/null 2>&1 ; \
  docker rm k6 > /dev/null 2>&1 ; \
  export SUMMARY_NAME=summary_$(date +%Y-%m-%d_%H-%M-%S).json; \
  java -jar /opt/app.jar & \
  docker run --network=host -v /var/run/docker.sock:/var/run/docker.sock --name k6 -i grafana/k6:latest-with-browser \
    --summary-export=$SUMMARY_NAME run - < ./k6/script.js > /dev/null 2>&1 ; \
  docker cp k6:/home/k6/$SUMMARY_NAME ./k6/$SUMMARY_NAME; \
  wait; \
  docker stop k6 > /dev/null 2>&1 ; \
  docker rm k6 > /dev/null 2>&1 '
