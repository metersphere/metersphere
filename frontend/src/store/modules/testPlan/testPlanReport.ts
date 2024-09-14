import { defineStore } from 'pinia';

import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

export interface testSetConfigType {
  TEST_PLAN: Record<keyof typeof ReportCardTypeEnum, boolean>;
  GROUP: Record<keyof typeof ReportCardTypeEnum, boolean>;
}

const useTestPlanReportStore = defineStore('testPlanReport', {
  persist: true,
  state: (): {
    testSetConfig: Record<string, Record<string, boolean>>;
  } => ({
    testSetConfig: {
      TEST_PLAN: {
        [ReportCardTypeEnum.FUNCTIONAL_DETAIL]: false,
        [ReportCardTypeEnum.API_CASE_DETAIL]: false,
        [ReportCardTypeEnum.SCENARIO_CASE_DETAIL]: false,
      },
      GROUP: {
        [ReportCardTypeEnum.FUNCTIONAL_DETAIL]: false,
        [ReportCardTypeEnum.API_CASE_DETAIL]: false,
        [ReportCardTypeEnum.SCENARIO_CASE_DETAIL]: false,
      },
    },
  }),
  actions: {
    setTestStatus(isGroup: boolean, enable: boolean, cardType: ReportCardTypeEnum) {
      const testSetKey: 'TEST_PLAN' | 'GROUP' = isGroup ? 'GROUP' : 'TEST_PLAN';
      this.testSetConfig[testSetKey][cardType as keyof typeof ReportCardTypeEnum] = enable;
    },
    getTestStatus(isGroup: boolean, cardType: ReportCardTypeEnum) {
      const testSetKey: 'TEST_PLAN' | 'GROUP' = isGroup ? 'GROUP' : 'TEST_PLAN';
      return this.testSetConfig[testSetKey][cardType as keyof typeof ReportCardTypeEnum];
    },
  },
});

export default useTestPlanReportStore;
