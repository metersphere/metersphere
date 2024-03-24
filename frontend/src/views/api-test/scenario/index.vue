<template>
  <MsCard no-content-padding simple>
    <div class="flex items-center justify-between p-[24px_24px_8px_24px]">
      <MsEditableTab
        v-model:active-tab="activeScenarioTab"
        v-model:tabs="apiTabs"
        class="flex-1 overflow-hidden"
        @add="() => newTab()"
      >
        <template #label="{ tab }">
          <a-tooltip :content="tab.label" :mouse-enter-delay="500">
            <div class="one-line-text max-w-[144px]">
              {{ tab.label }}
            </div>
          </a-tooltip>
        </template>
      </MsEditableTab>
      <div v-if="activeScenarioTab.id !== 'all'" class="flex items-center gap-[8px]">
        <environmentSelect />
        <a-button type="primary" :loading="saveLoading" @click="saveScenario">
          {{ t('common.save') }}
        </a-button>
        <!-- <executeButton /> -->
      </div>
    </div>
    <a-divider class="!my-0" />
    <div v-if="activeScenarioTab.id === 'all'" class="pageWrap">
      <MsSplitBox :size="300" :max="0.5">
        <template #first>
          <div class="flex h-full flex-col">
            <div class="p-[16px]">
              <scenarioModuleTree
                ref="scenarioModuleTreeRef"
                :is-show-scenario="isShowScenario"
                @folder-node-select="handleNodeSelect"
                @init="handleModuleInit"
                @new-scenario="() => newTab()"
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
            @open-scenario="openScenarioTab"
          />
        </template>
      </MsSplitBox>
    </div>
    <div v-else-if="activeScenarioTab.isNew" class="pageWrap">
      <create v-model:scenario="activeScenarioTab" :module-tree="folderTree"></create>
    </div>
    <div v-else class="pageWrap">
      <detail v-model:scenario="activeScenarioTab"></detail>
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 接口测试-接口场景主页
   */

  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import scenarioModuleTree from './components/scenarioModuleTree.vue';
  import environmentSelect from '@/views/api-test/components/environmentSelect.vue';
  // import executeButton from '@/views/api-test/components/executeButton.vue';
  import ScenarioTable from '@/views/api-test/scenario/components/scenarioTable.vue';

  import { addScenario, getScenarioDetail, getTrashModuleCount, updateScenario } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import router from '@/router';
  import useAppStore from '@/store/modules/app';
  import { getGenerateId } from '@/utils';

  import { ApiScenarioGetModuleParams, ApiScenarioTableItem, Scenario } from '@/models/apiTest/scenario';
  import { ModuleTreeNode } from '@/models/common';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';

  import { defaultScenario } from './components/config';

  // 异步导入
  const detail = defineAsyncComponent(() => import('./detail/index.vue'));
  const create = defineAsyncComponent(() => import('./create/index.vue'));

  export type ScenarioParams = Scenario & TabItem;

  const { t } = useI18n();

  const apiTabs = ref<ScenarioParams[]>([
    {
      id: 'all',
      label: t('apiScenario.allScenario'),
      closable: false,
    } as ScenarioParams,
  ]);
  const activeScenarioTab = ref<ScenarioParams>(apiTabs.value[0] as ScenarioParams);

  function newTab(defaultScenarioInfo?: Scenario, isCopy = false) {
    if (defaultScenarioInfo) {
      apiTabs.value.push({
        ...defaultScenarioInfo,
        id: isCopy ? getGenerateId() : defaultScenarioInfo.id || '',
        label: isCopy ? `copy-${defaultScenarioInfo.name}` : defaultScenarioInfo.name,
        isNew: false,
      });
    } else {
      apiTabs.value.push({
        ...cloneDeep(defaultScenario),
        id: `${t('apiScenario.createScenario')}${apiTabs.value.length}`,
        label: `${t('apiScenario.createScenario')}${apiTabs.value.length}`,
        moduleId: 'root',
        priority: 'P0',
      });
    }
    activeScenarioTab.value = apiTabs.value[apiTabs.value.length - 1] as ScenarioParams;
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

  async function selectRecycleCount() {
    const res = await getTrashModuleCount({
      projectId: appStore.currentProjectId,
    });
    recycleModulesCount.value = res.all;
  }

  function refreshTree(params: ApiScenarioGetModuleParams) {
    scenarioModuleTreeRef.value?.initModuleCount(params);
    selectRecycleCount();
  }

  function redirectRecycle() {
    router.push({
      name: ApiTestRouteEnum.API_TEST_SCENARIO_RECYCLE,
    });
  }

  onBeforeMount(selectRecycleCount);

  const saveLoading = ref(false);

  async function saveScenario() {
    try {
      saveLoading.value = true;
      if (activeScenarioTab.value.isNew) {
        const res = await addScenario({
          ...activeScenarioTab.value,
          projectId: appStore.currentProjectId,
        });
        const scenarioDetail = await getScenarioDetail(res.id);
        scenarioDetail.stepDetails = {};
        scenarioDetail.isNew = false;
        activeScenarioTab.value = scenarioDetail as ScenarioParams;
      } else {
        await updateScenario({
          ...activeScenarioTab.value,
        });
      }
      Message.success(activeScenarioTab.value.isNew ? t('common.createSuccess') : t('common.saveSuccess'));
      activeScenarioTab.value.unSaved = false;
      saveLoading.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      saveLoading.value = false;
    }
  }

  async function openScenarioTab(record: ApiScenarioTableItem, isCopy?: boolean) {
    try {
      appStore.showLoading();
      const res = await getScenarioDetail(record.id);
      res.stepDetails = {};
      newTab(res, isCopy);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      nextTick(() => {
        appStore.hideLoading();
      });
    }
  }
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
