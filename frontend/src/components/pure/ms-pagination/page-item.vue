<template>
  <li :class="cls" :style="mergedStyle" @click="handleClick">
    <slot :page="pageNumber">
      {{ pageNumber }}
    </slot>
  </li>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';

  import { getPrefixCls } from './utils';
  import type { CSSProperties } from 'vue';

  defineOptions({
    name: 'Pager',
  });

  export interface PageItemProps {
    pageNumber?: number;
    current?: number;
    disabled: boolean;
    style?: CSSProperties;
    activeStyle?: CSSProperties;
  }

  const props = withDefaults(defineProps<PageItemProps>(), {
    disabled: false,
  });

  const emit = defineEmits<{
    (e: 'click', value: number, event: MouseEvent): void;
  }>();

  const handleClick = (e: MouseEvent) => {
    if (!props.disabled) {
      emit('click', props.pageNumber as number, e);
    }
  };

  const prefixCls = getPrefixCls('pagination-item');
  const isActive = computed(() => props.current === props.pageNumber);
  const cls = computed(() => [
    prefixCls,
    {
      [`${prefixCls}-active`]: props.current === props.pageNumber,
    },
  ]);

  const mergedStyle = computed(() => {
    return isActive.value ? props.activeStyle : props.style;
  });
</script>
