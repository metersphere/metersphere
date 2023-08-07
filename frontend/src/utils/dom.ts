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
