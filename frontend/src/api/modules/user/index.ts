import MSR from '@/api/http/index';
import { LoginUrl, LogoutUrl, GetMenuListUrl, isLoginUrl, getPublicKeyUrl } from '@/api/requrls/user';
import type { RouteRecordNormalized } from 'vue-router';
import type { LoginData, LoginRes } from '@/models/user';

export function login(data: LoginData) {
  return MSR.post<LoginRes>({ url: LoginUrl, data });
}

export function isLogin() {
  return MSR.get<LoginRes>({ url: isLoginUrl }, { ignoreCancelToken: true });
}

export function logout() {
  return MSR.get<LoginRes>({ url: LogoutUrl });
}

export function getMenuList() {
  return MSR.post<RouteRecordNormalized[]>({ url: GetMenuListUrl });
}

export function getPublicKeyRequest() {
  return MSR.get<string>({ url: getPublicKeyUrl }, { ignoreCancelToken: true });
}
