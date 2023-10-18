import type { TreeFieldNames, TreeNodeData } from '@arco-design/web-vue';

export interface MsTreeFieldNames extends TreeFieldNames {
  key: string;
  title: string;
  children: string;
  isLeaf: string;
  [key: string]: any;
}

export type MsTreeNodeData = {
  hideMoreAction?: boolean; // 隐藏更多操作
  [key: string]: any;
} & TreeNodeData;

export interface MsTreeNodeStatus {
  loading: boolean;
  checked: boolean;
  selected: boolean;
  indeterminate: boolean;
  expanded: boolean;
  isLeaf: boolean;
}

export interface MsTreeSelectedData {
  selected?: boolean;
  selectedNodes: MsTreeNodeData[];
  node?: MsTreeNodeData;
  e?: Event;
}
