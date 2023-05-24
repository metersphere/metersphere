<template>
  <div class="my-container">
    <Breadcrumb :items="['menu.exception', 'menu.exception.500']" />
    <div class="content">
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
    padding: 0 20px 20px;
    height: calc(100% - 40px);
    :deep(.content) {
      position: relative;
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100%;
      border-radius: 4px;
      text-align: center;
      background-color: var(--color-bg-1);
      flex-direction: column;
    }
  }
</style>
