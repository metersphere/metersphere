<template>
  <a-dropdown position="tl" trigger="hover">
    <a-button size="mini" type="outline">
      <template #icon> <icon-upload class="text-[14px] !text-[rgb(var(--primary-5))]" /> </template>
    </a-button>
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
          <a-button size="small" type="text" class="ms-add-attachment-dropdown-btn">
            <icon-upload class="mr-[8px]" />{{ t('ms.add.attachment.localUpload') }}
          </a-button>
        </template>
      </a-upload>
      <a-button size="small" type="text" class="ms-add-attachment-dropdown-btn" @click="emit('linkFile')">
        <MsIcon type="icon-icon_link-copy_outlined" class="mr-[8px]" size="16" />
        {{ t('ms.add.attachment.associateFile') }}
      </a-button>
    </template>
  </a-dropdown>
</template>

<script setup lang="ts">
  import { MsFileItem } from '@/components/pure/ms-upload/types';

  import { useI18n } from '@/hooks/useI18n';

  const emit = defineEmits<{
    (e: 'upload', file: File): void;
    (e: 'change', _fileList: MsFileItem[], fileItem: MsFileItem): void;
    (e: 'linkFile'): void;
  }>();

  const { t } = useI18n();

  const innerFileList = defineModel<MsFileItem[]>('fileList', {
    required: true,
  });

  function beforeUpload(file: File) {
    emit('upload', file);
  }

  function handleChange(_fileList: MsFileItem[], fileItem: MsFileItem) {
    emit('change', _fileList, fileItem);
  }
</script>

<style lang="less" scoped>
  .ms-add-attachment-dropdown-btn {
    padding-right: 8px;
    padding-left: 8px;
    color: var(--color-text-1) !important;
    &:hover {
      background: rgb(var(--primary-1)) !important;
    }
  }
</style>
