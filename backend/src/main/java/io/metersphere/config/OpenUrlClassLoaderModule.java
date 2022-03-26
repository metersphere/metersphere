package io.metersphere.config;

import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.net.URLClassLoader;

@Configuration
public class OpenUrlClassLoaderModule {

    static {
        // If on Java 9+, open the URLClassLoader module to this module
        // so we can access its API via reflection without producing a warning.
        try {
            openUrlClassLoaderModule();
        } catch (Throwable e) {
            // ignore exception - will throw on Java 8 since the Module classes don't exist
            e.printStackTrace();
        }
    }

    private static void openUrlClassLoaderModule() throws Exception {
        // This is effectively calling:
        //
        // URLClassLoader.class.getModule().addOpens(
        //     URLClassLoader.class.getPackageName(),
        //     ReflectionClassLoader.class.getModule()
        // );
        //
        // We use reflection since we build against Java 8.

        Class<?> moduleClass = Class.forName("java.lang.Module");
        Method getModuleMethod = Class.class.getMethod("getModule");
        Method addOpensMethod = moduleClass.getMethod("addOpens", String.class, moduleClass);

        Object urlClassLoaderModule = getModuleMethod.invoke(URLClassLoader.class);
        Object thisModule = getModuleMethod.invoke(OpenUrlClassLoaderModule.class);

        addOpensMethod.invoke(urlClassLoaderModule, URLClassLoader.class.getPackage().getName(), thisModule);
    }
}
