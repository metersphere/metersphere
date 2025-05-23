<template>
  <MsMoreSettingCollapse :content-class="`!mt-[16px] ${props.contentClass}`" default-expand>
    <template #header="{ collapse }">
      <div class="flex items-center gap-[8px]">
        <div v-if="collapse" class="collapse-icon down">
          <icon-down :size="10" class="block" />
        </div>
        <div v-else class="collapse-icon top p-[4px]">
          <icon-right :size="10" class="block" />
        </div>
        <div class="font-medium">
          {{ props.title }}
        </div>
      </div>
    </template>
    <template #content="{ collapse }">
      <slot name="content" :collapse="collapse"></slot>
    </template>
  </MsMoreSettingCollapse>
</template>

<script setup lang="ts">
  import MsMoreSettingCollapse from '@/components/pure/ms-more-setting-collapse/index.vue';

  const props = defineProps<{
    title: string;
    contentClass?: string;
  }>();
</script>

<style scoped lang="less">
  .collapse-icon {
    padding: 3px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    &.down {
      color: rgb(var(--primary-5));
      background: rgb(var(--primary-1));
    }
    &.top {
      color: rgb(var(--color-text-n4));
      background: var(--color-text-n8);
    }
  }
</style>
