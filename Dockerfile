# Build stage — no build tooling is needed on the machine running this.
FROM docker.io/library/maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# Runtime — a Red Hat UBI image that already runs as a non-root user, so the container works
# under the restricted-v2 SCC with whatever UID OpenShift assigns it.
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:latest
COPY --from=build /build/target/sample-service-*.jar /deployments/app.jar
EXPOSE 8080
CMD ["java", "-jar", "/deployments/app.jar"]
