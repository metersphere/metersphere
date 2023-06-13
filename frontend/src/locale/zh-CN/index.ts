import dayjsLocale from 'dayjs/locale/zh-cn';
import localeSettings from './settings';
import sys from './sys';
import localeMessageBox from '@/components/pure/message-box/locale/zh-CN';
import minder from '@/components/pure/minder-editor/locale/zh-CN';
import localeLogin from '@/views/login/locale/zh-CN';
import localeTable from '@/components/pure/ms-table/locale/zh-CN';
import localeApiTest from '@/views/api-test/locale/zh-CN';
import localeSystem from '@/views/system/locale/zh-CN';

export default {
  message: {
    'menu.apiTest': '接口测试',
    'menu.settings': '系统设置',
    'menu.settings.user': '用户',
    'menu.settings.usergroup': '用户组',
    'menu.settings.organization': '组织',
    'menu.user': '个人中心',
    'navbar.action.locale': '切换为中文',
    ...sys,
    ...localeSettings,
    ...localeMessageBox,
    ...localeLogin,
    ...minder,
    ...localeTable,
    ...localeApiTest,
    ...localeSystem,
  },
  dayjsLocale,
  dayjsLocaleName: 'zh-CN',
};
