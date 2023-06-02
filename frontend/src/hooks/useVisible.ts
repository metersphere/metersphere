import { ref } from 'vue';

/**
 * 切换显示隐藏
 * @param initValue 初始化值
 * @returns 调用方法
 */
export default function useVisible(initValue = false) {
  const visible = ref(initValue);
  const setVisible = (value: boolean) => {
    visible.value = value;
  };
  const toggle = () => {
    visible.value = !visible.value;
  };
  return {
    visible,
    setVisible,
    toggle,
  };
}
