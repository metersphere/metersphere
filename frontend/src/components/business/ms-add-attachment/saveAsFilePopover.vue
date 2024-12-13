<template>
  <a-popover
    v-model:popup-visible="saveFilePopoverVisible"
    trigger="click"
    position="bl"
    content-class="ms-add-attachment-save-file-popover"
    arrow-class="hidden"
    :popup-offset="12"
    @popup-visible-change="
      (val) => {
        if (!val) handleSaveFileCancel();
      }
    "
  >
    <span class="mx-[2px]"><slot></slot></span>
    <template #content>
      <div class="flex flex-col gap-[16px] text-[14px]">
        <div class="font-semibold text-[var(--color-text-1)]">
          {{ t('ms.add.attachment.saveAsTitle') }}
        </div>
        <a-input
          v-model:model-value="saveFileForm.name"
          :placeholder="t('ms.add.attachment.saveAsNamePlaceholder')"
        ></a-input>
        <a-tree-select
          v-model:modelValue="saveFileForm.moduleId"
          :data="moduleTree"
          :field-names="{ title: 'name', key: 'id', children: 'children' }"
          :placeholder="t('ms.add.attachment.saveAsModulePlaceholder')"
          :loading="moduleTreeLoading"
          :tree-props="{
            virtualListProps: {
              height: 200,
              threshold: 200,
            },
          }"
          :filter-tree-node="filterTreeNode"
          allow-search
        >
          <template #tree-slot-title="node">
            <div class="one-line-text w-[300px]">{{ node.name }}</div>
          </template>
        </a-tree-select>
        <div class="flex items-center justify-end gap-[12px]">
          <a-button type="secondary" @click="handleSaveFileCancel">{{ t('common.cancel') }}</a-button>
          <a-button
            type="primary"
            :loading="saveLoading"
            :disabled="saveFileForm.name.trim() === ''"
            @click="handleSaveFileConfirm"
          >
            {{ t('common.confirm') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-popover>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import { MsFileItem } from '@/components/pure/ms-upload/types';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { filterTreeNode } from '@/utils';

  import { ModuleTreeNode, TransferFileParams } from '@/models/common';

  const props = withDefaults(
    defineProps<{
      savingFile?: MsFileItem;
      fileSaveAsSourceId: string | number;
      sourceIdKey?: string; // 资源id对应key
      fileIdKey?: string;
      fileSaveAsApi?: (params: TransferFileParams) => Promise<string>;
      fileModuleOptionsApi?: (projectId: string) => Promise<ModuleTreeNode[]>; // 文件转存目录下拉框接口
    }>(),
    {
      fileIdKey: 'fileId',
      sourceIdKey: 'sourceId',
    }
  );
  const emit = defineEmits<{
    (e: 'finish', fileId: string, fileName: string): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const saveFilePopoverVisible = defineModel<boolean>('visible', {
    default: false,
  });
  const saveFileForm = ref({
    name: '',
    moduleId: 'root',
  });
  const saveLoading = ref(false);
  const moduleTree = ref<ModuleTreeNode[]>([]);
  const moduleTreeLoading = ref(false);

  /**
   *  初始化文件转存目录下拉框选项
   */
  async function initModuleOptions() {
    try {
      if (props.fileModuleOptionsApi && moduleTree.value.length === 0) {
        // 只初始化一次
        moduleTreeLoading.value = true;
        moduleTree.value = await props.fileModuleOptionsApi(appStore.currentProjectId);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      moduleTreeLoading.value = false;
    }
  }

  watch(
    () => saveFilePopoverVisible.value,
    (visible) => {
      if (visible) {
        initModuleOptions();
        saveFileForm.value.name = (props.savingFile?.name || props.savingFile?.fileName)?.split('.').shift() || '';
      }
    },
    {
      immediate: true,
    }
  );

  /**
   * 关闭文件转存弹窗，清理数据
   */
  function handleSaveFileCancel() {
    saveFileForm.value = {
      name: '',
      moduleId: 'root',
    };
    saveFilePopoverVisible.value = false;
  }

  /**
   * 确认文件转存，转存成功后将本地文件改成关联文件类型
   */
  async function handleSaveFileConfirm() {
    try {
      if (props.fileSaveAsApi && props.savingFile) {
        saveLoading.value = true;
        const res = await props.fileSaveAsApi({
          projectId: appStore.currentProjectId,
          [props.sourceIdKey || 'sourceId']: props.fileSaveAsSourceId || '',
          fileId: props.savingFile[props.fileIdKey] || props.savingFile.uid,
          local: true,
          moduleId: saveFileForm.value.moduleId,
          fileName: saveFileForm.value.name,
          originalName: props.savingFile.name || props.savingFile.fileName || '',
        });
        emit(
          'finish',
          res,
          `${saveFileForm.value.name}.${(props.savingFile.name || props.savingFile.fileName)?.split('.').pop()}`
        );
        Message.success(t('ms.add.attachment.saveAsSuccess'));
        handleSaveFileCancel();
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      saveLoading.value = false;
    }
  }
</script>

<style lang="less">
  .ms-add-attachment-save-file-popover {
    padding: 24px;
    width: 300px;
    .arco-popover-content {
      margin-top: 0;
    }
  }
</style>
