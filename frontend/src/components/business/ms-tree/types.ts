import type { TreeNodeData, TreeFieldNames } from '@arco-design/web-vue';

export interface MsTreeFieldNames extends TreeFieldNames {
  key: string;
  title: string;
  children: string;
  isLeaf: string;
}

export type MsTreeNodeData = {
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
