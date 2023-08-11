package org.apache.shenyu.plugin.wasm.handler;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shenyu.common.constant.Constants;
import org.apache.shenyu.common.dto.MetaData;
import org.apache.shenyu.common.dto.PluginData;
import org.apache.shenyu.common.dto.RuleData;
import org.apache.shenyu.common.dto.SelectorData;
import org.apache.shenyu.common.dto.convert.plugin.GrpcRegisterConfig;
import org.apache.shenyu.common.dto.convert.rule.impl.WasmRuleHandle;
import org.apache.shenyu.common.dto.convert.selector.WasmUpstream;
import org.apache.shenyu.common.enums.PluginEnum;
import org.apache.shenyu.common.utils.GsonUtils;
import org.apache.shenyu.common.utils.Singleton;
import org.apache.shenyu.loadbalancer.cache.UpstreamCacheManager;
import org.apache.shenyu.loadbalancer.entity.Upstream;
import org.apache.shenyu.plugin.base.cache.CommonHandleCache;
import org.apache.shenyu.plugin.base.cache.MetaDataCache;
import org.apache.shenyu.plugin.base.handler.PluginDataHandler;
import org.apache.shenyu.plugin.base.utils.BeanHolder;
import org.apache.shenyu.plugin.base.utils.CacheKeyUtils;
import org.apache.shenyu.wasm.Instance;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class WasmPluginDataHandler implements PluginDataHandler {

    protected final Instance instance;

    private static final ConcurrentMap<String, MetaData> META_DATA = Maps.newConcurrentMap();

    public static final Supplier<CommonHandleCache<String, WasmRuleHandle>> CACHED_HANDLE = new BeanHolder<>(CommonHandleCache::new);

    public WasmPluginDataHandler() throws IOException, URISyntaxException {
        // locate `.wasm` lib.
        final Class<? extends WasmPluginDataHandler> clazz = this.getClass();
        Path wasmPath = Paths.get(clazz.getClassLoader().getResource(clazz.getName() + ".wasm").toURI());

        // Reads the WebAssembly module as bytes.
        byte[] wasmBytes = Files.readAllBytes(wasmPath);

        // Instantiates the WebAssembly module.
        this.instance = new Instance(wasmBytes);
        Runtime.getRuntime().addShutdownHook(new Thread(this.instance::close));
    }

    @Override
    public void handlerSelector(final SelectorData selectorData) {
        Object[] apply = this.instance.exports.getFunction("method1").apply(selectorData);
        for (int i = 0; i < apply.length; i++) {
            SelectorData serviceConfig = (SelectorData) apply[i];
            List<WasmUpstream> upstreamList = GsonUtils.getInstance().fromList(serviceConfig.getHandle(), WasmUpstream.class);
            if (CollectionUtils.isEmpty(upstreamList)) {
                return;
            }
            UpstreamCacheManager.getInstance().submit(selectorData.getId(), convertUpstreamList(upstreamList));
            // the update is also need to clean, but there is no way to
            // distinguish between crate and update, so it is always clean
            MetaDataCache.getInstance().clean();
            if (!selectorData.getContinued()) {
                CACHED_HANDLE.get().cachedHandle(CacheKeyUtils.INST.getKey(selectorData.getId(), Constants.DEFAULT_RULE), WasmRuleHandle.newInstance());
            }

        }
    }

    @Override
    public void removeSelector(final SelectorData selectorData) {
        Object[] apply = this.instance.exports.getFunction("method1").apply(selectorData);
        for (int i = 0; i < apply.length; i++) {
            SelectorData serviceConfig = (SelectorData) apply[i];
            UpstreamCacheManager.getInstance().removeByKey(serviceConfig.getId());
            MetaDataCache.getInstance().clean();
            CACHED_HANDLE.get().removeHandle(CacheKeyUtils.INST.getKey(serviceConfig.getId(), Constants.DEFAULT_RULE));
        }
    }

    @Override
    public void handlerRule(final RuleData ruleData) {
        Object[] apply = this.instance.exports.getFunction("method1").apply(ruleData);
        for (int i = 0; i < apply.length; i++) {
            RuleData ruleConfig = (RuleData) apply[i];
            Optional.ofNullable(ruleData.getHandle()).ifPresent(s -> {
                WasmRuleHandle wasmRuleHandle = GsonUtils.getInstance().fromJson(s, WasmRuleHandle.class);
                CACHED_HANDLE.get().cachedHandle(CacheKeyUtils.INST.getKey(ruleConfig), wasmRuleHandle);
                // the update is also need to clean, but there is no way to
                // distinguish between crate and update, so it is always clean
                MetaDataCache.getInstance().clean();
            });
        }
    }

    @Override
    public void removeRule(final RuleData ruleData) {
        Object[] apply = this.instance.exports.getFunction("method1").apply(ruleData);
        for (int i = 0; i < apply.length; i++) {
            RuleData ruleConfig = (RuleData) apply[i];
            Optional.ofNullable(ruleConfig.getHandle()).ifPresent(s -> CACHED_HANDLE.get().removeHandle(CacheKeyUtils.INST.getKey(ruleData)));
            MetaDataCache.getInstance().clean();
        }
    }

    @Override
    public String pluginNamed() {
        return PluginEnum.WASM.getName();
    }

    private List<Upstream> convertUpstreamList(final List<WasmUpstream> upstreamList) {
        return upstreamList.stream().map(u -> Upstream.builder()
                .protocol(u.getProtocol())
                .url(u.getUpstreamUrl())
                .status(u.isStatus())
                .timestamp(u.getTimestamp())
                .build()).collect(Collectors.toList());
    }
}
