<template>
  <a-tabs v-model:active-key="innerActiveKey" :class="[props.class, props.noContent ? 'no-content' : '']">
    <a-tab-pane v-for="item of props.contentTabList" :key="item.value" :title="item.label">
      <template #title>
        <a-badge
          v-if="props.getTextFunc(item.value) !== ''"
          :class="item.value === innerActiveKey ? 'active-badge' : ''"
          :max-count="99"
          :text="props.getTextFunc(item.value)"
        >
          <div class="mr-[4px]">
            {{ item.label }}
          </div>
        </a-badge>
        <div v-else>
          {{ item.label }}
        </div>
      </template>
    </a-tab-pane>
  </a-tabs>
</template>

<script setup lang="ts">
  const props = withDefaults(
    defineProps<{
      activeKey: string;
      contentTabList: { label: string; value: string }[];
      class?: string;
      getTextFunc?: (value: any) => string;
      noContent?: boolean;
    }>(),
    {
      getTextFunc: (value: any) => value,
      class: '',
    }
  );

  const innerActiveKey = defineModel<string>('activeKey', {
    default: '',
  });
</script>

<style lang="less" scoped>
  :deep(.arco-badge) {
    @apply flex items-center;

    line-height: 22px;
    .arco-badge-text,
    .arco-badge-number {
      @apply relative right-0 top-0  transform-none shadow-none;
    }
  }
  :deep(.active-badge) {
    .arco-badge-text,
    .arco-badge-number {
      background-color: rgb(var(--primary-5));
    }
  }
  .no-content {
    :deep(.arco-tabs-content) {
      display: none;
    }
  }
</style>
