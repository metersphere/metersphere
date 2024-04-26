/**
 * Theme import
 * 样式按需引入
 * https://github.com/arco-design/arco-plugins/blob/main/packages/plugin-vite-vue/README.md
 * https://arco.design/vue/docs/start
 */
import { vitePluginForArco } from '@arco-plugins/vite-vue';

export default function configArcoStyleImportPlugin() {
  // 按需加载主题样式
  const arcoResolverPlugin = vitePluginForArco({
    theme: '@arco-themes/vue-metersphere-v3',
  });
  return arcoResolverPlugin;
}
