<template>
  <a-alert v-if="isShowTip" class="mb-6" type="warning">
    <div class="flex items-start justify-between">
      <span class="w-[80%]">{{ t('system.orgTemplate.workFlowTip') }}</span>
      <span class="cursor-pointer text-[var(--color-text-2)]" @click="noRemindHandler">{{
        t('system.orgTemplate.noReminders')
      }}</span>
    </div>
  </a-alert>
  <div class="mb-4">
    <div class="mb-4 flex items-center"
      ><a-button
        v-if="!isEnableProjectState"
        v-permission="props.createPermission"
        class="mr-2"
        type="outline"
        @click="addStatus"
        >{{ t('system.orgTemplate.addState') }}</a-button
      >
      <span v-else class="mr-2 font-medium text-[var(--color-text-1)]">工作流</span>
      <a-popover title="" position="right">
        <MsButton class="!mr-1">{{ t('system.orgTemplate.example') }}</MsButton>
        <template #content>
          <div class="w-[410px] bg-[var(--color-bg-3)] p-1">
            <img src="@/assets/images/schematicDrawing.png" alt="" />
          </div>
        </template>
      </a-popover>
    </div>
    <a-table
      :columns="workFlowColumns"
      :data="dataList"
      row-key="id"
      :bordered="{ cell: true }"
      :pagination="false"
      :scroll="{ x: 'auto' }"
      :draggable="{ type: 'handle', width: 39 }"
      :loading="tableLoading"
      @change="handleChange"
    >
      <template #columns>
        <a-table-column
          v-for="column in workFlowColumns"
          :key="column.dataIndex"
          :data-index="column.dataIndex"
          :title="(column?.title as string)"
          :header-cell-class="column.headerCellClass"
          :fixed="column.fixed"
        >
          <template #title>
            <div v-if="column.dataIndex !== 'statusName'" class="w-full">
              <MsTag class="relative" size="large" theme="light">{{ column.title }} </MsTag></div
            >
            <div v-else class="splitBox">
              <div class="startStatus font-normal"> {{ t('system.orgTemplate.startState') }} </div>
              <div class="line"></div>
              <div class="endStatus font-normal"> {{ t('system.orgTemplate.flowState') }} </div>
            </div>
          </template>
          <template #cell="{ record }">
            <div v-if="column.dataIndex === 'statusName'">
              <div class="flex items-center justify-between">
                <div class="relative">
                  <MsTag class="relative font-normal" size="large" theme="light">{{ record.name }}</MsTag>
                  <span v-if="record.statusDefinitions.join().includes('START')" class="absolute -top-6 left-7">
                    <svg-icon width="36px" height="36px" class="inline-block text-[white]" name="start"></svg-icon
                  ></span>
                </div>

                <MsTableMoreAction
                  v-if="!isEnableProjectState"
                  class="mr-2"
                  :list="getMoreActions(record)"
                  @select="(item) => handleMoreActionSelect(item, record)"
                ></MsTableMoreAction>
              </div>
            </div>
            <div v-else class="!h-[82px] min-w-[116px] p-[2px]">
              <WorkflowCard
                :mode="props.mode"
                :column-item="column"
                :state-item="record"
                :cell-coordinates="cellCoordinates"
                :total-data="dataList"
                :delete-permission="props.deletePermission"
                :update-permission="props.updatePermission"
                :create-permission="props.createPermission"
                @click="selectCard(record, column.dataIndex)"
                @ok="getWorkFetchList()"
              />
            </div>
          </template>
        </a-table-column>
        <a-table-column
          :title="t('system.orgTemplate.operation')"
          :width="260"
          header-cell-class="splitOperation"
          fixed="right"
        >
          <template #cell="{ record }">
            <div class="ml-4 flex items-center">
              <MsButton
                v-if="!isEnableProjectState"
                v-permission="props.updatePermission"
                class="!mr-0 ml-4"
                @click="editWorkStatus(record)"
                >{{ t('common.edit') }}</MsButton
              >
              <a-divider
                v-if="!isEnableProjectState"
                v-permission="props.updatePermission"
                class="h-[12px]"
                direction="vertical"
              />
              <a-checkbox
                v-if="!isEnableProjectState"
                v-model="record.currentState"
                :disabled="!hasAnyPermission(props.updatePermission || [])"
                @change="(value) => changeState(value, record)"
              >
                <MsButton v-permission="props.updatePermission" class="!mr-0">{{
                  t('system.orgTemplate.endState')
                }}</MsButton></a-checkbox
              >
              <a-divider v-if="!isEnableProjectState" class="h-[12px]" direction="vertical" />
              <MsButton class="!mr-0" @click="detailWorkStatus(record)">{{ t('system.orgTemplate.details') }}</MsButton>
            </div>
          </template>
        </a-table-column>
      </template>
    </a-table>
    <div class="mt-4 flex items-center text-[var(--color-text-4)]">
      <span>tips: </span>
      <MsIcon type="icon-icon_drag" class="mx-4 text-[16px] text-[var(--color-text-4)]" />
      <span>{{ t('system.orgTemplate.iconTip') }}</span>
      <a-popover title="" position="right">
        <MsButton class="!mr-0 ml-2">{{ t('system.orgTemplate.example') }}</MsButton>
        <template #content>
          <div class="bg-[var(--color-bg-3)]">
            <img src="@/assets/images/colorSelect.png" alt="" />
          </div>
        </template>
      </a-popover>
    </div>
    <AddWorkStatusModal
      ref="addWorkStateRef"
      v-model:visible="showModel"
      :mode="props.mode"
      @success="getWorkFetchList()"
    />
    <MsDrawer
      ref="detailDrawerRef"
      v-model:visible="showDetailVisible"
      :width="480"
      :footer="false"
      :title="t('system.orgTemplate.stateDetail', { name: detailInfo?.name })"
    >
      <div class="p-4">
        <div class="flex">
          <span class="label">{{ t('system.orgTemplate.stateName') }}</span>
          <a-tooltip :content="detailInfo?.name" mini position="lt">
            <span class="content ellipsis">{{ detailInfo?.name }}</span>
          </a-tooltip>
        </div>
        <div class="flex">
          <span class="label">{{ t('common.desc') }}</span>
          <a-tooltip mini :content="detailInfo?.remark" position="lt">
            <span class="content ellipsis">{{ detailInfo?.remark || '-' }}</span>
          </a-tooltip>
        </div>
      </div>
    </MsDrawer>
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 模板-工作流table
   */
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { Message, TableColumnData, TableData } from '@arco-design/web-vue';
  import { isEqual } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import AddWorkStatusModal from '@/views/setting/organization/template/components/addWorkStatusModal.vue';
  import WorkflowCard from '@/views/setting/organization/template/components/workflowCard.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useVisit from '@/hooks/useVisit';
  import { useAppStore } from '@/store';
  import useTemplateStore from '@/store/modules/setting/template';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { SetStateType, WorkFlowType } from '@/models/setting/template';

  import { getWorkFlowRequestApi } from '@/views/setting/organization/template/components/fieldSetting';

  const { t } = useI18n();
  const appStore = useAppStore();

  const templateStore = useTemplateStore();

  const props = defineProps<{
    mode: 'organization' | 'project'; // 组织 || 项目
    deletePermission?: string[];
    createPermission?: string[];
    updatePermission?: string[];
  }>();

  const currentOrgId = computed(() => appStore.currentOrgId);
  const currentProjectId = computed(() => appStore.currentProjectId);
  const { openModal } = useModal();
  const route = useRoute();

  const defaultStatusColumn: TableColumnData = {
    title: '',
    dataIndex: 'statusName',
    fixed: 'left',
    width: 196,
    headerCellClass: 'splitTitle',
  };

  // 计算是否禁用状态
  const isEnableProjectState = computed(() => {
    return props.mode === 'organization'
      ? templateStore.projectStatus[route.query.type as string]
      : !templateStore.projectStatus[route.query.type as string];
  });
  const dataList = ref<WorkFlowType[]>([]);

  // 获取的状态流数据
  const workData = ref<WorkFlowType[]>([]);

  // 处理表格列数据'
  const workFlowColumns = ref<TableColumnData[]>([]);

  function getMoreActions(record: WorkFlowType) {
    const moreActions: ActionsItem[] = [
      {
        label: 'system.orgTemplate.setInitState',
        eventTag: 'setInit',
        disabled: record.statusDefinitions.join().includes('START'),
        permission: props.updatePermission,
      },
      {
        isDivider: true,
      },
      {
        label: 'system.orgTemplate.delete',
        eventTag: 'delete',
        danger: true,
        permission: props.updatePermission,
      },
    ];
    return moreActions;
  }

  const tableLoading = ref<boolean>(false);

  const scopedId = computed(() => (props.mode === 'organization' ? currentOrgId.value : currentProjectId.value));

  const getWorkList = getWorkFlowRequestApi(props.mode).list;

  // 获取table列表
  async function getWorkFetchList() {
    try {
      tableLoading.value = true;
      workData.value = await getWorkList(scopedId.value, route.query.type);
      workFlowColumns.value = workData.value.map((item) => {
        const columns = {
          title: item.name,
          dataIndex: item.id,
        };
        return columns;
      });
      workFlowColumns.value.splice(0, 0, defaultStatusColumn);

      dataList.value = workData.value.map((item, index) => {
        if (index === 0) {
          return {
            statusName: item.name,
            ...item,
            [item.id]: item.name,
            index,
            currentState: item.statusDefinitions.join().includes('END'),
          };
        }
        return {
          ...item,
          [item.id]: item.name,
          index,
          currentState: item.statusDefinitions.join().includes('END'),
        };
      });
      console.log(dataList.value);
    } catch (error) {
      console.log(error);
    } finally {
      tableLoading.value = false;
    }
  }
  const addWorkStateRef = ref();
  const showModel = ref<boolean>(false);

  // 编辑工作状态
  function editWorkStatus(record: WorkFlowType) {
    showModel.value = true;
    addWorkStateRef.value.handleEdit(record);
  }

  // 添加状态
  function addStatus() {
    showModel.value = true;
  }
  const deleteState = getWorkFlowRequestApi(props.mode).delete;
  // 删除状态
  function deleteHandler(record: WorkFlowType) {
    if (record.statusDefinitions.join().includes('START')) {
      Message.warning(t('system.orgTemplate.noAllowDeleteInitState'));
      return;
    }
    openModal({
      type: 'error',
      title: t('system.orgTemplate.deleteStateTitle', { name: characterLimit(record.name) }),
      content: t('system.orgTemplate.deleteStateContent'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          if (record.id) await deleteState(record.id);
          Message.success(t('system.orgTemplate.deleteSuccess'));
          getWorkFetchList();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const setInitAndEndState = getWorkFlowRequestApi(props.mode).changeState;
  // 设置初始状态|| 设置结束状态
  async function setState(record: WorkFlowType, type: string) {
    const params: SetStateType = {
      statusId: record.id,
      definitionId: type,
      enable: type === 'START' ? true : record.currentState,
    };
    try {
      await setInitAndEndState(params);
      Message.success(
        type === 'END' ? t('system.orgTemplate.setEndStateSuccess') : t('system.orgTemplate.setInitStateSuccess')
      );
      getWorkFetchList();
    } catch (error) {
      console.log(error);
    }
  }
  function handleMoreActionSelect(item: ActionsItem, record: WorkFlowType) {
    if (item.eventTag === 'delete') {
      deleteHandler(record);
    } else {
      setState(record, 'START');
    }
  }

  const dragChangeRequest = getWorkFlowRequestApi(props.mode).dragChange;
  // 表格拖拽改变回调
  async function handleChange(_data: TableData[]) {
    if (!hasAnyPermission(props.updatePermission || [])) {
      return;
    }
    const originIds = dataList.value.map((item: any) => item.id);
    dataList.value = _data as WorkFlowType[];
    const dataIds = _data.map((item: any) => item.id);
    const isChange = isEqual(originIds, dataIds);
    if (isChange) return false;
    try {
      await dragChangeRequest(scopedId.value, route.query.type, dataIds);
      Message.success(t('common.updateSuccess'));
      getWorkFetchList();
    } catch (error) {
      console.log(error);
    }
  }

  // 更改状态为结束&非结束
  function changeState(value: any, record: WorkFlowType) {
    setState(record, 'END');
  }

  // 存储当前选中状态card 坐标
  const cellCoordinates = ref<{ rowId: string; columnId: string }>({
    rowId: '',
    columnId: '',
  });

  // 清空选中状态
  function selectCard(record: WorkFlowType, columnDataIndex: string | undefined) {
    if (isEnableProjectState.value) {
      return;
    }
    cellCoordinates.value = {
      rowId: '',
      columnId: '',
    };
    cellCoordinates.value = {
      rowId: record.id,
      columnId: columnDataIndex || '',
    };
  }

  const showDetailVisible = ref<boolean>(false);
  const detailInfo = ref();

  // 详情
  function detailWorkStatus(record: WorkFlowType) {
    showDetailVisible.value = true;
    detailInfo.value = { ...record };
  }

  const visitedKey = 'notRemindWorkFlowTip';
  const { addVisited } = useVisit(visitedKey);
  const { getIsVisited } = useVisit(visitedKey);
  const isShowTip = ref<boolean>(true);

  // 不再提示
  const noRemindHandler = () => {
    isShowTip.value = false;
    addVisited();
  };

  const doCheckIsTip = () => {
    isShowTip.value = !getIsVisited();
  };

  onBeforeMount(() => {
    getWorkFetchList();
    doCheckIsTip();
  });
</script>

<style scoped lang="less">
  :deep(.arco-table-border) {
    border: 1px solid var(--color-text-n8) !important;
  }
  :deep(.arco-table .arco-table-td) {
    height: 82px;
  }
  :deep(.arco-table-tr .arco-table-th) {
    height: 82px;
    color: var(--color-text-3);
  }
  :deep(.arco-table .arco-table-cell) {
    padding: 0 !important;
  }
  :deep(.arco-table-cell-align-left) {
    justify-content: center;
    text-align: center;
  }
  :deep(.arco-table-cell-align-left):last-of-type {
    justify-content: center;
    text-align: center;
  }
  :deep(.arco-table-th.splitTitle .arco-table-cell-align-left) {
    justify-content: start;
  }
  :deep(.arco-table-th.splitOperation .arco-table-cell-align-left) {
    justify-content: start;
    .arco-table-th-title {
      padding-left: 16px !important;
    }
  }
  :deep(.arco-table-tr) {
    .splitTitle {
      width: 196px !important;
      min-width: 196px !important;
    }
  }
  :deep(.arco-table-drag-handle) {
    border-right: none !important;
    .arco-icon-drag-dot-vertical {
      color: var(--color-text-brand);
    }
    &:hover + .arco-table-td .action {
      background: rgb(var(--primary-9));
      opacity: 1;
      transition: 0.1;
    }
    & + .arco-table-td:hover .action {
      background: rgb(var(--primary-9));
      opacity: 1;
      transition: 0.1;
    }
  }
  :deep(.arco-table-operation) {
    border-right: none;
  }
  .startStatus {
    position: absolute;
    bottom: 16px;
    left: -24px;
  }
  .endStatus {
    position: absolute;
    top: 16px;
    right: 20px;
  }
  .line {
    position: absolute;
    left: -45px;
    width: 128%;
    height: 1px;
    background: var(--color-text-n8);
    transform: rotateZ(19deg);
  }
  .label {
    margin-top: 16px;
    width: 120px;
    flex-shrink: 0;
    text-align: left;
    color: var(--color-text-3);
  }
  .content {
    margin-top: 16px;
    flex: 1;
    color: var(--color-text-1);
    @apply inline-block;
  }
  .ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  :deep(.arco-table-border) {
    border-right: none !important;
    border-bottom: none !important;
  }
  :deep(.arco-table-tr):hover {
    .arco-table-td:not(.arco-table-col-fixed-right) {
      background: transparent !important;
    }
    .arco-table-td.arco-table-col-fixed-right::before {
      background: transparent !important;
    }
  }
</style>
