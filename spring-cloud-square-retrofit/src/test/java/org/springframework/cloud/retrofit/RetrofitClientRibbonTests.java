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

package org.springframework.cloud.retrofit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.retrofit.test.Hello;
import org.springframework.cloud.retrofit.test.HelloController;
import org.springframework.cloud.retrofit.test.LocalRibbonClientConfiguration;
import org.springframework.cloud.retrofit.test.LoggingRetrofitConfig;
import org.springframework.cloud.square.okhttp.OkHttpRibbonInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest( properties = { "spring.application.name=retrofitclientribbontest",
				"logging.level.org.springframework.cloud.retrofit=DEBUG",
				"retrofit.reactor.enabled=false",
		 }, webEnvironment = RANDOM_PORT)
@DirtiesContext
public class RetrofitClientRibbonTests {

	protected static final String HELLO_WORLD_1 = "hello world 1";

	@Autowired
	private TestClient testClient;

	@Autowired
	private RetrofitContext retrofitContext;

	@RetrofitClient(name = "localapp")
	protected interface TestClient {
		@GET("/hello")
		Call<Hello> getHello();
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@EnableRetrofitClients(clients = TestClient.class, defaultConfiguration = LoggingRetrofitConfig.class)
	@RibbonClient(name = "localapp", configuration = LocalRibbonClientConfiguration.class)
	@SuppressWarnings("unused")
	protected static class Application extends HelloController {
		@Bean
		@LoadBalanced
		public OkHttpClient.Builder builder() {
			return new OkHttpClient.Builder();
		}
	}

	@Test
	public void testOkHttpInterceptor() {
		Retrofit retrofit = this.retrofitContext.getInstance("localapp", Retrofit.class);
		assertThat(retrofit).isNotNull();
		okhttp3.Call.Factory callFactory = retrofit.callFactory();
		assertThat(callFactory).isInstanceOf(OkHttpClient.class);
		OkHttpClient client = (OkHttpClient) callFactory;
		assertThat(client.interceptors()).hasAtLeastOneElementOfType(OkHttpRibbonInterceptor.class);
	}

	@Test
	public void testSimpleType() throws Exception {
		Response<Hello> response = this.testClient.getHello().execute();
		assertThat(response).isNotNull();
		assertThat(response.isSuccessful()).as("checks response successful, code %d", response.code()).isTrue();
		assertThat(response.body()).isEqualTo(new Hello(HELLO_WORLD_1));
	}

}
