import MSR from '@/api/http/index';
import { OrganizationListItem } from '@/models/setting/orgnization';
import { GetAllOrgUrl } from '@/api/requrls/setting/orgnization';

// 获取全部组织列表
export function getAllOrgList() {
  return MSR.post<OrganizationListItem[]>({ url: GetAllOrgUrl });
}

export function other() {}
