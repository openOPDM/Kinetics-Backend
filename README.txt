Prerequisites
============================================================
Here the list of tools to be ready prior to start deployment:
Apache Tomcat 7.0.x (tested on 7.0.35);
Maven 3.x (to build war).

Configuration steps
============================================================
In order to set required env variables, locate profile with "env"
id and replace all properties with correct values.
 
Deployment steps
============================================================
Please, follow these steps to deploy webapp:
1. Build war for deployed application. Run next command from the server subdirectory of backend source: 
mvn clean package -P env
It will automatically run complete Unit Test suite and place final war in the target directory.
Pay attention to next file: spring-instrument-xxx.RELEASE, which is also deployed in target dir. We will need to deploy it on server.
2. Copy spring-instrument-xxx.RELEASE jar file to server via scp command, like:
scp -i ~/.ssh/KF1.pem /d/Projects/Kinetics/backend/target/spring-instrument-3.1.2.RELEASE.jar ubuntu@opdm.kineticsfoundation.org:/home/ubuntu
It is required to allow Spring Data JPA work correctly.
3. Modify catalina.sh to add Spring weaving support to running tomcat instance. If you deployed tomcat7 in Ubuntu via apt-get the required file will reside in this location:
 /usr/share/tomcat7/bin/catalina.sh
Find a place where JAVA_OPTS variable is created and and following line:
JAVA_OPTS="$JAVA_OPTS -javaagent:/home/user/spring-instrument-3.1.2.RELEASE.jar"
4. Deploy war file to tomcat7 webapps directory
5. Restart tomcat7 service, if deployed via apt-get use next command:
sudo service tomcat7 restart
6. Visit diagnostic URL to check that deployment is ok:
https://opdm.kineticsfoundation.org/kinetics/rest/mainpoint/info
Note, if you use other domain - change it, but the other path should remain the same.