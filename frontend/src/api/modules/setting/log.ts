import MSR from '@/api/http/index';
import { GetLogListUrl, GetLogOptionsUrl, GetLogUserUrl } from '@/api/requrls/setting/log';

import type { CommonList } from '@/models/common';
import type { LogOptions, LogItem, UserItem } from '@/models/setting/log';

// 获取日志列表
export function getLogList(data: any) {
  return MSR.post<CommonList<LogItem>>({ url: GetLogListUrl, data });
}

// 获取日志操作范围选项
export function getLogOptions() {
  return MSR.get<LogOptions>({ url: GetLogOptionsUrl });
}

// 获取日志操作用户列表
export function getLogUsers() {
  return MSR.get<UserItem[]>({ url: GetLogUserUrl });
}
