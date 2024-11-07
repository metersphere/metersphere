<template>
  <MsCard simple no-content-padding>
    <MsSplitBox :not-show-first="isAdvancedSearchMode">
      <template #first>
        <div class="min-w-[300px] p-[16px]">
          <div class="mb-[16px] flex justify-between">
            <a-input-search
              v-model:model-value="groupKeyword"
              :placeholder="t('caseManagement.featureCase.searchTip')"
              allow-clear
            />
            <a-dropdown-button
              v-permission="['PROJECT_TEST_PLAN:READ+ADD']"
              class="ml-2"
              type="primary"
              @click="handleSelect('createPlan')"
            >
              {{ t('common.newCreate') }}
              <template #icon>
                <icon-down />
              </template>
              <template #content>
                <a-doption value="createGroup" @click="handleSelect('createGroup')">
                  {{ t('testPlan.testPlanIndex.testPlanGroup') }}
                </a-doption>
              </template>
            </a-dropdown-button>
          </div>

          <div class="test-plan h-[100%]">
            <div class="case h-[38px]">
              <div class="flex items-center" :class="getActiveClass('all')" @click="setActiveFolder('all')">
                <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
                <div class="folder-name mx-[4px]">{{ t('testPlan.testPlanIndex.allTestPlan') }}</div>
                <div class="folder-count">({{ modulesCount.all || 0 }})</div></div
              >
              <div class="ml-auto flex items-center">
                <a-tooltip :content="isExpandAll ? t('common.collapseAllSubModule') : t('common.expandAllSubModule')">
                  <MsButton type="icon" status="secondary" class="!mr-0 p-[4px]" position="top" @click="expandHandler">
                    <MsIcon :type="isExpandAll ? 'icon-icon_folder_collapse1' : 'icon-icon_folder_expansion1'" />
                  </MsButton>
                </a-tooltip>
                <MsPopConfirm
                  v-model:visible="addSubVisible"
                  :is-delete="false"
                  :title="t('testPlan.testPlanIndex.addSubModule')"
                  :all-names="rootModulesName"
                  :loading="confirmLoading"
                  :ok-text="t('common.confirm')"
                  :field-config="{
                    placeholder: t('testPlan.testPlanIndex.addGroupTip'),
                    nameExistTipText: t('project.fileManagement.nameExist'),
                  }"
                  @confirm="confirmHandler"
                >
                  <MsButton v-permission="['PROJECT_TEST_PLAN:READ+ADD']" type="icon" class="!mr-0 p-[2px]">
                    <MsIcon
                      type="icon-icon_create_planarity"
                      size="18"
                      class="text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
                    />
                  </MsButton>
                </MsPopConfirm>
              </div>
            </div>
            <TestPlanTree
              ref="planTreeRef"
              v-model:selected-keys="selectedKeys"
              v-model:groupKeyword="groupKeyword"
              :all-names="rootModulesName"
              :active-folder="activeFolder"
              :is-expand-all="isExpandAll"
              :modules-count="modulesCount"
              @plan-tree-node-select="planNodeSelect"
              @init="setRootModules"
              @drag-update="dragUpdate"
              @delete-node="deleteNode"
            ></TestPlanTree>
          </div>
        </div>
      </template>
      <template #second>
        <div class="p-[16px]">
          <PlanTable
            ref="planTableRef"
            :active-folder="activeFolder"
            :offspring-ids="offspringIds"
            :active-folder-type="activeCaseType"
            :modules-count="modulesCount"
            :module-tree="folderTree"
            :node-name="nodeName"
            @init="initModulesCount"
            @edit="handleEdit"
            @new="createTestPlan"
            @handle-adv-search="handleAdvSearch"
          />
        </div>
      </template>
    </MsSplitBox>
    <CreateAndEditPlanDrawer
      v-model:visible="showPlanDrawer"
      :plan-id="planId"
      :module-id="selectedKeys[0]"
      :module-tree="folderTree"
      @close="resetPlanId"
      @load-plan-list="loadPlanList"
    />
    <CreateAndUpdatePlanGroup
      v-model:visible="showPlanGroupModel"
      :plan-group-id="planId"
      :module-tree="folderTree"
      :module-id="selectedKeys[0]"
      @close="resetPlanId"
      @load-plan-list="loadPlanList"
    />
  </MsCard>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsPopConfirm, { ConfirmValue } from '@/components/pure/ms-popconfirm/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import PlanTable from './components/planTable.vue';
  import TestPlanTree from './components/testPlanTree.vue';
  import CreateAndEditPlanDrawer from './createAndEditPlanDrawer.vue';
  import CreateAndUpdatePlanGroup from '@/views/test-plan/testPlan/planGroup/createAndUpdatePlanGroup.vue';

  import { createPlanModuleTree, getPlanModulesCount } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { CreateOrUpdateModule } from '@/models/caseManagement/featureCase';
  import { ModuleTreeNode, TableQueryParams } from '@/models/common';
  import type { TestPlanItem } from '@/models/testPlan/testPlan';
  import { RouteEnum } from '@/enums/routeEnum';
  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  import Message from '@arco-design/web-vue/es/message';

  defineOptions({
    name: RouteEnum.TEST_PLAN_INDEX,
  });

  const appStore = useAppStore();
  const { t } = useI18n();
  const currentProjectId = computed(() => appStore.currentProjectId);

  const activeFolder = ref<string>('all');

  const groupKeyword = ref<string>('');

  // 获取激活用例类型样式
  const getActiveClass = (type: string) => {
    return activeFolder.value === type ? 'folder-text case-active' : 'folder-text';
  };

  const modulesCount = ref<Record<string, any>>({});

  // 选中节点
  const selectedKeys = computed({
    get: () => [activeFolder.value],
    set: (val) => val,
  });

  const isExpandAll = ref(false);

  // 全部展开或折叠
  const expandHandler = () => {
    isExpandAll.value = !isExpandAll.value;
  };

  const addSubVisible = ref(false);
  const planTreeRef = ref();
  const confirmLoading = ref(false);
  async function confirmHandler(formValue: ConfirmValue) {
    try {
      confirmLoading.value = true;
      const params: CreateOrUpdateModule = {
        projectId: currentProjectId.value,
        name: formValue.field,
        parentId: 'NONE',
      };
      await createPlanModuleTree(params);
      Message.success(t('caseManagement.featureCase.addSuccess'));
      planTreeRef.value.initModules();
      addSubVisible.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  // 设置当前激活用例类型公共用例|全部用例|回收站
  const setActiveFolder = (type: string) => {
    activeFolder.value = type;
  };
  const activeCaseType = ref<'folder' | 'module'>('folder'); // 激活计划树类型

  const offspringIds = ref<string[]>([]);
  const nodeName = ref<string>('');
  const planTableRef = ref<InstanceType<typeof PlanTable>>();

  // 处理计划树节点选中
  function planNodeSelect(keys: string[], _offspringIds: string[], moduleName: string) {
    [activeFolder.value] = keys;
    activeCaseType.value = 'module';
    offspringIds.value = [..._offspringIds];
    nodeName.value = moduleName;
  }

  /**
   * 刷新模块树的统计数量
   */
  async function initModulesCount(params: TableQueryParams) {
    try {
      modulesCount.value = await getPlanModulesCount(params);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  /**
   * 设置根模块名称列表
   * @param names 根模块名称列表
   */
  const rootModulesName = ref<string[]>([]);
  const folderTree = ref<ModuleTreeNode[]>([]);
  function setRootModules(treeNode: ModuleTreeNode[]) {
    folderTree.value = treeNode;
    rootModulesName.value = treeNode.map((e) => e.name);
  }

  const showPlanDrawer = ref<boolean>(false);
  const showPlanGroupModel = ref<boolean>(false);
  function handleSelect(value: string | number | Record<string, any> | undefined) {
    switch (value) {
      case 'createPlan':
        showPlanDrawer.value = true;
        break;
      case 'createGroup':
        showPlanGroupModel.value = true;
        break;
      default:
        break;
    }
  }

  const planId = ref<string>();

  function handleEdit(record: TestPlanItem) {
    planId.value = record.id;
    if (record.type === testPlanTypeEnum.TEST_PLAN) {
      showPlanDrawer.value = true;
    } else {
      showPlanGroupModel.value = true;
    }
  }

  function resetPlanId() {
    planId.value = '';
  }

  function loadPlanList() {
    planTableRef.value?.fetchData();
  }
  function dragUpdate() {
    planTableRef.value?.emitTableParams();
  }

  function deleteNode() {
    nextTick(() => {
      if (activeFolder.value !== 'all') {
        setActiveFolder('all');
      } else {
        planTableRef.value?.fetchData();
      }
    });
  }

  function createTestPlan(type: string) {
    if (type === 'group') {
      showPlanGroupModel.value = true;
    } else {
      showPlanDrawer.value = true;
    }
  }

  const isAdvancedSearchMode = computed(() => planTableRef.value?.isAdvancedSearchMode);
  function handleAdvSearch() {
    setActiveFolder('all');
  }
</script>

<style scoped lang="less">
  .case {
    padding: 8px 0;
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
</style>
