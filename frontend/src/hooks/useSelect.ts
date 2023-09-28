import { Ref, computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { calculateMaxDepth } from '@/utils';

import type { CascaderOption, SelectOptionData } from '@arco-design/web-vue';

export interface UseSelectOption {
  selectRef: Ref; // 选择器 ref 对象
  selectVal: Ref; // 选择器的 v-model
  isCascade?: boolean; // 是否级联选择器
  panelWidth?: number; // 级联选择器的下拉面板宽度
  options?: CascaderOption[] | SelectOptionData[]; // 级联选择器的选项
}

/**
 * 计算 Select 组件的单行展示的最大标签数量
 * @param selectRef 选择器 ref 对象
 * @param selectVal 选择器的 v-model
 */
export default function useSelect(config: UseSelectOption) {
  const maxTagCount = ref(1);
  const selectWidth = ref(0);
  const selectViewInner = ref<HTMLElement | null>(null); // 输入框内容容器 DOM
  const cascadeDeep = ref(0); // 级联选择器的深度

  /**
   * 计算最大标签数量
   */
  function calculateMaxTag() {
    nextTick(() => {
      if (config.selectRef.value && selectViewInner.value && Array.isArray(config.selectVal.value)) {
        if (maxTagCount.value !== 0 && config.selectVal.value.length > maxTagCount.value) return; // 已经超过最大数量的展示，不需要再计算
        let lastWidth = selectViewInner.value?.getBoundingClientRect().width || 0;
        const tags = selectViewInner.value.querySelectorAll('.arco-tag');
        let tagCount = 0;
        for (let i = 0; i < tags.length; i++) {
          const tagWidth = Number(getComputedStyle(tags[i]).width.replace('px', ''));
          if (lastWidth < tagWidth + 65) {
            // 65px  是“+N”的标签宽度+聚焦输入框的宽度
            lastWidth = 0; // 当剩余宽度已经放不下刚添加的标签，则剩余宽度置为 0，避免后面再进行计算
            break;
          } else {
            tagCount += 1;
            lastWidth = lastWidth - tagWidth - 5;
          }
        }
        maxTagCount.value = tagCount === 0 ? 1 : tagCount;
      }
    });
  }

  const getOptionComputedStyle = computed(() => {
    if (config.isCascade) {
      // 减去 80px 是为了防止溢出，因为会出现单选框、右侧箭头
      return {
        width:
          cascadeDeep.value <= 2
            ? `${selectWidth.value / cascadeDeep.value - 80 - cascadeDeep.value * 4}px`
            : `${config.panelWidth}px` || '150px',
      };
    }
    // 减去 60px 是为了防止溢出，因为有复选框、边距等
    return {
      width: `${selectWidth.value - 60}px`,
    };
  });

  watch(
    () => config.options,
    (arr) => {
      if (config.isCascade && arr && arr.length > 0) {
        // 级联选择器的选项发生变化时，重新计算最大深度
        cascadeDeep.value = calculateMaxDepth(arr);
      }
    },
    {
      immediate: true,
      deep: true,
    }
  );

  onMounted(() => {
    if (config.selectRef.value) {
      selectWidth.value = config.selectRef.value.$el.nextElementSibling.getBoundingClientRect().width;
      selectViewInner.value = config.selectRef.value.$el.nextElementSibling.querySelector('.arco-select-view-inner');
    }
  });

  onBeforeUnmount(() => {
    selectViewInner.value = null; // 释放 DOM 引用
  });

  return {
    maxTagCount,
    getOptionComputedStyle, // 获取选择器选项的样式
    calculateMaxTag, // 在需要的时机调用此函数以计算最大标签数量，一般在 select 的 change 事件中调用
  };
}
