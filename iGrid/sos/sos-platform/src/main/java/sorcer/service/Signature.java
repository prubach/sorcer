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

package sorcer.service;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A service <code>Signature</code> is an indirect behavioral feature of
 * {@link Exertion}s that declares a service that can be performed by instances
 * of {@link Servicer}s. It contains a service type and a selector of operation
 * of that service type (interface). Its implicit parameter and return value is
 * a service {@link Context}. Thus, the explicit signature of service-oriented
 * operations is defined by the same {@link Context} type for any exertion
 * parameter and return value . A signature may include a collection of optional
 * attributes describing a preferred {@link Servicer} with a given service type.
 * Also a signature can carry own implementation when its type is implemented
 * with the provided codebase.
 * <p>
 * In other words, a service signature is a specification of a service that can
 * be requested dynamically at the boundary of a service provider. Operations
 * include modifying a service {@link Context} or disclosing information about
 * the service dataContext.
 * 
 * @author Mike Sobolewski
 */
@SuppressWarnings("rawtypes")
public interface Signature extends Serializable, Parameter {

	/**
	 * Returns an operation name of this signature.
	 * 
	 * @return name of signature
	 */
	public String getSelector();
	
	/**
	 * Returns a service provider name.
	 * 
	 * @return name of service provider
	 */
	public String getProviderName();
	
	public void setProviderName(String providerName);
	
	/**
	 * Returns a service type name of this signature.
	 * 
	 * @return name of service interface
	 */
	public Class<?> getServiceType();

	/**
	 * Assigns a path to the return value by this signature.
	 * 
	 * @param path to the return value
	 */
	public void setReturnPath(ReturnPath path);

	/**
	 * Returns a path to the return value by this signature.
	 * 
	 * @return path to the return value
	 */
	public ReturnPath getReturnPath();

	/**
	 * Assigns a service type name of this signature.
	 * 
	 * @return name of service interface
	 * @param serviceType name of service interface
	 */
	public void setServiceType(Class<?> serviceType);

	/**
	 * Returns a signature type of this signature.
	 * 
	 * @return a type of this signature
	 */
	public Type getType();

	/**
	 * Assigns a signature <code>type</code> for this service signature.
	 * 
	 * @param type
	 *            a signature type
	 */
	public void setType(Signature.Type type);
	
	/**
	 * Returns a codebase for the code implementing this signature. The codebase
	 * is a space separated string (list) of URls.
	 * 
	 * @return a codebase for the code implementing this signature
	 */
	public String getCodebase();

	/**
	 * Assigns a codbase to <code>urls</code> for the code implementing this
	 * signature. The codebase is a space separated string (list) of URls.
	 * 
	 * @param urls
	 *            a list of space separated URLS
	 */
	public void setCodebase(String urls);
	
	public static class ReturnPath<T> implements Serializable, Parameter {
		static final long serialVersionUID = 6158097800741638834L;
		public String path;
		public Direction direction;
		public String[] argPaths;
		public Class<T> type;
		
		public ReturnPath() {}
		
		public ReturnPath(String path, String... argPaths) {
			this.path = path;
			if (argPaths != null && argPaths.length > 0) {
				this.argPaths = argPaths;
				direction = Direction.OUT;
			}
		}
		
		public ReturnPath(String path, Direction direction, String...  argPaths) {
			this.path = path;
			this.argPaths = argPaths;
			this.direction = direction;
		}
		
		public ReturnPath(String path, Direction direction, Class<T> returnType, String...  argPaths) {
			this.path = path;
			this.direction = direction;
			this.argPaths = argPaths;
			type = returnType;
		}
		
		public String toString() {
			String params = "";
			if (argPaths != null) 
				 params = " argPaths: " + Arrays.toString(argPaths);
			return path + (direction != null ? " direction: " + direction : "") 
				+ params;
		}
	}
	
	/**
	 * There are four types of {@link Signature} operations that can be
	 * associated with signatures: <code>PREPROCESS</code>, <code>PROCESS</code>
	 * , <code>POSTPROCESS</code>, and <code>APPEND</code> signature. Only one
	 * <code>PROCESS</code> signature can be associated with any exertion. The
	 * <code>PROCESS</code> signature defines an executing provider dynamically
	 * bounded in runtime.
	 */
	public enum Type {
		SRV, PRE, POST, APD
	}
	
	public enum Direction { IN, OUT, INOUT };

	static final Type SRV = Type.SRV; 
	static final Type PRE = Type.PRE;
	static final Type POST = Type.POST;
	static final Type APD = Type.APD;
	
	// integers are used to persist types in the datastore
	static final int PREPROCESS_CD = 1; 
	static final int PROCESS_CD = 2; 
	static final int POSTPROCESS_CD = 3; 
	static final int APPEND_CD = 4;
}
