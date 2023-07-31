<template>
  <li :class="cls" @click="handleClick">
    <slot>
      <MsIcon type="icon-icon_more_outlined" />
    </slot>
  </li>
</template>

<script lang="ts">
  import { computed, defineComponent } from 'vue';
  import { getPrefixCls, getLegalPage } from './utils';
  import MsIcon from '../ms-icon-font/index.vue';

  export default defineComponent({
    name: 'EllipsisPager',
    components: {
      MsIcon,
    },
    props: {
      current: {
        type: Number,
        required: true,
      },
      step: {
        type: Number,
        default: 5,
      },
      pages: {
        type: Number,
        required: true,
      },
    },
    emits: ['click'],
    setup(props, { emit }) {
      const prefixCls = getPrefixCls('pagination-item');

      const nextPage = computed(() =>
        getLegalPage(props.current + props.step, {
          min: 1,
          max: props.pages,
        })
      );

      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const handleClick = (e: MouseEvent) => {
        emit('click', nextPage.value);
      };

      const cls = computed(() => [prefixCls, `${prefixCls}-ellipsis`]);

      return {
        prefixCls,
        cls,
        handleClick,
      };
    },
  });
</script>
