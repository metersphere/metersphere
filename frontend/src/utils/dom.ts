export interface ScrollToViewOptions {
  behavior?: 'auto' | 'smooth';
  block?: 'start' | 'center' | 'end' | 'nearest';
  inline?: 'start' | 'center' | 'end' | 'nearest';
}

export function scrollIntoView(targetRef: HTMLElement | Element | null, options: ScrollToViewOptions = {}) {
  const scrollOptions: ScrollToViewOptions = {
    behavior: options.behavior || 'smooth',
    block: options.block || 'start',
    inline: options.inline || 'nearest',
  };

  targetRef?.scrollIntoView(scrollOptions);
}
