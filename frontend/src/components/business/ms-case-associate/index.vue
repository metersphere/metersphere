<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="t('ms.case.associate.title')"
    :width="1200"
    :footer="false"
    no-content-padding
  >
    <template #headerLeft>
      <div class="float-left">
        <a-select
          v-model="caseType"
          class="ml-2 max-w-[100px]"
          :placeholder="t('caseManagement.featureCase.PleaseSelect')"
        >
          <a-option v-for="item of actionType" :key="item.value" :value="item.value">{{ item.name }}</a-option>
        </a-select>
      </div>
    </template>
    <div class="flex h-full">
      <div class="w-[292px] border-r border-[var(--color-text-n8)] p-[16px]">
        <div class="flex items-center justify-between">
          <MsProjectSelect v-model:project="innerProject" class="mb-[16px]" />
          <a-select v-if="caseType === 'API'" v-model="protocolType" class="mb-[16px] ml-2 max-w-[90px]">
            <a-option v-for="item of protocolOptions" :key="item" :value="item">{{ item }}</a-option>
          </a-select>
        </div>
        <a-input
          v-model:model-value="moduleKeyword"
          :placeholder="t('caseManagement.caseReview.folderSearchPlaceholder')"
          allow-clear
          class="mb-[16px]"
        />
        <div class="folder">
          <div :class="getFolderClass('all')" @click="setActiveFolder('all')">
            <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
            <div class="folder-name">{{ t('caseManagement.caseReview.allReviews') }}</div>
            <div class="folder-count">({{ allCaseCount }})</div>
          </div>
        </div>
        <a-divider class="my-[8px]" />
        <a-spin class="w-full" :loading="moduleLoading">
          <MsTree
            v-model:selected-keys="selectedModuleKeys"
            :data="folderTree"
            :keyword="moduleKeyword"
            :empty-text="t('caseManagement.caseReview.noReviews')"
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
              <div class="inline-flex w-full">
                <div class="one-line-text w-[calc(100%-32px)] text-[var(--color-text-1)]">{{ nodeData.name }}</div>
                <div class="ml-[4px] text-[var(--color-text-4)]">({{ nodeData.count || 0 }})</div>
              </div>
            </template>
          </MsTree>
        </a-spin>
      </div>
      <div class="flex w-[calc(100%-293px)] flex-col p-[16px]">
        <MsAdvanceFilter
          v-model:keyword="keyword"
          :filter-config-list="filterConfigList"
          :row-count="filterRowCount"
          :search-placeholder="t('caseManagement.caseReview.searchPlaceholder')"
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
          <template #right>
            <a-select
              v-model:model-value="version"
              :options="versionOptions"
              :placeholder="t('ms.case.associate.versionPlaceholder')"
              class="w-[200px]"
              allow-clear
            />
          </template>
        </MsAdvanceFilter>
        <ms-base-table v-bind="propsRes" no-disable class="mt-[16px]" v-on="propsEvent">
          <template #caseLevel="{ record }">
            <caseLevel :case-level="record.caseLevel" />
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
                :disabled="propsRes.selectedKeys.size === 0 || props.okButtonDisabled"
                @click="handleConfirm"
              >
                {{ t('ms.case.associate.associate') }}
              </a-button>
            </slot>
          </div>
        </div>
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';

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
  import caseLevel from './caseLevel.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { mapTree } from '@/utils';

  import { ModuleTreeNode } from '@/models/projectManagement/file';

  const props = defineProps<{
    visible: boolean;
    project: string;
    getModulesFunc: (params: any) => Promise<ModuleTreeNode[]>;
    modulesCount?: Record<string, number>; // 模块数量统计对象
    okButtonDisabled?: boolean; // 确认按钮是否禁用
    selectedKeys?: string[]; // 已选中的用例id
  }>();
  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'update:project', val: string): void;
    (e: 'init', val: string[]): void;
    (e: 'folderNodeSelect', ids: (string | number)[], springIds: string[]): void;
    (e: 'success', val: string[]): void;
    (e: 'close'): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const virtualListProps = computed(() => {
    return {
      height: 'calc(100vh - 251px)',
    };
  });

  const innerVisible = ref(props.visible);
  const innerProject = ref(props.project);

  // 协议类型
  const protocolType = ref('HTTP');
  const caseType = ref('API');

  const protocolOptions = ref(['DUBBO', 'HTTP', 'TCP', 'SQL']);
  const actionType = ref([
    {
      value: 'API',
      name: t('caseManagement.featureCase.apiCase'),
    },
    {
      value: 'SCENE',
      name: t('caseManagement.featureCase.sceneCase'),
    },
    {
      value: 'UI',
      name: t('caseManagement.featureCase.uiCase'),
    },
    {
      value: 'PERFORMANCE',
      name: t('caseManagement.featureCase.propertyCase'),
    },
  ]);

  watch(
    () => props.visible,
    (val) => {
      innerVisible.value = val;
    }
  );

  watch(
    () => innerVisible.value,
    (val) => {
      if (!val) {
        emit('update:visible', false);
      }
    }
  );

  watch(
    () => props.project,
    (val) => {
      innerProject.value = val;
    }
  );

  watch(
    () => innerProject.value,
    (val) => {
      emit('update:project', val);
    }
  );

  const activeFolder = ref('all');
  const activeFolderName = ref(t('ms.case.associate.allCase'));
  const allCaseCount = ref(0);
  const filterRowCount = ref(0);
  const filterConfigList = ref<FilterFormItem[]>([]);

  function getFolderClass(id: string) {
    return activeFolder.value === id ? 'folder-text folder-text--active' : 'folder-text';
  }

  const moduleKeyword = ref('');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const moduleLoading = ref(false);

  const selectedModuleKeys = ref<string[]>([]);

  function setActiveFolder(id: string) {
    activeFolder.value = id;
    activeFolderName.value = t('ms.case.associate.allCase');
    selectedModuleKeys.value = [];
    emit('folderNodeSelect', [id], []);
  }

  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */
  async function initModules(isSetDefaultKey = false) {
    try {
      moduleLoading.value = true;
      const res = await props.getModulesFunc(appStore.currentProjectId);
      folderTree.value = res;
      if (isSetDefaultKey) {
        selectedModuleKeys.value = [folderTree.value[0].id];
        activeFolderName.value = folderTree.value[0].name;
        const offspringIds: string[] = [];
        mapTree(folderTree.value[0].children || [], (e) => {
          offspringIds.push(e.id);
          return e;
        });

        emit('folderNodeSelect', selectedModuleKeys.value, offspringIds);
      }
      emit(
        'init',
        folderTree.value.map((e) => e.name)
      );
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      moduleLoading.value = false;
    }
  }

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(_selectedKeys: (string | number)[], node: MsTreeNodeData) {
    selectedModuleKeys.value = _selectedKeys as string[];
    activeFolder.value = node.id;
    activeFolderName.value = node.name;
    const offspringIds: string[] = [];
    mapTree(node.children || [], (e) => {
      offspringIds.push(e.id);
      return e;
    });

    emit('folderNodeSelect', _selectedKeys, offspringIds);
  }

  onBeforeMount(() => {
    initModules();
  });

  /**
   * 初始化模块资源数量
   */
  watch(
    () => props.modulesCount,
    (obj) => {
      folderTree.value = mapTree<ModuleTreeNode>(folderTree.value, (node) => {
        return {
          ...node,
          count: obj?.[node.id] || 0,
        };
      });
    }
  );

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
      dataIndex: 'id',
      sortIndex: 1,
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      width: 90,
    },
    {
      title: 'ms.case.associate.caseName',
      dataIndex: 'name',
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      showTooltip: true,
      width: 200,
    },
    {
      title: 'ms.case.associate.caseLevel',
      dataIndex: 'caseLevel',
      slotName: 'caseLevel',
      width: 90,
    },
    {
      title: 'ms.case.associate.version',
      slotName: 'version',
      width: 80,
    },
    {
      title: 'ms.case.associate.tags',
      dataIndex: 'tags',
      isTag: true,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    () =>
      Promise.resolve({
        list: [
          {
            id: 'ded3d43',
            name: '测试评审1',
            creator: '张三',
            reviewer: '李四',
            module: '模块1',
            caseLevel: 0, // 未开始、进行中、已完成、已归档
            caseCount: 100,
            passCount: 0,
            failCount: 10,
            reviewCount: 20,
            reviewingCount: 25,
            tags: ['标签1', '标签2'],
            type: 'single',
            desc: 'douifd9304',
            cycle: [1700200794229, 1700200994229],
          },
          {
            id: 'g545hj4',
            name: '测试评审2',
            creator: '张三',
            reviewer: '李四',
            module: '模块1',
            caseLevel: 1, // 未开始、进行中、已完成、已归档
            caseCount: 105,
            passCount: 50,
            failCount: 10,
            reviewCount: 20,
            reviewingCount: 25,
            tags: ['标签1', '标签2'],
            type: 'single',
            desc: 'douifd9304',
            cycle: [1700200794229, 1700200994229],
          },
          {
            id: 'hj65b54',
            name: '测试评审3',
            creator: '张三',
            reviewer: '李四',
            module: '模块1',
            caseLevel: 2, // 未开始、进行中、已完成、已归档
            caseCount: 125,
            passCount: 70,
            failCount: 10,
            reviewCount: 20,
            reviewingCount: 25,
            passRate: '80%',
            tags: ['标签1', '标签2'],
            type: 'single',
            desc: 'douifd9304',
            cycle: [1700200794229, 1700200994229],
          },
          {
            id: 'wefwefw',
            name: '测试评审4',
            creator: '张三',
            reviewer: '李四',
            module: '模块1',
            caseLevel: 3, // 未开始、进行中、已完成、已归档
            caseCount: 130,
            passCount: 70,
            failCount: 10,
            reviewCount: 0,
            reviewingCount: 50,
            passRate: '80%',
            tags: ['标签1', '标签2'],
            type: 'single',
            desc: 'douifd9304',
            cycle: [1700200794229, 1700200994229],
          },
        ],
        current: 1,
        pageSize: 10,
        total: 2,
      }),
    {
      columns,
      scroll: {
        x: '100%',
      },
      showSetting: false,
      selectable: true,
      showSelectAll: true,
    },
    (item) => {
      return {
        ...item,
        tags: item.tags?.map((e: string) => ({ id: e, name: e })) || [],
      };
    }
  );

  function searchCase() {
    setLoadListParams({
      version: version.value,
      keyword: keyword.value,
    });
    loadList();
  }

  onBeforeMount(() => {
    searchCase();
  });

  const loading = ref(false);

  async function handleConfirm() {
    try {
      loading.value = true;
      Message.success(t('ms.case.associate.associateSuccess'));
      innerVisible.value = false;
      emit('success', Array.from(propsRes.value.selectedKeys));
      resetSelector();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  function cancel() {
    innerVisible.value = false;
    resetSelector();
    emit('close');
  }

  defineExpose({
    initModules,
  });
</script>

<style lang="less" scoped>
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
