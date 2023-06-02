import { defineStore } from 'pinia';
import { Notification } from '@arco-design/web-vue';
import type { NotificationReturn } from '@arco-design/web-vue/es/notification/interface';
import type { RouteRecordNormalized } from 'vue-router';
import defaultSettings from '@/config/settings.json';
import { getMenuList } from '@/api/modules/user';
import { AppState } from './types';

const useAppStore = defineStore('app', {
  state: (): AppState => ({ ...defaultSettings }),

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
     * 切换暗黑模式
     * @param dark 是否暗黑模式
     */
    toggleTheme(dark: boolean) {
      if (dark) {
        this.theme = 'dark';
        document.body.setAttribute('MS-theme', 'dark');
      } else {
        this.theme = 'light';
        document.body.removeAttribute('MS-theme');
      }
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
      let notifyInstance: NotificationReturn | null = null;
      try {
        notifyInstance = Notification.info({
          id: 'menuNotice', // Keep the instance id the same
          content: 'loading',
          closable: true,
        });
        const data = await getMenuList();
        this.serverMenu = data;
        notifyInstance = Notification.success({
          id: 'menuNotice',
          content: 'success',
          closable: true,
        });
      } catch (error) {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        notifyInstance = Notification.error({
          id: 'menuNotice',
          content: 'error',
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
  },
});

export default useAppStore;
