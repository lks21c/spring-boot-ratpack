/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ratpack.spring.groovy.internal;

import groovy.lang.Closure;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ratpack.func.Action;
import ratpack.groovy.Groovy;
import ratpack.groovy.guice.internal.DefaultGroovyBindingsSpec;
import ratpack.groovy.handling.internal.DefaultGroovyChain;
import ratpack.groovy.internal.ClosureUtil;
import ratpack.guice.BindingsSpec;
import ratpack.handling.Chain;
import ratpack.server.ServerConfig;
import ratpack.spring.config.RatpackServerCustomizerAdapter;

/**
 * @author Dave Syer
 *
 */
@Component
public class RatpackScriptServerCustomizer extends RatpackServerCustomizerAdapter {

	@Autowired(required = false)
	private GroovyRatpackSource source;

	private RatpackImpl ratpack = new RatpackImpl();

	protected interface GroovyRatpackSource {
		Closure<?> getRatpack();
	}

	static class RatpackImpl implements Groovy.Ratpack {

		private Closure<?> handlersConfigurer;
		private Closure<?> bindingsConfigurer;
		private Closure<?> serverConfigurer;

		@Override
		public void handlers(Closure<?> handlersConfigurer) {
			this.handlersConfigurer = handlersConfigurer;
		}

		@Override
		public void serverConfig(Closure<?> serverConfigurer) {
			this.serverConfigurer = serverConfigurer;
		}

		@Override
		public void bindings(Closure<?> bindingsConfigurer) {
			this.bindingsConfigurer = bindingsConfigurer;
		}

	}

	@Override
	public List<Action<Chain>> getHandlers() {

		if (source == null) {
			return super.getHandlers();
		}

		ClosureUtil.configureDelegateFirst(ratpack, source.getRatpack());

		return Collections.<Action<Chain>> singletonList(chain -> ClosureUtil
				.configureDelegateFirst(new DefaultGroovyChain(chain),
						ratpack.handlersConfigurer));

	}

	@Override
	public Action<BindingsSpec> getBindings() {

		if (source == null) {
			return super.getBindings();
		}

		ClosureUtil.configureDelegateFirst(ratpack, source.getRatpack());

		return binding -> ClosureUtil.delegatingAction(ratpack.bindingsConfigurer)
				.execute(new DefaultGroovyBindingsSpec(binding));

	}

	@Override
	public Action<ServerConfig.Builder> getServerConfig() {

		if (source == null) {
			return super.getServerConfig();
		}
		
		ClosureUtil.configureDelegateFirst(ratpack, source.getRatpack());

		return serverConfig -> ClosureUtil.delegatingAction(ratpack.serverConfigurer)
				.execute(serverConfig);
	}
}
