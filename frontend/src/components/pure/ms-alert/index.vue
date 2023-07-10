<template>
  <a-alert v-bind="attrs" class="p-4" :class="`arco-alert-${type}`">
    <span class="float-left mr-3" :class="`arco-icon-${type}`"><icon-info-circle-fill /></span>
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
  .arco-icon-primary {
    color: rgb(var(--primary-5));
  }
  .arco-icon-error {
    color: rgb(rgb(var(--danger-6)));
  }
</style>
