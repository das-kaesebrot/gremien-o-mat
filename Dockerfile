FROM tomcat:10-jre11

RUN rm -rf /usr/local/tomcat/webapps/ROOT
# COPY ./build/libs/gremiomat-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/gremiomat.war
COPY ./build/libs/gremiomat-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
# CMD ["catalina.sh","run"]
