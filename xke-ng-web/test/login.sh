#!/bin/sh

#URL=https://ec2-46-137-184-99.eu-west-1.compute.amazonaws.com:8443/xkeng
#URL=https://ssl-lb-xkeng-1607107363.eu-west-1.elb.amazonaws.com/xkeng
URL=https://xke.xebia.com/xkeng

curl -v --insecure --include --request POST --data "{\"username\":\"test\",\"password\":\"test\"}" --header "Content-Type: text/json" $URL/login 
