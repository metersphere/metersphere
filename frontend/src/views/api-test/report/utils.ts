import { useI18n } from '@/hooks/useI18n';

import type { ScenarioItemType } from '@/models/apiTest/report';
import { TriggerModeLabelEnum } from '@/enums/reportEnum';

const { t } = useI18n();
export function addFoldField(node: ScenarioItemType) {
  if (node.children && node.children.length > 0) {
    node.fold = true;
    node.index = node.sort;
    node.children.forEach((child: ScenarioItemType) => {
      addFoldField(child);
    });
  } else {
    node.fold = true;
    node.index = node.sort;
  }
}

// 是否为计算中
export function getIndicators(value: any) {
  if (value === 'Calculating') {
    return 'Calculating';
  }
  if (value === null) {
    return 0;
  }
  return value;
}

export const triggerModeOptions = [
  {
    value: TriggerModeLabelEnum.MANUAL,
    label: t('report.trigger.manual'),
  },
  {
    value: TriggerModeLabelEnum.SCHEDULE,
    label: t('report.trigger.scheduled'),
  },
  {
    value: TriggerModeLabelEnum.BATCH,
    label: t('report.trigger.batch.execution'),
  },
  {
    value: TriggerModeLabelEnum.API,
    label: t('report.trigger.interface'),
  },
];

export default {};
