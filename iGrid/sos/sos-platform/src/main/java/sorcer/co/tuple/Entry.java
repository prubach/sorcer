/*
 * Copyright 2012 the original author or authors.
 * Copyright 2012 SorcerSoft.org.
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

import sorcer.service.Context;
import sorcer.service.Parameter;

public class Entry<T1, T2> extends Tuple2<T1, T2> implements Parameter {

	public int index;
	
	public Entry() {
	};

	public Entry(T1 path, T2 value, int index) {
		T2 v = value;
		if (v == null)
			v = (T2)Context.Value.NULL;

		_1 = path;
		this._2 = v;
		this.index = index;
	}

	public T1 key() {
		return _1;
	}

	public T2 value() {
		return _2;
	}

	public int idex() {
		return index;
	}
	
	@Override
	public String toString() {
		return _1 + ":" + _2 + ":" + index;
	}
}