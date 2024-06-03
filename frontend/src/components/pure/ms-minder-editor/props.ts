/**
 * Api 列表
 */

import type { MoveMode } from '@/models/common';

import type { PropType } from 'vue';

export interface MinderClass {
  stopPropagation: () => void; // 阻止事件冒泡
  stopPropagationImmediately: () => void; // 立即阻止事件冒泡
  [key: string]: any; // TODO: 其他事件属性
}
// TODO:脑图事件类型补充
export interface MinderEvent extends MinderClass {
  command: any;
  commandArgs: Record<string, any>[];
  commandName: string;
  minder: any;
  type: string;
}
export interface MinderIconButtonItem {
  icon: string;
  tooltip: string;
  eventTag: string;
}
export interface MinderJsonNodeData {
  id: string;
  text: string;
  resource?: string[];
  expandState?: 'collapse' | 'expand';
  priority?: number;
  // 前端渲染字段
  isNew?: boolean; // 是否脑图新增节点，需要在初始化脑图数据时标记已存在节点为 false 以区分是否新增节点
  changed?: boolean; // 脑图节点是否发生过变化
  moveMode?: MoveMode; // 移动方式（节点移动或新增时需要）
  targetId?: string; // 目标节点 id（节点移动或新增时需要）
  [key: string]: any;
}
export interface MinderJsonNode {
  data: MinderJsonNodeData;
  parent?: MinderJsonNode;
  children?: MinderJsonNode[];
  [key: string]: any; // minder 内置字段
}

export interface MinderJson {
  root: MinderJsonNode;
  template: string;
  treePath: Record<string, MinderJsonNode>[];
}

export const mainEditorProps = {
  importJson: {
    type: Object as PropType<MinderJson>,
    default() {
      return {
        root: {},
        template: 'default',
        treePath: [] as any[],
      };
    },
  },
  height: {
    type: Number,
    default: 500,
  },
  disabled: Boolean,
  extractContentTabList: Array as PropType<{ label: string; value: string }[]>,
};

export const headerProps = {
  iconButtons: {
    type: [] as PropType<MinderIconButtonItem[]>,
  },
};

export const priorityProps = {
  priorityCount: {
    type: Number,
    default: 4,
    validator: (value: number) => {
      // 优先级最多支持 9 个级别
      return value <= 9;
    },
  },
  priorityStartWithZero: {
    // 优先级是否从0开始
    type: Boolean,
    default: true,
  },
  priorityPrefix: {
    // 优先级显示的前缀
    type: String,
    default: 'P',
  },
  priorityDisableCheck: Function as PropType<(node: MinderJsonNode) => boolean>,
  operators: [],
};

export interface MinderReplaceTag {
  tags: string[];
  condition: (node: MinderJsonNode, tags: string[]) => boolean;
}
export const tagProps = {
  tags: {
    // 自定义标签
    type: Array<string>,
    default() {
      return [];
    },
  },
  distinctTags: {
    // 个别标签二选一
    type: Array<string>,
    default() {
      return [];
    },
  },
  singleTag: {
    // 单标签
    type: Boolean,
    default: false,
  },
  replaceableTags: Function as PropType<(node: MinderJsonNode, nodes: MinderJsonNode[]) => string[]>,
  tagDisableCheck: Function,
  tagEditCheck: Function as PropType<(node: MinderJsonNode, tag: string) => boolean>,
  afterTagEdit: Function as PropType<(node: MinderJsonNode, tag: string) => void>,
};

export const insertProps = {
  insertNode: {
    type: Function as PropType<(node: MinderJsonNode, type: string) => void>,
    default: undefined,
  },
};

export const editMenuProps = {
  sequenceEnable: {
    type: Boolean,
    default: true,
  },
  tagEnable: {
    type: Boolean,
    default: true,
  },
  progressEnable: {
    type: Boolean,
    default: true,
  },
  moveEnable: {
    type: Boolean,
    default: true,
  },
  moveConfirm: {
    type: Function,
    default: null,
  },
};

export const moleProps = {
  // 默认样式
  defaultMold: {
    type: Number,
    default: 3,
  },
};

export const delProps = {
  // 节点删除确认
  delConfirm: {
    type: Function as PropType<(node: MinderJsonNode) => void>,
    default: null,
  },
};

export const viewMenuProps = {
  viewMenuEnable: {
    type: Boolean,
    default: true,
  },
  moldEnable: {
    type: Boolean,
    default: true,
  },
  arrangeEnable: {
    type: Boolean,
    default: true,
  },
  styleEnable: {
    type: Boolean,
    default: true,
  },
  fontEnable: {
    type: Boolean,
    default: true,
  },
};
