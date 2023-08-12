package org.apache.shenyu.plugin.wasm;

import org.apache.commons.lang3.StringUtils;
import org.apache.shenyu.common.dto.RuleData;
import org.apache.shenyu.common.dto.SelectorData;
import org.apache.shenyu.common.dto.convert.rule.RedirectHandle;
import org.apache.shenyu.common.dto.convert.rule.impl.WasmRuleHandle;
import org.apache.shenyu.common.utils.UriUtils;
import org.apache.shenyu.plugin.api.ShenyuPluginChain;
import org.apache.shenyu.plugin.base.utils.CacheKeyUtils;
import org.apache.shenyu.plugin.wasm.handler.WasmPluginDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * The type Abstract filtered shenyu wasm plugin.
 */
public class AbstractFilteredShenyuWasmPlugin extends AbstractWasmPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractFilteredShenyuWasmPlugin.class);

    public AbstractFilteredShenyuWasmPlugin() throws IOException, URISyntaxException {
        super();
    }

    /**
     * do dubbo invoker.
     *
     * @param exchange exchange the current server exchange {@linkplain ServerWebExchange}
     * @param chain    chain the current chain  {@linkplain ServerWebExchange}
     * @param param    the param
     * @return {@code Mono<Void>} to indicate when request handling is complete
     */
    protected Mono<Void> doWasmInvoker(final ServerWebExchange exchange,
                                        final ShenyuPluginChain chain,
                                        final SelectorData selector,
                                        final RuleData rule,
                                        final String param) {
        Object[] selectorData = this.instance.exports.getFunction("method1").apply(selector);
        Object[] ruleData = this.instance.exports.getFunction("method1").apply(rule);
        for (int i = 0; i < selectorData.length; i++) {
            SelectorData wasmSelector = (SelectorData) selectorData[i];
            String handle = wasmSelector.getHandle();
            WasmRuleHandle wasmRuleHandle = WasmPluginDataHandler.CACHED_HANDLE.get().obtainHandle(CacheKeyUtils.INST.getKey(rule));
            if (Objects.isNull(wasmRuleHandle)) {
                LOG.error("wasm rule can not configuration: {}", handle);
                return chain.execute(exchange);
            }
            ServerHttpResponse response = exchange.getResponse();
            //set response header
            return response.setComplete();
        }
        for (int i = 0; i < ruleData.length; i++) {
            RuleData wasmRule = (RuleData) ruleData[i];
            String handle = wasmRule.getHandle();
            WasmRuleHandle wasmRuleHandle = WasmPluginDataHandler.CACHED_HANDLE.get().obtainHandle(CacheKeyUtils.INST.getKey(rule));
            if (Objects.isNull(wasmRuleHandle)) {
                LOG.error("wasm rule can not configuration: {}", handle);
                return chain.execute(exchange);
            }
            ServerHttpResponse response = exchange.getResponse();
            //set response header
            return response.setComplete();
        }
        return chain.execute(exchange);
    }



}
