<template>
  <div
    v-if="showTagList.length"
    :class="`tag-group-class ${props.allowEdit ? 'cursor-pointer' : ''}`"
    @click="emit('click')"
  >
    <MsTag v-for="tag of showTagList" :key="tag.id" :width="getTagWidth(tag)" :size="props.size" v-bind="attrs">
      {{ props.isStringTag ? tag : tag[props.nameKey] }}
    </MsTag>
    <a-tooltip
      v-if="props.tagList.length > props.showNum"
      :content="tagsTooltip"
      :position="props.tagPosition"
      :mouse-enter-delay="300"
    >
      <MsTag :width="numberTagWidth" :size="props.size" tooltip-disabled v-bind="attrs">
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

  const props = withDefaults(
    defineProps<{
      tagList: Array<any>;
      showNum?: number;
      nameKey?: string;
      isStringTag?: boolean; // 是否是字符串数组的标签
      size?: Size;
      allowEdit?: boolean;
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
</script>

<style scoped lang="less">
  .tag-group-class {
    max-width: 440px;
    @apply flex w-full flex-row;
  }
</style>
