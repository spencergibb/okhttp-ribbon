/*
 * Copyright 2013-2015 the original author or authors.
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

package org.springframework.cloud.retrofit;

import org.springframework.cloud.context.named.NamedContextFactory;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Dave Syer
 */
class RetrofitClientSpecification implements NamedContextFactory.Specification {

	private String name;

	private Class<?>[] configuration;

	public RetrofitClientSpecification(String name, Class<?>[] configuration) {
		this.name = name;
		this.configuration = configuration;
	}

	public RetrofitClientSpecification() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Class<?>[] getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Class<?>[] configuration) {
		this.configuration = configuration;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RetrofitClientSpecification that = (RetrofitClientSpecification) o;
		return Objects.equals(name, that.name) &&
				Arrays.equals(configuration, that.configuration);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, configuration);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("RetrofitClientSpecification{");
		sb.append("name='").append(name).append('\'');
		sb.append(", configuration=").append(Arrays.toString(configuration));
		sb.append('}');
		return sb.toString();
	}
}
