package io.metersphere.plugin.sdk.api;

import io.metersphere.plugin.sdk.util.PluginLogUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMsPlugin implements MsPlugin {

    private static final String SCRIPT_DIR = "script";
    private ClassLoader pluginClassLoader;

    public AbstractMsPlugin(ClassLoader pluginClassLoader) {
        this.pluginClassLoader = pluginClassLoader;
    }

    protected InputStream readResource(String name) {
        return pluginClassLoader.getResourceAsStream(name);
    }


    /**
     * @return 返回该加载前端配置文件的目录，默认是 script
     * 可以重写定制
     */
    protected String getScriptDir() {
        return SCRIPT_DIR;
    }

    /**
     * @return 返回 resources下的 script 下的 json 文件
     */
    @Override
    public List<String> getFrontendScript() {
        List<String> scriptList = new ArrayList<>();
        String scriptDirName = getScriptDir();
        URL scriptDir = pluginClassLoader.getResource(scriptDirName);
        if (scriptDir != null) {
            File resourceDir = new File(scriptDir.getFile());
            List<String> filePaths = getFilePaths(resourceDir);
            for (String filePath : filePaths) {
                InputStream in = readResource(scriptDirName + "/" + filePath);
                try {
                    if (in != null) {
                        scriptList.add(IOUtils.toString(in));
                    }
                } catch (IOException e) {
                    PluginLogUtils.error(e);
                }
            }
        }
        return scriptList;
    }

    private static List<String> getFilePaths(File directory) {
        List<String> filePaths = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    filePaths.addAll(getFilePaths(file));
                } else {
                    filePaths.add(file.getAbsolutePath());
                }
            }
        }
        return filePaths;
    }

    @Override
    public String getName() {
        return getKey();
    }

    @Override
    public String getPluginId() {
        return getName().toLowerCase() + "-" + getVersion();
    }
}
