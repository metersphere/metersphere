import MSR from '@/api/http/index';
import { addLicenseUrl, getLicenseUrl } from '@/api/requrls/setting/authorizedManagement';

import type { LicenseInfo } from '@/models/setting/authorizedManagement';
// 获取当前信息
export function getLicenseInfo() {
  return MSR.get<LicenseInfo>({ url: getLicenseUrl }, { ignoreCancelToken: true });
}
// 添加License
export function addLicense(data: string) {
  return MSR.post({ url: addLicenseUrl, data });
}
