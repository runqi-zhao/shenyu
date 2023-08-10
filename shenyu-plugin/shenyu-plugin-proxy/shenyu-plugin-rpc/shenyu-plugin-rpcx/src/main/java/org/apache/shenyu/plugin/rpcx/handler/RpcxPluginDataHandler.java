package org.apache.shenyu.plugin.rpcx.handler;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shenyu.common.constant.Constants;
import org.apache.shenyu.common.dto.PluginData;
import org.apache.shenyu.common.dto.RuleData;
import org.apache.shenyu.common.dto.SelectorData;
import org.apache.shenyu.common.dto.convert.plugin.RpcxRegisterConfig;
import org.apache.shenyu.common.dto.convert.rule.impl.WasmRuleHandle;
import org.apache.shenyu.common.dto.convert.selector.WasmUpstream;
import org.apache.shenyu.common.enums.PluginEnum;
import org.apache.shenyu.common.utils.GsonUtils;
import org.apache.shenyu.loadbalancer.cache.UpstreamCacheManager;
import org.apache.shenyu.loadbalancer.entity.Upstream;
import org.apache.shenyu.plugin.base.cache.CommonHandleCache;
import org.apache.shenyu.plugin.base.handler.PluginDataHandler;
import org.apache.shenyu.plugin.base.utils.BeanHolder;
import org.apache.shenyu.plugin.base.utils.CacheKeyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class RpcxPluginDataHandler implements PluginDataHandler {
    public static final Supplier<CommonHandleCache<String, WasmRuleHandle>> RULE_CACHED_HANDLE = new BeanHolder<>(CommonHandleCache::new);

    public static final Supplier<CommonHandleCache<String, List<WasmUpstream>>> SELECTOR_CACHED_HANDLE = new BeanHolder<>(CommonHandleCache::new);

    protected abstract void initConfigCache(RpcxRegisterConfig rpcxRegisterConfig);

    @Override
    public void handlerPlugin(final PluginData pluginData) {
        //TODO:相当于从go层面获取pluginData
    }

    @Override
    public void handlerSelector(final SelectorData selectorData) {
        if (!selectorData.getContinued()) {
            RULE_CACHED_HANDLE.get().cachedHandle(CacheKeyUtils.INST.getKey(selectorData.getId(), Constants.DEFAULT_RULE), WasmRuleHandle.newInstance());
        }
        List<WasmUpstream> wasmUpstreams = GsonUtils.getInstance().fromList(selectorData.getHandle(), WasmUpstream.class);
        if (CollectionUtils.isEmpty(wasmUpstreams)) {
            return;
        }
        List<WasmUpstream> graySelectorHandle = new ArrayList<>();
        for (WasmUpstream each : wasmUpstreams) {
            if (StringUtils.isNotBlank(each.getUpstreamUrl())) {
                graySelectorHandle.add(each);
            }
        }
        if (CollectionUtils.isNotEmpty(graySelectorHandle)) {
            SELECTOR_CACHED_HANDLE.get().cachedHandle(selectorData.getId(), graySelectorHandle);
            UpstreamCacheManager.getInstance().submit(selectorData.getId(), convertUpstreamList(graySelectorHandle));
        } else {
            // if update gray selector is empty, remove cache
            removeSelector(selectorData);
        }
    }

    @Override
    public void removeSelector(final SelectorData selectorData) {
        SELECTOR_CACHED_HANDLE.get().removeHandle(selectorData.getId());
        UpstreamCacheManager.getInstance().removeByKey(selectorData.getId());
        RULE_CACHED_HANDLE.get().removeHandle(CacheKeyUtils.INST.getKey(selectorData.getId(), Constants.DEFAULT_RULE));
    }

    @Override
    public void handlerRule(final RuleData ruleData) {
        RULE_CACHED_HANDLE.get().cachedHandle(ruleData.getId(), GsonUtils.getInstance().fromJson(ruleData.getHandle(), WasmRuleHandle.class));
    }

    @Override
    public void removeRule(final RuleData ruleData) {
        RULE_CACHED_HANDLE.get().removeHandle(ruleData.getId());
    }

    @Override
    public String pluginNamed() {
        return PluginEnum.WASM.getName();
    }

    private List<Upstream> convertUpstreamList(final List<WasmUpstream> handleList) {
        return handleList.stream().map(u -> Upstream.builder()
                .protocol(u.getProtocol())
                .url(u.getUpstreamUrl())
                .status(u.isStatus())
                .timestamp(u.getTimestamp())
                .build()).collect(Collectors.toList());
    }
}
