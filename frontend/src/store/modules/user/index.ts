import { defineStore } from 'pinia';
import { login as userLogin, logout as userLogout, isLogin as userIsLogin } from '@/api/modules/user';
import { setToken, clearToken } from '@/utils/auth';
import { removeRouteListener } from '@/utils/route-listener';
import useAppStore from '../app';
import useLicenseStore from '../setting/license';
import { useI18n } from '@/hooks/useI18n';

import type { LoginData } from '@/models/user';
import type { UserState } from './types';

const useUserStore = defineStore('user', {
  // 开启数据持久化
  persist: true,
  state: (): UserState => ({
    name: undefined,
    avatar: undefined,
    job: undefined,
    organization: undefined,
    location: undefined,
    email: undefined,
    introduction: undefined,
    personalWebsite: undefined,
    jobName: undefined,
    organizationName: undefined,
    locationName: undefined,
    phone: undefined,
    registrationDate: undefined,
    id: undefined,
    certification: undefined,
    role: '',
  }),

  getters: {
    userInfo(state: UserState): UserState {
      return { ...state };
    },
  },

  actions: {
    switchRoles() {
      return new Promise((resolve) => {
        this.role = this.role === 'user' ? 'admin' : 'user';
        resolve(this.role);
      });
    },
    // 设置用户信息
    setInfo(partial: Partial<UserState>) {
      this.$patch(partial);
    },

    // 重置用户信息
    resetInfo() {
      this.$reset();
    },

    // 登录
    async login(loginForm: LoginData) {
      try {
        const res = await userLogin(loginForm);
        setToken(res.sessionId, res.csrfToken);
        const appStore = useAppStore();
        appStore.setCurrentOrgId(res.lastOrganizationId || '');
        appStore.setCurrentProjectId(res.lastProjectId || '');
        this.setInfo(res);
      } catch (err) {
        clearToken();
        throw err;
      }
    },
    // 登出回调
    logoutCallBack() {
      const appStore = useAppStore();
      const licenseStore = useLicenseStore();
      this.resetInfo();
      clearToken();
      removeRouteListener();
      licenseStore.removeLicenseStatus();
      appStore.clearServerMenu();
      appStore.hideLoading();
    },
    // 登出
    async logout() {
      try {
        const { t } = useI18n();
        const appStore = useAppStore();
        appStore.showLoading(t('message.logouting'));
        await userLogout();
      } finally {
        this.logoutCallBack();
      }
    },
    // 是否已经登录
    async isLogin() {
      try {
        const res = await userIsLogin();
        const appStore = useAppStore();
        setToken(res.sessionId, res.csrfToken);
        this.setInfo(res);
        appStore.setCurrentOrgId(res.lastOrganizationId || '');
        appStore.setCurrentProjectId(res.lastProjectId || '');
        return true;
      } catch (err) {
        return false;
      }
    },
  },
});

export default useUserStore;
