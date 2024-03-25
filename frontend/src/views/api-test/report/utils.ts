import type { ScenarioItemType } from '@/models/apiTest/report';

export function addFoldField(node: ScenarioItemType) {
  if (node.children && node.children.length > 0) {
    node.fold = true;
    node.children.forEach((child: ScenarioItemType) => {
      addFoldField(child);
    });
  } else {
    node.fold = true;
  }
}

// 是否为计算中
export function getIndicators(value: any) {
  return value === 'Calculating' ? '-' : value || 0;
}
export default {};
