package org.apache.shenyu.plugin.rpcx.handler;

import com.google.common.collect.Maps;
import org.apache.shenyu.common.dto.MetaData;
import org.apache.shenyu.common.enums.RpcTypeEnum;
import org.apache.shenyu.plugin.base.handler.MetaDataHandler;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

public abstract class RpcxMetaDataHandler implements MetaDataHandler {
    private static final ConcurrentMap<String, MetaData> META_DATA = Maps.newConcurrentMap();

    @Override
    public void handle(final MetaData metaData) {
        MetaData exist = META_DATA.get(metaData.getPath());
        if (Objects.isNull(exist) || !isInitialized(metaData)) {
            // The first initialization
            initReference(metaData);
        } else {
            // There are updates, which only support the update of four properties of serviceName rpcExt parameterTypes methodName,
            // because these four properties will affect the call of Dubbo;
            if (!Objects.equals(metaData.getServiceName(), exist.getServiceName())
                    || !Objects.equals(metaData.getRpcExt(), exist.getRpcExt())
                    || !Objects.equals(metaData.getParameterTypes(), exist.getParameterTypes())
                    || !Objects.equals(metaData.getMethodName(), exist.getMethodName())) {
                updateReference(metaData);
            }
        }
        META_DATA.put(metaData.getPath(), metaData);
    }

    protected abstract boolean isInitialized(MetaData metaData);

    protected abstract void initReference(MetaData metaData);

    protected abstract void updateReference(MetaData metaData);

    @Override
    public void remove(final MetaData metaData) {
        invalidateReference(metaData.getPath());
        META_DATA.remove(metaData.getPath());
    }

    protected abstract void invalidateReference(String path);

    @Override
    public String rpcType() {
        return RpcTypeEnum.HTTP.getName();
    }
}
