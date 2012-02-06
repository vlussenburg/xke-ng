#!/bin/bash
 
#deploy kill current installation
VERSION=$1
if [ -z $VERSION ]
then
VERSION=1.0
fi

WAR_FILE=ng-rest-$VERSION.war

if [ -f $WAR_FILE ]
then
echo deploy war: $WAR_FILE...

./stop_jetty.sh
TODAY=`date +%Y%m%d`
echo $TODAY 

cp /opt/jetty-6.1.26/webapps/xkeng.war ~/xkeng.war.${TODAY}
cp -p $WAR_FILE /opt/jetty-6.1.26/webapps/xkeng.war
sudo service jetty start
echo successfully deployed war: $WAR_FILE

else
echo "$WAR_FILE does not exist. Exit"
exit
fi


