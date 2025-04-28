FROM mcr.microsoft.com/playwright:v1.50.0-noble

RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates && update-ca-certificates
RUN apt-get update && apt-get install -y openjdk-21-jdk

RUN npm install -g playwright@1.51.0
RUN npx playwright install

WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8210
CMD ["java", "-Duser.timezone=GMT+8", "-Xms2g", "-Xmx2g", "-jar", "app.jar"]