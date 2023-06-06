<template>
  <div class="my-container h-[calc(100% - 40px)] px-[20px] pb-[20px] pt-0">
    <Breadcrumb :items="['menu.exception', 'menu.exception.500']" />
    <div class="content rounded-[4px]">
      <a-result class="result" status="500" :subtitle="$t('exception.result.500.description')" />
      <a-button key="back" type="primary">
        {{ $t('exception.result.500.back') }}
      </a-button>
    </div>
  </div>
</template>

<script lang="ts" setup></script>

<script lang="ts">
  export default {
    name: '500',
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
