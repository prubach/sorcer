import sorcer.protocol.ProtocolHandlerRegistry
import sorcer.resolver.Resolver;
import sorcer.util.url.HandlerInstaller;

import sorcer.service.Task;
import sorcer.service.Context;
import sorcer.core.signature.NetSignature;
import sorcer.core.exertion.NetTask;
import sorcer.core.context.PositionalContext;
@Grab(group='org.sorcersoft.sorcer', module='sorcer-lib', version='[1.1-SNAPSHOT]')
//@Grab(group='org.sorcer', module='webster', version='[4.2.8]')
//@Grab(group='org.sorcer', module='sorcer-lib', version='[4.2.8]')
@GrabResolver(name='sorcersoft', root='http://mvn.sorcersoft.com/content/groups/public/')
@Grab(group='org.rioproject.resolver', module='resolver-api', version='[5.0-M4-S8]')
//@Grab(group='org.rioproject.resolver', module='resolver-aether', version='[5.0-M4-S8]')
@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.5')
import sorcer.core.requestor.ServiceRequestor

String sorcerRioStart = Resolver.resolveAbsolute("org.sorcersoft.sorcer:sorcer-rio-start:1.1-SNAPSHOT")
ClassLoader.systemClassLoader.addURL(new File(sorcerRioStart).toURI().toURL());

ServiceRequestor.prepareEnvironment();
def HandlerInstaller hInst = new HandlerInstaller(ProtocolHandlerRegistry.get());
//Class cl = RMIClassLoader.getClass();
println(System.getenv("RIO_HOME"));
println(System.getenv("SORCER_HOME"));
//println(System.getenv("IGRID_HOME"));

def cdb = new String[1];
cdb[0] = "org.sorcersoft.sorcer:ju-arithmetic-api";
//cdb[0] = "org.sorcer:ju-arithmetic-beans:4.2.8";
ServiceRequestor.prepareCodebase(cdb);
@Grab(group='org.sorcersoft.sorcer', module='ju-arithmetic-api', version='[1.1-SNAPSHOT]')
//@Grab(group='org.sorcer', module='ju-arithmetic-beans', version='[4.2.8]')
NetSignature sig = new NetSignature("add", junit.sorcer.core.provider.Adder.class);
Context ctx = new PositionalContext("add");
ctx.putInValue("arg1/value", 20.5);
ctx.putInValue("arg2/value", 80.0);
// We know that the output is gonna be placed in this path
ctx.putOutValue("out/value", 0)
Task task = new NetTask("ADD", sig);
task.setContext(ctx);
result=task.exert();
Context outctx = result.getContext();
println(outctx);
//context.respName = outctx.getValue0("out/value");
