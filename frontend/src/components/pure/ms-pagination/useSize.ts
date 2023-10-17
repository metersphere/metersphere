import { computed, inject, Ref } from 'vue';

import { Size } from './types';
import { configProviderInjectionKey } from './utils';

const useSize = (size?: Ref<Size | undefined>, { defaultValue = 'medium' }: { defaultValue?: Size } = {}) => {
  const configProviderCtx = inject(configProviderInjectionKey, undefined);

  const mergedSize = computed(() => size?.value ?? configProviderCtx?.size ?? defaultValue);

  return {
    mergedSize,
  };
};

export default useSize;
