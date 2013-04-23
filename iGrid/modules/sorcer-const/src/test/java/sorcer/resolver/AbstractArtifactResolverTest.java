package sorcer.resolver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Rafał Krupiński
 */
public class AbstractArtifactResolverTest {
	AbstractArtifactResolver resolver;

	@Before
	public void init() {
		resolver = new RepositoryArtifactResolver(null);
		//resolver = new RepositoryArtifactResolver(System.getProperty("user.home") + "/.m2/repository");
	}

	@Test
	public void testResolveVersionProper() throws Exception {
		String version = resolver.resolveVersion("org.slf4j", "slf4j-api");
		Assert.assertNotNull(version);
	}

	@Test
	public void testResolveVersionRiver() throws Exception {
		String version = resolver.resolveVersion("org.apache.river", "outrigger");
		Assert.assertNotNull(version);
	}

	@Test
	public void testResolveVersionImproper() throws Exception {
		String version = resolver.resolveVersion("---mxbnvksghlk", "asdasdasd");
		Assert.assertNull(version);
	}


}
