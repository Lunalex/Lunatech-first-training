name := """play-java-hello-world-web"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.13.0"

libraryDependencies ++=Seq(
  guice,
  evolutions,
  jdbc,
  "com.h2database" % "h2" % "1.4.199",
  "org.assertj" % "assertj-core" % "3.14.0" % Test,

  // To provide an implementation of JAXB-API, which is required by Ebean.
  "javax.xml.bind" % "jaxb-api" % "2.3.1",
  "javax.activation" % "activation" % "1.1.1",
  "org.glassfish.jaxb" % "jaxb-runtime" % "2.3.2",

  // to use the web service
  javaWs,

  // to upload a CSV file
  "org.apache.commons" % "commons-lang3" % "3.5",
  "com.univocity" % "univocity-parsers" % "2.4.1",

  // to use Elasticsearch with ebean using ebean-elastic
  "io.ebean" % "ebean-elastic" % "12.1.1",

  // to use Elasticsearch with the JAVA REST API (including Apapche Http Async Client)
  "org.elasticsearch.client" % "elasticsearch-rest-client" % "7.5.1",
  "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "7.5.1"
)

