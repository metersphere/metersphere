<template>
  <a-form :model="form">
    <a-form-item field="lastExecResult" class="mb-[8px]">
      <a-radio-group v-model:model-value="form.status">
        <a-radio v-for="item in StartReviewStatus" :key="item" :value="item">
          <ReviewResult :status="item" is-part />
        </a-radio>
      </a-radio-group>
    </a-form-item>
    <a-form-item field="content" asterisk-position="end" class="mb-0">
      <div class="flex w-full items-center">
        <MsRichText
          v-model:raw="form.content"
          v-model:commentIds="form.notifiers"
          v-model:filedIds="form.reviewCommentFileIds"
          :upload-image="handleUploadImage"
          :preview-url="`${PreviewEditorImageUrl}/${appStore.currentProjectId}`"
          :auto-height="false"
          class="w-full"
          :placeholder="
            props.isDblclickPlaceholder
              ? t('testPlan.featureCase.richTextDblclickPlaceholder')
              : t('editor.placeholder')
          "
        />
      </div>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import ReviewResult from './reviewResult.vue';

  import { editorUploadFile } from '@/api/modules/case-management/featureCase';
  import { PreviewEditorImageUrl } from '@/api/requrls/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { ReviewFormParams } from '@/models/caseManagement/caseReview';
  import { StartReviewStatus } from '@/enums/caseEnum';

  const props = defineProps<{
    isDblclickPlaceholder?: boolean;
  }>();

  const form = defineModel<ReviewFormParams>('form', {
    required: true,
  });

  const { t } = useI18n();
  const appStore = useAppStore();
  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }
</script>

<style lang="less" scoped>
  :deep(.arco-form-item-label-col) {
    display: none;
  }
  :deep(.arco-form-item-wrapper-col) {
    flex: 1;
  }
</style>
