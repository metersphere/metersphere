import { ref } from 'vue';

/**
 * @description 用于自定义双击事件
 */
export default function useDoubleClick(callback: () => void) {
  const count = ref(0);
  const lastClickTime = ref(0);
  const DOUBLE_CLICK_THRESHOLD = 300; // 300毫秒

  function handleClick() {
    const currentTime = new Date().getTime();
    const timeDiff = currentTime - lastClickTime.value;

    if (timeDiff < DOUBLE_CLICK_THRESHOLD) {
      count.value++;
    } else {
      count.value = 1;
    }
    lastClickTime.value = currentTime;

    if (count.value >= 2) {
      callback();
      count.value = 0;
    }
  }

  return {
    handleClick,
  };
}
