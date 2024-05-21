<template>
  <MsDrawer
    v-model:visible="editCaseVisible"
    :title="t('caseManagement.caseReview.updateCase')"
    :width="1200"
    :ok-text="t('common.update')"
    :ok-loading="updateCaseLoading"
    @confirm="updateCase"
  >
    <CaseTemplateDetail v-if="editCaseVisible" v-model:form-mode-value="editCaseForm" :case-id="props.caseId" />
  </MsDrawer>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import CaseTemplateDetail from '@/views/case-management/caseManagementFeature/components/caseTemplateDetail.vue';

  import { updateCaseRequest } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    caseId: string;
  }>();

  const emit = defineEmits<{
    (e: 'loadCase'): void;
  }>();

  const { t } = useI18n();

  const editCaseVisible = defineModel<boolean>('visible', {
    required: true,
  });
  const editCaseForm = ref<Record<string, any>>({});
  const updateCaseLoading = ref(false);

  async function updateCase() {
    try {
      updateCaseLoading.value = true;
      await updateCaseRequest(editCaseForm.value);
      editCaseVisible.value = false;
      Message.success(t('caseManagement.featureCase.editSuccess'));
      emit('loadCase');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      updateCaseLoading.value = false;
    } finally {
      updateCaseLoading.value = false;
    }
  }
</script>
