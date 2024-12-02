<template>
  <a-spin class="w-full" :loading="loading">
    <div class="h-full w-full">
      <div class="basic-container">
        <div class="baseItem">
          <span class="label"> {{ t('caseManagement.featureCase.tableColumnModule') }}</span>
          <span class="w-full">
            <a-tree-select
              v-model="detailInfo.moduleId"
              :data="caseTree"
              class="w-full"
              :allow-search="true"
              :filter-tree-node="filterTreeNode"
              :field-names="{
                title: 'name',
                key: 'id',
                children: 'children',
              }"
              :tree-props="{
                virtualListProps: {
                  height: 200,
                },
              }"
              @change="handleChangeModule"
            >
              <template #tree-slot-title="node">
                <a-tooltip :content="`${node.name}`" position="tl">
                  <div class="one-line-text w-[300px]">{{ node.name }}</div>
                </a-tooltip>
              </template>
            </a-tree-select>
          </span>
        </div>
        <MsFormCreate
          v-if="formRules.length"
          ref="formCreateRef"
          v-model:api="fApi"
          v-model:form-item="formItem"
          :form-rule="formRules"
          class="w-full"
          :option="options"
          @change="changeHandler"
        />
        <div class="baseItem">
          <span class="label"> {{ t('caseManagement.featureCase.tableColumnCreateUser') }}</span>
          <a-tooltip v-if="translateTextToPX(detailInfo?.createUserName) > 200" :content="detailInfo?.createUserName">
            <span class="one-line-text" style="max-width: 200px">{{ detailInfo?.createUserName }}</span>
          </a-tooltip>
          <span v-else class="value">{{ detailInfo?.createUserName }}</span>
        </div>
        <div class="baseItem">
          <span class="label"> {{ t('caseManagement.featureCase.tableColumnCreateTime') }}</span>
          <span class="value">{{ dayjs(detailInfo?.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
        </div>
        <div class="baseItem">
          <span class="label"> {{ t('caseManagement.featureCase.tableColumnTag') }}</span>
          <span class="value w-full">
            <MsTagsInput v-model:model-value="detailInfo.tags" class="w-full" @change="changeHandler" />
          </span>
        </div>
      </div>
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message, TreeNodeData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import type { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';

  import { getCaseModuleTree, updateCaseRequest } from '@/api/modules/case-management/featureCase';
  import { defaultCaseDetail } from '@/config/caseManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { translateTextToPX } from '@/utils/css';

  import type { CustomAttributes, DetailCase } from '@/models/caseManagement/featureCase';
  import { ModuleTreeNode } from '@/models/common';

  import { initFormCreate } from '../utils';
  import { LabelValue } from '@arco-design/web-vue/es/tree-select/interface';
  import debounce from 'lodash-es/debounce';

  const { t } = useI18n();

  const props = defineProps<{
    detail: DetailCase;
  }>();

  const emit = defineEmits<{
    (e: 'updateSuccess'): void;
  }>();

  const appStore = useAppStore();

  const detailInfo = ref<DetailCase>(cloneDeep(defaultCaseDetail));

  const options = {
    resetBtn: false, // 不展示默认配置的重置和提交
    submitBtn: false,
    on: false, // 取消绑定on事件
    form: {
      layout: 'horizontal',
      labelAlign: 'left',
      labelColProps: {
        span: 9,
      },
      wrapperColProps: {
        span: 15,
      },
    },
    row: {
      gutter: 0,
    },
    wrap: {
      'asterisk-position': 'end',
      'validate-trigger': ['change'],
    },
  };

  const formRules = ref<FormItem[]>([]);
  const formItem = ref<FormRuleItem[]>([]);
  function getParams() {
    const customFieldsArr = (formItem.value || []).map((item: any) => {
      return {
        fieldId: item.field,
        value: Array.isArray(item.value) ? JSON.stringify(item.value) : item.value,
      };
    });
    return {
      request: {
        ...detailInfo.value,
        deleteFileMetaIds: [], // 删除本地文件id
        unLinkFilesIds: [], // 取消关联文件id
        newAssociateFileListIds: [], // 新关联文件id
        customFields: customFieldsArr,
        caseDetailFileIds: [],
      },
      fileList: [], // 总文件列表
    };
  }
  const fApi = ref<any>(null);
  const loading = ref<boolean>(false);

  function updateHandler() {
    try {
      fApi.value?.validate().then(async (valid: any) => {
        if (valid === true) {
          loading.value = true;
          await updateCaseRequest(getParams());
          loading.value = false;
          Message.success(t('caseManagement.featureCase.editSuccess'));
          emit('updateSuccess');
        }
      });
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  function handleChangeModule(value: string | number | LabelValue | Array<string | number> | LabelValue[] | undefined) {
    detailInfo.value.moduleId = value as string;
    updateHandler();
  }

  function filterTreeNode(searchValue: string, nodeValue: TreeNodeData) {
    return (nodeValue as ModuleTreeNode).name.toLowerCase().indexOf(searchValue.toLowerCase()) > -1;
  }

  const changeHandler = debounce(() => {
    updateHandler();
  }, 300);

  const caseTree = ref<ModuleTreeNode[]>([]);

  async function getCaseTree() {
    try {
      caseTree.value = await getCaseModuleTree({ projectId: appStore.currentProjectId });
    } catch (error) {
      console.log(error);
    }
  }
  // 初始化表单
  function initForm() {
    const { customFields } = detailInfo.value;
    formRules.value = initFormCreate(customFields as CustomAttributes[], ['FUNCTIONAL_CASE:READ+UPDATE']);
  }

  watchEffect(() => {
    detailInfo.value = props.detail;
    initForm();
  });

  onBeforeMount(() => {
    getCaseTree();
  });
</script>

<style scoped lang="less">
  .basic-container {
    max-width: 50%;
    .baseItem {
      margin-bottom: 16px;
      height: 32px;
      line-height: 32px;
      @apply flex;
      .label {
        flex-shrink: 0;
        width: 84px;
        color: var(--color-text-3);
      }
      .value {
        padding-left: 10px;
      }
    }
    :deep(.arco-form-item-layout-horizontal) {
      margin-bottom: 16px !important;
    }
    :deep(.arco-form-item-label-col) {
      padding-right: 0;
    }
    :deep(.arco-col-9) {
      flex: 0 0 84px;
      width: 84px;
    }
    :deep(.arco-col-15) {
      flex: 0 0 calc(100% - 84px);
      width: calc(100% - 84px);
    }
    :deep(.arco-form-item-label::after) {
      color: red !important;
    }
    :deep(.arco-form-item-label-col > .arco-form-item-label) {
      overflow: hidden;
      width: 84px;
      text-overflow: ellipsis;
      white-space: nowrap;
      color: var(--color-text-3) !important;
    }
    :deep(.arco-select-view-single) {
      border-color: transparent !important;
      .arco-select-view-suffix {
        visibility: hidden;
      }
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
        .arco-select-view-suffix {
          visibility: visible !important;
        }
      }
      &:hover > .arco-input {
        font-weight: normal;
        text-decoration: none;
        color: var(--color-text-1);
      }
      & > .arco-input {
        font-weight: 500;
        text-decoration: underline;
        color: var(--color-text-1);
      }
    }
    :deep(.arco-input-tag) {
      border-color: transparent !important;
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
      }
    }
    :deep(.arco-input-wrapper) {
      border-color: transparent !important;
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
      }
    }
    :deep(.arco-select-view-multiple) {
      border-color: transparent !important;
      .arco-select-view-suffix {
        visibility: hidden;
      }
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
        .arco-select-view-suffix {
          visibility: visible !important;
        }
      }
    }
    :deep(.arco-textarea-wrapper) {
      border-color: transparent !important;
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
      }
    }
    :deep(.arco-input-number) {
      border-color: transparent !important;
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
      }
    }
    :deep(.arco-picker) {
      border-color: transparent !important;
      .arco-picker-suffix {
        visibility: hidden;
      }
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
        arco-picker-suffix {
          visibility: visible !important;
        }
      }
    }
  }
</style>
