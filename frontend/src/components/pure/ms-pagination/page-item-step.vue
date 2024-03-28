<template>
  <component :is="simple ? 'span' : 'li'" :class="cls" @click="handleClick">
    <slot :type="isNext ? 'next' : 'previous'">
      <MsIcon
        v-if="isNext"
        size="12px"
        :class="{ 'text-[var(--color-text-4)]': props.current === props.pages }"
        type="icon-icon_right_outlined"
      />
      <MsIcon
        v-else
        size="12px"
        :class="{ 'text-[var(--color-text-4)]': props.current === 1 }"
        type="icon-icon_left_outlined"
      />
    </slot>
  </component>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';

  import MsIcon from '../ms-icon-font/index.vue';

  import { getLegalPage, getPrefixCls } from './utils';

  defineOptions({
    name: 'PageItemStep',
  });

  export interface PageItemStepProps {
    pages: number;
    current: number;
    type: 'next' | 'previous';
    disabled: boolean;
    simple: boolean;
  }

  const props = withDefaults(defineProps<PageItemStepProps>(), {
    disabled: false,
    simple: false,
  });
  const emit = defineEmits<{
    (e: 'click', value: number): void;
  }>();
  const prefixCls = getPrefixCls('pagination-item');
  const isNext = props.type === 'next';
  const mergedDisabled = computed(() => {
    if (props.disabled) {
      return props.disabled;
    }
    if (!props.pages) {
      return true;
    }
    if (isNext && props.current === props.pages) {
      return true;
    }
    return !isNext && props.current <= 1;
  });
  const nextPage = computed(() =>
    getLegalPage(props.current + (isNext ? 1 : -1), {
      min: 1,
      max: props.pages,
    })
  );

  const handleClick = () => {
    if (!mergedDisabled.value) {
      emit('click', nextPage.value);
    }
  };

  const cls = computed(() => [
    prefixCls,
    `${prefixCls}-${props.type}`,
    {
      [`${prefixCls}-disabled`]: mergedDisabled.value,
    },
  ]);
</script>
