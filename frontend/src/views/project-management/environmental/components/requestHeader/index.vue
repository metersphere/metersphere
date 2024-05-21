<template>
  <httpHeader
    v-model:params="innerParams"
    :layout="activeLayout"
    :disabled-param-value="props.disabledParamValue"
    :disabled-except-param="props.disabledExceptParam"
    @change="emit('change')"
  />
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import { useI18n } from '@/hooks/useI18n';

  const httpHeader = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/header.vue'));

  defineOptions({
    name: 'EnvManageGlobalRequestHeader',
  });

  const props = defineProps<{
    params: any[];
    noParamType?: boolean;
    disabledParamValue?: boolean;
    disabledExceptParam?: boolean; // 除了可以修改参数值其他都禁用
  }>();
  const emit = defineEmits<{
    (e: 'update:params', value: any[]): void;
    (e: 'change'): void; //  数据发生变化
  }>();

  const innerParams = useVModel(props, 'params', emit);
  const activeLayout = ref<'horizontal' | 'vertical'>('vertical');
</script>

<style lang="less" scoped></style>
