import { computed } from 'vue';

export function useDialog(props: any, emits: any) {
  const dialogVisible = computed<boolean>({
    get() {
      return props.visible;
    },
    set(visible) {
      emits('update:visible', visible);
      if (!visible) {
        emits('close');
      }
    },
  });
  return { dialogVisible };
}

export default useDialog;
