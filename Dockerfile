
FROM eclipse-temurin:21-jre-jammy AS builder
WORKDIR /extracted
ARG JAR_FILE=target/*.jar
#ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /extracted/dependencies/ ./
COPY --from=builder /extracted/spring-boot-loader/ ./
COPY --from=builder /extracted/snapshot-dependencies/ ./
COPY --from=builder /extracted/application/ ./

ARG EXPOSED_PORT=8888
EXPOSE ${EXPOSED_PORT}

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]