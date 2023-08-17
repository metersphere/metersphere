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
  import { ref, watch, Ref, nextTick, onMounted, computed, onBeforeUnmount } from 'vue';
  import { calculateMaxDepth } from '@/utils';

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
  const maxTagCount = ref(1); // 最大显示 tag 数量
  const cascader: Ref = ref(null);
  const cascaderWidth = ref(0); // cascader 宽度
  const cascaderDeep = ref(1); // 默认层级只有一层
  const cascaderViewInner = ref<HTMLElement | null>(null); // 输入框内容容器 DOM

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

  watch(
    () => props.options,
    (arr) => {
      cascaderDeep.value = calculateMaxDepth(arr);
    },
    {
      immediate: true,
      deep: true,
    }
  );

  const getOptionComputedStyle = computed(() => {
    // 减去 80px 是为了防止溢出，因为会出现单选框、右侧箭头
    return {
      width: cascaderDeep.value <= 2 ? `${cascaderWidth.value / cascaderDeep.value - 80}px` : '150px',
    };
  });

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
    nextTick(() => {
      if (cascader.value && cascaderViewInner.value && Array.isArray(innerValue.value)) {
        if (maxTagCount.value !== 0 && innerValue.value.length > maxTagCount.value) return; // 已经超过最大数量的展示，不需要再计算
        let lastWidth = cascaderViewInner.value?.getBoundingClientRect().width || 0;
        const tags = cascaderViewInner.value.querySelectorAll('.arco-tag');
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

  onMounted(() => {
    if (cascader.value) {
      cascaderWidth.value = cascader.value.$el.nextElementSibling.getBoundingClientRect().width;
      cascaderViewInner.value = cascader.value.$el.nextElementSibling.querySelector('.arco-select-view-inner');
    }
  });

  onBeforeUnmount(() => {
    cascaderViewInner.value = null; // 释放 DOM 引用
  });
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
      }
      .arco-cascader-panel-column:first-child {
        .arco-checkbox {
          @apply hidden;
        }
      }
    }
  }
</style>
