import { statusCodeOptions } from '@/components/pure/ms-advance-filter/index';
import { generateTableHTML } from '@/components/pure/ms-rich-text/utils';
import { MsTableColumn } from '@/components/pure/ms-table/type';

import { getApiCaseReport, getApiCaseReportStep, getCaseDetail } from '@/api/modules/test-plan/testPlan';
import { useI18n } from '@/hooks/useI18n';

import { ResponseAssertionTableItem } from '@/models/apiTest/common';
import type { StepList } from '@/models/caseManagement/featureCase';
import { FullResponseAssertionType } from '@/enums/apiEnum';
import { CaseLinkEnum } from '@/enums/caseEnum';

import { responseAssertionTypeMap } from '@/views/api-test/components/config';
import { executionResultMap } from '@/views/case-management/caseManagementFeature/components/utils';

const { t } = useI18n();

export const detailContentMap: Record<string, string> = {
  [CaseLinkEnum.FUNCTIONAL]: '',
  [CaseLinkEnum.API]: '',
  [CaseLinkEnum.SCENARIO]: '',
};
// 获取步骤表格
function getStepsTable(steps: string) {
  const alertStatus = ['BLOCKED', 'ERROR'];
  const formatContent = (rows: StepList, key: keyof StepList) => {
    const valueKey = key as keyof StepList;
    return alertStatus.includes(rows.status as string)
      ? `<span style="color:#f00"> ${rows[valueKey] || '-'} </span>`
      : `<span> ${rows[valueKey] || '-'} </span>`;
  };
  const templateFieldColumns = [
    {
      title: 'system.orgTemplate.numberIndex',
      dataIndex: 'num',
      slotName: 'num',
      width: 30,
      format: (rows: StepList) => {
        return formatContent(rows, 'num');
      },
    },
    {
      title: 'system.orgTemplate.useCaseStep',
      slotName: 'caseStep',
      dataIndex: 'step',
      width: 200,
      format: (rows: StepList) => {
        return formatContent(rows, 'step');
      },
    },
    {
      title: 'system.orgTemplate.expectedResult',
      dataIndex: 'expected',
      slotName: 'expectedResult',
      width: 200,
      format: (rows: StepList) => {
        return formatContent(rows, 'expected');
      },
    },
    {
      title: 'system.orgTemplate.actualResult',
      dataIndex: 'actualResult',
      slotName: 'actualResult',
      width: 200,
      format: (rows: StepList) => {
        return formatContent(rows, 'actualResult');
      },
    },
    {
      title: 'system.orgTemplate.stepExecutionResult',
      dataIndex: 'executeResult',
      slotName: 'lastExecResult',
      width: 120,
      format: (rows: StepList) => {
        return formatContent(rows, 'executeResult');
      },
    },
  ];

  if (!steps.length) {
    return '-';
  }
  const stepsData = JSON.parse(steps).map((item: any, index: number) => {
    return {
      num: index + 1,
      id: item.id,
      step: item.desc,
      expected: item.result,
      actualResult: item.actualResult,
      executeResult: item.executeResult ? executionResultMap[item.executeResult].statusText : '-',
      status: item.executeResult,
    };
  });

  return generateTableHTML(templateFieldColumns, stepsData);
}
// 获取断言表格
function getAssertTable(assertions: ResponseAssertionTableItem[]) {
  const columns: MsTableColumn = [
    {
      title: 'apiTestDebug.assertionItem',
      dataIndex: 'assertionItem',
      slotName: 'assertionItem',
      width: 200,
      format: (rows: ResponseAssertionTableItem) => {
        return `<span> [${t(
          responseAssertionTypeMap[(rows as ResponseAssertionTableItem).assertionType] || 'apiTestDebug.responseBody'
        )}] ${rows.expectedValue || ''}</span>`;
      },
    },
    {
      title: 'apiTestDebug.actualValue',
      dataIndex: 'actualValue',
      slotName: 'actualValue',
      width: 200,
    },
    {
      title: 'apiTestDebug.condition',
      dataIndex: 'condition',
      slotName: 'condition',
      width: 100,
      format: (rows: ResponseAssertionTableItem) => {
        return `<span> ${
          rows.assertionType === FullResponseAssertionType.RESPONSE_TIME
            ? t('advanceFilter.operator.le')
            : t(statusCodeOptions.find((item) => item.value === rows.condition)?.label || '-')
        }</span>`;
      },
    },
    {
      title: 'apiTestDebug.expectedValue',
      dataIndex: 'expectedValue',
      slotName: 'expectedValue',
      width: 200,
    },
    {
      title: 'apiTestDebug.status',
      dataIndex: 'pass',
      slotName: 'status',
      width: 100,
      format: (rows: ResponseAssertionTableItem) => {
        return rows.pass === true
          ? `<span style="color:#0f0"> ${t('common.success')} </span>`
          : `<span style="color:#f00"> ${t('common.fail')} </span>`;
      },
    },
    {
      title: 'apiTestDebug.reason',
      dataIndex: 'message',
      slotName: 'message',
      width: 300,
    },
  ];
  return generateTableHTML(columns, assertions);
}

// 获取功能用例自动填充内容
export async function getCaseQuickContent(id: string) {
  try {
    const result = await getCaseDetail(id);
    const { prerequisite, steps, expectedResult, caseEditType, textDescription } = result;
    const getEmptyString = (value: string) => {
      const emptyString = '<p style=""></p>';
      return !value || emptyString.includes(value) ? '-' : value;
    };

    const stepData = getStepsTable(steps);
    const stepContent =
      caseEditType === 'STEP'
        ? `<p style=""><strong>${t('system.orgTemplate.stepDescription')}</strong></p>
            ${stepData}`
        : `
          <p style=""><strong>${t('system.orgTemplate.textDescription')}</strong></p>
          <p style="">${getEmptyString(textDescription)}</p>
          <p style=""><strong>${t('system.orgTemplate.expectedResult')}</strong></p>
          <p style="">${getEmptyString(expectedResult)}</p>
        `;

    // 处理步骤
    const caseContent = `
    <p style=""><strong>${t('system.orgTemplate.precondition')}</strong></p>
    <p style="">${getEmptyString(prerequisite)}</p>
    ${stepContent}
    <p style=""><strong>${t('system.orgTemplate.actualResult')}</strong></p>
    <p style="">-</p>`;
    detailContentMap[CaseLinkEnum.FUNCTIONAL] = caseContent;
  } catch (error) {
    // eslint-disable-next-line no-console
    console.log(error);
  }
}

// 获取接口用例快速填充内容
export async function getApiQuickContent(lastReportId: string) {
  let assertTable = '-';
  let reportHref = '-';
  if (!lastReportId) {
    const apiContent = `
    <p style=""><strong>${t('apiTestDebug.assertion')}</strong></p>
    ${assertTable}
    <p style="/"><strong>${t('testPlan.testPlanDetail.reportLink')}</strong></p>
    <p><span style="color:#2563eb" color="#2563eb">${reportHref}</span></p>`;
    detailContentMap[CaseLinkEnum.API] = apiContent;
    return;
  }
  try {
    const result = await getApiCaseReport(lastReportId);
    if (result) {
      const [steps] = result.children;
      const lastExecuteDetail = await getApiCaseReportStep(lastReportId, steps.stepId);
      const [stepContent] = lastExecuteDetail;

      if (stepContent?.content?.responseResult?.assertions.length) {
        assertTable = getAssertTable(stepContent?.content?.responseResult?.assertions || []);
      }
      const { origin } = window.location;

      reportHref = `${origin}/#/api-test/report?type=API_CASE&id=${lastReportId}`;

      const apiContent = `
      <p style=""><strong>${t('apiTestDebug.assertion')}</strong></p>
      ${assertTable}
      <p style="/"><strong>${t('testPlan.testPlanDetail.reportLink')}</strong></p>
      <p><span><a href="${reportHref}" link="${reportHref}">${reportHref}</a></span></p>`;
      detailContentMap[CaseLinkEnum.API] = apiContent;
    }
  } catch (error) {
    // eslint-disable-next-line no-console
    console.log(error);
  }
}
// 获取快速填充场景内容
async function getScenarioQuickContent(lastReportId: string) {
  const reportHref = lastReportId ? `${origin}/#/api-test/report?type=API_SCENARIO&id=${lastReportId}` : '-';
  const apiContent = `
  <p style=""><strong>${t('testPlan.testPlanDetail.reportLink')}</strong></p>
  <p><span><a href="${reportHref}" link="${reportHref}">${reportHref}</a></span></p>`;
  detailContentMap[CaseLinkEnum.SCENARIO] = apiContent;
}

// 获取接口用例自动填充内容
export async function getCaseTemplateContent(type: CaseLinkEnum, detailId: string): Promise<string | undefined> {
  switch (type) {
    case CaseLinkEnum.FUNCTIONAL:
      await getCaseQuickContent(detailId);
      return detailContentMap[CaseLinkEnum.FUNCTIONAL];
    case CaseLinkEnum.API:
      await getApiQuickContent(detailId);
      return detailContentMap[CaseLinkEnum.API];
    case CaseLinkEnum.SCENARIO:
      await getScenarioQuickContent(detailId);
      return detailContentMap[CaseLinkEnum.SCENARIO];
    default:
      break;
  }
}

export default {};
