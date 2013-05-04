Before starting this provider (development):

1) Install the jars in local maven repository, by calling
    mvn install
  in the project root directory.
2) start the Registry Service
  This can be done by starting SORCER from the distribution
3) start the service itself by calling
    ant -f ${rootArtifactId}-prv/boot.xml
4) test the service by running its requestor:
    ant -f ${rootArtifactId}-req/run.xml