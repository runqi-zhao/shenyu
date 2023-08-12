package org.apache.shenyu.plugin.wasm.handler;

import com.google.common.collect.Maps;
import org.apache.shenyu.common.dto.MetaData;
import org.apache.shenyu.common.enums.RpcTypeEnum;
import org.apache.shenyu.plugin.base.cache.MetaDataCache;
import org.apache.shenyu.plugin.base.handler.MetaDataHandler;
import org.apache.shenyu.wasm.Instance;
import org.apache.shenyu.wasm.exports.Function;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public class WasmMetaDataHandler implements MetaDataHandler {
    protected final Instance instance;

    private static final ConcurrentMap<String, MetaData> META_DATA = Maps.newConcurrentMap();

    public WasmMetaDataHandler() throws IOException, URISyntaxException {
        // locate `.wasm` lib.
        final Class<? extends WasmMetaDataHandler> clazz = this.getClass();
        Path wasmPath = Paths.get(clazz.getClassLoader().getResource(clazz.getName() + ".wasm").toURI());

        // Reads the WebAssembly module as bytes.
        byte[] wasmBytes = Files.readAllBytes(wasmPath);

        // Instantiates the WebAssembly module.
        this.instance = new Instance(wasmBytes);
        Runtime.getRuntime().addShutdownHook(new Thread(this.instance::close));
    }
    @Override
    public void handle(MetaData metaData) {
        MetaData exist = META_DATA.get(metaData.getPath());
        if (Objects.isNull(exist)) {
            // The first initialization
            Function method1 = this.instance.getFunction("method1");
            //get refernce from wasm
            Object[] apply = method1.apply(metaData);
            for (int i = 0; i < apply.length; i++) {
                MetaData serviceConfig = (MetaData) apply[i];
                MetaDataCache.getInstance().clean();
                META_DATA.put(serviceConfig.getPath(), serviceConfig);
            }
        } else {
            if (!exist.getServiceName().equals(metaData.getServiceName()) || !exist.getRpcExt().equals(metaData.getRpcExt())) {
                // update
                MetaDataCache.getInstance().clean();
            }
        }
        META_DATA.put(metaData.getPath(), metaData);
    }

    @Override
    public void remove(MetaData metaData) {
        Function method1 = this.instance.getFunction("method1");
        //get refernce from wasm
        Object[] apply = method1.apply(metaData);
        for (int i = 0; i < apply.length; i++) {
            MetaData serviceConfig = (MetaData) apply[i];
            MetaDataCache.getInstance().clean();
            META_DATA.remove(serviceConfig);
        }
    }

    @Override
    public String rpcType() {
        return RpcTypeEnum.HTTP.getName();
    }
}
