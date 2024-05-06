// 系统全局类的接口
import MSR from '@/api/http/index';
import { getSystemProjectListUrl } from '@/api/requrls/setting/member';
import {
  GetVersionUrl,
  OrgOptionsUrl,
  PackageTypeUrl,
  SwitchOrgUrl,
  userHasProjectPermissionUrl,
} from '@/api/requrls/system';

// 获取系统版本
export function getSystemVersion() {
  return MSR.get<string>({ url: GetVersionUrl }, { ignoreCancelToken: true });
}

// 获取当前登录用户组织机构下拉选项
export function getOrgOptions() {
  return MSR.get<{ id: string; name: string }[]>({ url: OrgOptionsUrl }, { ignoreCancelToken: true });
}

// 切换用户当前组织
export function switchUserOrg(organizationId: string, userId: string) {
  return MSR.post({ url: SwitchOrgUrl, data: { organizationId, userId } }, { ignoreCancelToken: true });
}

// 获取当前系统的版本
export function getPackageType() {
  return MSR.get<string>({ url: PackageTypeUrl });
}

// 获取当前用户是否具备项目权限
export function getUserHasProjectPermission(userId: string) {
  return MSR.get({ url: `${userHasProjectPermissionUrl}/${userId}` });
}

export function getSystemProjectList(keyword: string) {
  return MSR.get({ url: getSystemProjectListUrl, params: { keyword } });
}
