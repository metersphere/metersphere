<template>
  <div
    :class="`${
      hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+UPDATE']) && !shareId ? '' : 'cursor-not-allowed'
    } w-full`"
  >
    <MsRichText
      ref="msRichTextRef"
      v-model:raw="innerSummary.content"
      v-model:filedIds="innerSummary.richTextTmpFileIds"
      :upload-image="handleUploadImage"
      :preview-url="`${ReportPlanPreviewImageUrl}/${appStore.currentProjectId}`"
      class="mt-[8px]"
      :editable="props.canEdit"
      :auto-focus="autoFocus"
      @click="handleRichClick"
      @update="emit('handleSetSave')"
    />
    <MsFormItemSub
      v-if="hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+UPDATE']) && !shareId && props.canEdit && props.isPreview"
      :text="t('report.detail.oneClickSummary')"
      :show-fill-icon="true"
      @fill="handleSummary"
    />
  </div>

  <div
    v-show="hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+UPDATE']) && !shareId && props.canEdit"
    class="mt-[16px] flex items-center gap-[12px]"
  >
    <a-button type="primary" @click="handleUpdateReportDetail">{{ t('common.save') }}</a-button>
    <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
  </div>
</template>

<script setup lang="ts">
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsFormItemSub from '@/components/business/ms-form-item-sub/index.vue';

  import { editorUploadFile } from '@/api/modules/test-plan/report';
  import { ReportPlanPreviewImageUrl } from '@/api/requrls/test-plan/report';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { hasAnyPermission } from '@/utils/permission';

  import type { customValueForm, PlanReportDetail } from '@/models/testPlan/testPlanReport';

  import { getSummaryDetail } from '@/views/test-plan/report/utils';

  const { t } = useI18n();
  const appStore = useAppStore();

  const props = defineProps<{
    richText: customValueForm;
    shareId?: string;
    isPlanGroup: boolean;
    detail: PlanReportDetail;
    canEdit: boolean;
    isPreview?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'updateSummary', form: customValueForm): void;
    (e: 'cancel'): void;
    (e: 'handleClick'): void;
    (e: 'handleSetSave'): void;
    (e: 'handleSummary', content: string): void;
  }>();

  const innerSummary = ref(props.richText);
  const autoFocus = ref<boolean>(false);
  function handleCancel() {
    autoFocus.value = false;
    emit('cancel');
  }

  function handleUpdateReportDetail() {
    autoFocus.value = false;
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
      let summaryDesc = `<p style=""><strong>${props.detail.testPlanName} </strong><span style="color: rgb(24, 43, 80); font-size: 14px">包含 ${props.detail.planCount} 个子计划。 其中 ${props.detail.passCountOfPlan} 个子计划通过， ${props.detail.failCountOfPlan} 个子计划不通过；
      包含功能测试、接口用例、场景用例, 共 ${props.detail.caseTotal} 条用例，已执行 ${allHasExecutedCase} 条，通过用例 ${allSuccessCase} 条，通过率为 ${allSuccessRate} %；共关联缺陷 ${props.detail.bugCount} 个</span></p>`;
      // 计算子级别
      (props.detail?.children || []).forEach((item) => {
        const funChildrenCaseDetail = getSummaryDetail(item.functionalCount);
        const apiChildrenCaseDetail = getSummaryDetail(item.apiCaseCount);
        const apiChildrenScenarioDetail = getSummaryDetail(item.apiScenarioCount);
        const executedCase =
          funChildrenCaseDetail.hasExecutedCase +
          apiChildrenCaseDetail.hasExecutedCase +
          apiChildrenScenarioDetail.hasExecutedCase;
        const allPassCount =
          funChildrenCaseDetail.success + apiChildrenCaseDetail.success + apiChildrenScenarioDetail.success;
        const content = `<p style=""><span style="color: rgb(24, 43, 80); font-size: 14px"> ▪ ${item.testPlanName}子计划，包含功能测试、接口用例、场景用例, 共 ${item.caseTotal} 条用例，已执行 ${executedCase} 条，通过用例 ${allPassCount} 条，通过率为 ${item.passRate} %，</span><strong><span style="color: rgb(255, 59, 48)" color="rgb(255, 59, 48)" fontsize="">未达到</span></strong><span style="color: rgb(24, 43, 80); font-size: 14px">通过阈值（通过阈值为${item.passThreshold}%）</span></p>`;
        summaryDesc += content;
      });
      return summaryDesc;
    }
    const functionalCasText = `▪ 本次测试包含${functionalCaseDetail.caseTotal}条功能测试用例，执行了${functionalCaseDetail.hasExecutedCase}条，未执行${functionalCaseDetail.pending}条，执行率为${functionalCaseDetail.apiExecutedRate}，通过用例${functionalCaseDetail.success}条，通过率为${functionalCaseDetail.successRate}。共关联缺陷${props.detail.functionalBugCount}个。<br>`;
    const functionCaseDesc = functionalCaseDetail.caseTotal ? `${functionalCasText}` : ``;

    const apiCaseText = `▪ 本次测试包含${apiCaseDetail.caseTotal}条接口测试用例，执行了${apiCaseDetail.hasExecutedCase}条，未执行${apiCaseDetail.pending}条，执行率为${apiCaseDetail.apiExecutedRate}，通过用例${apiCaseDetail.success}条，通过率为${apiCaseDetail.successRate}。共关联缺陷 ${props.detail.apiBugCount} 个。<br>`;
    const apiCaseDesc = apiCaseDetail.caseTotal ? `${apiCaseText}` : ``;

    const scenarioCaseText = `▪ 本次测试包含${apiScenarioDetail.caseTotal}条场景测试用例，执行了${apiScenarioDetail.hasExecutedCase}条，未执行${apiScenarioDetail.pending}条，执行率为${apiScenarioDetail.apiExecutedRate}，通过用例${apiScenarioDetail.success}条，通过率为${apiScenarioDetail.successRate}。共关联缺陷 ${props.detail.scenarioBugCount}个`;
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

  const msRichTextRef = ref<InstanceType<typeof MsRichText>>();

  function handleRichClick() {
    if (!props.shareId) {
      autoFocus.value = true;
      emit('handleClick');
    }
  }

  watch(
    () => props.richText,
    (val) => {
      if (val) {
        innerSummary.value = { ...val };
      }
    },
    {
      deep: true,
    }
  );
</script>

<style scoped></style>
