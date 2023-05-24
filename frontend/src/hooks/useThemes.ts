import { computed } from 'vue';
import { useAppStore } from '@/store';

export default function useThemes() {
  const appStore = useAppStore();
  const isDark = computed(() => {
    return appStore.theme === 'dark';
  });
  return {
    isDark,
  };
}
