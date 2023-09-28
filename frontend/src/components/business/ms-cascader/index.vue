<template>
  <a-cascader
    v-if="props.mode === 'MS'"
    ref="cascader"
    v-model="innerValue"
    class="ms-cascader"
    :options="props.options"
    :trigger-props="{ contentClass: 'ms-cascader-popper' }"
    multiple
    allow-clear
    check-strictly
    :max-tag-count="maxTagCount"
    :virtual-list-props="props.virtualListProps"
    :placeholder="props.placeholder"
    :loading="props.loading"
    @change="handleMsCascaderChange"
    @clear="clearValues"
  >
    <template #prefix>
      {{ props.prefix }}
    </template>
    <template #label="{ data }">
      <a-tooltip :content="data.label" position="top" :mouse-enter-delay="500" mini>
        <div class="one-line-text inline-block">{{ data.label }}</div>
      </a-tooltip>
    </template>
    <template #option="{ data }">
      <a-tooltip :content="data.label" position="top" :mouse-enter-delay="500" mini>
        <a-radio
          v-if="data.level === 0"
          v-model:model-value="innerLevel"
          :value="data.value.value"
          size="mini"
          @change="handleLevelChange"
        >
          <div class="one-line-text" :style="getOptionComputedStyle">{{ data.label }}</div>
        </a-radio>
        <div v-else class="one-line-text" :style="getOptionComputedStyle">{{ data.label }}</div>
      </a-tooltip>
    </template>
  </a-cascader>
  <a-cascader
    v-else
    ref="cascader"
    v-model="innerValue"
    class="ms-cascader"
    :options="props.options"
    :trigger-props="{ contentClass: 'ms-cascader-popper' }"
    :multiple="props.multiple"
    allow-clear
    :check-strictly="props.strictly"
    :max-tag-count="maxTagCount"
    :placeholder="props.placeholder"
    :virtual-list-props="props.virtualListProps"
    :loading="props.loading"
  >
    <template #prefix>
      {{ props.prefix }}
    </template>
    <template #label="{ data }">
      <a-tooltip :content="data.label" position="top" :mouse-enter-delay="500" mini>
        <div class="one-line-text inline translate-y-[15%]">{{ data.label }}</div>
      </a-tooltip>
    </template>
    <template #option="{ data }">
      <a-tooltip :content="data.label" position="top" :mouse-enter-delay="500" mini>
        <div class="one-line-text" :style="getOptionComputedStyle">{{ data.label }}</div>
      </a-tooltip>
    </template>
  </a-cascader>
</template>

<script setup lang="ts">
  import { ref, watch, Ref } from 'vue';
  import useSelect from '@/hooks/useSelect';

  import type { CascaderOption } from '@arco-design/web-vue';
  import type { VirtualListProps } from '@arco-design/web-vue/es/_components/virtual-list-v2/interface';

  export type CascaderModelValue = string | number | Record<string, any> | (string | number | Record<string, any>)[];

  const props = withDefaults(
    defineProps<{
      modelValue: CascaderModelValue;
      options: CascaderOption[];
      mode?: 'MS' | 'native'; // MS的多选、原生;这里的多选指的是出了一级以外的多选，一级是顶级分类选项只能单选。原生模式使用 arco-design 的 cascader 组件，只加了getOptionComputedStyle
      prefix?: string; // 输入框前缀
      levelTop?: string[]; // 顶级选项，多选时则必传
      level?: string; // 顶级选项，该级别为单选选项
      multiple?: boolean; // 是否多选
      strictly?: boolean; // 是否严格模式
      virtualListProps?: VirtualListProps; // 传入开启虚拟滚动
      panelWidth?: number; // 下拉框宽度，默认为 150px
      placeholder?: string;
      loading?: boolean;
    }>(),
    {
      mode: 'MS',
    }
  );
  const emit = defineEmits(['update:modelValue', 'update:level']);

  const innerValue = ref<CascaderModelValue>([]);
  const innerLevel = ref(''); // 顶级选项，该级别为单选选项
  const cascader: Ref = ref(null);

  const { maxTagCount, getOptionComputedStyle, calculateMaxTag } = useSelect({
    selectRef: cascader,
    selectVal: innerValue,
    isCascade: true,
    options: props.options,
    panelWidth: props.panelWidth,
  });

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
        innerLevel.value = val[0] as string;
      }
    },
    {
      immediate: true,
    }
  );

  watch(
    () => innerValue.value,
    (val) => {
      emit('update:modelValue', val);
      if (val === '') {
        innerLevel.value = '';
        emit('update:level', val);
      }
    }
  );

  watch(
    () => props.level,
    (val) => {
      innerLevel.value = val || '';
    },
    {
      immediate: true,
    }
  );

  watch(
    () => innerLevel.value,
    (val) => {
      emit('update:level', val);
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
    calculateMaxTag();
  }

  function clearValues() {
    innerLevel.value = '';
  }
</script>

<style lang="less">
  .ms-cascader {
    @apply overflow-hidden;
    .arco-select-view-inner {
      height: 30px;
      .arco-select-view-tag {
        max-width: 75%;
      }
    }
  }
  .ms-cascader-popper {
    .arco-cascader-panel {
      .arco-cascader-panel-column {
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
      .arco-cascader-panel-column:first-child {
        .arco-checkbox {
          @apply hidden;
        }
      }
    }
  }
</style>
@/hooks/useSelect
