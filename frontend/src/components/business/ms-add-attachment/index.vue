<template>
  <a-form-item field="attachment" :label="t('caseManagement.featureCase.addAttachment')">
    <div class="flex flex-col">
      <div class="mb-1">
        <a-dropdown position="tr" trigger="hover">
          <a-button type="outline">
            <template #icon> <icon-plus class="text-[14px]" /> </template
            >{{ t('system.orgTemplate.addAttachment') }}</a-button
          >
          <template #content>
            <a-upload
              ref="uploadRef"
              v-model:file-list="innerFileList"
              :auto-upload="false"
              :show-file-list="false"
              :before-upload="beforeUpload"
              @change="handleChange"
            >
              <template #upload-button>
                <a-button type="text" class="!text-[var(--color-text-1)]">
                  <icon-upload />{{ t('caseManagement.featureCase.uploadFile') }}</a-button
                >
              </template>
            </a-upload>
            <a-button type="text" class="!text-[var(--color-text-1)]" @click="associatedFile">
              <MsIcon type="icon-icon_link-copy_outlined" size="16" />{{
                t('caseManagement.featureCase.associatedFile')
              }}</a-button
            >
          </template>
        </a-dropdown>
      </div>
      <div class="!hover:bg-[rgb(var(--primary-1))] !text-[var(--color-text-4)]">{{
        t('system.orgTemplate.addAttachmentTip')
      }}</div>
    </div>
  </a-form-item>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import type { MsFileItem } from '@/components/pure/ms-upload/types';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{
    fileList: MsFileItem[];
  }>();

  const emit = defineEmits<{
    (e: 'upload', file: File): void;
    (e: 'change', _fileList: MsFileItem[], fileItem: MsFileItem): void;
    (e: 'linkFile'): void;
    (e: 'update:fileList', fileList: MsFileItem[]): void;
  }>();

  // const innerFileList = ref<MsFileItem[]>([]);
  const innerFileList = useVModel(props, 'fileList', emit);

  function beforeUpload(file: File) {
    emit('upload', file);
  }

  function handleChange(_fileList: MsFileItem[], fileItem: MsFileItem) {
    innerFileList.value = _fileList;
    emit('change', _fileList, fileItem);
  }

  function associatedFile() {
    emit('linkFile');
  }
</script>

<style scoped></style>
