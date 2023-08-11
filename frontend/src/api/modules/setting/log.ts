import MSR from '@/api/http/index';
import { GetLogListUrl, GetLogOptionstUrl } from '@/api/requrls/setting/log';

import type { LogOptions } from '@/models/setting/log';

// 获取日志列表
export function getLogList(data: any) {
  return MSR.post({ url: GetLogListUrl, data });
}

// 获取日志操作范围选项
export function getLogOptions() {
  // return MSR.get<LogOptions>({ url: GetLogOptionstUrl });
  return {
    organizationList: [
      {
        id: 'bfa4feec-276f-11ee-bc36-0242ac1e0a05',
        name: '默认组织默认组织默认组织默认组织默认组织默认组织默认组织默认组织默认组织默认组织',
      },
      {
        id: 'e21d5270-369e-11ee-93aa-0242ac1e0a02',
        name: '测试组织2',
      },
      {
        id: 'e2433740-369e-11ee-93aa-0242ac1e0a02',
        name: '测试组织3',
      },
      {
        id: 'e2817131-369e-11ee-93aa-0242ac1e0a02',
        name: '测试组织4',
      },
      {
        id: 'e28950e3-369e-11ee-93aa-0242ac1e0a02',
        name: '测试组织5',
      },
      {
        id: 'e2c03258-369e-11ee-93aa-0242ac1e0a02',
        name: '测试组织6',
      },
      {
        id: 'e2f08005-369e-11ee-93aa-0242ac1e0a02',
        name: '测试组织7',
      },
      {
        id: 'e30650b8-369e-11ee-93aa-0242ac1e0a02',
        name: '测试组织8',
      },
      {
        id: 'e3487cf7-369e-11ee-93aa-0242ac1e0a02',
        name: '测试组织9',
      },
      {
        id: 'e363b914-369e-11ee-93aa-0242ac1e0a02',
        name: '测试组织10',
      },
      {
        id: 'e36bd98b-369e-11ee-93aa-0242ac1e0a02',
        name: '测试组织11',
      },
    ],
    projectList: [
      {
        id: 'ab11c4a1-369f-11ee-93aa-0242ac1e0a02',
        name: '测试2项目测试2项目测试2项目测试2项目测试2项目测试2项目测试2项目测试2项目测试2项目测试2项目测试2项目测试2项目',
      },
      {
        id: 'ab2cf284-369f-11ee-93aa-0242ac1e0a02',
        name: '测试3项目',
      },
      {
        id: 'ab6ffa01-369f-11ee-93aa-0242ac1e0a02',
        name: '测试4项目',
      },
      {
        id: 'ab845cd2-369f-11ee-93aa-0242ac1e0a02',
        name: '测试5项目',
      },
      {
        id: 'ab8aef35-369f-11ee-93aa-0242ac1e0a02',
        name: '测试6项目',
      },
      {
        id: 'ab91ff63-369f-11ee-93aa-0242ac1e0a02',
        name: '测试7项目',
      },
      {
        id: 'abc542d5-369f-11ee-93aa-0242ac1e0a02',
        name: '测试8项目',
      },
      {
        id: 'abcc2352-369f-11ee-93aa-0242ac1e0a02',
        name: '测试9项目',
      },
      {
        id: 'abeeb108-369f-11ee-93aa-0242ac1e0a02',
        name: '测试10项目',
      },
      {
        id: 'ac4e4fc0-369f-11ee-93aa-0242ac1e0a02',
        name: '测试11项目',
      },
      {
        id: 'acb0831f-369f-11ee-93aa-0242ac1e0a02',
        name: '测试12项目',
      },
      {
        id: 'ad5a7bc0-369f-11ee-93aa-0242ac1e0a02',
        name: '测试13项目',
      },
      {
        id: 'bfa52f87-276f-11ee-bc36-0242ac1e0a05',
        name: '默认项目',
      },
    ],
  };
}
