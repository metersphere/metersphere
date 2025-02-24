<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="title"
    :width="1200"
    :footer="false"
    no-content-padding
    :mask-closable="false"
    unmount-on-close
  >
    <div class="flex h-full">
      <div class="w-[292px] border-r border-[var(--color-text-n8)] p-[16px]">
        <a-input
          v-model:model-value="moduleKeyword"
          :placeholder="t('caseManagement.featureCase.searchTip')"
          allow-clear
          class="mb-[16px]"
          :max-length="255"
        />
        <div class="folder">
          <div :class="getFolderClass('all')" @click="setActiveFolder('all')">
            <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
            <div class="folder-name">{{ t('caseManagement.featureCase.allCase') }}</div>
            <div class="folder-count">({{ modulesCount['all'] }})</div>
          </div>
        </div>
        <a-spin class="w-full" :loading="moduleLoading">
          <MsTree
            v-model:focus-node-key="focusNodeKey"
            :selected-keys="selectedNodeKeys"
            :data="caseTree"
            :keyword="moduleKeyword"
            :empty-text="t('caseManagement.featureCase.caseEmptyRecycle')"
            :virtual-list-props="virtualListProps"
            :field-names="{
              title: 'name',
              key: 'id',
              children: 'children',
              count: 'count',
            }"
            block-node
            title-tooltip-position="left"
            @select="caseNodeSelect"
          >
            <template #title="nodeData">
              <div class="inline-flex w-full gap-[8px]">
                <div class="one-line-text w-full text-[var(--color-text-1)]" @click="setFocusKey(nodeData)">
                  {{ nodeData.name }}
                </div>
                <div class="ms-tree-node-count ml-[4px] text-[var(--color-text-brand)]">{{ nodeData.count || 0 }}</div>
              </div>
            </template>
          </MsTree>
        </a-spin>
      </div>
      <div class="flex w-[calc(100%-293px)] flex-col p-[16px]">
        <div class="mb-[16px] flex items-center justify-between">
          <div class="flex items-center">
            <div class="mr-[4px] text-[var(--color-text-1)]">{{ activeFolderName }}</div>
            <div class="text-[var(--color-text-4)]">({{ modulesCount[activeFolder] }})</div>
          </div>
          <div class="flex items-center gap-[8px]">
            <!-- TODO这个版本不做 -->
            <!-- <a-select
              v-model:model-value="version"
              :options="versionOptions"
              :placeholder="t('ms.case.associate.versionPlaceholder')"
              class="w-[200px]"
              allow-clear
            /> -->
            <a-input-search
              v-model="keyword"
              :placeholder="t('ms.case.associate.searchPlaceholder')"
              allow-clear
              class="w-[200px]"
              @press-enter="searchCase"
              @search="searchCase"
              @clear="searchCase"
            />
            <!-- TODO这个版本不做  -->
            <!-- <a-button type="outline" class="arco-btn-outline--secondary px-[8px]">
              <MsIcon type="icon-icon-filter" class="mr-[4px] text-[var(--color-text-4)]" />
              <div class="text-[var(--color-text-4)]">{{ t('common.filter') }}</div>
            </a-button> -->
          </div>
        </div>
        <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
          <template #caseLevel="{ record }">
            <caseLevel :case-level="getCaseLevels(record.customFields)" />
          </template>
          <template #caseLevelFilter="{ columnConfig }">
            <TableFilter
              v-model:visible="caseFilterVisible"
              v-model:status-filters="caseFilters"
              :title="(columnConfig.title as string)"
              :list="caseLevelList"
              value-key="value"
              @search="searchCase()"
            >
              <template #item="{ item }">
                <div class="flex"> <caseLevel :case-level="item.text" /></div>
              </template>
            </TableFilter>
          </template>
          <template v-if="(keyword || '').trim() === ''" #empty>
            <div class="flex w-full items-center justify-center p-[8px] text-[var(--color-text-4)]">
              {{ t('caseManagement.caseReview.tableNoData') }}
              <MsButton v-permission="['FUNCTIONAL_CASE:READ+ADD']" class="ml-[8px]" @click="createCase">
                {{ t('caseManagement.featureCase.creatingCase') }}
              </MsButton>
            </div>
          </template>
        </ms-base-table>
        <div class="footer">
          <div class="flex flex-1 items-center">
            <slot name="footerLeft"></slot>
          </div>
          <div class="flex items-center">
            <slot name="footerRight">
              <a-button type="secondary" :disabled="loading" class="mr-[12px]" @click="cancel">
                {{ t('common.cancel') }}
              </a-button>
              <a-button
                type="primary"
                :loading="loading"
                :disabled="propsRes.selectedKeys.size === 0"
                @click="handleConfirm"
              >
                {{ t('common.add') }}
              </a-button>
            </slot>
          </div>
        </div>
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import TableFilter from '../../tableFilter.vue';

  import {
    addPrepositionRelation,
    getAssociatedCaseIds,
    getCaseModulesCounts,
    getCaseModuleTree,
    getPrepositionRelation,
  } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { mapTree } from '@/utils';

  import type { CaseModuleQueryParams, OptionsField } from '@/models/caseManagement/featureCase';
  import type { ModuleTreeNode, TableQueryParams } from '@/models/common';

  import { getCaseLevels } from '../../utils';

  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const featureStore = useFeatureCaseStore();

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    showType: 'preposition' | 'postPosition';
    caseId: string;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'success'): void;
    (e: 'close'): void;
    (e: 'create'): void;
  }>();

  const innerVisible = computed({
    get() {
      return props.visible;
    },
    set(value) {
      emit('update:visible', value);
    },
  });

  const title = computed(() => {
    return props.showType === 'preposition' ? '添加前置用例' : '添加后置用例';
  });

  const caseTree = ref<ModuleTreeNode[]>([]);
  const moduleLoading = ref(false);

  const moduleKeyword = ref('');

  const virtualListProps = computed(() => {
    return {
      height: 'calc(100vh - 251px)',
      threshold: 200,
      fixedSize: true,
      buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
    };
  });

  const activeFolder = ref('all');
  const activeFolderName = ref(t('ms.case.associate.allCase'));
  const offspringIds = ref<string[]>([]);
  const modulesCount = ref<Record<string, any>>({});

  // 选中节点
  const selectedNodeKeys = computed({
    get: () => [activeFolder.value],
    set: (val) => val,
  });
  /**
   * 处理文件夹树节点选中事件
   */
  const focusNodeKey = ref<string>('');

  function caseNodeSelect(_selectedKeys: (string | number)[], node: MsTreeNodeData) {
    [activeFolder.value] = _selectedKeys as string[];
    activeFolder.value = node.id;
    activeFolderName.value = node.name;
    offspringIds.value = [];
    mapTree(node.children || [], (e) => {
      offspringIds.value.push(e.id);
      return e;
    });

    focusNodeKey.value = '';
  }

  function getFolderClass(id: string) {
    return activeFolder.value === id ? 'folder-text folder-text--active' : 'folder-text';
  }

  function setActiveFolder(id: string) {
    activeFolder.value = id;
    activeFolderName.value = t('ms.case.associate.allCase');
  }
  const keyword = ref('');
  const version = ref('');
  const versionOptions = ref([
    {
      label: '全部',
      value: 'all',
    },
    {
      label: '版本1',
      value: '1',
    },
    {
      label: '版本2',
      value: '2',
    },
  ]);

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
      title: 'caseManagement.featureCase.tableColumnName',
      dataIndex: 'name',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showTooltip: true,
      width: 300,
    },
    {
      title: 'ms.case.associate.caseLevel',
      dataIndex: 'caseLevel',
      slotName: 'caseLevel',
      titleSlotName: 'caseLevelFilter',
      width: 100,
    },
    // {
    //   title: 'caseManagement.featureCase.tableColumnVersion',
    //   slotName: 'version',
    //   width: 80,
    // },
    {
      title: 'caseManagement.featureCase.tableColumnTag',
      dataIndex: 'tags',
      isTag: true,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getPrepositionRelation,
    {
      columns,
      scroll: {
        x: '100%',
      },
      showSetting: false,
      selectable: true,
      heightUsed: 380,
      showSelectAll: true,
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

  const loading = ref(false);

  function cancel() {
    innerVisible.value = false;
    emit('close');
  }
  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */

  const selectedKeysNode = ref<(string | number)[]>([]);
  async function initModules(isSetDefaultKey = false) {
    try {
      moduleLoading.value = true;
      const res = await getCaseModuleTree({ projectId: appStore.currentProjectId });
      caseTree.value = mapTree<ModuleTreeNode>(res, (e) => {
        return {
          ...e,
          hideMoreAction: e.id === 'root',
          draggable: false,
          disabled: false,
          count: modulesCount.value?.[e.id] || 0,
        };
      });
      if (isSetDefaultKey) {
        selectedKeysNode.value = [caseTree.value[0].id];
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      moduleLoading.value = false;
    }
  }

  const searchParams = ref<TableQueryParams>({
    projectId: currentProjectId.value,
    moduleIds: [],
    excludeIds: [],
  });
  // 用例等级表头检索
  const caseLevelFields = ref<Record<string, any>>({});
  const caseFilterVisible = ref(false);
  const caseLevelList = ref<OptionsField[]>([]);
  const caseFilters = ref<string[]>([]);

  // 获取用例参数
  function getLoadListParams() {
    if (activeFolder.value === 'all') {
      searchParams.value.moduleIds = [];
    } else {
      searchParams.value.moduleIds = [activeFolder.value, ...offspringIds.value];
    }
    setLoadListParams({
      ...searchParams.value,
      keyword: keyword.value,
      id: props.caseId,
      type: props.showType === 'preposition' ? 'PRE' : 'POST',
      filter: {
        caseLevel: caseFilters.value,
      },
      condition: {
        keyword: keyword.value,
        filter: {
          caseLevel: caseFilters.value,
          ...propsRes.value.filter,
        },
      },
    });
  }

  async function getModulesCount() {
    const { excludeIds } = searchParams.value;
    try {
      const emitTableParams: CaseModuleQueryParams = {
        keyword: keyword.value,
        moduleIds: [],
        projectId: currentProjectId.value,
        current: propsRes.value.msPagination?.current,
        pageSize: propsRes.value.msPagination?.pageSize,
        excludeIds,
        condition: {
          keyword: keyword.value,
          filter: {
            caseLevel: caseFilters.value,
            ...propsRes.value.filter,
          },
        },
      };
      modulesCount.value = await getCaseModulesCounts(emitTableParams);
    } catch (error) {
      console.log(error);
    }
  }

  const setFocusKey = (node: MsTreeNodeData) => {
    focusNodeKey.value = node.id || '';
  };

  async function getAssociatedIds() {
    try {
      const result = await getAssociatedCaseIds(props.caseId);
      searchParams.value.excludeIds = result;
    } catch (error) {
      console.log(error);
    }
  }

  async function searchCase() {
    await getAssociatedIds();
    getLoadListParams();
    loadList();
    getModulesCount();
  }

  async function handleConfirm() {
    loading.value = true;
    try {
      const { excludeKeys, selectedKeys, selectorStatus } = propsRes.value;
      const { versionId, moduleIds } = searchParams.value;
      const params = {
        id: props.caseId,
        excludeIds: [...excludeKeys],
        selectIds: [...selectedKeys],
        selectAll: selectorStatus === 'all',
        moduleIds,
        versionId,
        refId: '',
        type: props.showType === 'preposition' ? 'PRE' : 'POST',
        projectId: currentProjectId.value,
        condition: {
          keyword: keyword.value,
        },
      };
      await addPrepositionRelation(params);
      Message.success(t('common.addSuccess'));
      emit('success');
      innerVisible.value = false;
      resetSelector();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  async function initFilter() {
    await featureStore.getDefaultTemplate();
    caseLevelList.value = featureStore.getSystemCaseLevelFields();
    caseFilters.value = [];
  }

  watch(
    () => innerVisible.value,
    (val) => {
      if (val) {
        initFilter();
        searchCase();
      }
    }
  );

  watch(
    () => modulesCount.value,
    (obj) => {
      caseTree.value = mapTree<ModuleTreeNode>(caseTree.value, (node) => {
        return {
          ...node,
          count: obj?.[node.id] || 0,
        };
      });
    }
  );

  watch(
    () => activeFolder.value,
    () => {
      searchCase();
    }
  );

  function createCase() {
    emit('create');
    innerVisible.value = false;
  }

  onMounted(() => {
    resetSelector();
  });

  defineExpose({
    initModules,
  });
</script>

<style lang="less" scoped>
  .folder {
    @apply flex cursor-pointer items-center justify-between;

    padding: 4px;
    border-radius: var(--border-radius-small);
    &:hover {
      background-color: rgb(var(--primary-1));
    }
    .folder-text {
      @apply flex cursor-pointer items-center;

      height: 26px;
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
