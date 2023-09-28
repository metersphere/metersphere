import MSR from '@/api/http/index';
import {
  RobotListUrl,
  GetRobotUrl,
  AddRobotUrl,
  UpdateRobotUrl,
  EnableRobotUrl,
} from '@/api/requrls/project-management/messageManagement';

import type { RobotItem, RobotAddParams, RobotEditParams } from '@/models/projectManagement/message';
import type { TableQueryParams, CommonList } from '@/models/common';

const list = [
  {
    id: '1',
    name: '站内信',
    description: '系统内置，在顶部导航栏显示消息通知',
    platform: 'IN_SITE',
    enable: true,
    webhook: 'asdasdasfasfsaf',
    projectId: '1',
    createTime: 1695721467045,
    createUser: 'admin',
  },
  {
    id: '2',
    name: '邮件',
    description: '系统内置，以添加用户邮箱为通知方式',
    platform: 'MAIL',
    enable: false,
    webhook: 'sdfsdfasfasf',
    projectId: '1',
    createTime: 1695721467045,
    createUser: 'admin',
  },
  {
    id: '3',
    name: '飞书',
    description: '',
    platform: 'LARK',
    enable: false,
    webhook: 'asdfgasdgasfgasgas',
    projectId: '1',
    createTime: 1695721467045,
    createUser: 'admin',
    updateUser: 'bai',
    updateTime: 1695721467045,
  },
  {
    id: '4',
    name: '钉钉',
    description: '',
    platform: 'DING_TALK',
    enable: false,
    webhook: 'asfgasfasdfa',
    type: 'CUSTOM',
    projectId: '1',
    createTime: 1695721467045,
    createUser: 'admin',
    updateUser: 'bai',
    updateTime: 1695721467045,
  },
  {
    id: '44',
    name: '钉钉',
    description: '',
    platform: 'DING_TALK',
    enable: false,
    webhook: 'asfgasfasdfa',
    appKey: 'asfasfasfasfasf',
    appSecret: 'asfasfasfasfasf',
    type: 'ENTERPRISE',
    projectId: '1',
    createTime: 1695721467045,
    createUser: 'admin',
    updateUser: 'bai',
    updateTime: 1695721467045,
  },
  {
    id: '5',
    name: '企业微信',
    description: '',
    platform: 'WE_COM',
    enable: false,
    webhook: 'vevbbt',
    projectId: '1',
    createTime: 1695721467045,
    createUser: 'admin',
    updateUser: 'bai',
    updateTime: 1695721467045,
  },
  {
    id: '5',
    name: '自定义',
    description: '',
    platform: 'CUSTOM',
    enable: false,
    webhook: 'bytnm',
    projectId: '1',
    createTime: 1695721467045,
    createUser: 'admin',
    updateUser: 'bai',
    updateTime: 1695721467045,
  },
  {
    id: '5',
    name: '自定义',
    description: '',
    platform: 'CUSTOM',
    enable: false,
    webhook: 'bytnm',
    projectId: '1',
    createTime: 1695721467045,
    createUser: 'admin',
    updateUser: 'bai',
    updateTime: 1695721467045,
  },
  {
    id: '5',
    name: '自定义',
    description: '',
    platform: 'CUSTOM',
    enable: false,
    webhook: 'bytnm',
    projectId: '1',
    createTime: 1695721467045,
    createUser: 'admin',
    updateUser: 'bai',
    updateTime: 1695721467045,
  },
];

export function getRobotList(projectId: string) {
  // return MSR.post<RobotItem[]>({ url: `${RobotListUrl}/${projectId}` });
  return Promise.resolve(list);
}

export function getRobotDetail(robotId: string) {
  // return MSR.get<RobotItem>({ url: GetRobotUrl, params: robotId });
  return Promise.resolve(list.find((item) => item.id === robotId));
}

export function addRobot(data: RobotAddParams) {
  return MSR.post({ url: AddRobotUrl, data });
}

export function updateRobot(data: RobotEditParams) {
  return MSR.post({ url: UpdateRobotUrl, data });
}

export function toggleRobot(id: string) {
  return MSR.get({ url: EnableRobotUrl, params: id });
}
