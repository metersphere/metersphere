<template>
  <MsCard simple>
    <div class="mb-4">
      <div class="back-btn" @click="handleBack">
        <icon-arrow-left />
      </div>
    </div>
    <div class="mb-4 flex items-center justify-between">
      <a-button type="primary" @click="showAddRule(undefined)">{{ t('project.menu.addFalseAlertRules') }}</a-button>
      <a-input-search
        v-model="keyword"
        :placeholder="t('project.menu.nameSearch')"
        class="w-[240px]"
        allow-clear
        @press-enter="fetchData"
        @search="fetchData"
      ></a-input-search>
    </div>
    <MsBaseTable
      v-bind="propsRes"
      :action-config="tableBatchActions"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
    >
      <template #operation="{ record }">
        <template v-if="!record.enable">
          <MsButton class="!mr-0" @click="handleEnableOrDisableProject(record.id)">{{ t('common.enable') }}</MsButton>
          <a-divider direction="vertical" />
          <MsButton class="!mr-0" @click="handleDelete(record.id)">{{ t('common.delete') }}</MsButton>
        </template>
        <template v-else>
          <MsButton class="!mr-0" @click="showAddRule(record)">{{ t('common.edit') }}</MsButton>
          <a-divider direction="vertical" />
          <MsButton class="!mr-0" @click="handleEnableOrDisableProject(record.id, false)">{{
            t('common.disable')
          }}</MsButton>
          <a-divider direction="vertical" />
          <MsTableMoreAction
            class="!mr-0"
            :list="tableActions"
            @select="handleMoreAction($event, record)"
          ></MsTableMoreAction>
        </template>
      </template>
    </MsBaseTable>
  </MsCard>
  <MsDrawer
    v-model:visible="addVisible"
    :title="t('project.menu.addFalseAlertRules')"
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

<script lang="ts" setup>
  import { useRouter } from 'vue-router';
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
  import { useAppStore } from '@/store';

  import { FakeTableListItem } from '@/models/projectManagement/menuManagement';
  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  type UserModalMode = 'create' | 'edit';

  const { t } = useI18n();
  const appStore = useAppStore();
  const router = useRouter();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const addVisible = ref(false);
  const addLoading = ref(false);
  const batchFormRef = ref();
  const ruleFormMode = ref<UserModalMode>('create');
  const currentList = ref<FakeTableListItem[]>([]);
  const headerOptions = computed(() => [
    { label: 'Response Headers', value: 'headers' },
    { label: 'Response Data', value: 'data' },
    { label: 'Response Code', value: 'code' },
  ]);
  const relationOptions = computed(() => [
    { label: '包含', value: 'contain' },
    { label: '不包含', value: 'notContain' },
    { label: '等于', value: 'equal' },
    { label: '不等于', value: 'notEqual' },
    { label: '正则匹配', value: 'regex' },
    { label: '以...开始', value: 'startWith' },
    { label: '以...结束', value: 'endWith' },
  ]);

  const tableBatchActions = {
    baseAction: [
      {
        label: 'common.enable',
        eventTag: 'batchEnable',
      },
      {
        label: 'common.disable',
        eventTag: 'batchDisable',
      },
      {
        label: 'common.delete',
        eventTag: 'batchDelete',
        danger: true,
      },
    ],
  };

  const getRowRuleString = (record: TableData) => {
    const header = headerOptions.value.find((item) => item.value === record.respType)?.label;
    const relation = relationOptions.value.find((item) => item.value === record.relation)?.label;
    return `${header} ${relation} ${record.expression}`;
  };

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
      title: 'project.menu.rule.operation',
      dataIndex: 'operation',
      slotName: 'operation',
      showTooltip: true,
      width: 169,
    },
  ];

  const { propsRes, propsEvent, loadList, setKeyword, setLoadListParams, resetSelector } = useTable(
    postFakeTableList,
    {
      scroll: { x: 1200 },
      columns: rulesColumn,
      tableKey: TableKeyEnum.PROJECT_MANAGEMENT_MENU_FALSE_ALERT,
      selectable: true,
      noDisable: false,
      size: 'default',
      debug: true,
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
    },
  ];

  const handleEnableOrDisableProject = async (v: string | BatchActionQueryParams, isEnable = true) => {
    const title = isEnable ? t('project.menu.rule.enableRule') : t('project.menu.rule.disableRule');
    const content = isEnable ? t('project.menu.rule.enableRuleTip') : t('project.menu.rule.disableRuleTip');
    const okText = isEnable ? t('common.confirmEnable') : t('common.confirmClose');
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
          Message.success(isEnable ? t('common.enableSuccess') : t('common.closeSuccess'));
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
        if (typeof record.type === 'string') {
          record.type = record.type.split(',');
        }
        currentList.value = [record];
      }
    } else {
      ruleFormMode.value = 'create';
    }
    addVisible.value = true;
  };

  const handleCancel = (shouldSearch: boolean, isClose = true) => {
    if (shouldSearch) {
      fetchData();
    }
    if (isClose) {
      addVisible.value = false;
    }
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

        handleCancel(true, false);
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

  const batchFormModels: Ref<FormItemModel[]> = ref([
    {
      filed: 'name',
      type: 'input',
      label: 'project.menu.rule.ruleName',
      rules: [
        { required: true, message: t('project.menu.rule.ruleNameNotNull') },
        { notRepeat: true, message: 'project.menu.rule.ruleNameRepeat' },
      ],
    },
    {
      filed: 'type',
      type: 'tagInput',
      label: 'project.menu.rule.label',
    },
    {
      filed: 'rule',
      type: 'multiple',
      label: 'project.menu.rule.rule',
      hasRedStar: true,
      children: [
        {
          filed: 'respType', // 匹配规则-内容类型/header/data/body
          type: 'select',
          options: headerOptions.value,
          className: 'w-[205px]',
          defaultValue: 'headers',
        },
        {
          filed: 'relation', // 匹配规则-操作类型
          type: 'select',
          options: relationOptions.value,
          className: 'w-[120px]',
          defaultValue: 'equal',
        },
        {
          filed: 'expression', // 匹配规则-表达式
          type: 'input',
          rules: [{ required: true, message: t('project.menu.rule.expressionNotNull') }],
          className: 'w-[301px]',
        },
      ],
    },
  ]);

  onMounted(() => {
    setLoadListParams({ projectId: currentProjectId.value });
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
