<template>
  <MsCard simple>
    <div class="mb-4">
      <div class="back-btn" @click="handleBack">
        <icon-arrow-left />
      </div>
    </div>
    <div class="mb-4 flex items-center justify-between">
      <a-button v-permission="['PROJECT_APPLICATION_API:UPDATE']" type="primary" @click="showAddRule(undefined)">
        {{ t('project.menu.addFalseAlertRules') }}
      </a-button>
      <a-input-search
        v-model="keyword"
        :placeholder="t('project.menu.nameSearch')"
        class="w-[240px]"
        allow-clear
        @press-enter="fetchData"
        @search="fetchData"
        @clear="fetchData"
      ></a-input-search>
    </div>
    <MsBaseTable
      v-bind="propsRes"
      :action-config="tableBatchActions"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
      @enable-change="enableChange"
    >
      <template #operation="{ record }">
        <span v-permission="['PROJECT_APPLICATION_API:UPDATE']" class="flex flex-row items-center">
          <MsButton class="!mr-0" @click="showAddRule(record)">{{ t('common.edit') }}</MsButton>
          <a-divider v-permission="['PROJECT_APPLICATION_API:DELETE']" class="h-[16px]" direction="vertical" />
        </span>
        <MsTableMoreAction class="!mr-0" :list="tableActions" @select="handleMoreAction($event, record)" />
      </template>
    </MsBaseTable>
  </MsCard>
  <MsDrawer
    v-model:visible="addVisible"
    :title="ruleFormMode === 'create' ? t('project.menu.addFalseAlertRules') : t('project.menu.updateFalseAlertRules')"
    :destroy-on-close="true"
    :closable="true"
    :mask-closable="false"
    :get-container="false"
    :body-style="{ padding: '0px' }"
    :width="1200"
    :ok-loading="addLoading"
    :ok-text="ruleFormMode === 'create' ? t('common.add') : t('common.update')"
    @cancel="handleCancel(false)"
    @confirm="handleConfirm"
  >
    <MsBatchForm
      ref="batchFormRef"
      :models="batchFormModels"
      :form-mode="ruleFormMode"
      add-text="project.menu.rule.addRule"
      :default-vals="currentList"
      show-enable
      :is-show-drag="false"
    ></MsBatchForm>
  </MsDrawer>
</template>

<script async lang="ts" setup>
  import { useRoute, useRouter } from 'vue-router';
  import { Message, TableData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsBatchForm from '@/components/business/ms-batch-form/index.vue';
  import { FormItemModel } from '@/components/business/ms-batch-form/types';

  import {
    getDeleteFake,
    postAddFake,
    postFakeTableList,
    postUpdateEnableFake,
    postUpdateFake,
  } from '@/api/modules/project-management/menuManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import { hasAnyPermission } from '@/utils/permission';

  import { FakeTableListItem } from '@/models/projectManagement/menuManagement';
  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  type UserModalMode = 'create' | 'edit';

  const { t } = useI18n();
  const appStore = useAppStore();
  const router = useRouter();
  const route = useRoute();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const addVisible = ref(false);
  const addLoading = ref(false);
  const batchFormRef = ref();
  const ruleFormMode = ref<UserModalMode>('create');
  const currentList = ref<FakeTableListItem[]>([]);
  const tableStore = useTableStore();
  const headerOptions = computed(() => [
    { label: 'Response Headers', value: 'RESPONSE_HEADERS' },
    { label: 'Response Data', value: 'RESPONSE_DATA' },
    { label: 'Response Code', value: 'RESPONSE_CODE' },
  ]);
  const relationOptions = computed(() => [
    { label: t('advanceFilter.operator.contains'), value: 'CONTAINS' },
    { label: t('advanceFilter.operator.not_contains'), value: 'NOT_CONTAINS' },
    { label: t('advanceFilter.operator.equal'), value: 'EQUALS' },
    { label: t('advanceFilter.operator.start_with'), value: 'START_WITH' },
    { label: t('advanceFilter.operator.end_with'), value: 'END_WITH' },
  ]);

  const tableBatchActions = {
    baseAction: [
      {
        label: 'common.enable',
        eventTag: 'batchEnable',
        permission: ['PROJECT_APPLICATION_API:UPDATE'],
      },
      {
        label: 'common.disable',
        eventTag: 'batchDisable',
        permission: ['PROJECT_APPLICATION_API:UPDATE'],
      },
      {
        label: 'common.delete',
        eventTag: 'batchDelete',
        danger: true,
        permission: ['PROJECT_APPLICATION_API:DELETE'],
      },
    ],
  };

  const getRowRuleString = (record: TableData) => {
    const header = headerOptions.value.find((item) => item.value === record.respType)?.label;
    const relation = relationOptions.value.find((item) => item.value === record.relation)?.label;
    return `${header} ${relation} ${record.expression}`;
  };
  const initBatchFormModels: FormItemModel[] = [
    {
      field: 'name',
      type: 'input',
      label: 'project.menu.rule.ruleName',
      rules: [
        { required: true, message: t('project.menu.rule.ruleNameNotNull') },
        { notRepeat: true, message: 'project.menu.rule.ruleNameRepeat' },
      ],
    },
    {
      field: 'type',
      type: 'tagInput',
      label: 'project.menu.rule.label',
    },
    {
      field: 'rule',
      type: 'multiple',
      label: 'project.menu.rule.rule',
      hasRedStar: true,
      children: [
        {
          field: 'respType', // 匹配规则-内容类型/header/data/body
          type: 'select',
          options: headerOptions.value,
          className: 'w-[205px]',
          defaultValue: 'RESPONSE_HEADERS',
        },
        {
          field: 'relation', // 匹配规则-操作类型
          type: 'select',
          options: relationOptions.value,
          defaultValue: 'CONTAINS',
          className: 'w-[120px]',
        },
        {
          field: 'expression', // 匹配规则-表达式
          type: 'textarea',
          rules: [{ required: true, message: t('project.menu.rule.expressionNotNull') }],
          title: t('project.menu.rule.ruleExpression'),
          className: 'w-[301px]',
        },
      ],
    },
  ];

  const hasOperationPermission = computed(() =>
    hasAnyPermission(['PROJECT_APPLICATION_API:UPDATE', 'PROJECT_APPLICATION_API:DELETE'])
  );

  const batchFormModels: Ref<FormItemModel[]> = ref([...initBatchFormModels]);

  const rulesColumn: MsTableColumn = [
    {
      title: 'project.menu.rule.name',
      dataIndex: 'name',
      showTooltip: true,
      width: 149,
    },
    {
      title: 'project.menu.rule.enable',
      dataIndex: 'enable',
      width: 143,
      permission: ['PROJECT_APPLICATION_API:UPDATE'],
      filterConfig: {
        options: [
          {
            value: true,
            label: t('common.enable'),
          },
          {
            value: false,
            label: t('common.close'),
          },
        ],
      },
    },
    {
      title: 'project.menu.rule.label',
      dataIndex: 'typeList',
      isStringTag: true,
      width: 146,
    },
    {
      title: 'project.menu.rule.rule',
      dataIndex: 'ruleResult',
      showTooltip: true,
    },
    {
      title: 'project.menu.rule.creator',
      dataIndex: 'createUser',
      width: 108,
      showTooltip: true,
    },
    {
      title: 'project.menu.rule.updateTime',
      dataIndex: 'updateTime',
      sortable: {
        sorter: true,
        sortDirections: ['ascend', 'descend'],
      },
      width: 210,
    },
    {
      title: hasOperationPermission.value ? 'project.menu.rule.operation' : '',
      dataIndex: 'operation',
      slotName: 'operation',
      showTooltip: true,
      width: hasOperationPermission.value ? 150 : 50,
    },
  ];
  await tableStore.initColumn(TableKeyEnum.PROJECT_MANAGEMENT_MENU_FALSE_ALERT, rulesColumn, 'drawer');
  const { propsRes, propsEvent, loadList, setKeyword, setLoadListParams, resetSelector } = useTable(
    postFakeTableList,
    {
      scroll: { x: 1200 },
      tableKey: TableKeyEnum.PROJECT_MANAGEMENT_MENU_FALSE_ALERT,
      selectable: true,
      noDisable: false,
      size: 'default',
      showSetting: true,
    },
    (record: TableData) => {
      record.typeList = record.type ? record.type.split(',') : [];
      record.ruleResult = getRowRuleString(record);
      return record;
    }
  );

  const { openDeleteModal, openModal } = useModal();

  const keyword = ref('');

  const fetchData = async () => {
    setKeyword(keyword.value);
    await loadList();
    resetSelector();
  };

  const handleBack = () => {
    router.push({
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT,
    });
  };

  const handleDelete = (v: string | BatchActionQueryParams) => {
    openDeleteModal({
      title: t('project.menu.rule.deleteRule', { size: typeof v === 'string' ? 1 : v.currentSelectCount }),
      content: t('project.menu.rule.deleteRuleTip'),
      onBeforeOk: async () => {
        try {
          if (typeof v === 'string') {
            // 单个删除
            await getDeleteFake({ selectIds: [v], projectId: currentProjectId.value, selectAll: false });
          } else {
            // 批量删除
            await getDeleteFake({
              selectIds: v.selectedIds,
              selectAll: v.selectAll,
              excludeIds: v.excludeIds,
              condition: v.params,
              projectId: currentProjectId.value,
            });
          }
          Message.success(t('common.deleteSuccess'));
          fetchData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
    });
  };

  const handleMoreAction = (tag: ActionsItem, record: TableData) => {
    if (tag.eventTag === 'delete') {
      handleDelete(record.id);
    }
  };

  const tableActions: ActionsItem[] = [
    {
      label: 'system.user.delete',
      eventTag: 'delete',
      danger: true,
      permission: ['PROJECT_APPLICATION_API:DELETE'],
    },
  ];

  const handleEnableOrDisableProject = async (v: string | BatchActionQueryParams, isEnable = true) => {
    const title = isEnable ? t('project.menu.rule.enableRule') : t('project.menu.rule.disableRule');
    const content = isEnable ? t('project.menu.rule.enableRuleTip') : t('project.menu.rule.disableRuleTip');
    const okText = isEnable ? t('common.confirmEnable') : t('common.confirmDisable');
    openModal({
      type: 'info',
      cancelText: t('common.cancel'),
      title,
      content,
      okText,
      onBeforeOk: async () => {
        try {
          if (typeof v === 'string') {
            // 单个启用/禁用
            await postUpdateEnableFake({
              selectIds: [v],
              projectId: currentProjectId.value,
              enable: isEnable,
              selectAll: false,
            });
          } else {
            // 批量启用/禁用
            await postUpdateEnableFake({
              selectIds: v.selectedIds,
              selectAll: v.selectAll,
              excludeIds: v.excludeIds,
              condition: v.params,
              projectId: currentProjectId.value,
              enable: isEnable,
            });
          }
          Message.success(isEnable ? t('common.enableSuccess') : t('common.disableSuccess'));
          fetchData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };

  const showAddRule = (record?: FakeTableListItem | FakeTableListItem[]) => {
    if (record) {
      // 编辑模式
      ruleFormMode.value = 'edit';

      if (record instanceof Array) {
        record.forEach((item) => {
          if (typeof item.type === 'string') {
            item.type = item.type.split(',');
          }
        });
        // 批量编辑
        currentList.value = record;
      } else {
        // 单个编辑
        if (typeof record.type === 'string' && record.type) {
          record.type = record.type.split(',');
        }
        currentList.value = [record];
      }
    } else {
      ruleFormMode.value = 'create';
    }
    addVisible.value = true;
  };

  const handleCancel = (shouldSearch: boolean) => {
    if (shouldSearch) {
      fetchData();
    }
    batchFormModels.value = [...initBatchFormModels];
    currentList.value = [];
    addVisible.value = false;
  };

  const handleConfirm = () => {
    batchFormRef.value.formValidate(async (list: FakeTableListItem[]) => {
      try {
        addLoading.value = true;
        // eslint-disable-next-line no-console
        const tmpArr = JSON.parse(JSON.stringify(list)) as FakeTableListItem[];
        tmpArr.forEach((element) => {
          if (ruleFormMode.value === 'create') {
            element.projectId = currentProjectId.value;
          } else if (element.typeList) {
            delete element.typeList;
          }
          element.type = element.type instanceof Array ? element.type.join(',') : '';
        });
        if (ruleFormMode.value === 'create') {
          await postAddFake(tmpArr);
          Message.success(t('common.addSuccess'));
        } else {
          await postUpdateFake(tmpArr);
          Message.success(t('common.updateSuccess'));
        }

        handleCancel(true);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        addLoading.value = false;
      }
    });
  };

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    switch (event.eventTag) {
      case 'batchModify':
        // showAddRule(params);
        break;
      case 'batchEnable':
        handleEnableOrDisableProject(params);
        break;
      case 'batchDisable':
        handleEnableOrDisableProject(params, false);
        break;
      default:
        handleDelete(params);
    }
  }

  function enableChange(record: FakeTableListItem, newValue: string | number | boolean) {
    if (record.id) {
      handleEnableOrDisableProject(record.id, newValue as boolean);
    }
  }

  onMounted(() => {
    const { status } = route.query;
    const enabledStatus = status === 'all' ? [] : [true];
    setLoadListParams({
      projectId: currentProjectId.value,
      filter: {
        enable: enabledStatus,
      },
    });
    fetchData();
  });
</script>

<style lang="less" scoped>
  .back-btn {
    @apply flex cursor-pointer items-center rounded-full;

    margin-right: 8px;
    width: 20px;
    height: 20px;
    border: 1px solid #ffffff;
    background: linear-gradient(90deg, rgb(var(--primary-9)) 3.36%, #ffffff 100%);
    box-shadow: 0 0 7px rgb(15 0 78 / 9%);
    .arco-icon {
      color: rgb(var(--primary-5));
    }
  }
</style>
