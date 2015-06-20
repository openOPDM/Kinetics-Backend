#!/bin/bash

#The script below will perform next tasks:
# 1. build the project
# 2. deploy the war on Staging instance
# 3. Redeploy war on Apache Tomcat

echo "Running Maven build..."
#mvn clean package
mvn clean test package

if [ ! -e "./target/kinetics.war" ];
then 
	echo "Maven build failed, exiting..."
	exit
fi

echo "###################################"
echo "Uploading WAR file to Staging box..."
scp ./target/kinetics.war user@kinetics-ci:/home/user/war_files/kinetics.war

echo "###################################"
echo "Establishing terminal session with Staging box..."
ssh -t -t user@kinetics-ci << EOF
./kinetics_deploy_war.sh
exit
EOF
echo "###################################"
echo "Finished deployment sequence!"
