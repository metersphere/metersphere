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
/**
 * 获取当前展示的最上层的浮层（弹窗、抽屉等）
 * @param selector 浮层选择器
 */
export function getMaxZIndexLayer(selector: string): HTMLElement | null {
  const layers = document.querySelectorAll<HTMLElement>(selector);

  let maxZIndex = 0;
  let maxZIndexDrawer: HTMLElement | null = null;

  layers.forEach((layer) => {
    const zIndex = parseInt(window.getComputedStyle(layer).zIndex, 10);
    if (!Number.isNaN(zIndex) && zIndex > maxZIndex) {
      maxZIndex = zIndex;
      maxZIndexDrawer = layer;
    }
  });

  return maxZIndexDrawer;
}

/**
 * 合并样式
 * @param element 当前元素
 * @param stylesToAdd 要添加的样式
 */
export function mergeStyles(element: HTMLElement | Element | null, stylesToAdd: string): void {
  if (element) {
    const originalStyles = element.getAttribute('style') || '';
    const mergedStyles: Record<string, string> = {};
    const originalStylePairs = originalStyles.split(';').filter((style) => style.trim() !== '');

    // 解析原有的 style 属性
    originalStylePairs.forEach((pair) => {
      const [key, value] = pair.split(':').map((item) => item.trim());
      mergedStyles[key] = value;
    });

    // 解析要添加的样式属性
    const stylesToAddPairs = stylesToAdd.split(';').filter((style) => style.trim() !== '');
    stylesToAddPairs.forEach((pair) => {
      const [key, value] = pair.split(':').map((item) => item.trim());
      mergedStyles[key] = value;
    });

    // 构造新的 style 属性字符串
    const mergedStyleString = Object.entries(mergedStyles)
      .map(([key, value]) => `${key}: ${value}`)
      .join(';');

    // 设置新的 style 属性值
    element.setAttribute('style', mergedStyleString);
  }
}

/**
 * 移除样式
 * @param element 当前元素
 * @param stylesToRemove 要移除的样式
 */
export function removeStyles(element: HTMLElement | Element | null, stylesToRemove: string): void {
  if (element) {
    const originalStyles = element.getAttribute('style') || '';
    const updatedStyles: Record<string, string> = {};
    const originalStylePairs = originalStyles.split(';').filter((style) => style.trim() !== '');

    // 解析原有的 style 属性
    originalStylePairs.forEach((pair) => {
      const [key, value] = pair.split(':').map((item) => item.trim());
      updatedStyles[key] = value;
    });

    // 移除指定的样式属性
    const stylesToRemovePairs = stylesToRemove.split(';').filter((style) => style.trim() !== '');
    stylesToRemovePairs.forEach((pair) => {
      const [key] = pair.split(':').map((item) => item.trim());
      delete updatedStyles[key];
    });

    // 构造新的 style 属性字符串
    const updatedStyleString = Object.entries(updatedStyles)
      .map(([key, value]) => `${key}: ${value}`)
      .join(';');

    // 设置新的 style 属性值
    element.setAttribute('style', updatedStyleString);
  }
}
