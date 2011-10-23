#!/bin/sh

applicationURL=http://ec2-46-137-184-99.eu-west-1.compute.amazonaws.com:8080/xkeng

curl $applicationURL/conferences/2011 > conferences.json

conferenceId=`cat conferences.json | grep id | head -n 1 | awk -F\" '{print $4}'`

echo Conference: $conferenceId
curl $applicationURL/conference/$conferenceId > conference.json

sessionId=`cat conference.json | grep id | awk 'NR==2' | awk -F: '{print $2}' | sed 's/,//'`

echo Sessie: $sessionId
curl $applicationURL/session/$sessionId > session.json

curl -X PUT -d @session.json $applicationURL/conference/$conferenceId/session

