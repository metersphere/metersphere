import { useI18n } from '@/hooks/useI18n';

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

export default {};
