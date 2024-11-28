<template>
  <MsMinderEditor
    v-model:extra-visible="extraVisible"
    v-model:loading="loading"
    v-model:import-json="importJson"
    :tags="[]"
    :minder-key="MinderKeyEnum.TEST_PLAN_MINDER"
    :insert-node="(node, type) => insertNode(node as PlanMinderNode,type)"
    :can-show-enter-node="false"
    :insert-sibling-menus="[]"
    :insert-son-menus="[]"
    :can-show-paste-menu="false"
    :can-show-more-menu="false"
    :can-show-priority-menu="false"
    :can-show-float-menu="canShowFloatMenu"
    :can-show-delete-menu="canShowDeleteMenu"
    :disabled="!hasEditPermission"
    :can-show-batch-delete="canShowBatchDelete"
    :can-show-dropdown="canShowDropdown"
    :dropdown-list="dropdownList"
    :checked-val="checkedVal"
    :shortcut-list="['expand', 'addSibling', 'addChild', 'delete']"
    custom-priority
    single-tag
    tag-enable
    sequence-enable
    @node-select="(node) => handleNodeSelect(node as PlanMinderNode)"
    @before-exec-command="handleBeforeExecCommand"
    @action="handleAction"
    @save="handleMinderSave"
  >
    <template #extractMenu>
      <a-tooltip v-if="canShowAddTestPointsMenu" :content="t('ms.minders.addTestSet')">
        <MsButton type="icon" class="ms-minder-node-float-menu-icon-button" @click="addTestSet">
          <MsIcon type="icon-icon_title-left-align_outlined" class="text-[var(--color-text-4)]" />
        </MsButton>
      </a-tooltip>
      <a-tooltip
        v-if="showAssociateCaseMenu && hasAnyPermission(['PROJECT_TEST_PLAN:READ+ASSOCIATION'])"
        :content="t('ms.case.associate.title')"
      >
        <MsButton type="icon" class="ms-minder-node-float-menu-icon-button" @click="associateCase">
          <MsIcon type="icon-icon_add_outlined" class="text-[var(--color-text-4)]" />
        </MsButton>
      </a-tooltip>
      <a-tooltip v-if="canShowExecuteMethodMenu && selectNodeExecuteMethod" :content="t('ms.minders.executeMethod')">
        <div
          :class="[
            'flex h-[20px] w-[20px] cursor-pointer items-center justify-center rounded-full text-[12px] font-medium',
            `${
              selectNodeExecuteMethod === RunMode.SERIAL
                ? 'bg-[rgb(var(--link-1))] text-[rgb(var(--link-5))]'
                : 'bg-[rgb(var(--success-1))] text-[rgb(var(--success-6))]'
            }`,
          ]"
          @click="
            handleExecuteMethodMenuSelect(
              selectNodeExecuteMethod === RunMode.SERIAL ? RunMode.PARALLEL : RunMode.SERIAL
            )
          "
        >
          {{ selectNodeExecuteMethod === RunMode.SERIAL ? t('ms.minders.serial') : t('ms.minders.parallel') }}
        </div>
      </a-tooltip>
      <a-tooltip v-if="showConfigMenu" :content="t('common.config')">
        <MsButton
          type="icon"
          class="ms-minder-node-float-menu-icon-button"
          :class="[extraVisible ? 'ms-minder-node-float-menu-icon-button--focus' : '']"
          @click="toggleConfig"
        >
          <MsIcon type="icon-icon_setting_filled" class="text-[var(--color-text-4)]" />
        </MsButton>
      </a-tooltip>
    </template>
    <template #extractTabContent>
      <div v-if="configForm" class="px-[16px]">
        <div class="mb-[16px] flex items-center gap-[8px]">
          <div class="h-[14px] w-[3px] rounded-[var(--border-radius-small)] bg-[rgb(var(--primary-4))]"></div>
          <a-tooltip :content="configForm.text" position="tl">
            <div class="one-line-text font-medium">{{ configForm.text }}</div>
          </a-tooltip>
        </div>
        <a-form ref="configFormRef" :model="configForm" :disabled="!hasEditPermission" layout="vertical">
          <a-form-item
            v-if="
              hasEditPermission && hasAnyPermission(['PROJECT_TEST_PLAN:READ+ASSOCIATION']) && configForm.level === 2
            "
          >
            <template #label>
              <div class="flex items-center">
                <div>{{ t('testPlan.planForm.pickCases') }}</div>
                <a-divider margin="4px" direction="vertical" />
                <MsButton
                  type="text"
                  :disabled="!hasEditPermission || selectedAssociateCasesParams.totalCount === 0"
                  @click="clearSelectedCases"
                >
                  {{ t('caseManagement.caseReview.clearSelectedCases') }}
                </MsButton>
              </div>
            </template>
            <div class="bg-[var(--color-text-n9)] p-[12px]">
              <div class="flex items-center">
                <div class="text-[var(--color-text-2)]">
                  {{
                    t('ms.minders.selectedCases', {
                      count: selectedAssociateCasesParams.totalCount || 0,
                    })
                  }}
                </div>
                <a-divider margin="8px" direction="vertical" />
                <MsButton
                  type="text"
                  class="font-medium"
                  :disabled="!hasEditPermission"
                  @click="openCaseAssociateDrawer"
                >
                  {{ t('ms.case.associate.title') }}
                </MsButton>
              </div>
            </div>
          </a-form-item>
          <a-form-item
            v-if="configForm.type !== PlanMinderCollectionType.FUNCTIONAL && configForm.level === 2"
            class="hidden-item"
          >
            <div class="flex items-center gap-[8px]">
              <a-switch v-model:model-value="configForm.extended" size="small" @change="handleExtendChange"></a-switch>
              <div>{{ t('ms.minders.extend') }}</div>
            </div>
          </a-form-item>
          <template v-if="configForm.type !== PlanMinderCollectionType.FUNCTIONAL">
            <a-form-item :label="t('system.project.resourcePool')">
              <MsSelect
                v-model:model-value="configForm.testResourcePoolId"
                :options="resourcePoolOptions"
                :disabled="configForm.level === 2 && configForm.extended"
                :option-not-exits-text="t('system.resourcePool.notExit')"
              >
              </MsSelect>
            </a-form-item>
            <a-form-item :label="t('project.environmental.env')">
              <a-select
                v-model:model-value="configForm.environmentId"
                :options="environmentOptions"
                :disabled="configForm.level === 2 && configForm.extended"
              ></a-select>
            </a-form-item>
            <a-form-item class="hidden-item">
              <a-radio-group
                v-model:model-value="configForm.executeMethod"
                :disabled="configForm.level === 2 && configForm.extended"
              >
                <a-radio :value="RunMode.SERIAL">{{ t('testPlan.testPlanIndex.serial') }}</a-radio>
                <a-radio :value="RunMode.PARALLEL">{{ t('testPlan.testPlanIndex.parallel') }}</a-radio>
              </a-radio-group>
            </a-form-item>
            <a-form-item v-if="configForm.executeMethod === RunMode.SERIAL" class="hidden-item">
              <div class="flex items-center gap-[8px]">
                <a-switch
                  v-model:model-value="configForm.stopOnFail"
                  size="small"
                  :disabled="configForm.level === 2 && configForm.extended"
                ></a-switch>
                <div>{{ t('ms.minders.failStop') }}</div>
              </div>
            </a-form-item>
            <a-form-item class="hidden-item">
              <div class="flex items-center gap-[8px]">
                <a-switch
                  v-model:model-value="configForm.retryOnFail"
                  :disabled="configForm.level === 2 && configForm.extended"
                  size="small"
                ></a-switch>
                <div>{{ t('ms.minders.failRetry') }}</div>
              </div>
            </a-form-item>
            <template v-if="configForm.retryOnFail">
              <a-form-item>
                <template #label>
                  <div class="flex items-center">
                    <div>{{ t('ms.minders.retry') }}</div>
                    <div class="text-[var(--color-text-4)]">{{ t('ms.minders.retryTimes') }}</div>
                  </div>
                </template>
                <a-input-number
                  v-model:model-value="configForm.retryTimes"
                  mode="button"
                  :step="1"
                  :min="1"
                  :max="10"
                  :precision="0"
                  :disabled="configForm.level === 2 && configForm.extended"
                  size="small"
                  class="w-[120px]"
                ></a-input-number>
              </a-form-item>
              <a-form-item>
                <template #label>
                  <div class="flex items-center">
                    <div>{{ t('ms.minders.retrySpace') }}</div>
                    <div class="text-[var(--color-text-4)]">{{ t('ms.minders.retrySpaces') }}</div>
                  </div>
                </template>
                <a-input-number
                  v-model:model-value="configForm.retryInterval"
                  mode="button"
                  :step="100"
                  :min="0"
                  :precision="0"
                  :disabled="configForm.level === 2 && configForm.extended"
                  size="small"
                  class="w-[120px]"
                ></a-input-number>
              </a-form-item>
            </template>
          </template>
        </a-form>
        <div v-if="hasEditPermission" class="flex items-center gap-[12px] bg-[var(--color-text-fff)] pb-[16px]">
          <a-button type="primary" :loading="loading" @click="handleConfigSave">
            {{ t('common.save') }}
          </a-button>
          <a-button type="secondary" :disabled="loading" @click="handleConfigCancel">{{ t('common.cancel') }}</a-button>
        </div>
      </div>
    </template>
  </MsMinderEditor>
  <caseAssociate
    v-model:visible="caseAssociateVisible"
    :association-type="currentSelectCase"
    :test-plan-id="props.planId"
    :modules-maps="selectedAssociateCasesParams.moduleMaps"
    :protocols="selectedAssociateCasesParams.protocols"
    :node-api-test-set="nodeApiTestSet"
    :node-scenario-test-set="nodeScenarioTestSet"
    @success="writeAssociateCases"
  />
</template>

<script setup lang="ts">
  import { type FormInstance, Message, type SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsMinderEditor from '@/components/pure/ms-minder-editor/minderEditor.vue';
  import {
    MinderDropdownListItem,
    MinderEvent,
    MinderJson,
    MinderJsonNode,
    MinderJsonNodeData,
  } from '@/components/pure/ms-minder-editor/props';
  import { setCustomPriorityView } from '@/components/pure/ms-minder-editor/script/tool/utils';
  import MsSelect from '@/components/business/ms-select';
  import caseAssociate from './associateDrawer.vue';

  import { getPoolOption } from '@/api/modules/api-test/management';
  import { editPlanMinder, getPlanMinder } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useMinderStore from '@/store/modules/components/minder-editor';
  import { MinderCustomEvent } from '@/store/modules/components/minder-editor/types';
  import { filterTree, getGenerateId, mapTree } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import {
    AssociateCaseRequestParams,
    PlanMinderEditListItem,
    PlanMinderNode,
    PlanMinderNodeData,
  } from '@/models/testPlan/testPlan';
  import { CaseLinkEnum } from '@/enums/caseEnum';
  import { MinderEventName, MinderKeyEnum } from '@/enums/minderEnum';
  import { PlanMinderAssociateType, PlanMinderCollectionType, RunMode } from '@/enums/testPlanEnum';

  const props = defineProps<{
    planId: string;
    status: string;
  }>();
  const emit = defineEmits<{
    (e: 'save'): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const minderStore = useMinderStore();
  const loading = ref(false);
  const importJson = ref<MinderJson>({
    root: {} as MinderJsonNode,
    treePath: [],
  });
  const caseCountTag = t('ms.minders.caseCount');
  const resourcePoolTag = t('ms.minders.resourcePool');
  const envTag = t('ms.minders.env');

  const canShowFloatMenu = ref(false);
  const canShowAddTestPointsMenu = ref(false);
  const showAssociateCaseMenu = ref(false);
  const canShowExecuteMethodMenu = ref(false);
  const showConfigMenu = ref(false);
  const canShowDeleteMenu = ref(false);
  const extraVisible = ref<boolean>(false);
  const hasEditPermission = computed(
    () => props.status !== 'ARCHIVED' && hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE'])
  );

  /**
   * 是否可以显示下拉菜单
   */
  const canShowDropdown = ref(false);
  const dropdownList = ref<MinderDropdownListItem[]>([]);
  const checkedVal = ref<string>();

  /**
   * 检测节点可展示的菜单项
   * @param node 选中节点
   */
  function checkNodeCanShowMenu(node: PlanMinderNode) {
    const { data } = node;
    if (!hasEditPermission.value && (data?.level === 1 || data?.level === 2)) {
      // 没有编辑权限，只能查看配置菜单（功能用例只有关联用例，所以配置菜单也不能看）
      if (data?.type === PlanMinderCollectionType.FUNCTIONAL) {
        canShowFloatMenu.value = false;
      } else {
        canShowFloatMenu.value = true;
        showConfigMenu.value = true;
        showAssociateCaseMenu.value = false;
        canShowExecuteMethodMenu.value = false;
        canShowDeleteMenu.value = false;
        canShowAddTestPointsMenu.value = false;
      }
      return;
    }

    if (data?.level === 1 || data?.level === 2) {
      canShowFloatMenu.value = true;
      if (data?.type === PlanMinderCollectionType.FUNCTIONAL || (data?.level === 2 && data?.extended === true)) {
        // 功能用例分类没有执行方式、继承上级配置的测试点节点不显示切换执行方式菜单
        canShowExecuteMethodMenu.value = false;
      } else {
        canShowExecuteMethodMenu.value = true;
      }
      if (data?.level === 1) {
        // 测试分类只能添加下级测试点
        canShowAddTestPointsMenu.value = true;
        if (data?.type === PlanMinderCollectionType.FUNCTIONAL) {
          // 功能用例分类不能配置任何东西
          showConfigMenu.value = false;
        } else {
          showConfigMenu.value = true;
        }
        showAssociateCaseMenu.value = false;
        canShowDeleteMenu.value = false;
      } else {
        // 测试点只能添加同级测试点
        canShowAddTestPointsMenu.value = true;
        showAssociateCaseMenu.value = true;
        showConfigMenu.value = true;
        canShowDeleteMenu.value = true;
      }
    } else {
      nextTick(() => {
        canShowFloatMenu.value = node.data.id === 'root';
        canShowExecuteMethodMenu.value = node.data.id === 'root';
      });
      showAssociateCaseMenu.value = false;
      showConfigMenu.value = false;
      extraVisible.value = false;
      canShowDeleteMenu.value = false;
      canShowAddTestPointsMenu.value = false;
    }
  }

  const inInsertingNode = ref(false);
  /**
   * 执行插入节点
   * @param command 插入命令
   * @param node 目标节点
   */
  function execInert(command: string, data?: PlanMinderNodeData | MinderJsonNodeData) {
    if (window.minder.queryCommandState(command) !== -1) {
      window.minder.execCommand(command, data);
    }
  }

  const nodeApiTestSet = ref<SelectOptionData[]>([]);
  const nodeScenarioTestSet = ref<SelectOptionData[]>([]);

  function getTestNodeSet(nodeSet: PlanMinderNode[] = []): { name: string; id: string }[] {
    return nodeSet.map((node) => ({
      name: node.data?.text ?? '',
      id: node.data?.id ?? '',
    }));
  }

  function setCaseSelectedSet() {
    const minderJson = window.minder.exportJson()?.root;
    if (minderJson) {
      const testApiNode =
        minderJson?.children?.find((item: PlanMinderNode) => item.data.type === 'API')?.children ?? [];
      const testScenarioNode =
        minderJson?.children?.find((item: PlanMinderNode) => item.data.type === 'SCENARIO')?.children ?? [];

      nodeApiTestSet.value = getTestNodeSet(testApiNode);
      nodeScenarioTestSet.value = getTestNodeSet(testScenarioNode);
    }
  }

  /**
   * 插入节点
   * @param node 目标节点
   * @param type 插入类型
   * @param value 插入值
   */
  function insertNode(node: PlanMinderNode, type: string) {
    if (
      (node.data?.level === 2 && type === 'AppendChildNode') ||
      (node.data?.level === 1 && type === 'AppendSiblingNode') ||
      node.data?.level > 2
    ) {
      return;
    }
    let child: PlanMinderNodeData | undefined;
    // 用例数子节点
    const caseCountNodeData = {
      id: getGenerateId(),
      text: t('ms.minders.item', { count: 0 }),
      resource: [caseCountTag],
      level: 3,
      disabled: true, // 只有测试点能改文本
      isNew: true,
    };
    // 环境子节点
    const envNodeData = {
      id: getGenerateId(),
      text: t('case.execute.defaultEnv'),
      resource: [envTag],
      level: 3,
      disabled: true, // 只有测试点能改文本
      isNew: true,
    };
    // 资源池子节点
    const resourcePoolNodeData = {
      id: getGenerateId(),
      resource: [resourcePoolTag],
      text: t('ms.minders.defaultResourcePool'),
      level: 3,
      disabled: true, // 只有测试点能改文本
      isNew: true,
    };
    if (node.data?.level === 1) {
      // 测试分类下插入测试点
      child = {
        ...node.data,
        id: getGenerateId(),
        text: t('ms.minders.defaultTestSet'),
        level: 2,
        disabled: false, // 只有测试点能改文本
        isNew: true,
      };
    } else if (node.parent?.data) {
      // 测试点同级插入测试点
      child = {
        ...(node.parent.data as PlanMinderNodeData),
        id: getGenerateId(),
        text: t('ms.minders.defaultTestSet'),
        level: 2,
        disabled: false, // 只有测试点能改文本
        isNew: true,
      };
    }
    if (child) {
      inInsertingNode.value = true;
      execInert(type, child);
      nextTick(() => {
        execInert('AppendChildNode', caseCountNodeData);
        if (node.data?.type !== PlanMinderCollectionType.FUNCTIONAL) {
          // 功能用例测试点没有环境和资源池
          execInert('AppendSiblingNode', envNodeData);
          execInert('AppendSiblingNode', resourcePoolNodeData);
        }
        setTimeout(() => {
          inInsertingNode.value = false;
        }, 300); // 等待节点插入完成，动画时间 300ms
      });
    }
  }

  /**
   * 添加测试点
   */
  function addTestSet() {
    const node: PlanMinderNode = window.minder.getSelectedNode();
    if (node?.data?.level === 1) {
      insertNode(node, 'AppendChildNode');
    } else if (node?.data?.level === 2) {
      insertNode(node, 'AppendSiblingNode');
    }
  }

  // 当前激活的测试点节点
  const activePlanSet = ref<PlanMinderNode>();
  const selectNodeExecuteMethod = ref<RunMode>(); // 当前选中节点的串/并

  // 优先级与串行/并行文本映射
  const priorityTextMap: Record<number, string> = {
    1: '!',
    2: t('ms.minders.serial'),
    3: t('ms.minders.parallel'),
  };
  // 串行/并行枚举值与优先级映射
  const priorityMap: Record<RunMode, number> = {
    [RunMode.SERIAL]: 2,
    [RunMode.PARALLEL]: 3,
  };

  function getExecuteMethod(data: MinderJsonNodeData) {
    if (activePlanSet.value?.data.id === data.id) {
      return activePlanSet.value?.data.executeMethod;
    }
    return data.priority === 2 ? RunMode.SERIAL : RunMode.PARALLEL;
  }

  const configFormRef = ref<FormInstance>();
  const configForm = ref<PlanMinderNodeData>();
  const resourcePoolOptions = ref<SelectOptionData[]>([]);
  const environmentOptions = computed(() => [
    {
      label: t('testPlan.testPlanIndex.defaultEnv'),
      value: 'NONE',
    },
    ...appStore.getEnvList.map((item) => ({ label: item.name, value: item.id })),
  ]);
  // 正在给 configForm 赋值，不触发表单变更
  const switchingConfigFormData = ref(false);
  const configFormUnsaved = ref(false);

  /**
   * 检查配置表单是否未保存
   */
  function checkConfigFormUnsaved() {
    const node: MinderJsonNode = window.minder.getSelectedNode();
    if (configFormUnsaved.value) {
      if (node?.data?.id !== configForm.value?.id) {
        // 避免重复提示
        Message.warning(t('ms.minders.unsavedTip'));
        // 前面激活的配置表单未保存，先取消选中当前节点，返回到上一个节点
        if (node) {
          window.minder.toggleSelect(node);
        }
        window.minder.selectById(configForm.value?.id);
      }
      return true;
    }
    return false;
  }

  /**
   * 切换测试点配置显示
   */
  function toggleConfig() {
    if (checkConfigFormUnsaved()) {
      // 未保存配置不能通过配置按钮关闭配置抽屉
      Message.warning(t('ms.minders.unsavedTip'));
      return;
    }
    const nodes: MinderJsonNode[] = window.minder.getSelectedNodes();
    if (nodes.length > 1) {
      extraVisible.value = false;
      return;
    }
    extraVisible.value = !extraVisible.value;
    const node = nodes[0];
    switchingConfigFormData.value = true;
    if (extraVisible.value) {
      activePlanSet.value = node as PlanMinderNode;
      configForm.value = cloneDeep(activePlanSet.value.data);
    } else {
      activePlanSet.value = undefined;
      configForm.value = undefined;
    }
    nextTick(() => {
      switchingConfigFormData.value = false;
    });
  }

  /**
   * 处理执行方式切换
   * @param val 执行方式
   */
  function handleExecuteMethodMenuSelect(val: RunMode) {
    // 对节点执行优先级设置命令
    window.minder.execCommand('priority', priorityMap[val]);
    // 手动设置一次脑图的优先级 DOM 内容替换
    setCustomPriorityView(priorityTextMap);
    selectNodeExecuteMethod.value = val;
    const node: PlanMinderNode = window.minder.getSelectedNode();
    if (configForm.value?.id === node?.data.id) {
      // 更新表单的执行方式
      configForm.value.executeMethod = val;
    }
  }

  const currentSelectCase = ref<CaseLinkEnum>(CaseLinkEnum.FUNCTIONAL);
  const caseAssociateVisible = ref<boolean>(false);

  // 批量关联用例表格参数
  const selectedAssociateCasesParams = ref<AssociateCaseRequestParams>({
    projectId: '',
    moduleMaps: {},
    syncCase: true,
    apiCaseCollectionId: '',
    apiScenarioCollectionId: '',
    selectAllModule: false,
    protocols: [],
  });

  function writeAssociateCases(param: AssociateCaseRequestParams) {
    selectedAssociateCasesParams.value = { ...param };
    const node: PlanMinderNode = window.minder.getSelectedNode();
    let associateType: string = '';
    let nodeDataType = node?.data?.type;
    const isCaseCountTag = node.data?.resource?.[0] === caseCountTag;
    if (isCaseCountTag) {
      // 选中的节点是测试点下的用例数节点，需要找到测试点节点
      nodeDataType = node?.parent?.data?.type;
    }
    if (nodeDataType === PlanMinderCollectionType.SCENARIO) {
      associateType = PlanMinderAssociateType.SCENARIO_CASE;
    } else {
      associateType = param?.associateType ?? nodeDataType;
    }
    if (!isCaseCountTag) {
      // 选中的节点是测试点节点
      node.data.associateDTOS = [
        {
          ...cloneDeep(param),
          associateType,
        },
      ];
    } else if (node.parent?.data) {
      // 选中的节点是测试点下的用例数节点，需要找到测试点节点赋值
      node.parent.data.associateDTOS = [
        {
          ...cloneDeep(param),
          associateType,
        },
      ];
    }

    if (!extraVisible.value) {
      // 派发SAVE_MINDER事件触发脑图的保存处理
      minderStore.dispatchEvent(MinderEventName.SAVE_MINDER);
    }
    caseAssociateVisible.value = false;
  }

  function clearSelectedCases() {
    selectedAssociateCasesParams.value = {
      projectId: '',
      moduleMaps: {},
      syncCase: true,
      apiCaseCollectionId: '',
      apiScenarioCollectionId: '',
      selectAllModule: false,
      protocols: [],
    };
    const node: PlanMinderNode = window.minder.getNodeById(activePlanSet.value?.data.id);
    if (node?.data) {
      node.data.associateDTOS = [];
    }
  }

  /**
   * 触发关联用例
   */
  function associateCase() {
    const node: PlanMinderNode = window.minder.getSelectedNode();
    if (node.data?.resource?.[0] === caseCountTag) {
      activePlanSet.value = node.parent as PlanMinderNode;
    } else {
      activePlanSet.value = node;
    }
    currentSelectCase.value = (activePlanSet.value?.data.type as unknown as CaseLinkEnum) || CaseLinkEnum.FUNCTIONAL;
    setCaseSelectedSet();
    caseAssociateVisible.value = true;
  }

  function openCaseAssociateDrawer() {
    currentSelectCase.value = (activePlanSet.value?.data?.type as unknown as CaseLinkEnum) || CaseLinkEnum.FUNCTIONAL;
    setCaseSelectedSet();
    caseAssociateVisible.value = true;
  }

  watch(
    () => [configForm.value, selectedAssociateCasesParams.value.moduleMaps],
    () => {
      if (!switchingConfigFormData.value && configForm.value) {
        configFormUnsaved.value = true;
        minderStore.setMinderUnsaved(true);
      }
    },
    {
      deep: true,
    }
  );

  watch(
    () => minderStore.event.eventId,
    () => {
      if ([MinderEventName.EXPAND, MinderEventName.COLLAPSE].includes(minderStore.event.name)) {
        setCustomPriorityView(priorityTextMap);
      } else if (minderStore.event.name === MinderEventName.DROPDOWN_SELECT) {
        const node: PlanMinderNode = window.minder.getSelectedNode();
        if (node?.data?.level === 3 && node?.data?.resource?.[0] === resourcePoolTag) {
          if (node.parent?.data) {
            node.parent.data.testResourcePoolId = minderStore.event.params;
            minderStore.dispatchEvent(MinderEventName.SAVE_MINDER);
          }
        } else if (node?.data?.level === 3 && node?.data?.resource?.[0] === envTag) {
          if (node.parent?.data) {
            node.parent.data.environmentId = minderStore.event.params;
            minderStore.dispatchEvent(MinderEventName.SAVE_MINDER);
          }
        }
      }
    }
  );

  function handleConfigCancel() {
    clearSelectedCases();
    extraVisible.value = false;
    activePlanSet.value = undefined;
    configForm.value = undefined;
    configFormUnsaved.value = false;
  }

  /**
   * 是否可以显示批量删除
   */
  const canShowBatchDelete = ref(false);
  watch(
    () => minderStore.event.eventId,
    async () => {
      if (window.minder) {
        const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
        if (
          minderStore.event.name === MinderEventName.DRAG_FINISH ||
          minderStore.event.name === MinderEventName.NODE_UNSELECT ||
          minderStore.event.name === MinderEventName.NODE_SELECT
        ) {
          canShowBatchDelete.value = selectedNodes.every((node) => node.data?.level === 2);
        }
      }
    },
    {
      immediate: true,
    }
  );

  /**
   * 处理节点选中
   * @param node 节点
   */
  function handleNodeSelect(node: PlanMinderNode) {
    canShowDropdown.value = false;
    if (checkConfigFormUnsaved()) {
      return;
    }
    if (node.data.priority) {
      selectNodeExecuteMethod.value = getExecuteMethod(node.data);
    } else {
      selectNodeExecuteMethod.value = undefined;
    }
    if (node.data?.level === 3 && node.data?.resource?.[0] === caseCountTag) {
      canShowFloatMenu.value = false;
      if (!inInsertingNode.value && hasEditPermission && hasAnyPermission(['PROJECT_TEST_PLAN:READ+ASSOCIATION'])) {
        // 新增测试点时不自动弹出关联用例
        associateCase();
      }
    } else if (
      node.data?.level === 3 &&
      (node.data?.resource?.[0] === resourcePoolTag || node.data?.resource?.[0] === envTag)
    ) {
      canShowFloatMenu.value = false;
      canShowDropdown.value = !node.parent?.data?.extended; // 继承上级配置的测试点节点不显示下拉菜单
      if (node.data?.resource?.[0] === resourcePoolTag) {
        dropdownList.value = resourcePoolOptions.value.map((item) => ({
          label: item.label || '',
          value: item.value as string,
        }));
        checkedVal.value = node.parent?.data?.testResourcePoolId;
      } else {
        dropdownList.value = environmentOptions.value.map((item) => ({
          label: item.label || '',
          value: item.value as string,
        }));
        checkedVal.value = node.parent?.data?.environmentId;
      }
    } else {
      checkNodeCanShowMenu(node);
      if (extraVisible.value) {
        if (node.data?.type === PlanMinderCollectionType.FUNCTIONAL && node.data?.level === 1) {
          // 功能用例分类没有配置
          extraVisible.value = false;
          return;
        }
        activePlanSet.value = node;
        switchingConfigFormData.value = true;
        configForm.value = cloneDeep(activePlanSet.value.data);
        nextTick(() => {
          switchingConfigFormData.value = false;
        });
      }
    }
  }

  /**
   * 是否停止拖拽排序动作
   * @param dragNode 拖动节点
   * @param dropNode 目标节点
   */
  function stopArrangeDrag(dragNodes: MinderJsonNode | MinderJsonNode[], dropNode: MinderJsonNode) {
    if (!Array.isArray(dragNodes)) {
      dragNodes = [dragNodes];
    }
    for (let i = 0; i < dragNodes.length; i++) {
      const dragNode = (dragNodes as MinderJsonNode[])[i];
      if (dragNode.parent?.data?.id !== dropNode.parent?.data?.id && dragNode.data?.level !== 2) {
        // 不允许跨节点拖拽，只允许拖拽同一节点内的测试点排序
        return true;
      }
    }
    return false;
  }

  /**
   * 脑图命令执行前拦截
   * @param event 命令执行事件
   */
  function handleBeforeExecCommand(event: MinderEvent) {
    if (event.commandName === 'movetoparent') {
      // 不允许跨节点拖拽
      event.stopPropagation();
    } else if (event.commandName === 'arrange') {
      // 拖拽排序拦截
      const dragNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
      let dropNode: MinderJsonNode;
      if (dragNodes[0].parent?.children?.[event.commandArgs[0] as number]) {
        // 释放到目标节点后
        dropNode = dragNodes[0].parent?.children?.[event.commandArgs[0] as number];
      } else if (dragNodes[0].parent?.children?.[(event.commandArgs[0] as number) - 1]) {
        // 释放到目标节点前
        dropNode = dragNodes[0].parent?.children?.[(event.commandArgs[0] as number) - 1];
      } else {
        // 释放到最后一个节点
        dropNode = dragNodes[dragNodes.length - 1];
      }
      if (stopArrangeDrag(dragNodes, dropNode)) {
        event.stopPropagation();
      }
    }
  }

  /**
   * 处理脑图节点操作
   * @param event 脑图事件对象
   */
  function handleAction(event: MinderCustomEvent) {
    const { nodes, name } = event;
    if (nodes && nodes.length > 0) {
      switch (name) {
        case MinderEventName.DELETE_NODE:
        case MinderEventName.CUT_NODE:
          nodes.forEach((node) => {
            if (node.data?.id === activePlanSet.value?.data.id) {
              handleConfigCancel();
            }
          });
          break;
        default:
          break;
      }
    }
  }

  function handleExtendChange(val: string | number | boolean) {
    if (val && configForm.value) {
      const node: PlanMinderNode = window.minder.getNodeById(configForm.value.id);
      if (node.parent?.data) {
        const {
          priority,
          executeMethod,
          grouped,
          environmentId,
          testResourcePoolId,
          retryOnFail,
          retryType,
          retryTimes,
          retryInterval,
          stopOnFail,
        } = node.parent.data;
        configForm.value = {
          ...configForm.value,
          priority,
          executeMethod,
          grouped,
          environmentId,
          testResourcePoolId,
          retryOnFail,
          retryType,
          retryTimes,
          retryInterval,
          stopOnFail,
        };
      }
    }
  }

  /**
   * 初始化测试规划脑图
   */
  async function initMinder(firstInit = false) {
    try {
      loading.value = true;
      const res = await getPlanMinder(props.planId);
      [importJson.value.root] = mapTree(res, (node, path, level) => {
        node.data = {
          ...node.data,
          level,
          isNew: false,
          changed: false,
          priority: node.data.priority === 0 ? 1 : node.data.priority,
          disabled: level !== 2, // 只有测试点能改文本
        };
        return node;
      });
      const template = await minderStore.getMode(MinderKeyEnum.TEST_PLAN_MINDER);
      importJson.value.template = template;
      window.minder.importJson(importJson.value);
      if (firstInit) {
        await minderStore.setMode(MinderKeyEnum.TEST_PLAN_MINDER, importJson.value.template);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  const tempMinderParams = ref({
    planId: props.planId,
    editList: [] as PlanMinderEditListItem[],
    deletedIds: [],
  });

  /**
   * 保存测试点配置
   */
  function handleConfigSave() {
    configFormRef.value?.validate((errors) => {
      if (!errors) {
        const node: MinderJsonNode = window.minder.getSelectedNode();
        if (node && node?.data && configForm.value) {
          node.data = {
            ...node.data,
            ...cloneDeep(configForm.value),
          };
        }
        // 派发SAVE_MINDER事件触发脑图的保存处理
        minderStore.dispatchEvent(MinderEventName.SAVE_MINDER);
      }
    });
  }

  /**
   * 生成脑图保存的入参
   */
  function makeMinderParams(fullJson: MinderJson) {
    filterTree(fullJson.root, (node, nodeIndex) => {
      if (node.data.isNew) {
        tempMinderParams.value.editList.push({
          ...node.data,
          num: nodeIndex,
          executeMethod: getExecuteMethod(node.data),
          tempCollectionNode: true,
        });
      } else {
        tempMinderParams.value.editList.push({
          ...node.data,
          num: nodeIndex,
          executeMethod: getExecuteMethod(node.data),
        });
      }
      return node.data.level < 2;
    });
    return tempMinderParams.value;
  }

  async function handleMinderSave(fullJson: MinderJson, callback: () => void) {
    try {
      let configFormValidResult = false;
      if (extraVisible.value) {
        //  如果此时配置抽屉是打开状态，则校验配置表单并写入最新表单数据
        await configFormRef.value?.validate((errors) => {
          if (!errors) {
            const node: MinderJsonNode = window.minder.getSelectedNode();
            if (node && node?.data && configForm.value) {
              node.data = {
                ...node.data,
                ...cloneDeep(configForm.value),
                text: node.data.text, // 避免因为此时脑图节点文本被修改而配置表单内的文本还是旧的
              };
            }
            configFormValidResult = true;
          }
        });
      } else {
        configFormValidResult = true;
      }
      if (!configFormValidResult) return;
      loading.value = true;
      await editPlanMinder(makeMinderParams(window.minder.exportJson()));
      Message.success(t('common.saveSuccess'));
      inInsertingNode.value = false;
      emit('save');
      clearSelectedCases();
      handleConfigCancel();
      callback();
      initMinder(false);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
      tempMinderParams.value = {
        planId: props.planId,
        editList: [],
        deletedIds: [],
      };
      clearSelectedCases();
    }
  }

  async function initResourcePoolList() {
    try {
      const res = await getPoolOption(appStore.currentProjectId);
      resourcePoolOptions.value = res.map((e) => ({ label: e.name, value: e.id }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onBeforeMount(() => {
    initResourcePoolList();
    appStore.initEnvList();
  });

  onMounted(() => {
    initMinder(true);
    nextTick(() => {
      window.minder.on('contentchange', () => {
        // 异步执行，否则执行完，还会被重置
        setTimeout(() => {
          setCustomPriorityView(priorityTextMap);
        }, 0);
      });
      window.minder.on('selectionchange', () => {
        // 异步执行，否则执行完，还会被重置
        setTimeout(() => {
          setCustomPriorityView(priorityTextMap);
        }, 0);
      });
    });
  });
</script>

<style lang="less" scoped>
  :deep(.arco-form-item) {
    margin-bottom: 16px;
  }
</style>
