#Creating new provider with Maven
##Prerequisites

- Java 1.6
- Maven 3.0
- Sorcer distribution
- Eclipse with m2e (Maven) plugin (or other IDE that supports Maven)
- Maven settings.xml updated for Sorcer

##Creating the project

1. Go to directory which will be the parent of your new project
1. Call maven to use the archetype <pre>mvn archetype:generate -DarchetypeGroupId=org.sorcersoft.sorcer -DarchetypeArtifactId=sorcer-provider</pre> or <pre>mvn archetype:generate -Dfilter=org.sorcersoft.sorcer:</pre>
1. Answer questions:
 1. groupId: This will usually be your organization's reversed domain name, e.g. com.sorcersoft
 1. artifactId: The project name and the project directory name, e.g. myprovider
 1. package: The Java package for your provider classes, by default groupId is used
 1. serviceDescription: The description of your provider. The given description can be seen in the Sorcer Service Browser
 1. providerInterface: The Java interface name of your provider (service type) that will be exposed to requestors
 1. providerClass: The name of the class that will implement the provider interface
 1. serviceName: The name of your service
1. Confirm properties configuration

 <pre>Now you can accept or modify entered inputs, including the default ones. If everything is OK, press Y.</pre>

1. Import your maven projects to your IDE (IntelliJ Idea or eclipse, for example). Below you can find information on how to import your project to IntelliJ idea
1. You can remove the (service UI) &lt;artifactId&gt;-sui module unless you are going to create a graphical user
                interface attached to your provider. You can also remove the &lt;artifactId&gt;-proxy module unless you want
                to crate a custom provider proxy. Remember to delete the corresponding &lt;module&gt; entry in your provider
                pom.xml for any removed module from your provider project.
1. To build your project in your the top-level directory of your project execute:
                    <pre>mvn install</pre>

##Choosing your Integrated Development Environment (IDE)

 For the development of Sorcer service providers you can choose any Java IDE that supports Apache Maven 3.x,
            however, we suggest that you use one of the following:

- Eclipse 4.x - please refer to [this manual](using-eclipse.html) to see how to import a Sorcer Service Provider project
                into Eclipse.
- IntelliJ Idea - below you can find a screenshot and a short instruction on how to import a Sorcer
                Service Provider project into IntelliJ Idea

##Importing project into IntelliJ Idea

 To work with Sorcer the free community edition of [IntelliJ Idea](http://www.jetbrains.com/idea/) should be sufficient.

1. Open IntelliJ Idea
1. Select import project and point the top-level directory of your provider project as in Figure below

 ![importing project into Idea](new-provider/importToIdea.png)

1. In the next steps of the importing wizard you can leave the defaults and continue
1. When the project is imported you can use the "Maven Projects" window to build the project

##cfg modules

A cfg module is a module (jar file) that consists only from configuration files: opstring, service configuration and policy file. It's purpose is encapsulate configurations of a service provider (or a group of).

Cfg module must have manifest file with these entries (names are compatible with Rio OAR):
- OAR-OperationalString path of opstring file inside cfg module
- OAR-Name - a name of Operational String, not used by sorcer, kept for compatibility with Rio OAR
- OAR-Version
- OAR-Activation - only for compatibility with RIO, possible value are Automatic or Manual

Also, cfg module should contain policy file named service.policy

If a module is kept in a directory under $SORCER_HOME it can be run with a special syntax (artifactId is usually module name):
<pre>sorcer-boot :artifactId</pre>

If you are using commercial distribution of SORCER, you can install cfg modules in Almanac to make it available for auto provisioning.

##Starting provider

1. Start Sorcer - please refer to our [Getting Started Guide](getting-started.html) to see how to do that
1. Start provider (from console in project directory):
                    <pre>ant -f &lt;serviceName&gt;-prv/boot.xml</pre>
1. If you use IntelliJ Idea you can drag and drop the boot.xml script to the "Ant build" window and double-click the "boot.provider" goal

##Testing provider

- To test your provider start sample requestor (client):
            <pre>ant -f &lt;serviceName&gt;-req/run.xml</pre>
- If you use IntelliJ Idea you can drag and drop the run.xml script to the "Ant build" window and double-click the "run.requestor" goal

- you can write JUnit tests for providers.
 - JUnit 4 or newer is required
 - sorcer-junit must be on classpath

    Annotate your test class with:
 - @RunWith(SorcerRunner.class) or @RunWith(SorcerSuite.class) (required)
 - @ExportCodebase({list of artifact coordinates}) exports artifacts as a codebase
 - @SorcerServiceConfiguration({list of artifacts}) annotation takes array of cfg module coordinates and starts the services before the tests are run.
 - @SorcerServiceConfigurations({array of @SorcerServiceConfiguration}) same as above, but tests are run for each @SorcerServiceConfiguration
