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

##Starting provider

1. Start Sorcer - please refer to our [Getting Started Guide](getting-started.html) to see how to do that
1. Start provider (from console in project directory):
                    <pre>ant -f &lt;serviceName&gt;-prv/boot.xml</pre>
1. If you use IntelliJ Idea you can drag and drop the boot.xml script to the "Ant build" window and double-click the "boot.provider" goal

##Testing provider

- To test your provider start sample requestor (client):
            <pre>ant -f &lt;serviceName&gt;-req/run.xml</pre>
- If you use IntelliJ Idea you can drag and drop the run.xml script to the "Ant build" window and double-click the "run.requestor" goal
