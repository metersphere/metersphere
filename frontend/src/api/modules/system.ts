// 系统全局类的接口
import MSR from '@/api/http/index';
import { GetVersionUrl, OrgOptionsUrl, SwitchOrgUrl } from '@/api/requrls/system';

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
