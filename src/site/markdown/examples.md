# Running provided Sorcer examples
## Prerequisites
- [Sorcer platform] (download.html)
- [Maven 3.0] (http://maven.apache.org)
- [Ant 1.7 or newer](http://ant.apache.org)

The examples are supplied in the form of source code in the Sorcer distribution (<tt>examples</tt> folder). To be able to compile and run them you need to install Maven and Ant and for conveniance add the <tt>bin</tt> folders of both of them to your <tt>PATH</tt>

Before reading on please make sure you have followed our [Getting Started guide](getting-started.html) and have a fully functioning Sorcer platform.



## Setup examples build environment
Sorcer providers and examples are built using Apache Maven, therefore they require the Sorcer components and <tt>pom</tt> files to be installed in your local maven repository (usually placed in your home folder: <tt>$HOME/.m2/repository</tt>).

This installation may be performed automatically by running the preparation script in the <tt>$SORCER_HOME/examples</tt> folder respectively
(on Windows use):
<pre>prepare-repository.bat</pre>
on Unix-based systems:
<pre>ant -f prepare-repository.xml</pre>

__Important note:__
Before you proceed, please note that Maven may need to download some plugins during this process, therefore it is necessary that you have an active internet connection and that your maven is not set to work [offline] (#offline).



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



## Creating your own Sorcer service provider using Sorcer maven archetype

When you have run the examples you are ready to go on and create your own service provider. Please visit the next tutorial on [Creating Sorcer service provider using a maven archetype] (new-provider.html).



## Using maven in offline mode

<a id="offline"></a>If you have previously run maven online and therefore maven has already installed all the necessary plugins and third-party libraries in your local repository you can use maven in offline mode.

Maven may be permanently set to offline mode by adding `<offline>true</offline>` to your maven settings file: <tt>$HOME/.m2/settings.xml</tt>

It may also be invoked in offline mode dynamically by providing the <tt>-o</tt> parameter, for example:
<pre>mvn -o install</pre>
