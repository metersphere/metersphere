import common from './common';
import localeSettings from './settings';
import sys from './sys';
import dayjsLocale from 'dayjs/locale/en';

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
    'menu.testPlan': 'Test Plan',
    'menu.bugManagement': 'Bug',
    'menu.caseManagement': 'Case Management',
    'menu.apiTest': 'API Test',
    'menu.apiTest.debug': 'API debug',
    'menu.apiTest.debug.debug': 'Debug',
    'menu.apiTest.management': 'API Management',
    'menu.apiTest.report': 'API Report',
    'menu.uiTest': 'UI Test',
    'menu.performanceTest': 'Performance Test',
    'menu.projectManagement': 'Project',
    'menu.projectManagement.fileManagement': 'File Management',
    'menu.projectManagement.messageManagement': 'Message Management',
    'menu.projectManagement.commonScript': 'Common Script',
    'menu.projectManagement.messageManagementEdit': 'Update Template',
    'menu.caseManagement.featureCase': 'Feature Case',
    'menu.caseManagement.featureCaseRecycle': 'Recycle',
    'menu.caseManagement.featureCaseList': 'Case list',
    'menu.caseManagement.featureCaseDetail': 'Create Case',
    'menu.caseManagement.featureCaseEdit': 'Update Case',
    'menu.caseManagement.featureCaseCreateSuccess': 'Create Success',
    'menu.caseManagement.caseManagementReview': 'Feature Case Review',
    'menu.caseManagement.caseManagementReviewCreate': 'Create Review',
    'menu.caseManagement.caseManagementReviewDetail': 'Review Detail',
    'menu.caseManagement.caseManagementReviewDetailCaseDetail': 'Review Case Detail',
    'menu.caseManagement.caseManagementCaseReviewEdit': 'Update Review',
    'menu.caseManagement.caseManagementCaseDetail': 'Case Detail',
    'menu.workstation': 'Workstation',
    'menu.loadTest': 'Performance Test',
    'menu.projectManagement.projectPermission': 'Project Permission',
    'menu.projectManagement.log': 'Log',
    'menu.projectManagement.taskCenter': 'The task center',
    'menu.projectManagement.environmentManagement': 'EnvironmentManagement',
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
    'menu.settings.system.parameter': 'System Parameter',
    'menu.settings.system.log': 'Log',
    'menu.settings.organization': 'Organization',
    'menu.settings.organization.member': 'Member',
    'menu.settings.organization.userGroup': 'User Group',
    'menu.settings.organization.project': 'Project',
    'menu.settings.organization.template': 'Template',
    'menu.settings.organization.bugTemplate': 'BUG Template',
    'menu.settings.organization.templateFieldSetting': 'fieldSetting',
    'menu.settings.organization.templateManagementList': 'Template list',
    'menu.settings.organization.templateManagementEdit': 'Update Template',
    'menu.settings.organization.templateManagementDetail': 'Create Template',
    'menu.settings.organization.templateManagementWorkFlow': 'WorkFlow Setting',
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
