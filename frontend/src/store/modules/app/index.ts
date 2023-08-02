import { defineStore } from 'pinia';
import { Notification } from '@arco-design/web-vue';
import defaultSettings from '@/config/settings.json';
import { getMenuList } from '@/api/modules/user';
import { getSystemVersion } from '@/api/modules/system';
import { useI18n } from '@/hooks/useI18n';
import { cloneDeep } from 'lodash-es';
import { getPageConfig } from '@/api/modules/setting/config';
import { setFavicon } from '@/utils';

import type { NotificationReturn } from '@arco-design/web-vue/es/notification/interface';
import type { RouteRecordNormalized, RouteRecordRaw } from 'vue-router';
import type { AppState } from './types';
import type { BreadcrumbItem } from '@/components/bussiness/ms-breadcrumb/types';
import type { PageConfig, PageConfigKeys } from '@/models/setting/config';

const defaultThemeConfig = {
  style: 'default',
  customStyle: '',
  theme: 'default',
  customTheme: '',
};
const defaultLoginConfig = {
  title: 'MeterSphere',
  icon: [],
  loginLogo: [],
  loginImage: [],
  slogan: '一站式开源持续测试平台',
};
const defaultPlatformConfig = {
  logoPlatform: [],
  platformName: 'MeterSphere',
  helpDoc: '',
};

const useAppStore = defineStore('app', {
  state: (): AppState => ({
    ...defaultSettings,
    loading: false,
    loadingTip: '',
    topMenus: [] as RouteRecordRaw[],
    currentTopMenu: {} as RouteRecordRaw,
    breadcrumbList: [] as BreadcrumbItem[],
    currentOrgId: '',
    currentProjectId: '',
    version: '',
    defaultThemeConfig,
    defaultLoginConfig,
    defaultPlatformConfig,
    pageConfig: {
      ...defaultThemeConfig,
      ...defaultLoginConfig,
      ...defaultPlatformConfig,
    },
  }),

  getters: {
    appCurrentSetting(state: AppState): AppState {
      return { ...state };
    },
    appDevice(state: AppState) {
      return state.device;
    },
    appAsyncMenus(state: AppState): RouteRecordNormalized[] {
      return state.serverMenu as unknown as RouteRecordNormalized[];
    },
    getLoadingStatus(state: AppState): boolean {
      return state.loading;
    },
    getTopMenus(state: AppState): RouteRecordRaw[] {
      return state.topMenus;
    },
    getCurrentTopMenu(state: AppState): RouteRecordRaw {
      return state.currentTopMenu;
    },
    getBreadcrumbList(state: AppState): BreadcrumbItem[] {
      return cloneDeep(state.breadcrumbList);
    },
    getCurrentOrgId(state: AppState): string {
      return state.currentOrgId;
    },
    getCurrentProjectId(state: AppState): string {
      return state.currentProjectId;
    },
    getDefaulPageConfig(state: AppState): PageConfig {
      return {
        ...state.defaultThemeConfig,
        ...state.defaultLoginConfig,
        ...state.defaultPlatformConfig,
      };
    },
  },

  actions: {
    /**
     * 更新设置
     * @param partial 设置
     */
    updateSettings(partial: Partial<AppState>) {
      // @ts-ignore-next-line
      this.$patch(partial);
    },
    /**
     * 切换显示模式
     * @param device 显示模式：mobile | desktop
     */
    toggleDevice(device: string) {
      this.device = device;
    },
    /**
     * 切换菜单显示
     * @param value 是否隐藏菜单
     */
    toggleMenu(value: boolean) {
      this.hideMenu = value;
    },
    /**
     * 获取服务端菜单配置
     */
    async fetchServerMenuConfig() {
      const { t } = useI18n();
      let notifyInstance: NotificationReturn | null = null;
      try {
        notifyInstance = Notification.info({
          id: 'menuNotice', // Keep the instance id the same
          content: t('message.menuLoading'),
          closable: true,
        });
        const data = await getMenuList();
        this.serverMenu = data;
        notifyInstance = Notification.success({
          id: 'menuNotice',
          content: t('message.menuLoadSuccess'),
          closable: true,
        });
      } catch (error) {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        notifyInstance = Notification.error({
          id: 'menuNotice',
          content: t('message.menuLoadError'),
          closable: true,
        });
      }
    },
    /**
     * 清空服务端菜单配置
     */
    clearServerMenu() {
      this.serverMenu = [];
    },
    /**
     * 显示全局 loading
     */
    showLoading(tip = '') {
      const { t } = useI18n();
      this.loading = true;
      this.loadingTip = tip || t('message.loadingDefaultTip');
    },
    /**
     * 隐藏全局 loading
     */
    hideLoading() {
      const { t } = useI18n();
      this.loading = false;
      this.loadingTip = t('message.loadingDefaultTip');
    },
    /**
     * 设置顶部菜单组
     */
    setTopMenus(menus: RouteRecordRaw[] | undefined) {
      this.topMenus = menus ? [...menus] : [];
    },
    /**
     * 设置激活的顶部菜单
     */
    setCurrentTopMenu(menu: RouteRecordRaw) {
      this.currentTopMenu = cloneDeep(menu);
    },
    /**
     * 设置面包屑
     */
    setBreadcrumbList(breadcrumbs: BreadcrumbItem[] | undefined) {
      this.breadcrumbList = breadcrumbs ? cloneDeep(breadcrumbs) : [];
    },
    /**
     * 设置当前组织 ID
     */
    setCurrentOrgId(id: string) {
      this.currentOrgId = id;
    },
    /**
     * 设置当前项目 ID
     */
    setCurrentProjectId(id: string) {
      this.currentProjectId = id;
    },
    /**
     * 获取系统版本
     */
    async initSystemversion() {
      try {
        this.version = await getSystemVersion();
      } catch (error) {
        console.log(error);
      }
    },
    /**
     * 初始化页面配置
     */
    async initPageConfig() {
      try {
        const res = await getPageConfig();
        if (Array.isArray(res) && res.length > 0) {
          res.forEach((e) => {
            const key = e.paramKey.split('ui.')[1] as PageConfigKeys; // 参数名前缀ui.去掉
            if (['icon', 'loginLogo', 'loginImage', 'logoPlatform'].includes(key)) {
              // 四个属性值为文件类型，单独处理
              this.pageConfig[key] = [
                {
                  url: e.paramValue,
                  name: e.fileName,
                },
              ] as any;
            } else {
              this.pageConfig[key] = e.paramValue as any;
            }
          });
          if (this.pageConfig.theme !== 'default') {
            // 判断是否选择了自定义主题色
            this.pageConfig.customTheme = this.pageConfig.theme;
            this.pageConfig.theme = 'custom';
          }
          if (!['default', 'follow'].includes(this.pageConfig.style)) {
            // 判断是否选择了自定义平台风格
            this.pageConfig.customStyle = this.pageConfig.style;
            this.pageConfig.style = 'custom';
          }
          if (this.pageConfig.icon[0]?.url) {
            // 设置网站 favicon
            setFavicon(this.pageConfig.icon[0].url);
          }
        }
      } catch (error) {
        console.log(error);
      }
    },
  },
  persist: {
    paths: ['currentOrgId', 'currentProjectId', 'pageConfig'],
  },
});

export default useAppStore;
