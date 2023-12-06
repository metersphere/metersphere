<template>
  <MsDrawer
    v-model:visible="showDrawer"
    :mask="false"
    :title="t('caseManagement.featureCase.createDefect')"
    :ok-text="t('common.confirm')"
    :ok-loading="drawerLoading"
    :width="800"
    unmount-on-close
    :show-continue="true"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item
        field="name"
        :label="t('bugManagement.bugName')"
        :rules="[{ required: true, message: t('bugManagement.edit.nameIsRequired') }]"
        :placeholder="t('bugManagement.edit.pleaseInputBugName')"
      >
        <a-input v-model="form.name" :max-length="255" show-word-limit />
      </a-form-item>
      <a-form-item :label="t('bugManagement.edit.content')">
        <MsRichText v-model="form.content" />
      </a-form-item>
      <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('bugManagement.edit.file') }}</div>
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
        <a-button type="outline">
          <template #icon>
            <icon-plus />
          </template>
          {{ t('bugManagement.edit.uploadFile') }}
        </a-button>
      </MsUpload>
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
    </a-form>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FileItem } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import FileList from '@/components/pure/ms-upload/fileList.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  const appStore = useAppStore();

  const props = defineProps<{
    visible: boolean;
  }>();

  const emit = defineEmits(['update:visible']);

  const fileList = ref<FileItem[]>([]);

  const { t } = useI18n();

  const form = ref({
    name: '',
    content: '',
    templateId: '',
    handleMan: [],
    status: '',
    severity: '',
    tag: [],
  });

  // 上传文件
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

  // 删除文件
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

  // 预览文件
  const handlePreview = (item: FileItem) => {
    const { url } = item;
    window.open(url);
  };

  const showDrawer = computed({
    get() {
      return props.visible;
    },
    set(value) {
      emit('update:visible', value);
    },
  });

  const drawerLoading = ref<boolean>(false);

  function handleDrawerConfirm() {}
  function handleDrawerCancel() {}
</script>

<style scoped></style>
