#!/bin/sh

applicationURL=http://ec2-46-137-184-99.eu-west-1.compute.amazonaws.com:8080/xkeng
conferenceID=4ea26b84e4b0777b9acd36c4

curl $applicationURL/conference/$conferenceID > updateConference.json
cat updateConference.json | sed 's/XKE/TED/' > updatedConference.json

curl -X PUT -d @updatedConference.json $applicationURL/conference/$conferenceID

rm *.json
