# Running provided Sorcer examples
## Prerequisites
- [Sorcer platform] (downloads.html)
- [Maven 3.0] (http://maven.apache.org)
- [Ant 1.7 or newer](http://ant.apache.org)

The examples are supplied in the form of source code in the Sorcer distribution (<tt>examples</tt> folder). To be able to compile and run them you need to install Maven and Ant and for conveniance add the <tt>bin</tt> folders of both of them to your <tt>PATH</tt>

Before reading on please make sure you have followed our [Getting Started guide](getting-started.html) and have a fully functioning Sorcer platform.

## Setup examples build environment
Sorcer providers and examples are built using Apache Maven, therefore they require the Sorcer components and <tt>pom</tt> files to be installed in your local maven repository (usually placed in your home folder: $HOME/.m2/repository). 

This installation may be performed automatically by running the preparation script in the <tt>$SORCER_HOME/examples</tt> folder:
<pre>ant -f prepare-repository.xml</pre>

## Build examples 
To build examples go to the <tt>$SORCER_HOME/examples</tt> folder and execute:
<pre>mvn install</pre>

## Running examples
Most examples contain the following directory structure:
- exX-api - this folder contains the code of the interfaces published by the service provider as well as classes required for passing input/output data (context) to/from the provider.
- exX-prv - the actual implementation of the service provider. This folder often contains ant scripts (XML files) for starting the provider.
- exX-req - this folder contains the requestor used for testing the service provider. The testing code may be implemented either as a regular java appication or a junit test class. 
In the first case (regular java file) you should find ant scripts for starting the requestor - there may be more than one script - they usually differ by input parameters that are provided as arguments during startup. 
In the second case (requestor as a junit class) you may have to start the junit tests by running in the exX-req folder:
<pre>mvn -Dmaven.test.skip=false test</pre>
