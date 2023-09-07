<template>
  <a-tooltip :content="(props.tagList.filter(((item:any)=>item))||[]).map((e: any) => e[nameKey]).join('，')">
    <div class="flex min-h-[22px] max-w-[456px] flex-row">
      <MsTag
        v-for="tag of (props.tagList.filter((item:any)=>item)).slice(0, props.showNum)"
        :key="tag.id"
        v-bind="attrs"
      >
        {{ tag[props.nameKey] }}
      </MsTag>
      <MsTag v-if="props.tagList.length > props.showNum" v-bind="attrs">
        +{{ props.tagList.length - props.showNum }}</MsTag
      >
    </div>
  </a-tooltip>
</template>

<script setup lang="ts">
  import { useAttrs } from 'vue';
  import MsTag from './ms-tag.vue';

  const props = withDefaults(
    defineProps<{
      tagList: any;
      showNum?: number;
      nameKey?: string;
    }>(),
    {
      showNum: 2,
      nameKey: 'name',
    }
  );

  const attrs = useAttrs();
</script>

<style scoped lang="less"></style>
