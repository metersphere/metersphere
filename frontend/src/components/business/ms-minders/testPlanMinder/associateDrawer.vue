<template>
  <MsCaseAssociate
    v-model:visible="innerVisible"
    v-model:project-id="currentProjectId"
    :get-modules-api-type="CaseModulesApiTypeEnum.TEST_PLAN_LINK_CASE_MODULE"
    :get-page-api-type="CasePageApiTypeEnum.TEST_PLAN_CASE_PAGE"
    :get-module-count-api-type="CaseCountApiTypeEnum.TEST_PLAN_CASE_COUNT"
    :confirm-loading="confirmLoading"
    :extra-table-params="{
      testPlanId: props?.testPlanId,
    }"
    :extra-modules-params="{
      testPlanId: props?.testPlanId,
    }"
    :modules-maps="props.modulesMaps"
    :associated-type="associationType"
    :node-api-test-set="nodeApiTestSet"
    :node-scenario-test-set="nodeScenarioTestSet"
    @save="saveHandler"
  >
  </MsCaseAssociate>
</template>

<script setup lang="ts">
  import { type SelectOptionData } from '@arco-design/web-vue';

  import MsCaseAssociate from '@/components/business/ms-associate-case/index.vue';
  import type { saveParams } from '@/components/business/ms-associate-case/types';

  import useAppStore from '@/store/modules/app';

  import type { AssociateCaseRequestParams } from '@/models/testPlan/testPlan';
  import { CaseCountApiTypeEnum, CaseModulesApiTypeEnum, CasePageApiTypeEnum } from '@/enums/associateCaseEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';

  const props = defineProps<{
    associationType: CaseLinkEnum;
    modulesMaps?: Record<string, saveParams>;
    testPlanId?: string;
    nodeApiTestSet?: SelectOptionData[];
    nodeScenarioTestSet?: SelectOptionData[];
  }>();
  const innerVisible = defineModel<boolean>('visible', {
    required: true,
  });
  const emit = defineEmits<{
    (e: 'success', val: AssociateCaseRequestParams): void;
  }>();

  const appStore = useAppStore();
  const currentProjectId = ref(appStore.currentProjectId);

  const confirmLoading = ref<boolean>(false);

  async function saveHandler(params: AssociateCaseRequestParams) {
    emit('success', { ...params });

    innerVisible.value = false;
  }
</script>
