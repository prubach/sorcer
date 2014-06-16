package sorcer.tools.shell;

import org.junit.Assert;
import org.junit.Test;
import sorcer.core.requestor.ServiceRequestor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class NetworkShellTest {

    /**
     * Test for calling NSH from API
     */
    @Test
    public void requestTest() throws Throwable {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        ServiceRequestor.prepareEnvironment();
        NetworkShell.buildInstance(new String[]{});
        NetworkShell.setShellOutput(printStream);
        NetworkShell.setRequest("help");
        NetworkShell.processRequest(true);
        String result = byteArrayOutputStream.toString();
        Assert.assertTrue(result.startsWith("You can manage"));
    }
}