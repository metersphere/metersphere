import Mock from 'mockjs';
import setupMock, { successResponseWrap, failResponseWrap } from '@/utils/setup-mock';

import { GetMenuListUrl, LogoutUrl, GetUserInfoUrl, LoginUrl } from '@/api/requrls/user';
import { isLogin } from '@/utils/auth';

setupMock({
  setup() {
    // 用户信息
    Mock.mock(new RegExp(GetUserInfoUrl), () => {
      if (isLogin()) {
        const role = window.localStorage.getItem('userRole') || 'admin';
        return successResponseWrap({
          name: '王立群',
          avatar: '//lf1-xgcdn-tos.pstatp.com/obj/vcloud/vadmin/start.8e0e4855ee346a46ccff8ff3e24db27b.png',
          email: 'wangliqun@email.com',
          job: 'frontend',
          jobName: '前端艺术家',
          organization: 'Frontend',
          organizationName: '前端',
          location: 'beijing',
          locationName: '北京',
          introduction: '人潇洒，性温存',
          personalWebsite: 'https://www.arco.design',
          phone: '150****0000',
          registrationDate: '2013-05-10 12:10:00',
          accountId: '15012312300',
          certification: 1,
          role,
        });
      }
      return failResponseWrap(null, '未登录', 50008);
    });

    // 登出
    Mock.mock(new RegExp(LoginUrl), () => {
      return successResponseWrap({});
    });

    // 登出
    Mock.mock(new RegExp(LogoutUrl), () => {
      return successResponseWrap(null);
    });

    // 用户的服务端菜单
    Mock.mock(new RegExp(GetMenuListUrl), () => {
      const menuList = [
        {
          path: '/api-test',
          name: 'apiTest',
          meta: {
            locale: 'menu.apiTest',
            icon: 'icon-dashboard',
            order: 0,
          },
          children: [
            {
              path: 'list',
              name: 'apiTest',
              meta: {
                locale: 'menu.apiTest',
                roles: ['*'],
                icon: 'icon-computer',
              },
            },
          ],
        },
        {
          path: '/setting',
          name: 'setting',
          meta: {
            locale: 'menu.settings',
            icon: 'icon-dashboard',
            order: 0,
          },
          children: [
            {
              path: 'system',
              name: 'settingSystem',
              redirect: '/setting/system/user',
              meta: {
                locale: 'menu.settings.system',
                roles: ['*'],
                hideChildrenInMenu: true,
              },
              children: [
                {
                  path: 'user',
                  name: 'settingSystemUser',
                  meta: {
                    locale: 'menu.settings.user',
                    roles: ['*'],
                    isTopMenu: true,
                  },
                },
                {
                  path: 'usergroup',
                  name: 'settingSystemUsergroup',
                  meta: {
                    locale: 'menu.settings.usergroup',
                    roles: ['*'],
                    isTopMenu: true,
                  },
                },
              ],
            },
          ],
        },
        {
          path: '/personal',
          name: 'personal',
          meta: {},
          children: [
            {
              path: '/personal/info',
              name: 'personalInfo',
              meta: {},
            },
          ],
        },
      ];
      return successResponseWrap(menuList);
    });
  },
});
