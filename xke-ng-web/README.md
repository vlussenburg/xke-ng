# XKE-NG REST API

This sub-project realizes the REST API of the XKE-NG platform.

The Platform uses the following technologies:

* Scala - http://www.scala-lang.org/
* MongoDB - http://www.mongodb.org/
* Lift for interfacing with Mongo and creating JSON - http://liftweb.net/

Building web
------------
You can build the project either using maven, or using sbt (>= 0.10)

Maven (http://maven.apache.org/)

* mvn clean install

SBT (https://github.com/harrah/xsbt/wiki)

* sbt test

Make sure MongoDB is running, otherwise tests will fail.

Running MongoDB
---------------
(paths use the ux notation)
MongoDB can be started by creating directory where the database can be created like:
..../mongodb/instances/xkeng/data/db

place this path in a start.sh script like:
/Users/rselie/Tools/MongoDB/mongodb-osx-x86_64-2.2.2/bin/mongod  
--dbpath /Users/rselie/Tools/MongoDB/Instances/xkeng/data/db

Start the MongoDB by starting the script: ./start.sh


Running web
-----------
1. Make sure MongoDB is running (see above).
2. mvn jetty:run
3. Go to http://localhost:8080/xkeng/index.html to go

Running RestSmokeTestClient from Eclipse to see how the json interface look like:
--------------------------
1. Install Eclipse.
2. Install the Scala Eclipse plugin:  
http://download.scala-ide.org/sdk/e37/scala29/stable/site/
3. Import the project xke-ng-web by using "import existing mvn project".
4. Make sure that all src/main/* src/test/* directories are source directories and that the ng-rest 
project is marked as a scala project. If not rightclick on the project and select "scala->
add scala nature.
5. Go to Eclipse/preferences/TCP/IP Monitor and add one on port: 8088, Hostname:localhost
port: 8080, type: HTTP, timeout: 0, startmonitor automatically: checked.
6. To run RestSmokeTestClient I had to set the property enable.security=false and change 
at the RestSmokeTestClient the LocalhostCfg port: Int = 8080 to 8088 for the TCP/IP monitor
7. Run the RestSmokeTestClient by rightclicking it and select run as-> scala application.
8. In the TCP/IP Monitor view you see all request and response messages.