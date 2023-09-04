<template>
  <a-tag
    v-bind="attrs"
    :type="props.type"
    defer
    :style="{ ...typeStyle, 'margin': tagMargin, 'max-width': '144px' }"
    :size="props.size"
    class="my-1"
  >
    <slot name="icon"></slot>
    <span class="one-line-text"> <slot></slot></span>
  </a-tag>
</template>

<script setup lang="ts">
  import { computed, ref, useAttrs, watchEffect } from 'vue';

  export type TagType = 'default' | 'primary' | 'danger' | 'warning' | 'success';
  export type Size = 'small' | 'medium' | 'large';
  export type Theme = 'dark' | 'light' | 'outline' | 'lightOutLine';

  const attrs = useAttrs();
  const props = withDefaults(
    defineProps<{
      type?: TagType; // tag类型
      size?: Size; // tag尺寸
      theme?: Theme; // tag主题
      selfStyle?: any; // 自定义样式
    }>(),
    {
      type: 'default',
      theme: 'dark',
      size: 'medium',
    }
  );

  // 标签之间的间距
  const tagMargin = computed(() => {
    switch (props.size) {
      case 'medium':
        return '3px';
      case 'large':
        return '4px';
      default:
        return '2px';
    }
  });

  // 计算标签的颜色和背景颜色
  const typeList: any = {
    dark: {
      'color': 'white',
      'border-color': 'rgb(var(--#{}-5))',
      'background': 'rgb(var(--#{}-5))',
    },
    light: {
      color: 'rgb(var(--#{}-5))',
      background: 'rgb(var(--#{}-1)',
    },
    outline: {
      'border-color': 'rgb(var(--#{}-5))',
      'color': 'rgb(var(--#{}-5))',
      'background': 'transparent',
    },
    lightOutLine: {
      'border-color': 'rgb(var(--#{}-5))',
      'color': 'rgb(var(--#{}-5))',
      'background': 'rgb(var(--#{}-1)',
    },
    default: {
      'color': 'var(--color-text-1)',
      'background': props.theme !== 'outline' ? 'var(--color-text-n8)' : 'white',
      'border-color':
        props.theme === 'lightOutLine' || props.theme === 'outline'
          ? 'var(--color-text-input-border)'
          : 'var(--color-text-n8)',
    },
  };

  const typeConst = ref<string>('');
  const typeStyle = ref<string[]>();
  // 样式优先级: 自定义样式 > default 样式 > 主题和类型样式
  const getTagType = (type: string, theme: string) => {
    if (props.selfStyle && Object.keys(props.selfStyle).length > 0) {
      typeStyle.value = props.selfStyle;
    } else {
      if (type === 'default') {
        typeStyle.value = typeList.default;
        return;
      }
      // 主题色
      if (type === 'primary') {
        typeConst.value = 'primary';
        // 非主题色
      } else {
        typeConst.value = type;
      }
      // 返回非主题色style
      if (theme !== 'default' && type !== 'default') {
        const themeStyle = typeList[theme];
        Object.keys(themeStyle).forEach((item) => {
          themeStyle[item] = themeStyle[item].replace('#{}', typeConst.value);
        });
        typeStyle.value = themeStyle;
      }
    }
  };

  watchEffect(() => {
    if (props.type && props.theme) {
      getTagType(props.type, props.theme);
    }
  });
</script>

<style scoped lang="less"></style>
