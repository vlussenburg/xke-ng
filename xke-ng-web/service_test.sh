echo "POST create"
curl -d @new_session.json -H"Content-Type:text/plain"  http://localhost:8080/xkeng/session
echo "PUT update"
curl -T new_session.json http://localhost:8080/xkeng/session/1
echo "GET read single"
curl  http://localhost:8080/xkeng/session/1234
echo "GET read all"
curl  http://localhost:8080/xkeng/sessions
