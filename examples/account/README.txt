Before starting this provider (development):

1) Install the jars in local maven repository, by calling
    mvn install
  in the project root directory.
2) start the Registry Service
  This can be done by starting SORCER from the distribution ($SORCER_HOME/bin/sorcer-boot)
3) start the service itself by calling
    sorcer-boot :ex0-cfg
4) test the service by running its requestor:
    ant -f ex0-req/run.xml
5) You can also run the netlet script by invoking:
    nsh -f run.ntl
    On UNIX you can also simply run the script ./run.ntl

This example was generated automatically using the Sorcer maven archetype, you can generate another one by invoking:

mvn archetype:generate -DarchetypeGroupId=org.sorcersoft.sorcer -DarchetypeArtifactId=sorcer-provider