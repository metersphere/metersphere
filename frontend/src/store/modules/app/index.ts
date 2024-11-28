import { defineStore } from 'pinia';
import { Notification } from '@arco-design/web-vue';
import { cloneDeep } from 'lodash-es';

import type { BreadcrumbItem } from '@/components/business/ms-breadcrumb/types';

import { getEnvironment, getEnvList } from '@/api/modules/api-test/common';
import { getProjectInfo } from '@/api/modules/project-management/basicInfo';
import { getProjectList } from '@/api/modules/project-management/project';
import { getBaseInfo, getPageConfig } from '@/api/modules/setting/config';
import { getPackageType, getSystemVersion } from '@/api/modules/system';
import { getMenuList } from '@/api/modules/user';
import defaultSettings from '@/config/settings.json';
import { useI18n } from '@/hooks/useI18n';
import useUser from '@/hooks/useUser';
import router from '@/router';
import { featureRouteMap, NO_PROJECT_ROUTE_NAME } from '@/router/constants';
import { watchStyle, watchTheme } from '@/utils/theme';

import type { EnvironmentItem } from '@/models/projectManagement/environmental';
import type { PageConfig, PageConfigKeys, Style, Theme } from '@/models/setting/config';
import { ProjectListItem } from '@/models/setting/project';

import type { AppState } from './types';
import type { NotificationReturn } from '@arco-design/web-vue/es/notification/interface';
import type { RouteRecordNormalized, RouteRecordRaw } from 'vue-router';

const defaultThemeConfig = {
  style: 'default' as Style,
  customStyle: '#f9f9fe',
  theme: 'default' as Theme,
  customTheme: '#811fa3',
};
const defaultLoginConfig = {
  title: 'MeterSphere',
  icon: [],
  loginLogo: [],
  loginImage: [],
  slogan: 'login.form.title',
};
const defaultPlatformConfig = {
  logoPlatform: [],
  platformName: 'MeterSphere',
  helpDoc: 'https://metersphere.io/docs/v3.x/',
};

const useAppStore = defineStore('app', {
  state: (): AppState => ({
    ...defaultSettings,
    loading: false,
    loadingTip: '',
    loginLoading: false,
    topMenus: [] as RouteRecordRaw[],
    currentTopMenu: {} as RouteRecordRaw,
    breadcrumbList: [] as BreadcrumbItem[],
    currentOrgId: '',
    currentProjectId: '',
    version: '',
    defaultThemeConfig,
    defaultLoginConfig,
    defaultPlatformConfig,
    innerHeight: 0,
    currentMenuConfig: Object.keys(featureRouteMap),
    pageConfig: {
      ...defaultThemeConfig,
      ...defaultLoginConfig,
      ...defaultPlatformConfig,
    },
    packageType: '',
    projectList: [] as ProjectListItem[],
    orgList: [],
    envList: [],
    currentEnvConfig: undefined,
    fileMaxSize: 50,
    isDarkTheme: false,
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
    getDefaultPageConfig(state: AppState): PageConfig {
      return {
        ...state.defaultThemeConfig,
        ...state.defaultLoginConfig,
        ...state.defaultPlatformConfig,
      };
    },
    getCurrentEnvId(state: AppState): string {
      return state.currentEnvConfig?.id || '';
    },
    getEnvList(state: AppState): EnvironmentItem[] {
      return state.envList;
    },
    getLoginLoadingStatus(state: AppState): boolean {
      return state.loginLoading;
    },
    getPackageType(state: AppState): string {
      return state.packageType;
    },
    getFileMaxSize(state: AppState): number {
      return state.fileMaxSize;
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
     * 设置当前组织列表
     */
    setOrgList(orgList: { id: string; name: string }[]) {
      this.orgList = orgList;
    },
    /**
     * 设置当前系统包类型
     */
    setPackageType(type: string) {
      this.packageType = type;
    },
    // 重置系统包的版本
    resetSystemPackageType() {
      this.packageType = '';
    },
    /**
     * 设置登录页面的loading
     */
    setLoginLoading(value: boolean) {
      this.loginLoading = value;
    },
    /**
     * 获取系统版本
     */
    async initSystemVersion() {
      try {
        this.version = await getSystemVersion();
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    },
    /**
     * 获取系统包类型
     */
    async initSystemPackage() {
      try {
        this.packageType = await getPackageType();
        // this.packageType = 'community';
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    },
    async getProjectInfos() {
      try {
        const { isWhiteListPage } = useUser();
        const routeName = router.currentRoute.value.name as string;
        if (
          !this.currentProjectId ||
          routeName?.includes('setting') ||
          routeName?.includes('workstation') ||
          isWhiteListPage()
        ) {
          // 如果没有项目id，或访问的是系统设置下的页面/白名单/工作台页面，则不读取项目基础信息
          return;
        }
        const res = await getProjectInfo(this.currentProjectId);
        if (!res || res.deleted) {
          router.push({
            name: NO_PROJECT_ROUTE_NAME,
          });
        }
        if (res) {
          this.setCurrentMenuConfig(res?.moduleIds || []);
        }
      } catch (error) {
        // eslint-disable-next-line no-console
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
          let hasStyleChange = false;
          let hasThemeChange = false;
          res.forEach((e) => {
            const key = e.paramKey.split('ui.')[1] as PageConfigKeys; // 参数名前缀ui.去掉
            if (['icon', 'loginLogo', 'loginImage', 'logoPlatform'].includes(key)) {
              // 四个属性值为文件类型，单独处理
              this.pageConfig[key] = [
                {
                  url: e.fileName,
                  name: e.paramValue,
                },
              ] as any;
            } else {
              if (key === 'style') {
                // 风格是否更改，先判断自定义风格的值是否相等，再判断非自定义的俩值是否相等
                hasStyleChange = !['default', 'follow'].includes(e.paramValue)
                  ? this.pageConfig.customStyle !== e.paramValue
                  : this.pageConfig.style !== e.paramValue;
              }
              if (key === 'theme') {
                // 主题是否更改，先判断自定义主题的值是否相等，再判断非自定义的俩值是否相等
                hasThemeChange =
                  e.paramValue !== 'default'
                    ? this.pageConfig.customTheme !== e.paramValue
                    : this.pageConfig.theme !== e.paramValue;
              }
              this.pageConfig[key] = e.paramValue as any;
            }
          });
          if (this.pageConfig.theme !== 'default') {
            // 判断是否选择了自定义主题色
            this.pageConfig.customTheme = this.pageConfig.theme;
            this.pageConfig.theme = 'custom';
          } else {
            // 非自定义则需要重置自定义主题色为空，避免本地缓存与接口配置不一致
            this.pageConfig.customTheme = defaultThemeConfig.customTheme;
          }
          if (!['default', 'follow'].includes(this.pageConfig.style)) {
            // 判断是否选择了自定义平台风格
            this.pageConfig.customStyle = this.pageConfig.style;
            this.pageConfig.style = 'custom';
          } else {
            // 非自定义则需要重置自定义风格，避免本地缓存与接口配置不一致
            this.pageConfig.customStyle = defaultThemeConfig.customStyle;
          }
          // 如果风格和主题有变化，则初始化一下主题和风格；没有变化则不需要在此初始化，在 App.vue 中初始化过了
          if (hasStyleChange) {
            watchStyle(this.pageConfig.style, this.pageConfig);
          }
          if (hasThemeChange) {
            watchTheme(this.pageConfig.theme, this.pageConfig);
          }
          window.document.title = this.pageConfig.title;
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    },

    /**
     * 设置当前项目菜单配置
     */
    async setCurrentMenuConfig(menuConfig: string[]) {
      this.currentMenuConfig = menuConfig;
    },
    async initProjectList() {
      try {
        if (this.currentOrgId) {
          const res = await getProjectList(this.getCurrentOrgId);
          this.projectList = res;
        } else {
          this.projectList = [];
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    },
    async setEnvConfig(env: string) {
      try {
        const res = await getEnvironment(env);
        this.currentEnvConfig = {
          ...res,
          id: env,
          name: this.envList.find((item) => item.id === env)?.name || '',
        };
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    },
    async initEnvList(env?: string) {
      try {
        this.envList = await getEnvList(this.currentProjectId);
        if (this.envList.findIndex((item) => item.id === this.currentEnvConfig?.id) === -1) {
          this.setEnvConfig(env || this.envList[0]?.id);
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    },
    async initFileSizeLimit() {
      try {
        const res = await getBaseInfo();
        this.fileMaxSize = Number(res.fileMaxSize) || 50;
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    },
    setFileMaxSize(size: number) {
      this.fileMaxSize = size;
    },
    setDarkTheme(isDarkTheme: boolean) {
      this.isDarkTheme = isDarkTheme;
    },
  },
  persist: {
    paths: ['currentOrgId', 'currentProjectId', 'pageConfig', 'menuCollapse', 'isDarkTheme'],
  },
});

export default useAppStore;
