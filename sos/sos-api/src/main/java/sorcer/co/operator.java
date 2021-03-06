/*
 * Copyright 2009 the original author or authors.
 * Copyright 2009 SorcerSoft.org.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sorcer.co;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sorcer.co.tuple.*;
import sorcer.core.context.ListContext;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.model.par.Par;
import sorcer.core.provider.DatabaseStorer;
import sorcer.service.*;
import sorcer.util.Loop;
//import sorcer.vfe.filter.TableReader;
//import sorcer.vfe.util.Response;
import sorcer.util.Sorcer;
import sorcer.util.Table;
import sorcer.util.bdb.objects.UuidObject;
import sorcer.util.url.sos.SdbUtil;

@SuppressWarnings({ "rawtypes" })
public class operator {

	private static int count = 0;

	public static <T1> Tuple1<T1> x(T1 x1 ){
		return new Tuple1<T1>( x1 );
	}
	
	public static <T1> Tuple1<T1> tuple(T1 x1 ){
		return new Tuple1<T1>( x1 );
	}
	
	public static <T1,T2> Tuple2<T1,T2> x(T1 x1, T2 x2 ){
		return new Tuple2<T1,T2>( x1, x2 );
	}
	
	public static <T1,T2> Tuple2<T1,T2> tuple(T1 x1, T2 x2 ){
		return new Tuple2<T1,T2>( x1, x2 );
	}
	
	public static <T1,T2,T3> Tuple3<T1,T2,T3> x(T1 x1, T2 x2, T3 x3 ){
		return new Tuple3<T1,T2,T3>( x1, x2, x3 );
	}
	
	public static <T1,T2,T3> Tuple3<T1,T2,T3> tuple(T1 x1, T2 x2, T3 x3 ){
		return new Tuple3<T1,T2,T3>( x1, x2, x3 );
	}
	
	public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> x(T1 x1, T2 x2, T3 x3, T4 x4 ){
		return new Tuple4<T1,T2,T3,T4>( x1, x2, x3, x4 );
	}
	
	public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> tuple(T1 x1, T2 x2, T3 x3, T4 x4 ){
		return new Tuple4<T1,T2,T3,T4>( x1, x2, x3, x4 );
	}
	
	public static <T1,T2,T3,T4,T5> Tuple5<T1,T2,T3,T4,T5> x(T1 x1, T2 x2, T3 x3, T4 x4, T5 x5 ){
		return new Tuple5<T1,T2,T3,T4,T5>( x1, x2, x3, x4, x5 );
	}
	
	public static <T1,T2,T3,T4,T5> Tuple5<T1,T2,T3,T4,T5> tuple(T1 x1, T2 x2, T3 x3, T4 x4, T5 x5 ){
		return new Tuple5<T1,T2,T3,T4,T5>( x1, x2, x3, x4, x5 );
	}
	
	public static <T1,T2,T3,T4,T5,T6> Tuple6<T1,T2,T3,T4,T5,T6> x(T1 x1, T2 x2, T3 x3, T4 x4, T5 x5, T6 x6 ){
		return new Tuple6<T1,T2,T3,T4,T5,T6>( x1, x2, x3, x4, x5, x6 );
	}
	
	public static <T1,T2,T3,T4,T5,T6> Tuple6<T1,T2,T3,T4,T5,T6> tuple(T1 x1, T2 x2, T3 x3, T4 x4, T5 x5, T6 x6 ){
		return new Tuple6<T1,T2,T3,T4,T5,T6>( x1, x2, x3, x4, x5, x6 );
	}

	public static Class[] types(Class... classes) {
		return classes;
	}

	public static Object[] typeArgs(Object... args) {
		return args;
	}

	public static String[] from(String... elems) {
		return elems;
	}
	
	public static <T> T[] array(T... elems) {
		return elems;
	}
	
	public static Arg[] args(Arg... elems) {
		return elems;
	}
	
	public static <T> Set<T> bag(T... elems) {
		return new HashSet<T>(list(elems));
	}

	public static <T> List<T> list(T... elems) {
		List<T> out = new ArrayList<T>(elems.length);
		for (T each : elems) {
			out.add(each);
		}
		return out;
	}

	public static List<Object> row(Object... elems) {
		return Arrays.asList(elems);
	}
	
	public static List<Object> values(Object... elems) {
		List<Object> list = new ArrayList<Object>();
		for(Object o: elems) {
			list.add(o);
		}
		return list;
	}

	public static List<String> header(String... elems) {
		List<String> out = new Header<String>(elems.length);
		for (String each : elems) {
			out.add(each);
		}
		return out;
	}

	public static List<String> names(String... elems) {
		List<String> out = new ArrayList<String>(elems.length);
		for (String each : elems) {
			out.add(each);
		}
		return out;
	}

	public static List<String> names(List<String>... nameLists) {
		List<String> out = new ArrayList<String>();
		for (List<String> each : nameLists) {
			out.addAll(each);
		}
		return out;
	}
	
	public static <T1, T2> Tuple2<T1, T2> duo(T1 x1, T2 x2) {
		return new Tuple2<T1, T2>(x1, x2);
	}

	public static <T1, T2, T3> Tuple3<T1, T2, T3> triplet(T1 x1, T2 x2, T3 x3) {
		return new Tuple3<T1, T2, T3>(x1, x2, x3);
	}

	public static <T2> Entry<T2> ent(String x1, T2 x2) {
		return new Entry<T2>(x1, x2);
	}
	
	public static <T2> Entry<T2> ent(String x1) {
		return new Entry<T2>(x1, null);
	}
	
	public static <T> AnnotatedEntry<T> ent(String x1, String tag, T x2) {
		return new AnnotatedEntry<T>(x1, tag, x2);
	}
	
	public static <T2> Entry<T2> dbEnt(String x1, T2 x2) {
		Entry<T2> t2 = new Entry<T2>(x1, x2);
		t2.isPersistent = true;
		return t2;
	}
	
	public static <T1, T2> Tuple2<T1, T2> dbEnt(T1 x1, T2 x2, URL dbURL) {
		Tuple2<T1, T2> t2 = new Tuple2<T1, T2>(x1, x2);
		t2.isPersistent = true;
		t2.datastoreURL = dbURL;
		return t2;
	}
	
	public static <T1, T2, T3> Tuple3<T1, T2, T3> entry(T1 x1, T2 x2, T3 x3) {
		return new Tuple3<T1, T2, T3>(x1, x2, x3);
	}

	public static FidelityEntry entry(String x1, FidelityInfo x3) {
		return new FidelityEntry(x1, x3);
	}
	
	public static StrategyEntry entry(String x1, Strategy x2) {
		return new StrategyEntry(x1, x2);
	}
	
	public static <T1, T2> T1 key(Tuple2<T1, T2> entry) {
		return entry._1;
	}

	public static <T extends List<?>> Table table(T... elems) {
		int rowCount = elems.length;
		int columnCount = ((List<?>) elems[0]).size();
		Table out = new Table(rowCount, columnCount);
		for (int i = 0; i < rowCount; i++) {
			if (elems[i] instanceof Header) {
				out.setColumnIdentifiers(elems[0]);
			} else {
				out.addRow((List<?>) elems[i]);
			}
		}
		return out;
	}

	public static <T extends Object> ListContext<T> listContext(T... elems)
			throws ContextException {
		ListContext<T> lc = new ListContext<T>();
		for (int i = 0; i < elems.length; i++) {
			lc.add(elems[i]);
		}
		return lc;
	}

	public static Object value(Table table, String rowName, String columnName) {
		return table.getValue(rowName, columnName);
	}

	public static Object value(Table table, int row, int column) {
		return table.getValueAt(row, column);
	}

	public static Map<Object, Object> dictionary(Tuple2<?, ?>... entries) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		for (Tuple2<?, ?> entry : entries) {
			map.put(entry._1, entry._2);
		}
		return map;
	}
	
	public static <K, V> Map<K, V> map(Tuple2<K, V>... entries) {
		Map<K, V> map = new HashMap<K, V>();
		for (Tuple2<K, V> entry : entries) {
			map.put(entry._1, entry._2);
		}
		return map;
	}

	public static Loop loop(int to) {
		Loop loop = new Loop(to);
		return loop;
	}

	public static Loop loop(int from, int to) {
		Loop loop = new Loop(from, to);
		return loop;
	}
	
	public static Loop loop(String template, int to) {
		Loop loop = new Loop(template, 1, to);
		return loop;
	}
	
	public static Loop loop(List<String> templates, int to) {
		Loop loop = new Loop(templates, to);
		return loop;
	}
	
	public static Loop loop(String template, int from, int to) {
		Loop loop = new Loop(template, from, to);
		return loop;
	}
	
	public static List<String> names(Loop loop, String prefix) {
		return loop.getNames(prefix);
	}
		
	public static String[] names(String name, int size, int from) {
		List<String> out = new ArrayList<String>();
		for (int i = from - 1; i < from + size - 1; i++) {
			out.add(name + (i + 1));
		}
		String[] names = new String[size];
		out.toArray(names);
		return names;
	}
	
	private static String getUnknown() {
		return "unknown" + count++;
	}
	
	private static String getUnknown(String name) {
		return name + count++;
	}
	
	public static class Header<T> extends ArrayList<T> {
		private static final long serialVersionUID = 1L;

		public Header() {
			super();
		}
		
		public Header(int initialCapacity) {
			super(initialCapacity);
		}
	}



	// Compat for SORCER 5.0.0-SNAPSHOT {
	public static InputEntry inEnt(String path, Object value) {
		return in(path, value);
	}

	public static InputEntry inEnt(String path) {
		return in(path);
	}

	public static OutputEntry outEnt(String path) {
		return out(path);
	}

	public static <T> InputEntry<T> inEnt(String path, T value, String annotation) {
		InputEntry<T> ie = inEnt(path, value);
		ie.annotation(annotation);
		return ie;
	}

	public static <T> OutputEntry<T> outEnt(String path, T value) {
		if (value instanceof String && ((String)value).indexOf('|') > 0) {
			OutputEntry oe =  outEnt(path, null);
			oe.annotation((String)value);
			return oe;
		}
		return new OutputEntry(path, value, 0);
	}

	public static <T> OutputEntry<T> outEnt(String path, T value, String annotation) {
		OutputEntry oe =  outEnt(path, value);
		oe.annotation(annotation);
		return oe;
	}


	public static OutputEntry output(String path, Object value) {
		return new OutputEntry(path, value, 0);
	}

	public static OutputEntry out(String path, Object value) {
		return new OutputEntry(path, value, 0);
	}

	public static OutputEntry output(String path, Object value, int index) {
		return new OutputEntry(path, value, index);
	}

	public static OutputEntry out(String path, Object value, int index) {
		return new OutputEntry(path, value, index);
	}

	public static InputEntry input(String path) {
		return new InputEntry(path, null, 0);
	}

	public static OutputEntry out(String path) {
		return new OutputEntry(path, null, 0);
	}

	public static OutputEntry output(String path) {
		return new OutputEntry(path, null, 0);
	}

	public static InputEntry in(String path) {
		return new InputEntry(path, null, 0);
	}

	public static Entry at(String path, Object value) {
		return new Entry(path, value, 0);
	}

	public static Entry at(String path, Object value, int index) {
		return new Entry(path, value, index);
	}

	public static InputEntry input(String path, Object value) {
		return new InputEntry(path, value, 0);
	}

	public static InputEntry in(String path, Object value) {
		return new InputEntry(path, value, 0);
	}

	public static InputEntry dbInEnt(String path, Object value) {
		return new InputEntry(path, value, true, 0);
	}

	public static OutputEntry dbOutEnt(String path, Object value) {
		return new OutputEntry(path, value, true, 0);
	}

	public static InputEntry dbInEnt(String path, Object value, URL datasoreURL) {
		return new InputEntry(path, value, true, datasoreURL, 0);
	}

	public static InputEntry input(String path, Object value, int index) {
		return new InputEntry(path, value, index);
	}

	public static InputEntry in(String path, Object value, int index) {
		return new InputEntry(path, value, index);
	}

	public static InputEntry inout(String path) {
		return new InputEntry(path, null, 0);
	}

	public static InputEntry inout(String path, Object value) {
		return new InputEntry(path, value, 0);
	}

	public static InoutEntry inout(String path, Object value, int index) {
		return new InoutEntry(path, value, index);
	}

	public static <T> AnnotatedEntry<T> ent(String path, T value, String association) {
		return new AnnotatedEntry<T>(path, association, value);
	}

	//  }

	public static URL dbURL() throws MalformedURLException {
		return new URL(Sorcer.getDatabaseStorerUrl());
	}

	public static URL dsURL() throws MalformedURLException {
		return new URL(Sorcer.getDataspaceStorerUrl());
	}

	public static void dbURL(Object object, URL dbUrl)
			throws MalformedURLException {
		if (object instanceof Par)
			((Par) object).setDbURL(dbUrl);
		else if (object instanceof ServiceContext)
			((ServiceContext) object).setDbUrl("" + dbUrl);
		else
			throw new MalformedURLException("Can not set URL to: " + object);
	}

	public static URL dbURL(Object object) throws MalformedURLException {
		if (object instanceof Par)
			return ((Par) object).getDbURL();
		else if (object instanceof ServiceContext)
			return new URL(((ServiceContext) object).getDbUrl());
		return null;
	}

	public static URL storeArg(Object object) throws EvaluationException {
		URL dburl = null;
		try {
			if (object instanceof Evaluation) {
				Evaluation entry = (Evaluation)	object;
				Object obj = entry.asis();
				if (SdbUtil.isSosURL(obj))
					dburl = (URL) obj;
				else {
					if (entry instanceof Setter) {
						((Setter) entry).setPersistent(true);
						entry.getValue();
						dburl = (URL) entry.asis();
					}
				}
			}
		} catch (Exception e) {
			throw new EvaluationException(e);
		}	return dburl;
	}


	public static URL store(Object object) throws EvaluationException {
		try {
//			if (object instanceof UuidObject)
				return SdbUtil.store(object);
//			else  {
//				return SdbUtil.store(new UuidObject(object));
//			}
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
	}

	public static Object retrieve(URL url) throws IOException {
		return url.getContent();
	}

	public static URL update(Object object) throws ExertionException,
			SignatureException, ContextException {
		return SdbUtil.update(object);
	}

	public static List<String> list(URL url) throws ExertionException,
			SignatureException, ContextException {
		return SdbUtil.list(url);
	}

	public static List<String> list(DatabaseStorer.Store store) throws ExertionException,
			SignatureException, ContextException {
		return SdbUtil.list(store);
	}

	public static URL delete(Object object) throws ExertionException,
			SignatureException, ContextException {
		return SdbUtil.delete(object);
	}

	public static int clear(DatabaseStorer.Store type) throws ExertionException,
			SignatureException, ContextException {
		return SdbUtil.clear(type);
	}

	public static int size(DatabaseStorer.Store type) throws ExertionException,
			SignatureException, ContextException {
		return SdbUtil.size(type);
	}


}