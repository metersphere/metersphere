<template>
  <li :class="cls" @click="handleClick">
    <slot>
      <MsIcon type="icon-icon_more_outlined" />
    </slot>
  </li>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';

  import MsIcon from '../ms-icon-font/index.vue';

  import { getLegalPage, getPrefixCls } from './utils';

  defineOptions({
    name: 'PageItemEllipsis',
  });

  export interface PageItemEllipsisProps {
    current: number;
    step: number;
    pages: number;
  }

  const props = withDefaults(defineProps<PageItemEllipsisProps>(), {
    step: 5,
  });
  const emit = defineEmits<{
    (e: 'click', value: number): void;
  }>();

  const prefixCls = getPrefixCls('pagination-item');
  const nextPage = computed(() =>
    getLegalPage(props.current + props.step, {
      min: 1,
      max: props.pages,
    })
  );
  const handleClick = () => {
    emit('click', nextPage.value);
  };
  const cls = computed(() => [prefixCls, `${prefixCls}-ellipsis`]);
</script>
