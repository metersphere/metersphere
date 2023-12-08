<template>
  <a-split
    v-model:size="innerSize"
    :min="props.min"
    :max="props.max"
    :class="['h-full', isExpanded ? '' : 'expanded-panel', isExpandAnimating ? 'animating' : '']"
    :direction="props.direction"
  >
    <template #first>
      <div v-if="props.direction === 'horizontal'" class="ms-split-box ms-split-box--left">
        <div v-if="props.expandDirection === 'right'" class="absolute right-0 flex h-full w-[16px] items-center">
          <div class="expand-icon expand-icon--left" @click="changeLeftExpand">
            <MsIcon
              :type="isExpanded ? 'icon-icon_up-left_outlined' : 'icon-icon_down-right_outlined'"
              class="text-[var(--color-text-brand)]"
              size="12"
            />
          </div>
        </div>
        <slot name="left"></slot>
      </div>
      <div v-else class="ms-split-box ms-split-box--top">
        <slot name="top"></slot>
      </div>
    </template>
    <template #second>
      <template v-if="props.direction === 'horizontal'">
        <div v-if="props.expandDirection === 'left'" class="absolute flex h-full w-[16px] items-center">
          <div class="expand-icon" @click="changeLeftExpand">
            <MsIcon
              :type="isExpanded ? 'icon-icon_up-left_outlined' : 'icon-icon_down-right_outlined'"
              class="text-[var(--color-text-brand)]"
              size="12"
            />
          </div>
        </div>
        <div class="ms-split-box ms-split-box--right">
          <slot name="right"></slot>
        </div>
      </template>
      <template v-else>
        <div class="absolute top-0 flex h-[16px] w-full items-center justify-center">
          <div class="expand-icon expand-icon--vertical" @click="changeLeftExpand">
            <MsIcon
              :type="isExpanded ? 'icon-icon_up-left_outlined' : 'icon-icon_down-right_outlined'"
              class="text-[var(--color-text-brand)]"
              size="12"
            />
          </div>
        </div>
        <div class="ms-split-box ms-split-box--bottom">
          <slot name="bottom"></slot>
        </div>
      </template>
    </template>
  </a-split>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  const props = withDefaults(
    defineProps<{
      size?: number | string; // 左侧宽度/顶部容器高度。expandDirection为 right 时，size 也是左侧容器宽度，所以想要缩小右侧容器宽度只需要将 size 调大即可
      min?: number | string;
      max?: number | string;
      direction?: 'horizontal' | 'vertical';
      expandDirection?: 'left' | 'right' | 'top'; // TODO: 未实现 bottom，有场景再补充。目前默认水平是 left，垂直是 top
    }>(),
    {
      size: '300px',
      min: '250px',
      max: 0.5,
      direction: 'horizontal',
      expandDirection: 'left',
    }
  );

  const emit = defineEmits(['update:size', 'expandChange']);

  const innerSize = ref(props.size || '300px');

  watch(
    () => props.size,
    (val) => {
      if (val !== undefined) {
        innerSize.value = val;
      }
    }
  );

  watch(
    () => innerSize.value,
    (val) => {
      emit('update:size', val);
    }
  );

  const isExpanded = ref(true);
  const isExpandAnimating = ref(false); // 控制动画类

  function changeLeftExpand() {
    isExpandAnimating.value = true;
    isExpanded.value = !isExpanded.value;
    if (isExpanded.value) {
      innerSize.value = props.size || '300px'; // 按初始化的 size 展开，无论是水平还是垂直，都是宽度/高度
      emit('expandChange', true);
    } else {
      innerSize.value = props.expandDirection === 'right' ? 1 : '0px'; // expandDirection为 right 时，收起即为把左侧容器宽度提到 100%
      emit('expandChange', false);
    }
    // 动画结束，去掉动画类
    setTimeout(() => {
      isExpandAnimating.value = false;
    }, 300);
  }
</script>

<style lang="less" scoped>
  /* stylelint-disable value-keyword-case */
  .expanded-panel {
    :deep(.arco-split-trigger) {
      @apply hidden;
    }
    :deep(.arco-split-pane) {
      @apply relative overflow-hidden;
    }
  }
  :deep(.arco-split-pane) {
    @apply relative overflow-hidden;
  }
  .animating {
    :deep(.arco-split-pane) {
      @apply relative overflow-hidden;

      transition: flex 0.3s ease;
    }
  }
  .ms-split-box {
    @apply relative h-full overflow-auto;
    .ms-scroll-bar();
  }
  .ms-split-box--left {
    width: calc(v-bind(innerSize) - 4px);
  }
  .expand-icon {
    @apply z-10 flex cursor-pointer justify-center;

    padding: 12px 2px;
    border-radius: 0 var(--border-radius-small) var(--border-radius-small) 0;
    background-color: var(--color-text-n8);
  }
  .ms-split-box--right {
    @apply w-full;
  }
  :deep(.arco-split-trigger-icon) {
    font-size: 14px;
  }
  .ms-split-box--bottom {
    @apply h-full;
  }
  .expand-icon--vertical {
    @apply rotate-90;
  }
  .expand-icon--left {
    @apply rotate-180;

    border-radius: 0 var(--border-radius-small) var(--border-radius-small) 0;
  }
</style>
