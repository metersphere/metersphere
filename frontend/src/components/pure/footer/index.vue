<template>
  <a-layout-footer class="footer h-[40px]">{{ props.text }}</a-layout-footer>
</template>

<script lang="ts" setup>
  const props = withDefaults(
    defineProps<{
      text: string;
    }>(),
    {
      text: 'MeterSphere',
    }
  );
</script>

<style lang="less" scoped>
  .footer {
    @apply flex items-center justify-center text-center;

    color: var(--color-text-2);
  }
</style>
