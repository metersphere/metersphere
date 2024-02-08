<template>
  <MsTag
    v-if="props.isTag"
    :self-style="{ border: `1px solid ${methodColor}`, color: methodColor, backgroundColor: 'white' }"
  >
    {{ props.method }}
  </MsTag>
  <div v-else class="font-medium" :style="{ color: methodColor }">{{ props.method }}</div>
</template>

<script setup lang="ts">
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { RequestMethods } from '@/enums/apiEnum';

  const props = defineProps<{
    method: RequestMethods;
    isTag?: boolean; // 是否展示为标签
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
    return colorMap?.color || 'rgb(var(--link-7))'; // 方法映射内找不到对应的 key 说明是插件，所有的插件协议颜色都是一样的
  });
</script>

<style lang="less" scoped></style>
