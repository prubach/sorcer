package sorcer.service;
/**
 *
 * Copyright 2013 Rafał Krupiński.
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


import java.io.Serializable;
import java.util.Arrays;

/**
 * Extracted from sorcer.service.Signature by Rafał Krupiński
 *
 * @author Mike Sobolewski
 */
public class ReturnPath<T> implements Serializable, Arg {
    static final long serialVersionUID = 6158097800741638834L;
    public String path;
    public Direction direction;
    public String[] argPaths;
    public Class<T> type;

    public ReturnPath() {
        //return the context
        path = "self";
    }

    public ReturnPath(String path, String... argPaths) {
        this.path = path;
        if (argPaths != null && argPaths.length > 0) {
            this.argPaths = argPaths;
            direction = Direction.OUT;
        }
    }

    public ReturnPath(String path, Direction direction, String... argPaths) {
        this.path = path;
        this.argPaths = argPaths;
        this.direction = direction;
    }

    public ReturnPath(String path, Direction direction, Class<T> returnType, String... argPaths) {
        this.path = path;
        this.direction = direction;
        this.argPaths = argPaths;
        type = returnType;
    }

    public String getName() {
        return path;
    }

    public String toString() {
        String params = "";
        if (argPaths != null)
            params = " argPaths: " + Arrays.toString(argPaths);
        return path + (direction != null ? " direction: " + direction : "")
            + params;
    }
}
