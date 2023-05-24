import MSR from '@/api/http/index';
import { LoginUrl, LogoutUrl, GetUserInfoUrl, GetMenuListUrl } from '@/api/requrls/user';
import type { RouteRecordNormalized } from 'vue-router';
import type { LoginData, LoginRes } from '@/models/user';
import type { UserState } from '@/store/modules/user/types';

export function login(data: LoginData) {
  return MSR.post<LoginRes>({ url: LoginUrl, data });
}

export function logout() {
  return MSR.post<LoginRes>({ url: LogoutUrl });
}

export function getUserInfo() {
  return MSR.post<UserState>({ url: GetUserInfoUrl });
}

export function getMenuList() {
  return MSR.post<RouteRecordNormalized[]>({ url: GetMenuListUrl });
}
