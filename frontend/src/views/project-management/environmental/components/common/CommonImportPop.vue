<template>
  <a-modal
    v-model:visible="visible"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :ok-text="t('common.import')"
    :ok-button-props="{ disabled: fileList.length === 0 }"
    unmount-on-close
    @cancel="handleCancel(false)"
    @ok="confirmHandler"
  >
    <template #title>
      <span>{{ t('common.import') + t(`project.environmental.${props.type}`) }}</span>
    </template>
    <div>
      <div v-if="props.type === EnvAuthTypeEnum.GLOBAL" class="title">
        <icon-exclamation-circle-fill :size="20" class="text-[rgb(var(--primary-5))]" />
        <span class="text-[var(--color-text-1)]"> {{ t('project.environmental.importTile') }}</span>
      </div>
      <MsUpload
        v-model:file-list="fileList"
        class="w-full"
        accept="json"
        size-unit="MB"
        main-text="system.user.importModalDragText"
        :sub-text="t('project.environmental.supportFormat', { size: appStore.getFileMaxSize })"
        :show-file-list="false"
        :auto-upload="false"
        :multiple="false"
        :disabled="confirmLoading"
        @change="handleChange"
      />
    </div>
    <template #footer>
      <div class="flex items-center justify-between">
        <a-spin class="left-items">
          <a-spin v-if="props.type === EnvAuthTypeEnum.ENVIRONMENT">
            <a-switch v-model="isCover" type="line" size="small" />
            <span>
              {{ t('project.environmental.cover') }}
              <a-tooltip>
                <template #content>
                  <div>
                    {{ t('project.environmental.cover.enable') }}
                  </div>
                  <div>
                    {{ t('project.environmental.cover.disable') }}
                  </div>
                </template>
                <icon-question-circle
                  class="ml-1 inline-block text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                />
              </a-tooltip>
            </span>
          </a-spin>
        </a-spin>
        <div>
          <a-button type="secondary" @click="handleCancel(false)">{{ t('system.plugin.pluginCancel') }}</a-button>
          <a-button class="ml-3" type="primary" :loading="confirmLoading" @click="confirmHandler">{{
            t('common.import')
          }}</a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { type FileItem, Message } from '@arco-design/web-vue';

  import MsUpload from '@/components/pure/ms-upload/index.vue';

  import { importEnv, importGlobalParam } from '@/api/modules/project-management/envManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { EnvAuthTypeEnum } from '@/enums/envEnum';

  const props = defineProps<{
    type: EnvAuthTypeEnum;
  }>();

  const confirmLoading = ref<boolean>(false);

  const emit = defineEmits<{
    (e: 'submit', shouldSearch: boolean): void;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();
  const visible = defineModel<boolean>('visible', { required: true, default: false });

  const fileList = ref<FileItem[]>([]);
  const isCover = ref<boolean>(false);

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
      const params: { request: Record<string, any>; fileList: File[] } = {
        request: { cover: isCover.value },
        fileList: fileList.value.map((item: any) => item.file),
      };
      if (props.type === EnvAuthTypeEnum.GLOBAL) {
        await importGlobalParam(params);
        emit('submit', true);
      } else if (props.type === EnvAuthTypeEnum.ENVIRONMENT) {
        await importEnv(params);
      }
      fileList.value = [];
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
  .right-align {
    text-align: right;
  }
  .left-items {
    align-items: center;
    width: 100px;
  }
</style>
