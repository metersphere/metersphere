<template>
  <div class="my-container h-[calc(100% - 40px)] px-[20px] pb-[20px] pt-0">
    <Breadcrumb :items="['menu.exception', 'menu.exception.403']" />
    <div class="content rounded-[4px]">
      <a-result class="result" status="403" :subtitle="$t('exception.result.403.description')" />
      <a-button key="back" type="primary">
        {{ $t('exception.result.403.back') }}
      </a-button>
    </div>
  </div>
</template>

<script lang="ts" setup></script>

<script lang="ts">
  export default {
    name: '403',
  };
</script>

<style scoped lang="less">
  .my-container {
    :deep(.content) {
      @apply relative flex h-full flex-col items-center justify-center text-center;

      background-color: var(--color-bg-1);
    }
  }
</style>
