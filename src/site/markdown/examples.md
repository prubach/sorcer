# Running provided Sorcer examples
## Prerequisites
- [Sorcer platform](download.html)

The examples are supplied in the form of source code in the SORCER distribution (<tt>examples</tt> folder). To be able to compile and run them you need have a running Maven and Ant and for convenience add the <tt>bin</tt> folders of both of them to your <tt>PATH</tt>.
SORCER distribution comes with both Maven and Ant - they're located in the SORCER_HOME/lib/ directory and SORCER scripts add their bin directories to your path. Please use the SORCER_HOME/bin/setenv scripts to configure your environment.
On Windows open the command line and run: `%SORCER_HOME%\bin\setenv.bat`
On Unix open a terminal and run: `source $SORCER_HOME/bin/setenv`

Before reading on please make sure you have followed our [Getting Started guide](getting-started.html) and have a fully functioning SORCER platform.


## Setup examples build environment
SORCER providers and examples are built using Apache Maven, therefore they require the SORCER components and <tt>pom</tt> files to be installed in your local maven repository (usually placed in your home folder: <tt>$HOME/.m2/repository</tt>).

__Important note:__
Before you proceed, please note that Maven may need to download some plugins during this process, therefore it is necessary that you have an active internet connection and that your maven is not set to work [offline](#offline).



## Build examples 
To build examples go to the <tt>$SORCER_HOME/examples</tt> folder and execute:
<pre>mvn install</pre>

This is not necessary if you just want to test existing examples without modifying their code - they come precompiled in the distribution.

## Running examples
To run the first "Hello World" example:
- go to $SORCER_HOME and execute
<pre>sorcer-boot :ex0-cfg</pre>
- open the sorcer-browser and look for HelloWorld-DEV service - it should be now booted
- open another console window and go to:
<pre>cd $SORCER_HOME/examples/ex0/ex0-req/</pre>
- run:
<pre>nsh run.ntl</pre>
or run
<pre>ant -f run.xml</pre>

If everything works correctly you should see something like this:
``
Created the nsh shell log file: /home/user/sorcer/logs/shell/nsh.log

     ---> OUTPUT EXERTION --->
     sorcer\.core\.exertion\.NetTask: hello1 task ID: 29eaa758-3bb8-4577-8646-b5b19610d2ec
       process sig: class sorcer.core.signature.NetSignature:\*;SRV;true;interface sorcer.ex0.HelloWorld;sayHelloWorld

       status: 4
       exec time: 18 msec

     ---> OUTPUT DATA CONTEXT --->
     Context name: Hello
       in/value = TESTER
       out/value = Hello there - TESTER
       task/provider = HelloWorld-DEV@192.168.1.240:192.168.1.240
``


## Running any example
Most examples contain the following directory structure:

- exX-api - this folder contains the code of the interfaces published by the service provider as well as classes required for passing input/output data (context) to/from the provider.
- exX-prv - the actual implementation of the service provider.
- exX-cfg - this folder contains the configuration files of the service. You can start the provider by executing <pre>sorcer-boot :exX-cfgY</pre> in the console after building the examples.
- exX-dl  - the codebase of the service provider (this module only contains a pom.xml file with dependent libraries that have to be exposed in the codebase of the provider).
- exX-req - this folder contains the requestor used for testing the service provider. The testing code may be implemented either as a:
 - regular java application

    you should find ant scripts for starting the requestor - there may be more than one script - they usually differ by input parameters that are provided as arguments during startup.
 - junit test class

    you may start the tests by calling <pre>mvn test -f examples/pom.xml</pre> which will run all junit tests in the example modules or <pre>mvn test -Prun-its</pre> which will run all tests that require running SORCER (which you must first run).

 - netlet script (*.ntl files) - to run them either execute the file (Unix) or run `nsh f1.ntl`.




## Creating your own SORCER service provider using SORCER maven archetype

When you have run the examples you are ready to go on and create your own service provider. Please visit the next tutorial on [Creating Sorcer service provider using a maven archetype](new-provider.html).



## Using maven in offline mode

<a id="offline"></a>If you have previously run maven online and therefore maven has already installed all the necessary plugins and third-party libraries in your local repository you can use maven in offline mode.

Maven may be permanently set to offline mode by adding `<offline>true</offline>` to your maven settings file: <tt>$HOME/.m2/settings.xml</tt>

It may also be invoked in offline mode dynamically by providing the <tt>-o</tt> parameter, for example:
<pre>mvn -o install</pre>
