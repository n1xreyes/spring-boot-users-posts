FROM eclipse-temurin:21-alpine
LABEL authors="Angelo Reyes"

EXPOSE 8081

ARG JAR_FILE=target/*.jar

WORKDIR /opt/app

COPY ${JAR_FILE} opt/app/app.jar

CMD ["java","-jar","opt/app/app.jar"]