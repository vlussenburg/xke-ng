// set the name of the project
name := "XKE-NG REST"

version := "1.0"

organization := "com.xebia.xke"

resolvers += "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"

resolvers += DefaultMavenRepository

libraryDependencies ++= Seq(
    "net.liftweb" %% "lift-webkit" % "2.4-M1" % "compile->default",
    "net.liftweb" %% "lift-mapper" % "2.4-M1" % "compile->default",
    "net.liftweb" %% "lift-wizard" % "2.4-M1" % "compile->default")

// add a test dependency on ScalaCheck
libraryDependencies += "org.scala-tools.testing" %% "scalacheck" % "1.8" % "test"

// reduce the maximum number of errors shown by the Scala compiler
maxErrors := 20

// increase the time between polling for file changes when using continuous execution
pollInterval := 1000

// append several options to the list of options passed to the Java compiler
javacOptions ++= Seq("-source", "1.5", "-target", "1.5")

// append -deprecation to the options passed to the Scala compiler
scalacOptions += "-deprecation"

seq(webSettings :_*)

libraryDependencies ++= {
  val liftVersion = "2.4-M2"
  Seq(
  "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
   "net.liftweb" %% "lift-mongodb" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mongodb-record" % liftVersion % "compile->default",
    "org.mortbay.jetty" % "jetty" % "6.1.22" % "jetty",
    "junit" % "junit" % "4.5" % "test->default",
    "ch.qos.logback" % "logback-classic" % "0.9.26",
    "org.scala-tools.testing" %% "specs" % "1.6.8" % "test->default",
    "com.h2database" % "h2" % "1.2.138"
  )
}

resolvers += "Jetty Repo" at "http://repo1.maven.org/maven2/org/mortbay/jetty"

libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "test->default"
