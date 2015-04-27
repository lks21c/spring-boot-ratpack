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

package ratpack.spring.config.internal;

import ratpack.func.Action;
import ratpack.func.Function;
import ratpack.guice.BindingsSpec;
import ratpack.guice.Guice;
import ratpack.handling.Handler;
import ratpack.handling.Handlers;
import ratpack.handling.internal.ClientErrorForwardingHandler;
import ratpack.registry.Registry;
import ratpack.server.ServerConfig;
import ratpack.spring.groovy.internal.RatpackScriptActionFactory;

import com.google.inject.Module;

/**
 * @author Dave Syer
 * 
 */
public class SpringBackedHandlerFactory implements Function<Registry, Handler> {

	@Override
	public Handler apply(Registry registry) throws Exception {
		Action<BindingsSpec> action = registry.get(RatpackScriptActionFactory.class)
				.getBindings();
		Registry guice = Guice.registry(bindings -> {
			for (Module module : registry.getAll(Module.class)) {
				bindings.add(module);
			}
			action.execute(bindings);
		}).apply(registry);
		return Handlers.chain(
				Handlers.chain(registry.get(ServerConfig.class), guice,
						registry.get(ChainConfigurers.class)),
				new ClientErrorForwardingHandler(404));
	}

}
