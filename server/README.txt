This directory contains Java powered backend for Kinetics POC project.

******* DEVELOPMENT HOW-TO *******

In our project we use JPA and its support usually provided by J2EE containers.
Our WebApp is lightweight and built on top of Spring and tomcat7. 
Spring provides so called "weaver" to address issue of JPA support.
In order to use it is required to configure UT environent and tomcat.

Weaving support provided by Spring via instrumentation.
NOTE: This instrumentation works automatically during Maven build.

*** BUILD via MAVEN ***
Make sure to setup Maven in your environment.
Execute next command to build server:
mvn clean package
It will execute UT automatically as a part of build process.

*** ECLIPSE UT CONFIG ***

In Eclipse we need to provide one VM parameter during UT execution:
-javaagent:${project_loc}/target/spring-instrument-3.2.0.RELEASE.jar
NOTE: project must be built at least once via Maven prior to Eclipse UT execution

*** TOMCAT7 CONFIG ***
For running tomcat, please alter catalina(.bat/.sh) script.
We need to provide additional JVM options to use Spring instrumentation:
set JAVA_OPTS=%JAVA_OPTS% -javaagent:SOME_PATH/spring-instrument-3.2.0.RELEASE.jar

*** SENDMAIL CONFIG ***
Install sendmail in Ubuntu server.
Add next line to configuration file (etc/mail/sendmail.mc)
define(`confDOMAIN_NAME', `kineticsfoundation.com')dnl

*** MySQL CONFIG ***
Install mysql-server on Ubuntu server.
Create databese "kinetics". (mysql>CREATE DATABASE kinetics;)
Update dao.properties file.
NOTE: if MySQL server installed on separate machine 
	1) Create new user (mysql> CREATE USER 'root'@'machine_name';)
	2) Grand permissions to new user (mysql> GRANT ALL PRIVILEGES ON kinetics.* TO 'root'@'machine_name';)
	3) Comment out parameter "bind-address" in "/etc/mysql/my.cnf" file
