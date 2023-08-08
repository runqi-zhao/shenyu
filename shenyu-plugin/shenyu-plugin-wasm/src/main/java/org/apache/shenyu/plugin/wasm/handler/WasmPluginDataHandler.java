package org.apache.shenyu.plugin.wasm.handler;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.shenyu.common.constant.Constants;
import org.apache.shenyu.common.dto.RuleData;
import org.apache.shenyu.common.dto.SelectorData;
import org.apache.shenyu.common.dto.convert.rule.impl.WasmRuleHandle;
import org.apache.shenyu.common.dto.convert.rule.impl.DivideRuleHandle;
import org.apache.shenyu.common.dto.convert.selector.WasmUpstream;
import org.apache.shenyu.common.enums.PluginEnum;
import org.apache.shenyu.common.utils.GsonUtils;
import org.apache.shenyu.loadbalancer.cache.UpstreamCacheManager;
import org.apache.shenyu.loadbalancer.entity.Upstream;
import org.apache.shenyu.plugin.base.cache.CommonHandleCache;
import org.apache.shenyu.plugin.base.cache.MetaDataCache;
import org.apache.shenyu.plugin.base.handler.PluginDataHandler;
import org.apache.shenyu.plugin.base.utils.BeanHolder;
import org.apache.shenyu.plugin.base.utils.CacheKeyUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class WasmPluginDataHandler implements PluginDataHandler {
    public static final Supplier<CommonHandleCache<String, WasmRuleHandle>> CACHED_HANDLE = new BeanHolder<>(CommonHandleCache::new);

    @Override
    public void handlerSelector(final SelectorData selectorData) {
        List<WasmUpstream> upstreamList = GsonUtils.getInstance().fromList(selectorData.getHandle(), WasmUpstream.class);
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

    @Override
    public void removeSelector(final SelectorData selectorData) {
        UpstreamCacheManager.getInstance().removeByKey(selectorData.getId());
        MetaDataCache.getInstance().clean();
        CACHED_HANDLE.get().removeHandle(CacheKeyUtils.INST.getKey(selectorData.getId(), Constants.DEFAULT_RULE));
    }

    @Override
    public void handlerRule(final RuleData ruleData) {
        Optional.ofNullable(ruleData.getHandle()).ifPresent(s -> {
            WasmRuleHandle wasmRuleHandle = GsonUtils.getInstance().fromJson(s, WasmRuleHandle.class);
            CACHED_HANDLE.get().cachedHandle(CacheKeyUtils.INST.getKey(ruleData), wasmRuleHandle);
            // the update is also need to clean, but there is no way to
            // distinguish between crate and update, so it is always clean
            MetaDataCache.getInstance().clean();
        });
    }

    @Override
    public void removeRule(final RuleData ruleData) {
        Optional.ofNullable(ruleData.getHandle()).ifPresent(s -> CACHED_HANDLE.get().removeHandle(CacheKeyUtils.INST.getKey(ruleData)));
        MetaDataCache.getInstance().clean();
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
