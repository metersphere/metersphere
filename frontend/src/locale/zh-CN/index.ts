import dayjsLocale from 'dayjs/locale/zh-cn';
import localeSettings from './settings';
import sys from './sys';

const _Cmodules: any = import.meta.glob('../../components/**/locale/zh-CN.ts', { eager: true });
const _Vmodules: any = import.meta.glob('../../views/**/locale/zh-CN.ts', { eager: true });
let result = {};
Object.keys(_Cmodules).forEach((key) => {
  const defaultModule = _Cmodules[key as any].default;
  if (!defaultModule) return;
  result = { ...result, ...defaultModule };
});
Object.keys(_Vmodules).forEach((key) => {
  const defaultModule = _Vmodules[key as any].default;
  if (!defaultModule) return;
  result = { ...result, ...defaultModule };
});
export default {
  message: {
    'menu.apiTest': '接口测试',
    'menu.settings': '系统设置',
    'menu.settings.system': '系统',
    'menu.settings.organization': '组织',
    'menu.settings.organization.member': '成员',
    'menu.settings.system.user': '用户',
    'menu.settings.system.usergroup': '用户组',
    'menu.settings.system.pluginmanger': '插件管理',
    'menu.settings.system.organizationAndProject': '组织与项目',
    'menu.settings.system.resourcePool': '资源池',
    'menu.settings.system.resourcePoolDetail': '添加资源池',
    'menu.settings.system.resourcePoolEdit': '编辑资源池',
    'navbar.action.locale': '切换为中文',
    ...sys,
    ...localeSettings,
    ...result,
  },
  dayjsLocale,
  dayjsLocaleName: 'zh-CN',
};
