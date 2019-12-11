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

package org.springframework.data.gemfire.config.schema.definitions;

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalArgumentException;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.gemfire.config.admin.GemfireAdminOperations;
import org.springframework.data.gemfire.config.schema.SchemaObjectDefinition;
import org.springframework.data.gemfire.config.schema.SchemaObjectType;
import org.springframework.data.gemfire.config.schema.support.DeploymentCollection;

/**
 * A {@link DeploymentDefinition} encapsulates the configuration meta-data used to define a
 * collection of jars to be deployed.
 *
 * @author Jens Deppe
 */
public class DeploymentDefinition extends SchemaObjectDefinition {

  /**
   * Ensure that classes are deployed before regions which may, (in the future), have dependencies
   * on these classes.
   */
  protected static final int ORDER = RegionDefinition.ORDER - 1;

  private final DeploymentCollection deploymentCollection;

  public static DeploymentDefinition from(DeploymentCollection collection) {
    return new DeploymentDefinition(collection);
  }

  protected DeploymentDefinition(DeploymentCollection collection) {
    super(Optional.ofNullable(collection).map(DeploymentCollection::getName)
        .orElseThrow(() -> newIllegalArgumentException("DeploymentCollection is required")));

    this.deploymentCollection = collection;
  }

  @Override
  public SchemaObjectType getType() {
    return SchemaObjectType.DEPLOYMENT;
  }

  @Override
  public int getOrder() {
    return ORDER;
  }

  @Override
  public void create(GemfireAdminOperations gemfireAdminOperations) {
    gemfireAdminOperations.deploy(deploymentCollection.getJars().stream()
        .map(URI::getPath).collect(Collectors.toList()));
  }
}
