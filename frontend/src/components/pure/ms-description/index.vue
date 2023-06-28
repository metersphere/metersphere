<template>
  <a-descriptions :data="props.descriptions" size="large" :column="1">
    <a-descriptions-item v-for="item of props.descriptions" :key="item.label" :label="item.label">
      <template v-if="item.isTag">
        <a-tag
          v-for="tag of item.value.split(',')"
          :key="tag"
          color="var(--color-text-n8)"
          class="mr-[8px] font-normal !text-[var(--color-text-1)]"
        >
          {{ tag }}
        </a-tag>
      </template>
      <div v-else>{{ item.value }}</div>
    </a-descriptions-item>
  </a-descriptions>
</template>

<script setup lang="ts">
  export interface Description {
    label: string;
    value: string;
    isTag?: boolean;
  }

  const props = defineProps<{ descriptions: Description[] }>();
</script>

<style lang="less" scoped>
  .arco-descriptions-item-label {
    @apply whitespace-pre-wrap font-normal;
  }
  .arco-descriptions-item-label-block {
    padding-right: 16px !important;
    word-wrap: break-word;
    width: 120px;
  }
  .arco-descriptions-item-value-block {
    @apply !pr-0 align-top;
  }
</style>
