<template>
  <a-form ref="dialogFormRef" :model="caseResultForm" layout="vertical">
    <a-form-item field="reason" class="mb-[4px]">
      <a-radio-group
        v-model:model-value="caseResultForm.result"
        :disabled="!hasAnyPermission(['CASE_REVIEW:READ+REVIEW'])"
        @change="() => dialogFormRef?.resetFields()"
      >
        <a-radio value="PASS">
          <div class="inline-flex items-center">
            <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--success-6))]" />
            {{ t('caseManagement.caseReview.pass') }}
          </div>
        </a-radio>
        <a-radio value="UN_PASS" @click="modalVisible = true">
          <div class="inline-flex items-center">
            <MsIcon type="icon-icon_close_filled" class="mr-[4px] text-[rgb(var(--danger-6))]" />
            {{ t('caseManagement.caseReview.fail') }}
          </div>
        </a-radio>
        <a-radio value="UNDER_REVIEWED" @click="modalVisible = true">
          <div class="inline-flex items-center">
            <MsIcon type="icon-icon_warning_filled" class="mr-[4px] text-[rgb(var(--warning-6))]" />
            {{ t('caseManagement.caseReview.suggestion') }}
          </div>
          <a-tooltip :content="t('caseManagement.caseReview.suggestionTip')" position="right">
            <icon-question-circle
              class="ml-[4px] mt-[2px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
        </a-radio>
      </a-radio-group>
    </a-form-item>
    <div class="flex w-full items-center">
      <a-button
        v-permission="['CASE_REVIEW:READ+REVIEW']"
        type="secondary"
        class="p-[8px_6px]"
        size="small"
        @click="modalVisible = true"
      >
        <icon-plus class="mr-[4px]" />
        {{ t('caseManagement.caseReview.reason') }}
      </a-button>
    </div>
  </a-form>
  <a-button
    v-permission="['CASE_REVIEW:READ+REVIEW']"
    type="primary"
    class="mt-[12px]"
    :disabled="submitDisabled"
    :loading="submitReviewLoading"
    @click="() => submitReview()"
  >
    {{ t('caseManagement.caseReview.submitReview') }}
  </a-button>
  <a-modal
    v-model:visible="modalVisible"
    :title="t('caseManagement.caseReview.reason')"
    class="p-[4px]"
    title-align="start"
    body-class="p-0"
    :width="680"
    :cancel-button-props="{ disabled: submitReviewLoading }"
    :ok-button-props="{ disabled: submitDisabled }"
    :ok-loading="submitReviewLoading"
    :ok-text="t('caseManagement.caseReview.submitReview')"
    @before-ok="submitReview"
  >
    <MsRichText
      v-model:raw="caseResultForm.reason"
      v-model:commentIds="caseResultForm.commentIds"
      v-model:filed-ids="caseResultForm.fileList"
      :auto-height="false"
      :upload-image="handleUploadImage"
      :preview-url="`${PreviewEditorImageUrl}/${appStore.currentProjectId}`"
      class="w-full"
    />
  </a-modal>
</template>

<script setup lang="ts">
  import { onMounted } from 'vue';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';

  import { getCaseReviewerList, saveCaseReviewResult } from '@/api/modules/case-management/caseReview';
  import { editorUploadFile } from '@/api/modules/case-management/featureCase';
  import { PreviewEditorImageUrl } from '@/api/requrls/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import { useUserStore } from '@/store';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  import { CaseReviewFunctionalCaseUserItem, ReviewPassRule } from '@/models/caseManagement/caseReview';
  import { StartReviewStatus } from '@/enums/caseEnum';

  const props = defineProps<{
    reviewId: string;
    caseId: string;
    reviewPassRule: ReviewPassRule;
  }>();
  const emit = defineEmits(['done']);

  const appStore = useAppStore();

  const userStore = useUserStore();

  const { t } = useI18n();

  const dialogFormRef = ref<FormInstance>();
  const caseReviewerList = ref<CaseReviewFunctionalCaseUserItem[]>([]);
  const caseResultForm = ref({
    result: StartReviewStatus.PASS,
    reason: '',
    fileList: [] as string[],
    commentIds: [] as string[],
  });
  const submitReviewLoading = ref(false);
  const submitDisabled = computed(
    () =>
      caseResultForm.value.result !== StartReviewStatus.PASS &&
      (caseResultForm.value.reason === '' || caseResultForm.value.reason.trim() === '<p style=""></p>')
  );
  const modalVisible = ref(false);

  const singleAdmin = ref(false);

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }

  onMounted(async () => {
    caseReviewerList.value = await getCaseReviewerList(props.reviewId, props.caseId);
  });

  // 提交评审
  function submitReview(done?: (close: boolean) => void) {
    dialogFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          submitReviewLoading.value = true;
          const params = {
            projectId: appStore.currentProjectId,
            caseId: props.caseId,
            reviewId: props.reviewId,
            status: caseResultForm.value.result,
            reviewPassRule: props.reviewPassRule,
            content: caseResultForm.value.reason,
            notifier: caseResultForm.value.commentIds.join(';'),
            reviewCommentFileIds: caseResultForm.value.fileList,
          };
          await saveCaseReviewResult(params);
          modalVisible.value = false;
          caseReviewerList.value.forEach((child) => {
            if (child.userId === userStore.id) {
              singleAdmin.value = true;
            }
          });
          if (userStore.isAdmin && !singleAdmin.value) {
            Message.warning(t('caseManagement.caseReview.reviewSuccess.widthAdmin'));
          } else {
            Message.success(t('caseManagement.caseReview.reviewSuccess'));
          }

          caseResultForm.value = {
            result: StartReviewStatus.PASS,
            reason: '',
            fileList: [] as string[],
            commentIds: [] as string[],
          };
          if (typeof done === 'function') {
            done(true);
          }
          emit('done', params.status);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
          if (typeof done === 'function') {
            done(false);
          }
        } finally {
          submitReviewLoading.value = false;
        }
      }
    });
  }
</script>

<style lang="less" scoped>
  :deep(.arco-form-item-label-col) {
    margin-bottom: 0;
  }
  .arco-radio {
    @apply pl-0;
  }
</style>
