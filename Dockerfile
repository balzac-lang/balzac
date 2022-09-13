FROM tomcat:9-jdk17

LABEL maintainer="atzeinicola@gmail.com"

ARG war

ENV WAR_FILE=${war:-xyz.balzaclang.balzac.web/target/*.war}

ADD $WAR_FILE /usr/local/tomcat/webapps/

EXPOSE 8080

HEALTHCHECK CMD curl --fail http://localhost:8080/balzac/version || exit 1

CMD ["catalina.sh", "run"]
