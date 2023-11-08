/**
 * 滚动到指定元素
 */
export interface ScrollToViewOptions {
  behavior?: 'auto' | 'smooth';
  block?: 'start' | 'center' | 'end' | 'nearest';
  inline?: 'start' | 'center' | 'end' | 'nearest';
}

/**
 * 将指定元素滚动至视图区域内
 * @param targetRef 目标 ref 或 DOM
 * @param options  滚动配置
 */
export function scrollIntoView(targetRef: HTMLElement | Element | null, options: ScrollToViewOptions = {}) {
  const scrollOptions: ScrollToViewOptions = {
    behavior: options.behavior || 'smooth',
    block: options.block || 'start',
    inline: options.inline || 'nearest',
  };

  targetRef?.scrollIntoView(scrollOptions);
}

/**
 * 无操作函数
 */
export const NOOP = () => {
  return undefined;
};

/**
 * 判断是否为服务端渲染
 */
export const isServerRendering = (() => {
  try {
    return !(typeof window !== 'undefined' && document !== undefined);
  } catch (e) {
    return true;
  }
})();

/**
 * 监听事件
 */
export const on = (() => {
  if (isServerRendering) {
    return NOOP;
  }
  return <K extends keyof HTMLElementEventMap>(
    element: HTMLElement | Window,
    event: K,
    handler: (ev: HTMLElementEventMap[K]) => void,
    options: boolean | AddEventListenerOptions = false
  ) => {
    element.addEventListener(event, handler as EventListenerOrEventListenerObject, options);
  };
})();

/**
 * 移除监听事件
 */
export const off = (() => {
  if (isServerRendering) {
    return NOOP;
  }
  return <K extends keyof HTMLElementEventMap>(
    element: HTMLElement | Window,
    type: K,
    handler: (ev: HTMLElementEventMap[K]) => void,
    options: boolean | EventListenerOptions = false
  ) => {
    element.removeEventListener(type, handler as EventListenerOrEventListenerObject, options);
  };
})();

/**
 * 获取元素宽度
 * @param el 当前元素
 * @returns number
 */
export function getNodeWidth(el: HTMLElement) {
  return el && +el.getBoundingClientRect().width.toFixed(2);
}

/**
 * 获取元素样式
 * @param element 当前元素
 * @param prop 样式属性
 * @returns string
 */
export function getStyle(element: HTMLElement | null, prop: string | null) {
  if (!element || !prop) return null;
  let styleName = prop as keyof CSSStyleDeclaration;
  if (styleName === 'float') {
    styleName = 'cssFloat';
  }
  try {
    if (document.defaultView) {
      const computed = document.defaultView.getComputedStyle(element, '');
      return element.style[styleName] || computed ? computed[styleName] : '';
    }
  } catch (e) {
    return element.style[styleName];
  }
  return null;
}
