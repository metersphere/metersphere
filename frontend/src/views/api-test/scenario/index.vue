<template>
  <MsCard no-content-padding simple>
    <MsSplitBox :not-show-first="isAdvancedSearchMode" :size="300" :max="0.5">
      <template #first>
        <div class="flex h-full flex-col">
          <div class="flex-1 p-[16px]">
            <scenarioModuleTree
              ref="scenarioModuleTreeRef"
              :is-show-scenario="isShowScenario"
              @count-recycle-scenario="selectRecycleCount"
              @folder-node-select="handleNodeSelect"
              @init="handleModuleInit"
              @new-scenario="() => newTab()"
              @change="handleModuleChange"
              @import="importDrawerVisible = true"
            ></scenarioModuleTree>
          </div>
          <a-divider margin="0" />
          <div class="case h-[40px] !px-[24px]" @click="redirectRecycle()">
            <div class="flex items-center" :class="getActiveClass('recycle')">
              <MsIcon type="icon-icon_delete-trash_outlined1" class="folder-icon" />
              <div class="folder-name mx-[4px]">{{ t('common.recycle') }}</div>
            </div>
            <div class="folder-count">{{ recycleModulesCount || 0 }}</div>
          </div>
        </div>
      </template>
      <template #second>
        <div class="flex items-center justify-between p-[8px_16px_8px_16px]">
          <MsEditableTab
            v-model:active-tab="activeScenarioTab"
            v-model:tabs="scenarioTabs"
            v-permission="['PROJECT_API_SCENARIO:READ']"
            class="flex-1 overflow-hidden"
            :show-add="hasAnyPermission(['PROJECT_API_SCENARIO:READ+ADD'])"
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
          <div v-show="activeScenarioTab.id !== 'all'" class="flex items-center gap-[8px]">
            <MsEnvironmentSelect :env="activeScenarioTab.environmentId" />
            <executeButton
              ref="executeButtonRef"
              v-permission="['PROJECT_API_SCENARIO:READ+EXECUTE']"
              :execute-loading="activeScenarioTab.executeLoading"
              @execute="(type) => handleExecute(type)"
              @stop-debug="handleStopExecute"
            />
            <a-button
              v-if="
                activeScenarioTab.isNew
                  ? hasAnyPermission(['PROJECT_API_SCENARIO:READ+ADD'])
                  : hasAnyPermission(['PROJECT_API_SCENARIO:READ+UPDATE'])
              "
              type="primary"
              :loading="saveLoading"
              @click="saveScenario"
            >
              {{ t('common.save') }}
            </a-button>
          </div>
        </div>
        <a-divider class="!my-0" />
        <keep-alive :include="cacheStore.cacheViews">
          <MsCacheWrapper
            v-if="activeScenarioTab.id === 'all'"
            :key="CacheTabTypeEnum.API_SCENARIO_TABLE"
            class="pageWrap overflow-x-hidden"
            :cache-name="CacheTabTypeEnum.API_SCENARIO_TABLE"
          >
            <ScenarioTable
              ref="apiTableRef"
              :module-tree="moduleTree"
              :active-module="activeModule"
              :offspring-ids="offspringIds"
              @refresh-module-tree="refreshTree"
              @open-scenario="openScenarioTab"
              @create-scenario="() => newTab()"
              @handle-adv-search="handleAdvSearch"
            />
          </MsCacheWrapper>
        </keep-alive>
        <div v-if="activeScenarioTab.isNew && activeScenarioTab.id !== 'all'" class="pageWrap">
          <create
            ref="createRef"
            v-model:scenario="activeScenarioTab"
            :module-tree="moduleTree"
            @batch-debug="realExecute($event, false)"
          ></create>
        </div>
        <div v-if="!activeScenarioTab.isNew && activeScenarioTab.id !== 'all'" class="pageWrap">
          <detail
            ref="detailRef"
            v-model:scenario="activeScenarioTab"
            :module-tree="moduleTree"
            @batch-debug="realExecute($event, false)"
          ></detail>
        </div>
      </template>
    </MsSplitBox>
  </MsCard>
  <importScenario
    v-model:visible="importDrawerVisible"
    :module-tree="moduleTree"
    :active-module="activeModule"
    popup-container="#managementContainer"
    @done="handleImportDone"
  />
</template>

<script setup lang="ts">
  /**
   * @description 接口测试-接口场景主页
   */

  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import MsCacheWrapper from '@/components/pure/ms-cache-wrapper/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsEnvironmentSelect from '@/components/business/ms-environment-select/index.vue';
  import importScenario from './components/import.vue';
  import scenarioModuleTree from './components/scenarioModuleTree.vue';
  import executeButton from '@/views/api-test/components/executeButton.vue';
  import ScenarioTable from '@/views/api-test/scenario/components/scenarioTable.vue';

  import { localExecuteApiDebug, stopLocalExecute } from '@/api/modules/api-test/common';
  import {
    addScenario,
    debugScenario,
    executeScenario,
    getScenarioDetail,
    getTrashModuleCount,
    updateScenario,
  } from '@/api/modules/api-test/scenario';
  import { getSocket } from '@/api/modules/project-management/commonScript';
  import { projectStopTask } from '@/api/modules/taskCenter/project';
  import { useI18n } from '@/hooks/useI18n';
  import useLeaveTabUnSaveCheck from '@/hooks/useLeaveTabUnSaveCheck';
  import useShortcutSave from '@/hooks/useShortcutSave';
  import router from '@/router';
  import useAppStore from '@/store/modules/app';
  import useCacheStore from '@/store/modules/cache/cache';
  import { filterTree, getGenerateId, mapTree, traverseTree } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { RequestResult } from '@/models/apiTest/common';
  import {
    ApiScenarioDebugRequest,
    ApiScenarioGetModuleParams,
    ApiScenarioTableItem,
    Scenario,
    ScenarioStepDetails,
    ScenarioStepItem,
  } from '@/models/apiTest/scenario';
  import { ModuleTreeNode } from '@/models/common';
  import { ScenarioExecuteStatus, ScenarioStepRefType, ScenarioStepType } from '@/enums/apiEnum';
  import { CacheTabTypeEnum } from '@/enums/cacheTabEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';

  import { defaultCsvParamItem, defaultNormalParamItem, defaultScenario } from './components/config';
  import updateStepStatus, { getScenarioFileParams, getStepDetails } from './components/utils';
  import {
    filterAssertions,
    filterConditionsSqlValidParams,
    filterKeyValParams,
  } from '@/views/api-test/components/utils';

  // 异步导入
  const detail = defineAsyncComponent(() => import('./detail/index.vue'));
  const create = defineAsyncComponent(() => import('./create/index.vue'));

  export type ScenarioParams = Scenario & TabItem;

  const route = useRoute();
  const appStore = useAppStore();
  const cacheStore = useCacheStore();

  const { t } = useI18n();

  const scenarioTabs = ref<ScenarioParams[]>([
    {
      id: 'all',
      label: t('apiScenario.allScenario'),
      closable: false,
      environmentId: '',
    } as ScenarioParams,
  ]);
  const activeScenarioTab = ref<ScenarioParams>(scenarioTabs.value[0] as ScenarioParams);
  const executeButtonRef = ref<InstanceType<typeof executeButton>>();
  const localExecuteUrl = computed(() => executeButtonRef.value?.localExecuteUrl);

  const websocketMap: Record<string | number, WebSocket> = {};

  function setStepExecuteStatus(scenario: Scenario) {
    updateStepStatus(scenario.steps, scenario.stepResponses);
  }

  /**
   * 开启websocket监听，接收执行结果
   */
  function debugSocket(scenario: Scenario, executeType?: 'localExec' | 'serverExec') {
    return new Promise((resolve) => {
      websocketMap[scenario.reportId] = getSocket(
        scenario.reportId || '',
        executeType === 'localExec' ? '/ws/debug' : '',
        executeType === 'localExec' ? localExecuteUrl.value : ''
      );
      websocketMap[scenario.reportId].addEventListener('message', (event) => {
        const data = JSON.parse(event.data);
        if (data.msgType === 'EXEC_RESULT') {
          if (scenario.reportId === data.reportId) {
            // 判断当前查看的tab是否是当前返回的报告的tab，是的话直接赋值
            data.taskResult.requestResults.forEach((result: RequestResult) => {
              if (result.stepId) {
                // 过滤掉前后置配置的执行结果，没有步骤 id
                if (scenario.stepResponses[result.stepId] === undefined) {
                  scenario.stepResponses[result.stepId] = [];
                }
                scenario.stepResponses[result.stepId].push({
                  ...result,
                  console: data.taskResult.console,
                });
                if (result.status === ScenarioExecuteStatus.FAKE_ERROR) {
                  scenario.executeFakeErrorCount += 1;
                } else if (result.isSuccessful) {
                  scenario.executeSuccessCount += 1;
                } else {
                  scenario.executeFailCount += 1;
                }
              }
            });
          }
        } else if (data.msgType === 'EXEC_END') {
          // 执行结束，关闭websocket
          websocketMap[scenario.reportId]?.close();
          if (scenario.reportId === data.reportId) {
            scenario.executeLoading = false;
            scenario.isExecute = false;
            setStepExecuteStatus(scenario);
          }
        }
      });
      websocketMap[scenario.reportId].addEventListener('open', () => {
        resolve(true);
      });
    });
  }

  const executeTaskId = ref('');
  /**
   * 实际执行函数
   * @param executeParams 执行参数
   * @param isExecute 是否执行，否则是调试
   * @param executeType 执行类型
   */
  async function realExecute(
    executeParams: Pick<ApiScenarioDebugRequest, 'steps' | 'stepDetails' | 'reportId'>,
    isExecute?: boolean,
    executeType?: 'localExec' | 'serverExec',
    envId?: string
  ) {
    try {
      activeScenarioTab.value.executeLoading = true;
      // 重置执行结果
      activeScenarioTab.value.executeTime = dayjs().format('YYYY-MM-DD HH:mm:ss');
      activeScenarioTab.value.executeSuccessCount = 0;
      activeScenarioTab.value.executeFailCount = 0;
      activeScenarioTab.value.executeFakeErrorCount = 0;
      activeScenarioTab.value.stepResponses = {};
      activeScenarioTab.value.reportId = executeParams.reportId; // 存储报告ID
      activeScenarioTab.value.executeType = executeType; // 存储报告ID
      activeScenarioTab.value.isDebug = !isExecute;
      await debugSocket(activeScenarioTab.value, executeType); // 开启websocket
      let res;
      if (isExecute && executeType !== 'localExec' && !activeScenarioTab.value.isNew) {
        // 执行场景且非本地执行且非未保存场景
        res = await executeScenario({
          id: activeScenarioTab.value.id,
          grouped: false,
          environmentId: envId || appStore.getCurrentEnvId || '',
          projectId: appStore.currentProjectId,
          scenarioConfig: activeScenarioTab.value.scenarioConfig,
          ...executeParams,
          stepDetails: getStepDetails(executeParams.steps, executeParams.stepDetails),
          stepFileParam: activeScenarioTab.value.stepFileParam,
          fileParam: {
            ...getScenarioFileParams(activeScenarioTab.value),
          },
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
          environmentId: envId || appStore.getCurrentEnvId || '',
          projectId: appStore.currentProjectId,
          scenarioConfig: activeScenarioTab.value.scenarioConfig,
          stepFileParam: activeScenarioTab.value.stepFileParam,
          fileParam: {
            ...getScenarioFileParams(activeScenarioTab.value),
          },
          frontendDebug: executeType === 'localExec',
          ...executeParams,
          stepDetails: getStepDetails(executeParams.steps, executeParams.stepDetails),
          steps: mapTree(executeParams.steps, (node) => {
            return {
              ...node,
              parent: null, // 原树形结构存在循环引用，这里要去掉以免 axios 序列化失败
            };
          }),
        });
      }
      executeTaskId.value = res.taskInfo?.taskId;
      if (executeType === 'localExec' && localExecuteUrl.value) {
        // 本地执行需要调 debug 接口获取响应结果，然后再调本地执行接口
        await localExecuteApiDebug(localExecuteUrl.value, res);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      websocketMap[activeScenarioTab.value.reportId]?.close();
      activeScenarioTab.value.executeLoading = false;
      setStepExecuteStatus(activeScenarioTab.value);
    }
  }

  /**
   * 执行场景
   * @param executeType 执行类型
   * @param envId 环境ID
   */
  function handleExecute(executeType?: 'localExec' | 'serverExec', envId?: string) {
    const waitingDebugStepDetails: Record<string, ScenarioStepDetails> = {};
    const waitTingDebugSteps = filterTree(activeScenarioTab.value.steps, (node) => {
      if (node.enable) {
        node.executeStatus = ScenarioExecuteStatus.EXECUTING;
        if (!node.isQuoteScenarioStep) {
          // 引用场景的步骤详情不传
          waitingDebugStepDetails[node.id] = activeScenarioTab.value.stepDetails[node.id];
        }
      } else {
        node.executeStatus = undefined;
      }
      return !!node.enable;
    });
    if (waitTingDebugSteps.length === 0) {
      Message.warning(t('apiScenario.execute.no.step.tips'));
      return;
    }
    realExecute(
      {
        steps: waitTingDebugSteps as ScenarioStepItem[],
        stepDetails: waitingDebugStepDetails,
        reportId: getGenerateId(),
      },
      true,
      executeType,
      envId
    );
  }

  async function handleStopExecute() {
    if (!activeScenarioTab.value.isDebug) {
      // 调试模式不需要调停止执行接口
      try {
        if (activeScenarioTab.value.executeType === 'localExec') {
          await stopLocalExecute(
            executeButtonRef.value?.localExecuteUrl || '',
            activeScenarioTab.value.reportId,
            ScenarioStepType.API_SCENARIO
          );
        } else {
          await projectStopTask(executeTaskId.value);
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    }
    websocketMap[activeScenarioTab.value.reportId]?.close();
    activeScenarioTab.value.executeLoading = false;
    setStepExecuteStatus(activeScenarioTab.value);
  }

  const moduleTree = ref<ModuleTreeNode[]>([]);
  const folderTreePathMap = ref<Record<string, any>>({});
  const activeModule = ref<string>('all');
  const activeFolder = ref<string>('all');
  const offspringIds = ref<string[]>([]);
  const isShowScenario = ref(false);

  // 获取激活用例类型样式
  const getActiveClass = (type: string) => {
    return activeFolder.value === type ? 'folder-text case-active' : 'folder-text';
  };
  const recycleModulesCount = ref(0);

  function newTab(defaultScenarioInfo?: Scenario, action?: 'copy' | 'execute') {
    if (defaultScenarioInfo) {
      const isCopy = action === 'copy';
      let copySteps: ScenarioStepItem[] = [];
      const copyCsvVariables = defaultScenarioInfo.scenarioConfig.variable.csvVariables.map((e) => ({
        ...e,
        copyId: e.id,
        id: isCopy ? getGenerateId() : e.id,
      }));
      if (isCopy) {
        // 场景被复制，递归处理节点，增加copyFromStepId
        copySteps = mapTree(defaultScenarioInfo.steps, (node) => {
          node.isNew = true;
          node.copyFromStepId = node.id;
          if (
            node.parent &&
            node.parent.stepType === ScenarioStepType.API_SCENARIO &&
            [ScenarioStepRefType.REF, ScenarioStepRefType.PARTIAL_REF].includes(node.parent.refType)
          ) {
            // 如果根节点是引用场景
            node.isQuoteScenarioStep = true; // 标记为引用场景下的子步骤
            node.isRefScenarioStep = node.parent.refType === ScenarioStepRefType.REF; // 标记为完全引用场景
            node.draggable = false; // 引用场景下的任何步骤不可拖拽
            node.id = getGenerateId(); // 重新生成 ID
          } else if (node.parent) {
            // 如果有父节点
            node.isQuoteScenarioStep = node.parent.isQuoteScenarioStep; // 复用父节点的引用场景标记
            node.isRefScenarioStep = node.parent.isRefScenarioStep; // 复用父节点的是否完全引用场景标记
            node.draggable = !node.parent.isQuoteScenarioStep; // 引用场景下的任何步骤不可拖拽
          }
          if (!node.isQuoteScenarioStep && !node.isRefScenarioStep) {
            // 非引用场景步骤
            node.id = getGenerateId(); // 重新生成 ID
          }
          if (node.csvIds && node.csvIds.length > 0) {
            node.csvIds = node.csvIds.map((e: string) => copyCsvVariables.find((c) => c.copyId === e)?.id || '');
          }
          node.uniqueId = node.id;
          return node;
        });
      } else {
        // 正常打开场景详情，递归处理节点，标记引用场景下的子步骤
        copySteps = mapTree(defaultScenarioInfo.steps, (node) => {
          if (
            node.parent &&
            node.parent.stepType === ScenarioStepType.API_SCENARIO &&
            [ScenarioStepRefType.REF, ScenarioStepRefType.PARTIAL_REF].includes(node.parent.refType)
          ) {
            // 如果根节点是引用场景
            node.isQuoteScenarioStep = true; // 标记为引用场景下的子步骤
            node.isRefScenarioStep = node.parent.refType === ScenarioStepRefType.REF; // 标记为完全引用场景
            node.draggable = false; // 引用场景下的任何步骤不可拖拽
          } else if (node.parent) {
            // 如果有父节点
            node.isQuoteScenarioStep = node.parent.isQuoteScenarioStep; // 复用父节点的引用场景标记
            node.isRefScenarioStep = node.parent.isRefScenarioStep; // 复用父节点的是否完全引用场景标记
            node.draggable = !node.parent.isQuoteScenarioStep; // 引用场景下的任何步骤不可拖拽
          }
          node.uniqueId = getGenerateId();
          return node;
        });
      }
      let copyName = `copy_${defaultScenarioInfo.name}`;
      if (copyName.length > 255) {
        copyName = copyName.slice(0, 255);
      }
      scenarioTabs.value.push({
        ...defaultScenarioInfo,
        steps: copySteps,
        id: isCopy ? getGenerateId() : defaultScenarioInfo.id || '',
        copyFromScenarioId: isCopy ? defaultScenarioInfo.id : '',
        label: isCopy ? copyName : defaultScenarioInfo.name,
        name: isCopy ? copyName : defaultScenarioInfo.name,
        isNew: isCopy,
        scenarioConfig: {
          ...defaultScenarioInfo.scenarioConfig,
          variable: {
            commonVariables: defaultScenarioInfo.scenarioConfig.variable.commonVariables,
            csvVariables: copyCsvVariables,
          },
        },
        stepResponses: {},
        errorMessageInfo: {},
      });
      if (action === 'execute') {
        nextTick(() => {
          // 等待激活 tab 设置完毕后执行
          handleExecute(
            executeButtonRef.value?.isPriorityLocalExec ? 'localExec' : 'serverExec',
            defaultScenarioInfo.environmentId
          );
        });
      }
    } else {
      scenarioTabs.value.push({
        ...cloneDeep(defaultScenario),
        id: getGenerateId(),
        environmentId: appStore.getCurrentEnvId || '',
        label: `${t('apiScenario.createScenario')}${scenarioTabs.value.length}`,
        moduleId: activeModule.value === 'all' ? 'root' : activeModule.value,
        projectId: appStore.currentProjectId,
        priority: 'P0',
      });
    }
    activeScenarioTab.value = scenarioTabs.value[scenarioTabs.value.length - 1] as ScenarioParams;
  }

  const scenarioModuleTreeRef = ref<InstanceType<typeof scenarioModuleTree>>();
  const apiTableRef = ref<InstanceType<typeof ScenarioTable>>();
  const isAdvancedSearchMode = computed(() => apiTableRef.value?.isAdvancedSearchMode);
  function handleAdvSearch() {
    scenarioModuleTreeRef.value?.setActiveFolder('all');
  }

  function handleModuleInit(tree: any, _protocol: string, pathMap: Record<string, any>) {
    moduleTree.value = tree;
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

  const tempTableQueryParams = ref<ApiScenarioGetModuleParams>();
  function refreshTree(params?: ApiScenarioGetModuleParams) {
    tempTableQueryParams.value = params;
    if (params) {
      scenarioModuleTreeRef.value?.initModuleCount(params);
    } else {
      scenarioModuleTreeRef.value?.initModuleCount({
        projectId: appStore.currentProjectId,
      });
    }
    selectRecycleCount();
  }

  function redirectRecycle() {
    router.push({
      name: ApiTestRouteEnum.API_TEST_SCENARIO_RECYCLE,
    });
  }

  const createRef = ref<InstanceType<typeof create>>();
  const detailRef = ref<InstanceType<typeof detail>>();
  const saveLoading = ref(false);

  function handleModuleChange() {
    apiTableRef.value?.loadScenarioList();
  }

  async function realSaveScenario() {
    try {
      saveLoading.value = true;
      const { assertionConfig } = activeScenarioTab.value.scenarioConfig;
      if (activeScenarioTab.value.isNew) {
        const res = await addScenario({
          ...activeScenarioTab.value,
          stepDetails: getStepDetails(activeScenarioTab.value.steps, activeScenarioTab.value.stepDetails),
          steps: mapTree(activeScenarioTab.value.steps, (node) => {
            return {
              ...node,
              parent: null, // 原树形结构存在循环引用，这里要去掉以免 axios 序列化失败
            };
          }),
          scenarioConfig: {
            ...activeScenarioTab.value.scenarioConfig,
            assertionConfig: { ...assertionConfig, assertions: filterAssertions(assertionConfig) },
            postProcessorConfig: filterConditionsSqlValidParams(
              activeScenarioTab.value.scenarioConfig.postProcessorConfig
            ),
            preProcessorConfig: filterConditionsSqlValidParams(
              activeScenarioTab.value.scenarioConfig.preProcessorConfig
            ),
            variable: {
              commonVariables: filterKeyValParams(
                activeScenarioTab.value.scenarioConfig.variable.commonVariables,
                defaultNormalParamItem
              ).validParams,
              csvVariables: filterKeyValParams(
                activeScenarioTab.value.scenarioConfig.variable.csvVariables,
                defaultCsvParamItem
              ).validParams,
            },
          },
          projectId: appStore.currentProjectId,
          environmentId: appStore.getCurrentEnvId || '',
        });
        const scenarioDetail = await getScenarioDetail(res.id);
        // 添加后获取后台组装的场景信息
        scenarioDetail.stepDetails = {};
        scenarioDetail.isNew = false;
        scenarioDetail.id = res.id;
        if (!scenarioDetail.steps) {
          scenarioDetail.steps = [];
        } else {
          scenarioDetail.steps = mapTree(scenarioDetail.steps, (node) => {
            if (
              node.parent &&
              node.parent.stepType === ScenarioStepType.API_SCENARIO &&
              [ScenarioStepRefType.REF, ScenarioStepRefType.PARTIAL_REF].includes(node.parent.refType)
            ) {
              // 如果根节点是引用场景
              node.isQuoteScenarioStep = true; // 标记为引用场景下的子步骤
              node.isRefScenarioStep = node.parent.refType === ScenarioStepRefType.REF; // 标记为完全引用场景
              node.draggable = false; // 引用场景下的任何步骤不可拖拽
            } else if (node.parent) {
              // 如果有父节点
              node.isQuoteScenarioStep = node.parent.isQuoteScenarioStep; // 复用父节点的引用场景标记
              node.isRefScenarioStep = node.parent.isRefScenarioStep; // 复用父节点的是否完全引用场景标记
              node.draggable = !node.parent.isQuoteScenarioStep; // 引用场景下的任何步骤不可拖拽
            }
            node.uniqueId = getGenerateId();
            return node;
          });
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
          stepDetails: getStepDetails(activeScenarioTab.value.steps, activeScenarioTab.value.stepDetails),
          scenarioConfig: {
            ...activeScenarioTab.value.scenarioConfig,
            assertionConfig: { ...assertionConfig, assertions: filterAssertions(assertionConfig) },
            postProcessorConfig: filterConditionsSqlValidParams(
              activeScenarioTab.value.scenarioConfig.postProcessorConfig
            ),
            preProcessorConfig: filterConditionsSqlValidParams(
              activeScenarioTab.value.scenarioConfig.preProcessorConfig
            ),
            variable: {
              commonVariables: filterKeyValParams(
                activeScenarioTab.value.scenarioConfig.variable.commonVariables,
                defaultNormalParamItem
              ).validParams,
              csvVariables: filterKeyValParams(
                activeScenarioTab.value.scenarioConfig.variable.csvVariables,
                defaultCsvParamItem
              ).validParams,
            },
          },
          environmentId: appStore.getCurrentEnvId || '',
          steps: mapTree(activeScenarioTab.value.steps, (node) => {
            return {
              ...node,
              parent: null, // 原树形结构存在循环引用，这里要去掉以免 axios 序列化失败
            };
          }),
        });
      }
      traverseTree(activeScenarioTab.value.steps, (node) => {
        node.isNew = false;
      });
      activeScenarioTab.value.stepFileParam = {};
      refreshTree(tempTableQueryParams.value);
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
    if (activeScenarioTab.value.id === 'all') return;
    if (activeScenarioTab.value.isNew && hasAnyPermission(['PROJECT_API_SCENARIO:READ+ADD'])) {
      createRef.value?.validScenarioForm(realSaveScenario);
    } else if (!activeScenarioTab.value.isNew && hasAnyPermission(['PROJECT_API_SCENARIO:READ+UPDATE'])) {
      detailRef.value?.validScenarioForm(realSaveScenario);
    }
  }

  async function openScenarioTab(record: ApiScenarioTableItem | string, action?: 'copy' | 'execute') {
    const isLoadedTabIndex = scenarioTabs.value.findIndex(
      (e) => e.id === (typeof record === 'string' ? record : record.id)
    );
    if (isLoadedTabIndex > -1 && action !== 'copy') {
      // 如果点击的场景在tab中已经存在，则直接切换到该tab
      activeScenarioTab.value = scenarioTabs.value[isLoadedTabIndex];
      // tab子组件里监听的是id变化,所以id相等的时候需要单独调执行
      if (action === 'execute') {
        handleExecute(
          executeButtonRef.value?.isPriorityLocalExec ? 'localExec' : 'serverExec',
          typeof record === 'string' ? undefined : record.environmentId
        );
      }
      return;
    }
    try {
      appStore.showLoading();
      const res = await getScenarioDetail(typeof record === 'string' ? record : record.id);
      res.stepFileParam = {};
      res.stepDetails = {};
      if (!res.steps) {
        res.steps = [];
      }
      newTab(res, action);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      nextTick(() => {
        appStore.hideLoading();
      });
    }
  }

  const importDrawerVisible = ref(false);

  async function handleImportDone() {
    await scenarioModuleTreeRef.value?.refresh();
    apiTableRef.value?.loadScenarioList();
  }

  const { registerCatchSaveShortcut, removeCatchSaveShortcut } = useShortcutSave(saveScenario);
  onBeforeMount(async () => {
    selectRecycleCount();
    if (route.query.id) {
      await openScenarioTab(route.query.id as string);
    }
    registerCatchSaveShortcut();
  });

  onBeforeUnmount(() => {
    removeCatchSaveShortcut();
  });

  useLeaveTabUnSaveCheck(scenarioTabs.value, ['PROJECT_API_SCENARIO:READ+ADD', 'PROJECT_API_SCENARIO:READ+UPDATE']);

  const hasLocalExec = computed(() => executeButtonRef.value?.hasLocalExec);
  const isPriorityLocalExec = computed(() => executeButtonRef.value?.isPriorityLocalExec);
  const scenarioId = computed(() => activeScenarioTab.value.id);
  const scenarioExecuteLoading = computed(() => activeScenarioTab.value.executeLoading);
  // 为子孙组件提供属性
  provide('isPriorityLocalExec', readonly(isPriorityLocalExec));
  provide('hasLocalExec', readonly(hasLocalExec));
  provide('localExecuteUrl', readonly(localExecuteUrl));
  provide('scenarioId', scenarioId);
  provide('scenarioExecuteLoading', scenarioExecuteLoading);
  provide('moduleTree', readonly(moduleTree));
  provide('activeModule', readonly(activeModule));
</script>

<style scoped lang="less">
  .pageWrap {
    height: calc(100% - 50px);
    border-radius: var(--border-radius-large);
    background-color: var(--color-text-fff);
  }
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
    @apply absolute bottom-0  pb-4;

    background-color: var(--color-text-fff);
    :deep(.arco-divider-horizontal) {
      margin: 8px 0;
    }
    .recycle-bin {
      @apply bottom-0 flex items-center;

      background-color: var(--color-text-fff);
      .recycle-count {
        margin-left: 4px;
        color: var(--color-text-4);
      }
    }
  }
</style>
