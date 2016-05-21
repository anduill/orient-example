# OrientDB Demo
This small project demonstrates how to use orient DB to do identity resolution (i.e. linking ephemeral ID's to some kind of universal ID)
For this project, I used a simple HTML-5 web-project template, and built an orientDB Dao (incorporating the identity resolution logic).

There are a few steps needed to get the web-app running.

## Install and start OrientDB

The following link can be referenced for a quick way to get the DB installed: [OrientInstall](http://orientdb.com/docs/2.2/Tutorial-Installation.html)
I recommend checking out the project and building yourself (this web app uses a snapshot version of the client, so the SNAPSHOT will most likely not be available in public repos)

When building the project make sure to skip tests:
```
mvn clean install -DskipTests=true
```

Here are some instructions for configuring OrientDB for your environment [OrientConfigure](http://orientdb.com/docs/2.2/Unix-Service.html)
I used the Mac alias option.  So the remainder of this README will use that syntax.

## Start OrientDB and run the Console
Run the following (for mac install)
```
$ orientdb-server start
```
```
$ orientdb-console
```

Once you get the console up and running, the following databases need to be created:
* Identity
* IdentityUnitTest

You can create a database as follows (in the console)
```
create database PLOCAL:/Users/dgarcia/databases/IdentityUnitTest
```
This command will create an "IdentityUnitTest" database and log you into it.  This database is required for the unit tests to run.
At this point, you have to exit out of the database (or quit the console) to run the unit tests.  Default mode doesn't allow connections
from multiple client (to a single database)

Make a database for the webapp (if you desire, you can use the UnitTest one you just created).

## Start the web App
After you start orientDB, and make your Identity database, you can start the webapp.
The web app doesn't need a servelet container.  First build the assembly jar:
```
mvn clean compile assembly:single
```
Then you can run the following to start the webapp:
```
$ java -cp target/orient-frontend-1.0-SNAPSHOT-jar-with-dependencies.jar net.orient.demo.App -p orient.properties -l links.csv
```
This properties file must have the following properties defined:
```
orient.url=remote:localhost/Identity
user.name=<userName>
user.pw=<userPassword>
```
The "links.csv" file is an optional parameter with localID associations (i.e. "id1,id2,id3" means that these three id's should all have the same universal-id)
In this example, our database is named "Identity".  Obviously, you can set the username and pw to whatever you like.  By default, username=admin, and pw=admin, should work for a newly created database.
After starting the webapp, simply go to [localhost:8085](http://localhost:8085) to see the webapp work.
