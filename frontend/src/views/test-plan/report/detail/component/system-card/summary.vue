<template>
  <div :class="`${hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+UPDATE']) && !shareId ? '' : 'cursor-not-allowed'}`">
    <MsRichText
      v-model:raw="innerSummary.content"
      v-model:filedIds="innerSummary.richTextTmpFileIds"
      :upload-image="handleUploadImage"
      :preview-url="ReportPlanPreviewImageUrl"
      class="mt-[8px] w-full"
      :editable="props.canEdit"
      @click="handleClick"
    />
    <MsFormItemSub
      v-if="
        hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+UPDATE']) &&
        !shareId &&
        props.showButton &&
        props.canEdit &&
        props.isPreview
      "
      :text="t('report.detail.oneClickSummary')"
      :show-fill-icon="true"
      @fill="handleSummary"
    />
  </div>

  <div
    v-show="props.showButton && hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+UPDATE']) && !shareId && props.canEdit"
    class="mt-[16px] flex items-center gap-[12px]"
  >
    <a-button type="primary" @click="handleUpdateReportDetail">{{ t('common.save') }}</a-button>
    <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';

  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsFormItemSub from '@/components/business/ms-form-item-sub/index.vue';

  import { editorUploadFile } from '@/api/modules/test-plan/report';
  import { ReportPlanPreviewImageUrl } from '@/api/requrls/test-plan/report';
  import useDoubleClick from '@/hooks/useDoubleClick';
  import { useI18n } from '@/hooks/useI18n';
  import { hasAnyPermission } from '@/utils/permission';

  import type { customValueForm, PlanReportDetail } from '@/models/testPlan/testPlanReport';

  import { getSummaryDetail } from '@/views/test-plan/report/utils';

  const { t } = useI18n();
  const props = defineProps<{
    richText: customValueForm;
    shareId?: string;
    showButton: boolean;
    isPlanGroup: boolean;
    detail: PlanReportDetail;
    canEdit: boolean;
    isPreview?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'updateSummary', form: customValueForm): void;
    (e: 'cancel'): void;
    (e: 'dblclick'): void;
    (e: 'handleSummary', content: string): void;
  }>();

  const innerSummary = useVModel(props, 'richText', emit);

  function handleCancel() {
    emit('cancel');
  }

  function handleUpdateReportDetail() {
    emit('updateSummary', innerSummary.value);
  }

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }

  function getResultText(isPass: boolean, text: string) {
    const successColor = 'rgb(76, 217, 100)';
    const errorColor = 'rgb(255, 59, 48)';
    const color = isPass ? successColor : errorColor;
    return `<strong><span style="color: ${color}" color="${color}" fontsize="">${text}</span></strong>`;
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
    const allSuccessRate = `${Number.isNaN(allSuccessCount) ? 0 : allSuccessCount.toFixed(2)}`;

    if (props.isPlanGroup) {
      return `<p style=""><span color="" fontsize=""> <strong>${props.detail.testPlanName}</strong>包含 ${props.detail.planCount}个子计划。
             其中 ${props.detail.passCountOfPlan} 个子计划通过， ${props.detail.failCountOfPlan} 个子计划不通过。</span></p>`;
    }
    const functionalCasText = `（1）本次测试包含${functionalCaseDetail.caseTotal}条功能测试用例，执行了${functionalCaseDetail.hasExecutedCase}条，未执行${functionalCaseDetail.pending}条，执行率为${functionalCaseDetail.apiExecutedRate}，通过用例${functionalCaseDetail.success}条，通过率为${functionalCaseDetail.successRate}。共发现缺陷${props.detail.functionalBugCount}个。<br>`;
    const functionCaseDesc = functionalCaseDetail.caseTotal ? `${functionalCasText}` : ``;

    const apiCaseText = `（2）本次测试包含${apiCaseDetail.caseTotal}条接口测试用例，执行了${apiCaseDetail.hasExecutedCase}条，未执行${apiCaseDetail.pending}条，执行率为${apiCaseDetail.apiExecutedRate}，通过用例${apiCaseDetail.success}条，通过率为${apiCaseDetail.successRate}。共发现缺陷 ${props.detail.apiBugCount} 个。<br>`;
    const apiCaseDesc = apiCaseDetail.caseTotal ? `${apiCaseText}` : ``;

    const scenarioCaseText = `（3）本次测试包含${apiScenarioDetail.caseTotal}条场景测试用例，执行了${apiScenarioDetail.hasExecutedCase}条，未执行${apiScenarioDetail.pending}条，执行率为${apiScenarioDetail.apiExecutedRate}%，通过用例${apiScenarioDetail.success}条，通过率为${apiScenarioDetail.successRate}。共发现缺陷${props.detail.scenarioBugCount}个`;
    const scenarioCaseDesc = apiScenarioDetail.caseTotal ? `${scenarioCaseText}` : ``;

    const isPass = Number(allSuccessRate) >= Number(props.detail.passThreshold);

    const isAchieveText = getResultText(isPass, isPass ? '达到' : '未达到');
    const isMeetText = getResultText(isPass, isPass ? '满足' : '不满足');
    // 接口用例通过率
    return `<p style=""><span color="" fontsize=""> <strong>${props.detail.testPlanName}</strong> 包含功能测试、接口用例、场景用例, 共 ${allCaseTotal}条用例，已执行 ${allHasExecutedCase} 条，通过用例 ${allSuccessCase} 条，通过率为 ${allSuccessRate}%，${isAchieveText}通过阈值（通过阈值为${props.detail.passThreshold}%），<strong>${props.detail.testPlanName}</strong> 计划${isMeetText}发布要求。<br>
      ${functionCaseDesc}
      ${apiCaseDesc}
      ${scenarioCaseDesc}</span></p>
  `;
  });

  function handleSummary() {
    emit('handleSummary', summaryContent.value);
  }

  function emitDoubleClick() {
    if (!props.shareId) {
      emit('dblclick');
    }
  }
  const { handleClick } = useDoubleClick(emitDoubleClick);
</script>

<style scoped></style>
