<template>
  <a-spin class="block h-full" :loading="props.loading" :size="28">
    <div class="ms-card relative h-full pr-[10px]">
      <div v-if="!props.simple" class="card-header">
        <div v-if="!props.hideBack" class="back-btn" @click="back"><icon-arrow-left /></div>
        <div class="text-[var(--color-text-000)]">{{ props.title }}</div>
      </div>
      <a-divider v-if="!props.simple" class="mb-[16px]" />
      <div class="mr-[-10px]">
        <a-scrollbar
          class="pr-[10px]"
          :style="{
            overflowY: 'auto',
            minWidth: 1000,
            height: `calc(100vh - ${cardOverHeight}px)`,
          }"
        >
          <slot></slot>
        </a-scrollbar>
      </div>
      <div
        v-if="!props.hideFooter && !props.simple"
        class="fixed bottom-0 right-[16px] z-10 w-full bg-white p-[24px] shadow-[0_-1px_4px_rgba(2,2,2,0.1)]"
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
  import { useRouter } from 'vue-router';
  import { useI18n } from '@/hooks/useI18n';
  import { computed } from 'vue';

  const props = withDefaults(
    defineProps<
      Partial<{
        simple: boolean;
        title: string;
        hideContinue: boolean;
        hideFooter: boolean;
        loading: boolean;
        isEdit: boolean;
        specialHeight: number; // 特殊高度，例如某些页面有面包屑
        hideBack: boolean;
        handleBack: () => void;
      }>
    >(),
    {
      simple: false,
      hideContinue: false,
      hideFooter: false,
      isEdit: false,
      specialHeight: 0,
      hideBack: false,
    }
  );

  const emit = defineEmits(['saveAndContinue', 'save']);

  const router = useRouter();
  const { t } = useI18n();

  const cardOverHeight = computed(() => {
    if (props.simple) {
      // 简单模式没有标题、没有底部
      return 163;
    }
    if (props.hideFooter) {
      // 隐藏底部
      return 192;
    }
    return 256 + props.specialHeight;
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
  }
</style>
