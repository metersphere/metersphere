<template>
  <a-layout-footer class="footer">{{ props.text }}</a-layout-footer>
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
    display: flex;
    justify-content: center;
    align-items: center;
    height: 40px;
    text-align: center;
    color: var(--color-text-2);
  }
</style>
