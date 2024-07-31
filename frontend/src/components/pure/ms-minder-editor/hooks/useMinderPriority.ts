import { isDisableNode, setPriorityView } from '../script/tool/utils';

export interface PriorityProps {
  priorityStartWithZero: boolean;
  priorityPrefix: string;
  customPriority?: boolean;
  priorityDisableCheck?: (node: any) => boolean;
}

export default function usePriority(options: PriorityProps) {
  const priorityDisabled = ref(true);
  function isDisable(): boolean {
    if (Object.keys(window.minder).length === 0) return true;
    nextTick(() => {
      setPriorityView(options.priorityStartWithZero, options.priorityPrefix);
    });
    const node = window.minder.getSelectedNode();
    if (isDisableNode(window.minder) || !node || node.parent === null) {
      return true;
    }
    if (options.priorityDisableCheck) {
      return options.priorityDisableCheck(node);
    }
    return !!window.minder.queryCommandState && window.minder.queryCommandState('priority') === -1;
  }

  function setPriority(value?: string) {
    if (value && !priorityDisabled.value) {
      window.minder.execCommand('priority', value);
      setPriorityView(options.priorityStartWithZero, options.priorityPrefix);
    } else if (window.minder.execCommand && !priorityDisabled.value) {
      window.minder.execCommand('priority');
    }
  }

  onMounted(() => {
    nextTick(() => {
      const freshFuc = setPriorityView;
      if (window.minder && !options.customPriority) {
        window.minder.on('contentchange', () => {
          // 异步执行，否则执行完，还会被重置
          setTimeout(() => {
            freshFuc(options.priorityStartWithZero, options.priorityPrefix);
          }, 0);
        });
        window.minder.on('selectionchange', () => {
          priorityDisabled.value = isDisable();
        });
      }
    });
  });

  return {
    priorityDisabled,
    setPriority,
  };
}
