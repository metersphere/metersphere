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

  const { t } = useI18n();
  const props = defineProps<{
    richText: { summary: string; richTextTmpFileIds?: string[] };
    shareId?: string;
    showButton: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'updateSummary'): void;
    (e: 'cancel'): void;
    (e: 'handleSummary'): void;
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

  function handleSummary() {
    emit('handleSummary');
  }
</script>

<style scoped></style>
