import MSR from '@/api/http/index';
import { FileDetailUrl, FileListUrl } from '@/api/requrls/project-management/fileManagement';

import type { TableQueryParams, CommonList } from '@/models/common';

const fileList = [
  {
    id: 100001,
    name: 'JAR',
    url: 'https://github.com/metersphere/metersphere/blob/v2.10/.gitignore',
    type: 'JAR',
    storage: 'minio',
    tag: ['dsadasd'],
    size: '12MB',
    enable: true,
    fileModule: 'XXX',
    creator: '创建人',
    updater: '更新人',
    updateTime: 18975439859,
    createTime: 18975439859,
  },
  {
    id: 1000002,
    name: 'PNG',
    url: 'http://localhost:5173/front/base-display/get/logo-platform',
    type: 'PNG',
    storage: 'github',
    gitBranch: 'master',
    gitVersion: 'v2.10',
    gitPath: '/asdas/xas/xas/fd/f/',
    tag: ['asfasdfas'],
    size: '12MB',
    enable: false,
    fileModule: 'XXX',
    creator: '创建人',
    updater: '更新人',
    updateTime: 18975439859,
    createTime: 18975439859,
  },
];
// 获取文件列表
export function getFileList(data: TableQueryParams) {
  // return MSR.post<CommonList<any>>({ url: FileListUrl, data });
  return Promise.resolve({
    total: 2,
    list: fileList,
    pageSize: 10,
    current: 1,
  });
}

// 获取文件详情
export function getFileDetail(id: string | number) {
  // return MSR.post<CommonList<any>>({ url: FileDetailUrl, data });
  return Promise.resolve(fileList.find((item) => item.id === id));
}

// 获取文件关联用例列表
export function getFileCases(data: TableQueryParams) {
  // return MSR.post<CommonList<any>>({ url: FileDetailUrl, data });
  return Promise.resolve({
    total: 1,
    list: [
      {
        id: 1,
        name: '用例名称 xxxx',
        type: '功能',
        fileVersion: '93f9384yf9',
        updateTime: 18975439859,
        createTime: 18975439859,
      },
      {
        id: 1,
        name: '用例名称 12433421',
        type: '接口',
        fileVersion: '93f9384yf9',
        updateTime: 18975439859,
        createTime: 18975439859,
      },
    ],
    pageSize: 10,
    current: 1,
  });
}

// 获取文件版本列表
export function getFileVersions(data: TableQueryParams) {
  // return MSR.post<CommonList<any>>({ url: FileDetailUrl, data });
  return Promise.resolve({
    total: 1,
    list: [
      {
        id: 1,
        version: 'd08hf034f3',
        record: 'fuih03h4f0ihoiw',
        creator: '创建人',
        createTime: 18975439859,
      },
      {
        id: 1,
        version: 'd08hf034f3',
        record: 'fuih03h4f0ihoiw',
        creator: '创建人',
        createTime: 18975439859,
      },
    ],
    pageSize: 10,
    current: 1,
  });
}
