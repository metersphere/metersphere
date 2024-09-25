<template>
  <div
    v-if="showTagList.length"
    :class="`tag-group-class ${props.allowEdit ? 'cursor-pointer' : ''}`"
    @click="emit('click')"
  >
    <MsTag
      v-for="tag of showTagList"
      :key="tag.id"
      :class="`${props.showTable ? 'ms-tag-group' : ''}`"
      :width="getTagWidth(tag)"
      :size="props.size"
      v-bind="attrs"
    >
      {{ getTagContent(tag) }}
      <template v-if="props.showTable" #tooltipContent>
        {{ props.isStringTag ? tag : tag[props.nameKey] }}
      </template>
    </MsTag>
    <a-tooltip
      :disabled="!(props.tagList.length > props.showNum)"
      :content="tagsTooltip"
      :position="props.tagPosition"
      :mouse-enter-delay="300"
    >
      <MsTag
        v-show="props.tagList.length > props.showNum"
        class="ms-tag-num"
        :width="numberTagWidth"
        :min-width="60"
        :size="props.size"
        tooltip-disabled
        v-bind="attrs"
      >
        +
        {{ props.tagList.length - props.showNum }}
      </MsTag>
    </a-tooltip>
  </div>
  <!-- 避免在标签为空时，增大点击区域快速编辑 -->
  <div v-else :class="`tag-group-class ${props.allowEdit ? 'min-h-[24px] cursor-pointer' : ''}`" @click="emit('click')">
    -
  </div>
</template>

<script setup lang="ts">
  import { computed, useAttrs } from 'vue';

  import MsTag, { Size } from './ms-tag.vue';

  import { characterLimit } from '@/utils';

  const props = withDefaults(
    defineProps<{
      tagList: Array<any>;
      showNum?: number;
      nameKey?: string;
      isStringTag?: boolean; // 是否是字符串数组的标签
      size?: Size;
      allowEdit?: boolean;
      showTable?: boolean;
      tagPosition?:
        | 'top'
        | 'tl'
        | 'tr'
        | 'bottom'
        | 'bl'
        | 'br'
        | 'left'
        | 'lt'
        | 'lb'
        | 'right'
        | 'rt'
        | 'rb'
        | undefined; // 提示位置防止窗口抖动
    }>(),
    {
      showNum: 2,
      nameKey: 'name',
      size: 'medium',
      tagPosition: 'top',
    }
  );
  const emit = defineEmits<{
    (e: 'click'): void;
  }>();

  const attrs = useAttrs();

  const filterTagList = computed(() => {
    return (props.tagList || []).filter((item: any) => item) || [];
  });

  const showTagList = computed(() => {
    // 在表格展示则全部展示，按照自适应去展示标签个数
    if (props.showTable) {
      return filterTagList.value;
    }
    return filterTagList.value.slice(0, props.showNum);
  });

  const tagsTooltip = computed(() => {
    return filterTagList.value.map((e: any) => (props.isStringTag ? e : e[props.nameKey])).join('，');
  });

  const getTagWidth = (tag: { [x: string]: any }) => {
    const tagStr = props.isStringTag ? tag : tag[props.nameKey];

    const tagWidth = tagStr ? tagStr.length : 0;
    // 16个中文字符
    return tagWidth < 16 ? tagWidth : 16;
  };

  const numberTagWidth = computed(() => {
    const numberStr = `${props.tagList.length - props.showNum}`;
    return numberStr.length + 4;
  });

  function getTagContent(tag: { [x: string]: any }) {
    const tagContent = (props.isStringTag ? tag : tag[props.nameKey]) || '';
    if (props.showTable) {
      return tagContent.length > 16 ? characterLimit(tagContent, 9) : tagContent;
    }
    return tagContent;
  }
</script>

<style scoped lang="less">
  .tag-group-class {
    overflow: hidden;
    max-width: 440px;
    white-space: nowrap;
    @apply flex w-full flex-row;
  }
  .ms-tag-group {
    min-width: min-content !important;
    max-width: 144px !important;
  }
</style>
