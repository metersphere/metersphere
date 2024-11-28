<template>
  <a-tag
    v-bind="attrs"
    :type="props.type"
    defer
    :size="props.size"
    :style="{
      ...typeStyle,
      'margin-right': noMargin ? 0 : tagMargin,
      'min-width': props.width && `${props.width}ch`,
      'max-width': props.maxWidth || '144px',
    }"
    :closable="props.closable"
    @close="emit('close')"
  >
    <slot name="icon"></slot>
    <a-tooltip :disabled="props.tooltipDisabled">
      <div :class="`one-line-text max-w-[${props.maxWidth || '144px'}]`">
        <slot></slot>
      </div>
      <template v-if="$slots.tooltipContent" #content>
        <slot name="tooltipContent"></slot>
      </template>
      <template v-else #content>
        <slot></slot>
      </template>
    </a-tooltip>
  </a-tag>
</template>

<script setup lang="ts">
  import { computed, ref, useAttrs, watchEffect } from 'vue';
  import { cloneDeep } from 'lodash-es';

  export type TagType = 'default' | 'primary' | 'danger' | 'warning' | 'success' | 'link';
  export type Size = 'small' | 'medium' | 'large';
  export type Theme = 'dark' | 'light' | 'outline' | 'lightOutLine' | 'default';

  const attrs = useAttrs();
  const props = withDefaults(
    defineProps<{
      type?: TagType; // tag类型
      size?: Size; // tag尺寸
      theme?: Theme; // tag主题
      selfStyle?: Record<string, any>; // 自定义样式
      width?: number; // tag宽度,不传入时绑定max-width
      maxWidth?: string;
      noMargin?: boolean; // tag之间是否有间距
      closable?: boolean; // 是否可关闭
      tooltipDisabled?: boolean; // 是否禁用tooltip
    }>(),
    {
      type: 'default',
      theme: 'dark',
      size: 'medium',
      noMargin: false,
    }
  );
  const emit = defineEmits<{
    (e: 'close'): void;
  }>();

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
  const typeList: Record<string, any> = {
    dark: {
      'color': 'var(--color-text-fff)',
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
      'background': props.theme !== 'outline' ? 'var(--color-text-n8)' : 'var(--color-text-fff)',
      'border-color':
        props.theme === 'lightOutLine' || props.theme === 'outline'
          ? 'var(--color-text-input-border)'
          : 'var(--color-text-n8)',
    },
  };

  const typeConst = ref<string>('');
  const typeStyle = ref<Record<string, any>>();
  // 样式优先级: 自定义样式 > default 样式 > 主题和类型样式
  const getTagType = (type: string, theme: Theme) => {
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
        const themeStyle = cloneDeep(typeList[theme]); // 引用类型直接使用会修改源数据，导致props更新的时候取的不是默认值了，是修改后的值
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

<style lang="less">
  .arco-tag {
    .arco-icon {
      font-size: 14px !important;
    }
    .arco-icon-close {
      color: var(--color-text-4) !important;
      &:hover {
        color: var(--color-text-1) !important;
      }
    }
    .arco-icon-close-hover.arco-tag-icon-hover::before {
      background: none !important;
    }
  }
  .arco-tag-size-small {
    line-height: 16px;
  }
</style>
