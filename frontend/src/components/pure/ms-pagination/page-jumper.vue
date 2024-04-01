<template>
  <span :class="cls">
    <span :class="[`${prefixCls}-prepend`, `${prefixCls}-text-goto`]">
      <slot name="jumper-prepend">{{ t('msPagination.goto') }}</slot>
    </span>
    <a-input-number
      v-model="inputValue"
      :class="`${prefixCls}-input`"
      :min="1"
      :max="pages"
      :size="size"
      :disabled="disabled"
      hide-button
      :formatter="handleFormatter"
      @change="handleChange"
      @enter="handleChange"
    />
    <span v-if="$slots['jumper-append']" :class="`${prefixCls}-append`">
      <slot name="jumper-append" />
    </span>
    <span :class="`${prefixCls}-total-page`" :style="{ 'min-width': totalPageWidth }">
      {{ t('msPagination.page', { page: pages }) }}
    </span>
  </span>
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';

  import { useI18n } from '@/hooks/useI18n';
  import useLocale from '@/locale/useLocale';

  import { getPrefixCls } from './utils';

  defineOptions({ name: 'PageJumper' });

  export interface PageJumperProps {
    current: number;
    simple?: boolean;
    disabled: boolean;
    pages: number;
    size?: 'small' | 'mini' | 'medium' | 'large';
    onChange?: (value: number) => void;
  }

  const props = withDefaults(defineProps<PageJumperProps>(), {
    simple: false,
    disabled: false,
  });

  const emit = defineEmits<{
    (e: 'change', value: number): void;
  }>();

  const { currentLocale } = useLocale();

  const prefixCls = getPrefixCls('pagination-jumper');
  const { t } = useI18n();
  const inputValue = ref(props.simple ? props.current : undefined);
  const handleFormatter = (value: number) => {
    const parseIntVal = parseInt(value.toString(), 10);
    return Number.isNaN(parseIntVal) ? undefined : String(parseIntVal);
  };
  const handleChange = () => {
    emit('change', inputValue.value as number);
  };

  watch(
    () => props.current,
    (value) => {
      if (value !== inputValue.value) {
        inputValue.value = value;
      }
    }
  );

  const cls = computed(() => [
    prefixCls,
    {
      [`${prefixCls}-simple`]: props.simple,
    },
  ]);

  const totalPageWidth = computed(() => {
    return currentLocale.value === 'en-US' ? '64px' : '43px';
  });
</script>
