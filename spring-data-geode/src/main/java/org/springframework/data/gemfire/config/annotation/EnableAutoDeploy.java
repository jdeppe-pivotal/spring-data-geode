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

package org.springframework.data.gemfire.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * The {@link EnableAutoDeploy @EnableAutoDeploy} annotation provides the ability for application classes to
 * automatically be deployed to the cluster members. This obviates the need to perform explicit
 * <i>{@code gfsh deploy --jar...}</i> operations. This functionality is enabled by providing an
 * {@link EnableAutoDeploy#anchors()} point from which all necessary dependent classes (specifically
 * jars containing those classes) are inferred. Classes that are discovered within directories
 * on the filesystem are bundled into temporary zip files containing the whole directory contents.
 * <p/>
 * This capability must be used in conjunction with {@link EnableClusterConfiguration}.
 *
 * @author Jens Deppe
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(AutoDeploymentConfiguration.class)
public @interface EnableAutoDeploy {

  /**
   * The anchor classes from which all necessary dependencies are inferred.
   * @return
   */
  Class<?>[] anchors();

}

