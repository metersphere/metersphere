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
      <div class="float-left">
        <a-select
          v-model="innerProject"
          class="ml-2 w-[240px]"
          :default-value="innerProject"
          allow-search
          :placeholder="t('common.pleaseSelect')"
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
    </template>
    <MsTab
      v-model:active-key="activeTab"
      :show-badge="false"
      :content-tab-list="contentTabList"
      class="no-content relative border-b"
    />
    <div class="flex h-[calc(100vh-104px)]">
      <div class="w-[292px] border-r border-[var(--color-text-n8)] p-[16px]">
        <CaseTree
          ref="caseTreeRef"
          :modules-count="modulesCount"
          :selected-keys="selectedKeys"
          :get-modules-api-type="props.getModulesApiType"
          :current-project="innerProject"
          :active-tab="activeTab"
          :extra-modules-params="props.extraModulesParams"
          @folder-node-select="handleFolderNodeSelect"
          @init="initModuleTree"
        />
      </div>
      <div class="flex w-[calc(100%-293px)] flex-col p-[16px]">
        <MsAdvanceFilter
          v-model:keyword="keyword"
          :filter-config-list="[]"
          :custom-fields-config-list="[]"
          :row-count="0"
          :search-placeholder="t('ms.case.associate.searchPlaceholder')"
          @keyword-search="loadCaseList"
          @adv-search="loadCaseList"
          @refresh="loadCaseList"
        >
          <template #left>
            <div class="flex w-full items-center justify-between">
              <a-radio-group v-if="activeTab === 'API'" v-model="showType" type="button" class="file-show-type mr-2">
                <a-radio value="API" class="show-type-icon p-[2px]">API</a-radio>
                <a-radio value="CASE" class="show-type-icon p-[2px]">CASE</a-radio>
              </a-radio-group>
              <a-popover v-else title="" position="bottom">
                <div class="flex">
                  <div class="one-line-text mr-1 max-h-[32px] max-w-[300px] text-[var(--color-text-1)]">
                    {{ activeFolderName }}
                  </div>
                  <span class="text-[var(--color-text-4)]"> ({{ modulesCount[activeFolder] || 0 }})</span>
                </div>
                <template #content>
                  <div class="max-w-[400px] text-[14px] font-medium text-[var(--color-text-1)]">
                    {{ activeFolderName }}
                    <span class="text-[var(--color-text-4)]">({{ modulesCount[activeFolder] || 0 }})</span>
                  </div>
                </template>
              </a-popover>
              <a-checkbox v-if="activeTab === 'FUNCTIONAL'" v-model="isAddAssociatedCase">
                <div class="flex items-center">
                  {{ t('ms.case.associate.addAssociatedCase') }}
                  <a-tooltip position="top" :content="t('ms.case.associate.automaticallyAddApiCase')">
                    <icon-question-circle
                      class="ml-[4px] mr-[12px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                      size="16"
                    />
                  </a-tooltip>
                </div>
              </a-checkbox>
            </div>
          </template>
        </MsAdvanceFilter>
        <!-- 功能用例 -->
        <CaseTable
          v-if="activeTab === CaseLinkEnum.FUNCTIONAL"
          ref="functionalTableRef"
          :association-type="associateType"
          :get-page-api-type="getPageApiType"
          :active-module="activeFolder"
          :offspring-ids="offspringIds"
          :current-project="innerProject"
          :associated-ids="props.associatedIds"
          :active-source-type="activeTab"
          :extra-table-params="props.extraTableParams"
          :keyword="keyword"
          @get-module-count="initModulesCount"
        />
        <!-- 接口用例 API -->
        <ApiTable
          v-if="activeTab === CaseLinkEnum.API && showType === 'API'"
          ref="apiTableRef"
          :get-page-api-type="getPageApiType"
          :extra-table-params="props.extraTableParams"
          :association-type="associateType"
          :active-module="activeFolder"
          :offspring-ids="offspringIds"
          :current-project="innerProject"
          :associated-ids="props.associatedIds"
          :active-source-type="activeTab"
          :keyword="keyword"
          :show-type="showType"
          @get-module-count="initModulesCount"
        />
        <!-- 接口用例 CASE -->
        <ApiCaseTable
          v-if="activeTab === CaseLinkEnum.API && showType === 'CASE'"
          ref="caseTableRef"
          :get-page-api-type="getPageApiType"
          :extra-table-params="props.extraTableParams"
          :association-type="associateType"
          :active-module="activeFolder"
          :offspring-ids="offspringIds"
          :current-project="innerProject"
          :associated-ids="props.associatedIds"
          :active-source-type="activeTab"
          :keyword="keyword"
          :show-type="showType"
          @get-module-count="initModulesCount"
        />
        <!-- 接口场景用例 -->
        <ScenarioCaseTable
          v-if="activeTab === CaseLinkEnum.SCENARIO"
          ref="scenarioTableRef"
          :association-type="associateType"
          :modules-count="modulesCount"
          :active-module="activeFolder"
          :offspring-ids="offspringIds"
          :current-project="innerProject"
          :associated-ids="props.associatedIds"
          :active-source-type="activeTab"
          :keyword="keyword"
          @get-module-count="initModulesCount"
        />

        <div class="footer">
          <div class="flex flex-1 items-center">
            <slot name="footerLeft">
              <a-form ref="formRef" :model="form" layout="vertical" class="mb-0 max-w-[260px]">
                <a-form-item
                  field="name"
                  hide-label
                  class="test-set-form-item"
                  :rules="[{ required: true, message: t('project.commonScript.publicScriptNameNotEmpty') }]"
                >
                  <a-input-group class="w-full">
                    <div class="test-set h-[32px] w-[80px]">{{ t('ms.case.associate.testSet') }}</div>
                    <a-select
                      v-model="form.testMap"
                      class="max-w-[260px]"
                      :default-value="innerProject"
                      allow-search
                      :placeholder="t('common.pleaseSelect')"
                    >
                      <template #arrow-icon>
                        <icon-caret-down />
                      </template>

                      <a-tooltip
                        v-for="item of testList"
                        :key="item.value"
                        :mouse-enter-delay="500"
                        :content="item.name"
                      >
                        <a-option
                          :value="item.value"
                          :class="item.value === form.testMap ? 'arco-select-option-selected' : ''"
                        >
                          {{ item.label }}
                        </a-option>
                      </a-tooltip>
                    </a-select>
                  </a-input-group>
                </a-form-item>
              </a-form>
            </slot>
          </div>
          <div class="flex items-center">
            <slot name="footerRight">
              <a-button type="secondary" :disabled="props.confirmLoading" class="mr-[12px]" @click="cancel">
                {{ t('common.cancel') }}
              </a-button>
              <a-button :loading="props.confirmLoading" type="primary" @click="handleConfirm">
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
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';
  import { FormInstance, SelectOptionData, ValidatedError } from '@arco-design/web-vue';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import ApiCaseTable from './apiCaseTable.vue';
  import ApiTable from './apiTable.vue';
  import CaseTable from './caseTable.vue';
  import CaseTree from './caseTree.vue';
  import ScenarioCaseTable from './scenarioCaseTable.vue';

  import { getAssociatedProjectOptions } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { ModuleTreeNode, TableQueryParams } from '@/models/common';
  import type { ProjectListItem } from '@/models/setting/project';
  import { CaseModulesApiTypeEnum, CasePageApiTypeEnum } from '@/enums/associateCaseEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';

  import { initGetModuleCountFunc } from './utils/moduleCount';

  const appStore = useAppStore();
  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    projectId: string; // 项目id
    caseId?: string; // 用例id
    getModulesApiType: CaseModulesApiTypeEnum[keyof CaseModulesApiTypeEnum]; // 获取模块树Api
    extraModulesParams?: Record<string, any>; // 获取模块树请求额外参数
    getPageApiType: keyof typeof CasePageApiTypeEnum; // 获取未关联分页Api
    extraTableParams?: TableQueryParams; // 查询表格的额外参数
    getModuleCountApiType: CasePageApiTypeEnum[keyof CasePageApiTypeEnum]; // 获取模块count分页Api
    extraModuleCountParams?: TableQueryParams; // 查询模块数量额外参数
    okButtonDisabled?: boolean; // 确认按钮是否禁用
    confirmLoading?: boolean;
    associatedIds?: string[]; // 已关联用例id集合用于去重已关联
    hideProjectSelect?: boolean; // 是否隐藏项目选择
  }>();
  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'update:projectId', val: string): void;
    (e: 'update:currentSelectCase', val: string | number | Record<string, any> | undefined): void;
    (e: 'init', val: TableQueryParams): void; // 初始化模块数量
    (e: 'close'): void;
    (e: 'save', params: any): void; // 保存对外传递关联table 相关参数
  }>();

  const projectList = ref<ProjectListItem[]>([]);
  const keyword = ref<string>('');
  const innerProject = useVModel(props, 'projectId', emit);
  const showType = ref('API');
  const innerVisible = useVModel(props, 'visible', emit);

  const associateType = ref<string>('project');

  const modulesCount = ref<Record<string, any>>({});

  const activeTab = ref<keyof typeof CaseLinkEnum>(CaseLinkEnum.FUNCTIONAL);
  const form = ref({
    type: t('ms.case.associate.testSet'),
    testMap: '',
  });

  const testList = ref<SelectOptionData>([]);

  const contentTabList = [
    {
      value: CaseLinkEnum.FUNCTIONAL,
      label: t('ms.case.associate.functionalCase'),
    },
    {
      value: CaseLinkEnum.API,
      label: t('ms.case.associate.apiCase'),
    },
    {
      value: CaseLinkEnum.SCENARIO,
      label: t('ms.case.associate.apiScenarioCase'),
    },
  ];
  const activeFolder = ref('all');
  const activeFolderName = ref(t('ms.case.associate.allCase'));

  const selectedKeys = computed({
    get: () => [activeFolder.value],
    set: (val) => val,
  });

  /**
   * 处理模块树节点选中事件
   */
  const offspringIds = ref<string[]>([]);

  function handleFolderNodeSelect(ids: string[], _offspringIds: string[], name?: string) {
    [activeFolder.value] = ids;
    offspringIds.value = [..._offspringIds];
    activeFolderName.value = name ?? '';
  }

  const moduleTree = ref<ModuleTreeNode[]>([]);

  function initModuleTree(tree: ModuleTreeNode[]) {
    moduleTree.value = unref(tree);
  }

  const isAddAssociatedCase = ref<boolean>(false);
  const formRef = ref<FormInstance | null>(null);
  const functionalTableRef = ref<InstanceType<typeof CaseTable>>();
  const apiTableRef = ref<InstanceType<typeof ApiTable>>();
  const caseTableRef = ref<InstanceType<typeof ApiCaseTable>>();
  const scenarioTableRef = ref<InstanceType<typeof ScenarioCaseTable>>();

  function makeParams() {
    switch (activeTab.value) {
      case CaseLinkEnum.FUNCTIONAL:
        return functionalTableRef.value?.getFunctionalSaveParams();
      case CaseLinkEnum.API:
        return showType.value === 'API'
          ? apiTableRef.value?.getApiSaveParams()
          : caseTableRef.value?.getApiCaseSaveParams();
      case CaseLinkEnum.SCENARIO:
        return scenarioTableRef.value?.getScenarioSaveParams();
      default:
        break;
    }
  }
  // 保存
  function handleConfirm() {
    const params = makeParams();
    if (!params?.selectIds.length) {
      return;
    }
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        // emit('save', params);
      }
    });
    // TODO: 待联调 先不加测试集允许关联
    emit('save', params);
  }

  function cancel() {
    innerVisible.value = false;
    keyword.value = '';
    activeFolder.value = 'all';
    activeFolderName.value = t('ms.case.associate.allCase');
    formRef.value?.resetFields();
    emit('close');
  }

  async function initProjectList(setDefault: boolean) {
    try {
      projectList.value = await getAssociatedProjectOptions(appStore.currentOrgId, activeTab.value);
      if (setDefault) {
        innerProject.value = projectList.value[0].id;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function initModulesCount(params: TableQueryParams) {
    try {
      modulesCount.value = await initGetModuleCountFunc(props.getModuleCountApiType, activeTab.value, {
        ...params,
        ...props.extraModuleCountParams,
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watch(
    () => activeTab.value,
    (val) => {
      if (val) {
        showType.value = 'API';
        activeFolder.value = 'all';
        initProjectList(true);
      }
    }
  );

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        initProjectList(false);
        innerProject.value = appStore.currentProjectId;
      }
      activeTab.value = CaseLinkEnum.FUNCTIONAL;
    }
  );

  watch(
    () => innerProject.value,
    (val) => {
      if (val) {
        activeFolder.value = 'all';
      }
    }
  );

  function loadCaseList() {
    switch (activeTab.value) {
      case CaseLinkEnum.FUNCTIONAL:
        return functionalTableRef.value?.loadCaseList();
      case CaseLinkEnum.API:
        return showType.value === 'API' ? apiTableRef.value?.loadApiList() : caseTableRef.value?.loadCaseList();
      case CaseLinkEnum.SCENARIO:
        return scenarioTableRef.value?.loadScenarioList();
      default:
        break;
    }
  }
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
  :deep(.test-set-form-item) {
    margin-bottom: 0;
    .test-set {
      border: 1px solid var(--color-text-n8);
      border-right: none;
      @apply flex items-center justify-center;
    }
  }
</style>
