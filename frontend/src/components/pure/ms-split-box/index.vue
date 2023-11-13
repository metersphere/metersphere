<template>
  <a-split
    v-model:size="innerWidth"
    :min="props.min"
    :max="props.max"
    :class="['h-full', isExpandedLeft ? '' : 'expanded-panel', isExpandAnimating ? 'animating' : '']"
  >
    <template #first>
      <div class="ms-split-box ms-split-box--left">
        <slot name="left"></slot>
      </div>
    </template>
    <template #second>
      <div class="absolute flex h-full w-[16px] items-center">
        <div class="expand-icon" @click="changeLeftExpand">
          <MsIcon
            :type="isExpandedLeft ? 'icon-icon_up-left_outlined' : 'icon-icon_down-right_outlined'"
            class="text-[var(--color-text-brand)]"
            size="12"
          />
        </div>
      </div>
      <div class="ms-split-box ms-split-box--right">
        <slot name="right"></slot>
      </div>
    </template>
  </a-split>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  const props = withDefaults(
    defineProps<{
      width?: number | string;
      min?: number | string;
      max?: number | string;
    }>(),
    {
      width: '300px',
      min: '250px',
      max: 0.5,
    }
  );

  const emit = defineEmits(['update:width', 'expandChange']);

  const innerWidth = ref(props.width || '300px');

  watch(
    () => props.width,
    (val) => {
      if (val !== undefined) {
        innerWidth.value = val;
      }
    }
  );

  watch(
    () => innerWidth.value,
    (val) => {
      emit('update:width', val);
    }
  );

  const isExpandedLeft = ref(true);
  const isExpandAnimating = ref(false); // 控制动画类

  function changeLeftExpand() {
    isExpandAnimating.value = true;
    isExpandedLeft.value = !isExpandedLeft.value;
    if (isExpandedLeft.value) {
      innerWidth.value = props.width || '300px';
      emit('expandChange', true);
    } else {
      innerWidth.value = '0px';
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
    width: calc(v-bind(innerWidth) - 4px);
  }
  .expand-icon {
    @apply z-10 cursor-pointer;

    padding: 8px 2px;
    border-radius: 0 var(--border-radius-small) var(--border-radius-small) 0;
    background-color: var(--color-text-n8);
  }
  .ms-split-box--right {
    @apply w-full;
  }
  :deep(.arco-split-trigger-icon) {
    font-size: 14px;
  }
</style>
