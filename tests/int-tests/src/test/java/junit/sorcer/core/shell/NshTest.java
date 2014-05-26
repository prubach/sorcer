package junit.sorcer.core.shell;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import sorcer.core.SorcerEnv;
import sorcer.junit.SorcerClient;
import sorcer.junit.SorcerRunner;
import sorcer.util.exec.ExecUtils;

import java.io.IOException;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * SORCER class
 * User: prubach
 * Date: 28.04.14
 */
@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
public class NshTest {

    private final static Logger logger = Logger
            .getLogger(NshTest.class.getName());
    private static final String EXCEPTION = "Exception";

    private StringBuilder sb;

    @Before
    public void init() throws IOException {
        sb = new StringBuilder(new java.io.File(SorcerEnv.getHomeDir(),
                "bin"+ java.io.File.separator + "nsh").getCanonicalPath());
    }


    @Test
    public void discoCmdTest() throws Exception {
        sb.append(" -c");
        sb.append(" disco");

        ExecUtils.CmdResult result = ExecUtils.execCommand(sb.toString());
        String res =  result.getOut();
        logger.info("Result running: " + sb.toString() +":\n" + res);
        assertTrue(res.contains("LOOKUP SERVICE"));
        assertTrue(res.contains(SorcerEnv.getLookupGroups()[0]));
        assertFalse(res.contains(EXCEPTION));
        assertFalse(result.getErr().contains(EXCEPTION));
    }

    @Test
    public void lupCmdTest() throws Exception {
        sb.append(" -c");
        sb.append(" lup -s");

        ExecUtils.CmdResult result = ExecUtils.execCommand(sb.toString());
        logger.info("Result running: " + sb.toString() +":\n" + result.getOut());
        if (!result.getErr().isEmpty())
            logger.info("Result ERROR: " + result.getErr());
        String res = result.getOut();
        assertTrue(res.contains(SorcerEnv.getActualName("Jobber")));
        assertTrue(res.contains(SorcerEnv.getActualSpacerName()));
        assertTrue(res.contains(SorcerEnv.getActualDatabaseStorerName()));
        assertTrue(res.contains(SorcerEnv.getLookupGroups()[0]));
        assertFalse(res.contains(EXCEPTION));
        assertFalse(result.getErr().contains(EXCEPTION));
    }

    @Test
    public void spCmdTest() throws Exception {
        sb.append(" -c");
        sb.append(" sp");

        ExecUtils.CmdResult result = ExecUtils.execCommand(sb.toString());
        // getting around a problem with out and err mixed up.
        String res = result.getOut();
        logger.info("Result running: " + sb.toString() +":\n" + res);
        assertTrue(res.contains(SorcerEnv.getActualSpaceName()));
        assertFalse(res.contains(EXCEPTION));
        assertFalse(result.getErr().contains(EXCEPTION));
    }

    @Test
    public void dsCmdTest() throws Exception {
        sb.append(" -c");
        sb.append(" ds");

        ExecUtils.CmdResult result = ExecUtils.execCommand(sb.toString());
        String res = result.getOut();
        logger.info("Result running: " + sb.toString() +":\n" + res);
        assertTrue(res.contains(SorcerEnv.getActualDatabaseStorerName()));
        assertFalse(res.contains(EXCEPTION));
        assertFalse(result.getErr().contains(EXCEPTION));
    }

    @Test
    public void batchCmdTest() throws Exception {
        sb.append(" -b");
        sb.append(" ${sys.sorcer.home}/configs/int-tests/nsh/batch.nsh");

        ExecUtils.CmdResult result = ExecUtils.execCommand(sb.toString());
        String res = result.getOut();
        logger.info("Result running: " + sb.toString() +":\n" + res);
        if (!result.getErr().isEmpty())
            logger.info("Result ERROR: " + result.getErr());
        assertTrue(res.contains(SorcerEnv.getActualName("Jobber")));
        assertTrue(res.contains(SorcerEnv.getActualSpacerName()));
        assertTrue(res.contains(SorcerEnv.getActualDatabaseStorerName()));
        assertTrue(res.contains(SorcerEnv.getLookupGroups()[0]));
        assertFalse(res.contains(EXCEPTION));
        assertFalse(result.getErr().contains(EXCEPTION));
    }

    @Test(timeout = 90000)
    public void batchExertCmdTest() throws Exception {
        sb.append(" -b");
        sb.append(" ${sys.sorcer.home}/configs/int-tests/nsh/batchExert.nsh");
        logger.info("Running: " + sb.toString());
        ExecUtils.CmdResult result = ExecUtils.execCommand(sb.toString());
        String res = result.getOut();
        logger.info("Result running: " + sb.toString() +":\n" + res);
        assertFalse(res.contains("ExertionException:"));
        assertTrue(res.contains("f1/f3/result/y3 = 400.0"));
        assertFalse(result.getErr().contains("ExertionException:"));
    }
}
