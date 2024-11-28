<template>
  <div>
    <MsDrawer
      v-model:visible="visible"
      :width="960"
      :title="t('apiScenario.importScenario')"
      :closable="false"
      :ok-disabled="!fileList.length"
      :ok-text="t('common.import')"
      :ok-loading="importLoading"
      disabled-width-drag
      desc
      @confirm="confirmImport"
      @cancel="cancelImport"
    >
      <div class="mb-[16px] flex items-center gap-[16px]">
        <div
          v-for="item of platformList"
          :key="item.value"
          :class="`import-item ${importForm.type === item.value ? 'import-item--active' : ''}`"
          @click="() => setActiveImportFormat(item.value)"
        >
          <div class="text-[var(--color-text-1)]">{{ item.name }}</div>
        </div>
      </div>
      <a-form ref="importFormRef" :model="importForm" layout="vertical">
        <a-form-item :label="t('apiTestManagement.belongModule')">
          <a-tree-select
            v-model:modelValue="importForm.moduleId"
            :data="innerModuleTree"
            class="w-[436px]"
            :field-names="{ title: 'name', key: 'id', children: 'children' }"
            :draggable="false"
            allow-search
            allow-clear
            :filter-tree-node="filterTreeNode"
            :fallback-option="(key: string | number) => ({
                name: t('apiTestManagement.moduleNotExist'),
                key,
                disabled: true,
              })"
          >
            <template #tree-slot-title="node">
              <a-tooltip :content="`${node.name}`" position="tl">
                <div class="one-line-text w-[300px]">{{ node.name }}</div>
              </a-tooltip>
            </template>
          </a-tree-select>
        </a-form-item>
        <a-form-item :label="t('apiTestManagement.importMode')">
          <a-radio-group v-model:model-value="importForm.coverData">
            <a-radio :value="true">
              <div class="flex items-center gap-[2px]">
                {{ t('apiTestManagement.cover') }}
                <a-tooltip position="right" :content="t('apiScenario.importScenarioCoverTip')">
                  <icon-question-circle
                    class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                    size="16"
                  />
                </a-tooltip>
              </div>
            </a-radio>
            <a-radio :value="false">
              <div class="flex items-center gap-[2px]">
                {{ t('apiTestManagement.uncover') }}
                <a-tooltip position="right">
                  <icon-question-circle
                    class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                    size="16"
                  />
                  <template #content>
                    <div>{{ t('apiScenario.importScenarioUncoverTip1') }}</div>
                    <div>{{ t('apiScenario.importScenarioUncoverTip2') }}</div>
                  </template>
                </a-tooltip>
              </div>
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <MsUpload
          v-model:file-list="fileList"
          :accept="fileAccept"
          :auto-upload="false"
          draggable
          size-unit="MB"
          class="w-full"
        >
          <template #subText>
            <div class="flex">
              {{ t('apiScenario.importScenarioUploadTip', { type: fileAccept, size: appStore.getFileMaxSize }) }}
            </div>
          </template>
        </MsUpload>
      </a-form>
    </MsDrawer>
  </div>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import type { MsFileItem } from '@/components/pure/ms-upload/types';

  import { importScenario } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { filterTree, filterTreeNode, TreeNode } from '@/utils';

  import { ImportScenarioRequest } from '@/models/apiTest/scenario';
  import type { ModuleTreeNode } from '@/models/common';
  import { RequestImportFormat } from '@/enums/apiEnum';

  const props = defineProps<{
    visible: boolean;
    moduleTree: ModuleTreeNode[];
    activeModule: string;
  }>();
  const emit = defineEmits(['update:visible', 'done']);

  const { t } = useI18n();
  const appStore = useAppStore();

  const visible = useVModel(props, 'visible', emit);
  const innerModuleTree = ref<TreeNode<ModuleTreeNode>[]>([]);
  const platformList: {
    name: string;
    value: RequestImportFormat.MeterSphere | RequestImportFormat.Jmeter | RequestImportFormat.Har;
  }[] = [
    {
      name: 'MeterSphere',
      value: RequestImportFormat.MeterSphere,
    },
    {
      name: 'Jmeter',
      value: RequestImportFormat.Jmeter,
    },
    {
      name: 'Har',
      value: RequestImportFormat.Har,
    },
  ];
  const fileList = ref<MsFileItem[]>([]);
  const defaultForm: ImportScenarioRequest = {
    moduleId: '',
    coverData: false,
    projectId: appStore.currentProjectId,
    type: RequestImportFormat.MeterSphere,
  };
  const importForm = ref({ ...defaultForm });
  const importFormRef = ref<FormInstance>();
  const fileAccept = computed(() => {
    if (importForm.value.type === RequestImportFormat.MeterSphere) {
      return 'ms';
    }
    if (importForm.value.type === RequestImportFormat.Jmeter) {
      return 'jmx';
    }
    if (importForm.value.type === RequestImportFormat.Har) {
      return 'har';
    }
    return 'ms';
  });

  watch(
    () => visible.value,
    (val) => {
      if (val) {
        importForm.value.moduleId = props.activeModule !== 'all' ? props.activeModule : '';
        innerModuleTree.value = filterTree(props.moduleTree, (node) => node.type === 'MODULE');
      }
    },
    {
      immediate: true,
    }
  );

  const importLoading = ref(false);

  function setActiveImportFormat(
    format: RequestImportFormat.MeterSphere | RequestImportFormat.Jmeter | RequestImportFormat.Har
  ) {
    importForm.value.type = format;
  }

  function cancelImport() {
    visible.value = false;
    importForm.value = { ...defaultForm };
    importFormRef.value?.resetFields();
    fileList.value = [];
  }

  async function doImportScenario() {
    try {
      importLoading.value = true;
      await importScenario({
        file: fileList.value[0].file || null,
        request: importForm.value,
      });
      Message.success(t('common.importSuccess'));
      emit('done');
      cancelImport();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      importLoading.value = false;
    }
  }

  function confirmImport() {
    importFormRef.value?.validate((errors) => {
      if (!errors) {
        doImportScenario();
      }
    });
  }
</script>

<style lang="less" scoped>
  .import-item {
    @apply flex cursor-pointer items-center;

    padding: 8px;
    width: 200px;
    border: 1px solid var(--color-text-n8);
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-fff);
    gap: 6px;
  }
  .import-item--active {
    border: 1px solid rgb(var(--primary-5));
    background-color: rgb(var(--primary-1));
  }
  :deep(.arco-form-item) {
    margin-bottom: 16px;
  }
  :deep(.arco-select-view-value::after) {
    @apply hidden;
  }
</style>
