<template>
  <div ref="cardWrapperRef">
    <a-tabs v-if="props.contentTabList.length" default-active-key="1" class="ms-tab-card">
      <a-tab-pane v-for="item of props.contentTabList" :key="item.value" :title="`${item.label}`">
        <template #title>
          <slot name="item" :item="item">
            <div class="wrapper-card w-full">
              <div class="card-title flex items-center gap-[8px]">
                <div :class="`card-title-icon bg-[${item?.color}]`">
                  <MsIcon :type="item.icon" class="text-white" size="12" />
                </div>
                <div class="text-[var(--color-text-1)]"> {{ item.label }}</div>
              </div>
              <div class="card-number !text-[20px] !font-medium"> {{ addCommasToNumber(item.count || 0) }} </div>
            </div>
          </slot>
        </template>
      </a-tab-pane>
    </a-tabs>
    <NoData v-else :no-permission-text="props.noPermissionText" />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { debounce } from 'lodash-es';

  import NoData from '../../components/notData.vue';

  import { addCommasToNumber } from '@/utils';

  const props = defineProps<{
    contentTabList: {
      label: string;
      value: string | number;
      count?: number;
      icon?: string;
      color?: string;
      [key: string]: any;
    }[];
    minWidth?: string;
    notHasPadding?: boolean;
    hiddenBorder?: boolean;
    noPermissionText?: string;
  }>();

  const width = ref<string | number>();

  const cardWrapperRef = ref<HTMLElement | null>(null);
  const calculateWidth = debounce(() => {
    const wrapperContent = cardWrapperRef.value as HTMLElement;
    if (wrapperContent) {
      const wrapperTotalWidth = wrapperContent.offsetWidth;
      const gap = 16;
      const gapWidth = (props.contentTabList.length - 1) * gap; // 总间隙宽度
      const itemWidth = Math.floor((wrapperTotalWidth - gapWidth) / props.contentTabList.length);
      width.value = `${itemWidth}px`;
    }
  }, 50);

  let resizeObserver: ResizeObserver;

  onMounted(() => {
    const wrapperContent = cardWrapperRef.value;
    if (wrapperContent) {
      resizeObserver = new ResizeObserver(() => {
        calculateWidth();
      });
      resizeObserver.observe(wrapperContent);
    }
  });

  const minwidth = ref();
  const padding = ref();
  const color = ref();
  watch(
    [() => props.minWidth, () => props.notHasPadding, () => props.hiddenBorder],
    (val) => {
      const [newMinWidth, noPadding, isHiddenBorder] = val;
      minwidth.value = `${newMinWidth || '136px'}`;
      padding.value = `${noPadding ? '0px' : '16px'}`;
      color.value = `${isHiddenBorder ? 'transparent' : 'var(--color-text-n8)'}`;
      calculateWidth();
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props.contentTabList,
    (val) => {
      if (val.length) {
        calculateWidth();
      }
    }
  );

  onBeforeUnmount(() => {
    window.removeEventListener('resize', calculateWidth);
  });
</script>

<style scoped lang="less">
  :deep(.ms-tab-card) {
    .arco-tabs-nav-tab {
      .arco-tabs-nav-ink {
        display: none;
      }
      .arco-tabs-nav-tab-list {
        gap: 16px;
        @apply flex;
      }
      .arco-tabs-tab {
        margin: 0;
        box-sizing: border-box;
        padding: v-bind(padding) !important;
        width: v-bind(width);
        min-width: v-bind(minwidth) !important;
        border: 1px solid v-bind(color);
        border-radius: 4px;
        &.arco-tabs-tab-active {
          color: var(--color-text-1);
        }
        &:hover {
          color: var(--color-text-1);
        }
      }
      .arco-tabs-tab-title {
        @apply w-full;
        .card-number {
          margin-left: 28px;
          font-size: 20px !important;
        }
      }
    }
    .card-title-icon {
      width: 20px;
      height: 20px;
      border-radius: 50%;
      @apply flex items-center justify-center;
    }
  }
  :deep(.arco-tabs-nav-button) {
    width: 36px;
    height: 36px;
    border: 1px solid var(--color-text-n8);
    border-radius: 2px;
  }
</style>
