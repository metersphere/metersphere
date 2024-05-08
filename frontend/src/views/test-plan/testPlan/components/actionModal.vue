<template>
  <a-modal
    v-model:visible="showModalVisible"
    class="ms-modal-form ms-modal-no-padding ms-modal-small"
    unmount-on-close
    title-align="start"
    :mask="true"
    :mask-closable="false"
    @close="cancelHandler"
  >
    <template #title>
      <div class="flex items-center justify-start">
        <MsIcon type="icon-icon_close_colorful" class="mr-[8px] text-[rgb(var(--danger-6))]" size="16" />
        <div class="text-[var(--color-text-1)]">
          {{ t('common.deleteConfirmTitle', { name: characterLimit(record?.name) }) }}
        </div>
      </div>
    </template>
    {{ contentTip }}
    <template #footer>
      <div class="flex justify-end">
        <a-button type="secondary" :disabled="confirmLoading" @click="cancelHandler">
          {{ t('common.cancel') }}
        </a-button>
        <a-button class="ml-3" type="primary" status="danger" :loading="confirmLoading" @click="confirmHandler(true)">
          {{ t('common.confirmDelete') }}
        </a-button>
        <a-button
          v-if="props.record?.status === 'COMPLETED'"
          :loading="confirmLoading"
          class="ml-3"
          type="primary"
          @click="confirmHandler(false)"
        >
          {{ t('common.archive') }}
        </a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import { useI18n } from '@/hooks/useI18n';
  import { characterLimit } from '@/utils';

  import type { TestPlanItem } from '@/models/testPlan/testPlan';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    // isScheduled: boolean; // TODO 这个版本不做有无定时任务区分
    record: TestPlanItem | undefined; // 表record
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
  }>();

  const showModalVisible = useVModel(props, 'visible', emit);

  function cancelHandler() {
    showModalVisible.value = false;
  }

  const confirmLoading = ref<boolean>(false);
  function confirmHandler(isDelete: boolean) {
    try {
      Message.success(isDelete ? t('common.deleteSuccess') : t('common.batchArchiveSuccess'));
    } catch (error) {
      console.log(error);
    }
  }

  function getDeleteTip() {
    switch (props.record && props.record.status) {
      case 'ARCHIVED':
        return t('testPlan.testPlanIndex.deleteArchivedPlan');
      case 'UNDERWAY':
        return t('testPlan.testPlanIndex.deleteRunningPlan');
      case 'COMPLETED':
        return t('testPlan.testPlanIndex.deleteCompletedPlan');
      default:
        return t('testPlan.testPlanIndex.deletePendingPlan');
    }
  }

  const contentTip = computed(() => {
    return getDeleteTip();
  });
</script>

<style scoped lang="less">
  :deep(.ms-modal-form .arco-modal-body) {
    padding: 0 !important;
  }
</style>
