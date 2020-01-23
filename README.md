# Play for Java training app - made by Lunalex

To follow the steps in this tutorial, you will need the correct version of Java and a build tool. You can build Play projects with any Java build tool. Since sbt takes advantage of Play features such as auto-reload, the tutorial describes how to build the project with sbt. 

Prerequisites include:

* Java Software Developer's Kit (SE) 1.8 or higher
* sbt 0.13.15 or higher (we recommend 1.2.3) Note: if you downloaded this project as a zip file from https://developer.lightbend.com, the file includes an sbt distribution for your convenience.

To check your Java version, enter the following in a command window:

`java -version`

To check your sbt version, enter the following in a command window:

`sbt sbtVersion`

If you do not have the required versions, follow these links to obtain them:

* [Java SE](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [sbt](http://www.scala-sbt.org/download.html)



## H2 database

This project uses an h2 SQL database.

For this project to be launched successfully, h2 needs to be started first:
--> go to https://github.com/Lunalex/Lunatech-first-training-db_h2 and follow the README instructions to setup the Db


## Docker & Elasticsearch (+ Kibana)

This project uses Elasticsearch (incl. Kibana) in a Docker container.

First install Docker on your machine : https://www.docker.com/

Then in the terminal go to the app in the /elasticsearch folder and type: `docker-compose up`

This will launch Elasticsearch on the port localhost:9200 and Kibana on the port localhost:5601


## Build and run the project

When H2 & Elasticsearch are setup, you can now launch the actual Play Java application (incl. Akka) through sbt.

To build and run the project:

1. In a terminal, go to the root folder of the app

2. Build the project. Enter: `sbt run`. The project builds and starts the embedded HTTP server. Since this downloads libraries and dependencies, the amount of time required depends partly on your connection's speed.

3. The message `Server started, ...` displays


The app is now started and available on the port localhost:9000 !
