# Builds one service of the multi-module project, for example:
#   docker build --build-arg MODULE=order-service -t order-service .

FROM eclipse-temurin:17-jdk AS build
ARG MODULE
WORKDIR /workspace
COPY . .
# Files checked out on Windows can have CRLF line endings, which break the wrapper script
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
# "locked" makes parallel builds of the services take turns with the shared Maven cache
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    ./mvnw -B -q -pl "${MODULE}" -am package -DskipTests \
    && cp "${MODULE}"/target/"${MODULE}"-*.jar /workspace/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=build /workspace/app.jar app.jar
USER spring
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
