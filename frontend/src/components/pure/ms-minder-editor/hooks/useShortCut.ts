import type { MinderJsonNode } from '../props';
import useMinderOperation, { type MinderOperationProps } from './useMinderOperation';

type ShortcutKey =
  | 'save'
  | 'expand'
  | 'enter'
  | 'appendSiblingNode'
  | 'appendChildNode'
  | 'undo'
  | 'redo'
  | 'delete'
  | 'executeToSuccess'
  | 'executeToBlocked'
  | 'executeToError'
  | 'addChildCase'
  | 'addChildModule'
  | 'addSiblingModule'
  | 'addSiblingCase'
  | 'reviewPass'
  | 'reviewReject'
  | 'input';
// 快捷键事件映射，combinationShortcuts中定义了组合键事件，key为组合键，value为事件名称；
type Shortcuts = {
  [key in ShortcutKey]?: (event: KeyboardEvent) => void;
};

export default function useShortCut(shortcuts: Shortcuts, options: MinderOperationProps) {
  const { minderCopy, minderCut, minderPaste } = useMinderOperation(options);

  const handleKeyDown = (event: KeyboardEvent) => {
    const nodes: MinderJsonNode[] = window.minder.getSelectedNodes();
    if (nodes.length === 0) {
      return;
    }
    const { editor } = window;
    const { fsm } = editor;
    const state = fsm.state();
    switch (state) {
      case 'input': {
        // 输入状态下不响应快捷键
        return;
      }
      default:
    }
    const key = event.key.toLowerCase();
    const isCtrlOrCmd = event.ctrlKey || event.metaKey;
    const isShift = event.shiftKey;

    // 定义组合键事件
    const combinationShortcuts: { [key: string]: ShortcutKey } = {
      // z: 'undo', // 撤销 TODO:暂时不上撤销和重做
      // y: 'redo', // 重做
      enter: 'enter', // 进入节点
    };
    // 定义单键事件
    const singleShortcuts: { [key: string]: ShortcutKey } = {
      '/': 'expand', // 展开/折叠
      'enter': 'appendSiblingNode',
      'tab': 'appendChildNode',
      'backspace': 'delete',
    };
    // 业务快捷键
    const businessShortcuts: { [key: string]: ShortcutKey } = {
      s: 'executeToSuccess', // 执行结果为成功
      b: 'executeToBlocked', // 执行结果为阻塞
      e: 'executeToError', // 执行结果为失败
      p: 'reviewPass', // 评审结果为通过
      r: 'reviewReject', // 评审结果为失败
      m: 'addSiblingModule', // 添加同级模块
      c: 'addSiblingCase', // 添加同级用例
    };
    const shiftCombinationShortcuts: { [key: string]: ShortcutKey } = {
      m: 'addChildModule', // 添加子级模块
      c: 'addChildCase', // 添加子级用例
    };

    let action;

    if (isCtrlOrCmd) {
      // 执行组合键事件
      if (combinationShortcuts[key]) {
        action = combinationShortcuts[key];
      }
    } else if (isShift && shiftCombinationShortcuts[key]) {
      // 执行 shift 组合键事件
      action = shiftCombinationShortcuts[key];
    } else if (singleShortcuts[key]) {
      // 执行单键事件
      action = singleShortcuts[key];
    } else if (businessShortcuts[key]) {
      // 执行业务快捷键事件
      action = businessShortcuts[key];
    }
    if (action && shortcuts[action]) {
      shortcuts[action]!(event);
    }
  };

  const handleShortcutInput = (event: KeyboardEvent) => {
    if ((event.target as HTMLElement)?.tagName?.toLowerCase() === 'input') {
      // 输入框中不响应快捷键
      return;
    }
    const { editor } = window;
    const { fsm } = editor;
    const state = fsm.state();
    switch (state) {
      case 'input': {
        // 输入状态下不响应快捷键
        return;
      }
      default:
    }
    const code = event.code.toLowerCase();
    if (code === 'space') {
      // 执行快捷键编辑事件
      if (shortcuts.input) {
        event.preventDefault();
        shortcuts.input(event);
      }
    }
  };

  const handleShortcutSave = (event: KeyboardEvent) => {
    const { editor } = window;
    const { fsm } = editor;
    const state = fsm.state();
    switch (state) {
      case 'input': {
        // 输入状态下不响应快捷键
        return;
      }
      default:
    }
    const key = event.key.toLowerCase();
    const isCtrlOrCmd = event.ctrlKey || event.metaKey;

    if (isCtrlOrCmd && key === 's') {
      // 执行快捷键保存事件
      if (shortcuts.save) {
        shortcuts.save(event);
      }
    }
  };

  onMounted(() => {
    const minderContainer = document.querySelector('.ms-minder-container');
    if (minderContainer) {
      minderContainer.addEventListener('keydown', (e) => handleKeyDown(e as KeyboardEvent));
    }
    window.addEventListener('keydown', handleShortcutSave);
    window.addEventListener('keydown', handleShortcutInput);
    window.addEventListener('copy', minderCopy);
    window.addEventListener('cut', minderCut);
    window.addEventListener('paste', minderPaste);
  });

  function unbindShortcuts() {
    const minderContainer = document.querySelector('.ms-minder-container');
    if (minderContainer) {
      minderContainer.removeEventListener('keydown', (e) => handleKeyDown(e as KeyboardEvent));
    }
    window.removeEventListener('keydown', handleShortcutSave);
    window.removeEventListener('keydown', handleShortcutInput);
    window.removeEventListener('copy', minderCopy);
    window.removeEventListener('cut', minderCut);
    window.removeEventListener('paste', minderPaste);
  }

  return {
    unbindShortcuts,
  };
}
