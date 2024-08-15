<template>
  <a-form ref="viewFormRef" class="rounded-[4px]" :model="form" layout="vertical">
    <a-form-item
      field="title"
      :label="t('bugManagement.bugName')"
      :rules="[{ required: props.isDisabled, message: t('bugManagement.edit.nameIsRequired') }]"
      :class="`${props.isDisabled ? '' : 'label-validate-star'}`"
      asterisk-position="end"
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
        :preview-url="previewEditorImageUrl"
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

  import { editorUploadFile } from '@/api/modules/setting/template';
  import { previewOrgImageUrl, previewProImageUrl } from '@/api/requrls/setting/template';
  import { defaultTemplateBugDetail } from '@/config/template';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { defaultBugField } from '@/models/setting/template';

  const appStore = useAppStore();

  const { t } = useI18n();

  const props = defineProps<{
    mode: 'organization' | 'project';
    isDisabled?: boolean;
  }>();

  const fileList = ref([]);

  const form = defineModel<defaultBugField>('defaultForm', {
    default: defaultTemplateBugDetail,
  });

  const uploadImgFileIds = defineModel<string[]>('uploadImgFileIds', {
    default: [],
  });

  // 富文本附件ID
  const descriptionFileIds = ref<string[]>([]);

  // 上传图片
  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile(
      {
        fileList: [file],
      },
      props.mode
    );
    return data;
  }

  const previewEditorImageUrl = computed(() =>
    props.mode === 'organization'
      ? `${previewOrgImageUrl}/${appStore.currentOrgId}`
      : `${previewProImageUrl}/${appStore.currentProjectId}`
  );

  watch(
    () => descriptionFileIds.value,
    (val) => {
      if (val) {
        uploadImgFileIds.value = val;
      }
    },
    {
      deep: true,
    }
  );
</script>

<style scoped></style>
