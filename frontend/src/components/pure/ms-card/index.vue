<template>
  <a-spin class="!block h-full" :loading="props.loading" :size="28">
    <div
      :class="[
        'ms-card',
        'relative',
        'h-full',
        props.autoHeight ? '' : 'min-h-[500px]',
        props.noContentPadding ? 'ms-card--noContentPadding' : 'p-[24px]',
      ]"
    >
      <div v-if="!props.simple" class="card-header">
        <div v-if="!props.hideBack" class="back-btn" @click="back"><icon-arrow-left /></div>
        <div class="font-medium text-[var(--color-text-000)]">{{ props.title }}</div>
      </div>
      <a-divider v-if="!props.simple" class="mb-[16px]" />
      <div class="ms-card-container">
        <a-scrollbar
          class="pr-[5px]"
          :style="{
            overflow: 'auto',
            width: `calc(100vw - ${menuWidth}px - 58px)`,
            height: props.autoHeight ? 'auto' : `calc(100vh - ${cardOverHeight}px)`,
          }"
        >
          <div class="min-w-[1000px]">
            <slot></slot>
          </div>
        </a-scrollbar>
      </div>
      <div
        v-if="!props.hideFooter && !props.simple"
        class="fixed bottom-0 right-[16px] z-10 flex items-center bg-white p-[24px] shadow-[0_-1px_4px_rgba(2,2,2,0.1)]"
        :style="{ width: `calc(100% - ${menuWidth + 16}px)` }"
      >
        <div class="ml-0 mr-auto">
          <slot name="footerLeft"></slot>
        </div>
        <slot name="footerRight">
          <div class="flex justify-end gap-[16px]">
            <a-button type="secondary" @click="back">{{ t('mscard.defaultCancelText') }}</a-button>
            <a-button v-if="!props.hideContinue && !props.isEdit" type="secondary" @click="emit('saveAndContinue')">
              {{ t('mscard.defaultSaveAndContinueText') }}
            </a-button>
            <a-button type="primary" @click="emit('save')">
              {{ t(props.isEdit ? 'mscard.defaultUpdate' : 'mscard.defaultConfirm') }}
            </a-button>
          </div>
        </slot>
      </div>
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import { useRouter } from 'vue-router';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  const props = withDefaults(
    defineProps<
      Partial<{
        simple: boolean; // 简单模式，没有标题和底部栏
        title: string; // 卡片标题
        hideContinue: boolean; // 隐藏保存并继续创建按钮
        hideFooter: boolean; // 隐藏底部栏
        loading: boolean; // 卡片 loading 状态
        isEdit: boolean; // 是否编辑状态
        specialHeight: number; // 特殊高度，例如某些页面有面包屑
        hideBack: boolean; // 隐藏返回按钮
        autoHeight: boolean; // 内容区域高度是否自适应
        hasBreadcrumb: boolean; // 是否有面包屑，如果有面包屑，高度需要减去面包屑的高度
        noContentPadding: boolean; // 内容区域是否有padding
        handleBack: () => void; // 自定义返回按钮触发事件
      }>
    >(),
    {
      simple: false,
      hideContinue: false,
      hideFooter: false,
      isEdit: false,
      specialHeight: 0,
      hideBack: false,
      autoHeight: false,
      hasBreadcrumb: false,
      noContentPadding: false,
    }
  );

  const emit = defineEmits(['saveAndContinue', 'save']);

  const router = useRouter();
  const { t } = useI18n();

  const appStore = useAppStore();
  const collapsedWidth = 86;
  const menuWidth = computed(() => {
    return appStore.menuCollapse ? collapsedWidth : appStore.menuWidth;
  });

  const _specialHeight = props.hasBreadcrumb ? 31 + props.specialHeight : props.specialHeight; // 有面包屑的话，默认面包屑高度31

  const cardOverHeight = computed(() => {
    if (props.simple) {
      // 简单模式没有标题、没有底部
      return 136 + _specialHeight;
    }
    if (props.hideFooter) {
      // 隐藏底部
      return 192;
    }
    return 246 + _specialHeight;
  });

  function back() {
    if (typeof props.handleBack === 'function') {
      props.handleBack();
    } else {
      router.back();
    }
  }
</script>

<style lang="less" scoped>
  .ms-card {
    @apply overflow-hidden bg-white;

    border-radius: var(--border-radius-large);
    box-shadow: 0 0 10px rgb(120 56 135 / 5%);
    &--noContentPadding {
      border-radius: var(--border-radius-large) var(--border-radius-large) 0 0;
      .card-header {
        padding: 24px 24px 0;
      }
      .arco-divider {
        @apply mb-0;
      }
      .ms-card-container {
        padding: 0;
      }
    }
    .card-header {
      @apply flex items-center;
      .back-btn {
        @apply flex cursor-pointer items-center rounded-full;

        margin-right: 8px;
        width: 20px;
        height: 20px;
        border: 1px solid #ffffff;
        background: linear-gradient(90deg, rgb(var(--primary-9)) 3.36%, #ffffff 100%);
        box-shadow: 0 0 7px rgb(15 0 78 / 9%);
        .arco-icon {
          color: rgb(var(--primary-5));
        }
      }
    }
    :deep(.arco-scrollbar-track-direction-vertical) {
      right: -10px;
    }
    :deep(.arco-scrollbar-track-direction-horizontal) {
      bottom: -10px;
    }
  }
</style>
