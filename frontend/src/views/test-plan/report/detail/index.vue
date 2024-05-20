<template>
  <MsCard class="mb-[16px]" hide-back hide-footer auto-height no-content-padding hide-divider> </MsCard>
  <MsCard class="mb-[16px]" simple auto-height>
    <div class="font-medium">{{ t('report.detail.reportSummary') }}</div>
    <MsRichText
      v-model:raw="richText.summary"
      v-model:filedIds="richText.richTextTmpFileIds"
      :upload-image="handleUploadImage"
      :preview-url="PreviewEditorImageUrl"
      class="mt-[8px] w-full"
    />
    <div class="mt-[16px] flex items-center gap-[12px]">
      <a-button type="primary" @click="handleUpdateReportDetail">{{ t('common.save') }}</a-button>
      <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
    </div>
  </MsCard>
  <MsCard simple auto-height>
    <MsTab
      v-model:active-key="activeTab"
      :show-badge="false"
      :content-tab-list="contentTabList"
      no-content
      class="relative mb-[16px] border-b"
    />
    <BugTable v-if="activeTab === 'bug'" :report-id="reportId" />
    <FeatureCaseTable v-if="activeTab === 'featureCase'" :report-id="reportId" />
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import BugTable from './component/bugTable.vue';
  import FeatureCaseTable from './component/featureCaseTable.vue';

  import { editorUploadFile } from '@/api/modules/case-management/featureCase';
  import { PreviewEditorImageUrl } from '@/api/requrls/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const route = useRoute();

  const reportId = ref(route.query.id as string);
  const activeTab = ref('bug');
  const contentTabList = ref([
    {
      value: 'bug',
      label: t('report.detail.bugDetails'),
    },
    {
      value: 'featureCase',
      label: t('report.detail.featureCaseDetails'),
    },
  ]);

  // TODO 暂时
  const richText = ref({
    summary: '',
    richTextTmpFileIds: [],
  });
  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }
  function handleUpdateReportDetail() {
    // TODO: 更新
  }
  function handleCancel() {
    // TODO: 取消 数据还原
  }
</script>

<style scoped></style>
