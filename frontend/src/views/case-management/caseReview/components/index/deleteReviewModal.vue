<template>
  <a-modal v-model:visible="dialogVisible" class="p-[4px]" title-align="start" body-class="p-0" :mask-closable="false">
    <template #title>
      <div class="flex items-center justify-start">
        <icon-exclamation-circle-fill size="20" class="mr-[8px] text-[rgb(var(--danger-6))]" />
        <a-tooltip :content="props.record.name">
          <div class="one-line-text text-[var(--color-text-1)]">
            {{ t('caseManagement.caseReview.deleteReviewTitle', { name: characterLimit(props.record.name) }) }}
          </div>
        </a-tooltip>
      </div>
    </template>
    <!-- <div v-if="props.record.status === 'COMPLETED'" class="mb-[10px]">
      <div>{{ t('caseManagement.caseReview.deleteFinishedReviewContent1') }}</div>
      <div>{{ t('caseManagement.caseReview.deleteFinishedReviewContent2') }}</div>
    </div> -->
    <div class="mb-[10px]">
      {{
        t('caseManagement.caseReview.deleteReviewContent', {
          status: t(reviewStatusMap[props.record.status as ReviewStatus].label),
        })
      }}
    </div>
    <!-- <a-input
      v-model:model-value="confirmReviewName"
      :placeholder="t('caseManagement.caseReview.deleteReviewPlaceholder')"
      :max-length="255"
    /> -->
    <template #footer>
      <div class="flex items-center justify-end">
        <a-button type="secondary" @click="handleDialogCancel">{{ t('common.cancel') }}</a-button>
        <a-button type="primary" status="danger" :disabled="loading" class="ml-[12px]" @click="handleDeleteConfirm">
          {{ t('common.confirmDelete') }}
        </a-button>
        <!-- <a-button
          v-if="props.record.status === 'COMPLETED'"
          :loading="loading"
          type="primary"
          class="ml-[12px]"
          @click="handleDeleteConfirm"
        >
          {{ t('caseManagement.caseReview.archive') }}
        </a-button> -->
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import { deleteReview } from '@/api/modules/case-management/caseReview';
  import { reviewStatusMap } from '@/config/caseManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { characterLimit } from '@/utils';

  import { ReviewStatus } from '@/models/caseManagement/caseReview';

  const props = defineProps<{
    visible: boolean;
    record: {
      id: string;
      name: string;
      status: ReviewStatus;
    };
  }>();
  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'success'): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const dialogVisible = useVModel(props, 'visible', emit);
  // const confirmReviewName = ref('');
  const loading = ref(false);

  function handleDialogCancel() {
    dialogVisible.value = false;
  }

  /**
   * 删除确认
   * @param done 关闭弹窗
   */
  async function handleDeleteConfirm() {
    try {
      loading.value = true;
      await deleteReview(props.record.id, appStore.currentProjectId);
      Message.success(t('common.deleteSuccess'));
      dialogVisible.value = false;
      emit('success');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }
</script>

<style lang="less" scoped></style>
