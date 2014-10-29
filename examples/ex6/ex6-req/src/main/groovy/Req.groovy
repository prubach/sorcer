import sorcer.protocol.ProtocolHandlerRegistry;
import sorcer.util.url.HandlerInstaller;

import sorcer.service.Task;
import sorcer.service.Context;
import sorcer.core.signature.NetSignature;
import sorcer.core.exertion.NetTask;
import sorcer.core.context.PositionalContext;
@Grab(group='org.sorcersoft.sorcer', module='sorcer-lib', version='[1.0-SNAPSHOT]')
@Grab(group='org.rioproject', module='rio-api', version='[5.0-M4-S7]')
@Grab(group='org.rioproject.resolver', module='resolver-api', version='[5.0-M4-S7]')
import sorcer.core.requestor.ServiceRequestor

ServiceRequestor.prepareEnvironment();
def HandlerInstaller hInst = new HandlerInstaller(ProtocolHandlerRegistry.get());
println(System.getenv("RIO_HOME"));
println(System.getenv("SORCER_HOME"));

def cdb = new String[1];
cdb[0] = "org.sorcersoft.sorcer:ju-arithmetic-api";
ServiceRequestor.prepareCodebase(cdb);
@Grab(group='org.sorcersoft.sorcer', module='ju-arithmetic-api', version='[1.0-SNAPSHOT]')
NetSignature sig = new NetSignature("add", junit.sorcer.core.provider.Adder.class);
Context ctx = new PositionalContext("add");
ctx.putInValue("arg1/value", 20.5);
ctx.putInValue("arg2/value", 80.0);
ctx.putOutValue("out/value", 0)
Task task = new NetTask("ADD", sig);
task.setContext(ctx);
result=task.exert();
Context outctx = result.getContext();
println(outCtx);
