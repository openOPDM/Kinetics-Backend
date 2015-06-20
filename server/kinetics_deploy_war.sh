#!/bin/bash
# WAR deployment helper script

TARGET_WEBAPP=./opt/apache-tomcat-7.0.35/webapps/kinetics.war

echo "Starting WAR deployment sequence..."
rm $TARGET_WEBAPP
cp ./war_files/kinetics.war $TARGET_WEBAPP
echo "DONE"
