import { defineStore } from 'pinia';
import { Notification } from '@arco-design/web-vue';
import defaultSettings from '@/config/settings.json';
import { getMenuList } from '@/api/modules/user';
import { useI18n } from '@/hooks/useI18n';

import type { AppState } from './types';
import type { NotificationReturn } from '@arco-design/web-vue/es/notification/interface';
import type { RouteRecordNormalized, RouteRecordRaw } from 'vue-router';

const useAppStore = defineStore('app', {
  state: (): AppState => ({ ...defaultSettings, loading: false, loadingTip: '', topMenus: [] as RouteRecordRaw[] }),

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
    getCustomTheme(state: AppState): string {
      return state.customTheme as string;
    },
    getLoadingStatus(state: AppState): boolean {
      return state.loading;
    },
    getTopMenus(state: AppState): RouteRecordRaw[] {
      return state.topMenus;
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
  },
});

export default useAppStore;
