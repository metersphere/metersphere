import useMinderOperation, { type MinderOperationProps } from './useMinderOperation';

type ShortcutKey = 'expand' | 'enter' | 'appendSiblingNode' | 'appendChildNode' | 'undo' | 'redo' | 'delete';
// 快捷键事件映射，combinationShortcuts中定义了组合键事件，key为组合键，value为事件名称；
type Shortcuts = {
  [key in ShortcutKey]?: () => void;
};

export default function useShortCut(shortcuts: Shortcuts, options: MinderOperationProps) {
  const { minderCopy, minderCut, minderPaste } = useMinderOperation(options);

  const handleKeyDown = (event: KeyboardEvent) => {
    const key = event.key.toLowerCase();
    const isCtrlOrCmd = event.ctrlKey || event.metaKey;

    // 定义组合键事件
    const combinationShortcuts: { [key: string]: ShortcutKey } = {
      z: 'undo', // 撤销
      y: 'redo', // 重做
      enter: 'enter', // 进入节点
    };
    // 定义单键事件
    const singleShortcuts: { [key: string]: ShortcutKey } = {
      '/': 'expand', // 展开/折叠
      'enter': 'appendSiblingNode',
      'tab': 'appendChildNode',
      'backspace': 'delete',
    };

    if (isCtrlOrCmd && combinationShortcuts[key]) {
      // 执行组合键事件
      event.preventDefault();
      const action = combinationShortcuts[key];
      if (shortcuts[action]) {
        shortcuts[action]!();
      }
    } else if (singleShortcuts[key]) {
      // 执行单键事件
      event.preventDefault();
      const action = singleShortcuts[key];
      if (shortcuts[action]) {
        shortcuts[action]!();
      }
    }
  };

  onMounted(() => {
    const minderContainer = document.querySelector('.ms-minder-container');
    if (minderContainer) {
      minderContainer.addEventListener('keydown', (e) => handleKeyDown(e as KeyboardEvent));
      minderContainer.addEventListener('copy', (e) => minderCopy(e as ClipboardEvent));
      minderContainer.addEventListener('cut', (e) => minderCut(e as ClipboardEvent));
      minderContainer.addEventListener('paste', (e) => minderPaste(e as ClipboardEvent));
    }
  });

  function unbindShortcuts() {
    const minderContainer = document.querySelector('.ms-minder-container');
    if (minderContainer) {
      minderContainer.removeEventListener('keydown', (e) => handleKeyDown(e as KeyboardEvent));
      minderContainer.removeEventListener('copy', (e) => minderCopy(e as ClipboardEvent));
      minderContainer.removeEventListener('cut', (e) => minderCut(e as ClipboardEvent));
      minderContainer.removeEventListener('paste', (e) => minderPaste(e as ClipboardEvent));
    }
  }

  return {
    unbindShortcuts,
  };
}
