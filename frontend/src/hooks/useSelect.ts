import { computed, nextTick, onBeforeUnmount, onMounted, Ref, ref, watch } from 'vue';

import { calculateMaxDepth } from '@/utils';

import type { CascaderOption, SelectOptionData } from '@arco-design/web-vue';

export interface UseSelectOption {
  selectRef: Ref; // 选择器 ref 对象
  selectVal: Ref; // 选择器的 v-model
  isCascade?: boolean; // 是否级联选择器
  panelWidth?: number; // 级联选择器的下拉面板宽度
  options?: CascaderOption[] | SelectOptionData[]; // 选择器的选项
  valueKey?: string; // 选项的 value 字段名，默认为 value
  labelKey?: string; // 选项的 label 字段名，默认为 label
}

/**
 * 计算 Select 组件的单行展示的最大标签数量
 * @param selectRef 选择器 ref 对象
 * @param selectVal 选择器的 v-model
 */
export default function useSelect(config: UseSelectOption) {
  const maxTagCount = ref(0);
  const selectWidth = ref(0);
  const selectViewInner = ref<HTMLElement | null>(null); // 输入框内容容器 DOM
  const singleTagMaxWidth = ref(0); // 单个标签的最大宽度，当只显示得下一个标签时，有可能出现标签+额外宽度超出选择框的情况导致选择框换行撑高了
  const cascadeDeep = ref(0); // 级联选择器的深度

  /**
   * 计算最大标签数量
   */
  function calculateMaxTag() {
    nextTick(() => {
      if (config.selectRef.value && selectViewInner.value && Array.isArray(config.selectVal.value)) {
        if (maxTagCount.value >= 1 && config.selectVal.value.length > maxTagCount.value) return; // 已经超过最大数量的展示，不需要再计算
        const innerViewWidth = selectViewInner.value?.getBoundingClientRect().width;
        let lastWidth = innerViewWidth - 60; // 60px 是“+N”的标签宽度+聚焦输入框的宽度
        let tagCount = 0;
        const values = Object.values(config.selectVal.value);
        for (let i = 0; i < values.length; i++) {
          const tagWidth = values[i][config.labelKey || 'label'].length * 12; // 计算每个标签渲染出来的宽度，文字大小在12px时宽度也是 12px
          if (lastWidth > tagWidth + 36) {
            tagCount += 1;
            lastWidth -= tagWidth + 36; // 36px是标签的边距、边框等宽度
          } else {
            lastWidth = 0; // 当剩余宽度已经放不下刚添加的标签，则剩余宽度置为 0，避免后面再进行计算
            break;
          }
        }
        if (lastWidth === 0) {
          maxTagCount.value = tagCount || 1;
        }
        if (tagCount <= 1 && values.length > 0) {
          singleTagMaxWidth.value = innerViewWidth - 100; // 100px 是 60px + 标签边距边框和 x 图标等40px
        } else {
          singleTagMaxWidth.value = 0;
        }
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
    singleTagMaxWidth,
    getOptionComputedStyle, // 获取选择器选项的样式
    calculateMaxTag, // 在需要的时机调用此函数以计算最大标签数量，一般在 select 的 change 事件中调用
  };
}
