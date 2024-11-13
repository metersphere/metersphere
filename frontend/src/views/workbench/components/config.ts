import { FeatureEnum } from '@/enums/workbenchEnum';

export const featuresMap = {
  [FeatureEnum.API_CASE]: 'apiTest',
  [FeatureEnum.API_SCENARIO]: 'apiTest',
  [FeatureEnum.CASE_REVIEW]: 'caseManagement',
  [FeatureEnum.TEST_CASE]: 'caseManagement',
  [FeatureEnum.TEST_PLAN]: 'testPlan',
  [FeatureEnum.BUG]: 'bugManagement',
};

export default {};
