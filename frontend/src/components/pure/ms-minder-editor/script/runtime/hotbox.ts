import useMinderStore from '@/store/modules/components/minder-editor';

interface Position {
  x: number;
  y: number;
}

function HotboxRuntime(this: any) {
  const { fsm } = this;
  const { minder } = this;
  const { receiver } = this;
  const { container } = this;
  const { HotBox } = window;
  const hotbox = new HotBox(container);
  const minderStore = useMinderStore();

  hotbox.setParentFSM(fsm);

  function handleHotBoxShow() {
    const node = minder.getSelectedNode();
    let position: Position | undefined;
    if (node) {
      const box = node.getRenderBox();
      position = {
        x: box.cx,
        y: box.cy,
      };
      minderStore.dispatchEvent('hotbox', position, node.rc.node);
    }
  }

  fsm.when('normal -> hotbox', () => {
    handleHotBoxShow();
  });

  fsm.when('hotbox -> hotbox', () => {
    handleHotBoxShow();
  });

  function handleShortcut(e: any) {
    // 检查是否按下Ctrl键（Windows和Linux）或Command键（Mac）
    const isCtrlKey = e.ctrlKey || e.metaKey;
    // 检查是否按下Enter键
    const isEnterKey = e.key === 'Enter';
    // 检查是否同时按下Ctrl（或Command）和Enter键
    if (isCtrlKey && isEnterKey) {
      // 处理Ctrl+Enter组合键事件
      e.preventDefault(); // 阻止默认行为
      // 执行进入模块方法
      const node = minder.getSelectedNode();
      let position: Position | undefined;
      if (node) {
        const box = node.getRenderBox();
        position = {
          x: box.cx,
          y: box.cy,
        };
        minderStore.dispatchEvent('enterNode', position, node.rc.node, node.data);
        return;
      }
    }
    minder.dispatchKeyEvent(e);
  }

  fsm.when('normal -> normal', (exit: any, enter: any, reason: any, e: any) => {
    if (reason === 'shortcut-handle') {
      handleShortcut(e);
    }
  });

  fsm.when('modal -> normal', (exit: any, enter: any, reason: any) => {
    if (reason === 'import-text-finish') {
      receiver.element.focus();
    }
  });

  this.hotbox = hotbox;
  minder.hotbox = hotbox;
}

export default HotboxRuntime;
