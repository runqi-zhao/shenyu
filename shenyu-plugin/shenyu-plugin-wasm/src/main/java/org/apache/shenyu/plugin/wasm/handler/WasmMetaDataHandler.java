package org.apache.shenyu.plugin.wasm.handler;

import org.apache.shenyu.common.dto.MetaData;
import org.apache.shenyu.common.enums.RpcTypeEnum;
import org.apache.shenyu.plugin.base.cache.MetaDataCache;
import org.apache.shenyu.plugin.base.handler.MetaDataHandler;

/**
 *
 */
public class WasmMetaDataHandler implements MetaDataHandler {
    @Override
    public void handle(final MetaData metaData) {
        // the update is also need to clean, but there is no way to
        // distinguish between crate and update, so it is always clean
        MetaDataCache.getInstance().clean();
    }

    @Override
    public void remove(final MetaData metaData) {
        MetaDataCache.getInstance().clean();
    }

    @Override
    public void refresh() {
        MetaDataCache.getInstance().clean();
    }

    @Override
    public String rpcType() {
        return RpcTypeEnum.HTTP.getName();
    }
}
