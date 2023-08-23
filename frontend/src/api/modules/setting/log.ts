import MSR from '@/api/http/index';
import {
  GetSystemLogListUrl,
  GetSystemLogOptionsUrl,
  GetSystemLogUserUrl,
  GetOrgLogListUrl,
  GetOrgLogOptionsUrl,
  GetOrgLogUserUrl,
} from '@/api/requrls/setting/log';

import type { CommonList } from '@/models/common';
import type { LogOptions, LogItem, UserItem } from '@/models/setting/log';

// 获取系统日志列表
export function getSystemLogList(data: any) {
  return MSR.post<CommonList<LogItem>>({ url: GetSystemLogListUrl, data });
}

// 获取系统日志-操作范围选项
export function getSystemLogOptions() {
  return MSR.get<LogOptions>({ url: GetSystemLogOptionsUrl });
}

// 获取系统日志-操作用户列表
export function getSystemLogUsers() {
  return MSR.get<UserItem[]>({ url: GetSystemLogUserUrl });
}

// 获取组织日志列表
export function getOrgLogList(data: any) {
  return MSR.post<CommonList<LogItem>>({ url: GetOrgLogListUrl, data });
}

// 获取组织日志-操作范围选项
export function getOrgLogOptions(id: string) {
  return MSR.get<LogOptions>({ url: GetOrgLogOptionsUrl, params: id });
}

// 获取组织日志-操作用户列表
export function getOrgLogUsers(id: string) {
  return MSR.get<UserItem[]>({ url: GetOrgLogUserUrl, params: id });
}
