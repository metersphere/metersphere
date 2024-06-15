<template>
  <MsCard class="mb-[16px]" simple auto-height auto-width>
    <div class="font-medium">{{ t('report.detail.reportSummary') }}</div>
    <div
      :class="`${hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+UPDATE']) && !shareId ? '' : 'cursor-not-allowed'}`"
    >
      <MsRichText
        v-model:raw="innerSummary.summary"
        v-model:filedIds="innerSummary.richTextTmpFileIds"
        :upload-image="handleUploadImage"
        :preview-url="PreviewEditorImageUrl"
        class="mt-[8px] w-full"
        :editable="!!shareId"
      />
      <MsFormItemSub
        v-if="hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+UPDATE']) && !shareId && props.showButton"
        :text="t('report.detail.oneClickSummary')"
        :show-fill-icon="true"
        @fill="handleSummary"
      />
    </div>

    <div
      v-show="props.showButton && hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+UPDATE']) && !shareId"
      class="mt-[16px] flex items-center gap-[12px]"
    >
      <a-button type="primary" @click="handleUpdateReportDetail">{{ t('common.save') }}</a-button>
      <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsFormItemSub from '@/components/business/ms-form-item-sub/index.vue';

  import { editorUploadFile } from '@/api/modules/test-plan/report';
  import { PreviewEditorImageUrl } from '@/api/requrls/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import { hasAnyPermission } from '@/utils/permission';

  import type { PlanReportDetail } from '@/models/testPlan/testPlanReport';

  import { getSummaryDetail } from '@/views/test-plan/report/utils';

  const { t } = useI18n();
  const props = defineProps<{
    richText: { summary: string; richTextTmpFileIds?: string[] };
    shareId?: string;
    showButton: boolean;
    isPlanGroup: boolean;
    detail: PlanReportDetail;
  }>();

  const emit = defineEmits<{
    (e: 'updateSummary'): void;
    (e: 'cancel'): void;
    (e: 'handleSummary', content: string): void;
  }>();

  const innerSummary = useVModel(props, 'richText', emit);

  function handleCancel() {
    emit('cancel');
  }

  function handleUpdateReportDetail() {
    emit('updateSummary');
  }

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }

  const summaryContent = computed(() => {
    const { functionalCount, apiCaseCount, apiScenarioCount } = props.detail;
    const functionalCaseDetail = getSummaryDetail(functionalCount);
    const apiCaseDetail = getSummaryDetail(apiCaseCount);
    const apiScenarioDetail = getSummaryDetail(apiScenarioCount);
    const allCaseTotal = functionalCaseDetail.caseTotal + apiCaseDetail.caseTotal + apiScenarioDetail.caseTotal;
    const allHasExecutedCase =
      functionalCaseDetail.hasExecutedCase + apiCaseDetail.hasExecutedCase + apiScenarioDetail.hasExecutedCase;
    const allSuccessCase = functionalCaseDetail.success + apiCaseDetail.success + apiScenarioDetail.success;

    // 通过率
    const allSuccessCount = (allSuccessCase / allCaseTotal) * 100;
    const allSuccessRate = `${Number.isNaN(allSuccessCount) ? 0 : allSuccessCount.toFixed(2)}%`;
    // TODO 待联调
    if (props.isPlanGroup) {
      return `<p style=""><span color="" fontsize=""> <strong>${props.detail.name}</strong>包含 ${props.detail.planCount}个子计划。
             其中 ${props.detail.passCountOfPlan} 个子计划通过， ${props.detail.failCountOfPlan} 个子计划不通过。</span></p>`;
    }
    // 接口用例通过率
    return `<p style=""><span color="" fontsize=""> <strong>${props.detail.name}</strong> 包含功能测试、接口用例、场景用例, 共 ${allCaseTotal}条用例，已执行 ${allHasExecutedCase} 条，通过用例 ${allSuccessCase} 条，通过率为 ${allSuccessRate}，达到/未达到通过阈值（通过阈值为${props.detail.passThreshold}%），<strong>${props.detail.name}</strong> 计划满足/不满足发布要求。<br>
      （1）本次测试包含${functionalCaseDetail.caseTotal}条功能测试用例，执行了${functionalCaseDetail.hasExecutedCase}条，未执行${functionalCaseDetail.pending}条，执行率为${functionalCaseDetail.apiExecutedRate}，通过用例${functionalCaseDetail.success}条，通过率为${functionalCaseDetail.successRate}。共发现缺陷${props.detail.functionalBugCount}个。<br>
      （2）本次测试包含${apiCaseDetail.caseTotal}条接口测试用例，执行了${apiCaseDetail.hasExecutedCase}条，未执行${apiCaseDetail.pending}条，执行率为${apiCaseDetail.apiExecutedRate}，通过用例${apiCaseDetail.success}条，通过率为${apiCaseDetail.successRate}。共发现缺陷 ${props.detail.apiBugCount} 个。<br>
      （3）本次测试包含${apiScenarioDetail.caseTotal}条场景测试用例，执行了${apiScenarioDetail.hasExecutedCase}条，未执行${apiScenarioDetail.pending}条，执行率为${apiScenarioDetail.apiExecutedRate}%，通过用例${apiScenarioDetail.success}条，通过率为${apiScenarioDetail.successRate}。共发现缺陷${props.detail.scenarioBugCount}个</span></p>
  `;
  });

  function handleSummary() {
    emit('handleSummary', summaryContent.value);
  }
</script>

<style scoped></style>
