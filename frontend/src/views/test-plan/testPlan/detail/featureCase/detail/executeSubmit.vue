<template>
  <ExecuteForm
    v-model:achieved="achievedForm"
    v-model:form="form"
    is-dblclick-placeholder
    class="execute-form"
    @dblclick="dblOrClickHandler"
    @click="dblOrClickHandler"
  >
    <template #headerRight>
      <slot name="headerRight"></slot>
    </template>
  </ExecuteForm>
  <div class="mt-[12px] flex items-center">
    <a-button type="primary" :loading="submitLoading" @click="() => submit()">
      {{ t('caseManagement.caseReview.commitResult') }}
    </a-button>
    <slot name="commitRight"></slot>
  </div>
  <a-modal
    v-model:visible="modalVisible"
    :title="t('testPlan.featureCase.startExecution')"
    class="p-[4px]"
    title-align="start"
    body-class="p-0"
    :width="800"
    :cancel-button-props="{ disabled: submitLoading }"
    :ok-loading="submitLoading"
    :ok-text="t('caseManagement.caseReview.commitResult')"
    @before-ok="submit"
    @cancel="cancel"
  >
    <ExecuteForm v-model:form="dialogForm" />
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useEventListener } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import type { MinderJsonNode } from '@/components/pure/ms-minder-editor/props';
  import { getMinderOperationParams } from '@/components/business/ms-minders/caseReviewMinder/utils';
  import ExecuteForm from '@/views/test-plan/testPlan/detail/featureCase/components/executeForm.vue';

  import { batchExecuteCase, runFeatureCase } from '@/api/modules/test-plan/testPlan';
  import { defaultExecuteForm } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { StepExecutionResult } from '@/models/caseManagement/featureCase';
  import type { BatchExecuteFeatureCaseParams, ExecuteFeatureCaseFormParams } from '@/models/testPlan/testPlan';
  import { LastExecuteResults } from '@/enums/caseEnum';

  const props = defineProps<{
    caseId?: string;
    testPlanId: string;
    id?: string;
    selectNode?: MinderJsonNode;
    stepExecutionResult?: StepExecutionResult[];
    isDefaultActivate?: boolean; // 是否默认激活状态
    treeType?: 'MODULE' | 'COLLECTION';
  }>();

  const emit = defineEmits<{
    (e: 'done', status: LastExecuteResults, content: string): void;
    (e: 'dblclick'): void;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();

  const form = ref<ExecuteFeatureCaseFormParams>({ ...defaultExecuteForm });
  const dialogForm = ref<ExecuteFeatureCaseFormParams>({ ...defaultExecuteForm });

  const modalVisible = ref(false);
  const submitLoading = ref(false);
  // 双击富文本内容或者双击描述文本框打开弹窗
  const achievedForm = ref<boolean>(props.isDefaultActivate);

  const clickTimeout = ref<ReturnType<typeof setTimeout> | null>();

  // 双击处理逻辑
  function handleDblClick() {
    if (clickTimeout.value) {
      clearTimeout(clickTimeout.value); // 清除单击的处理
      clickTimeout.value = null;
    }
    modalVisible.value = true;
    dialogForm.value = cloneDeep(form.value); // 克隆表单数据到弹窗表单
  }

  // 单击处理逻辑
  function handleClick() {
    if (clickTimeout.value) {
      clearTimeout(clickTimeout.value);
    }
    clickTimeout.value = setTimeout(() => {
      if (!achievedForm.value) {
        achievedForm.value = true; // 切换到富文本框
      }
      clickTimeout.value = null;
    }, 200);
  }

  function dblOrClickHandler() {
    nextTick(() => {
      const editorContent = document.querySelector('.execute-form')?.querySelector('.editor-content');
      const textareaContent = document.querySelector('.execute-form')?.querySelector('.textarea-input');

      const bindEvents = (element: Element | null) => {
        if (element) {
          useEventListener(element, 'dblclick', handleDblClick);
          useEventListener(element, 'click', handleClick);
        }
      };

      if (editorContent) {
        bindEvents(editorContent);
      }
      if (textareaContent) {
        bindEvents(textareaContent);
      }
    });
  }

  watch(
    () => props.stepExecutionResult,
    () => {
      const executionResultList = props.stepExecutionResult?.map((item) => item.executeResult);
      if (executionResultList?.includes(LastExecuteResults.ERROR)) {
        form.value.lastExecResult = LastExecuteResults.ERROR;
      } else if (executionResultList?.includes(LastExecuteResults.BLOCKED)) {
        form.value.lastExecResult = LastExecuteResults.BLOCKED;
      } else {
        form.value.lastExecResult = LastExecuteResults.SUCCESS;
      }
    },
    { deep: true }
  );

  watch(
    () => props.id,
    () => {
      form.value = { ...defaultExecuteForm };
    }
  );

  function cancel(e: Event) {
    // 点击取消/关闭，弹窗关闭，富文本内容都清空；点击空白处，弹窗关闭，将弹窗内容填入下面富文本内容里
    if (!(e.target as any)?.classList.contains('arco-modal-wrapper')) {
      dialogForm.value = { ...defaultExecuteForm };
    }
    form.value = cloneDeep(dialogForm.value);
    achievedForm.value = false;
  }

  // 提交执行
  async function submit() {
    try {
      submitLoading.value = true;
      const params = {
        projectId: appStore.currentProjectId,
        testPlanId: props.testPlanId,
        ...(modalVisible.value ? dialogForm.value : form.value),
        stepsExecResult: JSON.stringify(props.stepExecutionResult),
        notifier: (modalVisible.value ? dialogForm.value : form.value)?.commentIds?.join(';'),
      };
      // 脑图执行是批量执行
      if (props.selectNode) {
        await batchExecuteCase({
          ...params,
          ...getMinderOperationParams(props.selectNode, props.treeType === 'COLLECTION'),
        } as BatchExecuteFeatureCaseParams);
      } else {
        await runFeatureCase({
          ...params,
          caseId: props.caseId ?? '',
          id: props.id ?? '',
        });
      }
      modalVisible.value = false;
      Message.success(t('common.updateSuccess'));
      form.value = { ...defaultExecuteForm };
      achievedForm.value = false;
      emit('done', params.lastExecResult, params.content ?? '');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      submitLoading.value = false;
    }
  }
</script>

<style lang="less" scoped>
  .execute-form :deep(.rich-wrapper) .halo-rich-text-editor .ProseMirror {
    height: 58px;
  }
</style>
