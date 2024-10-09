export default function useShortcutSave(onSave: (event: KeyboardEvent) => void) {
  const saveShortcut = (event: KeyboardEvent) => {
    // 检查是否按下了 Ctrl 键（Windows/Linux）或 Command 键（Mac）
    const isCtrlPressed = event.ctrlKey || event.metaKey;
    // 检查是否按下了 'S' 键
    const isSPressed = event.key === 's';
    // 如果同时按下了 Ctrl 键和 'S' 键
    if (isCtrlPressed && isSPressed && onSave) {
      // 阻止默认行为，防止浏览器保存页面
      event.preventDefault();
      // 在这里添加你的保存逻辑
      onSave(event);
    }
  };

  /**
   * 注册 Ctrl + S 快捷键保存事件拦截
   */
  function registerCatchSaveShortcut() {
    document.addEventListener('keydown', saveShortcut);
  }

  /**
   * 移除 Ctrl + S 快捷键保存事件拦截
   */
  function removeCatchSaveShortcut() {
    document.removeEventListener('keydown', saveShortcut);
  }
  return {
    registerCatchSaveShortcut,
    removeCatchSaveShortcut,
  };
}
