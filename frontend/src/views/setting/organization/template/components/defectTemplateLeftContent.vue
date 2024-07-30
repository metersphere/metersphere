<template>
  <a-form ref="viewFormRef" class="rounded-[4px]" :model="form" layout="vertical">
    <a-form-item
      field="title"
      :label="t('bugManagement.bugName')"
      :rules="[{ required: true, message: t('bugManagement.edit.nameIsRequired') }]"
    >
      <a-input
        v-model="form.title"
        :placeholder="t('bugManagement.edit.pleaseInputBugName')"
        :disabled="props.isDisabled"
        :max-length="255"
      />
    </a-form-item>
    <a-form-item field="description" :label="t('bugManagement.edit.content')">
      <MsRichText
        v-model:raw="form.description"
        v-model:filed-ids="descriptionFileIds"
        :upload-image="handleUploadImage"
        :preview-url="EditorPreviewFileUrl"
        :editable="!props.isDisabled"
      />
    </a-form-item>
    <AddAttachment v-model:file-list="fileList" disabled multiple />
  </a-form>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织-模板管理-缺陷模板左侧内容
   */
  import { ref } from 'vue';

  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import AddAttachment from '@/components/business/ms-add-attachment/index.vue';

  import { editorUploadFile } from '@/api/modules/bug-management';
  import { EditorPreviewFileUrl } from '@/api/requrls/bug-management';
  import { defaultTemplateBugDetail } from '@/config/template';
  import { useI18n } from '@/hooks/useI18n';

  import type { defaultBugField } from '@/models/setting/template';

  const { t } = useI18n();

  const props = defineProps<{
    isDisabled?: boolean;
  }>();

  const fileList = ref([]);

  const form = defineModel<defaultBugField>('defaultForm', {
    default: defaultTemplateBugDetail,
  });

  // 富文本附件ID
  const descriptionFileIds = ref<string[]>([]);

  // TODO 上传图片需要接口
  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }
</script>

<style scoped></style>
