// 系统全局类的接口
import MSR from '@/api/http/index';
import { GetVersionUrl } from '@/api/requrls/system';

// 获取系统版本
export function getSystemVersion() {
  return MSR.get<string>({ url: GetVersionUrl }, { ignoreCancelToken: true });
}

export default { getSystemVersion };
