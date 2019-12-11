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

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.gemfire.config.annotation.AutoDeploymentConfiguration;
import org.springframework.data.gemfire.config.annotation.EnableAutoDeploy;
import org.springframework.data.gemfire.config.schema.SchemaObjectCollector;

/**
 * The {@link DeploymentCollection} class is an implementation of {@link SchemaObjectCollector} that
 * inspects the {@link ApplicationContext} in order to extract all the jars which need to be
 * deployed as a result of specifying {@link EnableAutoDeploy}.
 * @author Jens Deppe
 */
public class DeploymentCollector implements SchemaObjectCollector<DeploymentCollection> {

  @Override
  public List<DeploymentCollection> collectFrom(ApplicationContext applicationContext) {
    AutoDeploymentConfiguration.AutoDeploymentInitializer initializer;

    try {
      initializer = applicationContext.getBean(AutoDeploymentConfiguration.AutoDeploymentInitializer.class);
    } catch (NoSuchBeanDefinitionException nex) {
      return Collections.EMPTY_LIST;
    }

    return Collections.singletonList(
        new DeploymentCollection(initializer.getReferencingClassName(),
            initializer.getDeploymentJars()));
  }
}
