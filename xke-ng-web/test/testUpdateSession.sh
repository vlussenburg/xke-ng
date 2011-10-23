#!/bin/sh

applicationURL=http://ec2-46-137-184-99.eu-west-1.compute.amazonaws.com:8080/xkeng
sessionID=1319297592354
conferenceID=4ea26b84e4b0777b9acd36c4

curl $applicationURL/session/$sessionID > updateSession.json
cat updateSession.json | sed 's/Android/Androidize/' > updatedSession.json

curl -X PUT -d @updatedSession.json $applicationURL/conference/$conferenceID/session
