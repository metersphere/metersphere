<template>
  <div class="font-medium" :style="{ color: methodColor }">{{ props.method }}</div>
</template>

<script setup lang="ts">
  import { RequestMethods } from '@/enums/apiEnum';

  const props = defineProps<{
    method: RequestMethods;
  }>();

  const colorMaps = [
    {
      color: 'rgb(var(--success-6))',
      includes: [RequestMethods.GET, RequestMethods.HEAD],
    },
    {
      color: 'rgb(var(--warning-7))',
      includes: [RequestMethods.POST],
    },
    {
      color: 'rgb(var(--link-7))',
      includes: [RequestMethods.PUT, RequestMethods.OPTIONS],
    },
    {
      color: 'rgb(var(--danger-6))',
      includes: [RequestMethods.DELETE],
    },
    {
      color: 'rgb(var(--primary-7))',
      includes: [RequestMethods.PATCH],
    },
    {
      color: 'rgb(var(--primary-4))',
      includes: [RequestMethods.CONNECT],
    },
  ];

  const methodColor = computed(() => {
    const colorMap = colorMaps.find((item) => item.includes.includes(props.method));
    return colorMap?.color;
  });
</script>

<style lang="less" scoped></style>
