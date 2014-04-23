package sorcer.resolver;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Warning! these tests depend on versions of installed Maven artifacts and thus may not be portable
 *
 * @author Rafał Krupiński
 */
@Ignore
public class RepositoryArtifactResolverTest {
	RepositoryArtifactResolver resolver = new RepositoryArtifactResolver(new File(System.getProperty("user.home"), ".m2/repository").getPath());

	@Test
	public void testResolveSimpleName() throws Exception {
		String path = resolver.resolveSimpleName("serviceui", "jar");

		String expected = resolver.resolveRelative("com.sorcersoft.river:serviceui");
		Assert.assertEquals(expected, path);
	}

	@Ignore
	@Test
	public void testWithGuava() throws Exception {
		String path = resolver.resolveSimpleName("guava", "jar");

		String expected = resolver.resolveRelative("com.google.guava:guava:16.0.1");
		Assert.assertEquals(expected, path);
	}
}
