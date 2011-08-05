echo "POST create"
curl -d @i-892.json -H"Content-Type:text/plain"  http://localhost:8080/conference
curl -d @i-892-loc.json -H"Content-Type:text/plain"  http://localhost:8080/conference

echo "Delete" 
curl -X DELETE http://localhost:8080/conference/4e3c10250364c1b1f8134b26

echo "PUT update"
curl -T i-892-loc-update.json  http://localhost:8080/conference
#echo "GET read single"
#curl  http://localhost:8080/session/1234
#echo "GET read all"
#curl  http://localhost:8080/sessions
