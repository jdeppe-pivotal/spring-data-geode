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

package org.springframework.data.gemfire;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Set;

import org.junit.Test;

/**
 * @author Jens Deppe
 */
public class ClassDependencyResolverTest {

	@Test
	public void objectDoesNotProduceDependencies() throws Exception {
		ClassDependencyResolver resolver = new ClassDependencyResolver();
		resolver.withClasses(Object.class);

		Set<URI> uris = resolver.process(false);
		assertThat(uris).hasSize(0);
	}

	@Test
	public void servletHasOneDependency() throws Exception {
		ClassDependencyResolver resolver = new ClassDependencyResolver();
		resolver.withClasses(javax.servlet.Servlet.class);

		Set<URI> uris = resolver.process(false);
		assertThat(uris).hasSize(1);
	}

	@Test
	public void cacheDependsAtLeastOnGeodeCore() throws Exception {
		ClassDependencyResolver resolver = new ClassDependencyResolver();
		resolver.withClasses(org.apache.geode.cache.Cache.class);

		Set<URI> uris = resolver.process(false);
		assertThat(uris.stream().anyMatch(x -> x.toString().contains("geode-core"))).isTrue();
	}
}
