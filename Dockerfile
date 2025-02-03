FROM eclipse-temurin:21
RUN mkdir /opt/app
COPY out/artifacts/quickbite_jar/quickbite.jar /opt/app
COPY ./.env /opt/app
WORKDIR /opt/app
CMD ["java", "-jar", "quickbite.jar"]