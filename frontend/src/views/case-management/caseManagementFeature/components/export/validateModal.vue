<template>
  <a-modal
    v-model:visible="dialogVisible"
    title-align="start"
    class="ms-modal-form ms-modal-small"
    :ok-text="t('common.confirm')"
    :cancel-text="t('common.cancel')"
    unmount-on-close
    :footer="false"
    @close="handleCancel"
  >
    <template #title> {{ t('caseManagement.featureCase.importingUseCase') }} </template>
    <div class="flex h-[40px] items-center">
      <div class="mr-3 flex h-8 w-8 items-center justify-center bg-[var(--color-text-n9)]">
        <MsIcon
          :type="props.validateType === 'Excel' ? 'icon-icon_file-excel_colorful1' : 'icon-icon_file-xmind_colorful1'"
      /></div>

      <div class="flex w-[92%] flex-col">
        <span class="text-[var(--color-text-1)]">{{
          props.validateType === 'Excel'
            ? t('caseManagement.featureCase.verifyingFile')
            : t('caseManagement.featureCase.verifyingTemplate')
        }}</span>
        <a-progress :percent="props.percent" size="large" />
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    validateType: 'Excel' | 'Xmind';
    percent: number;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'cancel'): void;
    (e: 'checkFinished'): void;
  }>();

  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val),
  });

  const handleCancel = () => {
    dialogVisible.value = false;
  };
  watch(
    () => props.percent,
    (val) => {
      if (val === 1) {
        handleCancel();
        emit('checkFinished');
      }
    }
  );
</script>

<style scoped></style>
