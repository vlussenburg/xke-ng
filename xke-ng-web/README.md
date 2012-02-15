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

Running web
-----------
1. Make sure MongoDB is running
2. mvn jetty:run
3. Go to http://localhost:8080/xkeng/index.html
