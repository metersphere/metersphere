import { nextTick, reactive, onMounted, onUnmounted } from 'vue';
// 自定义展开行:表格的高度计算和展开折叠图标颜色控制
const useExpandStyle = (wrapperName?: string, className?: string[]) => {
  const cssHeight = reactive({
    height: '460px',
  });
  // 设置折叠展开后图标的颜色
  const expandOrcollapseStyle = () => {
    nextTick(() => {
      const expandBtns = document.querySelectorAll('.expand');
      const collapseBtns = document.querySelectorAll('.collapsebtn');
      expandBtns.forEach((node) => {
        (node.parentNode as HTMLElement).style.borderColor = 'rgb(var(--primary-6))';
      });
      collapseBtns.forEach((node) => {
        (node.parentNode as HTMLElement).style.borderColor = 'var(--color-border-4)';
      });
    });
  };
  // 获取盒子的总高度
  const getPageContentHeight = (boxElement: string) => {
    const pageContent = document.querySelector(boxElement);
    return pageContent ? pageContent.getBoundingClientRect().height : 0;
  };
  // 计算每一个元素的高度
  const getDomHeightWithMargin = (selector: string) => {
    const dom = document.querySelector(selector) as HTMLElement;
    const computedStyle = getComputedStyle(dom);
    const marginTop = parseFloat(computedStyle.marginTop);
    const marginBottom = parseFloat(computedStyle.marginBottom);
    return dom ? dom.getBoundingClientRect().height + marginTop + marginBottom : 460;
  };
  // 计算最后的高度
  const countHeight = () => {
    const contentHeight = getPageContentHeight(wrapperName as string);
    const excludeTotalHeight =
      className?.reduce((prev, item) => {
        return prev + getDomHeightWithMargin(item);
      }, 0) || 0;
    return `${contentHeight - excludeTotalHeight - 70}px`;
  };
  const onResize = () => {
    cssHeight.height = countHeight();
  };
  window.addEventListener('resize', onResize);
  onMounted(() => {
    onResize();
  });
  onUnmounted(() => {
    window.removeEventListener('resize', onResize);
  });

  return {
    expandOrcollapseStyle,
    cssHeight,
  };
};

export default useExpandStyle;
