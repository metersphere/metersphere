<template>
  <MsCard simple>
    <div class="mb-4 flex items-center justify-between">
      <a-button type="primary" @click="showAddRule">{{ t('project.menu.addFalseAlertRules') }}</a-button>
      <a-input-search
        v-model="keyword"
        :placeholder="t('project.menu.nameSearch')"
        class="w-[240px]"
        allow-clear
        @press-enter="fetchData"
        @search="fetchData"
      ></a-input-search>
    </div>
    <MsBaseTable v-bind="propsRes" v-on="propsEvent">
      <template #operation="{ record }">
        <template v-if="record.deleted">
          <MsButton @click="handleRevokeDelete(record)">{{ t('common.revokeDelete') }}</MsButton>
        </template>
        <template v-else-if="!record.enable">
          <MsButton @click="handleEnableOrDisableProject(record)">{{ t('common.enable') }}</MsButton>
          <MsButton @click="handleDelete(record)">{{ t('common.delete') }}</MsButton>
        </template>
        <template v-else>
          <MsButton @click="showAddProjectModal(record)">{{ t('common.edit') }}</MsButton>
          <MsButton @click="showAddUserModal(record)">{{ t('system.organization.addMember') }}</MsButton>
          <MsButton @click="handleEnableOrDisableProject(record, false)">{{ t('common.end') }}</MsButton>
          <MsTableMoreAction :list="tableActions" @select="handleMoreAction($event, record)"></MsTableMoreAction>
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
    @cancel="handleCancel(false)"
    @confirm="handleConfirm"
  >
    <a-form ref="ruleFormRef" class="rounded-[4px]" :model="ruleForm" layout="vertical">
      <MsBatchForm
        ref="batchFormRef"
        :models="batchFormModels"
        :form-mode="ruleFormMode"
        add-text="system.user.addUser"
        :default-vals="ruleForm.list"
        show-enable
      ></MsBatchForm>
    </a-form>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { FormInstance, Message, TableData } from '@arco-design/web-vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsBatchForm from '@/components/business/ms-batch-form/index.vue';
  import { FormItemModel } from '@/components/business/ms-batch-form/types';

  import { postFakeTableList } from '@/api/modules/project-management/menuManagement';
  import {
    createOrUpdateProjectByOrg,
    deleteProjectByOrg,
    enableOrDisableProjectByOrg,
    postProjectTableByOrg,
    revokeDeleteProjectByOrg,
  } from '@/api/modules/setting/organizationAndProject';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore } from '@/store';
  import { validateEmail, validatePhone } from '@/utils/validate';

  import { TableKeyEnum } from '@/enums/tableEnum';

  type UserModalMode = 'create' | 'edit';

  const { t } = useI18n();
  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const addVisible = ref(false);
  const addLoading = ref(false);
  const ruleFormRef = ref<FormInstance>();
  const batchFormRef = ref<FormInstance>();
  const ruleFormMode = ref<UserModalMode>('create');
  const ruleForm = reactive({
    name: '',
    enable: true,
    label: '',
    type: '',
    creator: '',
    updateTime: '',
    list: [],
  });
  const rulesColumn: MsTableColumn = [
    {
      title: 'project.menu.rule.name',
      dataIndex: 'name',
      width: 100,
      showTooltip: true,
    },
    {
      title: 'project.menu.rule.enable',
      dataIndex: 'enable',
      showTooltip: true,
    },
    {
      title: 'project.menu.rule.label',
      dataIndex: 'label',
      width: 100,
      showTooltip: true,
    },
    {
      title: 'project.menu.rule.rule',
      dataIndex: 'type',
      width: 100,
      showTooltip: true,
    },
    {
      title: 'project.menu.rule.creator',
      dataIndex: 'creator',
      width: 100,
      showTooltip: true,
    },
    {
      title: 'project.menu.rule.updateTime',
      dataIndex: 'updateTime',
      width: 100,
      showTooltip: true,
    },
    {
      title: 'project.menu.rule.operation',
      dataIndex: 'operation',
      slotName: 'operation',
      width: 100,
      showTooltip: true,
    },
  ];
  const { propsRes, propsEvent, loadList, setKeyword, setLoadListParams } = useTable(postFakeTableList, {
    columns: rulesColumn,
    tableKey: TableKeyEnum.PROJECT_MANAGEMENT_MENU_FALSE_ALERT,
    selectable: true,
    noDisable: false,
    size: 'default',
  });

  const { openDeleteModal, openModal } = useModal();

  const keyword = ref('');

  const fetchData = async () => {
    setKeyword(keyword.value);
    await loadList();
  };

  const handleDelete = (record: TableData) => {
    openDeleteModal({
      title: t('system.organization.deleteName', { name: record.name }),
      content: t('system.organization.deleteTip'),
      onBeforeOk: async () => {
        try {
          await deleteProjectByOrg(record.id);
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
      handleDelete(record);
    }
  };

  const tableActions: ActionsItem[] = [
    {
      label: 'system.user.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const handleEnableOrDisableProject = async (record: any, isEnable = true) => {
    const title = isEnable ? t('system.project.enableTitle') : t('system.project.endTitle');
    const content = isEnable ? t('system.project.enableContent') : t('system.project.endContent');
    const okText = isEnable ? t('common.confirmEnable') : t('common.confirmClose');
    openModal({
      type: 'error',
      cancelText: t('common.cancel'),
      title,
      content,
      okText,
      onBeforeOk: async () => {
        try {
          await enableOrDisableProjectByOrg(record.id, isEnable);
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
  const handleRevokeDelete = async (record: TableData) => {
    openModal({
      type: 'error',
      cancelText: t('common.cancel'),
      title: t('system.project.revokeDeleteTitle', { name: record.name }),
      content: t('system.project.enableContent'),
      okText: t('common.revokeDelete'),
      onBeforeOk: async () => {
        try {
          await revokeDeleteProjectByOrg(record.id);
          Message.success(t('common.revokeDeleteSuccess'));
          fetchData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.error(error);
        }
      },
      hideCancel: false,
    });
  };

  const showAddProjectModal = (record: any) => {
    console.log(record);
  };

  const showAddUserModal = (record: any) => {
    console.log(record);
  };
  const showAddRule = () => {
    addVisible.value = true;
  };

  const handleConfirm = () => {
    addVisible.value = false;
  };

  const handleCancel = (shouldSearch: boolean) => {
    if (shouldSearch) {
      fetchData();
    }
    addVisible.value = false;
  };

  /**
   * 校验用户姓名
   * @param value 输入的值
   * @param callback 失败回调，入参是提示信息
   */
  function checkUerName(value: string | undefined, callback: (error?: string) => void) {
    if (value === '' || value === undefined) {
      callback(t('system.user.createUserNameNotNull'));
    } else if (value.length > 50) {
      callback(t('system.user.createUserNameOverLength'));
    }
  }

  const batchFormModels: Ref<FormItemModel[]> = ref([
    {
      filed: 'name',
      type: 'input',
      label: 'project.menu.rule.ruleName',
      rules: [
        { required: true, message: t('system.user.createUserNameNotNull') },
        { validator: checkUerName },
        { notRepeat: true, message: 'system.user.createUserEmailNoRepeat' },
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
      // rules: [{ required: true, message: t('system.user.createUserEmailNotNull') }],
      children: [
        {
          filed: 'respType', // 匹配规则-内容类型/header/data/body
          type: 'select',
          rules: [{ required: true, message: t('system.user.createUserEmailNotNull') }],
          options: [
            { label: 'Response Headers', value: 'headers' },
            { label: 'Response Data', value: 'data' },
            { label: 'Response Body', value: 'body' },
          ],
          className: 'w-[205px]',
          defaultValue: 'headers',
        },
        {
          filed: 'relation', // 匹配规则-操作类型
          type: 'select',
          options: [
            { label: '包含', value: 'contain' },
            { label: '不包含', value: 'notContain' },
            { label: '等于', value: 'equal' },
            { label: '不等于', value: 'notEqual' },
            { label: '正则匹配', value: 'regex' },
            { label: '以...开始', value: 'startWith' },
            { label: '以...结束', value: 'endWith' },
          ],
          rules: [{ required: true, message: t('system.user.createUserEmailNotNull') }],
          className: 'w-[120px]',
          defaultValue: 'contain',
        },
        {
          filed: 'expression', // 匹配规则-表达式
          type: 'input',
          rules: [{ required: true, message: t('system.user.createUserEmailNotNull') }],
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
