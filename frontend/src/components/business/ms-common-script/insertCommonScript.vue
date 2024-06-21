<template>
  <MsDrawer
    v-model:visible="insertScriptDrawer"
    :title="
      props.enableRadioSelected
        ? t('project.commonScript.quoteCommonScript')
        : t('project.commonScript.insertCommonScript')
    "
    :width="960"
    unmount-on-close
    :show-continue="false"
    :ok-loading="drawerLoading"
    :ok-disabled="!isDisabled"
    :ok-text="t('project.commonScript.apply')"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <div class="mb-4 flex items-center justify-between">
      <a-button v-permission="['PROJECT_CUSTOM_FUNCTION:READ+ADD']" type="outline" @click="addCommonScript">
        {{ t('project.commonScript.addPublicScript') }}
      </a-button>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('apiScenario.params.searchPlaceholder')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
        @clear="searchList"
      ></a-input-search>
    </div>

    <ms-base-table v-bind="propsRes" v-model:selected-key="innerCheckedKey" v-on="propsEvent">
      <template #name="{ record }">
        <div class="flex items-center">
          <a-tooltip :content="record.name">
            <div class="one-line-text max-w-[200px] cursor-pointer text-[rgb(var(--primary-5))]">{{
              characterLimit(record.name)
            }}</div>
          </a-tooltip>
          <a-popover :title="record.name" position="bottom">
            <a-button type="text" class="ml-2 px-0"> {{ t('project.commonScript.preview') }}</a-button>
            <template #title>
              <div class="w-[436px] bg-[var(--color-bg-3)] px-2 pb-2">
                <span style="word-break: break-all"> {{ characterLimit(record.name) }} </span>
              </div>
            </template>
            <template #content>
              <div class="w-[436px] bg-[var(--color-bg-3)] px-2 pb-2">
                <MsCodeEditor
                  v-model:model-value="record.script"
                  :show-theme-change="false"
                  title=""
                  width="100%"
                  height="376px"
                  theme="MS-text"
                  :read-only="true"
                  :show-full-screen="false"
                />
              </div>
            </template>
          </a-popover>
        </div>
      </template>
      <template #createUserFilter="{ columnConfig }">
        <TableFilter
          v-model:visible="createUserFilterVisible"
          v-model:status-filters="createUserFilterValue"
          :title="(columnConfig.title as string)"
          :list="createUserFilterOptions"
          value-key="value"
          @search="initData()"
        >
          <template #item="{ item }">
            {{ item.text }}
          </template>
        </TableFilter>
      </template>
      <template #status="{ record }">
        <a-select
          v-if="hasAnyPermission(['PROJECT_CUSTOM_FUNCTION:READ+UPDATE'])"
          v-model:model-value="record.status"
          class="param-input w-[110px]"
          size="mini"
          @change="() => handleStatusChange(record)"
        >
          <template #label>
            <commonScriptStatus :status="record.status" />
          </template>
          <a-option :key="CommonScriptStatusEnum.PASSED" :value="CommonScriptStatusEnum.PASSED">
            <commonScriptStatus :status="CommonScriptStatusEnum.PASSED" />
          </a-option>
          <a-option :key="CommonScriptStatusEnum.DRAFT" :value="CommonScriptStatusEnum.DRAFT">
            <commonScriptStatus :status="CommonScriptStatusEnum.DRAFT" />
          </a-option>
        </a-select>
        <commonScriptStatus v-else :status="record.status" />
      </template>
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
              <div class="flex w-full items-center justify-start overflow-hidden px-[12px] py-[2px]">
                <a-checkbox-group v-model:model-value="statusFilters" direction="vertical" size="small">
                  <a-checkbox :key="CommonScriptStatusEnum.PASSED" :value="CommonScriptStatusEnum.PASSED">
                    <commonScriptStatus :status="CommonScriptStatusEnum.PASSED" />
                  </a-checkbox>
                  <a-checkbox :key="CommonScriptStatusEnum.DRAFT" :value="CommonScriptStatusEnum.DRAFT">
                    <commonScriptStatus :status="CommonScriptStatusEnum.DRAFT" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
              <div class="filter-button">
                <a-button size="mini" class="mr-[8px]" @click="resetStatusFilter">
                  {{ t('common.reset') }}
                </a-button>
                <a-button type="primary" size="mini" @click="handleFilterHidden(false)">
                  {{ t('system.orgTemplate.confirm') }}
                </a-button>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>
      <template #enable="{ record }">
        <MsTag v-if="record.enable" type="success" theme="light">{{ t('project.commonScript.testsPass') }}</MsTag>
        <MsTag v-else>{{ t('project.commonScript.draft') }}</MsTag>
      </template>
    </ms-base-table>
  </MsDrawer>
  <AddScriptDrawer
    v-model:visible="showScriptDrawer"
    v-model:params="paramsList"
    :confirm-loading="confirmLoading"
    :script-id="isEditId"
    :enable-radio-selected="radioSelected"
    @save="saveHandler"
  />
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { Language } from '@/components/pure/ms-code-editor/types';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import TableFilter from '@/views/case-management/caseManagementFeature/components/tableFilter.vue';
  import commonScriptStatus from '@/views/project-management/commonScript/components/commonScriptStatus.vue';

  import {
    addOrUpdateCommonScriptReq,
    getCustomFuncColumnOption,
    getInsertCommonScriptPage,
    updateStatusCommonScript,
  } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { BugOptionItem } from '@/models/bug-management';
  import type {
    AddOrUpdateCommonScript,
    CommonScriptItem,
    ParamsRequestType,
  } from '@/models/projectManagement/commonScript';
  import { CommonScriptStatusEnum } from '@/enums/commonScriptStatusEnum';

  import Message from '@arco-design/web-vue/es/message';
  import debounce from 'lodash-es/debounce';

  const statusFilterVisible = ref(false);
  const statusFilters = ref<string[]>([]);

  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const AddScriptDrawer = defineAsyncComponent(
    () => import('@/components/business/ms-common-script/ms-addScriptDrawer.vue')
  );
  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      visible: boolean;
      scriptLanguage?: Language;
      enableRadioSelected?: boolean; // 是否单选开启
      okText?: string;
      checkedId?: string; // 单选时默认选中的id
    }>(),
    {
      checkedId: '',
    }
  );

  const emit = defineEmits(['update:visible', 'update:checkedId', 'save']);
  const insertScriptDrawer = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emit('update:visible', val);
    },
  });
  const createUserFilterOptions = ref<BugOptionItem[]>([]);
  const createUserFilterVisible = ref(false);
  const createUserFilterValue = ref<string[]>([]);
  const keyword = ref<string>('');

  const columns: MsTableColumn = [
    {
      title: 'project.commonScript.name',
      dataIndex: 'name',
      slotName: 'name',
      width: 300,
      showInTable: true,
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      showDrag: true,
      showTooltip: true,
      width: 150,
    },
    {
      title: 'project.commonScript.enable',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      showInTable: true,
      width: 150,
      showDrag: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
    {
      title: 'project.commonScript.tags',
      dataIndex: 'tags',
      showInTable: true,
      isTag: true,
      width: 440,
      showDrag: true,
      tagPosition: 'tr',
    },
    {
      title: 'project.commonScript.createUser',
      dataIndex: 'createUserName',
      titleSlotName: 'createUserFilter',
      showInTable: true,
      showDrag: true,
      showTooltip: true,
      width: 150,
    },
    {
      title: 'project.commonScript.createTime',
      dataIndex: 'createTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
      showDrag: true,
      showTooltip: true,
      width: 200,
    },
    {
      title: 'system.resourcePool.tableColumnUpdateTime',
      dataIndex: 'updateTime',
      showDrag: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showTooltip: true,
      width: 200,
    },
  ];

  const innerCheckedKey = defineModel<string>('checkedId', { default: '' });

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getInsertCommonScriptPage,
    {
      columns,
      scroll: {
        x: 1300,
      },
      showSetting: false,
      selectable: true,
      selectorType: props.enableRadioSelected ? 'radio' : 'checkbox',
      heightUsed: 300,
      showSelectAll: !props.enableRadioSelected,
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

  const drawerLoading = ref<boolean>(false);

  const isDisabled = computed(() => {
    if (props.enableRadioSelected) {
      return !!innerCheckedKey.value;
    }
    return propsRes.value.selectedKeys.size > 0;
  });

  function initData() {
    setLoadListParams({
      keyword: keyword.value,
      projectId: currentProjectId.value,
      type: props.scriptLanguage,
      filter: {
        createUser: createUserFilterValue.value,
        status: statusFilters.value,
      },
    });
    loadList();
  }

  const searchList = debounce(() => {
    initData();
  }, 100);

  function handleDrawerConfirm() {
    if (props.enableRadioSelected) {
      emit(
        'save',
        propsRes.value.data.find((item) => item.id === innerCheckedKey.value)
      );
    } else {
      const selectKeysIds = [...propsRes.value.selectedKeys];
      emit(
        'save',
        propsRes.value.data.filter((item) => selectKeysIds.includes(item.id))
      );
    }
  }

  function handleDrawerCancel() {
    insertScriptDrawer.value = false;
  }
  const showScriptDrawer = ref<boolean>(false);
  function addCommonScript() {
    showScriptDrawer.value = true;
  }

  const paramsList = ref<ParamsRequestType[]>([]);
  const confirmLoading = ref<boolean>(false);
  const isEditId = ref<string>('');
  const radioSelected = ref<boolean>(false);

  // 保存自定义代码片段应用
  async function saveHandler(form: AddOrUpdateCommonScript) {
    try {
      confirmLoading.value = true;
      const { status } = form;
      const paramTableList = paramsList.value.slice(0, -1);
      const paramsObj: AddOrUpdateCommonScript = {
        ...form,
        status: status || 'DRAFT',
        projectId: currentProjectId.value,
        params: JSON.stringify(paramTableList),
      };
      await addOrUpdateCommonScriptReq(paramsObj);
      showScriptDrawer.value = false;
      initData();
      Message.success(form.id ? t('common.updateSuccess') : t('common.createSuccess'));
    } catch (error) {
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  async function initFilterOptions() {
    const res = await getCustomFuncColumnOption(appStore.currentProjectId);
    createUserFilterOptions.value = res.userOption;
  }
  async function handleStatusChange(record: CommonScriptItem) {
    try {
      await updateStatusCommonScript({
        id: record.id,
        status: record.status,
        projectId: record.projectId,
      });
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  function resetStatusFilter() {
    statusFilterVisible.value = false;
    statusFilters.value = [];
    initData();
  }
  function handleFilterHidden(val: boolean) {
    if (!val) {
      statusFilterVisible.value = false;
      initData();
    }
  }
  watch(
    () => props.visible,
    (val) => {
      if (val) {
        resetSelector();
        initFilterOptions();
        initData();
      }
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
