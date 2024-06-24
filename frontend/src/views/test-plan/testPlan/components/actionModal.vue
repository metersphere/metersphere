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
          v-if="showArchive"
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

  import { archivedPlan, deletePlan } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { characterLimit } from '@/utils';

  import type { CreateTask, TestPlanDetail, TestPlanItem } from '@/models/testPlan/testPlan';
  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    record: TestPlanItem | TestPlanDetail | undefined; // 表record
    scheduleConfig?: CreateTask;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'success', isDelete: boolean): void;
  }>();

  const showModalVisible = useVModel(props, 'visible', emit);

  function cancelHandler() {
    showModalVisible.value = false;
  }

  const confirmLoading = ref<boolean>(false);

  // 计划组删除
  async function confirmHandler(isDelete: boolean) {
    try {
      confirmLoading.value = true;
      if (isDelete) {
        await deletePlan(props.record?.id);
      } else {
        await archivedPlan(props.record?.id);
      }
      emit('success', isDelete);
      Message.success(isDelete ? t('common.deleteSuccess') : t('common.batchArchiveSuccess'));
      showModalVisible.value = false;
    } catch (error) {
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  const contentTip = computed(() => {
    let deleteMessage;
    const planType =
      props.record?.type === testPlanTypeEnum.GROUP
        ? `${t('testPlan.testPlanIndex.testPlanGroup')}`
        : `${t('testPlan.testPlanIndex.plan')}`;

    switch (props.record && props.record.status) {
      case 'ARCHIVED':
        deleteMessage = t('testPlan.testPlanIndex.deleteArchivedPlan');
        break;
      case 'UNDERWAY':
        deleteMessage = t('testPlan.testPlanIndex.deleteRunningPlan');
        break;
      case 'COMPLETED':
        deleteMessage = t('testPlan.testPlanGroup.planGroupDeleteContent');
        break;
      default:
        deleteMessage = t('testPlan.testPlanIndex.deletePendingPlan');
    }
    const scheduledMessage = props.scheduleConfig ? t('testPlan.testPlanIndex.scheduledTask') : '';
    return `${planType}${deleteMessage}${scheduledMessage}${t('testPlan.testPlanIndex.operateWithCaution')}`;
  });

  const showArchive = computed(() => {
    return props.record?.status === 'COMPLETED' && props.record.groupId && props.record.groupId === 'NONE';
  });
</script>

<style scoped lang="less">
  :deep(.ms-modal-form .arco-modal-body) {
    padding: 0 !important;
  }
</style>
