<template>
  <a-alert v-bind="attrs" class="p-4" :class="`arco-alert-${type}`">
    <div>
      <slot name="content"></slot>
    </div>
  </a-alert>
</template>

<script setup lang="ts">
  import { useAttrs } from 'vue';

  const attrs = useAttrs();
  defineProps<{
    type: string;
  }>();
</script>

<style scoped>
  .arco-alert-primary {
    border-color: rgb(var(--primary-5));
    /* stylelint-disable-next-line color-function-notation */
    background-color: rgba(var(--primary-5), 0.1);
  }
  .arco-alert-error {
    border-color: rgb(var(--danger-6));
    /* stylelint-disable-next-line color-function-notation */
    background-color: rgba(var(--danger-6), 0.1);
  }
  .arco-alert-warning {
    border-color: rgb(var(--warning-6));
    /* stylelint-disable-next-line color-function-notation */
    background-color: rgba(var(--warning-6), 0.1);
  }
  .arco-alert-success {
    border-color: rgb(var(--success-6));
    /* stylelint-disable-next-line color-function-notation */
    background-color: rgba(var(--success-6), 0.1);
  }
</style>
