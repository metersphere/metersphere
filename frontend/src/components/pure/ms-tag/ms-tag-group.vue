<template>
  <a-tooltip :content="(props.tagList||[]).map((e: any) => e.name).join('，')">
    <div class="float-left flex min-h-[22px] max-w-[456px]">
      <MsTag v-for="tag of props.tagList.slice(0, props.showNum)" :key="tag.id" v-bind="attrs">
        <slot :tag="tag"> </slot>
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
    }>(),
    {
      showNum: 2,
    }
  );

  const attrs = useAttrs();
</script>

<style scoped lang="less"></style>
