

/*@Grab(group='org.sorcersoft.sorcer', module='sorcer-api', version='[1.0-SNAPSHOT]')
@Grab(group='org.sorcersoft.sorcer', module='sorcer-spi', version='[1.0-SNAPSHOT]')
@Grab(group='org.sorcersoft.sorcer', module='dbp-handler', version='[1.0-SNAPSHOT]')
@Grab(group='org.sorcersoft.sorcer', module='commons-req', version='[1.0-SNAPSHOT]')
@Grab(group='org.sorcersoft.sorcer', module='sorcer-rio-start', version='[1.0-SNAPSHOT]')
@Grab(group='org.sorcersoft.sorcer', module='sorcer-resolver', version='[1.0-SNAPSHOT]')
@Grab(group='org.sorcersoft.sorcer', module='sos-platform', version='[1.0-SNAPSHOT]')
@Grab(group='org.sorcersoft.sorcer', module='sos-webster', version='[1.0-SNAPSHOT]')
@Grab(group='net.jini', module='jsk-platform', version='[2.2.2]')
@Grab(group='org.rioproject.resolver', module='resolver-api', version='[5.0-M4-S7]')
@Grab(group='org.rioproject.resolver', module='resolver-aether', version='[5.0-M4-S7]')
@Grab(group='org.rioproject', module='rio-start', version='[5.0-M4-S7]')*/
@Grab(group='org.sorcersoft.sorcer', module='sorcer-lib', version='[1.0-SNAPSHOT]')
import sorcer.protocol.ProtocolHandlerRegistry;
import sorcer.util.url.HandlerInstaller;
@Grab(group='org.sorcersoft.sorcer', module='sorcer-rio-start', version='[1.0-SNAPSHOT]')
import sorcer.service.Task;
import sorcer.service.Context;
import sorcer.core.signature.NetSignature;
import sorcer.core.exertion.NetTask;
import sorcer.core.context.PositionalContext;
import sorcer.core.requestor.ServiceRequestor;

//@GrabResolver(name='sorcersoft', root='http://mvn.sorcersoft.com/')
ServiceRequestor.prepareEnvironment();
def HandlerInstaller hInst = new HandlerInstaller(ProtocolHandlerRegistry.get());

def cdb = new String[1];
cdb[0] = "org.sorcersoft.sorcer:ju-arithmetic-api";
ServiceRequestor.prepareCodebase(cdb);

System.properties.each { k,v->
    println "$k = $v"
}
this.class.classLoader.rootLoader.addURL(
    new URL("file:///pol/.m2/repository/org/sorcersoft/sorcer/sorcer-rio-start/1.0-SNAPSHOT/sorcer-rio-start-1.0-SNAPSHOT.jar"));
this.class.classLoader.rootLoader.addURL(
    new URL("file:///pol/.m2/repository/org/sorcersoft/sorcer/sorcer-resolver/1.0-SNAPSHOT/sorcer-resolver-1.0-SNAPSHOT.jar"));
this.class.classLoader.rootLoader.addURL(
    new URL("file:///pol/.m2/repository/org/rioproject/resolver/resolver-aether/5.0-M4-S7/resolver-aether-5.0-M4-S7.jar"));

        //new URL("artifact:org.sorcersoft.sorcer/sorcer-rio-start/1.0-SNAPSHOT"));
        //new URL("artifact:org.sorcersoft.sorcer/sorcer-lib/1.0-SNAPSHOT"))
//@GrabResolver(name='sorcersoft', root='http://mvn.sorcersoft.com/')
@Grab(group='org.sorcersoft.sorcer', module='ju-arithmetic-api', version='[1.0-SNAPSHOT]')
//NetSignature sig = new NetSignature("add", "sorcer.arithmetic.provider.Adder");
NetSignature sig = new NetSignature("add", junit.sorcer.core.provider.Adder.class);
@Grab(group='org.sorcersoft.sorcer', module='sos-platform', version='[1.0-SNAPSHOT]')
Context ctx = new PositionalContext("add");
ctx.putInValue("arg1/value", 20.5);
ctx.putInValue("arg2/value", 80.0);
// We know that the output is gonna be placed in this path
ctx.putOutValue("out/value", 0)
Task task = new NetTask("ADD", sig);
task.setContext(ctx);
result=task.exert();
println result;
Context outctx = result.getContext();
println outctx.getValue0("out/value");
//context.respName = outctx.getValue0("out/value");

