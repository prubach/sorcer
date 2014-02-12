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
**      To set the environment variables permanently on Unix add this line to your
**      .profile, .bashrc or /etc/environment file
**   4. Before building examples for the first time please run:
**      ./prepare-repository
**   5. Then run: mvn install to build the examples.
**   6. If all builds succeed you are ready to go.
**      You can start providers by executing:
**      sorcer-boot :ex0-cfg or
**      sorcer-boot ex0/ex0-cfg/target/ex0-cfg.jar
**      Run sorcer-boot -h to see all options
**      or
**      look for xml ant scripts to start providers
**   7. You can run an NTL script by executing:
**      ./run.ntl or
**      Starting the nsh shell: nsh
**      and then running: exert path/to/file.ntl
**   8. Some examples have precompiled requestors. In that case look for
**      ant scripts and start them by running:
**      ant -f script.xml
