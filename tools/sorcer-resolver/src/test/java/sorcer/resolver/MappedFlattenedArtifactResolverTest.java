package sorcer.resolver;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * @author Rafał Krupiński
 */
@Ignore
public class MappedFlattenedArtifactResolverTest {
	MappedFlattenedArtifactResolver resolver = new MappedFlattenedArtifactResolver(new File(System.getenv("SORCER_HOME"), "lib"));

	@Test
	public void testResolveSimpleName() throws Exception {
		String path = resolver.resolveSimpleName("serviceui", "jar");

		String expected = Resolver.resolveRelative("net.jini.lookup:serviceui");
		Assert.assertEquals(expected, path);
	}

	@Test
	public void testWithGuava() throws Exception {
		String path = resolver.resolveSimpleName("guava", "jar");

		String expected = resolver.resolveRelative("com.google.guava:guava:15.0");
		Assert.assertEquals(expected, path);
	}
}
