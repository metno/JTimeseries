FROM adoptopenjdk/maven-openjdk8 as mvn-build-stage
MAINTAINER Met Norway <it-team-frontend@met.no>

COPY settings.xml /usr/share/maven/conf/settings.xml
ARG BUILD_DIR=/root/app
WORKDIR $BUILD_DIR
COPY jtimeseries-api $BUILD_DIR/jtimeseries-api
COPY jtimeseries-webservices $BUILD_DIR/jtimeseries-webservices
COPY pom.xml $BUILD_DIR/pom.xml
RUN mvn clean install

FROM tomcat:9.0-jdk8-openjdk

ARG BUILD_DIR=/root/app
WORKDIR $BUILD_DIR
# create snake oil certificate for testing
ENV DEBIAN_FRONTEND noninteractive
RUN apt-get -q update
RUN apt-get install -y -q apt-utils
RUN apt-get install -y -q ssl-cert
RUN make-ssl-cert generate-default-snakeoil

# install web app
COPY --from=mvn-build-stage $BUILD_DIR/jtimeseries-webservices/target/jtimeseries-webservices /usr/local/tomcat/webapps/jtimeseries-webservices

# install config
ADD jtimeseries-webservices/src/config/docker/jtimeseries.properties /usr/local/tomcat/conf/jtimeseries.properties
ADD jtimeseries-webservices/src/config/docker/server.xml /usr/local/tomcat/conf/server.xml

# expose port for TLS
EXPOSE 8443

CMD ["catalina.sh", "run"]
