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
    'menu.workbenchHomeSort': 'Home page',
    'menu.workbenchWaitSort': 'My to do',
    'menu.workbenchFollowSort': 'My follow',
    'menu.workbenchCreatedSort': 'My created',
    'menu.testPlan': 'Test Plan',
    'menu.testPlanGroup': 'Planning groups',
    'menu.testPlanShort': 'Plan',
    'menu.testPlan.testPlanDetail': 'Test plan details',
    'menu.bugManagement': 'Bug',
    'menu.bugManagementShort': 'Bug',
    'menu.bugManagement.bugDetail': 'Bug',
    'menu.bugManagement.bugRecycle': 'Recycle',
    'menu.caseManagement': 'Functional',
    'menu.apiTest': 'API Test',
    'menu.caseManagementShort': 'Case',
    'menu.apiTestShort': 'API',
    'menu.apiTest.debug': 'Debug',
    'menu.apiTest.debug.debug': 'Debug',
    'menu.apiTest.management': 'API',
    'menu.apiTest.management.definition': 'API',
    'menu.apiTest.api': 'API List',
    'menu.apiTest.scenario': 'Scenario',
    'menu.apiTest.apiScenario': 'Scenario',
    'menu.apiTest.scenario.recycle': 'Recycle',
    'menu.apiTest.report': 'Report',
    'menu.apiTest.reportTestPlan': 'Test plan report',
    'menu.apiTest.reportTestGroupPlan': 'Test plan group report',
    'menu.apiTest.reportDetail': 'Report Detail',
    'menu.taskCenter': 'Task Center',
    'menu.uiTest': 'UI Test',
    'menu.performanceTest': 'Performance Test',
    'menu.projectManagement': 'Project',
    'menu.projectManagementShort': 'Project',
    'menu.projectManagement.fileManagement': 'File',
    'menu.projectManagement.messageManagement': 'Message',
    'menu.projectManagement.commonScript': 'Common Script',
    'menu.projectManagement.fakeError': 'Fake Error',
    'menu.projectManagement.messageManagementEdit': 'Update Template',
    'menu.caseManagement.featureCase': 'Feature Case',
    'menu.caseManagement.featureCaseRecycle': 'Recycle',
    'menu.caseManagement.featureCaseList': 'Case list',
    'menu.caseManagement.featureCaseDetail': 'Create Case',
    'menu.caseManagement.featureCaseEdit': 'Update Case',
    'menu.caseManagement.featureCaseCreateSuccess': 'Create Success',
    'menu.caseManagement.caseManagementReview': 'Feature Case Review',
    'menu.caseManagement.caseManagementReviewShort': 'Review',
    'menu.caseManagement.caseManagementReviewCreate': 'Create Review',
    'menu.caseManagement.caseManagementReviewDetail': 'Review Detail',
    'menu.caseManagement.caseManagementReviewDetailCaseDetail': 'Review Case Detail',
    'menu.caseManagement.caseManagementCaseReviewEdit': 'Update Review',
    'menu.caseManagement.caseManagementCaseDetail': 'Case Detail',
    'menu.loadTest': 'Performance Test',
    'menu.projectManagement.projectPermission': 'Project Permission',
    'menu.projectManagement.log': 'Log',
    'menu.projectManagement.taskCenter': 'Task center',
    'menu.projectManagement.environmentManagement': 'Environment',
    'menu.settings': 'Settings',
    'menu.settingsShort': 'System',
    'menu.settings.system': 'System',
    'menu.settings.system.usergroup': 'User Group',
    'menu.settings.system.authorizedManagement': 'Authorized',
    'menu.settings.system.pluginManager': 'Plugin',
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
    'menu.projectManagement.templateManager': 'Template',
    ...sys,
    ...localeSettings,
    ...result,
    ...common,
  },
  dayjsLocale,
  dayjsLocaleName: 'en-US',
};
