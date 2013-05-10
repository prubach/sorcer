package sorcer.network;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import sorcer.resolver.Resolver;
import sorcer.util.ArtifactCoordinates;
import sorcer.util.LibraryPathHelper;

import java.io.File;
import java.net.InetAddress;

/**
 * @author Rafał Krupiński
 */
public class AddressProviderImplTest {
	@BeforeClass
	public static void init() {
		ArtifactCoordinates sigar = ArtifactCoordinates.coords("org.sorcersoft.sigar:sigar:zip:native:1.6.4");
		//FIXME move to own class (SigarHelper or sth)
		LibraryPathHelper.updateLibraryPath(new File(new File(Resolver.resolveAbsolute(sigar)).getParentFile(), "lib").getAbsolutePath());
	}

	@Test
	public void testGetLocalAddress() throws Exception {
		AddressProviderImpl a = new AddressProviderImpl();
		InetAddress localAddress = a.getLocalAddress();
		Assert.assertNotNull(localAddress);
	}
}
