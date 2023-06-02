import { defineStore } from 'pinia';
import { login as userLogin, logout as userLogout, getUserInfo } from '@/api/modules/user';
import { setToken, clearToken } from '@/utils/auth';
import { removeRouteListener } from '@/utils/route-listener';

import type { LoginData } from '@/models/user';
import { UserState } from './types';
import useAppStore from '../app';

const useUserStore = defineStore('user', {
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
    accountId: undefined,
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

    // 获取用户信息
    async info() {
      const res = await getUserInfo();

      this.setInfo(res);
    },

    // 登录
    async login(loginForm: LoginData) {
      try {
        const res = await userLogin(loginForm);
        setToken(res.sessionId, res.csrfToken);
      } catch (err) {
        clearToken();
        throw err;
      }
    },
    // 登出回调
    logoutCallBack() {
      const appStore = useAppStore();
      this.resetInfo();
      clearToken();
      removeRouteListener();
      appStore.clearServerMenu();
    },
    // 登出
    async logout() {
      try {
        await userLogout();
      } finally {
        this.logoutCallBack();
      }
    },
  },
});

export default useUserStore;
