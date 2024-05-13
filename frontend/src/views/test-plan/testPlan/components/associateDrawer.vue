<template>
  <MsCaseAssociate
    v-model:visible="innerVisible"
    v-model:currentSelectCase="currentSelectCase"
    :get-modules-func="getCaseModuleTree"
    :get-table-func="getCaseList"
    :confirm-loading="confirmLoading"
    :associated-ids="[]"
    :project-id="currentProjectId"
    :type="RequestModuleEnum.CASE_MANAGEMENT"
    hide-project-select
    is-hidden-case-level
    :has-not-associated-ids="props.hasNotAssociatedIds"
    @save="saveHandler"
  >
  </MsCaseAssociate>
</template>

<script setup lang="ts">
  import MsCaseAssociate from '@/components/business/ms-case-associate/index.vue';
  import { RequestModuleEnum } from '@/components/business/ms-case-associate/utils';

  import { getCaseList, getCaseModuleTree } from '@/api/modules/case-management/featureCase';
  import useAppStore from '@/store/modules/app';

  import type { AssociateCaseRequest } from '@/models/testPlan/testPlan';
  import { CaseLinkEnum } from '@/enums/caseEnum';

  const props = defineProps<{
    hasNotAssociatedIds?: string[];
  }>();
  const innerVisible = defineModel<boolean>('visible', {
    required: true,
  });
  const emit = defineEmits<{
    (e: 'success', val: AssociateCaseRequest): void;
  }>();

  const appStore = useAppStore();

  const currentSelectCase = ref<keyof typeof CaseLinkEnum>('FUNCTIONAL');
  const currentProjectId = ref(appStore.currentProjectId);

  const confirmLoading = ref<boolean>(false);

  function saveHandler(params: AssociateCaseRequest) {
    try {
      confirmLoading.value = true;
      emit('success', { ...params, functionalSelectIds: params.selectIds });
      innerVisible.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }
</script>
