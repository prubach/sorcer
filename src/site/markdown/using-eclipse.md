# Using Eclipse with a Sorcer Service Provider

## Download install and configure Eclipse

Please follow the steps below to setup the Eclipse IDE.

1.  Please download `Eclipse IDE for Java Developers`   or   `Eclipse IDE for Java EE Developers`  for your operating system from:
http://www.eclipse.org/downloads/
2.  Unzip the downloaded file and start Eclipse by running `Eclipse.exe` (on Windows) or `./eclipse` (on Linux/Unix/Mac OS X).
3.  Install the Maven Integration for Eclipse (m2e) - to do that in Eclipse go to   `Help -> Eclipse Marketplace`  and search
for this plugin (see below), then click install and follow the instructions, accept the license agreement and finally restart
Eclipse.

       ![Install Maven Plugin](using-eclipse/eclipse1.png)

## Import Sorcer service provider project

To import the project generated using the [Sorcer Provider Archetype] (new-provider.html) into eclipse please follow
the following steps:

1.  Go to:  `File -> Import...`  and select  `Maven -> Existing Maven Projects`

       ![Import maven project](using-eclipse/eclipse2.png)

2.  Next, browse and select the top-level directory of the generated service provider project (`Hello`   in the example below)
When you do it for the first time it may take a couple minutes since Eclipse will try to download all necessary plugins
and libraries.

       ![Select project to import](using-eclipse/eclipse3.png)

3.  Once this directory is selected you can selects defaults or directly click on  `Finish`.


## Compile and run the project

To enforce a manual compilation of the project in Eclipse select the particular module you'd like to compile or the
top-level project (Hello in the example below) to compile all modules. Next right click it, go to:  `Run as`  and select
`Maven clean`   or   `Maven install` respectively.

![Select project to import](using-eclipse/eclipse7.png)

