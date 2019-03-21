/*
 * Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.retrofit.support;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.retrofit.test.DefinedPortTests;
import org.springframework.cloud.retrofit.test.Hello;
import org.springframework.cloud.retrofit.test.HelloController;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "spring.application.name=retrofitnoannotationtest",
				"logging.level.org.springframework.cloud.retrofit=DEBUG",
				"retrofit.reactor.enabled=false",
				"okhttp.ribbon.enabled=false",
		 }, webEnvironment = DEFINED_PORT)
@DirtiesContext
public class RetrofitNoAnnotationTests extends DefinedPortTests {

	protected static final String HELLO_WORLD_1 = "hello world 1";

	@Autowired
	private TestClient testClient;

	protected interface TestClient {
		@GET("/hello")
		Call<Hello> getHello();
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@SuppressWarnings("unused")
	protected static class Application extends HelloController {

		@Value("${server.port}")
		private int port;

		@Bean
		public SpringConverterFactory springConverterFactory(ConversionService conversionService,
															 ObjectFactory<HttpMessageConverters> messageConverters) {
			return new SpringConverterFactory(messageConverters, conversionService);
		}

		@Bean
		public TestClient testClient(SpringConverterFactory springConverterFactory) {
			HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
			interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

			return new Retrofit.Builder().baseUrl("http://localhost:"+port)
					.client(new OkHttpClient.Builder().addInterceptor(interceptor).build())
					.addConverterFactory(springConverterFactory)
					.build()
					.create(TestClient.class);
		}

	}

	@Test
	public void testSimpleType() throws Exception {
		Response<Hello> response = this.testClient.getHello().execute();
		assertThat(response).isNotNull();
		assertThat(response.isSuccessful()).as("checks response successful, code %d", response.code()).isTrue();
		assertThat(response.body()).isEqualTo(new Hello(HELLO_WORLD_1));
	}

}
