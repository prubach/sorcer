package sorcer.maven.plugin;

/**
 * Configure in pom.xml as
 * <pre>
 *     <requestors>
 *         <param>
 *         <mainClass>com.examples.Main</mainClass>
 *         <codebase>
 *             <param>groupId:artifactId:version</param>
 *         </codebase>
 *         <classpath>
 *             <param>groupId:artifactId:version</param>
 *         </classpath>
 *         <param>
 *     </requestors>
 * </pre>
 *
 * @author Rafał Krupiński
 */
public class ClientConfiguration {
	public String mainClass;
	public String[] codebase;
	public String[] classpath;
	public String[] arguments;

	public ClientConfiguration() {
	}

	public ClientConfiguration(String mainClass, String[] codebase, String[] classpath) {
		this.mainClass = mainClass;
		this.codebase = codebase;
		this.classpath = classpath;
	}
}
