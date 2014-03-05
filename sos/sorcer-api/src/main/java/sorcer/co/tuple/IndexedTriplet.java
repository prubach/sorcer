/**
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Sorcersoft.com S.A.
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
package sorcer.co.tuple;

public class IndexedTriplet<T> extends Tuple3<String, T, Object> {

	public int index;
	
	public IndexedTriplet() {}
	
	public IndexedTriplet(Object extention) {
		_3 = extention;
	}
	
	public IndexedTriplet(String x1, T value) {
		_1 = x1;
		_2 = value;
	}

	public IndexedTriplet(String x1, T value, Object extention) {
		_1 = x1;
		_2 = value;
		_3 = extention;
	}
	
	public Object extention () {
		return _3;
	}
	
	public void ext(Object extention) {
		_3 = extention;
	}
}