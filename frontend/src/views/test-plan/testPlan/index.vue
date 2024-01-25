<template>
  <div class="rounded-2xl bg-white">
    <div class="p-[24px] pb-[16px]">
      <a-button v-permission="['PROJECT_TEST_PLAN:READ+ADD']" type="primary">
        {{ t('testPlan.testPlanIndex.createTestPlan') }}
      </a-button>
    </div>
    <a-divider class="!my-0" />
    <div class="pageWrap">
      <MsSplitBox>
        <template #first>
          <div class="p-[24px] pb-0">
            <div class="test-plan h-[100%]">
              <div class="case h-[38px]">
                <div class="flex items-center" :class="getActiveClass('all')" @click="setActiveFolder('all')">
                  <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
                  <div class="folder-name mx-[4px]">{{ t('testPlan.testPlanIndex.allTestPlan') }}</div>
                  <div class="folder-count">({{ modulesCount.all || 0 }})</div></div
                >
                <div class="ml-auto flex items-center">
                  <a-tooltip
                    :content="
                      isExpandAll ? t('testPlan.testPlanIndex.collapseAll') : t('testPlan.testPlanIndex.expandAll')
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
                    :title="t('testPlan.testPlanIndex.addSubModule')"
                    :all-names="rootModulesName"
                    :loading="confirmLoading"
                    :ok-text="t('common.confirm')"
                    :field-config="{
                      placeholder: t('testPlan.testPlanIndex.addGroupTip'),
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
              <TestPlanTree
                ref="planTreeRef"
                v-model:selected-keys="selectedKeys"
                :all-names="rootModulesName"
                :active-folder="activeFolder"
                :is-expand-all="isExpandAll"
                :modules-count="modulesCount"
                @plan-tree-node-select="planNodeSelect"
                @init="setRootModules"
              ></TestPlanTree>
            </div>
          </div>
        </template>
        <template #second>
          <div class="p-[24px]">
            <PlanTable
              :active-folder="activeFolder"
              :offspring-ids="offspringIds"
              :active-folder-type="activeCaseType"
              :modules-count="modulesCount"
              @init="initModulesCount"
            />
          </div>
        </template>
      </MsSplitBox>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useRouter } from 'vue-router';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsPopConfirm from '@/components/pure/ms-popconfirm/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import PlanTable from './components/planTable.vue';
  import TestPlanTree from './components/testPlanTree.vue';

  import { createPlanModuleTree } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { CaseModuleQueryParams, CreateOrUpdateModule, ValidateInfo } from '@/models/caseManagement/featureCase';

  import Message from '@arco-design/web-vue/es/message';

  const router = useRouter();

  const appStore = useAppStore();
  const { t } = useI18n();
  const currentProjectId = computed(() => appStore.currentProjectId);

  const activeFolder = ref<string>('all');

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
  const rootModulesName = ref<string[]>([]);
  const planTreeRef = ref();
  const confirmLoading = ref(false);
  const confirmRef = ref();
  async function confirmHandler() {
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
      await createPlanModuleTree(params);
      Message.success(t('caseManagement.featureCase.addSuccess'));
      planTreeRef.value.initModules();
      addSubVisible.value = false;
    } catch (error) {
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
  // 处理计划树节点选中
  function planNodeSelect(keys: string[], _offspringIds: string[]) {
    [activeFolder.value] = keys;
    activeCaseType.value = 'module';
    offspringIds.value = [..._offspringIds];
  }

  /**
   * 设置根模块名称列表
   * @param names 根模块名称列表
   */
  function setRootModules(names: string[]) {
    rootModulesName.value = names;
  }

  /**
   * 右侧表格数据刷新后，若当前展示的是模块，则刷新模块树的统计数量
   */
  function initModulesCount(params: any) {}
</script>

<style scoped>
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
</style>
