package sorcer.schema;
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


import sorcer.service.Direction;

import java.io.Serializable;

/**
 * @author Rafał Krupiński
 */
public class SchemaEntry implements Serializable {
    private static final long serialVersionUID = -809999449914128883L;
    public String path;
    public Direction direction;
    public boolean required;
    public Class type;

    public SchemaEntry() {
    }

    public SchemaEntry(Class type, String path, Direction direction, boolean required) {
        if (type == null) throw new IllegalArgumentException("type is null");
        if (path == null) throw new IllegalArgumentException("path is null");
        if (direction == null) throw new IllegalArgumentException("direction is null");
        this.type = type;
        this.path = path;
        this.direction = direction;
        this.required = required;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null && obj == this) || (obj instanceof SchemaEntry && equals((SchemaEntry) obj));
    }

    protected boolean equals(SchemaEntry entry) {
        return type.equals(entry.type) && path.equals(entry.path) && direction.equals(entry.direction) && required == entry.required;
    }

    @Override
    public int hashCode() {
        return type.hashCode() + 13 * path.hashCode() + 17 * direction.ordinal() + 31 * (required ? 1 : 0);
    }

    @Override
    public String toString() {
        return (required ? "required" : "optional") + " " + type.getName() + " " + path + " " + direction;
    }
}
