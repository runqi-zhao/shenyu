package org.apache.shenyu.plugin.rpcx.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.shenyu.common.constant.Constants;
import org.apache.shenyu.common.dto.MetaData;
import org.apache.shenyu.common.dto.convert.plugin.DubboRegisterConfig;
import org.apache.shenyu.common.exception.ShenyuException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class RpcxConfigCache {
    private static final Logger LOG = LoggerFactory.getLogger(RpcxConfigCache.class);

    private static final RpcxConfigCache INSTANCE = new RpcxConfigCache();

    private RpcxConfigCache() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static RpcxConfigCache getInstance() {
        return INSTANCE;
    }

    /**
     *
     * @param path
     * @return
     */
    public Object get(String path) {
        //TODO:get cache
        return null;
    }

    public void initRef(MetaData metaData) {

    }

    public void build(MetaData metaData, String s) {
    }

    public void invalidate(String path) {

    }
}
