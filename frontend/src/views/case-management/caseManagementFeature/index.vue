<template>
  <MsCard simple no-content-padding>
    <MsSplitBox>
      <template #first>
        <div class="p-[16px] pb-0">
          <div class="feature-case h-[100%]">
            <div class="mb-[16px] flex justify-between">
              <a-input
                v-model:model-value="groupKeyword"
                :placeholder="t('caseManagement.caseReview.folderSearchPlaceholder')"
                allow-clear
                :max-length="255"
              />
              <a-dropdown-button
                v-if="hasAllPermission(['FUNCTIONAL_CASE:READ+ADD', 'FUNCTIONAL_CASE:READ+IMPORT'])"
                class="ml-2"
                type="primary"
                @click="handleSelect('newCase')"
              >
                {{ t('common.newCreate') }}
                <template #icon>
                  <icon-down />
                </template>
                <template #content>
                  <a-doption
                    v-permission="['FUNCTIONAL_CASE:READ+IMPORT']"
                    value="Excel"
                    @click="handleSelect('import', 'Excel')"
                  >
                    {{ t('caseManagement.featureCase.importExcel') }}
                  </a-doption>
                </template>
              </a-dropdown-button>
            </div>

            <div class="case h-[38px]">
              <div class="flex items-center" :class="getActiveClass('all')" @click="setActiveFolder('all')">
                <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
                <div class="folder-name mx-[4px]">{{ t('caseManagement.featureCase.allCase') }}</div>
                <div class="folder-count">({{ modulesCount.all || 0 }})</div></div
              >
              <div class="ml-auto flex items-center">
                <a-tooltip :content="isExpandAll ? t('common.expandAllSubModule') : t('common.collapseAllSubModule')">
                  <MsButton type="icon" status="secondary" class="!mr-0 p-[4px]" @click="expandHandler">
                    <MsIcon :type="isExpandAll ? 'icon-icon_folder_collapse1' : 'icon-icon_folder_expansion1'" />
                  </MsButton>
                </a-tooltip>
                <MsPopConfirm
                  v-if="hasAnyPermission(['FUNCTIONAL_CASE:READ+ADD'])"
                  v-model:visible="addSubVisible"
                  :is-delete="false"
                  :title="t('caseManagement.featureCase.addSubModule')"
                  :all-names="rootModulesName"
                  :loading="confirmLoading"
                  :ok-text="t('common.confirm')"
                  :field-config="{
                    placeholder: t('caseManagement.featureCase.addGroupTip'),
                    nameExistTipText: t('project.fileManagement.nameExist'),
                  }"
                  @confirm="confirmHandler"
                >
                  <MsButton type="icon" class="!mr-0 p-[2px]">
                    <MsIcon
                      type="icon-icon_create_planarity"
                      size="18"
                      class="text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
                    />
                  </MsButton>
                </MsPopConfirm>
              </div>
            </div>
            <a-divider class="my-[8px]" />
            <FeatureCaseTree
              ref="caseTreeRef"
              v-model:selected-keys="selectedKeys"
              v-model:group-keyword="groupKeyword"
              :all-names="rootModulesName"
              :active-folder="activeFolder"
              :is-expand-all="isExpandAll"
              :modules-count="modulesCount"
              :is-modal="false"
              @case-node-select="caseNodeSelect"
              @init="setRootModules"
              @drag-update="dragUpdate"
            ></FeatureCaseTree>
          </div>
        </div>
        <div class="flex-1">
          <a-divider class="!my-0 !mb-0" />
          <div class="case">
            <div
              class="flex items-center px-[20px]"
              :class="getActiveClass('recycle')"
              @click="setActiveFolder('recycle')"
            >
              <MsIcon type="icon-icon_delete-trash_outlined" class="folder-icon" />
              <div class="folder-name mx-[4px]">{{ t('caseManagement.featureCase.recycle') }}</div>
              <div class="folder-count">({{ recycleModulesCount.all || 0 }})</div>
            </div>
          </div>
        </div>
      </template>
      <template #second>
        <div class="h-full p-[16px_16px]">
          <CaseTable
            ref="caseTableRef"
            :active-folder="activeFolder"
            :offspring-ids="offspringIds"
            :modules-count="modulesCount"
            :module-name="activeFolderName"
            @init="initModulesCount"
            @import="importCase"
          ></CaseTable>
        </div>
      </template>
    </MsSplitBox>
  </MsCard>
  <!-- </div> -->
  <ExportExcelModal
    v-model:visible="showExcelModal"
    :validate-type="validateType"
    :confirm-loading="validateLoading"
    @save="validateTemplate"
    @close="showExcelModal = false"
  />
  <ValidateModal
    v-model:visible="validateModal"
    :validate-type="validateType"
    :percent="progress"
    @cancel="cancelValidate"
    @check-finished="checkFinished"
  />
  <ValidateResult
    v-model:visible="validateResultModal"
    :validate-type="validateType"
    :validate-info="validateInfo"
    :import-loading="importLoading"
    @close="closeHandler"
    @save="conFirmImport"
  />
</template>

<script setup lang="ts">
  /**
   * @description 功能测试-功能用例
   */
  import { computed, ref } from 'vue';
  import { useRouter } from 'vue-router';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsPopConfirm, { ConfirmValue } from '@/components/pure/ms-popconfirm/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import CaseTable from './components/caseTable.vue';
  import FeatureCaseTree from './components/caseTree.vue';
  import ExportExcelModal from './components/export/exportCaseModal.vue';
  import ValidateModal from './components/export/validateModal.vue';
  import ValidateResult from './components/export/validateResult.vue';

  import { createCaseModuleTree, importExcelCase, importExcelChecked } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { hasAllPermission, hasAnyPermission } from '@/utils/permission';

  import type { CreateOrUpdateModule, ValidateInfo } from '@/models/caseManagement/featureCase';
  import { TableQueryParams } from '@/models/common';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  import type { FileItem } from '@arco-design/web-vue';
  import Message from '@arco-design/web-vue/es/message';

  const router = useRouter();

  const appStore = useAppStore();
  const { t } = useI18n();
  const currentProjectId = computed(() => appStore.currentProjectId);

  const isExpandAll = ref(false);
  const activeCaseType = ref<'folder' | 'module'>('folder'); // 激活用例类型
  const rootModulesName = ref<string[]>([]); // 根模块名称列表
  const groupKeyword = ref<string>('');
  // 全部展开或折叠
  const expandHandler = () => {
    isExpandAll.value = !isExpandAll.value;
  };

  const activeFolder = ref<string>('all');
  const activeFolderName = ref('');

  // 选中节点
  const selectedKeys = computed({
    get: () => [activeFolder.value],
    set: (val) => val,
  });

  const offspringIds = ref<string[]>([]);

  // 设置当前激活用例类型公共用例|全部用例|回收站
  const setActiveFolder = (type: string) => {
    activeFolder.value = type;
    if (['public', 'all', 'recycle'].includes(type)) {
      activeCaseType.value = 'folder';
    }
    if (type === 'recycle') {
      router.push({
        name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_RECYCLE,
      });
    }
  };

  // 获取激活用例类型样式
  const getActiveClass = (type: string) => {
    return activeFolder.value === type ? 'folder-text case-active' : 'folder-text';
  };

  const featureCaseStore = useFeatureCaseStore();
  // 处理用例树节点选中
  function caseNodeSelect(keys: string[], _offspringIds: string[], node: MsTreeNodeData) {
    [activeFolder.value] = keys;
    activeCaseType.value = 'module';
    offspringIds.value = [..._offspringIds];
    featureCaseStore.setModuleId(keys);
    activeFolderName.value = node.title || node.name;
  }

  const confirmLoading = ref(false);
  const addSubVisible = ref(false);
  const caseTreeRef = ref();
  const caseTableRef = ref();

  // 添加子模块
  async function confirmHandler(formValue: ConfirmValue) {
    try {
      confirmLoading.value = true;
      const params: CreateOrUpdateModule = {
        projectId: currentProjectId.value,
        name: formValue.field,
        parentId: 'NONE',
      };
      await createCaseModuleTree(params);
      Message.success(t('caseManagement.featureCase.addSuccess'));
      caseTreeRef.value.initModules();
      addSubVisible.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  /**
   * 设置根模块名称列表
   * @param names 根模块名称列表
   */
  function setRootModules(names: string[], isDelete = false) {
    rootModulesName.value = names;
    if (isDelete) {
      caseTreeRef.value?.initModules(true);
      caseTableRef.value?.initData();
    }
  }

  // 表格搜索参数
  const tableFilterParams = ref<TableQueryParams>({
    moduleIds: [],
    projectId: '',
  });

  const modulesCount = computed(() => {
    return featureCaseStore.modulesCount;
  });

  const recycleModulesCount = computed(() => {
    return featureCaseStore.recycleModulesCount;
  });

  /**
   * 右侧表格数据刷新后，若当前展示的是模块，则刷新模块树的统计数量
   */
  function initModulesCount(params: TableQueryParams, refreshModule = false) {
    if (refreshModule) {
      caseTreeRef.value.initModules();
    }
    featureCaseStore.getCaseModulesCount(params);
    featureCaseStore.getRecycleModulesCount(params);
    tableFilterParams.value = { ...params };
  }

  // 创建用例
  function caseDetail() {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
    });
  }

  const showExcelModal = ref<boolean>(false);
  const validateType = ref<'Excel' | 'Xmind'>('Excel');

  // 导入excel
  function importCase(type: 'Excel' | 'Xmind') {
    validateType.value = type;
    showExcelModal.value = true;
  }

  const validateModal = ref<boolean>(false);

  // 校验结果弹窗
  const validateResultModal = ref<boolean>(false);

  const validateInfo = ref<ValidateInfo>({
    failCount: 0,
    successCount: 0,
    errorMessages: [],
  });

  const intervalId = ref<any>(null);
  const progress = ref<number>(0);
  const increment = ref<number>(0.1);

  function updateProgress() {
    progress.value = Math.floor(progress.value + increment.value);
    if (progress.value >= 1) {
      progress.value = 1;
    }
  }

  function finish() {
    clearInterval(intervalId.value);
    progress.value = 1;
    updateProgress();
  }

  function start() {
    progress.value = 0;
    increment.value = 0.1;
    intervalId.value = setInterval(() => {
      if (progress.value >= 1) {
        finish();
      } else {
        updateProgress();
      }
    }, 100);
  }

  const fileList = ref<FileItem[]>([]);
  const isCover = ref<boolean>(false);
  const validateLoading = ref<boolean>(false);

  // 校验导入模板
  async function validateTemplate(files: FileItem[], cover: boolean) {
    fileList.value = files;
    isCover.value = cover;
    validateLoading.value = true;
    try {
      validateModal.value = true;
      start();
      const params = {
        projectId: appStore.currentProjectId,
        versionId: '',
        cover,
      };
      if (validateType.value === 'Excel') {
        const result = await importExcelChecked({ request: params, fileList: files.map((item: any) => item.file) });
        finish();
        validateInfo.value = result.data;
      }
    } catch (error) {
      validateModal.value = false;
      console.log(error);
    } finally {
      validateLoading.value = false;
    }
  }

  function checkFinished() {
    validateResultModal.value = true;
  }

  function cancelValidate() {
    validateModal.value = false;
  }

  function closeHandler() {
    showExcelModal.value = false;
    validateResultModal.value = false;
    caseTreeRef.value.initModules();
  }
  const importLoading = ref<boolean>(false);
  // 确定导入
  async function conFirmImport() {
    importLoading.value = true;
    try {
      const params = {
        projectId: appStore.currentProjectId,
        versionId: '',
        cover: isCover.value,
        count: validateInfo.value.successCount,
      };
      await importExcelCase({ request: params, fileList: fileList.value.map((item: any) => item.file) });
      Message.success(t('caseManagement.featureCase.importSuccess'));
      validateResultModal.value = false;
      showExcelModal.value = false;
      caseTableRef.value.initData();
      caseTreeRef.value.initModules();
    } catch (error) {
      console.log(error);
    } finally {
      importLoading.value = false;
    }
  }

  function handleSelect(value: string | number | Record<string, any> | undefined, type?: 'Excel' | 'Xmind') {
    switch (value) {
      case 'newCase':
        caseDetail();
        break;
      case 'import':
        importCase(type as 'Excel' | 'Xmind');
        break;
      default:
        break;
    }
  }

  function dragUpdate() {
    caseTableRef.value.emitTableParams();
  }
</script>

<style scoped lang="less">
  .case {
    padding: 8px 4px;
    border-radius: var(--border-radius-small);
    @apply flex cursor-pointer  items-center justify-between;
    &:hover {
      background-color: rgb(var(--primary-1));
    }
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
    .case-active {
      .folder-icon,
      .folder-name,
      .folder-count {
        color: rgb(var(--primary-5));
      }
    }
    .back {
      margin-right: 8px;
      width: 20px;
      height: 20px;
      border: 1px solid #ffffff;
      background: linear-gradient(90deg, rgb(var(--primary-9)) 3.36%, #ffffff 100%);
      box-shadow: 0 0 7px rgb(15 0 78 / 9%);
      .arco-icon {
        color: rgb(var(--primary-5));
      }
      @apply flex cursor-pointer items-center rounded-full;
    }
  }
  .recycle {
    @apply absolute bottom-0 bg-white  pb-4;
    :deep(.arco-divider-horizontal) {
      margin: 8px 0;
    }
    .recycle-bin {
      @apply bottom-0 flex items-center bg-white;
      .recycle-count {
        margin-left: 4px;
        color: var(--color-text-4);
      }
    }
  }
</style>
