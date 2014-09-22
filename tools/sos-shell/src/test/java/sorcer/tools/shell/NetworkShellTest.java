package sorcer.tools.shell;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import sorcer.core.requestor.ServiceRequestor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class NetworkShellTest {

    /**
     * Test for calling NSH from API
     */
    @Ignore
    @Test
    public void requestTest() throws Throwable {
        ServiceRequestor.prepareEnvironment();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        NetworkShell.buildInstance(true, new String[]{});
        NetworkShell.setShellOutput(printStream);
        NetworkShell.setRequest("help");
        NetworkShell.processRequest(true);
        String result = byteArrayOutputStream.toString();
        Assert.assertTrue(result.startsWith("You can manage"));
    }
}