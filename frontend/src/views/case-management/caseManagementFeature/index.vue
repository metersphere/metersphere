<template>
  <div class="rounded-2xl bg-white">
    <div class="p-[24px] pb-[16px]">
      <a-button type="primary" @click="caseDetail">
        {{ t('caseManagement.featureCase.creatingCase') }}
      </a-button>
      <a-button class="mx-3" type="outline"> {{ t('caseManagement.featureCase.importExcel') }} </a-button>
      <a-button type="outline"> {{ t('caseManagement.featureCase.importXmind') }} </a-button>
    </div>
    <a-divider class="!my-0" />
    <div class="pageWrap">
      <MsSplitBox>
        <template #left>
          <div class="p-[24px] pb-0">
            <div class="feature-case h-[100%]">
              <div class="case h-[38px]">
                <div class="flex items-center" :class="getActiveClass('all')" @click="setActiveFolder('all')">
                  <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
                  <div class="folder-name mx-[4px]">{{ t('caseManagement.featureCase.allCase') }}</div>
                  <div class="folder-count">({{ modulesCount.all || 0 }})</div></div
                >
                <div class="ml-auto flex items-center">
                  <a-tooltip
                    :content="
                      isExpandAll ? t('project.fileManagement.collapseAll') : t('project.fileManagement.expandAll')
                    "
                  >
                    <MsButton type="icon" status="secondary" class="!mr-0 p-[4px]" @click="expandHandler">
                      <MsIcon :type="isExpandAll ? 'icon-icon_folder_collapse1' : 'icon-icon_folder_expansion1'" />
                    </MsButton>
                  </a-tooltip>
                  <MsPopConfirm
                    ref="confirmRef"
                    v-model:visible="addSubVisible"
                    :is-delete="false"
                    :title="t('caseManagement.featureCase.addSubModule')"
                    :all-names="rootModulesName"
                    :loading="confirmLoading"
                    :ok-text="t('common.confirm')"
                    :field-config="{
                      placeholder: t('caseManagement.featureCase.addGroupTip'),
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
                :all-names="rootModulesName"
                :active-folder="activeFolder"
                :is-expand-all="isExpandAll"
                :modules-count="modulesCount"
                @case-node-select="caseNodeSelect"
                @init="setRootModules"
              ></FeatureCaseTree>
              <div class="b-0 absolute w-[88%]">
                <a-divider class="!my-0 !mb-2" />
                <div class="case h-[38px]">
                  <div class="flex items-center" :class="getActiveClass('recycle')" @click="setActiveFolder('recycle')">
                    <MsIcon type="icon-icon_delete-trash_outlined" class="folder-icon" />
                    <div class="folder-name mx-[4px]">{{ t('caseManagement.featureCase.recycle') }}</div>
                    <div class="folder-count">({{ recycleModulesCount.all || 0 }})</div></div
                  >
                </div>
              </div>
            </div>
          </div>
        </template>
        <template #right>
          <div class="p-[24px]">
            <CaseTable
              :active-folder="activeFolder"
              :offspring-ids="offspringIds"
              :active-folder-type="activeCaseType"
              :modules-count="modulesCount"
              @init="initModulesCount"
            ></CaseTable>
          </div>
        </template>
      </MsSplitBox>
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 功能测试-功能用例
   */
  import { computed, ref } from 'vue';
  import { useRouter } from 'vue-router';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsPopConfirm from '@/components/pure/ms-popconfirm/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import CaseTable from './components/caseTable.vue';
  import FeatureCaseTree from './components/caseTree.vue';

  import { createCaseModuleTree } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';

  import type { CaseModuleQueryParams, CreateOrUpdateModule } from '@/models/caseManagement/featureCase';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  import Message from '@arco-design/web-vue/es/message';

  const router = useRouter();

  const appStore = useAppStore();
  const { t } = useI18n();
  const currentProjectId = computed(() => appStore.currentProjectId);

  const isExpandAll = ref(false);
  const activeCaseType = ref<'folder' | 'module'>('folder'); // 激活用例类型
  const rootModulesName = ref<string[]>([]); // 根模块名称列表

  // 全部展开或折叠
  const expandHandler = () => {
    isExpandAll.value = !isExpandAll.value;
  };

  const activeFolder = ref<string>('all');

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
  function caseNodeSelect(keys: string[], _offspringIds: string[]) {
    [activeFolder.value] = keys;
    activeCaseType.value = 'module';
    offspringIds.value = [..._offspringIds];
    featureCaseStore.setModuleId(keys, offspringIds.value);
  }

  const confirmLoading = ref(false);
  const confirmRef = ref();
  const addSubVisible = ref(false);
  const caseTreeRef = ref();

  // 添加子模块
  const confirmHandler = async () => {
    try {
      confirmLoading.value = true;
      const { field } = confirmRef.value.form;
      if (!confirmRef.value.isPass) {
        return;
      }
      const params: CreateOrUpdateModule = {
        projectId: currentProjectId.value,
        name: field,
        parentId: 'NONE',
      };
      await createCaseModuleTree(params);
      Message.success(t('caseManagement.featureCase.addSuccess'));
      caseTreeRef.value.initModules();
      addSubVisible.value = false;
    } catch (error) {
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  };

  /**
   * 设置根模块名称列表
   * @param names 根模块名称列表
   */
  function setRootModules(names: string[]) {
    rootModulesName.value = names;
  }

  // 表格搜索参数
  const tableFilterParams = ref<CaseModuleQueryParams>({
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
  function initModulesCount(params: CaseModuleQueryParams) {
    featureCaseStore.getCaseModulesCountCount(params);
    featureCaseStore.getRecycleMModulesCountCount(params);
    tableFilterParams.value = { ...params };
  }

  // 创建用例
  function caseDetail() {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
    });
  }

  // 设置默认选中状态
  router.beforeEach((to: any, from: any, next) => {
    const routeEnumValues = Object.values(CaseManagementRouteEnum);
    if (!routeEnumValues.includes(to.name)) {
      // 当前路由不在枚举中，清空仓库的状态值
      featureCaseStore.setIsAlreadySuccess(false);
    }
    next();
  });

  onMounted(() => {
    if (featureCaseStore.operatingState) {
      [activeFolder.value] = featureCaseStore.moduleId;
    }
  });
</script>

<style scoped lang="less">
  .pageWrap {
    min-width: 1000px;
    height: calc(100vh - 166px);
    border-radius: var(--border-radius-large);
    @apply bg-white;
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
