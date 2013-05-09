package sorcer.maven.util;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;

/**
 * @author Rafał Krupiński
 */
public class PolicyFileHelper {
	public static String preparePolicyFile(String outputDirectory) throws MojoFailureException {
		File policy = new File(outputDirectory, "sorcer.policy");
		if (!policy.exists()) {
			try {
				FileUtils.write(policy, "grant {permission java.security.AllPermission;};");
			} catch (IOException e) {
				throw new MojoFailureException("could not write to " + outputDirectory, e);
			}
		}
		return policy.getPath();
	}
}
