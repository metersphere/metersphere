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
