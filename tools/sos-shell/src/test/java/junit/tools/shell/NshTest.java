package junit.tools.shell;

import groovy.lang.Category;
import org.junit.Test;
import org.junit.runner.RunWith;
import sorcer.core.SorcerEnv;
import sorcer.junit.ExportCodebase;
import sorcer.junit.SorcerClient;
import sorcer.junit.SorcerRunner;
import sorcer.junit.SorcerServiceConfiguration;
import sorcer.service.Task;
import sorcer.util.exec.ExecUtils;

import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static sorcer.eo.operator.*;
import static sorcer.eo.operator.value;

/**
 * SORCER class
 * User: prubach
 * Date: 28.04.14
 */
@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
//@SorcerServiceConfiguration(":ju-arithmetic-cfg-all")

public class NshTest {

    private final static Logger logger = Logger
            .getLogger(NshTest.class.getName());

    private StringBuilder sb = new StringBuilder(new java.io.File(SorcerEnv.getHomeDir(),
            "bin"+ java.io.File.separator + "nsh").getAbsolutePath());


    @Test
    public void discoCmdTest() throws Exception {
        sb.append(" -c");
        sb.append(" disco");

        ExecUtils.CmdResult result = ExecUtils.execCommand(sb.toString());
        logger.info("Result running: " + sb.toString() +":\n" + result.getOut());
        assertEquals(0,result.getExitValue());
        assertTrue(result.getErr().isEmpty());
        assertTrue(result.getOut().contains("LOOKUP SERVICE"));
        assertTrue(result.getOut().contains(SorcerEnv.getLookupGroups()[0]));
        assertTrue(!result.getOut().contains("Exception"));
    }

    @Test
    public void lupCmdTest() throws Exception {
        sb.append(" -c");
        sb.append(" lup -s");

        ExecUtils.CmdResult result = ExecUtils.execCommand(sb.toString());
        assertEquals(0,result.getExitValue());
        assertTrue(result.getErr().isEmpty());
        logger.info("Result running: " + sb.toString() +":\n" + result.getOut());
        assertTrue(result.getOut().contains(SorcerEnv.getActualName("Jobber")));
        assertTrue(result.getOut().contains(SorcerEnv.getActualSpacerName()));
        assertTrue(result.getOut().contains(SorcerEnv.getActualDatabaseStorerName()));
        assertTrue(result.getOut().contains(SorcerEnv.getLookupGroups()[0]));
        assertTrue(!result.getOut().contains("Exception"));
    }

    @Test
    public void spCmdTest() throws Exception {
        sb.append(" -c");
        sb.append(" sp");

        ExecUtils.CmdResult result = ExecUtils.execCommand(sb.toString());
        assertEquals(0,result.getExitValue());
        assertTrue(result.getErr().isEmpty());
        logger.info("Result running: " + sb.toString() +":\n" + result.getOut());
        assertTrue(result.getOut().contains(SorcerEnv.getActualSpaceName()));
        assertTrue(!result.getOut().contains("Exception"));
    }

    @Test
    public void dsCmdTest() throws Exception {
        sb.append(" -c");
        sb.append(" ds");

        ExecUtils.CmdResult result = ExecUtils.execCommand(sb.toString());
        assertEquals(0,result.getExitValue());
        assertTrue(result.getErr().isEmpty());
        logger.info("Result running: " + sb.toString() +":\n" + result.getOut());
        assertTrue(result.getOut().contains(SorcerEnv.getActualDatabaseStorerName()));
        assertTrue(!result.getOut().contains("Exception"));
    }

    @Test
    public void batchCmdTest() throws Exception {
        sb.append(" -b");
        sb.append(" ${sys.sorcer.home}/tools/sos-shell/target/test-classes/batch.nsh");

        ExecUtils.CmdResult result = ExecUtils.execCommand(sb.toString());
        assertEquals(0,result.getExitValue());
        assertTrue(result.getErr().isEmpty());
        logger.info("Result running: " + sb.toString() +":\n" + result.getOut());
        assertTrue(result.getOut().contains(SorcerEnv.getActualName("Jobber")));
        assertTrue(result.getOut().contains(SorcerEnv.getActualSpacerName()));
        assertTrue(result.getOut().contains(SorcerEnv.getActualDatabaseStorerName()));
        assertTrue(result.getOut().contains(SorcerEnv.getLookupGroups()[0]));
        assertTrue(!result.getOut().contains("Exception"));
    }

    @Test
    public void batchExertCmdTest() throws Exception {
        sb.append(" -b");
        sb.append(" ${sys.sorcer.home}/tools/sos-shell/target/test-classes/batchExert.nsh");

        ExecUtils.CmdResult result = ExecUtils.execCommand(sb.toString());
        logger.info("Result running: " + sb.toString() +":\n" + result.getOut());
        assertEquals(0,result.getExitValue());
        assertTrue(result.getErr().isEmpty());
        assertTrue(!result.getOut().contains("ExertionException:"));
        assertTrue(result.getOut().contains("f1/f3/result/y3 = 400.0"));
    }
}
