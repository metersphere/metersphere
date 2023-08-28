import dayjsLocale from 'dayjs/locale/en';
import localeSettings from './settings';
import sys from './sys';
import common from './common';

const _Cmodules: any = import.meta.glob('../../components/**/locale/en-US.ts', { eager: true });
const _Vmodules: any = import.meta.glob('../../views/**/locale/en-US.ts', { eager: true });
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
    'menu.workbench': 'Workbench',
    'menu.testPlan': 'Test plan',
    'menu.bugManagement': 'Bug management',
    'menu.featureTest': 'Feature test',
    'menu.apiTest': 'API test',
    'menu.uiTest': 'UI test',
    'menu.performanceTest': 'Performance test',
    'menu.projectManagement': 'Project management',
    'menu.projectManagement.projectPermission': 'Project Permission',
    'menu.projectManagement.log': 'Log',
    'menu.settings': 'Settings',
    'menu.settings.system': 'System',
    'menu.settings.system.usergroup': 'User Group',
    'menu.settings.system.authorizedManagement': 'Authorized Management',
    'menu.settings.system.pluginManager': 'Plugin Manger',
    'menu.settings.system.user': 'User',
    'menu.settings.system.organizationAndProject': 'Org & Project',
    'menu.settings.system.resourcePool': 'Resource Pool',
    'menu.settings.system.resourcePoolDetail': 'Add resource pool',
    'menu.settings.system.resourcePoolEdit': 'Edit resource pool',
    'menu.settings.system.parameter': 'System parameter',
    'menu.settings.system.log': 'Log',
    'menu.settings.organization': 'Organization',
    'menu.settings.organization.member': 'Member',
    'menu.settings.organization.userGroup': 'User Group',
    'menu.settings.organization.project': 'Project',
    'menu.settings.organization.serviceIntegration': 'Service Integration',
    'menu.settings.organization.log': 'Log',
    'navbar.action.locale': 'Switch to English',
    ...sys,
    ...localeSettings,
    ...result,
    ...common,
  },
  dayjsLocale,
  dayjsLocaleName: 'en-US',
};
