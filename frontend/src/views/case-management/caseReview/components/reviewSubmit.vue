<template>
  <ReviewForm v-model:form="form" is-dblclick-placeholder class="execute-form" />
  <div v-show="props.reviewPassRule === 'MULTIPLE'" class="mt-[4px] text-[12px] text-[var(--color-text-4)]">{{
    t('caseManagement.caseReview.reviewFormTip')
  }}</div>
  <a-button
    type="primary"
    class="mt-[12px]"
    :loading="submitLoading"
    :disabled="submitDisabled"
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
    :ok-loading="submitLoading"
    :ok-text="t('caseManagement.caseReview.commitResult')"
    :ok-button-props="{ disabled: submitDisabled }"
    @before-ok="submit"
  >
    <ReviewForm v-model:form="form" />
  </a-modal>
</template>

<script setup lang="ts">
  import { nextTick, onMounted, ref, watch } from 'vue';
  import { useEventListener } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import type { MinderJsonNode } from '@/components/pure/ms-minder-editor/props';
  import { getMinderOperationParams } from '@/components/business/ms-minders/caseReviewMinder/utils';
  import ReviewForm from './reviewFormRichText.vue';

  import { batchReview } from '@/api/modules/case-management/caseReview';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { BatchReviewCaseParams, ReviewFormParams, ReviewPassRule } from '@/models/caseManagement/caseReview';
  import { StartReviewStatus } from '@/enums/caseEnum';

  const props = defineProps<{
    selectNode: MinderJsonNode;
    reviewPassRule: ReviewPassRule;
    reviewId: string;
    userId: string;
  }>();

  const emit = defineEmits<{
    (e: 'done'): void;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();

  const defaultForm: ReviewFormParams = {
    status: StartReviewStatus.PASS,
    content: '',
    reviewCommentFileIds: [] as string[],
    notifiers: [] as string[],
  };

  const form = ref({ ...defaultForm });

  const modalVisible = ref(false);
  const submitLoading = ref(false);
  const submitDisabled = computed(
    () =>
      form.value.status !== StartReviewStatus.PASS &&
      (form.value.content === '' || form.value.content.trim() === '<p style=""></p>')
  );

  // 双击富文本内容打开弹窗
  onMounted(() => {
    nextTick(() => {
      const editorContent = document.querySelector('.execute-form')?.querySelector('.editor-content');
      useEventListener(editorContent, 'dblclick', () => {
        modalVisible.value = true;
      });
    });
  });

  watch(
    () => props.selectNode.data?.caseId,
    () => {
      form.value = { ...defaultForm };
    }
  );

  // 提交执行
  async function submit() {
    try {
      submitLoading.value = true;
      const params: BatchReviewCaseParams = {
        projectId: appStore.currentProjectId,
        userId: props.userId,
        reviewId: props.reviewId,
        reviewPassRule: props.reviewPassRule,
        ...form.value,
        notifier: form.value.notifiers?.join(';') ?? '',
        ...getMinderOperationParams(props.selectNode),
      };
      await batchReview(params);
      modalVisible.value = false;
      Message.success(t('caseManagement.caseReview.reviewSuccess'));
      emit('done');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      submitLoading.value = false;
    }
  }
</script>

<style lang="less" scoped>
  .execute-form :deep(.rich-wrapper) .halo-rich-text-editor .editor-content {
    max-height: 54px !important;
    .ProseMirror {
      min-height: 38px;
    }
  }
</style>
