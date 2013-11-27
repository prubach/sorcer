package sorcer.config;
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


import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Rafał Krupiński
 */
public class ConfigurerTest {
    Configurer configurer = new Configurer();
    Configuration config;

    public ConfigurerTest() throws ConfigurationException {
        config = ConfigurationProvider.getInstance(new String[]{"classpath:/sorcer/config/ConfigureTest.config"});
    }

    @Test
    public void testConfigure() throws Exception {
        ConfigureTest o = new ConfigureTest();
        assertNotNull("Configuration problem", config.getEntry("sorcer.core.provider.ServiceProvider", "boolValue", Boolean.TYPE));
        configurer.process(o, config);
        assertEquals(true, o.value);
        assertEquals(true, o.value2);
    }

/*
    TODO RKR add support for optional entries
    @Test(expected = IllegalArgumentException.class)
    public void testWrongType() throws Exception {
        Object o = new WrongType();
        configurer.process(o,config);
    }
*/
}

@Component
class ConfigureTest {
    boolean value;
    Boolean value2;

    @ConfigEntry("boolValue")
    void setBool(boolean value) {
        this.value = value;
    }

    @ConfigEntry("booleanValue")
    void setBoolean(Boolean value) {
        value2 = value;
    }
}

@Component
class WrongType{
    @ConfigEntry("booleanValue")
    void setString(String s){}
}