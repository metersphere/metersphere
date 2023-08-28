import dayjsLocale from 'dayjs/locale/zh-cn';
import localeSettings from './settings';
import sys from './sys';
import common from './common';

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
    'menu.workbench': '工作台',
    'menu.testPlan': '测试计划',
    'menu.bugManagement': '缺陷管理',
    'menu.featureTest': '功能测试',
    'menu.apiTest': '接口测试',
    'menu.uiTest': 'UI测试',
    'menu.performanceTest': '性能测试',
    'menu.projectManagement': '项目管理',
    'menu.projectManagement.log': '日志',
    'menu.settings': '系统设置',
    'menu.settings.system': '系统',
    'menu.settings.system.user': '用户',
    'menu.settings.system.usergroup': '用户组',
    'menu.settings.system.authorizedManagement': '授权管理',
    'menu.settings.system.pluginManager': '插件管理',
    'menu.settings.system.organizationAndProject': '组织与项目',
    'menu.settings.system.resourcePool': '资源池',
    'menu.settings.system.resourcePoolDetail': '添加资源池',
    'menu.settings.system.resourcePoolEdit': '编辑资源池',
    'menu.settings.system.parameter': '系统参数',
    'menu.settings.system.log': '日志',
    'menu.settings.organization': '组织',
    'menu.settings.organization.member': '成员',
    'menu.settings.organization.userGroup': '用户组',
    'menu.settings.organization.project': '项目',
    'menu.settings.organization.serviceIntegration': '服务集成',
    'menu.settings.organization.log': '日志',
    'navbar.action.locale': '切换为中文',
    ...sys,
    ...localeSettings,
    ...result,
    ...common,
  },
  dayjsLocale,
  dayjsLocaleName: 'zh-CN',
};
