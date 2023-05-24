/**
 * If you use the template method for development, you can use the unplugin-vue-components plugin to enable on-demand loading support.
 * 按需引入
 * https://github.com/antfu/unplugin-vue-components
 * https://arco.design/vue/docs/start
 * Although the Pro project is full of imported components, this plugin will be used by default.
 * 虽然Pro项目中是全量引入组件，但此插件会默认使用。
 */
import Components from 'unplugin-vue-components/vite';
import { ArcoResolver } from 'unplugin-vue-components/resolvers';

export default function configArcoResolverPlugin() {
  const arcoResolverPlugin = Components({
    dirs: [], // Avoid parsing src/components.  避免解析到src/components
    deep: false,
    resolvers: [ArcoResolver()],
  });
  return arcoResolverPlugin;
}
