package org.apache.shenyu.plugin.wasm;

import org.apache.shenyu.common.dto.RuleData;
import org.apache.shenyu.common.dto.SelectorData;
import org.apache.shenyu.plugin.api.ShenyuPluginChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.net.URISyntaxException;

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
        //TODO:使用selector与rule
        Mono<Void> execute = this.execute(exchange, chain);
        return execute;
    }



}
