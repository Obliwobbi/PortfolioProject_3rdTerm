FROM amazoncorretto:17-alpine

RUN apk add --no-cache curl

WORKDIR /app

COPY target/app.jar app.jar

ENV PORT=7000

EXPOSE 7000

ENTRYPOINT ["java", "-jar", "app.jar"]