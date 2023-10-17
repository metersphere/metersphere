import { onBeforeMount, onBeforeUnmount, onMounted } from 'vue';
import { useDebounceFn } from '@vueuse/core';

import { useAppStore } from '@/store';
import { addEventListen, removeEventListen } from '@/utils/event';

const WIDTH = 992; // https://arco.design/vue/component/grid#responsivevalue

/**
 * 判断设备是否为移动端
 * @returns 是否为移动端
 */
function queryDevice() {
  const rect = document.body.getBoundingClientRect();
  return rect.width - 1 < WIDTH;
}

/**
 * 响应布局变化
 * @param immediate 是否立即执行
 */
export default function useResponsive(immediate?: boolean) {
  const appStore = useAppStore();
  /**
   * 切换布局
   */
  function resizeHandler() {
    if (!document.hidden) {
      const isMobile = queryDevice();
      appStore.toggleDevice(isMobile ? 'mobile' : 'desktop');
      appStore.toggleMenu(isMobile);
    }
  }
  const debounceFn = useDebounceFn(resizeHandler, 100);
  onMounted(() => {
    if (immediate) debounceFn();
  });
  onBeforeMount(() => {
    addEventListen(window, 'resize', debounceFn);
  });
  onBeforeUnmount(() => {
    removeEventListen(window, 'resize', debounceFn);
  });
}
