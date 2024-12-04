<template>
  <a-cascader
    v-if="props.mode === 'MS'"
    ref="cascader"
    v-model:model-value="innerValue"
    class="ms-cascader"
    :options="props.options"
    :trigger-props="{ contentClass: `ms-cascader-popper ms-cascader-popper--${props.optionSize}` }"
    multiple
    allow-clear
    check-strictly
    :max-tag-count="props.shouldCalculateMaxTag ? maxTagCount : 1"
    :virtual-list-props="props.virtualListProps"
    :placeholder="props.placeholder"
    :loading="props.loading"
    :value-key="props.valueKey"
    :field-names="{
      label: props.labelKey,
      value: props.valueKey,
    }"
    :path-mode="false"
    @change="handleMsCascaderChange"
    @clear="clearValues"
    @popup-visible-change="updateTriggerWidth"
  >
    <template v-if="props.prefix" #prefix>
      {{ props.prefix }}
    </template>
    <template #label="{ data }">
      <a-tooltip :content="getInputLabelTooltip(data)" position="top" :mouse-enter-delay="500" mini>
        <div class="one-line-text inline-block">{{ getInputLabel(data) }}</div>
      </a-tooltip>
    </template>
    <template #option="{ data }">
      <a-tooltip :content="t(data.label)" position="top" :mouse-enter-delay="500" mini>
        <a-radio
          v-if="data.level === 0"
          v-model:model-value="innerLevel"
          :value="data.value[props.valueKey]"
          size="mini"
          @change="handleLevelChange"
        >
          <div class="one-line-text" :style="getOptionComputedStyle">{{ t(data.label) }}</div>
        </a-radio>
        <div v-else class="one-line-text" :style="getOptionComputedStyle">{{ t(data.label) }}</div>
      </a-tooltip>
    </template>
  </a-cascader>
  <a-cascader
    v-else
    ref="cascader"
    v-model:model-value="innerValue"
    class="ms-cascader"
    :options="props.options"
    :trigger-props="{ contentClass: `ms-cascader-popper-native ms-cascader-popper--${props.optionSize}` }"
    :multiple="props.multiple"
    allow-clear
    :load-more="props.loadMore"
    :check-strictly="props.strictly"
    :max-tag-count="props.shouldCalculateMaxTag ? maxTagCount : 1"
    :placeholder="props.placeholder"
    :virtual-list-props="props.virtualListProps"
    :loading="props.loading"
    :value-key="props.valueKey"
    :field-names="{
      label: props.labelKey,
      value: props.valueKey,
    }"
    :path-mode="props.pathMode"
    @change="(val) => emit('change', val)"
    @popup-visible-change="updateTriggerWidth"
  >
    <template v-if="props.prefix" #prefix>
      {{ props.prefix }}
    </template>
    <template #label="{ data }">
      <slot name="label" :data="{ ...data, [props.labelKey]: getInputLabel(data) }">
        <a-tooltip :content="getInputLabelTooltip(data)" position="top" :mouse-enter-delay="500" mini>
          <div class="one-line-text inline">
            {{ getInputLabel(data) }}
          </div>
        </a-tooltip>
      </slot>
    </template>
    <template #option="{ data }">
      <slot name="option" :data="data">
        <a-tooltip :content="t(data.label)" position="top" :mouse-enter-delay="500" mini>
          <div class="one-line-text" :style="getOptionComputedStyle">
            {{ t(data.label) }}
          </div>
        </a-tooltip>
      </slot>
    </template>
  </a-cascader>
</template>

<script setup lang="ts">
  import { Ref, ref, watch } from 'vue';
  import { useVModel } from '@vueuse/core';

  import { useI18n } from '@/hooks/useI18n';
  import useSelect from '@/hooks/useSelect';

  import type { CascaderOption } from '@arco-design/web-vue';
  import type { VirtualListProps } from '@arco-design/web-vue/es/_components/virtual-list-v2/interface';

  export type CascaderModelValue = string | number | Record<string, any> | (string | number | Record<string, any>)[];

  export interface MsCascaderProps {
    modelValue: CascaderModelValue;
    options: CascaderOption[];
    mode?: 'MS' | 'native'; // MS的多选、原生;这里的多选指的是出了一级以外的多选，一级是顶级分类选项只能单选。原生模式使用 arco-design 的 cascader 组件，只加了getOptionComputedStyle
    prefix?: string; // 输入框前缀
    levelTop?: string[]; // 顶级选项，多选时则必传
    level?: string; // 顶级选项，该级别为单选选项
    multiple?: boolean; // 是否多选
    strictly?: boolean; // 是否严格模式
    virtualListProps?: VirtualListProps; // 传入开启虚拟滚动
    panelWidth?: number; // 下拉框宽度，默认为 150px （超过两级时）
    placeholder?: string;
    loading?: boolean;
    optionSize?: 'small' | 'default';
    pathMode?: boolean; // 是否开启路径模式,TODO:目前 arco 组件库开启 pathmode 存在 BUG，不开启，实际上值也是 pathmode 格式的
    labelPathMode?: boolean; // 是否开启回显的 label 是路径模式
    valueKey?: string;
    labelKey?: string; // 传入自定义的 labelKey
    shouldCalculateMaxTag?: boolean; // 是否需要计算最大标签数
    loadMore?: (option: CascaderOption, done: (children?: CascaderOption[]) => void) => void;
  }

  const props = withDefaults(defineProps<MsCascaderProps>(), {
    mode: 'MS',
    optionSize: 'default',
    pathMode: false,
    valueKey: 'value',
    labelKey: 'label',
    shouldCalculateMaxTag: true,
  });
  const emit = defineEmits(['update:modelValue', 'update:level', 'change']);

  const { t } = useI18n();

  const innerValue = ref<CascaderModelValue>([]);
  const innerLevel = useVModel(props, 'level', emit); // 顶级选项，该级别为单选选项
  const cascader: Ref = ref(null);
  let selectedLabelObj: Record<string, any> = {}; // 存储已选的选项的label，用于多选时展示全部选项的 tooltip

  const { maxTagCount, getOptionComputedStyle, calculateMaxTag, updateTriggerWidth } = useSelect(
    {
      selectRef: cascader,
      selectVal: innerValue,
      isCascade: true,
      options: props.options,
      panelWidth: props.panelWidth,
    },
    props
  );

  watch(
    () => props.modelValue,
    (val) => {
      innerValue.value = val;
      if (
        props.mode === 'MS' &&
        Array.isArray(val) &&
        val[0] &&
        typeof val[0] === 'string' &&
        props.levelTop?.includes(val[0])
      ) {
        // 顶级选项，该级别为单选选项
        innerLevel.value = val[0] as string;
      }
      if (props.shouldCalculateMaxTag !== false && props.multiple) {
        calculateMaxTag();
      }
    },
    {
      immediate: true,
    }
  );

  watch(
    () => innerValue.value,
    (val) => {
      if (Array.isArray(val)) {
        // 选项变化时，清理一次已选的选项的label对象
        selectedLabelObj = {};
        for (let i = 0; i < val.length; i++) {
          const item = val[i];
          const value = typeof item === 'object' ? item[props.valueKey] : item;
          if (!props.labelPathMode) {
            selectedLabelObj[value] = t((item[props.labelKey] || '').split('/').pop() || '');
          } else {
            selectedLabelObj[value] = t(item[props.labelKey] || '');
          }
        }
      }
      emit('update:modelValue', val);
      if (val === '') {
        innerLevel.value = '';
        emit('update:level', val);
      }
    }
  );

  interface CascaderValue {
    level: keyof typeof props.levelTop;
    value: string;
  }

  function handleLevelChange(val: string | number | boolean) {
    innerValue.value = [val as string];
  }

  function handleMsCascaderChange(
    value:
      | string
      | number
      | Record<string, any>
      | (string | number | Record<string, any> | (string | number | Record<string, any>)[])[]
      | undefined
  ) {
    const lastValue = Array.isArray(value) ? value[value.length - 1] : '';
    if (lastValue && Array.isArray(innerValue.value)) {
      if (typeof lastValue === 'object') {
        // 当选中二级选项时，剔除选中的一级选项以及不同类别的选项
        innerValue.value = innerValue.value.filter(
          (e) => typeof e !== 'string' && (e as CascaderValue).level === lastValue.level
        );
        innerLevel.value = '';
      }
    }
    if (props.shouldCalculateMaxTag) {
      calculateMaxTag();
    }
  }

  // TODO: 临时解决 arco-design 的 cascader 组件已选项的label只能是带路径‘/’的 path-mode 的问题
  function getInputLabel(data: CascaderOption) {
    const isTagCount = (data[props.labelKey] || data.label).includes('+');
    if (isTagCount) {
      return data.label;
    }
    if (!props.labelPathMode) {
      return t((data.label || '').split('/').pop() || ''); // 取路径最后一级
    }
    return t(data.label || '');
  }

  function getInputLabelTooltip(data: CascaderOption) {
    const isTagCount = (data[props.labelKey] || data.label).includes('+');
    const label = getInputLabel(data);
    if (isTagCount && Array.isArray(innerValue.value)) {
      return Object.values(selectedLabelObj).join('，');
    }
    return label;
  }

  function clearValues() {
    innerLevel.value = '';
  }
</script>

<style lang="less">
  /* stylelint-disable value-keyword-case */
  .ms-cascader {
    @apply overflow-hidden;
    .arco-select-view-inner {
      height: 30px;
      .arco-select-view-tag {
        max-width: 75%;
      }
    }
  }
  .ms-cascader-popper,
  .ms-cascader-popper-native {
    .arco-cascader-panel {
      .arco-cascader-panel-column {
        .arco-cascader-column-content {
          padding: 4px 0;
        }
        .arco-virtual-list {
          .ms-scroll-bar();
        }
        .arco-cascader-option {
          margin: 2px 6px;
          border-radius: 4px;
        }
        .arco-cascader-option-active {
          background-color: rgb(var(--primary-1));
        }
      }
    }
  }
  .ms-cascader-popper {
    .arco-cascader-panel {
      .arco-cascader-panel-column:first-child {
        .arco-checkbox {
          @apply hidden;
        }
      }
    }
  }
  .ms-cascader-popper--small {
    .arco-cascader-panel {
      .arco-cascader-panel-column {
        .arco-cascader-option {
          height: 28px;
          line-height: 28px;
        }
      }
    }
  }
</style>
