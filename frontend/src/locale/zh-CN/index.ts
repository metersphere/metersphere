import dayjsLocale from 'dayjs/locale/zh-cn';
import localeSettings from './settings';
import sys from './sys';
import localeMessageBox from '@/components/message-box/locale/zh-CN';
import minder from '@/components/minder-editor/locale/zh-CN';
import localeLogin from '@/views/login/locale/zh-CN';
import localeWorkplace from '@/views/dashboard/workplace/locale/zh-CN';
import localeThemebox from '@/components/theme-box/locale/zh-CN';
import localeApiTest from '@/views/api-test/locale/zh-CN';

export default {
  message: {
    'menu.component': '组件库',
    'menu.component.demo': '组件示例',
    'menu.apitest': '接口测试',
    'menu.dashboard': '仪表盘',
    'menu.minder': '脑图',
    'menu.server.dashboard': '仪表盘-服务端',
    'menu.server.workplace': '工作台-服务端',
    'menu.server.monitor': '实时监控-服务端',
    'menu.list': '列表页',
    'menu.result': '结果页',
    'menu.exception': '异常页',
    'menu.form': '表单页',
    'menu.profile': '详情页',
    'menu.visualization': '数据可视化',
    'menu.user': '个人中心',
    'navbar.action.locale': '切换为中文',
    ...sys,
    ...localeSettings,
    ...localeMessageBox,
    ...localeLogin,
    ...localeWorkplace,
    ...minder,
    ...localeThemebox,
    ...localeApiTest,
  },
  dayjsLocale,
  dayjsLocaleName: 'zh-CN',
};
