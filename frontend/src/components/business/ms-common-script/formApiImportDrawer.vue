<template>
  <MsDrawer
    v-model:visible="exportScriptDrawer"
    :title="t('project.code_segment.importApiTest')"
    :width="1200"
    unmount-on-close
    :show-continue="false"
    :ok-loading="drawerLoading"
    :ok-text="t('project.commonScript.apply')"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <div class="flex h-full">
      <div class="w-[292px] border-r border-[var(--color-text-n8)] p-[16px]">
        <div class="flex items-center justify-between">
          <MsProjectSelect v-model:project="innerProject" class="mb-[16px]" />
          <a-select v-model="protocolType" class="mb-[16px] ml-2 max-w-[90px]">
            <a-option v-for="item of protocolOptions" :key="item" :value="item">{{ item }}</a-option>
          </a-select>
        </div>
        <a-input
          v-model:model-value="moduleKeyword"
          :max-length="255"
          :placeholder="t('project.commonScript.folderSearchPlaceholder')"
          allow-clear
          class="mb-[16px]"
        />
        <div class="folder">
          <div :class="getFolderClass('all')" @click="setActiveFolder('all')">
            <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
            <div class="folder-name">{{ t('project.commonScript.allApis') }}</div>
            <div class="folder-count">({{ modulesCount['all'] || 0 }})</div>
          </div>
        </div>
        <a-spin class="w-full" :loading="moduleLoading">
          <MsTree
            v-model:selected-keys="selectedModuleKeys"
            :data="folderTree"
            :keyword="moduleKeyword"
            :empty-text="t('project.commonScript.noTreeData')"
            :virtual-list-props="virtualListProps"
            :field-names="{
              title: 'name',
              key: 'id',
              children: 'children',
              count: 'count',
            }"
            block-node
            title-tooltip-position="left"
            @select="folderNodeSelect"
          >
            <template #title="nodeData">
              <div class="inline-flex w-full gap-[8px]">
                <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
                <div class="ms-tree-node-count ml-[4px] text-[var(--color-text-brand)]">{{ nodeData.count || 0 }}</div>
              </div>
            </template>
          </MsTree>
        </a-spin>
      </div>
      <div class="flex w-[calc(100%-293px)] flex-col p-[16px]">
        <MsAdvanceFilter
          v-model:keyword="keyword"
          :filter-config-list="filterConfigList"
          :custom-fields-config-list="searchCustomFields"
          :search-placeholder="t('project.commonScript.searchPlaceholder')"
          @keyword-search="searchCase"
          @adv-search="searchCase"
        >
          <template #left>
            <div class="flex items-center justify-between">
              <div class="flex items-center">
                <div class="mr-[4px] text-[var(--color-text-1)]">{{ activeFolderName }}</div>
                <div class="text-[var(--color-text-4)]">({{ propsRes.msPagination?.total }})</div>
              </div>
            </div>
          </template>
        </MsAdvanceFilter>
        <ms-base-table v-bind="propsRes" no-disable class="mt-[16px]" v-on="propsEvent">
          <template #method="{ record }">
            <apiMethodName :method="record.method" is-tag class="flex items-center" />
          </template>
        </ms-base-table>
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem } from '@/components/pure/ms-advance-filter/type';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsProjectSelect from '@/components/business/ms-project-select/index.vue';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';

  import {
    getFormApiImportModule,
    getFormApiImportModuleCount,
    getFormApiImportPageList,
  } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';
  import { mapTree } from '@/utils';

  import type { ModuleTreeNode, TableQueryParams } from '@/models/common';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    projectId: string; // 项目id
    confirmLoading: boolean;
  }>();

  const emit = defineEmits(['update:visible', 'save', 'close', 'update:projectId']);

  const innerProject = useVModel(props, 'projectId', emit);

  const drawerLoading = ref<boolean>(false);

  const exportScriptDrawer = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emit('update:visible', val);
    },
  });

  const moduleKeyword = ref('');
  const activeFolder = ref('all');
  function getFolderClass(id: string) {
    return activeFolder.value === id ? 'folder-text folder-text--active' : 'folder-text';
  }
  const activeFolderName = ref(t('project.commonScript.allApis'));
  const selectedModuleKeys = ref<string[]>([]);

  function setActiveFolder(id: string) {
    activeFolder.value = id;
    activeFolderName.value = t('project.commonScript.allApis');
    selectedModuleKeys.value = [];
  }

  const modulesCount = ref<Record<string, any>>({});
  const moduleLoading = ref(false);
  const folderTree = ref<ModuleTreeNode[]>([]);

  const virtualListProps = computed(() => {
    return {
      height: 'calc(100vh - 251px)',
      threshold: 200,
      fixedSize: true,
      buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
    };
  });

  /**
   * 处理模块树节点选中事件
   */
  const offspringIds = ref<string[]>([]);

  function folderNodeSelect(_selectedKeys: (string | number)[], node: MsTreeNodeData) {
    selectedModuleKeys.value = _selectedKeys as string[];
    activeFolder.value = node.id;
    activeFolderName.value = node.name;
    offspringIds.value = [];
    mapTree(node.children || [], (e) => {
      offspringIds.value.push(e.id);
      return e;
    });
  }

  const keyword = ref('');
  const filterConfigList = ref<FilterFormItem[]>([]);
  const searchCustomFields = ref<FilterFormItem[]>([]);
  const combine = ref<Record<string, any>>({});
  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      sortIndex: 1,
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
    },
    {
      title: 'project.commonScript.apiName',
      dataIndex: 'name',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showTooltip: true,
      width: 300,
    },
    {
      title: 'project.commonScript.requestType',
      dataIndex: 'method',
      slotName: 'method',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
    },
    {
      title: 'project.commonScript.path',
      slotName: 'path',
      dataIndex: 'path',
      width: 200,
    },
    {
      title: 'ms.case.associate.tags',
      dataIndex: 'tags',
      slotName: 'tags',
      isTag: true,
    },
    {
      title: 'ms.case.associate.version',
      dataIndex: 'versionName',
      slotName: 'versionName',
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateUser',
      slotName: 'createUserName',
      dataIndex: 'createUserName',
      showInTable: true,
      width: 300,
      showTooltip: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateTime',
      slotName: 'createTime',
      dataIndex: 'createTime',
      showInTable: true,
      width: 300,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getFormApiImportPageList,
    {
      scroll: { x: '100%' },
      columns,
      showSetting: false,
      selectable: true,
      showSelectAll: true,
      heightUsed: 310,
    },
    (record) => {
      return {
        ...record,
        tags: (record.tags || []).map((item: string, i: number) => {
          return {
            id: `${record.id}-${i}`,
            name: item,
          };
        }),
      };
    }
  );

  const protocolType = ref('HTTP'); // 协议类型
  const protocolOptions = ref(['HTTP']);
  const searchParams = ref<TableQueryParams>({
    moduleIds: [],
    protocol: protocolType.value,
  });

  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */
  async function initModules(isSetDefaultKey = false) {
    try {
      moduleLoading.value = true;
      const params = {
        projectId: innerProject.value,
        protocol: protocolType.value,
      };
      const res = await getFormApiImportModule(params);
      folderTree.value = mapTree<ModuleTreeNode>(res, (e) => {
        return {
          ...e,
          hideMoreAction: e.id === 'root',
          draggable: false,
          disabled: false,
          count: modulesCount.value[e.id] || 0,
        };
      });
      if (isSetDefaultKey) {
        selectedModuleKeys.value = [folderTree.value[0].id];
        activeFolderName.value = folderTree.value[0].name;
        mapTree(folderTree.value[0].children || [], (e) => {
          offspringIds.value.push(e.id);
          return e;
        });
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      moduleLoading.value = false;
    }
  }

  function getLoadListParams() {
    if (activeFolder.value === 'all' || !activeFolder.value) {
      searchParams.value.moduleIds = [];
    } else {
      searchParams.value.moduleIds = [activeFolder.value, ...offspringIds.value];
    }
    setLoadListParams({
      ...searchParams.value,
      keyword: keyword.value,
      projectId: innerProject.value,
    });
  }

  // 初始化模块数量
  async function initModuleCount() {
    try {
      const params = {
        keyword: keyword.value,
        moduleIds: [],
        projectId: innerProject.value,
        current: propsRes.value.msPagination?.current,
        pageSize: propsRes.value.msPagination?.pageSize,
        combine: combine.value,
        protocol: protocolType.value,
      };
      modulesCount.value = await getFormApiImportModuleCount(params);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function searchCase() {
    getLoadListParams();
    loadList();
    initModuleCount();
  }

  // 保存参数
  function handleDrawerConfirm() {
    const { excludeKeys, selectedKeys, selectorStatus } = propsRes.value;
    const { versionId, moduleIds } = searchParams.value;
    const params = {
      excludeIds: [...excludeKeys],
      selectIds: selectorStatus === 'all' ? [] : [...selectedKeys],
      selectAll: selectorStatus === 'all',
      moduleIds,
      versionId,
      refId: '',
      projectId: innerProject.value,
    };
    emit('save', params);
  }

  function handleDrawerCancel() {
    exportScriptDrawer.value = false;
    resetSelector();
    emit('close');
  }

  watch(
    () => exportScriptDrawer.value,
    (val) => {
      emit('update:visible', val);
      if (val) {
        searchCase();
        initModules();
      }
    }
  );

  watch(
    () => innerProject.value,
    (val) => {
      if (val) {
        resetSelector();
        initModules();
        searchCase();
      }
    }
  );

  watch(
    () => activeFolder.value,
    () => {
      searchCase();
    }
  );

  /**
   * 初始化模块数量
   */
  watch(
    () => modulesCount.value,
    (obj) => {
      folderTree.value = mapTree<ModuleTreeNode>(folderTree.value, (node) => {
        return {
          ...node,
          count: obj?.[node.id] || 0,
        };
      });
    }
  );
</script>

<style scoped lang="less">
  .folder {
    @apply flex cursor-pointer items-center justify-between;

    padding: 8px 4px;
    border-radius: var(--border-radius-small);
    &:hover {
      background-color: rgb(var(--primary-1));
    }
    .folder-text {
      @apply flex cursor-pointer items-center;
      .folder-icon {
        margin-right: 4px;
        color: var(--color-text-4);
      }
      .folder-name {
        color: var(--color-text-1);
      }
      .folder-count {
        margin-left: 4px;
        color: var(--color-text-4);
      }
    }
    .folder-text--active {
      .folder-icon,
      .folder-name,
      .folder-count {
        color: rgb(var(--primary-5));
      }
    }
  }
  .footer {
    @apply flex items-center justify-between;

    margin: auto -16px -16px;
    padding: 12px 16px;
    box-shadow: 0 -1px 4px 0 rgb(31 35 41 / 10%);
  }
</style>
