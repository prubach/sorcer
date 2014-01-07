**                       SORCER Examples
**
**   To build and run examples you have to have the following tools
**   installed:
**
**   - Java Full JDK (http://java.oracle.com)
**
**   When you have them installed and available in your PATH:
**   1. Open a terminal window or command line and navigate to the
**      directory where SORCER is installed.
**   2. Check if you have the environment variable SORCER_HOME set
**      to point to the installation directory of SORCER.
**      You can do it using:
**      export | grep SORCER_HOME
**   3. If you don't have SORCER_HOME environment variable then you
**      can set it for the current session by navigating to the
**      SORCER/examples directory and using the following script:
**      source ./setenv
**      or
**      setenv.bat (on Windows)
**   4. Before building examples for the first time please run:
**      prepare-repository
**   5. Then run: mvn install to build the examples.
**   6. If all builds succeed you are ready to go.
**      You can start providers by executing:
**      boot :ex0-cfg or
**      boot ex0/ex0-cfg/target/ex0-cfg.jar
**      or
**      look for xml ant scripts to start providers
**      and requestors.
**   7. You can start them by running:
**      ant -f script.xml
