<template>
  <a-tabs
    v-if="props.mode === 'origin'"
    v-model:active-key="tempActiveKey"
    :class="[props.class, props.noContent ? 'no-content' : '']"
    @change="(val) => handleTabClick(val as string)"
  >
    <a-tab-pane v-for="item of props.contentTabList" :key="item.value" :title="item.label">
      <template v-if="props.showBadge" #title>
        <a-badge
          v-if="props.getTextFunc(item.value) !== ''"
          :class="item.value === tempActiveKey ? 'active-badge' : ''"
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
  <div v-else class="ms-tab--button">
    <div
      v-for="item of props.contentTabList"
      :key="item.value"
      class="ms-tab--button-item"
      :class="item.value === tempActiveKey ? 'ms-tab--button-item--active' : ''"
      @click="handleTabClick(item.value)"
    >
      {{ item.label }}
    </div>
  </div>
</template>

<script setup lang="ts">
  const props = withDefaults(
    defineProps<{
      mode?: 'origin' | 'button';
      contentTabList: { label: string; value: string }[];
      class?: string;
      getTextFunc?: (value: any) => string;
      noContent?: boolean;
      showBadge?: boolean;
      changeInterceptor?: (newVal: string, oldVal: string, done: () => void) => void;
    }>(),
    {
      mode: 'origin',
      showBadge: true,
      getTextFunc: (value: any) => value,
      class: '',
    }
  );

  // 实际值，用于最终确认修改的 tab 值
  const activeKey = defineModel<string>('activeKey', {
    default: '',
  });
  // 临时值，用于组件内部变更，但未影响到实际值
  const tempActiveKey = ref(activeKey.value);

  watch(
    () => activeKey.value,
    (val) => {
      tempActiveKey.value = val;
    }
  );

  function handleTabClick(value: string) {
    if (value === activeKey.value) {
      return;
    }
    if (props.changeInterceptor) {
      // 存在拦截器，则先将临时值重置为实际值（此时实际值是未变更之前的值），再执行拦截器
      tempActiveKey.value = activeKey.value;
      props.changeInterceptor(value, activeKey.value, () => {
        // 拦截器成功回调=》修改实际值，也将临时值修改为实际值
        activeKey.value = value;
        tempActiveKey.value = value;
      });
    } else {
      // 不存在拦截器，直接修改实际值
      activeKey.value = value;
    }
  }
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
  .no-content {
    :deep(.arco-tabs-content) {
      display: none;
    }
  }
  .ms-tab--button {
    @apply flex;

    border-radius: var(--border-radius-small);
    .ms-tab--button-item {
      @apply cursor-pointer;

      padding: 4px 12px;
      border: 1px solid var(--color-text-n8);
      color: var(--color-text-2);
      &:first-child {
        border-top-left-radius: var(--border-radius-small);
        border-bottom-left-radius: var(--border-radius-small);
      }
      &:last-child {
        border-top-right-radius: var(--border-radius-small);
        border-bottom-right-radius: var(--border-radius-small);
      }
      &:not(:last-child) {
        margin-right: -1px;
      }
      &:hover {
        color: rgb(var(--primary-5));
      }
    }
    .ms-tab--button-item--active {
      z-index: 2;
      border: 1px solid rgb(var(--primary-5)) !important;
      color: rgb(var(--primary-5));
    }
  }
</style>
