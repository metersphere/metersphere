<template>
  <a-split
    v-model:size="innerSize"
    :min="props.min"
    :max="props.max"
    :class="[
      'h-full',
      'ms-split-box-second',
      isExpanded ? '' : 'expanded-panel',
      isExpandAnimating ? 'animating' : '',
      props.direction === 'vertical' ? 'ms-split-box--vertical' : '',
    ]"
    :direction="props.direction"
    :disabled="props.disabled || !isExpanded"
  >
    <template #first>
      <div
        :class="`ms-split-box ${props.direction === 'horizontal' ? 'ms-split-box--left' : 'ms-split-box--top'} ${
          props.disabled && props.direction === 'horizontal' ? 'border-r border-[var(--color-text-n8)]' : ''
        } ${props.firstContainerClass || ''}`"
      >
        <div
          v-if="props.direction === 'horizontal' && props.expandDirection === 'right' && !props.disabled"
          class="absolute right-0 z-40 h-full w-[12px]"
        >
          <div class="expand-icon expand-icon--left" @click="() => changeExpand()">
            <MsIcon
              :type="isExpanded ? 'icon-icon_up-left_outlined' : 'icon-icon_down-right_outlined'"
              class="!w-auto text-[var(--color-text-brand)]"
              size="9"
            />
          </div>
        </div>
        <slot name="first"></slot>
      </div>
    </template>
    <template #resize-trigger>
      <div v-if="props.notShowFirst"></div>
      <div v-else :class="props.direction === 'horizontal' ? 'horizontal-expand-line' : 'vertical-expand-line'">
        <div v-if="isExpanded" class="expand-color-line"></div>
      </div>
    </template>
    <template #second>
      <div class="ms-split-box-second">
        <div
          v-if="
            !props.notShowFirst &&
            props.direction === 'horizontal' &&
            props.expandDirection === 'left' &&
            !props.disabled
          "
          class="absolute h-full w-[12px]"
        >
          <div class="expand-icon" @click="() => changeExpand()">
            <MsIcon
              :type="isExpanded ? 'icon-icon_up-left_outlined' : 'icon-icon_down-right_outlined'"
              class="!w-auto text-[var(--color-text-brand)]"
              size="9"
            />
          </div>
        </div>
        <div
          :class="`ms-split-box ${props.direction === 'horizontal' ? 'ms-split-box--right' : 'ms-split-box--bottom'} ${
            props.secondContainerClass
          }`"
        >
          <slot name="second"></slot>
        </div>
      </div>
    </template>
  </a-split>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  export type Direction = 'horizontal' | 'vertical';

  const props = withDefaults(
    defineProps<{
      size?: number | string; // 左侧宽度/顶部容器高度。expandDirection为 right 时，size 也是左侧容器宽度，所以想要缩小右侧容器宽度只需要将 size 调大即可
      min?: number | string;
      max?: number | string;
      direction?: Direction;
      expandDirection?: 'left' | 'right' | 'top'; // TODO: 未实现 bottom，有场景再补充。目前默认水平是 left，垂直是 top
      disabled?: boolean; // 是否禁用
      firstContainerClass?: string; // first容器类名
      secondContainerClass?: string; // second容器类名
      notShowFirst?: boolean;
    }>(),
    {
      size: '300px',
      min: '300px',
      max: 0.5,
      direction: 'horizontal',
      expandDirection: 'left',
    }
  );

  const emit = defineEmits(['update:size', 'expandChange']);

  const innerSize = ref(props.size || '300px');
  const initialSize = props.size || '300px';
  const isExpanded = ref(true);
  const isExpandAnimating = ref(false); // 控制动画类

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

  function expand(size?: string | number) {
    isExpandAnimating.value = true;
    isExpanded.value = true;
    innerSize.value = size || initialSize || '300px'; // 按初始化的 size 展开，无论是水平还是垂直，都是宽度/高度
    emit('expandChange', true);
    // 动画结束，去掉动画类
    setTimeout(() => {
      isExpandAnimating.value = false;
    }, 300);
  }

  function collapse(size?: string | number) {
    isExpandAnimating.value = true;
    isExpanded.value = false;
    innerSize.value = props.expandDirection === 'right' ? 1 : size || '0px'; // expandDirection为 right 时，收起即为把左侧容器宽度提到 100%
    emit('expandChange', false);
    // 动画结束，去掉动画类
    setTimeout(() => {
      isExpandAnimating.value = false;
    }, 300);
  }

  function changeExpand() {
    if (isExpanded.value) {
      collapse();
    } else {
      expand();
    }
  }

  watch(
    () => props.notShowFirst,
    (val) => {
      if (val) {
        collapse();
      } else {
        expand();
      }
    },
    { immediate: true }
  );

  defineExpose({
    expand,
    collapse,
  });
</script>

<style lang="less" scoped>
  /* stylelint-disable value-keyword-case */
  .expanded-panel {
    :deep(.arco-split-pane) {
      @apply relative overflow-hidden;
    }
  }
  :deep(.arco-split-pane) {
    @apply relative overflow-hidden;
  }
  // :deep(.arco-split-pane-second) {
  //   @apply z-10;
  // }
  .animating {
    :deep(.arco-split-pane) {
      @apply relative overflow-hidden;

      transition: flex 0.3s ease;
    }
  }
  .ms-split-box {
    @apply relative h-full overflow-auto;
    .ms-scroll-bar();
    :deep(.arco-split-vertical) {
      .arco-split-pane-first {
        padding-bottom: 6px; // 为了避免滚动条遮挡，垂直布局下第二个盒子顶部有6px的阴影
      }
    }
  }
  .ms-split-box--left {
    width: calc(v-bind(innerSize) + 7px);
  }
  .expand-icon {
    @apply invisible relative flex cursor-pointer justify-center;

    top: 25%;
    z-index: 999;
    padding: 12px 2px;
    border-radius: 0 var(--border-radius-small) var(--border-radius-small) 0;
    background-color: var(--color-text-n9);
    transform: translateY(50%);
  }
  .ms-split-box-second {
    @apply h-full;
    &:hover {
      .expand-icon {
        @apply visible;
      }
    }
  }
  .ms-split-box--right {
    @apply w-full;
  }
  :deep(.arco-split-trigger-icon) {
    font-size: 14px;
  }
  .expand-icon--left {
    @apply rotate-180;

    border-radius: 0 var(--border-radius-small) var(--border-radius-small) 0;
  }
  .horizontal-expand-line {
    padding-left: 2px;
    height: 100%;
    .expand-color-line {
      width: 1px;
      height: 100%;
      background-color: var(--color-text-n8);
    }
    &:hover,
    &:active {
      background-color: rgb(var(--primary-5));
      .expand-color-line {
        background-color: transparent;
      }
    }
  }
  .ms-split-box--vertical {
    .ms-split-box--bottom {
      @apply h-full;

      background-color: var(--color-text-fff);
    }
    .vertical-expand-line {
      @apply relative flex items-center justify-center bg-transparent;

      z-index: 1;
      &::before {
        @apply absolute w-full bg-transparent;

        margin-bottom: -4px;
        height: 4px;
        box-shadow: 0 -2px 2px 0 rgb(31 35 41 / 10%), 0 -4px 4px 0 rgb(255 255 255), 0 -4px 4px 0 rgb(255 255 255),
          0 -4px 4px 0 rgb(255 255 255);
        content: '';
      }
      // .expand-icon--vertical {
      //   width: 20px;
      //   height: 0;
      //   margin-top: 4px;
      //   background-color: transparent;
      //   border-radius: 2px;
      //   background-color: var(--color-text-n8);
      // }
    }
  }
</style>
