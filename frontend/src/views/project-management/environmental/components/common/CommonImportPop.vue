<template>
  <a-modal
    v-model:visible="visible"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :ok-text="t('common.import')"
    :ok-loading="confirmLoading"
    :ok-button-props="{ disabled: fileList.length === 0 }"
    unmount-on-close
    @cancel="handleCancel(false)"
    @ok="confirmHandler"
  >
    <template #title>
      <span>{{ t('common.import') + t(`project.environmental.${props.type}`) }}</span>
    </template>
    <template #default>
      <div class="title">
        <icon-exclamation-circle-fill :size="20" class="text-[rgb(var(--primary-5))]" />
        <span class="text-[var(--color-text-1)]"> {{ t('project.environmental.importTile') }}</span>
      </div>
      <MsUpload
        v-model:file-list="fileList"
        class="w-full"
        accept="json"
        :max-size="50"
        size-unit="MB"
        main-text="system.user.importModalDragText"
        :sub-text="t('project.environmental.supportFormat')"
        :show-file-list="false"
        :auto-upload="false"
        :multiple="false"
        :disabled="confirmLoading"
        @change="handleChange"
      />
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { type FileItem, Message } from '@arco-design/web-vue';

  import MsUpload from '@/components/pure/ms-upload/index.vue';

  import { importGlobalParam } from '@/api/modules/project-management/envManagement';
  import { useI18n } from '@/hooks/useI18n';

  import { EnvAuthTypeEnum } from '@/enums/envEnum';

  const props = defineProps<{
    type: EnvAuthTypeEnum;
  }>();

  const confirmLoading = ref<boolean>(false);

  const emit = defineEmits<{
    (e: 'submit', shouldSearch: boolean): void;
  }>();

  const { t } = useI18n();
  const visible = defineModel<boolean>('visible', { required: true, default: false });

  const fileList = ref<FileItem[]>([]);

  const handleCancel = (shouldSearch = false) => {
    visible.value = false;
    emit('submit', shouldSearch);
  };
  const handleChange = (_fileList: FileItem[]) => {
    fileList.value = _fileList;
  };
  const confirmHandler = async () => {
    try {
      confirmLoading.value = true;
      const params = {
        request: null,
        fileList: fileList.value,
      };
      await importGlobalParam(params);
      Message.success(t('common.importSuccess'));
      handleCancel(true);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  };
</script>

<style lang="less" scoped>
  .title {
    display: flex;
    align-items: center;
    margin-bottom: 24px;
    padding: 16px;
    width: 632px;
    border: 1px solid rgb(var(--primary-5));
    border-radius: 6px;
    background: rgb(var(--primary-1));
    gap: 8px;
  }
</style>
