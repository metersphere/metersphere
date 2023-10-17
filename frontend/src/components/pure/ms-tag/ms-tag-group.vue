<template>
  <div class="flex min-h-[22px] flex-row">
    <MsTag v-for="tag of showTagList" :key="tag.id" v-bind="attrs">
      {{ props.isStringTag ? tag : tag[props.nameKey] }}
    </MsTag>
    <a-tooltip :content="tagsTooltip">
      <MsTag v-if="props.tagList.length > props.showNum" v-bind="attrs">
        +{{ props.tagList.length - props.showNum }}</MsTag
      >
    </a-tooltip>
  </div>
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
    return props.tagList.filter((item: any) => item) || [];
  });

  const showTagList = computed(() => {
    return filterTagList.value.slice(0, props.showNum);
  });

  const tagsTooltip = computed(() => {
    return filterTagList.value.map((e: any) => (props.isStringTag ? e : e[props.nameKey])).join('，');
  });
</script>

<style scoped lang="less"></style>
