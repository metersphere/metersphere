/**
 * Api 列表
 */

import { ModeType } from '@/store/modules/components/minder-editor/types';

import type { MoveMode } from '@/models/common';
import { MinderKeyEnum } from '@/enums/minderEnum';

import type { PropType } from 'vue';

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
  disabled?: boolean; // 是否禁用
  [key: string]: any;
}
export interface MinderJsonNode {
  data?: MinderJsonNodeData;
  parent?: MinderJsonNode;
  children?: MinderJsonNode[];
  type?: string; // 节点类型，root为根节点，其他为普通节点
  [key: string]: any; // minder 内置字段
}

export interface MinderJson {
  root: MinderJsonNode;
  template?: ModeType;
  treePath: MinderJsonNodeData[];
}
// 脑图类
export interface MinderClass {
  stopPropagation: () => void; // 阻止事件冒泡
  stopPropagationImmediately: () => void; // 立即阻止事件冒泡
  [key: string]: any; // TODO: 其他事件属性
}
// TODO:脑图事件类型补充
export interface MinderEvent extends MinderClass {
  command: any;
  commandArgs: ((Record<string, any> & MinderJsonNode) | (Record<string, any> & MinderJsonNode)[] | number)[];
  commandName: string;
  minder: any;
  type: string;
}

export const mainEditorProps = {
  height: {
    type: Number,
    default: 500,
  },
  minderKey: String as PropType<MinderKeyEnum>,
  disabled: Boolean,
  extractContentTabList: Array as PropType<{ label: string; value: string }[]>,
  disabledExtraTab: Boolean,
  insertNode: {
    type: Function as PropType<(node: MinderJsonNode, type: string, value?: string) => void>,
    default: undefined,
  },
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
  priorityTooltip: {
    type: String,
    default: '',
  },
};
export const priorityColorMap: Record<number, string> = {
  1: 'rgb(var(--danger-6))',
  2: 'rgb(var(--link-6))',
  3: 'rgb(var(--success-6))',
  4: 'rgb(var(--warning-6))',
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
  replaceableTags: Function as PropType<(nodes: MinderJsonNode[]) => string[]>,
  tagDisableCheck: Function,
  tagEditCheck: Function as PropType<(nodes: MinderJsonNode[], tag: string) => boolean>,
  afterTagEdit: Function as PropType<(nodes: MinderJsonNode[], tag: string) => void>,
};

export interface MinderMenuItem {
  value: string;
  label: string;
}
export interface MoreMenuOtherOperationItem {
  value: string;
  label: string;
  permission?: string[];
  onClick: () => void;
}
export const floatMenuProps = {
  // 插入同级选项
  insertSiblingMenus: {
    type: Array as PropType<MinderMenuItem[]>,
    default() {
      return [];
    },
  },
  // 插入子级选项
  insertSonMenus: {
    type: Array as PropType<MinderMenuItem[]>,
    default() {
      return [];
    },
  },
  // 是否显示更多菜单
  canShowMoreMenu: {
    type: Boolean,
    default: true,
  },
  // 是否显示更多菜单里的[复制、粘贴、剪切、删除]操作
  canShowMoreMenuNodeOperation: {
    type: Boolean,
    default: true,
  },
  // 不显示更多菜单时，是否显示删除菜单
  canShowDeleteMenu: {
    type: Boolean,
    default: false,
  },
  // 是否显示进入节点
  canShowEnterNode: {
    type: Boolean,
    default: false,
  },
  // 是否显示粘贴菜单
  canShowPasteMenu: {
    type: Boolean,
    default: true,
  },
  // 更多菜单里自定义操作
  moreMenuOtherOperationList: {
    type: Array as PropType<MoreMenuOtherOperationItem[]>,
    default() {
      return [];
    },
  },
  // 是否显示等级菜单
  canShowPriorityMenu: {
    type: Boolean,
    default: true,
  },
  // 节点可选标签集合
  replaceableTags: {
    type: Function as PropType<(nodes: MinderJsonNode[]) => string[]>,
  },
  // 是否显示浮动菜单
  canShowFloatMenu: {
    type: Boolean,
    default: true,
  },
  // 是否自定义优先级
  customPriority: {
    type: Boolean,
    default: false,
  },
};

export interface MinderDropdownListItem {
  value: string;
  label: string;
  permission?: string[];
  onClick?: () => void;
}

export const dropdownMenuProps = {
  // 是否显示Dropdown
  canShowDropdown: {
    type: Boolean,
    default: false,
  },
  dropdownList: {
    type: Array as PropType<MinderDropdownListItem[]>,
    default() {
      return [];
    },
  },
  checkedVal: {
    type: String as PropType<string>,
    default: undefined,
  },
};
export const batchMenuProps = {
  canShowMoreBatchMenu: {
    type: Boolean,
    default: false,
  },
  canShowBatchCopy: {
    type: Boolean,
    default: false,
  },
  canShowBatchCut: {
    type: Boolean,
    default: false,
  },
  canShowBatchDelete: {
    type: Boolean,
    default: false,
  },
  canShowBatchExpand: {
    type: Boolean,
    default: false,
  },
  customBatchExpand: {
    type: Function as PropType<(node: MinderJsonNode) => void>,
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

export const navigatorProps = {
  // 显示的快捷键列表
  shortcutList: {
    type: Array as PropType<string[]>,
    default() {
      return ['expand', 'addSibling', 'addChild', 'delete', 'cut', 'copy', 'paste', 'enter'];
    },
  },
};
