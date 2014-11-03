package junit.sorcer.core.shell;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import sorcer.core.SorcerEnv;
import sorcer.junit.SorcerClient;
import sorcer.junit.SorcerRunner;
import sorcer.util.StringUtils;
import sorcer.util.exec.ExecUtils;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * SORCER class
 * User: prubach
 * Date: 28.04.14
 */
@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
public class NshTest {

    private final static Logger logger = LoggerFactory
            .getLogger(NshTest.class.getName());
    private static final String EXCEPTION = "Exception";

    private static String baseCmd;

    private String[] cmds;

    @BeforeClass
    public static void init() throws IOException {
        baseCmd = new StringBuilder(new java.io.File(SorcerEnv.getHomeDir(),
                "bin"+ java.io.File.separator + "nsh").getCanonicalPath()).toString();
    }


    @Test
    public void discoCmdTest() throws Exception {
        cmds = new String[] { baseCmd, "-c", "disco"};

        ExecUtils.CmdResult result = ExecUtils.execCommand(cmds);
        String res =  result.getOut();
        logger.info("Result running: " + StringUtils.join(cmds, " ") +":\n" + res);
        assertTrue(res.contains("LOOKUP SERVICE"));
        assertTrue(res.contains(SorcerEnv.getLookupGroups()[0]));
        assertFalse(res.contains(EXCEPTION));
        if (!result.getErr().isEmpty())
            logger.info("discoCmdTest result ERROR: " + result.getErr());
        //assertFalse(result.getErr().contains(EXCEPTION));
    }

    @Test
    public void lupCmdTest() throws Exception {
        cmds = new String[] { baseCmd, "-c", "lup", "-s"};

        ExecUtils.CmdResult result = ExecUtils.execCommand(cmds);
        String res =  result.getOut();
        logger.info("Result running: " + StringUtils.join(cmds, " ") +":\n" + res);
        assertTrue(res.contains(SorcerEnv.getActualName("Rendezvous")));
        assertTrue(res.contains(SorcerEnv.getActualSpacerName()));
        assertTrue(res.contains(SorcerEnv.getActualDatabaseStorerName()));
        assertTrue(res.contains(SorcerEnv.getLookupGroups()[0]));
        assertFalse(res.contains(EXCEPTION));
        if (!result.getErr().isEmpty())
            logger.info("lupCmdTest result ERROR: " + result.getErr());
        //assertFalse(result.getErr().contains(EXCEPTION));
    }

    @Test
    public void spCmdTest() throws Exception {
        cmds = new String[] { baseCmd, "-c", "sp"};

        ExecUtils.CmdResult result = ExecUtils.execCommand(cmds);
        String res =  result.getOut();
        logger.info("Result running: " + StringUtils.join(cmds, " ") +":\n" + res);

        assertTrue(res.contains(SorcerEnv.getActualSpaceName()));
        assertFalse(res.contains(EXCEPTION));
        if (!result.getErr().isEmpty())
            logger.info("spCmdTest result ERROR: " + result.getErr());
        //assertFalse(result.getErr().contains(EXCEPTION));
    }

    @Test
    public void dsCmdTest() throws Exception {
        cmds = new String[] { baseCmd, "-c", "ds"};

        ExecUtils.CmdResult result = ExecUtils.execCommand(cmds);
        String res =  result.getOut();
        logger.info("Result running: " + StringUtils.join(cmds, " ") +":\n" + res);

        assertTrue(res.contains(SorcerEnv.getActualDatabaseStorerName()));
        assertFalse(res.contains(EXCEPTION));
        if (!result.getErr().isEmpty())
            logger.info("dsCmdTest result ERROR: " + result.getErr());
        //assertFalse(result.getErr().contains(EXCEPTION));
    }

    @Test
    public void batchCmdTest() throws Exception {
        cmds = new String[] { baseCmd, "-b", "${sys.sorcer.home}/configs/int-tests/nsh/batch.nsh"};

        ExecUtils.CmdResult result = ExecUtils.execCommand(cmds);
        String res =  result.getOut();
        logger.info("Result running: " + StringUtils.join(cmds, " ") +":\n" + res);
        if (!result.getErr().isEmpty())
            logger.info("batchCmdTest Result ERROR: " + result.getErr());
        assertTrue(res.contains(SorcerEnv.getActualName("Rendezvous")));
        assertTrue(res.contains(SorcerEnv.getActualSpacerName()));
        assertTrue(res.contains(SorcerEnv.getActualDatabaseStorerName()));
        assertTrue(res.contains(SorcerEnv.getLookupGroups()[0]));
        assertFalse(res.contains(EXCEPTION));
        assertFalse(result.getErr().contains(EXCEPTION));
    }

    @Test(timeout = 120000)
    public void batchExertCmdTest() throws Exception {
        cmds = new String[] { baseCmd, "-b", "${sys.sorcer.home}/configs/int-tests/nsh/batchExert.nsh"};

        logger.info("Running: " + StringUtils.join(cmds, " ") +":\n");
        ExecUtils.CmdResult result = ExecUtils.execCommand(cmds);
        String res =  result.getOut();
        logger.info("Result running: " + StringUtils.join(cmds, " ") +":\n" + res);
        assertFalse(res.contains("ExertionException:"));
        assertTrue(res.contains("f1/f3/result/y3 = 400.0"));
        assertFalse(result.getErr().contains("ExertionException:"));
    }
}
