package org.apache.shenyu.plugin.wasm.handler;

import org.apache.shenyu.common.dto.MetaData;
import org.apache.shenyu.plugin.rpcx.cache.RpcxConfigCache;
import org.apache.shenyu.plugin.rpcx.handler.RpcxMetaDataHandler;

import java.util.Objects;

/**
 *
 */
public class WasmMetaDataHandler extends RpcxMetaDataHandler {

    @Override
    protected boolean isInitialized(final MetaData metaData) {
        return Objects.nonNull(RpcxConfigCache.getInstance().get(metaData.getPath()));
    }

    @Override
    protected void initReference(final MetaData metaData) {
        RpcxConfigCache.getInstance().initRef(metaData);
    }

    @Override
    protected void updateReference(final MetaData metaData) {
        RpcxConfigCache.getInstance().build(metaData, "");
    }

    @Override
    protected void invalidateReference(final String path) {
        RpcxConfigCache.getInstance().invalidate(path);
    }
}
