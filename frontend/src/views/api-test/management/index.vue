<template>
  <MsCard simple no-content-padding>
    <MsSplitBox :size="300" :max="0.5">
      <template #first>
        <div class="flex flex-col">
          <div class="p-[16px]" :style="{ height: `calc(100vh - 120px)` }">
            <moduleTree
              ref="moduleTreeRef"
              :active-node-id="activeNodeId"
              @init="handleModuleInit"
              @new-api="newApi"
              @import="importDrawerVisible = true"
              @folder-node-select="handleNodeSelect"
              @click-api-node="handleApiNodeClick"
              @change-protocol="handleProtocolChange"
              @update-api-node="handleUpdateApiNode"
              @delete-node="handleDeleteApiFromModuleTree"
              @execute="handleExecute"
            />
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
                <div class="folder-count">({{ recycleModulesCount || 0 }})</div>
              </div>
            </div>
          </div>
        </div>
      </template>
      <template #second>
        <div class="relative flex h-full flex-col">
          <div
            id="managementContainer"
            :class="['absolute z-[102] h-full w-full', importDrawerVisible ? '' : 'invisible']"
            style="transition: all 0.3s"
          >
            <importApi
              v-model:visible="importDrawerVisible"
              :module-tree="folderTree"
              :active-module="activeModule"
              popup-container="#managementContainer"
              @done="handleImportDone"
            />
          </div>
          <management
            ref="managementRef"
            :module-tree="folderTree"
            :active-module="activeModule"
            :offspring-ids="offspringIds"
            :selected-protocols="selectedProtocols"
            @import="importDrawerVisible = true"
          />
        </div>
      </template>
    </MsSplitBox>
  </MsCard>
</template>

<script lang="ts" setup>
  /**
   * @description 接口测试-接口管理
   */
  import { provide } from 'vue';
  import { useRoute, useRouter } from 'vue-router';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import { RequestParam } from '../components/requestComposition/index.vue';
  import importApi from './components/import.vue';
  import management from './components/management/index.vue';
  import moduleTree from './components/moduleTree.vue';

  import { getTrashModuleCount } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ApiDefinitionGetModuleParams } from '@/models/apiTest/management';
  import { ModuleTreeNode } from '@/models/common';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';

  const route = useRoute();
  const { t } = useI18n();
  const router = useRouter();
  const activeModule = ref<string>('all');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const folderTreePathMap = ref<Record<string, any>>({});
  const importDrawerVisible = ref(false);
  const offspringIds = ref<string[]>([]);
  const selectedProtocols = ref<string[]>([]);
  const activeNodeId = ref<string | number>('all');
  const moduleTreeRef = ref<InstanceType<typeof moduleTree>>();
  const managementRef = ref<InstanceType<typeof management>>();

  function handleModuleInit(tree: ModuleTreeNode[], _protocols: string[], pathMap: Record<string, any>) {
    folderTree.value = tree;
    selectedProtocols.value = _protocols;
    folderTreePathMap.value = pathMap;
  }

  function newApi() {
    importDrawerVisible.value = false;
    managementRef.value?.newTab();
  }

  function handleNodeSelect(keys: string[], _offspringIds: string[]) {
    [activeModule.value] = keys;
    offspringIds.value = _offspringIds;
    managementRef.value?.changeActiveApiTabToFirst();
  }

  function handleApiNodeClick(node: ModuleTreeNode) {
    managementRef.value?.newTab(node);
  }

  function setActiveApi(params: RequestParam) {
    if (params.id === 'all') {
      // 切换到全部 tab 时需设置为上次激活的 api 节点的模块
      activeNodeId.value = params.moduleId;
    } else {
      activeNodeId.value = params.id;
    }
  }

  function handleProtocolChange(val: string[]) {
    selectedProtocols.value = val;
  }

  const appStore = useAppStore();
  const recycleModulesCount = ref(0);
  async function selectRecycleCount() {
    const res = await getTrashModuleCount({
      projectId: appStore.currentProjectId,
      keyword: '',
      moduleIds: [],
      protocols: selectedProtocols.value,
    });
    recycleModulesCount.value = res.all;
  }

  async function refreshModuleTree() {
    await moduleTreeRef.value?.refresh();
    //  涉及到模块树的刷新操作（比如删除），也会刷新回收站的数量
    selectRecycleCount();
  }

  function refreshModuleTreeCount(params: ApiDefinitionGetModuleParams) {
    moduleTreeRef.value?.initModuleCount(params);
  }

  async function handleImportDone() {
    await refreshModuleTree();
    managementRef.value?.refreshApiTable();
  }

  function handleUpdateApiNode(newInfo: { id: string; name: string; moduleId?: string; [key: string]: any }) {
    managementRef.value?.handleApiUpdateFromModuleTree(newInfo);
  }

  function handleDeleteApiFromModuleTree(id: string, isModule?: boolean) {
    managementRef.value?.handleDeleteApiFromModuleTree(id, isModule);
  }

  function handleExecute(id: string) {
    managementRef.value?.newTab(id, false, true);
  }

  onMounted(() => {
    if (route.query.dId) {
      // 携带 dId 参数，自动打开接口定义详情 tab
      managementRef.value?.newTab(route.query.dId as string);
    } else if (route.query.cId) {
      // 携带 cId 参数，自动打开接口用例详情 tab
      managementRef.value?.newCaseTab(route.query.cId as string);
    }
    selectRecycleCount();
  });

  // 获取激活用例类型样式
  const getActiveClass = (type: string) => {
    return activeModule.value === type ? 'folder-text case-active' : 'folder-text';
  };

  // 设置当前激活用例类型公共用例|全部用例|回收站
  const setActiveFolder = (type: string) => {
    if (type === 'recycle') {
      router.push({
        name: ApiTestRouteEnum.API_TEST_MANAGEMENT_RECYCLE,
      });
    }
  };

  /** 向子孙组件提供方法和值 */
  provide('setActiveApi', setActiveApi);
  provide('refreshModuleTree', refreshModuleTree);
  provide('refreshModuleTreeCount', refreshModuleTreeCount);
  provide('folderTreePathMap', folderTreePathMap.value);
</script>

<style lang="less" scoped>
  .case {
    padding: 8px 4px;
    border-radius: var(--border-radius-small);
    @apply flex cursor-pointer items-center justify-between;
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
    @apply absolute bottom-0 bg-white pb-4;
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
