<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="t('ms.case.associate.title')"
    :width="1200"
    :footer="false"
    no-content-padding
    unmount-on-close
  >
    <template #headerLeft>
      <a-select
        v-if="props?.moduleOptions"
        v-model="caseType"
        class="ml-2 max-w-[100px]"
        :placeholder="t('caseManagement.featureCase.PleaseSelect')"
        @change="changeCaseType"
      >
        <a-option v-for="item of props?.moduleOptions" :key="item.value" :value="item.value">
          {{ t(item.label) }}
        </a-option>
      </a-select>
    </template>
    <div class="flex h-full">
      <div v-show="!isAdvancedSearchMode" class="w-[292px] border-r border-[var(--color-text-n8)] p-[16px]">
        <div class="flex items-center justify-between">
          <div v-if="!props.hideProjectSelect" class="w-full max-w-[259px]">
            <a-select
              v-model="innerProject"
              class="mb-[16px] w-full"
              :default-value="innerProject"
              allow-search
              :placeholder="t('common.pleaseSelect')"
              @change="changeProject"
            >
              <template #arrow-icon>
                <icon-caret-down />
              </template>
              <a-tooltip v-for="item of projectList" :key="item.id" :mouse-enter-delay="500" :content="item.name">
                <a-option :value="item.id" :class="item.id === innerProject ? 'arco-select-option-selected' : ''">
                  {{ item.name }}
                </a-option>
              </a-tooltip>
            </a-select>
          </div>
        </div>
        <div class="mb-[8px] flex items-center gap-[8px]">
          <a-input
            v-model:model-value="moduleKeyword"
            :placeholder="t('caseManagement.caseReview.folderSearchPlaceholder')"
            allow-clear
            :max-length="255"
          />
          <a-tooltip :content="isExpandAll ? t('common.collapseAllSubModule') : t('common.expandAllSubModule')">
            <a-button
              type="outline"
              class="expand-btn arco-btn-outline--secondary"
              @click="() => (isExpandAll = !isExpandAll)"
            >
              <MsIcon v-if="isExpandAll" type="icon-icon_comment_collapse_text_input" />
              <MsIcon v-else type="icon-icon_comment_expand_text_input" />
            </a-button>
          </a-tooltip>
        </div>
        <TreeFolderAll
          ref="treeFolderAllRef"
          :protocol-key="ProtocolKeyEnum.CASE_MANAGEMENT_ASSOCIATE_PROTOCOL"
          :active-folder="activeFolder"
          :folder-name="t('caseManagement.featureCase.allCase')"
          :all-count="modulesCount.total || modulesCount.all || 0"
          :show-expand-api="false"
          :not-show-operation="caseType !== 'API'"
          @set-active-folder="setActiveFolder"
          @selected-protocols-change="selectedProtocolsChange"
        />
        <a-spin class="w-full" :loading="moduleLoading">
          <MsTree
            v-model:selected-keys="selectedModuleKeys"
            :data="folderTree"
            :keyword="moduleKeyword"
            :empty-text="t('common.noData')"
            :virtual-list-props="virtualListProps"
            :field-names="{
              title: 'name',
              key: 'id',
              children: 'children',
              count: 'count',
            }"
            :expand-all="isExpandAll"
            block-node
            title-tooltip-position="top"
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
      <div :class="[`flex ${!isAdvancedSearchMode ? 'w-[calc(100%-293px)]' : 'w-full'} flex-col p-[16px]`]">
        <MsAdvanceFilter
          ref="msAdvanceFilterRef"
          v-model:keyword="keyword"
          :view-type="props.viewType"
          :filter-config-list="filterConfigList"
          :custom-fields-config-list="searchCustomFields"
          :search-placeholder="t('caseManagement.featureCase.searchPlaceholder')"
          :count="propsRes.msPagination?.total || 0"
          :name="activeFolderName"
          @keyword-search="searchCase"
          @adv-search="handleAdvSearch"
          @refresh="searchCase()"
        />
        <ms-base-table
          v-bind="propsRes"
          :columns="columns"
          :action-config="{
            baseAction: [],
            moreAction: [],
          }"
          :not-show-table-filter="isAdvancedSearchMode"
          no-disable
          class="mt-[16px]"
          v-on="propsEvent"
          @filter-change="filterChange"
        >
          <template #num="{ record }">
            <a-button type="text" class="px-0" @click="openDetail(record.id)">{{ record.num }} </a-button>
          </template>
          <template #caseLevel="{ record }">
            <caseLevel :case-level="getCaseLevels(record.customFields)" />
          </template>
          <template #lastExecuteResult="{ record }">
            <ExecuteStatusTag v-if="record.lastExecuteResult" :execute-result="record.lastExecuteResult" />
            <span v-else>-</span>
          </template>
          <!-- 用例等级 -->
          <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
            <caseLevel :case-level="filterContent.value" />
          </template>
          <!-- 评审结果 -->
          <template #reviewStatus="{ record }">
            <MsIcon
              :type="statusIconMap[record.reviewStatus]?.icon || ''"
              class="mr-1"
              :class="[statusIconMap[record.reviewStatus].color]"
            ></MsIcon>
            <span>{{ statusIconMap[record.reviewStatus]?.statusText || '' }} </span>
          </template>
          <!-- 执行结果 -->
          <template #[FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT]="{ filterContent }">
            <ExecuteStatusTag :execute-result="filterContent.value" />
          </template>
          <template #createUserName="{ record }">
            <a-tooltip :content="`${record.createUserName}`" position="tl">
              <div class="one-line-text">{{ record.createUserName }}</div>
            </a-tooltip>
          </template>
        </ms-base-table>
        <div class="footer">
          <div class="flex flex-1 items-center">
            <slot name="footerLeft"></slot>
          </div>
          <div class="flex items-center">
            <slot name="footerRight">
              <a-button type="secondary" :disabled="props.confirmLoading" class="mr-[12px]" @click="cancel">
                {{ t('common.cancel') }}
              </a-button>
              <a-button
                type="primary"
                :loading="props.confirmLoading"
                :disabled="propsRes.selectedKeys.size === 0"
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
  import { computed, ref, watch } from 'vue';
  import { useRouter } from 'vue-router';
  import { useVModel } from '@vueuse/core';

  import { getFilterCustomFields, MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import ExecuteStatusTag from '@/components/business/ms-case-associate/executeResult.vue';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import caseLevel from './caseLevel.vue';
  import TreeFolderAll from '@/views/api-test/components/treeFolderAll.vue';

  import { getAssociatedProjectOptions, getCustomFieldsTable } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { mapTree } from '@/utils';

  import type { CaseManagementTable } from '@/models/caseManagement/featureCase';
  import type { CommonList, ModuleTreeNode, TableQueryParams } from '@/models/common';
  import type { ProjectListItem } from '@/models/setting/project';
  import { FilterType, ViewTypeEnum } from '@/enums/advancedFilterEnum';
  import { ProtocolKeyEnum } from '@/enums/apiEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { initGetModuleCountFunc, type RequestModuleEnum } from './utils';
  import { casePriorityOptions } from '@/views/api-test/components/config';
  import {
    executionResultMap,
    getCaseLevels,
    statusIconMap,
  } from '@/views/case-management/caseManagementFeature/components/utils';

  const router = useRouter();
  const appStore = useAppStore();
  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      visible: boolean;
      projectId?: string; // 项目id
      caseId?: string; // 用例id  用例评审那边不需要传递
      getModulesFunc: (params: TableQueryParams) => Promise<ModuleTreeNode[]>; // 获取模块树请求
      modulesParams?: Record<string, any>; // 获取模块树请求
      getTableFunc: (params: TableQueryParams) => Promise<CommonList<CaseManagementTable>>; // 获取表请求函数
      tableParams?: TableQueryParams; // 查询表格的额外的参数
      okButtonDisabled?: boolean; // 确认按钮是否禁用
      currentSelectCase: CaseLinkEnum; // 当前选中的用例类型
      moduleOptions?: { label: string; value: string }[]; // 功能模块对应用例下拉
      confirmLoading: boolean;
      associatedIds: string[]; // 已关联用例id集合用于去重已关联
      hasNotAssociatedIds?: string[];
      type: RequestModuleEnum[keyof RequestModuleEnum];
      moduleCountParams?: TableQueryParams; // 获取模块树数量额外的参数
      hideProjectSelect?: boolean; // 是否隐藏项目选择
      isHiddenCaseLevel?: boolean;
      selectorAll?: boolean;
      viewType?: ViewTypeEnum;
    }>(),
    {
      isHiddenCaseLevel: false,
      selectorAll: false,
    }
  );

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'update:projectId', val: string): void;
    (e: 'init', val: TableQueryParams): void; // 初始化模块数量
    (e: 'close'): void;
    (e: 'save', params: any): void; // 保存对外传递关联table 相关参数
  }>();

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

  const moduleKeyword = ref('');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const moduleLoading = ref(false);

  const selectedModuleKeys = ref<string[]>([]);

  function setActiveFolder(id: string) {
    activeFolder.value = id;
    activeFolderName.value = t('ms.case.associate.allCase');
    selectedModuleKeys.value = [];
  }

  const innerVisible = useVModel(props, 'visible', emit);
  const innerProject = ref<string | undefined>(props.projectId);

  const modulesCount = ref<Record<string, any>>({});
  const isExpandAll = ref(false);

  const caseType = ref<CaseLinkEnum>(props.currentSelectCase);

  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */
  async function initModules(isSetDefaultKey = false) {
    try {
      moduleLoading.value = true;
      let params = {
        projectId: innerProject.value,
        sourceType: props.moduleOptions && props.moduleOptions.length ? caseType.value : undefined,
        sourceId: props.moduleOptions && props.moduleOptions.length ? props.caseId : undefined,
      };
      if (props.modulesParams) {
        params = {
          ...params,
          ...props.modulesParams,
        };
      }
      const res = await props.getModulesFunc(params);
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
        [activeFolder.value] = [folderTree.value[0].id];
        activeFolderName.value = folderTree.value[0].name;
        const offspringIds: string[] = [];
        mapTree(folderTree.value[0].children || [], (e) => {
          offspringIds.push(e.id);
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
  const version = ref('');
  const projectList = ref<ProjectListItem[]>([]);

  function getCaseLevelColumn() {
    if (!props.isHiddenCaseLevel) {
      return [
        {
          title: 'ms.case.associate.caseLevel',
          dataIndex: 'caseLevel',
          slotName: 'caseLevel',
          width: 150,
          filterConfig: {
            options: casePriorityOptions,
            filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
          },
        },
      ];
    }
    return [];
  }

  const reviewResultOptions = computed(() => {
    return Object.keys(statusIconMap).map((key) => {
      return {
        value: key,
        label: statusIconMap[key].statusText,
      };
    });
  });
  const executeResultOptions = computed(() => {
    return Object.keys(executionResultMap).map((key) => {
      return {
        value: key,
        label: executionResultMap[key].statusText,
      };
    });
  });

  function getReviewStatus() {
    if (!props.isHiddenCaseLevel) {
      return [
        {
          title: 'caseManagement.featureCase.tableColumnReviewResult',
          dataIndex: 'reviewStatus',
          slotName: 'reviewStatus',
          filterConfig: {
            options: reviewResultOptions.value,
            filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_REVIEW_RESULT,
          },
          showInTable: true,
          width: 150,
          showDrag: true,
        },
        {
          title: 'caseManagement.featureCase.tableColumnExecutionResult',
          dataIndex: 'lastExecuteResult',
          slotName: 'lastExecuteResult',
          filterConfig: {
            options: executeResultOptions.value,
            filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT,
          },
          showInTable: true,
          width: 150,
          showDrag: true,
        },
      ];
    }
    return [];
  }

  const columns = computed<MsTableColumn>(() => {
    return [
      {
        title: 'ID',
        dataIndex: 'num',
        slotName: 'num',
        sortIndex: 1,
        showTooltip: true,
        sortable: {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        },
        width: 120,
      },
      {
        title: 'ms.case.associate.caseName',
        dataIndex: 'name',
        sortable: {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        },
        showTooltip: true,
        width: 250,
      },
      ...getCaseLevelColumn(),
      ...getReviewStatus(),
      {
        title: 'ms.case.associate.tags',
        dataIndex: 'tags',
        isTag: true,
      },
      {
        title: 'caseManagement.featureCase.tableColumnCreateUser',
        slotName: 'createUserName',
        dataIndex: 'createUser',
        showTooltip: true,
        width: 200,
        filterConfig: {
          mode: 'remote',
          loadOptionParams: {
            projectId: innerProject.value,
          },
          remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
        },
        showDrag: true,
      },
      {
        title: 'caseManagement.featureCase.tableColumnCreateTime',
        slotName: 'createTime',
        dataIndex: 'createTime',
        sortable: {
          sortDirections: ['ascend', 'descend'],
          sorter: true,
        },
        width: 200,
        showDrag: true,
      },
    ];
  });

  watchEffect(() => {
    getCaseLevelColumn();
  });

  const {
    propsRes,
    propsEvent,
    viewId,
    advanceFilter,
    setAdvanceFilter,
    loadList,
    setLoadListParams,
    resetSelector,
    setTableSelected,
    resetFilterParams,
  } = useTable(
    props.getTableFunc,
    {
      columns: columns.value,
      tableKey: TableKeyEnum.CASE_MANAGEMENT_ASSOCIATED_TABLE,
      scroll: { x: '100%' },
      showSetting: false,
      selectable: true,
      showSelectAll: true,
      heightUsed: 310,
      showSelectorAll: !props.selectorAll,
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

  const searchParams = ref<TableQueryParams>({
    moduleIds: [],
    version: version.value,
  });

  const treeFolderAllRef = ref<InstanceType<typeof TreeFolderAll>>();
  const selectedProtocols = computed<string[]>(() => treeFolderAllRef.value?.selectedProtocols ?? []);

  const msAdvanceFilterRef = ref<InstanceType<typeof MsAdvanceFilter>>();
  const isAdvancedSearchMode = computed(() => msAdvanceFilterRef.value?.isAdvancedSearchMode);
  function getLoadListParams() {
    if (activeFolder.value === 'all' || !activeFolder.value || isAdvancedSearchMode.value) {
      searchParams.value.moduleIds = [];
    } else {
      searchParams.value.moduleIds = [activeFolder.value, ...offspringIds.value];
    }
    if (props.moduleOptions && props.moduleOptions.length) {
      searchParams.value.sourceType = caseType.value;
      searchParams.value.sourceId = props.caseId;
    }
    setLoadListParams({
      ...searchParams.value,
      ...props.tableParams,
      keyword: keyword.value,
      viewId: viewId.value,
      combineSearch: advanceFilter,
      projectId: innerProject.value,
      excludeIds: [...props.associatedIds], // 已经存在的关联的id列表
      condition: {
        keyword: keyword.value,
        filter: propsRes.value.filter,
      },
      protocols: caseType.value === 'API' ? selectedProtocols.value : [],
    });
    if (props.hasNotAssociatedIds && props.hasNotAssociatedIds.length > 0) {
      props.hasNotAssociatedIds.forEach((hasNotAssociatedId) => {
        setTableSelected(hasNotAssociatedId);
      });
    }
  }

  const searchCustomFields = ref<FilterFormItem[]>([]);
  const filterConfigList = ref<FilterFormItem[]>([]);

  async function initFilter() {
    if (props.viewType !== ViewTypeEnum.FUNCTIONAL_CASE) return;
    try {
      const result = await getCustomFieldsTable(appStore.currentProjectId);
      searchCustomFields.value = getFilterCustomFields(result); // 处理自定义字段
      filterConfigList.value = [
        {
          title: 'caseManagement.featureCase.tableColumnID',
          dataIndex: 'num',
          type: FilterType.INPUT,
        },
        {
          title: 'caseManagement.featureCase.tableColumnName',
          dataIndex: 'name',
          type: FilterType.INPUT,
        },
        {
          title: 'common.belongModule',
          dataIndex: 'moduleId',
          type: FilterType.TREE_SELECT,
          treeSelectData: folderTree.value,
          treeSelectProps: {
            fieldNames: {
              title: 'name',
              key: 'id',
              children: 'children',
            },
            multiple: true,
            treeCheckable: true,
            treeCheckStrictly: true,
          },
        },
        {
          title: 'caseManagement.featureCase.tableColumnReviewResult',
          dataIndex: 'reviewStatus',
          type: FilterType.SELECT,
          selectProps: {
            multiple: true,
            options: reviewResultOptions.value,
          },
        },
        {
          title: 'caseManagement.featureCase.tableColumnExecutionResult',
          dataIndex: 'lastExecuteResult',
          type: FilterType.SELECT,
          selectProps: {
            multiple: true,
            options: executeResultOptions.value,
          },
        },
        {
          title: 'caseManagement.featureCase.associatedDemand',
          dataIndex: 'demand',
          type: FilterType.INPUT,
        },
        {
          title: 'caseManagement.featureCase.relatedAttachments',
          dataIndex: 'attachment',
          type: FilterType.INPUT,
        },
        {
          title: 'common.creator',
          dataIndex: 'createUser',
          type: FilterType.MEMBER,
        },
        {
          title: 'common.createTime',
          dataIndex: 'createTime',
          type: FilterType.DATE_PICKER,
        },
        {
          title: 'common.updateUserName',
          dataIndex: 'updateUser',
          type: FilterType.MEMBER,
        },
        {
          title: 'common.updateTime',
          dataIndex: 'updateTime',
          type: FilterType.DATE_PICKER,
        },
        {
          title: 'common.tag',
          dataIndex: 'tags',
          type: FilterType.TAGS_INPUT,
          numberProps: {
            min: 0,
            precision: 0,
          },
        },
      ];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 初始化模块数量
  async function initModuleCount() {
    try {
      const params = {
        keyword: keyword.value,
        moduleIds: selectedModuleKeys.value,
        projectId: innerProject.value,
        current: propsRes.value.msPagination?.current,
        pageSize: propsRes.value.msPagination?.pageSize,
        sourceId: props.caseId,
        sourceType: caseType.value,
        ...props.tableParams,
        ...props.moduleCountParams,
        filter: propsRes.value.filter,
        protocols: caseType.value === 'API' ? selectedProtocols.value : [],
      };
      modulesCount.value = await initGetModuleCountFunc(props.type, params);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function searchCase() {
    getLoadListParams();
    loadList();
    if (!isAdvancedSearchMode.value) {
      initModuleCount();
    }
  }

  function setAllSelectModule() {
    nextTick(() => {
      if (activeFolder.value !== 'all') {
        setActiveFolder('all');
      } else {
        searchCase();
      }
    });
  }

  // 高级检索
  const handleAdvSearch = (filter: FilterResult, id: string) => {
    resetSelector();
    setActiveFolder('all');
    keyword.value = '';
    setAdvanceFilter(filter, id);
    searchCase(); // 基础筛选都清空
  };

  async function initProjectList(setDefault: boolean) {
    try {
      projectList.value = await getAssociatedProjectOptions(appStore.currentOrgId, caseType.value);
      if (setDefault) {
        innerProject.value = projectList.value[0].id;
      }
      resetSelector();
      resetFilterParams();
      await initModules();
      setAllSelectModule();
      initFilter();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 保存参数
  function handleConfirm() {
    const { excludeKeys, selectedKeys, selectorStatus } = propsRes.value;
    const { versionId, moduleIds } = searchParams.value;
    const params = {
      excludeIds: [...excludeKeys],
      selectIds: selectorStatus === 'all' ? [] : [...selectedKeys],
      selectAll: selectorStatus === 'all',
      moduleIds,
      versionId,
      refId: '',
      sourceType: caseType.value,
      projectId: innerProject.value,
      sourceId: props.caseId,
      totalCount: propsRes.value.msPagination?.total,
      condition: {
        keyword: keyword.value,
        filter: propsRes.value.filter,
        viewId: viewId.value,
        combineSearch: advanceFilter,
      },
    };

    emit('save', params);
  }

  function openDetail(id: string) {
    window.open(
      `${window.location.origin}#${
        router.resolve({ name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE }).fullPath
      }?id=${id}`
    );
  }

  function cancel() {
    innerVisible.value = false;
    keyword.value = '';
    version.value = '';
    searchParams.value = {
      moduleIds: [],
      version: '',
    };
    innerProject.value = props.projectId ?? '';
    activeFolder.value = 'all';
    activeFolderName.value = t('ms.case.associate.allCase');
    resetSelector();
    emit('close');
  }

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        caseType.value = props.currentSelectCase;
        resetFilterParams();
        resetSelector();
        if (!props.hideProjectSelect) {
          initProjectList(true);
        }
      } else {
        cancel();
      }
    }
  );

  watch(
    () => props.currentSelectCase,
    (val) => {
      caseType.value = val;
    },
    {
      immediate: true,
    }
  );

  // 改变关联类型
  async function changeCaseType(
    value: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
  ) {
    caseType.value = value as CaseLinkEnum;
    await initModules();
    setAllSelectModule();
    initFilter();
  }

  async function selectedProtocolsChange() {
    if (innerProject.value) {
      await initModules();
      setAllSelectModule();
      initFilter();
    }
  }
  // 改变项目
  async function changeProject(
    value: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
  ) {
    innerProject.value = value as string;
    resetFilterParams();
    await initModules();
    setAllSelectModule();
    initFilter();
  }

  watch(
    () => activeFolder.value,
    () => {
      if (!isAdvancedSearchMode.value && innerProject.value) {
        searchCase();
      }
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

  function filterChange() {
    initModuleCount();
  }

  defineExpose({
    initModules,
  });
</script>

<style lang="less" scoped>
  .footer {
    @apply flex items-center justify-between;

    margin: auto -16px -16px;
    padding: 12px 16px;
    box-shadow: 0 -1px 4px 0 rgb(31 35 41 / 10%);
  }
  .expand-btn {
    padding: 8px;
    .arco-icon {
      color: var(--color-text-4);
    }
    &:hover {
      border-color: rgb(var(--primary-5)) !important;
      background-color: rgb(var(--primary-1)) !important;
      .arco-icon {
        color: rgb(var(--primary-5));
      }
    }
  }
</style>
