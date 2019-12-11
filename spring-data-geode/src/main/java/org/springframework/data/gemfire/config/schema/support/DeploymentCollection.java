/*
 * Copyright 2017-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.gemfire.config.schema.support;

import java.net.URI;
import java.util.List;

/**
 * The group of jars to be deployed as a unit.
 *
 * @author Jens Deppe
 */
public class DeploymentCollection {

  private final List<URI> jars;

  private final String name;

  public DeploymentCollection(String name, List<URI> jars) {
    this.name = name;
    this.jars = jars;
  }

  public String getName() {
    return name;
  }

  public List<URI> getJars() {
    return jars;
  }
}
