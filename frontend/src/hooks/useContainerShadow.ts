import { computed, onBeforeUnmount, Ref, ref } from 'vue';

export interface ContainerShadowOptions {
  /**
   * 内容区域
   */
  content?: Ref<HTMLElement>;
  /**
   * 判断显示阴影的高度
   */
  overHeight: number;
  /**
   * 内容区域的类名
   */
  containerClassName: string;
}

/**
 * 监听指定容器的滚动事件，以给容器添加顶部和底部的阴影，需要给指定的容器添加 .ms-container--shadow-y() 样式类使用阴影
 * @param options ContainerShadowOptions
 */
export default function useContainerShadow(options: ContainerShadowOptions) {
  const isArrivedTop = ref(true);
  const isArrivedBottom = ref(true);
  const isInitListener = ref(false);
  const containerRef = ref<Ref<HTMLElement> | undefined>(options.content);

  function setContainer(dom: HTMLElement) {
    if (dom) {
      containerRef.value = dom;
    }
  }

  function calculateArrivedPosition(listContent: HTMLElement) {
    const { scrollTop, scrollHeight, clientHeight } = listContent;
    const scrollBottom = scrollHeight - clientHeight - scrollTop;
    isArrivedTop.value = scrollTop < options.overHeight;
    isArrivedBottom.value = scrollBottom < options.overHeight;
  }

  /**
   * 监听列表内容区域滚动，以切换顶部底部阴影
   * @param event 滚动事件
   */
  function listenScroll(event: Event) {
    if (event.target) {
      const listContent = event.target as HTMLElement;
      calculateArrivedPosition(listContent);
    }
  }

  function initScrollListener() {
    if (!isInitListener.value && containerRef.value) {
      containerRef.value.addEventListener('scroll', listenScroll);
      calculateArrivedPosition(containerRef.value); // 初始化计算一次，因为初始化的时候内容可能超出可视区域了
      isInitListener.value = true;
    }
  }

  const containerStatusClass = computed(() => {
    if (isArrivedTop.value && isArrivedBottom.value) {
      // 内容不足一屏，不展示阴影
      return '';
    }
    if (isArrivedTop.value) {
      // 滚动到顶部，隐藏顶部阴影
      return `${options.containerClassName}-shadow-bottom`;
    }
    if (isArrivedBottom.value) {
      // 滚动到底部，隐藏底部阴影
      return `${options.containerClassName}-shadow-top`;
    }
    // 滚动到中间，展示两侧阴影
    return `${options.containerClassName}-shadow-top ${options.containerClassName}-shadow-bottom`;
  });

  onBeforeUnmount(() => {
    if (containerRef.value) {
      containerRef.value.removeEventListener('scroll', listenScroll);
    }
  });

  return {
    isArrivedTop,
    isArrivedBottom,
    isInitListener,
    containerStatusClass,
    setContainer,
    initScrollListener,
  };
}
