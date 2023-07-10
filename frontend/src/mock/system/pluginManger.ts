import Mock from 'mockjs';
import setupMock, { successTableResponseWrap } from '@/utils/setup-mock';

const getPluginList = () => {
  return [
    {
      id: '1-1',
      name: '插件一',
      describe: '插件一',
      enable: true,
      createTime: 'number',
      updateTime: 'number',
      jarPackage: 'string',
      version: 'string',
      applicationScene: 'string',
      createUser: 'string',
      updateUser: 'string',
      children: [
        {
          id: '1-1-1',
          name: '插件1-1',
          describe: '插件1-1',
          enable: true,
          createTime: 'number',
          updateTime: 'number',
          jarPackage: 'string',
          version: 'string',
          applicationScene: 'string',
          createUser: 'string',
          updateUser: 'string',
        },
      ],
    },
    {
      id: '2-1',
      name: '插件一',
      describe: '插件一',
      enable: true,
      createTime: 'number',
      updateTime: 'number',
      jarPackage: 'string',
      version: 'string',
      applicationScene: 'string',
      createUser: 'string',
      updateUser: 'string',
      children: [
        {
          id: '2-1-1',
          name: '插件2-1',
          describe: '插件2-1',
          enable: true,
          createTime: 'number',
          updateTime: 'number',
          jarPackage: 'string',
          version: 'string',
          applicationScene: 'string',
          createUser: 'string',
          updateUser: 'string',
        },
      ],
    },
  ];
};

setupMock({
  setup: () => {
    Mock.mock(new RegExp('/plugin/page'), () => {
      return successTableResponseWrap(getPluginList());
    });
  },
});
