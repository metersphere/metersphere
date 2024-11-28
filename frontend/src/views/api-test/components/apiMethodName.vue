<template>
  <div v-if="props.method">
    <MsTag
      v-if="props.isTag"
      :self-style="{
        border: `1px solid ${props.tagBackgroundColor || methodColor}`,
        color: props.tagTextColor || methodColor,
        backgroundColor: props.tagBackgroundColor || 'var(--color-text-fff)',
        display: 'flex',
      }"
      :size="props.tagSize"
    >
      {{ props.method }}
    </MsTag>
    <div v-else class="font-medium" :style="{ color: methodColor }">{{ props.method }}</div>
  </div>
  <div v-else>-</div>
</template>

<script setup lang="ts">
  import MsTag, { Size } from '@/components/pure/ms-tag/ms-tag.vue';

  import { RequestMethods } from '@/enums/apiEnum';

  const props = withDefaults(
    defineProps<{
      method: RequestMethods | string;
      isTag?: boolean;
      tagSize?: Size;
      tagBackgroundColor?: string;
      tagTextColor?: string;
    }>(),
    {
      isTag: false,
      tagSize: 'medium',
    }
  );

  const colorMaps = [
    {
      color: 'rgb(var(--success-7))',
      includes: [RequestMethods.GET, RequestMethods.HEAD, 'HTTP'],
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
    if (props.method) {
      const colorMap = colorMaps.find((item) => item.includes.includes(props.method as RequestMethods));
      return colorMap?.color || 'rgb(var(--link-7))'; // 方法映射内找不到对应的 key 说明是插件，所有的插件协议颜色都是一样的
    }
    return 'rgb(var(--link-7))';
  });
</script>

<style lang="less" scoped></style>
