import { defineStore } from 'pinia';

import {
  getAuthenticationList,
  getLocalConfig,
  isLogin as userIsLogin,
  login as userLogin,
  logout as userLogout,
} from '@/api/modules/user';
import { useI18n } from '@/hooks/useI18n';
import useLicenseStore from '@/store/modules/setting/license';
import { getHashParameters } from '@/utils';
import { clearToken, setToken } from '@/utils/auth';
import { composePermissions } from '@/utils/permission';
import { removeRouteListener } from '@/utils/route-listener';

import type { LoginData } from '@/models/user';

import useAppStore from '../app';
import { UserState } from './types';

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
    userRolePermissions: [],
    userRoles: [],
    userRoleRelations: [],
    loginType: [],
    hasLocalExec: false, // 是否配置了api本地执行
    isPriorityLocalExec: false, // 是否优先本地执行
    localExecuteUrl: '',
  }),

  getters: {
    userInfo(state: UserState): UserState {
      return { ...state };
    },
    isAdmin(state: UserState): boolean {
      if (!state.userRolePermissions) return false;
      return state.userRolePermissions.findIndex((ur) => ur.userRole.id === 'admin') > -1;
    },
    currentRole(state: UserState): {
      projectPermissions: string[];
      orgPermissions: string[];
      systemPermissions: string[];
    } {
      const appStore = useAppStore();

      state.userRoleRelations?.forEach((ug) => {
        state.userRolePermissions?.forEach((gp) => {
          if (gp.userRole.id === ug.roleId) {
            ug.userRolePermissions = gp.userRolePermissions;
            ug.userRole = gp.userRole;
          }
        });
      });

      return {
        projectPermissions: composePermissions(state.userRoleRelations || [], 'PROJECT', appStore.currentProjectId),
        orgPermissions: composePermissions(state.userRoleRelations || [], 'ORGANIZATION', appStore.currentOrgId),
        systemPermissions: composePermissions(state.userRoleRelations || [], 'SYSTEM', 'global'),
      };
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
        const appStore = useAppStore();

        setToken(res.sessionId, res.csrfToken);

        appStore.setCurrentOrgId(res.lastOrganizationId || '');
        appStore.setCurrentProjectId(res.lastProjectId || '');
        this.setInfo(res);
      } catch (err) {
        clearToken();
        throw err;
      }
    },
    // 获取登录认证方式
    async getAuthentication() {
      try {
        const res = await getAuthenticationList();
        this.loginType = res;
      } catch (error) {
        console.log(error);
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
      appStore.resetSystemPackageType();
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
    /**
     * 判断用户是否登录并设置用户信息
     * @param forceSet 是否强制设置项目 id 和组织 id，用于切换组织和项目时重写用户信息
     */
    async isLogin(forceSet = false) {
      try {
        const res = await userIsLogin();
        const appStore = useAppStore();
        setToken(res.sessionId, res.csrfToken);
        this.setInfo(res);
        const { orgId, pId } = getHashParameters();
        appStore.setCurrentOrgId(forceSet ? res.lastOrganizationId || '' : orgId || '');
        appStore.setCurrentProjectId(forceSet ? res.lastProjectId || '' : pId || '');
        return true;
      } catch (err) {
        // eslint-disable-next-line no-console
        console.log(err);
        return false;
      }
    },
    // 更新本地设置
    updateLocalConfig(partial: Partial<UserState>) {
      this.$patch(partial);
    },
    // 获取本地执行配置
    async initLocalConfig() {
      try {
        const res = await getLocalConfig();
        const apiLocalExec = res.find((e) => e.type === 'API');
        if (apiLocalExec) {
          this.hasLocalExec = true;
          this.isPriorityLocalExec = apiLocalExec.enable || false;
          this.localExecuteUrl = apiLocalExec.userUrl || '';
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    },
  },
});

export default useUserStore;
