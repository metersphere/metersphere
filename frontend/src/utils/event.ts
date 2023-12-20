/**
 * 给目标元素添加监听事件
 * @param target 目标元素 DOM
 * @param event 事件名称
 * @param handler 处理函数
 * @param capture 是否在捕获阶段触发
 */
export function addEventListen(
  target: Window | HTMLElement,
  event: string,
  handler: EventListenerOrEventListenerObject,
  capture = false
) {
  if (target.addEventListener && typeof target.addEventListener === 'function') {
    target.addEventListener(event, handler, capture);
  }
}

/**
 * 移除事件监听
 * @param target 目标元素 DOM
 * @param event 事件名称
 * @param handler 处理函数
 * @param capture 是否在捕获阶段触发
 */
export function removeEventListen(
  target: Window | HTMLElement,
  event: string,
  handler: EventListenerOrEventListenerObject,
  capture = false
) {
  if (target.removeEventListener && typeof target.removeEventListener === 'function') {
    target.removeEventListener(event, handler, capture);
  }
}

/**
 * 注册 Ctrl + S 快捷键保存事件拦截
 * @param callback 保存回调
 */
export function registerCatchSaveShortcut(callback: () => void) {
  document.addEventListener('keydown', (event) => {
    // 检查是否按下了 Ctrl 键（Windows/Linux）或 Command 键（Mac）
    const isCtrlPressed = event.ctrlKey || event.metaKey;
    // 检查是否按下了 'S' 键
    const isSPressed = event.key === 's';
    // 如果同时按下了 Ctrl 键和 'S' 键
    if (isCtrlPressed && isSPressed) {
      // 阻止默认行为，防止浏览器保存页面
      event.preventDefault();
      // 在这里添加你的保存逻辑
      callback();
    }
  });
}

/**
 * 移除 Ctrl + S 快捷键保存事件拦截
 * @param callback 保存回调
 */
export function removeCatchSaveShortcut(callback: () => void) {
  document.removeEventListener('keydown', callback);
}
