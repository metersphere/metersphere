<template>
  <div v-if="!getIsVisited()" class="title">
    <span class="text-[var(--color-text-1)]">{{ t('project.environmental.httpTitle') }}</span>
    <span class="cursor-pointer text-[var(--color-text-2)]" @click="handleNoWarning">{{
      t('project.environmental.httpNoWarning')
    }}</span>
  </div>
  <div class="flex items-center justify-between">
    <a-button
      v-if="!store.currentEnvDetailInfo.mock"
      v-permission="['PROJECT_ENVIRONMENT:READ+UPDATE']"
      type="outline"
      @click="handleAddHttp"
      >{{ t('project.environmental.addHttp') }}</a-button
    >

    <div class="flex items-center justify-end gap-[8px]">
      <a-input-number
        v-model:model-value="form.requestTimeout"
        :min="0"
        :step="100"
        :precision="0"
        :max="600000"
        :disabled="isDisabled"
        class="w-[180px]"
      >
        <template #prefix>
          <span class="text-[var(--color-text-3)]">{{ t('project.environmental.http.linkTimeOut') }}</span>
        </template>
      </a-input-number>
      <a-input-number
        v-model:model-value="form.responseTimeout"
        :min="0"
        :step="100"
        :max="600000"
        :precision="0"
        :disabled="isDisabled"
        class="w-[180px]"
      >
        <template #prefix>
          <span class="text-[var(--color-text-3)]">{{ t('project.environmental.http.resTimeOut') }}</span>
        </template>
      </a-input-number>
      <!-- TOTO 第一个版本不做 -->
      <!-- <a-select v-model:model-value="form.authType" class="w-[200px]">
        <template #prefix>
          <span class="text-[var(--color-text-3)]">{{ t('project.environmental.http.authType') }}</span>
        </template>
        <a-option>Basic Auth</a-option>
        <a-option>Basic Auth2</a-option>
        <a-option>Basic Auth3</a-option>
      </a-select> -->
    </div>
  </div>
  <MsBaseTable ref="tableRef" class="mt-[16px]" v-bind="propsRes" v-on="propsEvent" @change="changeHandler">
    <template #type="{ record }">
      <span>{{ getEnableScope(record.type) }}</span>
    </template>
    <template #moduleValue="{ record }">
      <a-tooltip :content="getModuleName(record)" position="left">
        <span class="one-line-text max-w-[300px]">{{ getModuleName(record) }}</span>
      </a-tooltip>
    </template>
    <template #operation="{ record }">
      <div class="flex flex-row flex-nowrap items-center">
        <MsButton class="!mr-0" :disabled="isDisabled || store.currentEnvDetailInfo.mock" @click="handleCopy(record)">{{
          t('common.copy')
        }}</MsButton>
        <a-divider class="h-[16px]" direction="vertical" />
        <MsButton class="!mr-0" :disabled="isDisabled || store.currentEnvDetailInfo.mock" @click="handleEdit(record)">{{
          t('common.edit')
        }}</MsButton>
        <a-divider class="h-[16px]" direction="vertical" />
        <MsTableMoreAction
          v-permission="['PROJECT_ENVIRONMENT:READ+UPDATE']"
          :list="moreActionList"
          trigger="click"
          @select="handleMoreActionSelect($event, record)"
        />
      </div>
    </template>
    <template #empty>
      <div class="flex w-full items-center justify-center text-[var(--color-text-4)]">
        <span v-if="hasAnyPermission(['PROJECT_ENVIRONMENT:READ+UPDATE'])">{{
          t('caseManagement.caseReview.tableNoData')
        }}</span>
        <span v-else>{{ t('caseManagement.featureCase.tableNoData') }}</span>
        <MsButton v-permission="['PROJECT_ENVIRONMENT:READ+UPDATE']" class="ml-[8px]" @click="handleAddHttp">
          {{ t('project.environmental.addHttp') }}
        </MsButton>
      </div>
    </template>
  </MsBaseTable>
  <AddHttpDrawer v-model:visible="addVisible" :is-copy="isCopy" :current-id="httpId" @close="addVisible = false" />
</template>

<script lang="ts" async setup>
  import { TableChangeExtra, TableData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import AddHttpDrawer from './popUp/AddHttpDrawer.vue';

  import { getEnvModules } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';
  import { useAppStore, useTableStore } from '@/store';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';
  import { findNodeByKey, findNodeNames } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { BugListItem } from '@/models/bug-management';
  import type { ModuleTreeNode } from '@/models/common';
  import type { CommonParams } from '@/models/projectManagement/environmental';
  import { HttpForm } from '@/models/projectManagement/environmental';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const appStore = useAppStore();
  const { t } = useI18n();
  const visitedKey = 'notRemindHttp';
  const { addVisited, getIsVisited } = useVisit(visitedKey);
  const store = useProjectEnvStore();

  const tableStore = useTableStore();
  const addVisible = ref(false);
  const columns: MsTableColumn = [
    {
      title: 'project.environmental.http.host',
      dataIndex: 'url',
      slotName: 'url',
      showTooltip: true,
      showDrag: false,
      columnSelectorDisabled: true,
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      showDrag: true,
      showTooltip: true,
    },
    {
      title: 'project.environmental.http.enableScope',
      dataIndex: 'type',
      slotName: 'type',
      showDrag: true,
    },
    {
      title: 'project.environmental.http.value',
      dataIndex: 'value',
      slotName: 'moduleValue',
      showTooltip: false,
      showDrag: true,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 170,
    },
  ];
  await tableStore.initColumn(TableKeyEnum.PROJECT_MANAGEMENT_ENV_ENV_DATASTORES, columns);
  const { propsRes, propsEvent } = useTable(undefined, {
    tableKey: TableKeyEnum.PROJECT_MANAGEMENT_ENV_ENV_DATASTORES,
    columns,
    scroll: { x: '100%' },
    selectable: false,
    noDisable: true,
    showSetting: true,
    showPagination: false,
    draggable: { type: 'handle' },
    showMode: false,
    heightUsed: 644,
    debug: true,
  });
  const isDisabled = computed(() => !hasAnyPermission(['PROJECT_ENVIRONMENT:READ+UPDATE']));

  const moreActionList: ActionsItem[] = [
    {
      label: t('common.delete'),
      danger: true,
      eventTag: 'delete',
      disabled: store.currentEnvDetailInfo.mock,
    },
  ];

  const handleSingleDelete = (record?: TableData) => {
    if (record) {
      const index = store.currentEnvDetailInfo.config.httpConfig.findIndex((item) => item.id === record.id);
      if (index > -1) {
        store.currentEnvDetailInfo.config.httpConfig.splice(index, 1);
      }
    }
  };

  function handleMoreActionSelect(item: ActionsItem, record: BugListItem) {
    if (item.eventTag === 'delete') {
      handleSingleDelete(record);
    }
  }

  const httpId = ref<string>('');
  const isCopy = ref<boolean>(false);
  const handleCopy = (record: any) => {
    httpId.value = record.id;
    isCopy.value = true;
    addVisible.value = true;
  };

  const handleEdit = (record: any) => {
    httpId.value = record.id;
    isCopy.value = false;
    addVisible.value = true;
  };

  const handleAddHttp = () => {
    httpId.value = '';
    isCopy.value = false;
    addVisible.value = true;
  };

  const handleNoWarning = () => {
    addVisited();
  };

  watch(
    store.currentEnvDetailInfo.config.httpConfig,
    () => {
      propsRes.value.data = store.currentEnvDetailInfo.config.httpConfig;
    },
    { deep: true, immediate: true }
  );

  const form = computed({
    set: (value: any) => {
      store.currentEnvDetailInfo.config.commonParams = { ...value };
    },
    get: () => store.currentEnvDetailInfo.config.commonParams as CommonParams,
  });

  const data = computed({
    set: (value: any) => {
      store.currentEnvDetailInfo.config.httpConfig = value;
    },
    get: () => {
      return store.currentEnvDetailInfo.config.httpConfig;
    },
  });

  watch(
    () => data.value,
    (val) => {
      if (val) {
        propsRes.value.data = data.value;
      }
    }
  );

  // 排序
  function changeHandler(_data: TableData[], extra: TableChangeExtra, currentData: TableData[]) {
    if (!currentData || currentData.length === 1) {
      return false;
    }
    propsRes.value.data = _data;
    data.value = _data;
  }

  function getEnableScope(type: string) {
    switch (type) {
      case 'NONE':
        return t('project.environmental.http.none');
      case 'MODULE':
        return t('project.environmental.http.module');
      case 'PATH':
        return t('project.environmental.http.path');
      default:
        break;
    }
  }
  const moduleTree = ref<ModuleTreeNode[]>([]);
  async function initModuleTree() {
    try {
      const res = await getEnvModules({
        projectId: appStore.currentProjectId,
      });
      moduleTree.value = res.moduleTree;
    } catch (error) {
      console.log(error);
    }
  }
  await initModuleTree();

  const OPERATOR_MAP = [
    {
      value: 'CONTAINS',
      label: '包含',
    },
    {
      value: 'EQUALS',
      label: '等于',
    },
  ];

  function getCondition(condition: string) {
    return OPERATOR_MAP.find((item) => item.value === condition)?.label;
  }

  function getModuleName(record: HttpForm) {
    if (record.type === 'MODULE') {
      const moduleIds: string[] = [];
      // 勾了包含子模块的只显示父模块
      record.moduleMatchRule.modules.forEach((item) => {
        const realNode = findNodeByKey<ModuleTreeNode>(moduleTree.value, item.moduleId, 'id');
        const notShow = record.moduleMatchRule.modules.find(
          (i) => i.moduleId === realNode?.parentId && i.containChildModule
        );
        if (!notShow) {
          moduleIds.push(item.moduleId);
        }
      });
      const result = findNodeNames(moduleTree.value, moduleIds);
      return result.join(',');
    }
    if (record.type === 'PATH') {
      return `${getCondition(record.pathMatchRule.condition)}${record.pathMatchRule.path}`;
    }
    return '-';
  }
  const tableRef = ref();
</script>

<style lang="less" scoped>
  .title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 0 16px;
    height: 38px;
    border: 1px solid rgb(var(--primary-5));
    border-radius: 4px;
    background-color: rgb(var(--primary-1));
  }
</style>
