<template>
  <span :class="prefixCls">
    <a-select
      :model-value="pageSize"
      :options="options"
      :size="size"
      :disabled="disabled"
      v-bind="selectProps"
      @change="handleChange"
    />
  </span>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  import { Size } from './types';
  import { getPrefixCls } from './utils';
  import { SelectProps } from '@arco-design/web-vue/es/select/interface';

  defineOptions({ name: 'PageOptions' });

  interface PageOptionsProps {
    sizeOptions: number[];
    pageSize: number;
    disabled: boolean;
    size: Size;
    onChange: (value: number) => void;
    selectProps?: SelectProps;
  }

  const prefixCls = getPrefixCls('pagination-options');
  const { t } = useI18n();

  const emit = defineEmits<{
    (e: 'change', value: number): void;
  }>();

  const props = defineProps<PageOptionsProps>();

  const handleChange = (
    value: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
  ) => {
    emit('change', value as number);
  };

  const options = computed(() =>
    props.sizeOptions.map((value) => ({
      value,
      label: `${value} ${t('msPagination.countPerPage')}`,
    }))
  );
</script>
