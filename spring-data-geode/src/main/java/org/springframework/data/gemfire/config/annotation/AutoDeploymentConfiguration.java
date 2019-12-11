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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.gemfire.ClassDependencyResolver;
import org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport;
import org.springframework.data.gemfire.config.support.AbstractSmartLifecycle;
import org.springframework.data.gemfire.util.CacheUtils;

import org.apache.geode.cache.GemFireCache;

/**
 * A {@link Configuration @Configuration} class which performs class dependency resolution on behalf
 * of {@link EnableAutoDeploy @EnableAutoDeploy}.
 *
 * @author Jens Deppe
 */
@Configuration
public class AutoDeploymentConfiguration extends AbstractAnnotationConfigSupport
    implements ImportAware {

  private static final String ANCHORS_ATTRIBUTE_NAME = "anchors";

  private List<Class<?>> anchorClassNames;

  private String referencingClassName;

  @Override
  protected Class<? extends Annotation> getAnnotationType() {
    return EnableAutoDeploy.class;
  }

  private void setDeploy(Class<?>[] anchorClassNames) {
    this.anchorClassNames = Arrays.asList(anchorClassNames);
  }

  @Override
  public void setImportMetadata(AnnotationMetadata importMetadata) {

    if (isAnnotationPresent(importMetadata)) {

      AnnotationAttributes enableAutoDeployAttributes =
          getAnnotationAttributes(importMetadata);

      Class<?>[] anchorClasses = enableAutoDeployAttributes.getClassArray(ANCHORS_ATTRIBUTE_NAME);
      setDeploy(resolveProperty(managementProperty(ANCHORS_ATTRIBUTE_NAME), Class[].class,  anchorClasses));
      referencingClassName = importMetadata.getClassName();
    }
  }

  @Bean
  public AutoDeploymentInitializer gemfireAutoDeploymentInitializer(GemFireCache gemfireCache) {

    return Optional.ofNullable(gemfireCache)
        .filter(CacheUtils::isClient)
        .map(clientCache -> new AutoDeploymentInitializer(referencingClassName, anchorClassNames))
        .orElse(null);
  }

  public static class AutoDeploymentInitializer extends AbstractSmartLifecycle {

    private final List<Class<?>> deploymentAnchorClasses;

    private final String referencingClassName;

    private List<URI> deploymentJars;

    public AutoDeploymentInitializer(String referencingClassName, List<Class<?>> deploymentAnchorClasses) {
      this.referencingClassName = referencingClassName;
      this.deploymentAnchorClasses = deploymentAnchorClasses;
    }

    @Override
    public void start() {
      ClassDependencyResolver resolver = new ClassDependencyResolver()
          .withClasses(deploymentAnchorClasses);
      Set<URI> jarUris;

      try {
        jarUris = resolver.process(true);
      } catch (IOException ioex) {
        // TODO: should this be handled here?
        throw new UncheckedIOException(ioex);
      }

      deploymentJars = new ArrayList<>(jarUris);
    }

    public String getReferencingClassName() {
      return referencingClassName;
    }

    public List<URI> getDeploymentJars() {
      return deploymentJars;
    }

    @Override
    public boolean isAutoStartup() {
      return true;
    }

    @Override
    public int getPhase() {
      return Integer.MIN_VALUE;
    }

  }
}

