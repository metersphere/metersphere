<template>
  <a-dropdown v-model:popup-visible="dropdownVisible" :disabled="props.disabled" position="tl" trigger="click">
    <a-button :disabled="props.disabled" size="mini" type="outline">
      <template #icon>
        <icon-upload class="!hover:text-[rgb(var(--primary-5))] text-[14px] !text-[rgb(var(--primary-5))]" />
      </template>
    </a-button>
    <template #content>
      <MsUpload
        v-model:file-list="innerFileList"
        :accept="props.accept || 'none'"
        :auto-upload="false"
        :show-file-list="false"
        :limit="50"
        size-unit="MB"
        :multiple="false"
        class="w-full"
        @change="handleChange"
      >
        <a-button size="small" type="text" class="ms-add-attachment-dropdown-btn">
          <icon-upload class="mr-[8px]" />{{ t('ms.add.attachment.localUpload') }}
        </a-button>
      </MsUpload>
      <a-button
        v-permission="['PROJECT_FILE_MANAGEMENT:READ']"
        size="small"
        type="text"
        class="ms-add-attachment-dropdown-btn"
        @click="emit('linkFile')"
      >
        <MsIcon type="icon-icon_link-copy_outlined" class="mr-[8px]" size="16" />
        {{ t('ms.add.attachment.associateFile') }}
      </a-button>
    </template>
  </a-dropdown>
</template>

<script setup lang="ts">
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import { MsFileItem, UploadType } from '@/components/pure/ms-upload/types';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    disabled?: boolean;
    accept?: UploadType;
  }>();

  const emit = defineEmits<{
    (e: 'upload', file: File): void;
    (e: 'change', _fileList: MsFileItem[], fileItem: MsFileItem): void;
    (e: 'linkFile'): void;
  }>();

  const { t } = useI18n();

  const innerFileList = defineModel<MsFileItem[]>('fileList', {
    default: () => [],
  });

  const dropdownVisible = ref(false);

  function handleChange(_fileList: MsFileItem[], fileItem: MsFileItem) {
    fileItem.local = true;
    emit('change', _fileList, fileItem);
    nextTick(() => {
      // emit 完文件之后再关闭菜单
      dropdownVisible.value = false;
    });
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
