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

import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.retrofit.test.DefinedPortTests;
import org.springframework.cloud.retrofit.test.Hello;
import org.springframework.cloud.retrofit.test.HelloController;
import org.springframework.cloud.retrofit.test.LoggingRetrofitConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.jakewharton.retrofit2.adapter.reactor.ReactorCallAdapterFactory;
import com.jakewharton.retrofit2.adapter.reactor.Result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

import okhttp3.OkHttpClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "spring.application.name=retrofitclientreactortest",
				"logging.level.org.springframework.cloud.retrofit=DEBUG",
				"okhttp.ribbon.enabled=false"
		 }, webEnvironment = DEFINED_PORT)
@DirtiesContext
public class RetrofitClientReactorTests extends DefinedPortTests {

	@Autowired
	private TestClient testClient;

	@Autowired
	private RetrofitContext retrofitContext;

	protected enum Arg {
		A, B;

		@Override
		public String toString() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}

	protected static class OtherArg {
		public final String value;

		public OtherArg(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return this.value;
		}
	}


	@RetrofitClient(name = "localapp", url = "${retrofit.client.url.tests.url}")
	protected interface TestClient {
		@GET("/hello")
		Call<Hello> getHello();

		@GET("/hello")
		Mono<Hello> getHelloMono();

		@GET("/hello")
		Mono<Response<Hello>> getHelloMonoResponse();

		@GET("/hello")
		Mono<Result<Hello>> getHelloMonoResult();

		//TODO: get working with boot reactive
		@GET("/hello")
		Flux<Hello> getHelloFlux();
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@EnableRetrofitClients(clients = { TestClient.class, },
			defaultConfiguration = LoggingRetrofitConfig.class)
	@SuppressWarnings("unused")
	protected static class Application extends HelloController {
		@Bean
		public OkHttpClient.Builder builder() {
			return new OkHttpClient.Builder();
		}
	}

	@Test
	public void testCallAdapterFactory() {
		Retrofit retrofit = this.retrofitContext.getInstance("localapp", Retrofit.class);
		assertThat(retrofit).isNotNull();
		assertThat(retrofit.callAdapterFactories())
				.hasAtLeastOneElementOfType(ReactorCallAdapterFactory.class);
	}

	@Test
	public void testSimpleType() throws Exception {
		Response<Hello> response = this.testClient.getHello().execute();
		assertThat(response).isNotNull();
		assertThat(response.isSuccessful()).as("checks response successful, code %d", response.code()).isTrue();
		assertThat(response.body()).isEqualTo(new Hello(HELLO_WORLD_1));
	}

	@Test
	public void testMono() throws Exception {
		Mono<Hello> response = this.testClient.getHelloMono();
		assertThat(response).isNotNull();
		assertThat(response.block()).isEqualTo(new Hello(HELLO_WORLD_1));
	}

	@Test
	public void testMonoResponse() throws Exception {
		Mono<Response<Hello>> mono = this.testClient.getHelloMonoResponse();
		assertThat(mono).isNotNull();
		Response<Hello> response = mono.block();
		assertThat(response.isSuccessful()).as("checks response successful, code %d", response.code()).isTrue();
		assertThat(response.body()).isEqualTo(new Hello(HELLO_WORLD_1));
	}

	@Test
	public void testMonoResult() throws Exception {
		Mono<Result<Hello>> mono = this.testClient.getHelloMonoResult();
		assertThat(mono).isNotNull();
		Result<Hello> result = mono.block();
		assertThat(result.isError()).as("checks result in error, error %s", result.error()).isFalse();
		Response<Hello> response = result.response();
		assertThat(response.isSuccessful()).as("checks response successful, code %d", response.code()).isTrue();
		assertThat(response.body()).isEqualTo(new Hello(HELLO_WORLD_1));
	}

	@Test
	public void testFlux() throws Exception {
		Flux<Hello> flux = this.testClient.getHelloFlux();
		assertThat(flux).isNotNull();
		assertThat(flux.blockFirst()).isEqualTo(new Hello(HELLO_WORLD_1));
	}

}
