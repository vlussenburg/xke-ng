KEY_NAME=ec2xkeng
echo "use as first and last name the hostname: ec2-46-137-184-99.eu-west-1.compute.amazonaws.com"
echo "the other data does not really matter"
echo "keystore pwd: jetty123"
echo "certificate pwd: xke123"
#generate keystore with certificate
keytool -genkey -keyalg RSA -keysize 1024 -alias $KEY_NAME  -keystore ec2-jetty-ssl.keystore -validity 365
#export certificate
keytool -storepass jetty123 -alias  $KEY_NAME -keystore ec2-jetty-ssl.keystore -export -rfc -file ${KEY_NAME}.cert

echo "because it's a self-signed certificate it needs to imported in the local keystore. Otherwhise it's not trused"
echo "for the mac:"
echo "cd /Library/Java/Home/lib/security"
echo "sudo keytool -import -keystore cacerts -alias $KEY_NAME -file ~/${KEY_NAME}.cert"
echo "jdk keystore (cacerts) password: changeit" 
echo "${KEY_NAME}.cert key password: xke123"
echo "ec2-jetty-ssl.keystore password: jetty123"
