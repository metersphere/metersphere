<template>
  <a-spin class="block h-full" :loading="props.loading" :size="28">
    <div class="ms-card relative h-full pr-[10px]">
      <div v-if="!simple" class="card-header">
        <div class="back-btn" @click="back"><icon-arrow-left /></div>
        <div class="text-[var(--color-text-000)]">{{ props.title }}</div>
      </div>
      <a-divider v-if="!simple" class="mb-[16px]" />
      <div class="mr-[-10px]">
        <a-scrollbar
          class="pr-[10px]"
          :style="{
            overflowY: 'auto',
            minWidth: 1000,
            height: `calc(100vh - ${256 + specialHeight}px)`,
          }"
        >
          <slot></slot>
        </a-scrollbar>
      </div>
      <div
        v-if="!hideFooter && !simple"
        class="relative z-10 m-[0_-24px_-24px] flex justify-end gap-[16px] p-[24px] shadow-[0_-1px_4px_rgba(2,2,2,0.1)]"
      >
        <div class="ml-0 mr-auto">
          <slot name="footerLeft"></slot>
        </div>
        <slot name="footerRight">
          <a-button type="secondary" @click="back">{{ t('mscard.defaultCancelText') }}</a-button>
          <a-button v-if="!props.hideContinue && !props.isEdit" type="secondary" @click="emit('saveAndContinue')">
            {{ t('mscard.defaultSaveAndContinueText') }}
          </a-button>
          <a-button type="primary" @click="emit('save')">
            {{ t(props.isEdit ? 'mscard.defaultUpdate' : 'mscard.defaultConfirm') }}
          </a-button>
        </slot>
      </div>
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router';
  import { useI18n } from '@/hooks/useI18n';

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
        handleBack: () => void;
      }>
    >(),
    {
      simple: false,
      hideContinue: false,
      hideFooter: false,
      isEdit: false,
      specialHeight: 0,
    }
  );

  const emit = defineEmits(['saveAndContinue', 'save']);

  const router = useRouter();
  const { t } = useI18n();
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
