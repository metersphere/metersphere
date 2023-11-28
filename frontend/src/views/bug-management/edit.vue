<template>
  <MsCard :special-height="-54" no-content-padding divider-has-p-x has-breadcrumb :title="title">
    <template #headerRight>
      <a-select
        v-model="templateId"
        class="w-[240px]"
        :options="templateOption"
        allow-search
        :placeholder="t('bugManagement.edit.defaultSystemTemplate')"
      />
    </template>
    <a-form ref="formRef" :model="form" layout="vertical">
      <div class="flex flex-row" style="height: calc(100vh - 224px)">
        <div class="left mt-[16px] min-w-[732px] grow pl-[24px]">
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
        </div>
        <a-divider class="ml-[16px]" direction="vertical" />
        <div class="right mt-[16px] grow pr-[24px]">
          <a-form-item
            :label="t('bugManagement.handleMan')"
            field="handleMan"
            :rules="[{ required: true, message: t('bugManagement.edit.handleManIsRequired') }]"
          >
            <MsUserSelector
              v-model:model-value="form.handleMan"
              placeholder="bugManagement.edit.handleManPlaceholder"
            />
          </a-form-item>
          <a-form-item
            field="status"
            :label="t('bugManagement.status')"
            :rules="[{ required: true, message: t('bugManagement.edit.statusIsRequired') }]"
          >
            <a-select
              v-model:model-value="form.status"
              :placeholder="t('bugManagement.edit.statusPlaceholder')"
            ></a-select>
          </a-form-item>
          <a-form-item field="severity" :label="t('bugManagement.severity')">
            <a-select
              v-model:model-value="form.severity"
              :placeholder="t('bugManagement.edit.severityPlaceholder')"
            ></a-select>
          </a-form-item>
          <a-form-item field="tag" :label="t('bugManagement.tag')">
            <a-input-tag
              v-model:model-value="form.tag"
              :placeholder="t('bugManagement.edit.tagPlaceholder')"
              allow-clear
            />
          </a-form-item>
        </div>
      </div>
    </a-form>
  </MsCard>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import { FileItem } from '@arco-design/web-vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import FileList from '@/components/pure/ms-upload/fileList.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import { MsUserSelector } from '@/components/business/ms-user-selector';

  import { getTemplageOption } from '@/api/modules/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  const { t } = useI18n();

  interface TemplateOption {
    label: string;
    value: string;
  }

  const appStore = useAppStore();

  const route = useRoute();
  const templateOption = ref<TemplateOption[]>([]);
  const form = ref({
    name: '',
    content: '',
    templateId: '',
    handleMan: [],
    status: '',
    severity: '',
    tag: [],
  });
  const formRef = ref<any>(null);

  const fileList = ref<FileItem[]>([]);

  // 模板id
  const templateId = ref<string>('');
  const isEdit = computed(() => !!route.query.id);

  const title = computed(() => {
    return isEdit.value ? t('bugManagement.editBug') : t('bugManagement.createBug');
  });

  const getTemplateOptions = async () => {
    try {
      const res = await getTemplageOption({ projectId: appStore.currentProjectId });
      templateOption.value = res.map((item) => {
        if (item.enableDefault && !isEdit.value) {
          templateId.value = item.id;
        }
        return {
          label: item.name,
          value: item.id,
        };
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
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

  onBeforeMount(() => {
    getTemplateOptions();
  });
</script>

<style lang="less" scoped>
  :deep(.arco-form-item-extra) {
    font-size: 14px;
    color: var(--color-text-4);
  }
</style>
