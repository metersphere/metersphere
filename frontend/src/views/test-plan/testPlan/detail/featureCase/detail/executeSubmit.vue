<template>
  <ExecuteForm v-model:form="form" />
  <!-- TODO: 双击富文本内容打开弹窗 -->
  <a-button
    type="primary"
    class="mt-[12px]"
    :disabled="submitDisabled"
    :loading="submitLoading"
    @click="() => submit()"
  >
    {{ t('caseManagement.caseReview.commitResult') }}
  </a-button>
  <a-modal
    v-model:visible="modalVisible"
    :title="t('testPlan.featureCase.startExecution')"
    class="p-[4px]"
    title-align="start"
    body-class="p-0"
    :width="800"
    :cancel-button-props="{ disabled: submitLoading }"
    :ok-button-props="{ disabled: submitDisabled }"
    :ok-loading="submitLoading"
    :ok-text="t('caseManagement.caseReview.commitResult')"
    @before-ok="submit"
  >
    <ExecuteForm v-model:form="form" />
  </a-modal>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import ExecuteForm from '@/views/test-plan/testPlan/detail/featureCase/components/executeForm.vue';

  import { runFeatureCase } from '@/api/modules/test-plan/testPlan';
  import { defaultExecuteForm } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { StepExecutionResult } from '@/models/caseManagement/featureCase';
  import type { ExecuteFeatureCaseFormParams } from '@/models/testPlan/testPlan';
  import { LastExecuteResults } from '@/enums/caseEnum';

  const props = defineProps<{
    caseId: string;
    testPlanId: string;
    id: string;
    stepExecutionResult?: StepExecutionResult[];
  }>();

  const emit = defineEmits<{
    (e: 'done'): void;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();

  const form = ref<ExecuteFeatureCaseFormParams>({ ...defaultExecuteForm });

  const modalVisible = ref(false);
  const submitLoading = ref(false);
  const submitDisabled = computed(
    () =>
      form.value.lastExecResult !== 'PASSED' &&
      (form.value.content === '' || form.value.content?.trim() === '<p style=""></p>')
  );

  watch(
    () => props.stepExecutionResult,
    () => {
      const executionResultList = props.stepExecutionResult?.map((item) => item.executeResult);
      if (executionResultList?.includes(LastExecuteResults.FAILED)) {
        form.value.lastExecResult = LastExecuteResults.FAILED;
      } else if (executionResultList?.includes(LastExecuteResults.BLOCKED)) {
        form.value.lastExecResult = LastExecuteResults.BLOCKED;
      } else {
        form.value.lastExecResult = LastExecuteResults.PASSED;
      }
    },
    { deep: true }
  );

  // 提交执行
  async function submit() {
    try {
      submitLoading.value = true;
      const params = {
        projectId: appStore.currentProjectId,
        caseId: props.caseId,
        testPlanId: props.testPlanId,
        id: props.id,
        lastExecResult: form.value.lastExecResult,
        stepsExecResult: JSON.stringify(props.stepExecutionResult) ?? '',
        content: form.value.content,
        notifier: form.value?.commentIds?.join(';'),
      };
      await runFeatureCase(params);
      modalVisible.value = false;
      Message.success(t('common.updateSuccess'));
      form.value = { ...defaultExecuteForm };
      emit('done');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      submitLoading.value = false;
    }
  }
</script>
