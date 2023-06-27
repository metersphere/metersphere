import Mock from 'mockjs';
import setupMock, { successResponseWrap } from '@/utils/setup-mock';

const getProjectList = () => {
  return [
    {
      id: '0283f238hf2',
      num: 0,
      organizationId: 'v3v4h434c3',
      name: '发了多少',
      description: 'string',
      createTime: 0,
      updateTime: 0,
      updateUser: 'string',
      createUser: 'string',
      deleteTime: 0,
      deleted: true,
      deleteUser: 'string',
      enable: true,
    },
    {
      id: 'f9h832',
      num: 0,
      organizationId: 'v3v4h434c3',
      name: '你了大 V',
      description: 'string',
      createTime: 0,
      updateTime: 0,
      updateUser: 'string',
      createUser: 'string',
      deleteTime: 0,
      deleted: true,
      deleteUser: 'string',
      enable: true,
    },
    {
      id: '0v023i92',
      num: 0,
      organizationId: 'v3v4h434c3',
      name: '代付款就是快递方式觉得都是就',
      description: 'string',
      createTime: 0,
      updateTime: 0,
      updateUser: 'string',
      createUser: 'string',
      deleteTime: 0,
      deleted: true,
      deleteUser: 'string',
      enable: true,
    },
  ];
};

setupMock({
  setup: () => {
    Mock.mock(new RegExp('/system/project/list'), () => {
      return successResponseWrap(getProjectList());
    });
  },
});
