import { RouteRecordName, RouteRecordRaw } from 'vue-router';

export interface RouteRecordAppend {
  parentName: RouteRecordName;
  route: RouteRecordRaw;
}
export interface PluginModule {
  /**
   * These components will be registered when plugin is activated.
   */
  components?: Record<string, Component>;

  /**
   * Activate hook will be called when plugin is activated.
   */
  activated?: () => void;

  /**
   * Deactivate hook will be called when plugin is deactivated.
   */
  deactivated?: () => void;

  routes?: RouteRecordRaw[] | RouteRecordAppend[];

  ucRoutes?: RouteRecordRaw[] | RouteRecordAppend[];

  extensionPoints?: any;
}
