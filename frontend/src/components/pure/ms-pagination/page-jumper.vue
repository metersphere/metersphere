<template>
  <span :class="cls">
    <span v-if="!simple" :class="[`${prefixCls}-prepend`, `${prefixCls}-text-goto`]">
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
    />
    <span v-if="$slots['jumper-append']" :class="`${prefixCls}-append`"><slot name="jumper-append" /></span>
    <template v-if="simple">
      <span :class="`${prefixCls}-separator`">/</span>
      <span :class="`${prefixCls}-total-page`">{{ pages }}</span>
    </template>
  </span>
</template>

<script lang="ts">
  import { computed, defineComponent, nextTick, PropType, ref, watch } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import { getPrefixCls } from './utils';

  export default defineComponent({
    name: 'PageJumper',
    props: {
      current: {
        type: Number,
        required: true,
      },
      simple: {
        type: Boolean,
        default: false,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      pages: {
        type: Number,
        required: true,
      },
      size: {
        type: String as PropType<'small' | 'mini' | 'medium' | 'large' | undefined>,
      },
      onChange: {
        type: Function as PropType<(value: number) => void>,
      },
    },
    emits: ['change'],
    setup(props, { emit }) {
      const prefixCls = getPrefixCls('pagination-jumper');
      const { t } = useI18n();
      const inputValue = ref(props.simple ? props.current : undefined);

      const handleFormatter = (value: number) => {
        const parseIntVal = parseInt(value.toString(), 10);
        return Number.isNaN(parseIntVal) ? undefined : String(parseIntVal);
      };

      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const handleChange = (value: number | undefined) => {
        emit('change', inputValue.value);
        nextTick(() => {
          if (!props.simple) {
            inputValue.value = undefined;
          }
        });
      };

      watch(
        () => props.current,
        (value) => {
          if (props.simple && value !== inputValue.value) {
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

      return {
        prefixCls,
        cls,
        t,
        inputValue,
        handleChange,
        handleFormatter,
      };
    },
  });
</script>
