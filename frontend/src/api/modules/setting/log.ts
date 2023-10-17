import MSR from '@/api/http/index';
import {
  GetOrgLogListUrl,
  GetOrgLogOptionsUrl,
  GetOrgLogUserUrl,
  GetProjectLogListUrl,
  GetProjectLogUserUrl,
  GetSystemLogListUrl,
  GetSystemLogOptionsUrl,
  GetSystemLogUserUrl,
} from '@/api/requrls/setting/log';

import type { CommonList } from '@/models/common';
import type { LogItem, LogListParams, LogOptions, UserItem } from '@/models/setting/log';

// 获取系统日志列表
export function getSystemLogList(data: LogListParams) {
  return MSR.post<CommonList<LogItem>>({ url: GetSystemLogListUrl, data });
}

// 获取系统日志-操作范围选项
export function getSystemLogOptions() {
  return MSR.get<LogOptions>({ url: GetSystemLogOptionsUrl });
}

// 获取系统日志-操作用户列表
export function getSystemLogUsers({ keyword }: { keyword: string }) {
  return MSR.get<UserItem[]>({ url: GetSystemLogUserUrl, params: { keyword } });
}

// 获取组织日志列表
export function getOrgLogList(data: LogListParams) {
  return MSR.post<CommonList<LogItem>>({ url: GetOrgLogListUrl, data });
}

// 获取组织日志-操作范围选项
export function getOrgLogOptions(id: string) {
  return MSR.get<LogOptions>({ url: GetOrgLogOptionsUrl, params: id });
}

// 获取组织日志-操作用户列表
export function getOrgLogUsers({ id, keyword }: { id: string; keyword: string }) {
  return MSR.get<UserItem[]>({ url: `${GetOrgLogUserUrl}/${id}`, params: { keyword } });
}

// 获取项目日志列表
export function getProjectLogList(data: LogListParams) {
  return MSR.post<CommonList<LogItem>>({ url: GetProjectLogListUrl, data });
}

// 获取项目日志-操作用户列表
export function getProjectLogUsers({ id, keyword }: { id: string; keyword: string }) {
  return MSR.get<UserItem[]>({ url: `${GetProjectLogUserUrl}/${id}`, params: { keyword } });
}
