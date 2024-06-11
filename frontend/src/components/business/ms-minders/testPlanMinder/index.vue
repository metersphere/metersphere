<template>
  <MsMinderEditor
    v-model:extra-visible="extraVisible"
    v-model:loading="loading"
    v-model:import-json="importJson"
    :tags="[]"
    :insert-node="insertNode"
    :can-show-enter-node="false"
    :insert-sibling-menus="insertSiblingMenus"
    :insert-son-menus="insertSonMenus"
    :can-show-paste-menu="false"
    :can-show-more-menu="false"
    :can-show-priority-menu="false"
    :can-show-float-menu="canShowFloatMenu"
    custom-priority
    single-tag
    tag-enable
    sequence-enable
    @content-change="handleContentChange"
    @node-select="checkNodeCanShowMenu"
    @before-exec-command="handleBeforeExecCommand"
    @save="handleMinderSave"
  >
    <template #extractMenu>
      <a-dropdown
        v-if="canShowExecuteMethodMenu"
        v-model:popup-visible="executeMethodMenuVisible"
        class="ms-minder-dropdown"
        :popup-translate="[0, 4]"
        position="bl"
        trigger="click"
        @select="(val) => handleExecuteMethodMenuSelect(val as RunMode)"
      >
        <a-tooltip :content="t('ms.minders.executeMethod')">
          <MsButton
            type="icon"
            class="ms-minder-node-float-menu-icon-button"
            :class="[executeMethodMenuVisible ? 'ms-minder-node-float-menu-icon-button--focus' : '']"
          >
            <MsIcon type="icon-icon_play-round_filled" class="text-[var(--color-text-4)]" />
          </MsButton>
        </a-tooltip>
        <template #content>
          <div class="mx-[6px] px-[8px] py-[3px] text-[var(--color-text-4)]">
            {{ t('ms.minders.executeMethod') }}
          </div>
          <a-doption :value="RunMode.SERIAL">
            <div
              class="flex h-[20px] w-[20px] items-center justify-center rounded-full bg-[rgb(var(--link-1))] text-[12px] font-medium text-[rgb(var(--link-5))]"
            >
              {{ t('ms.minders.serial') }}
            </div>
          </a-doption>
          <a-doption :value="RunMode.PARALLEL">
            <div
              class="flex h-[20px] w-[20px] items-center justify-center rounded-full bg-[rgb(var(--success-1))] text-[12px] font-medium text-[rgb(var(--success-6))]"
            >
              {{ t('ms.minders.parallel') }}
            </div>
          </a-doption>
        </template>
      </a-dropdown>
      <a-tooltip v-if="showConfigMenu" :content="t('common.config')">
        <MsButton
          type="icon"
          class="ms-minder-node-float-menu-icon-button"
          :class="[extraVisible ? 'ms-minder-node-float-menu-icon-button--focus' : '']"
          @click="toggleDetail"
        >
          <MsIcon type="icon-icon_setting_filled" class="text-[var(--color-text-4)]" />
        </MsButton>
      </a-tooltip>
    </template>
    <template #extractTabContent>
      <div class="px-[16px]">
        <a-form ref="configFormRef" :model="configForm" layout="vertical">
          <a-form-item>
            <template #label>
              <div class="flex items-center">
                <div>{{ t('testPlan.planForm.pickCases') }}</div>
                <a-divider margin="4px" direction="vertical" />
                <MsButton
                  type="text"
                  :disabled="
                    (selectedAssociateCasesParams.totalCount || selectedAssociateCasesParams.selectIds.length) === 0
                  "
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
                    t('caseManagement.caseReview.selectedCases', {
                      count: selectedAssociateCasesParams.selectAll
                        ? selectedAssociateCasesParams.totalCount
                        : selectedAssociateCasesParams.selectIds.length,
                    })
                  }}
                </div>
                <a-divider margin="8px" direction="vertical" />
                <MsButton
                  v-permission="['CASE_REVIEW:READ+RELEVANCE']"
                  type="text"
                  class="font-medium"
                  @click="caseAssociateVisible = true"
                >
                  {{ t('ms.case.associate.title') }}
                </MsButton>
              </div>
            </div>
          </a-form-item>
          <a-form-item :label="t('system.project.resourcePool')">
            <a-select v-model:model-value="configForm.resourcePool" :options="resourcePoolOptions"></a-select>
          </a-form-item>
          <a-form-item class="hidden-item">
            <a-radio-group v-model:model-value="configForm.executeType">
              <a-radio value="serial">{{ t('testPlan.testPlanIndex.serial') }}</a-radio>
              <a-radio value="parallel">{{ t('testPlan.testPlanIndex.parallel') }}</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item v-if="configForm.executeType === 'serial'" class="hidden-item">
            <div class="flex items-center gap-[8px]">
              <a-switch v-model:model-value="configForm.failStop" size="small"></a-switch>
              <div>{{ t('ms.minders.failStop') }}</div>
            </div>
          </a-form-item>
          <a-form-item class="hidden-item">
            <div class="flex items-center gap-[8px]">
              <a-switch v-model:model-value="configForm.failRetry" size="small"></a-switch>
              <div>{{ t('ms.minders.failRetry') }}</div>
            </div>
          </a-form-item>
          <template v-if="configForm.failRetry">
            <a-form-item class="hidden-item">
              <a-radio-group v-model:model-value="configForm.failRetryType">
                <a-radio value="step">{{ t('ms.minders.stepRetry') }}</a-radio>
                <a-radio value="scenario">{{ t('ms.minders.scenarioRetry') }}</a-radio>
              </a-radio-group>
            </a-form-item>
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
                :precision="0"
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
                v-model:model-value="configForm.retrySpace"
                mode="button"
                :step="100"
                :min="0"
                :precision="0"
                size="small"
                class="w-[120px]"
              ></a-input-number>
            </a-form-item>
          </template>
          <a-form-item class="hidden-item">
            <div class="flex items-center gap-[8px]">
              <a-switch v-model:model-value="configForm.extend" size="small"></a-switch>
              <div>{{ t('ms.minders.extend') }}</div>
            </div>
          </a-form-item>
        </a-form>
      </div>
    </template>
  </MsMinderEditor>
  <caseAssociate
    v-model:visible="caseAssociateVisible"
    v-model:currentSelectCase="currentSelectCase"
    :has-not-associated-ids="selectedAssociateCasesParams.selectIds"
    test-plan-id=""
    @success="writeAssociateCases"
  />
</template>

<script setup lang="ts">
  import { FormInstance, SelectOptionData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsMinderEditor from '@/components/pure/ms-minder-editor/minderEditor.vue';
  import { InsertMenuItem, MinderEvent, MinderJson, MinderJsonNode } from '@/components/pure/ms-minder-editor/props';
  import { setCustomPriorityView } from '@/components/pure/ms-minder-editor/script/tool/utils';
  import caseAssociate from './associateDrawer.vue';

  import { getPlanMinder } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useMinderStore from '@/store/modules/components/minder-editor';
  import { filterTree, mapTree } from '@/utils';

  import { AssociateCaseRequest } from '@/models/testPlan/testPlan';
  import { CaseLinkEnum } from '@/enums/caseEnum';
  import { RunMode } from '@/enums/testPlanEnum';

  import Message from '@arco-design/web-vue/es/message';

  const props = defineProps<{
    planId: string;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const minderStore = useMinderStore();
  const loading = ref(false);
  const extraVisible = ref<boolean>(false);
  const importJson = ref<MinderJson>({
    root: {} as MinderJsonNode,
    template: 'default',
    treePath: [],
  });

  /**
   * 插入节点
   * @param node 目标节点
   * @param type 插入类型
   * @param value 插入值
   */
  function insertNode(node: MinderJsonNode, type: string, value?: string) {
    switch (type) {
      case 'AppendChildNode':
        break;
      case 'AppendSiblingNode':
        break;
      default:
        break;
    }
  }

  function handleContentChange(node: MinderJsonNode) {}

  const insertSiblingMenus = ref<InsertMenuItem[]>([]);
  const insertSonMenus = ref<InsertMenuItem[]>([]);
  const canShowFloatMenu = ref(false);
  const canShowExecuteMethodMenu = ref(true);
  const executeMethodMenuVisible = ref(false);
  const showConfigMenu = ref(false);

  /**
   * 检测节点可展示的菜单项
   * @param node 选中节点
   */
  function checkNodeCanShowMenu(node: MinderJsonNode) {
    const { data } = node;

    if (data?.level === 1 || data?.level === 2) {
      canShowFloatMenu.value = true;
      canShowExecuteMethodMenu.value = true;
      if (data?.level === 1) {
        insertSiblingMenus.value = [];
        insertSonMenus.value = [
          {
            value: 'testSet',
            label: t('ms.minders.testSet'),
          },
        ];
        showConfigMenu.value = true;
      } else {
        insertSiblingMenus.value = [];
        insertSonMenus.value = [];
      }
    } else {
      canShowFloatMenu.value = false;
      canShowExecuteMethodMenu.value = false;
    }
  }

  const currentPriority = ref<RunMode>(RunMode.SERIAL);
  const priorityTextMap = {
    2: t('ms.minders.serial'),
    3: t('ms.minders.parallel'),
  };
  const priorityMap = {
    [RunMode.SERIAL]: 2,
    [RunMode.PARALLEL]: 3,
  };
  function handleExecuteMethodMenuSelect(val: RunMode) {
    currentPriority.value = val;
    window.minder.execCommand('priority', priorityMap[val]);
    setCustomPriorityView(priorityTextMap);
  }

  /**
   * 切换用例详情显示
   */
  async function toggleDetail() {
    extraVisible.value = !extraVisible.value;
    const node: MinderJsonNode = window.minder.getSelectedNode();
    const { data } = node;
    if (extraVisible.value) {
      if (data?.resource && data.resource.includes('')) {
        console.log();
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
    }
    return true;
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

  const tempMinderParams = ref({
    projectId: appStore.currentProjectId,
    versionId: '',
    updateCaseList: [],
    updateModuleList: [],
    deleteResourceList: [],
    additionalNodeList: [],
  });

  const configFormRef = ref<FormInstance>();
  const configForm = ref({
    resourcePool: '',
    executeType: 'serial',
    failStop: true,
    failRetry: true,
    failRetryType: 'step',
    retryTimes: 1,
    retrySpace: 1000,
    extend: true,
  });
  const resourcePoolOptions = ref<SelectOptionData[]>();

  const currentSelectCase = ref<keyof typeof CaseLinkEnum>('FUNCTIONAL');
  const caseAssociateVisible = ref<boolean>(false);
  const caseAssociateProject = ref(appStore.currentProjectId);

  // 批量关联用例表格参数
  const selectedAssociateCasesParams = ref<AssociateCaseRequest>({
    excludeIds: [],
    selectIds: [],
    selectAll: false,
    condition: {},
    moduleIds: [],
    versionId: '',
    refId: '',
    projectId: '',
  });

  function writeAssociateCases(param: AssociateCaseRequest) {
    selectedAssociateCasesParams.value = { ...param };
    caseAssociateVisible.value = false;
  }

  function clearSelectedCases() {
    selectedAssociateCasesParams.value = {
      excludeIds: [],
      selectIds: [],
      selectAll: false,
      condition: {},
      moduleIds: [],
      versionId: '',
      refId: '',
      projectId: '',
    };
  }

  /**
   * 初始化测试规划脑图
   */
  async function initMinder() {
    try {
      loading.value = true;
      const res = await getPlanMinder(props.planId);
      [importJson.value.root] = mapTree(res, (node, path, level) => {
        node.data = {
          ...node.data,
          level,
          isNew: false,
          changed: false,
        };
        return node;
      });
      window.minder.importJson(importJson.value);
      window.minder.execCommand('template', Object.keys(window.kityminder.Minder.getTemplateList())[3]);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 生成脑图保存的入参
   */
  function makeMinderParams(fullJson: MinderJson) {
    filterTree(fullJson.root.children, (node, nodeIndex, parent) => {
      if (node.data.isNew !== false || node.data.changed === true) {
        return true;
      }
      return true;
    });
    return tempMinderParams.value;
  }

  async function handleMinderSave(fullJson: MinderJson, callback: () => void) {
    try {
      loading.value = true;
      // await saveCaseMinder(makeMinderParams(fullJson));
      Message.success(t('common.saveSuccess'));
      initMinder();
      callback();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  onMounted(() => {
    initMinder();
    nextTick(() => {
      window.minder.on('contentchange', () => {
        // 异步执行，否则执行完，还会被重置
        setTimeout(() => {
          setCustomPriorityView(priorityTextMap);
        }, 0);
      });
      window.minder.on('selectionchange', () => {
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
