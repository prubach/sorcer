package sorcer.config;
/*
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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

import sorcer.core.service.IServiceBuilder;

/**
 * @author Rafał Krupiński
 */
public interface BeanListener {
    /**
     * preprocess service builder before creating the bean
     *
     */
    <T> void preProcess(IServiceBuilder<T> serviceBuilder);

    /**
     * preprocess the bean
     *
     */
    <T> void preProcess(IServiceBuilder<T> serviceBuilder, T bean);

    public <T> void destroy(IServiceBuilder<T> serviceBuilder, T bean);
}
