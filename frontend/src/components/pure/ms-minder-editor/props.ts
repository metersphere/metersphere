/**
 * Api 列表
 */

export const mainEditorProps = {
  importJson: {
    type: Object,
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
  priorityDisableCheck: Function,
  operators: [],
};

export const tagProps = {
  tags: {
    // 自定义标签
    type: Array<string>,
    default() {
      return [] as string[];
    },
  },
  distinctTags: {
    // 个别标签二选一
    type: Array<string>,
    default() {
      return [] as string[];
    },
  },
  tagDisableCheck: Function,
  tagEditCheck: Function,
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
  delConfirm: {
    type: Function,
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
