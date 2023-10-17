<template>
  <MsCard ref="fullRef" :special-height="132" :is-fullscreen="isFullscreen" simple>
    <div id="mscard">
      <div class="mb-[16px] flex items-center justify-between">
        <div class="font-medium text-[var(--color-text-000)]">{{ t('project.messageManagement.config') }}</div>
        <div>
          <MsSelect
            v-model:model-value="robotFilters"
            :options="robotOptions"
            :allow-search="false"
            class="mr-[8px] w-[240px]"
            :prefix="t('project.messageManagement.robot')"
            value-key="id"
            :multiple="true"
            :has-all-select="true"
            :default-all-select="true"
            :popup-container="isFullscreen ? '#mscard' : undefined"
          >
            <template #footer>
              <div class="mb-[6px] mt-[4px] p-[3px_8px]">
                <MsButton type="text" @click="emit('createRobot')">
                  <MsIcon type="icon-icon_add_outlined" class="mr-[8px] text-[rgb(var(--primary-6))]" size="14" />
                  {{ t('project.messageManagement.createBot') }}
                </MsButton>
              </div>
            </template>
          </MsSelect>
          <a-button type="outline" class="arco-btn-outline--secondary px-[5px]" @click="toggle">
            <template #icon>
              <MsIcon
                :type="isFullscreen ? 'icon-icon_off_screen' : 'icon-icon_full_screen_one'"
                class="text-[var(--color-text-4)]"
                size="14"
              />
            </template>
            {{ t(isFullscreen ? 'common.offFullScreen' : 'common.fullScreen') }}
          </a-button>
        </div>
      </div>
      <ms-base-table
        ref="tableRef"
        v-bind="propsRes"
        v-model:expandedKeys="expandedKeys"
        no-disable
        span-all
        :indent-size="0"
        v-on="propsEvent"
      >
        <template #name="{ record }">
          <span class="font-medium text-[var(--color-text-1)]">{{ record.name }}</span>
        </template>
        <template #eventName="{ record }">
          <span>{{ record.eventName || '' }}</span>
        </template>
        <template #receiver="{ record, dataIndex }">
          <MsSelect
            v-if="!record.children"
            :id="`${record.taskType}-${record.event}`"
            v-model:model-value="record.receivers"
            v-model:loading="record.loading"
            mode="remote"
            :options="defaultReceivers"
            :search-keys="['label']"
            allow-search
            value-key="id"
            label-key="name"
            :multiple="true"
            :placeholder="t('project.messageManagement.receiverPlaceholder')"
            :remote-extra-params="{ projectId: appStore.currentProjectId }"
            :remote-func="getMessageUserList"
            :remote-fields-map="{ label: 'name', value: 'id', id: 'id' }"
            :not-auto-init-search="true"
            :popup-container="isFullscreen ? '#mscard' : undefined"
            :fallback-option="(val) => ({
              label: (val as Record<string, any>).name,
              value: val,
            })"
            @popup-visible-change="changeMessageReceivers($event, record, dataIndex as string)"
          />
          <span v-else></span>
        </template>
        <template #robot="{ record, dataIndex }">
          <div v-if="!record.children && record.projectRobotConfigMap?.[dataIndex as string]" class="flex items-center">
            <a-switch
              v-model:model-value="record.projectRobotConfigMap[dataIndex as string].enable"
              :before-change="(val) => handleChangeIntercept(!!val, record, dataIndex as string)"
              size="small"
            />
            <a-popover position="right" :popup-container="isFullscreen ? '#mscard' : undefined">
              <div
                class="ml-[8px] mr-[4px] cursor-pointer text-[var(--color-text-1)] hover:text-[rgb(var(--primary-6))]"
              >
                {{ t('common.preview') }}
              </div>
              <template #content>
                <MessagePreview
                  :robot="record.projectRobotConfigMap[dataIndex as string]"
                  :function-name="record.functionName"
                  :event-name="record.eventName"
                />
              </template>
            </a-popover>
            <MsButton type="button" @click="editRobot(record, dataIndex as string)">{{ t('common.setting') }}</MsButton>
          </div>
          <span v-else></span>
        </template>
      </ms-base-table>
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref, watch } from 'vue';
  import { useRouter } from 'vue-router';
  import { useFullscreen } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsSelect from '@/components/business/ms-select';
  import MessagePreview from './messagePreview.vue';

  import {
    getMessageList,
    getMessageUserList,
    getRobotList,
    saveMessageConfig,
  } from '@/api/modules/project-management/messageManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { MessageItem, ProjectRobotConfig, Receiver, RobotItem } from '@/models/projectManagement/message';
  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';

  import type { SelectOptionData, TableColumnData, TableData } from '@arco-design/web-vue';
  import type { TableOperationColumn } from '@arco-design/web-vue/es/table/interface';

  const emit = defineEmits(['createRobot']);

  const router = useRouter();
  const appStore = useAppStore();
  const { t } = useI18n();

  const robotFilters = ref<RobotItem[]>([]);
  const robotOptions = ref<(SelectOptionData & RobotItem)[]>([]);
  const fullRef = ref<HTMLElement | null>();

  const { isFullscreen, toggle } = useFullscreen(fullRef);

  const tableRef = ref<InstanceType<typeof MsBaseTable> | null>(null);
  const staticColumns: MsTableColumn = [
    {
      title: 'project.messageManagement.function',
      dataIndex: 'name',
      slotName: 'name',
      width: 150,
      fixed: 'left',
      headerCellClass: 'pl-[16px]',
      bodyCellClass: (record) => {
        if (record.children) {
          return '';
        }
        return 'border-r border-[var(--color-text-n8)]';
      },
    },
    {
      title: 'project.messageManagement.noticeEvent',
      dataIndex: 'eventName',
      slotName: 'eventName',
      width: 150,
      fixed: 'left',
    },
    {
      title: 'project.messageManagement.receiver',
      dataIndex: 'receiver',
      slotName: 'receiver',
      width: 208,
      fixed: 'left',
    },
  ];

  // 根据机器人筛选器动态生成表格列
  const columns = computed(() => {
    if (robotFilters.value.length === 0) {
      return staticColumns;
    }
    const tempArr = [...staticColumns];
    for (let i = 0; i < robotFilters.value.length; i++) {
      const robotId = robotFilters.value[i].id;
      tempArr.push({
        title: robotOptions.value.find((e) => e.id === robotId)?.label,
        dataIndex: robotId,
        slotName: 'robot',
        width: 180,
      });
    }
    return tempArr;
  });

  watch(
    () => columns.value,
    (arr) => {
      tableRef.value?.initColumn(arr);
    }
  );

  const expandedKeys = ref<string[]>([]);

  interface TableMessageChildrenItem {
    functionName: string;
    taskType: string;
    name: string;
    rowspan?: number;
    projectRobotConfigMap: Record<string, ProjectRobotConfig>;
    [key: string]: any;
  }

  interface TableMessageItem {
    name: string;
    type: string;
    children?: TableMessageChildrenItem[];
  }

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    getMessageList,
    {
      columns: columns.value as MsTableColumn,
      scroll: { x: 'auto' },
      showPagination: false,
      hoverable: false,
      showExpand: true,
      heightUsed: 50,
      rowKey: 'key',
      rowClass: (record: TableMessageItem) => {
        if (record.children) {
          return 'gray-td-bg';
        }
      },
    },
    (item) => {
      const children = [];
      for (let i = 0; i < (item as unknown as MessageItem).messageTaskTypeDTOList.length; i++) {
        const child = (item as unknown as MessageItem).messageTaskTypeDTOList[i];
        for (let j = 0; j < child.messageTaskDetailDTOList.length; j++) {
          const grandson = child.messageTaskDetailDTOList[j];
          if (grandson.projectRobotConfigMap) {
            const firstRobot = Object.values(grandson.projectRobotConfigMap)[0];
            const openRobotKeys = Object.keys(grandson.projectRobotConfigMap); // 当前消息配置已经配置的机器人
            robotOptions.value.forEach((e) => {
              if (!openRobotKeys.includes(e.id)) {
                // 如果当前机器人未配置，则需要添加默认机器人信息到列表数据内
                grandson.projectRobotConfigMap[e.id] = {
                  ...e,
                  dingType: e.type,
                  robotId: e.id,
                  robotName: e.name,
                  enable: false,
                  previewTemplate: firstRobot.previewTemplate, // 消息配置机器人预览模版
                  template: firstRobot.defaultTemplate, // 消息配置机器人发送模版
                  defaultTemplate: firstRobot.defaultTemplate, // 消息配置机器人默认发送模版
                  useDefaultTemplate: true, // 消息配置机器人是否使用默认模版
                  previewSubject: firstRobot.previewSubject, // 消息模版配置的标题
                  subject: firstRobot.defaultSubject, // 消息模版配置的标题
                  defaultSubject: firstRobot.defaultSubject, // 消息模版配置的默认标题
                  useDefaultSubject: true, // 消息模版是否使用默认标题
                };
              }
            });
          }
          children.push({
            key: `${(item as unknown as MessageItem).type}-${child.taskType}-${grandson.event}`,
            functionName: (item as unknown as MessageItem).name,
            taskType: child.taskType,
            name: child.taskTypeName,
            rowspan: child.messageTaskDetailDTOList.length,
            ...grandson,
          });
        }
      }
      return {
        key: (item as unknown as MessageItem).type,
        name: (item as unknown as MessageItem).name,
        type: (item as unknown as MessageItem).type,
        loading: false,
        hasSubtree: true,
        children,
      };
    }
  );

  function spanMethod(data: {
    record: TableData;
    column: TableColumnData | TableOperationColumn;
    rowIndex: number;
    columnIndex: number;
  }): { rowspan?: number; colspan?: number } | void {
    const { record, columnIndex } = data;
    if (record.rowspan && columnIndex === 0) {
      return {
        rowspan: record.rowspan,
      };
    }
  }

  const defaultReceivers = ref<SelectOptionData[]>([]);

  async function initReceivers() {
    const res = await getMessageUserList({ projectId: appStore.currentProjectId, keyword: '' });
    defaultReceivers.value = res.map((e) => ({
      label: e.name,
      ...e,
    }));
  }

  async function initRobotList() {
    const res = await getRobotList(appStore.currentProjectId);
    robotOptions.value = res
      .filter((e) => e.enable)
      .map((e) => ({
        label: e.name,
        ...e,
      }));
  }

  onBeforeMount(async () => {
    initReceivers();
    await initRobotList();
    setLoadListParams({ projectId: appStore.currentProjectId });
    loadList();
  });

  function editRobot(record: TableMessageChildrenItem, dataIndex: string) {
    router.push({
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT_EDIT,
      query: {
        id: record.projectRobotConfigMap?.[dataIndex].robotId,
        taskType: record.taskType,
        event: record.event,
      },
    });
  }

  /**
   * 消息配置机器人切换启用禁用
   * @param val 开启/关闭
   * @param record 操作行记录
   * @param dataIndex 操作的机器人列
   */
  async function handleChangeIntercept(val: boolean, record: TableMessageChildrenItem, dataIndex: string) {
    if (record.receivers.length === 0) {
      Message.warning(t('project.messageManagement.unsetReceiverTip'));
      return false;
    }
    try {
      await saveMessageConfig({
        ...record.projectRobotConfigMap?.[dataIndex],
        projectId: appStore.currentProjectId,
        taskType: record.taskType,
        event: record.event,
        receiverIds: record.receivers.map((e: Receiver) => (e.id !== undefined ? e.id : e)),
        enable: val, // 消息配置是否启用
      });
      Message.success(
        t('project.messageManagement.enableRobotSuccess', {
          name: `${record.functionName}-${record.eventName}-${record.projectRobotConfigMap?.[dataIndex].robotName}`,
        })
      );
      return true;
    } catch (error: any) {
      // eslint-disable-next-line no-console
      console.log(error);
      return false;
    }
  }

  async function changeMessageReceivers(val: boolean, record: TableMessageChildrenItem, dataIndex: string) {
    if (!val) {
      if (record.receivers.length === 0) {
        Message.warning(t('project.messageManagement.receiverNotNull'));
        return false;
      }
      try {
        record.loading = true;
        await saveMessageConfig({
          ...record.projectRobotConfigMap?.[dataIndex],
          projectId: appStore.currentProjectId,
          taskType: record.taskType,
          event: record.event,
          receiverIds: record.receivers.map((e: Receiver) => (e.id !== undefined ? e.id : e)),
        });
        Message.success(t('project.messageManagement.saveReceiverSuccess'));
      } catch (error: any) {
        // eslint-disable-next-line no-console
        console.log(error);
        loadList();
      } finally {
        record.loading = false;
      }
    }
  }
</script>

<style lang="less" scoped>
  :deep(.gray-td-bg) {
    td {
      background-color: var(--color-text-n9);
    }
  }
</style>
