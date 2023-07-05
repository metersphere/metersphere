<template>
  <div class="relative h-full">
    <div class="card-header">
      <div class="back-btn" @click="back"><icon-arrow-left /></div>
      <div class="text-[var(--color-text-000)]">{{ props.title }}</div>
    </div>
    <a-divider />
    <a-scrollbar class="mt-[16px]" style="overflow-y: auto; height: calc(100vh - 264px)">
      <slot></slot>
    </a-scrollbar>
    <div
      v-if="!hideFooter"
      class="m-[0_-24px_-24px] flex justify-end gap-[16px] p-[24px] shadow-[0_-1px_4px_rgba(2,2,2,0.1)]"
    >
      <a-button type="secondary" @click="back">{{ t('mscard.defaultCancelText') }}</a-button>
      <a-button v-if="!props.hideContinue" type="secondary" @click="emit('saveAndContinue')">
        {{ t('mscard.defaultSaveAndContinueText') }}
      </a-button>
      <a-button type="primary" @click="emit('save')">{{ t('mscard.defaultConfirm') }}</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import { useRouter } from 'vue-router';

  const props = withDefaults(
    defineProps<{
      title: string;
      hideContinue?: boolean;
      handleBack?: () => void;
      hideFooter?: boolean;
    }>(),
    {
      hideContinue: false,
      hideFooter: false,
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
        font-size: 20px !important;
        color: rgb(var(--primary-5));
      }
    }
  }
</style>
