<template>
  <MsCard no-content-padding simple>
    <div class="flex items-center justify-between p-[24px_24px_8px_24px]">
      <MsEditableTab
        v-model:active-tab="activeScenarioTab"
        v-model:tabs="scenarioTabs"
        class="flex-1 overflow-hidden"
        @add="() => newTab()"
      >
        <template #label="{ tab }">
          <a-tooltip :content="tab.name || tab.label" :mouse-enter-delay="500">
            <div class="one-line-text max-w-[144px]">
              {{ tab.name || tab.label }}
            </div>
          </a-tooltip>
        </template>
      </MsEditableTab>
      <div v-if="activeScenarioTab.id !== 'all'" class="flex items-center gap-[8px]">
        <environmentSelect v-model:current-env-config="currentEnvConfig" />
        <a-button type="primary" :loading="saveLoading" @click="saveScenario">
          {{ t('common.save') }}
        </a-button>
        <executeButton
          ref="executeButtonRef"
          :execute-loading="activeScenarioTab.executeLoading"
          @execute="handleExecute"
          @stop-debug="handleStopExecute"
        />
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
      <create
        ref="createRef"
        v-model:scenario="activeScenarioTab"
        :module-tree="folderTree"
        @batch-debug="realExecute($event, false)"
      ></create>
    </div>
    <div v-else class="pageWrap">
      <detail v-model:scenario="activeScenarioTab" @batch-debug="realExecute($event, false)"></detail>
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 接口测试-接口场景主页
   */

  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import scenarioModuleTree from './components/scenarioModuleTree.vue';
  import environmentSelect from '@/views/api-test/components/environmentSelect.vue';
  import executeButton from '@/views/api-test/components/executeButton.vue';
  import ScenarioTable from '@/views/api-test/scenario/components/scenarioTable.vue';

  import { localExecuteApiDebug } from '@/api/modules/api-test/common';
  import {
    addScenario,
    debugScenario,
    executeScenario,
    getScenarioDetail,
    getTrashModuleCount,
    updateScenario,
  } from '@/api/modules/api-test/scenario';
  import { getSocket } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';
  import router from '@/router';
  import useAppStore from '@/store/modules/app';
  import { filterTree, getGenerateId, mapTree } from '@/utils';

  import {
    ApiScenarioDebugRequest,
    ApiScenarioGetModuleParams,
    ApiScenarioTableItem,
    Scenario,
  } from '@/models/apiTest/scenario';
  import { ModuleTreeNode } from '@/models/common';
  import { EnvConfig } from '@/models/projectManagement/environmental';
  import { ScenarioExecuteStatus, ScenarioStepType } from '@/enums/apiEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';

  import { defaultScenario } from './components/config';

  // 异步导入
  const detail = defineAsyncComponent(() => import('./detail/index.vue'));
  const create = defineAsyncComponent(() => import('./create/index.vue'));

  export type ScenarioParams = Scenario & TabItem;

  const appStore = useAppStore();
  const { t } = useI18n();

  const scenarioTabs = ref<ScenarioParams[]>([
    {
      id: 'all',
      label: t('apiScenario.allScenario'),
      closable: false,
    } as ScenarioParams,
  ]);
  const activeScenarioTab = ref<ScenarioParams>(scenarioTabs.value[0] as ScenarioParams);

  function newTab(defaultScenarioInfo?: Scenario, isCopy = false) {
    if (defaultScenarioInfo) {
      scenarioTabs.value.push({
        ...defaultScenarioInfo,
        id: isCopy ? getGenerateId() : defaultScenarioInfo.id || '',
        label: isCopy ? `copy-${defaultScenarioInfo.name}` : defaultScenarioInfo.name,
        isNew: false,
        stepResponses: {},
      });
    } else {
      scenarioTabs.value.push({
        ...cloneDeep(defaultScenario),
        id: getGenerateId(),
        label: `${t('apiScenario.createScenario')}${scenarioTabs.value.length}`,
        moduleId: 'root',
        projectId: appStore.currentProjectId,
        priority: 'P0',
      });
    }
    activeScenarioTab.value = scenarioTabs.value[scenarioTabs.value.length - 1] as ScenarioParams;
  }

  const folderTree = ref<ModuleTreeNode[]>([]);
  const folderTreePathMap = ref<Record<string, any>>({});
  const activeModule = ref<string>('all');
  const activeFolder = ref<string>('all');
  const offspringIds = ref<string[]>([]);
  const isShowScenario = ref(false);
  const executeButtonRef = ref<InstanceType<typeof executeButton>>();
  const currentEnvConfig = ref<EnvConfig>();

  // 获取激活用例类型样式
  const getActiveClass = (type: string) => {
    return activeFolder.value === type ? 'folder-text case-active' : 'folder-text';
  };
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

  const createRef = ref<InstanceType<typeof create>>();
  const saveLoading = ref(false);

  async function realSaveScenario() {
    try {
      saveLoading.value = true;
      if (activeScenarioTab.value.isNew) {
        const res = await addScenario({
          ...activeScenarioTab.value,
          steps: mapTree(activeScenarioTab.value.steps, (node) => {
            return {
              ...node,
              parent: null, // 原树形结构存在循环引用，这里要去掉以免 axios 序列化失败
            };
          }),
          projectId: appStore.currentProjectId,
          environmentId: currentEnvConfig.value?.id || '',
        });
        const scenarioDetail = await getScenarioDetail(res.id);
        scenarioDetail.stepDetails = {};
        scenarioDetail.isNew = false;
        scenarioDetail.id = res.id;
        if (!scenarioDetail.steps) {
          scenarioDetail.steps = [];
        }
        const index = scenarioTabs.value.findIndex((e) => e.id === activeScenarioTab.value.id);
        if (index !== -1) {
          const newScenarioTab = {
            ...cloneDeep(activeScenarioTab.value),
            ...scenarioDetail,
          };
          scenarioTabs.value.splice(index, 1, newScenarioTab);
          activeScenarioTab.value = newScenarioTab;
        }
      } else {
        await updateScenario({
          ...activeScenarioTab.value,
          environmentId: currentEnvConfig.value?.id || '',
          steps: mapTree(activeScenarioTab.value.steps, (node) => {
            return {
              ...node,
              parent: null, // 原树形结构存在循环引用，这里要去掉以免 axios 序列化失败
            };
          }),
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

  function saveScenario() {
    if (activeScenarioTab.value.isNew) {
      createRef.value?.validScenarioForm(realSaveScenario);
    } else {
      realSaveScenario();
    }
  }

  async function openScenarioTab(record: ApiScenarioTableItem, isCopy?: boolean) {
    try {
      appStore.showLoading();
      const res = await getScenarioDetail(record.id);
      res.stepDetails = {};
      if (!res.steps) {
        res.steps = [];
      }
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

  const websocket = ref<WebSocket>();
  const temporaryScenarioReportMap = {}; // 缓存websocket返回的报告内容，避免执行接口后切换tab导致报告丢失

  /**
   * 开启websocket监听，接收执行结果
   */
  function debugSocket(reportId?: string | number, executeType?: 'localExec' | 'serverExec', localExecuteUrl?: string) {
    websocket.value = getSocket(
      reportId || '',
      executeType === 'localExec' ? '/ws/debug' : '',
      executeType === 'localExec' ? localExecuteUrl : ''
    );
    websocket.value.addEventListener('message', (event) => {
      const data = JSON.parse(event.data);
      if (data.msgType === 'EXEC_RESULT') {
        if (activeScenarioTab.value.reportId === data.reportId) {
          // 判断当前查看的tab是否是当前返回的报告的tab，是的话直接赋值
          data.taskResult.requestResults.forEach((result) => {
            activeScenarioTab.value.stepResponses[result.stepId] = {
              ...result,
              console: data.taskResult.console,
            };
            if (result.isSuccessful) {
              activeScenarioTab.value.executeSuccessCount += 1;
            } else {
              activeScenarioTab.value.executeFailCount += 1;
            }
          });
        } else {
          // 不是则需要把报告缓存起来，等切换到对应的tab再赋值
          data.taskResult.requestResults.forEach((result) => {
            if (activeScenarioTab.value.reportId) {
              if (temporaryScenarioReportMap[activeScenarioTab.value.reportId] === undefined) {
                temporaryScenarioReportMap[activeScenarioTab.value.reportId] = {};
              }
              temporaryScenarioReportMap[activeScenarioTab.value.reportId][result.stepId] = {
                ...result,
                console: data.taskResult.console,
              };
            }
          });
        }
      } else if (data.msgType === 'EXEC_END') {
        // 执行结束，关闭websocket
        websocket.value?.close();
        if (activeScenarioTab.value.reportId === data.reportId) {
          activeScenarioTab.value.executeLoading = false;
          activeScenarioTab.value.isExecute = false;
        }
      }
    });
  }

  async function realExecute(
    executeParams: Pick<ApiScenarioDebugRequest, 'steps' | 'stepDetails' | 'reportId'>,
    isExecute?: boolean,
    executeType?: 'localExec' | 'serverExec',
    localExecuteUrl?: string
  ) {
    try {
      activeScenarioTab.value.executeLoading = true;
      debugSocket(executeParams.reportId, executeType, localExecuteUrl); // 开启websocket
      // 重置执行结果
      activeScenarioTab.value.executeTime = dayjs().format('YYYY-MM-DD HH:mm:ss');
      activeScenarioTab.value.executeSuccessCount = 0;
      activeScenarioTab.value.executeFailCount = 0;
      activeScenarioTab.value.stepResponses = {};
      activeScenarioTab.value.reportId = executeParams.reportId; // 存储报告ID
      activeScenarioTab.value.isDebug = !isExecute;
      let res;
      if (isExecute && executeType !== 'localExec' && !activeScenarioTab.value.isNew) {
        // 执行场景且非本地执行且非未保存场景
        res = await executeScenario({
          id: activeScenarioTab.value.id,
          grouped: false,
          environmentId: currentEnvConfig.value?.id || '',
          projectId: appStore.currentProjectId,
          scenarioConfig: activeScenarioTab.value.scenarioConfig,
          uploadFileIds: activeScenarioTab.value.uploadFileIds,
          linkFileIds: activeScenarioTab.value.linkFileIds,
          ...executeParams,
          steps: mapTree(executeParams.steps, (node) => {
            return {
              ...node,
              parent: null, // 原树形结构存在循环引用，这里要去掉以免 axios 序列化失败
            };
          }),
        });
      } else {
        res = await debugScenario({
          id: activeScenarioTab.value.id,
          grouped: false,
          environmentId: currentEnvConfig.value?.id || '',
          projectId: appStore.currentProjectId,
          scenarioConfig: activeScenarioTab.value.scenarioConfig,
          uploadFileIds: activeScenarioTab.value.uploadFileIds,
          linkFileIds: activeScenarioTab.value.linkFileIds,
          frontendDebug: executeType === 'localExec',
          ...executeParams,
          steps: mapTree(executeParams.steps, (node) => {
            return {
              ...node,
              parent: null, // 原树形结构存在循环引用，这里要去掉以免 axios 序列化失败
            };
          }),
        });
      }
      if (executeType === 'localExec' && localExecuteUrl) {
        // 本地执行需要调 debug 接口获取响应结果，然后再调本地执行接口
        await localExecuteApiDebug(localExecuteUrl, res);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      websocket.value?.close();
      activeScenarioTab.value.executeLoading = false;
    }
  }

  function handleExecute(executeType?: 'localExec' | 'serverExec', localExecuteUrl?: string) {
    const waitingDebugStepDetails = {};
    const waitTingDebugSteps = filterTree(activeScenarioTab.value.steps, (node) => {
      if (node.enable) {
        node.executeStatus = ScenarioExecuteStatus.EXECUTING;
        waitingDebugStepDetails[node.id] = activeScenarioTab.value.stepDetails[node.id];
        if (
          [ScenarioStepType.API, ScenarioStepType.API_CASE, ScenarioStepType.CUSTOM_REQUEST].includes(node.stepType)
        ) {
          // 请求和场景类型才直接显示执行中，其他控制器需要等待执行完毕才结算执行结果
          node.executeStatus = ScenarioExecuteStatus.EXECUTING;
        }
      }
      return !!node.enable;
    });
    realExecute(
      {
        steps: waitTingDebugSteps,
        stepDetails: waitingDebugStepDetails,
        reportId: getGenerateId(),
      },
      true,
      executeType,
      localExecuteUrl
    );
  }

  function handleStopExecute() {
    websocket.value?.close();
    activeScenarioTab.value.executeLoading = false;
  }

  watch(
    () => activeScenarioTab.value.id,
    (val) => {
      if (val !== 'all' && activeScenarioTab.value.reportId && !activeScenarioTab.value.executeLoading) {
        // 当前查看的 tab 非全部场景 tab 页，且当前场景有报告ID，且不是正在执行中，则读取缓存报告
        const cacheReport = temporaryScenarioReportMap[activeScenarioTab.value.reportId];
        if (cacheReport) {
          // 如果有缓存的报告未读取，则直接赋值
          Object.keys(cacheReport).forEach((stepId) => {
            const result = cacheReport[stepId];
            activeScenarioTab.value.stepResponses[stepId] = result;
            if (result.isSuccessful) {
              activeScenarioTab.value.executeSuccessCount += 1;
            } else {
              activeScenarioTab.value.executeFailCount += 1;
            }
          });
          activeScenarioTab.value.executeLoading = false;
          delete temporaryScenarioReportMap[activeScenarioTab.value.reportId]; // 取完释放缓存
        }
      }
    }
  );

  const isPriorityLocalExec = computed(() => executeButtonRef.value?.isPriorityLocalExec);
  const scenarioId = computed(() => activeScenarioTab.value.id);
  const scenarioExecuteLoading = computed(() => activeScenarioTab.value.executeLoading);
  // 为子孙组件提供属性
  provide('isPriorityLocalExec', readonly(isPriorityLocalExec));
  provide('currentEnvConfig', readonly(currentEnvConfig));
  provide('scenarioId', scenarioId);
  provide('scenarioExecuteLoading', scenarioExecuteLoading);
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
