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

import static org.springframework.data.gemfire.util.CollectionUtils.asSet;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.gemfire.config.annotation.EnableAutoDeploy;
import org.springframework.data.gemfire.config.schema.SchemaObjectDefiner;
import org.springframework.data.gemfire.config.schema.SchemaObjectType;
import org.springframework.data.gemfire.config.schema.definitions.DeploymentDefinition;

/**
 * The {@link DeploymentDefiner} produces a {@link DeploymentDefinition} encapsulating the jars
 * to be deployed as a result of specifying {@link EnableAutoDeploy}.
 *
 * @author Jens Deppe
 */
public class DeploymentDefiner implements SchemaObjectDefiner {

  @Override
  public Set<SchemaObjectType> getSchemaObjectTypes() {
    return asSet(SchemaObjectType.DEPLOYMENT);
  }

  @Override
  public Optional<DeploymentDefinition> define(Object schemaObject) {
    return Optional.ofNullable(schemaObject).filter(this::canDefine)
        .map(it -> DeploymentDefinition.from((DeploymentCollection) it));
  }
}
