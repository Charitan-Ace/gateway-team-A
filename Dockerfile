#FROM openjdk:23-jdk
#VOLUME /tmp
#COPY target/*.jar app.jar
#ENTRYPOINT ["java", "-jar", "/app.jar"]
## BUILD STAGE ##
FROM maven:3.9-amazoncorretto-23-alpine AS build

# create and copy maven settings, including repository & github credentials env settings
#RUN mkdir -p /root/.m2 && mkdir /root/.m2/repository
#COPY settings.xml /root/.m2

VOLUME /root/.m2

COPY . /tmp/app
WORKDIR /tmp/app

# token are mount as Docker secret mounts, see: https://docs.docker.com/reference/dockerfile/#run---mounttypesecret
# application following nixpacks build command, see: https://nixpacks.com/docs/providers/java
#RUN --mount=type=secret,id=GITHUB_USERNAME,env=GITHUB_USERNAME,required=true \
#    --mount=type=secret,id=GITHUB_TOKEN,env=GITHUB_TOKEN,required=true \
RUN mvn -DoutputFile=target/mvn-dependency-list.log -B -DskipTests clean dependency:list install

# use layertools for build cache
RUN mkdir -p /tmp/extracted && java -Djarmode=layertools -jar target/*jar extract --destination /tmp/extracted

## DISTROLESS IMAGE ##
FROM gcr.io/distroless/java21-debian12:nonroot
#FROM amazoncorretto:23-headless
WORKDIR /tmp/app

COPY --from=build /tmp/extracted/dependencies /tmp/app/
COPY --from=build /tmp/extracted/spring-boot-loader /tmp/app/
COPY --from=build /tmp/extracted/snapshot-dependencies /tmp/app/
COPY --from=build /tmp/extracted/application /tmp/app/

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
