package org.apache.shenyu.plugin.wasm;

import java.io.IOException;
import java.net.URISyntaxException;

public class WasmPlugin extends AbstractWasmPlugin {

    public WasmPlugin() throws IOException, URISyntaxException {
        super();
    }

    //单独一个逻辑，首先先load一个wasm文件，然后在执行wasm文件
}
