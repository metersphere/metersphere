<template>
  <MsCaseAssociate
    v-model:visible="innerVisible"
    v-model:currentSelectCase="currentSelectCase"
    :get-modules-func="getCaseModuleTree"
    :get-table-func="getTestPlanCaseList"
    :confirm-loading="confirmLoading"
    :table-params="{
      testPlanId: props?.testPlanId,
    }"
    :associated-ids="props.hasNotAssociatedIds || []"
    :project-id="currentProjectId"
    :type="RequestModuleEnum.CASE_MANAGEMENT"
    hide-project-select
    :is-hidden-case-level="false"
    :selector-all="true"
    @save="saveHandler"
  >
  </MsCaseAssociate>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsCaseAssociate from '@/components/business/ms-case-associate/index.vue';
  import { RequestModuleEnum } from '@/components/business/ms-case-associate/utils';

  import { getCaseModuleTree } from '@/api/modules/case-management/featureCase';
  import { getTestPlanCaseList } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { AssociateCaseRequest, AssociateCaseRequestType } from '@/models/testPlan/testPlan';
  import { CaseLinkEnum } from '@/enums/caseEnum';

  const { t } = useI18n();
  const props = defineProps<{
    hasNotAssociatedIds?: string[];
    saveApi?: (params: AssociateCaseRequestType) => Promise<any>;
    testPlanId?: string;
  }>();
  const innerVisible = defineModel<boolean>('visible', {
    required: true,
  });
  const emit = defineEmits<{
    (e: 'success', val: AssociateCaseRequest): void;
  }>();

  const appStore = useAppStore();
  const route = useRoute();
  const currentSelectCase = ref<keyof typeof CaseLinkEnum>('FUNCTIONAL');
  const currentProjectId = ref(appStore.currentProjectId);

  const confirmLoading = ref<boolean>(false);
  const planId = ref(route.query.id as string);

  async function saveHandler(params: AssociateCaseRequest) {
    try {
      confirmLoading.value = true;
      if (typeof props.saveApi !== 'function') {
        emit('success', { ...params, functionalSelectIds: params.selectIds });
      } else {
        try {
          await props.saveApi({
            functionalSelectIds: params.selectIds,
            testPlanId: planId.value,
          });
          emit('success', { ...params, functionalSelectIds: params.selectIds });
          Message.success(t('ms.case.associate.associateSuccess'));
          confirmLoading.value = false;
        } catch (error) {
          console.log(error);
        }
      }
      innerVisible.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }
</script>
