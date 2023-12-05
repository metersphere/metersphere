<template>
  <div class="p-[16px]">
    <div class="header">
      <div class="header-title">{{ t('bugManagement.edit.content') }}</div>
      <div class="header-action">
        <a-button>
          <template #icon> <MsIconfont type="icon-icon_edit_outlined" /> </template>
          {{ t('bugManagement.edit.contentEdit') }}
        </a-button>
      </div>
    </div>
    <div class="header">
      <div class="header-title">{{ t('bugManagement.edit.content') }}</div>
    </div>
    <div class="mt-[8]" :class="{ 'max-h-[260px]': contentEditAble }">
      <MsRichText
        v-if="form.content"
        v-model:model-value="form.content"
        :disabled="!contentEditAble"
        :placeholder="t('bugManagement.edit.contentPlaceholder')"
      />
      <div v-else>-</div>
    </div>
  </div>
  <a-dropdown trigger="hover">
    <template #content>
      <MsUpload
        v-model:file-list="fileList"
        :auto-upload="false"
        multiple
        draggable
        accept="unknown"
        is-limit
        size-unit="MB"
        :max-size="500"
      >
        <a-doption>{{ t('bugManagement.edit.localUpload') }}</a-doption>
      </MsUpload>
      <a-doption @click="handleLineFile">{{ t('bugManagement.edit.linkFile') }}</a-doption>
    </template>
    <a-button type="outline">
      <template #icon>
        <icon-plus />
      </template>
      {{ t('bugManagement.edit.uploadFile') }}
    </a-button>
  </a-dropdown>
  <div class="mb-[8px] mt-[2px] text-[var(--color-text-4)]">{{ t('bugManagement.edit.fileExtra') }}</div>
  <FileList
    :show-tab="false"
    :file-list="fileList"
    :upload-func="uploadFile"
    @delete-file="deleteFile"
    @reupload="reupload"
    @handle-preview="handlePreview"
  >
  </FileList>
</template>

<script setup lang="ts">
  import { FileItem } from '@arco-design/web-vue';

  import MsIconfont from '@/components/pure/ms-icon-font/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import FileList from '@/components/pure/ms-upload/fileList.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const contentEditAble = ref(false);
  const fileList = ref<FileItem[]>([]);
  const form = ref({
    content: '',
    fileList: [],
  });
  const uploadFile = (file: File) => {
    const fileItem: FileItem = {
      uid: `${Date.now()}`,
      name: file.name,
      status: 'init',
      file,
    };
    fileList.value.push(fileItem);
    return Promise.resolve(fileItem);
  };
  const handlePreview = (item: FileItem) => {
    const { url } = item;
    window.open(url);
  };

  const deleteFile = (item: FileItem) => {
    fileList.value = fileList.value.filter((e) => e.uid !== item.uid);
  };

  const reupload = (item: FileItem) => {
    fileList.value = fileList.value.map((e) => {
      if (e.uid === item.uid) {
        return {
          ...e,
          status: 'init',
        };
      }
      return e;
    });
  };

  const handleLineFile = () => {};
</script>

<style lang="less" scoped>
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    &-title {
      color: var(--color-text-1);
    }
    &-action {
      color: rgb(var(--primary-7));
    }
  }
</style>
