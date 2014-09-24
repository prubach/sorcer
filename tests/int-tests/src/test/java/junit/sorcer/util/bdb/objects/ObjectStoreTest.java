package junit.sorcer.util.bdb.objects;

import static sorcer.eo.operator.clear;
import static sorcer.eo.operator.context;
import static sorcer.eo.operator.cxt;
import static sorcer.eo.operator.delete;
import static sorcer.eo.operator.exert;
import static sorcer.eo.operator.in;
import static sorcer.eo.operator.list;
import static sorcer.eo.operator.result;
import static sorcer.eo.operator.sig;
import static sorcer.eo.operator.size;
import static sorcer.eo.operator.store;
import static sorcer.eo.operator.task;
import static sorcer.eo.operator.value;
import static sorcer.po.operator.par;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jini.id.Uuid;

import org.junit.Assert;
import org.junit.Test;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import sorcer.core.context.PositionalContext;
import sorcer.core.context.ServiceContext;
import sorcer.core.provider.DatabaseStorer;
import sorcer.junit.ExportCodebase;
import sorcer.junit.SorcerClient;
import sorcer.junit.SorcerRunner;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.ExertionException;
import sorcer.service.SignatureException;
import sorcer.service.Task;
import sorcer.util.Sorcer;
import sorcer.util.url.sos.SdbUtil;

/**
* @author Mike Sobolewski
*/
@SuppressWarnings({ "rawtypes", "unchecked"})
@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
@ExportCodebase({"org.sorcersoft.sorcer:sorcer-api", "org.sorcersoft.sorcer:dbp-api"})
public class ObjectStoreTest {
	private final static Logger logger = LoggerFactory.getLogger(ObjectStoreTest.class.getName());

	
	@Test
	public void storeTest() throws SignatureException, ExertionException, ContextException, IOException, InterruptedException {
		Context data = cxt("stored", in("arg/x3", par("x3")), in("arg/x4", par("x4")), result("result/y"));
		
		Task objectStoreTask = task(
				"objectStore",
				sig("contextStore", DatabaseStorer.class, null, Sorcer.getActualDatabaseStorerName()),
					SdbUtil.getStoreContext(data));
		
		//objectStoreTask = exert(objectStoreTask);
		
		//logger.info("objectStoreTask: " + objectStoreTask);
		//logger.info("objectStoreTask context: " + context(objectStoreTask));
		URL objURL = (URL)value(objectStoreTask);
		logger.info("stored object URL: " + objURL);
		logger.info("retrieved object: " + objURL.getContent());
		
		Assert.assertEquals(data, objURL.getContent());
	}

	@Test
	public void storageContextTest() throws SignatureException, ExertionException, ContextException, IOException, InterruptedException {
		Context data = cxt("stored", in("arg/x3", par("x3")), in("arg/x4", par("x4")), result("result/y"));
		
		Task objectStoreTask = task(
				"objectStore",
				sig("contextStore", DatabaseStorer.class, null, Sorcer.getActualDatabaseStorerName()),
				SdbUtil.getStoreContext(data));
		
		//objectStoreTask = exert(objectStoreTask);
		
		//logger.info("objectStoreTask: " + objectStoreTask);
		//logger.info("objectStoreTask context: " + context(objectStoreTask));
		URL objURL = (URL)value(objectStoreTask);
		logger.info("stored object URL: " + objURL);
//		logger.info("retrieved object: " + objURL.getContent());
		
		Assert.assertEquals(data, objURL.getContent());
	}
	
	@Test
	public void storeOperatorTest() throws SignatureException, ExertionException, ContextException,
            IOException, InterruptedException {
		Context data = cxt("stored", in("arg/x3", par("x3")), in("arg/x4", par("x4")), result("result/y"));
		
		URL objURL = store(data);
		logger.info("stored object URL: " + objURL);
//		logger.info("retrieved object: " + objURL.getContent());
		
		Assert.assertEquals(data, objURL.getContent());
	}
	
	@Test
	public void retrievalContextTest() throws SignatureException, ExertionException, ContextException, IOException, InterruptedException {
		Context data = cxt("store", in("arg/x3", par("x3")), in("arg/x4", par("x4")), result("result/y"));
		
		Uuid uuid = data.getId();
		store(data);
		Task objectRetrieveTask = task(
				"retrieve",
				sig("contextRetrieve", DatabaseStorer.class, null, Sorcer.getActualDatabaseStorerName()),
					SdbUtil.getRetrieveContext(uuid, DatabaseStorer.Store.context));
				
		objectRetrieveTask = exert(objectRetrieveTask);
		logger.info("objectRetrieveTask: " + objectRetrieveTask);
		Object retrived = value(context(objectRetrieveTask));
//		logger.info("objectRetrieveTask context: " + retrived);
		Assert.assertEquals(data, retrived);
	}
	
	@Test
	public void updateContextTest() throws SignatureException, ExertionException, ContextException, IOException, InterruptedException {
		Context data = cxt("storeUpd", in("arg/x3", par("x3")), in("arg/x4", par("x4")), result("result/y"));
		//Context updatedData = cxt("storeUpd", in("arg/x3", par("x3", 10.0)), in("arg/x4", par("x4", 20.0)));
		
		//store task to be executed for data
		//URL objURL = store(data);
		String storageName = Sorcer.getActualName(Sorcer.getDatabaseStorerName());
		Task objectStoreTask = task(
				"store",
				sig("contextStore", DatabaseStorer.class, null, storageName),
				SdbUtil.getStoreContext(data));
	
		objectStoreTask = exert(objectStoreTask);
		Uuid objUuid = (Uuid)value(context(objectStoreTask), DatabaseStorer.object_uuid);
        logger.warn("updateContextTest, stored id: " + objUuid);


        //retrieve task to be executed for updatedData in the previous task
        Task objectRetrieveTask = task(
                "retrieve",
                sig("contextRetrieve", DatabaseStorer.class, null,
                        Sorcer.getActualDatabaseStorerName()),
                SdbUtil.getRetrieveContext(objUuid, DatabaseStorer.Store.context));

        objectRetrieveTask = exert(objectRetrieveTask);
        objUuid = (Uuid)value(context(objectRetrieveTask), DatabaseStorer.object_uuid);
        Context ctxRtr = (Context)value(context(objectRetrieveTask), DatabaseStorer.object_retrieved);
        logger.warn("updateContextTest, retrieved id: " + objUuid);
        logger.warn("updateContextTest, retrieved data: " + ctxRtr.toString());

        ctxRtr.putInValue("arg/x5", 50d);
        Context updatedData = ctxRtr;
        logger.warn("updateContextTest, retrieved moded data: " + ctxRtr.toString());


		//updated task to be executed for updatedData
		Task objectUpdateTask = task(
				"update",
				sig("contextUpdate", DatabaseStorer.class, null, Sorcer.getActualDatabaseStorerName()),
                SdbUtil.getUpdateContext(ctxRtr, objUuid));
		
		objectUpdateTask = exert(objectUpdateTask);
        objUuid = (Uuid)value(context(objectUpdateTask), DatabaseStorer.object_uuid);
        logger.warn("updateContextTest, updated id: " + objUuid);

		//retrieve task to be executed for updatedData in the previous task
		objectRetrieveTask = task(
				"retrieve",
				sig("contextRetrieve", DatabaseStorer.class, null,
                        Sorcer.getActualDatabaseStorerName()),
                SdbUtil.getRetrieveContext(objUuid, DatabaseStorer.Store.context));
		
		objectRetrieveTask = exert(objectRetrieveTask);
        objUuid = (Uuid)value(context(objectRetrieveTask), DatabaseStorer.object_uuid);
        logger.warn("updateContextTest, retrieved id: " + objUuid);
        logger.info("updated data: " + updatedData);
		logger.info("retrieved updated data: " + value(context(objectRetrieveTask), DatabaseStorer.object_retrieved));
		Assert.assertEquals(value(context(objectRetrieveTask),DatabaseStorer.object_retrieved), updatedData);
	}
	
	@Test
	public void listStoredEntriesTest() throws SignatureException, ExertionException, ContextException, IOException, InterruptedException {
		Context data1 = cxt("stored", in("arg/x3", par("x3")), in("arg/x4", par("x4")), result("result/y"));
		//logger.info("id1: " + data1.getId());
		URL objURL1 = store(data1);
		List<String> content = list(objURL1);
		int initSize = content.size();
		//logger.info("initial store size: " + initSize);
		//logger.info("content 1: " + content);

		Context data2 = cxt("stored", in("arg/x5", par("x5")));
		//logger.info("id2: " + data2.getId());
		URL objURL2 = store(data2);
        content = list(objURL2);
		//logger.info("content size: " + content.size());
		//logger.info("content 2: " + content);
		Assert.assertEquals(content.size(), initSize + 1);
	}

	//@Ignore
	@Test
	public void deleteStoredEntriesTest() throws SignatureException, ExertionException, ContextException, IOException, InterruptedException {
		Context data1 = cxt("stored", in("arg/x3", par("x3")), in("arg/x4", par("x4")), result("result/y"));
		URL objURL1 = store(data1);
        List<String> content = list(objURL1);
		int initSize = content.size();
		logger.info("initial store size: " + initSize);
		logger.info("content 1: " + content);

		Context data2 = cxt("stored", in("arg/x5", par("x5")));
		logger.info("id2: " + data2.getId());
		URL objURL2 = store(data2);
        content = list(objURL2);
		logger.info("content size: " + content.size());
		logger.info("content 2: " + content);
		Assert.assertEquals(content.size(), initSize + 1);

		delete(objURL1);
        content = list(objURL2);
		Assert.assertEquals(content.size(), initSize);
		
		objURL2 = store(data1);
        content = list(objURL2);
		Assert.assertEquals(content.size(), initSize + 1);

		delete(data1);
        content = list(objURL2);
		Assert.assertEquals(content.size(), initSize);
		
		int storeSize = size(DatabaseStorer.Store.context);
		logger.info("storeSize before clear: " + storeSize);
		
		int size = (int)clear(DatabaseStorer.Store.context);
        logger.info("cleared tally: " + size);
		Assert.assertEquals(storeSize, size);
		
		storeSize = size(DatabaseStorer.Store.context);
		logger.info("storeSize after clear: " + storeSize);
		Assert.assertEquals(storeSize, 0);

	}
}
