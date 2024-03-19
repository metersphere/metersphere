<template>
  <MsCard no-content-padding simple>
    <div class="p-[24px_24px_8px_24px]">
      <MsEditableTab
        v-model:active-tab="activeApiTab"
        v-model:tabs="apiTabs"
        class="flex-1 overflow-hidden"
        @add="newTab"
      >
        <template #label="{ tab }">
          <a-tooltip :content="tab.label" :mouse-enter-delay="500">
            <div class="one-line-text max-w-[144px]">
              {{ tab.label }}
            </div>
          </a-tooltip>
        </template>
      </MsEditableTab>
    </div>
    <a-divider class="!my-0" />
    <div v-if="activeApiTab.id === 'all'" class="pageWrap">
      <MsSplitBox :size="300" :max="0.5">
        <template #first>
          <div class="flex h-full flex-col">
            <div class="p-[16px]">
              <scenarioModuleTree
                ref="scenarioModuleTreeRef"
                :is-show-scenario="isShowScenario"
                @folder-node-select="handleNodeSelect"
                @init="handleModuleInit"
                @new-scenario="newTab"
              ></scenarioModuleTree>
            </div>
            <div class="flex-1">
              <a-divider margin="0" />
              <div class="case">
                <div class="flex items-center px-[20px]" :class="getActiveClass('recycle')" @click="redirectRecycle()">
                  <MsIcon type="icon-icon_delete-trash_outlined" class="folder-icon" />
                  <div class="folder-name mx-[4px]">{{ t('apiScenario.tree.recycleBin') }}</div>
                  <div class="folder-count">({{ recycleModulesCount || 0 }})</div>
                </div>
              </div>
            </div>
          </div>
        </template>
        <template #second>
          <ScenarioTable
            ref="apiTableRef"
            :active-module="activeModule"
            :offspring-ids="offspringIds"
            @refresh-module-tree="refreshTree"
          />
        </template>
      </MsSplitBox>
    </div>
    <div v-else-if="activeApiTab.is" class="pageWrap">
      <detail :detail="activeApiTab"></detail>
    </div>
    <div v-else class="pageWrap">
      <create :module-tree="folderTree"></create>
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 接口测试-接口场景主页
   */

  import { onBeforeMount, ref } from 'vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import scenarioModuleTree from './components/scenarioModuleTree.vue';
  import ScenarioTable from '@/views/api-test/scenario/components/scenarioTable.vue';

  import { getTrashModuleCount } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import router from '@/router';

  import { ApiScenarioGetModuleParams } from '@/models/apiTest/scenario';
  import { ModuleTreeNode } from '@/models/common';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';

  import useAppStore from '../../../store/modules/app';

  // 异步导入
  const detail = defineAsyncComponent(() => import('./detail/index.vue'));
  const create = defineAsyncComponent(() => import('./create/index.vue'));

  const { t } = useI18n();

  const apiTabs = ref<TabItem[]>([
    {
      id: 'all',
      label: t('apiScenario.allScenario'),
      closable: false,
    },
  ]);
  const activeApiTab = ref<TabItem>(apiTabs.value[0]);

  function newTab() {
    apiTabs.value.push({
      id: `newTab${apiTabs.value.length}`,
      label: `New Tab ${apiTabs.value.length}`,
      closable: true,
      isNew: true,
    });
    activeApiTab.value = apiTabs.value[apiTabs.value.length - 1];
  }

  const folderTree = ref<ModuleTreeNode[]>([]);
  const folderTreePathMap = ref<Record<string, any>>({});
  const activeModule = ref<string>('all');
  const activeFolder = ref<string>('all');
  const offspringIds = ref<string[]>([]);
  const isShowScenario = ref(false);

  // 获取激活用例类型样式
  const getActiveClass = (type: string) => {
    return activeFolder.value === type ? 'folder-text case-active' : 'folder-text';
  };
  const appStore = useAppStore();
  const recycleModulesCount = ref(0);

  const scenarioModuleTreeRef = ref<InstanceType<typeof scenarioModuleTree>>();

  function handleModuleInit(tree: any, _protocol: string, pathMap: Record<string, any>) {
    folderTree.value = tree;
    folderTreePathMap.value = pathMap;
  }

  function handleNodeSelect(keys: string[], _offspringIds: string[]) {
    [activeModule.value] = keys;
    offspringIds.value = _offspringIds;
  }

  function refreshTree(params: ApiScenarioGetModuleParams) {
    scenarioModuleTreeRef.value?.initModuleCount(params);
  }

  function redirectRecycle() {
    router.push({
      name: ApiTestRouteEnum.API_TEST_SCENARIO_RECYCLE,
    });
  }

  onBeforeMount(async () => {
    const res = await getTrashModuleCount({
      projectId: appStore.currentProjectId,
    });
    recycleModulesCount.value = res.all;
  });
</script>

<style scoped lang="less">
  .pageWrap {
    height: calc(100% - 65px);
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
