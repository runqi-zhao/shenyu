/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shenyu.plugin.wasm;

import org.apache.shenyu.common.dto.RuleData;
import org.apache.shenyu.common.dto.SelectorData;
import org.apache.shenyu.plugin.api.ShenyuPluginChain;
import org.apache.shenyu.plugin.base.AbstractShenyuPlugin;
import org.apache.shenyu.wasm.Instance;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractWasmPlugin extends AbstractFilteredShenyuWasmPlugin {

    protected final Instance instance;

    public AbstractWasmPlugin() throws IOException, URISyntaxException {
        // locate `.wasm` lib.
        final Class<? extends AbstractWasmPlugin> clazz = this.getClass();
        Path wasmPath = Paths.get(clazz.getClassLoader().getResource(clazz.getName() + ".wasm").toURI());

        // Reads the WebAssembly module as bytes.
        byte[] wasmBytes = Files.readAllBytes(wasmPath);

        // Instantiates the WebAssembly module.
        this.instance = new Instance(wasmBytes);
        Runtime.getRuntime().addShutdownHook(new Thread(this.instance::close));
    }

    protected Mono<Void> doMethod1(final ServerWebExchange exchange, final ShenyuPluginChain chain, final SelectorData selector, final RuleData rule) {
        this.instance.exports.getFunction("method1");
        this.doWasmInvoker(exchange, chain, selector, rule, "");
        return chain.execute(exchange);
    }

    protected Mono<Void> doMethod2(final ServerWebExchange exchange, final ShenyuPluginChain chain, final SelectorData selector, final RuleData rule) {
        this.instance.exports.getFunction("method2");
        this.doWasmInvoker(exchange, chain, selector, rule, "");
        return chain.execute(exchange);
    }
}
