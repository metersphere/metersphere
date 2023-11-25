<template>
  <a-tooltip :content="tagsTooltip">
    <div class="flex max-w-[440px] flex-row">
      <MsTag v-for="tag of showTagList" :key="tag.id" :width="getTagWidth(tag)" v-bind="attrs">
        {{ props.isStringTag ? tag : tag[props.nameKey] }}
      </MsTag>
      <MsTag v-if="props.tagList.length > props.showNum" :width="numberTagWidth" v-bind="attrs">
        +{{ props.tagList.length - props.showNum }}</MsTag
      >
    </div>
  </a-tooltip>
</template>

<script setup lang="ts">
  import { computed, useAttrs } from 'vue';

  import MsTag from './ms-tag.vue';

  const props = withDefaults(
    defineProps<{
      tagList: Array<any>;
      showNum?: number;
      nameKey?: string;
      isStringTag?: boolean; // 是否是字符串数组的标签
    }>(),
    {
      showNum: 2,
      nameKey: 'name',
    }
  );

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
    const tagWidth = tagStr.length;
    // 16个中文字符
    return tagWidth < 16 ? tagWidth : 16;
  };

  const numberTagWidth = computed(() => {
    const numberStr = `${props.tagList.length - props.showNum}`;
    return numberStr.length + 4;
  });
</script>

<style scoped lang="less"></style>
