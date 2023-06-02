import { computed } from 'vue';
import { useAppStore } from '@/store';

/**
 * 暗黑模式相关
 * @returns 调用方法
 */
export default function useThemes() {
  const appStore = useAppStore();
  const isDark = computed(() => {
    return appStore.theme === 'dark';
  });
  return {
    isDark,
  };
}
