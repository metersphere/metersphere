import { MsTableColumnData } from '@/components/pure/ms-table/type';
import type { MsFileItem } from '@/components/pure/ms-upload/types';
import type { CaseLevel } from '@/components/business/ms-case-associate/types';

import { useI18n } from '@/hooks/useI18n';

import type { AssociatedList, CustomAttributes } from '@/models/caseManagement/featureCase';
import { StatusType } from '@/enums/caseEnum';

const { t } = useI18n();

// 获取列表对应的状态图标
const statusIconMap = [
  {
    key: 'UN_REVIEWED',
    icon: StatusType.UN_REVIEWED,
    statusText: t('caseManagement.featureCase.notReviewed'),
  },
  {
    key: 'UNDER_REVIEWED',
    icon: StatusType.UNDER_REVIEWED,
    statusText: t('caseManagement.featureCase.reviewing'),
  },
  {
    key: 'PASS',
    icon: StatusType.PASS,
    statusText: t('caseManagement.featureCase.passed'),
  },
  {
    key: 'UN_PASS',
    icon: StatusType.UN_PASS,
    statusText: t('caseManagement.featureCase.notPass'),
  },
  {
    key: 'RE_REVIEWED',
    icon: StatusType.RE_REVIEWED,
    statusText: t('caseManagement.featureCase.retrial'),
  },
  {
    key: 'UN_EXECUTED',
    icon: StatusType.UN_EXECUTED,
    statusText: t('caseManagement.featureCase.nonExecution'),
  },
  {
    key: 'PASSED',
    icon: StatusType.PASSED,
    statusText: t('caseManagement.featureCase.passed'),
  },
  {
    key: 'FAILED',
    icon: StatusType.FAILED,
    statusText: t('caseManagement.featureCase.failure'),
  },
  {
    key: 'BLOCKED',
    icon: StatusType.BLOCKED,
    statusText: t('caseManagement.featureCase.chokeUp'),
  },
  {
    key: 'SKIPPED',
    icon: StatusType.SKIPPED,
    statusText: t('caseManagement.featureCase.skip'),
  },
];

/** *
 *
 * @description 获取对应的状态文本
 * @param {status} 列表状态
 */
export function getStatusText(status: keyof typeof StatusType) {
  const currentStatus = statusIconMap.find((item) => item.key === status);
  return {
    iconType: currentStatus?.icon,
    statusType: currentStatus?.statusText,
  };
}
/** *
 *
 * @description 获取状态对应颜色
 * @param {status} 列表状态
 */
export function getReviewStatusClass(status: keyof typeof StatusType) {
  const grayColor = ['UN_REVIEWED', 'UN_EXECUTED'];
  const yellowColor = ['RE_REVIEWED', 'BLOCKED'];
  const blueColor = ['UNDER_REVIEWED', 'SKIPPED'];
  if (grayColor.includes(status)) {
    return 'text-[var(--color-text-brand)]';
  }
  if (yellowColor.includes(status)) {
    return 'text-[rgb(var(--warning-6))]';
  }
  if (blueColor.includes(status)) {
    return 'text-[rgb(var(--link-6))]';
  }
}
/** *
 *
 * @description 将文件信息转换为文件格式
 * @param {stafileInfotus} 文件file
 */

export function convertToFile(fileInfo: AssociatedList): MsFileItem {
  const gatewayAddress = `${window.location.protocol}//${window.location.hostname}:${window.location.port}`;
  const fileName = fileInfo.fileType ? `${fileInfo.name}.${fileInfo.fileType || ''}` : `${fileInfo.name}`;
  const type = fileName.split('.')[1];
  const file = new File([new Blob()], `${fileName}`, {
    type: `application/${type}`,
  });
  Object.defineProperty(file, 'size', { value: fileInfo.size });
  const { id, local, isUpdateFlag, associateId } = fileInfo;
  return {
    enable: fileInfo.enable || false,
    file,
    name: fileName,
    percent: 0,
    status: 'done',
    uid: id,
    url: `${gatewayAddress}/${fileInfo.filePath || ''}`,
    local,
    deleteContent: local ? '' : 'caseManagement.featureCase.cancelLink',
    isUpdateFlag,
    associateId,
  };
}

// 返回用例等级
export function getCaseLevels(customFields: CustomAttributes[]): CaseLevel {
  const caseLevelItem = (customFields || []).find((it: any) => it.internal && it.fieldName === '用例等级');
  return (
    (caseLevelItem?.options.find((it: any) => it.value === caseLevelItem.defaultValue)?.text as CaseLevel) ||
    ('P0' as CaseLevel)
  );
}

// 处理自定义字段
export function getTableFields(customFields: CustomAttributes[], itemDataIndex: MsTableColumnData) {
  const multipleExcludes = ['MULTIPLE_SELECT', 'CHECKBOX', 'MULTIPLE_MEMBER'];
  const selectExcludes = ['MEMBER', 'RADIO', 'SELECT'];

  const currentColumnData: CustomAttributes | undefined = customFields.find(
    (item: any) => itemDataIndex.dataIndex === item.fieldId
  );

  if (currentColumnData) {
    // 处理多选项
    if (multipleExcludes.includes(currentColumnData.type)) {
      const selectValue = JSON.parse(currentColumnData.defaultValue);
      return (
        (currentColumnData.options || [])
          .filter((item: any) => selectValue.includes(item.value))
          .map((it: any) => it.text)
          .join(',') || '-'
      );
    }
    if (currentColumnData.type === 'MULTIPLE_INPUT') {
      // 处理标签形式
      return JSON.parse(currentColumnData.defaultValue).join('，') || '-';
    }
    if (selectExcludes.includes(currentColumnData.type)) {
      return (
        (currentColumnData.options || [])
          .filter((item: any) => currentColumnData.defaultValue === item.value)
          .map((it: any) => it.text)
          .join() || '-'
      );
    }
    return currentColumnData.defaultValue || '-';
  }
}

export default {};
