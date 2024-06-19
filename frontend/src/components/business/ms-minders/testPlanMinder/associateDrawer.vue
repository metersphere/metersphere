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
    :associated-ids="props.hasNotAssociatedIds || []"
    :associated-type="associationType"
    @save="saveHandler"
  >
  </MsCaseAssociate>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsCaseAssociate from '@/components/business/ms-associate-case/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { AssociateCaseRequest, AssociateCaseRequestType } from '@/models/testPlan/testPlan';
  import { CaseCountApiTypeEnum, CaseModulesApiTypeEnum, CasePageApiTypeEnum } from '@/enums/associateCaseEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';

  const { t } = useI18n();
  const props = defineProps<{
    associationType: CaseLinkEnum;
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
  const currentProjectId = ref(appStore.currentProjectId);

  const confirmLoading = ref<boolean>(false);
  const planId = ref(route.query.id as string);

  async function saveHandler(params: AssociateCaseRequest) {
    if (typeof props.saveApi !== 'function') {
      emit('success', { ...params });
    } else {
      try {
        confirmLoading.value = true;
        await props.saveApi({
          functionalSelectIds: params.selectIds,
          testPlanId: planId.value,
        });
        emit('success', { ...params });
        Message.success(t('ms.case.associate.associateSuccess'));
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        confirmLoading.value = false;
      }
    }
    innerVisible.value = false;
  }
</script>
