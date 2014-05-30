Before starting this provider (development):

1) Install the jars in local maven repository, by calling
    mvn install
  in the project root directory.
2) start the Registry Service
  This can be done by starting SORCER from the distribution ($SORCER_HOME/bin/sorcer-boot)
3) start the service itself by calling
    sorcer-boot :${rootArtifactId}-cfg
4) test the service by running its requestor:
    ant -f ${rootArtifactId}-req/run.xml
5) You can also run the netlet script by invoking:
    nsh -f run.ntl
    On UNIX you can also simply run the script ./run.ntl
6) You can also run the generated integration test located in: ${rootArtifactId}-req/src/test/java/
   This test will automatically start the provider and run the test on it - to run it go to ${rootArtifactId}-req
   and run:
   mvn -Prun-int test

This example was generated automatically using the Sorcer maven archetype, you can generate another one by invoking:

mvn archetype:generate -DarchetypeGroupId=org.sorcersoft.sorcer -DarchetypeArtifactId=sorcer-provider