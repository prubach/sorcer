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


import java.util.List;

/**
 * Exertion containing other ecertions: Job
 * @author Rafał Krupiński
 */
public interface ComplexExertion {
    boolean hasChild(String childName);

    Exertion getChild(String childName);

    int size();

    int indexOf(Exertion ex);

    void setExertionAt(Exertion ex, int i);

    ComplexExertion addExertion(Exertion ex);

    void addExertions(List<Exertion> exertions);

    void setExertions(List<Exertion> exertions);

    ComplexExertion addExertion(Exertion exertion, int priority);

    Exertion removeExertion(Exertion exertion);

    void removeExertionAt(int index);

    Exertion exertionAt(int index);

    List<Exertion> getExertions();

    List<Exertion> getExertions(List<Exertion> exs);

}
