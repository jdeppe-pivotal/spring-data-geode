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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.admin.GemfireAdminOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = EnableAutoDeployIntegrationTest.TestConfiguration.class)
public class EnableAutoDeployIntegrationTest {

  @Autowired
  public GemfireAdminOperations adminOperations;

  @ClientCacheApplication
  @EnableAutoDeploy(anchors = {TestConfiguration.class})
  @EnableClusterConfiguration
  public static class TestConfiguration {

    @Bean
    GemfireAdminOperations mockGemfireAdminOperations() {
      return mock(GemfireAdminOperations.class);
    }
  }

  @Test
  public void checkClassesDirJarIsCreated() {
    assertThat(adminOperations).isNotNull();

    ArgumentCaptor<List<String>> jarCaptor = ArgumentCaptor.forClass(List.class);
    verify(adminOperations, times(1)).deploy(jarCaptor.capture());

    List<String> jars = jarCaptor.getValue();
    assertThat(jars.stream().anyMatch(x -> x.contains("test-classes-dir.jar"))).isTrue();
  }

}
