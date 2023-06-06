<template>
  <div class="my-container h-[calc(100% - 40px)] px-[20px] pb-[20px] pt-0">
    <Breadcrumb :items="['menu.exception', 'menu.exception.404']" />
    <div class="content rounded-[4px]">
      <a-result class="result" status="404" :subtitle="$t('exception.result.404.description')"> </a-result>
      <div class="operation-row">
        <a-button key="again" style="margin-right: 16px">
          {{ $t('exception.result.404.retry') }}
        </a-button>
        <a-button key="back" type="primary">
          {{ $t('exception.result.404.back') }}
        </a-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup></script>

<script lang="ts">
  export default {
    name: '404',
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
