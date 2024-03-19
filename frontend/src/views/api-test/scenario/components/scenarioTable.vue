<template>
  <div :class="['p-[16px_16px]', props.class]">
    <div class="mb-[16px] flex items-center justify-between">
      <div>
        <span class="flex items-center">
          <a-switch
            v-model:model-value="showItemFolderScenario"
            size="small"
            type="line"
            @change="loadScenarioList(false)"
          />
          <span style="margin-left: 8px; font-family: 'PingFang SC'; color: #323233">{{
            t('apiScenario.table.showChildrenModuleScenario')
          }}</span>
        </span>
      </div>
      <div class="flex items-center gap-[8px]">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('api_scenario.table.searchPlaceholder')"
          allow-clear
          class="mr-[8px] w-[240px]"
          @search="loadScenarioList(true)"
          @press-enter="loadScenarioList"
        />
        <a-button type="outline" class="arco-btn-outline--secondary !p-[8px]" @click="loadScenarioList(true)">
          <template #icon>
            <icon-refresh class="text-[var(--color-text-4)]" />
          </template>
        </a-button>
      </div>
    </div>
    <ms-base-table
      v-bind="propsRes"
      :action-config="batchActions"
      :first-column-width="44"
      no-disable
      filter-icon-align-left
      v-on="propsEvent"
      @selected-change="handleTableSelect"
      @batch-action="handleTableBatch"
    >
      <template #statusFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="statusFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <MsButton type="text" class="arco-btn-text--secondary ml-[10px]" @click="statusFilterVisible = true">
            {{ t(columnConfig.title as string) }}
            <icon-down :class="statusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </MsButton>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="statusFilters" direction="vertical" size="small">
                  <a-checkbox v-for="val of Object.values(ApiScenarioStatus)" :key="val" :value="val">
                    <apiStatus :status="val" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>
      <template #num="{ record }">
        <MsButton type="text" @click="openScenarioTab(record)">{{ record.num }}</MsButton>
      </template>
      <template #status="{ record }">
        <a-select
          v-model:model-value="record.status"
          class="param-input w-full"
          @change="() => handleStatusChange(record)"
        >
          <template #label>
            <apiStatus :status="record.status" />
          </template>
          <a-option v-for="item of Object.values(ApiScenarioStatus)" :key="item" :value="item">
            <apiStatus :status="item" />
          </a-option>
        </a-select>
      </template>
      <template #priority="{ record }">
        <a-select
          v-model:model-value="record.priority"
          :placeholder="t('common.pleaseSelect')"
          class="param-input w-full"
          @change="() => handlePriorityStatusChange(record)"
        >
          <template #label>
            <span class="text-[var(--color-text-2)]">
              <caseLevel :case-level="record.priority as CaseLevel" />
            </span>
          </template>
          <a-option v-for="item of scenarioPriorityList" :key="item.value" :value="item.value">
            <caseLevel :case-level="item.text as CaseLevel" />
          </a-option>
        </a-select>
      </template>
      <template #operation="{ record }">
        <MsButton
          v-permission="['PROJECT_API_SCENARIO:READ+EXECUTE']"
          type="text"
          class="!mr-0"
          @click="Message.info('// todo @ba1q1')"
        >
          {{ t('apiScenario.execute') }}
        </MsButton>
        <a-divider v-permission="['PROJECT_API_SCENARIO:READ+EXECUTE']" direction="vertical" :margin="8"></a-divider>
        <MsButton
          v-permission="['PROJECT_API_SCENARIO:READ+ADD']"
          type="text"
          class="!mr-0"
          @click="Message.info('// todo @ba1q1')"
        >
          {{ t('common.copy') }}
        </MsButton>
        <a-divider v-permission="['PROJECT_API_SCENARIO:READ+ADD']" direction="vertical" :margin="8"></a-divider>
        <MsTableMoreAction :list="tableMoreActionList" @select="handleTableMoreActionSelect($event, record)" />
      </template>
    </ms-base-table>
  </div>

  <a-modal v-model:visible="showBatchModal" title-align="start" class="ms-modal-upload ms-modal-medium" :width="480">
    <template #title>
      {{ t('common.batchEdit') }}
      <div class="text-[var(--color-text-4)]">
        {{
          t('api_scenario.table.batchModalSubTitle', {
            count: batchParams.currentSelectCount || tableSelected.length,
          })
        }}
      </div>
    </template>
    <a-form ref="batchFormRef" class="rounded-[4px]" :model="batchForm" layout="vertical">
      <a-form-item
        field="attr"
        :label="t('api_scenario.table.chooseAttr')"
        :rules="[{ required: true, message: t('api_scenario.table.attrRequired') }]"
        asterisk-position="end"
      >
        <a-select v-model="batchForm.attr" :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of fullAttrs" :key="item.value" :value="item.value">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        v-if="batchForm.attr === 'TAGS'"
        field="values"
        :label="t('api_scenario.table.batchUpdate')"
        :validate-trigger="['blur', 'input']"
        :rules="[{ required: true, message: t('api_scenario.table.valueRequired') }]"
        asterisk-position="end"
        class="mb-0"
        required
      >
        <MsTagsInput
          v-model:model-value="batchForm.values"
          placeholder="common.tagsInputPlaceholder"
          allow-clear
          unique-value
          retain-input-value
        />
      </a-form-item>
      <a-form-item
        v-else-if="batchForm.attr === 'Priority'"
        field="value"
        :label="t('api_scenario.table.batchUpdate')"
        :rules="[{ required: true, message: t('api_scenario.table.valueRequired') }]"
        asterisk-position="end"
        class="mb-0"
      >
        <a-select v-model="batchForm.value" :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of scenarioPriorityList" :key="item.value" :value="item.value">
            <caseLevel :case-level="item.text as CaseLevel" />
          </a-option>
        </a-select>
      </a-form-item>

      <a-form-item
        v-else
        field="value"
        :label="t('api_scenario.table.batchUpdate')"
        :rules="[{ required: true, message: t('api_scenario.table.valueRequired') }]"
        asterisk-position="end"
        class="mb-0"
      >
        <a-select v-model="batchForm.value" :placeholder="t('common.pleaseSelect')" :disabled="batchForm.attr === ''">
          <a-option v-for="item of valueOptions" :key="item.value" :value="item.value">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <template #footer>
      <a-button type="secondary" :disabled="batchUpdateLoading" @click="cancelBatch">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="primary" :loading="batchUpdateLoading" @click="batchUpdate">
        {{ t('common.update') }}
      </a-button>
    </template>
  </a-modal>
  <!--  </MsDialog>-->
  <a-modal
    v-model:visible="moveModalVisible"
    title-align="start"
    class="ms-modal-no-padding ms-modal-small"
    :mask-closable="false"
    :ok-text="
      t('api_scenario.table.batchMoveConfirm', {
        opt: batchOptionType,
        count: batchOptionScenarioCount,
      })
    "
    :ok-button-props="{ disabled: selectedBatchOptModuleKey === '' }"
    :cancel-button-props="{ disabled: scenarioBatchOptTreeLoading }"
    :on-before-ok="handleScenarioTreeOperation"
    @close="handleCancelScenarioTreeModal"
  >
    <template #title>
      <div class="flex items-center">
        <div v-if="isBatchCopy">
          {{ t('common.batchCopy') }}
          <div
            class="one-line-text ml-[4px] max-w-[100%] text-[var(--color-text-4)]"
            :title="t('api_scenario.table.batchModalSubTitle', { count: tableSelected.length })"
          >
            {{ t('api_scenario.table.batchModalSubTitle', { count: tableSelected.length }) }}
          </div>
        </div>
        <div v-else-if="isBatchMove">
          {{ t('common.batchMove') }}
          <div
            class="one-line-text ml-[4px] max-w-[100%] text-[var(--color-text-4)]"
            :title="t('api_scenario.table.batchModalSubTitle', { count: batchOptionScenarioCount })"
          >
            {{ t('api_scenario.table.batchModalSubTitle', { count: batchOptionScenarioCount }) }}
          </div>
        </div>
      </div>
    </template>
    <operationScenarioModuleTree
      v-if="moveModalVisible"
      :is-expand-all="true"
      :is-modal="true"
      :active-module="props.activeModule"
      @folder-node-select="folderNodeSelect"
    />
  </a-modal>
</template>

<script setup lang="ts">
  import { FormInstance, Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import operationScenarioModuleTree from '@/views/api-test/scenario/components/operationScenarioModuleTree.vue';

  import {
    batchEditScenario,
    batchOptionScenario,
    batchRecycleScenario,
    getScenarioPage,
    recycleScenario,
    updateScenario,
  } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  import { ApiScenarioDetail, ApiScenarioUpdateDTO } from '@/models/apiTest/scenario';
  import { ApiScenarioStatus } from '@/enums/apiEnum';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';

  const props = defineProps<{
    class?: string;
    activeModule: string;
    offspringIds: string[];
    readOnly?: boolean; // 是否是只读模式
  }>();
  // 场景属性
  const showItemFolderScenario = ref(false);

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();
  const scenarioPriorityList = ref([
    {
      value: 'P0',
      text: 'P0',
    },
    {
      value: 'P1',
      text: 'P1',
    },
    {
      value: 'P2',
      text: 'P2',
    },
    {
      value: 'P3',
      text: 'P3',
    },
  ]);
  const emit = defineEmits(['refreshModuleTree']);
  const keyword = ref('');
  const moveModalVisible = ref(false);
  const isBatchMove = ref(false); // 是否批量移动场景
  const isBatchCopy = ref(false); // 是否批量复制场景

  let columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      fixed: 'left',
      width: 126,
      showTooltip: true,
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiScenario.table.columns.name',
      dataIndex: 'name',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 134,
      showTooltip: true,
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiScenario.table.columns.level',
      dataIndex: 'priority',
      slotName: 'priority',
      width: 100,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.status',
      dataIndex: 'status',
      slotName: 'status',
      width: 140,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.runResult',
      titleSlotName: 'lastReportStatus',
      dataIndex: 'lastReportStatus',
      showTooltip: true,
      width: 100,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.tags',
      dataIndex: 'tags',
      isTag: true,
      isStringTag: true,
      width: 456,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.scenarioEnv',
      dataIndex: 'environmentName',
      width: 159,
    },
    {
      title: 'apiScenario.table.columns.steps',
      dataIndex: 'stepTotal',
      width: 100,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.passRate',
      dataIndex: 'requestPassRate',
      width: 100,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.module',
      dataIndex: 'modulePath',
      width: 176,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.createTime',
      dataIndex: 'createTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 189,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.updateTime',
      dataIndex: 'updateTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 189,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.createUser',
      dataIndex: 'createUserName',
      titleSlotName: 'createUser',
      width: 109,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.updateUser',
      dataIndex: 'updateUserName',
      titleSlotName: 'updateUser',
      width: 109,
      showDrag: true,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 180,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getScenarioPage,
    {
      columns: props.readOnly ? columns : [],
      scroll: { x: '100%' },
      tableKey: props.readOnly ? undefined : TableKeyEnum.API_TEST,
      showSetting: !props.readOnly,
      selectable: true,
      showSelectAll: !props.readOnly,
      draggable: props.readOnly ? undefined : { type: 'handle', width: 32 },
      heightUsed: 374,
    },
    (item) => ({
      ...item,
      createTime: dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss'),
      updateTime: dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );
  const batchActions = {
    baseAction: [
      {
        label: 'common.edit',
        eventTag: 'edit',
        permission: ['PROJECT_API_SCENARIO:READ+UPDATE'],
      },
      {
        label: 'apiScenario.execute',
        eventTag: 'execute',
        permission: ['PROJECT_API_SCENARIO:READ+EXECUTE'],
      },
      {
        label: 'msTable.batch.moveTo',
        eventTag: 'moveTo',
        permission: ['PROJECT_API_SCENARIO:READ+UPDATE'],
      },
      {
        label: 'msTable.batch.copyTo',
        eventTag: 'copyTo',
        permission: ['PROJECT_API_SCENARIO:READ+ADD'],
      },
    ],
    moreAction: [
      {
        label: 'common.delete',
        eventTag: 'delete',
        permission: ['PROJECT_API_SCENARIO:READ+DELETE'],
        danger: true,
      },
    ],
  };
  const tableMoreActionList = [
    {
      eventTag: 'delete',
      label: t('common.delete'),
      permission: ['PROJECT_API_SCENARIO:READ+DELETE'],
      danger: true,
    },
  ];

  const statusFilterVisible = ref(false);
  const statusFilters = ref(Object.keys(ApiScenarioStatus));
  const moduleIds = computed(() => {
    if (props.activeModule === 'all') {
      return [];
    }
    if (showItemFolderScenario.value) {
      return [props.activeModule, ...props.offspringIds];
    }
    return [props.activeModule];
  });

  function loadScenarioList(refreshTreeCount?: boolean) {
    const params = {
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleIds: moduleIds.value,
      filter: {
        status: statusFilters.value.length === Object.keys(ApiScenarioStatus).length ? undefined : statusFilters.value,
      },
    };
    setLoadListParams(params);
    loadList();
    if (refreshTreeCount) {
      emit('refreshModuleTree', params);
    }
  }

  function handleFilterHidden(val: boolean) {
    if (!val) {
      loadScenarioList(false);
    }
  }

  async function handleStatusChange(record: ApiScenarioUpdateDTO) {
    try {
      await updateScenario({
        id: record.id,
        status: record.status,
      });
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function handlePriorityStatusChange(record: ApiScenarioUpdateDTO) {
    try {
      await updateScenario({
        id: record.id,
        priority: record.priority,
      });
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const tableSelected = ref<(string | number)[]>([]);

  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  /**
   * 删除接口
   */
  function deleteScenario(record?: ApiScenarioDetail, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('api_scenario.table.deleteScenarioTipTitle', { name: record?.name });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('api_scenario.table.batchDeleteScenarioTip', {
        count: params?.currentSelectCount || tableSelected.value.length,
      });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'error',
      title,
      content: t('api_scenario.table.deleteScenarioTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          if (isBatch) {
            await batchRecycleScenario({
              selectIds,
              selectAll: !!params?.selectAll,
              excludeIds: params?.excludeIds || [],
              condition: { keyword: keyword.value },
              projectId: appStore.currentProjectId,
              moduleIds: props.activeModule === 'all' ? [] : [props.activeModule],
              deleteAll: true,
            });
          } else {
            await recycleScenario(record?.id as string);
          }
          Message.success(t('common.deleteSuccess'));
          resetSelector();
          loadScenarioList(true);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleTableMoreActionSelect(item: ActionsItem, record: ApiScenarioDetail) {
    switch (item.eventTag) {
      case 'delete':
        deleteScenario(record);
        break;
      default:
        break;
    }
  }

  /**
   * 处理表格选中
   */
  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
  }

  const showBatchModal = ref(false);
  const batchUpdateLoading = ref(false);
  const batchFormRef = ref<FormInstance>();
  const batchForm = ref({
    attr: '',
    value: '',
    values: [],
  });
  const fullAttrs = [
    {
      name: 'apiScenario.table.columns.level',
      value: 'Priority',
    },
    {
      name: 'api_scenario.table.apiStatus',
      value: 'Status',
    },
    {
      name: '标签（待定）',
      value: 'Tags',
    },
    {
      name: '环境（待定）',
      value: 'Environment',
    },
  ];

  const valueOptions = computed(() => {
    switch (batchForm.value.attr) {
      case 'Status':
        return [
          {
            name: 'api_scenario.table.status.underway',
            value: ApiScenarioStatus.UNDERWAY,
          },
          {
            name: 'api_scenario.table.status.completed',
            value: ApiScenarioStatus.COMPLETED,
          },
          {
            name: 'api_scenario.table.status.deprecate',
            value: ApiScenarioStatus.DEPRECATED,
          },
        ];
      default:
        return [];
    }
  });

  function cancelBatch() {
    showBatchModal.value = false;
    batchFormRef.value?.resetFields();
    batchForm.value = {
      attr: '',
      value: '',
      values: [],
    };
  }

  function batchUpdate() {
    batchFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          batchUpdateLoading.value = true;

          // value: 'PRIORITY',
          //    value: 'STATUS',
          const batchEditParam = {
            selectIds: batchParams.value?.selectedIds || [],
            selectAll: !!batchParams.value?.selectAll,
            excludeIds: batchParams.value?.excludeIds || [],
            condition: { keyword: keyword.value },
            projectId: appStore.currentProjectId,
            moduleIds: props.activeModule === 'all' ? [] : [props.activeModule],
            type: batchForm.value?.attr,
            priority: '',
            status: '',
          };

          if (batchForm.value.attr === 'Priority') {
            batchEditParam.priority = batchForm.value.value;
          } else if (batchForm.value.attr === 'Status') {
            batchEditParam.status = batchForm.value.value;
          }

          await batchEditScenario(batchEditParam);
          Message.success(t('common.updateSuccess'));
          cancelBatch();
          resetSelector();
          loadScenarioList(true);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          batchUpdateLoading.value = false;
        }
      }
    });
  }

  const selectedBatchOptModuleKey = ref(''); // 移动文件选中节点
  const selectedBatchOptModuleName = ref(''); // 移动文件选中节点  用于页面文案显示
  const batchOptionType = ref(''); // 批量操作类型  用于页面提示语
  const batchOptionScenarioCount = ref<number>(0);
  const activeScenario = ref<ApiScenarioDetail | null>(null); // 当前查看的接口项
  const scenarioBatchOptTreeLoading = ref(false); // 批量移动文件loading

  /**
   * 批量接口
   */
  async function handleScenarioTreeOperation() {
    try {
      scenarioBatchOptTreeLoading.value = true;
      let optionType = '';
      if (isBatchMove.value) {
        optionType = 'batchMove';
      } else if (isBatchCopy.value) {
        optionType = 'batchCopy';
      }
      await batchOptionScenario(optionType, {
        selectIds: batchParams.value?.selectedIds || [],
        selectAll: !!batchParams.value?.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        condition: { keyword: keyword.value },
        projectId: appStore.currentProjectId,
        moduleIds: props.activeModule === 'all' ? [] : [props.activeModule],
        targetModuleId: selectedBatchOptModuleKey.value,
      });

      Message.success(
        t('api_scenario.batch_operation.success', {
          opt: batchOptionType.value,
          name: selectedBatchOptModuleName?.value,
        })
      );
      tableSelected.value = [];
      activeScenario.value = null;
      loadScenarioList(true);
      resetSelector();
    } catch (error) {
      console.log(error);
    } finally {
      scenarioBatchOptTreeLoading.value = false;
    }
  }

  function handleCancelScenarioTreeModal() {
    moveModalVisible.value = false;
    isBatchMove.value = false;
    isBatchCopy.value = false;
    selectedBatchOptModuleKey.value = '';
    selectedBatchOptModuleName.value = '';
    batchOptionType.value = '';
    batchOptionScenarioCount.value = 0;
  }

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(node: MsTreeNodeData) {
    selectedBatchOptModuleKey.value = node.id;
    if (node.name != null) {
      selectedBatchOptModuleName.value = node.name;
    }
  }

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = params;
    switch (event.eventTag) {
      case 'delete':
        deleteScenario(undefined, true, batchParams.value);
        break;
      case 'edit':
        showBatchModal.value = true;
        break;
      case 'moveTo':
        batchOptionType.value = t('common.move');
        if (params.currentSelectCount != null) {
          batchOptionScenarioCount.value = params.currentSelectCount;
        }
        isBatchMove.value = true;
        moveModalVisible.value = true;
        break;
      case 'copyTo':
        batchOptionType.value = t('common.copy');
        if (params.currentSelectCount != null) {
          batchOptionScenarioCount.value = params.currentSelectCount;
        }
        isBatchCopy.value = true;
        moveModalVisible.value = true;
        break;
      case 'execute':
        Message.info('// todo @ba1q1');
        break;
      default:
        break;
    }
  }

  function openScenarioTab(record: ApiScenarioDetail) {
    Message.info('// todo @ba1q1');
  }

  defineExpose({
    loadScenarioList,
  });

  if (!props.readOnly) {
    const tableStore = useTableStore();
    await tableStore.initColumn(TableKeyEnum.API_TEST, columns, 'drawer', true);
  } else {
    columns = columns.filter(
      (item) => !['version', 'createTime', 'updateTime', 'operation'].includes(item.dataIndex as string)
    );
  }

  onBeforeMount(() => {
    loadScenarioList();
  });
  watch(
    () => props.activeModule,
    () => {
      resetSelector();
      loadScenarioList();
    }
  );
  watch(
    () => batchForm.value.attr,
    () => {
      batchForm.value.value = '';
    }
  );
</script>

<style lang="less" scoped>
  :deep(.param-input:not(.arco-input-focus, .arco-select-view-focus)) {
    &:not(:hover) {
      border-color: transparent !important;
      .arco-input::placeholder {
        @apply invisible;
      }
      .arco-select-view-icon {
        @apply invisible;
      }
      .arco-select-view-value {
        color: var(--color-text-brand);
      }
    }
  }
</style>
