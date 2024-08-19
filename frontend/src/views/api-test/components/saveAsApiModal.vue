<template>
  <a-modal
    v-model:visible="visible"
    :title="t('apiTestDebug.saveAsApi')"
    class="ms-modal-form"
    title-align="start"
    body-class="!p-0"
    @close="emit('close')"
  >
    <a-form ref="saveModalFormRef" :model="saveModalForm" layout="vertical">
      <a-form-item
        field="name"
        :label="t('apiTestDebug.requestName')"
        :rules="[{ required: true, message: t('apiTestDebug.requestNameRequired') }]"
        asterisk-position="end"
      >
        <a-input
          v-model:model-value="saveModalForm.name"
          :max-length="255"
          :placeholder="t('apiTestDebug.requestNamePlaceholder')"
        />
      </a-form-item>
      <a-form-item
        v-if="props.detail.protocol === 'HTTP'"
        field="path"
        :label="t('apiTestDebug.requestUrl')"
        :rules="[{ required: true, message: t('apiTestDebug.requestUrlRequired') }]"
        asterisk-position="end"
      >
        <a-input
          v-model:model-value="saveModalForm.path"
          :max-length="255"
          :placeholder="t('apiTestDebug.commonPlaceholder')"
        />
      </a-form-item>
      <a-form-item :label="t('apiTestDebug.requestModule')" class="mb-0">
        <a-tree-select
          v-model:modelValue="saveModalForm.moduleId"
          :filter-tree-node="filterTreeNode"
          :data="apiModuleTree"
          :field-names="{ title: 'name', key: 'id', children: 'children' }"
          :tree-props="{
            virtualListProps: {
              height: 200,
              threshold: 200,
            },
          }"
          allow-search
        />
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-[4px]">
          <a-checkbox v-model:model-value="saveModalForm.saveApiAsCase"></a-checkbox>
          {{ t('apiScenario.syncSaveAsCase') }}
        </div>
        <div class="flex items-center gap-[12px]">
          <a-button type="secondary" :disabled="saveLoading" @click="handleSaveApiCancel">
            {{ t('common.cancel') }}
          </a-button>
          <a-button type="primary" :loading="saveLoading" @click="handleSaveApi">{{ t('common.confirm') }}</a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import { addCase, addDefinition, getModuleTreeOnlyModules } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { filterTreeNode } from '@/utils';

  import { AddApiCaseParams } from '@/models/apiTest/management';
  import { RequestCaseStatus, RequestDefinitionStatus } from '@/enums/apiEnum';

  import { defaultResponseItem } from '@/views/api-test/components/config';
  import type { RequestParam as ApiDefinitionRequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import type { RequestParam } from '@/views/api-test/scenario/components/common/customApiDrawer.vue';
  import type { FormInstance } from '@arco-design/web-vue';

  const props = defineProps<{
    detail: RequestParam | ApiDefinitionRequestParam;
  }>();
  const emit = defineEmits(['close']);

  const appStore = useAppStore();
  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });
  const saveModalForm = ref({
    name: '',
    path: '',
    moduleId: 'root',
    saveApiAsCase: false,
  });
  const saveModalFormRef = ref<FormInstance>();
  const saveLoading = ref(false);

  const apiModuleTree = ref<MsTreeNodeData[]>([]);
  async function initApiModuleTree(protocol: string) {
    try {
      apiModuleTree.value = await getModuleTreeOnlyModules({
        keyword: '',
        protocols: [protocol],
        projectId: appStore.currentProjectId,
        moduleIds: [],
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  }

  async function saveApiAsCase(id: string) {
    let url;
    let path = '';
    try {
      url = new URL(saveModalForm.value.path);
      path = url.pathname + url.search + url.hash;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      path = saveModalForm.value.path;
    }
    const params: AddApiCaseParams = {
      name: saveModalForm.value.name,
      projectId: appStore.currentProjectId,
      environmentId: appStore.currentEnvConfig?.id || '',
      apiDefinitionId: id,
      request: {
        ...props.detail,
        url: path,
      },
      priority: 'P0',
      status: RequestCaseStatus.PROCESSING,
      tags: [],
      uploadFileIds: props.detail.uploadFileIds || [],
      linkFileIds: props.detail.linkFileIds || [],
    };
    await addCase(params);
  }

  /**
   * 保存请求
   */
  async function realSaveAsApi() {
    try {
      saveLoading.value = true;
      let url;
      let path = '';
      try {
        url = new URL(saveModalForm.value.path);
        path = url.pathname + url.search + url.hash;
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
        path = saveModalForm.value.path;
      }
      const res = await addDefinition({
        ...saveModalForm.value,
        path,
        projectId: appStore.currentProjectId,
        tags: [],
        description: '',
        status: RequestDefinitionStatus.PROCESSING,
        customFields: [],
        versionId: '',
        environmentId: appStore.currentEnvConfig?.id || '',
        request: {
          ...props.detail,
          url: path,
          path,
        },
        uploadFileIds: props.detail.uploadFileIds || [],
        linkFileIds: props.detail.linkFileIds || [],
        response: [defaultResponseItem],
        method: props.detail.method,
        protocol: props.detail.protocol,
      });
      if (saveModalForm.value.saveApiAsCase) {
        await saveApiAsCase(res.id);
      }
      Message.success(t('common.saveSuccess'));
      visible.value = false;
      saveLoading.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      saveLoading.value = false;
    }
  }

  function handleSaveApiCancel() {
    saveModalFormRef.value?.resetFields();
    saveModalForm.value.saveApiAsCase = false;
    visible.value = false;
  }

  function handleSaveApi() {
    saveModalFormRef.value?.validate(async (errors) => {
      if (!errors) {
        await realSaveAsApi();
        handleSaveApiCancel();
      }
    });
  }

  onBeforeMount(() => {
    saveModalForm.value.path = props.detail.url || props.detail.path;
    initApiModuleTree(props.detail.protocol);
  });
</script>

<style lang="less" scoped></style>
