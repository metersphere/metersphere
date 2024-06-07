<template>
  <MsMinderEditor
    v-model:activeExtraKey="activeExtraKey"
    v-model:extra-visible="extraVisible"
    v-model:loading="loading"
    v-model:import-json="importJson"
    :tags="[]"
    :replaceable-tags="replaceableTags"
    :insert-node="insertNode"
    :priority-disable-check="priorityDisableCheck"
    :after-tag-edit="afterTagEdit"
    :extract-content-tab-list="extractContentTabList"
    :can-show-enter-node="canShowEnterNode"
    :insert-sibling-menus="insertSiblingMenus"
    :insert-son-menus="insertSonMenus"
    :can-show-paste-menu="!stopPaste()"
    :can-show-more-menu="canShowMoreMenu()"
    :can-show-priority-menu="canShowPriorityMenu()"
    :priority-tooltip="t('caseManagement.caseReview.caseLevel')"
    single-tag
    tag-enable
    sequence-enable
    @content-change="handleContentChange"
    @node-select="handleNodeSelect"
    @action="handleAction"
    @before-exec-command="handleBeforeExecCommand"
    @save="handleMinderSave"
  >
    <template #extractMenu>
      <a-tooltip v-if="showDetailMenu" :content="t('common.detail')">
        <MsButton
          type="icon"
          class="ms-minder-node-float-menu-icon-button"
          :class="[extraVisible ? 'ms-minder-node-float-menu-icon-button--focus' : '']"
          @click="toggleDetail"
        >
          <MsIcon type="icon-icon_describe_outlined" class="text-[var(--color-text-4)]" />
        </MsButton>
      </a-tooltip>
    </template>
    <template #extractTabContent>
      <baseInfo
        v-if="activeExtraKey === 'baseInfo'"
        ref="baseInfoRef"
        :loading="baseInfoLoading"
        :active-case="activeCase"
        @init-template="(id) => (templateId = id)"
        @cancel="handleBaseInfoCancel"
      />
      <attachment
        v-else-if="activeExtraKey === 'attachment'"
        v-model:model-value="fileList"
        :active-case="activeCase"
        @upload-success="initCaseDetail"
      />
      <caseCommentList v-else-if="activeExtraKey === 'comments'" :active-case="activeCase" />
      <bugList v-else :active-case="activeCase" />
    </template>
  </MsMinderEditor>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import { FormItem } from '@/components/pure/ms-form-create/types';
  import MsMinderEditor from '@/components/pure/ms-minder-editor/minderEditor.vue';
  import type {
    InsertMenuItem,
    MinderEvent,
    MinderJson,
    MinderJsonNode,
    MinderJsonNodeData,
  } from '@/components/pure/ms-minder-editor/props';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import attachment from './attachment.vue';
  import baseInfo from './basInfo.vue';
  import bugList from './bugList.vue';
  import caseCommentList from './commentList.vue';

  import {
    checkFileIsUpdateRequest,
    getCaseDetail,
    getCaseMinder,
    getCaseMinderTree,
    saveCaseMinder,
  } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useMinderStore from '@/store/modules/components/minder-editor/index';
  import { MinderCustomEvent } from '@/store/modules/components/minder-editor/types';
  import { filterTree, getGenerateId, mapTree } from '@/utils';

  import {
    FeatureCaseMinderEditType,
    FeatureCaseMinderStepItem,
    FeatureCaseMinderUpdateParams,
  } from '@/models/caseManagement/featureCase';
  import { MoveMode, TableQueryParams } from '@/models/common';
  import { MinderEventName } from '@/enums/minderEnum';

  import { convertToFile, initFormCreate } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    moduleId: string;
    moduleName: string;
    modulesCount: Record<string, number>; // 模块数量
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const minderStore = useMinderStore();

  const caseTag = t('common.case');
  const moduleTag = t('common.module');
  const topTags = [moduleTag, caseTag];
  const stepTag = t('ms.minders.stepDesc');
  const textDescTag = t('ms.minders.textDesc');
  const prerequisiteTag = t('ms.minders.precondition');
  const stepExpectTag = t('ms.minders.stepExpect');
  const remarkTag = t('ms.minders.remark');
  const descTags = [stepTag, textDescTag];
  const caseChildTags = [prerequisiteTag, stepTag, textDescTag, remarkTag];
  const caseOffspringTags = [...caseChildTags, stepTag, stepExpectTag, textDescTag, remarkTag];
  const importJson = ref<MinderJson>({
    root: {} as MinderJsonNode,
    template: 'default',
    treePath: [],
  });
  const caseTree = ref<MinderJsonNode[]>([]);
  const loading = ref(false);
  const tempMinderParams = ref<FeatureCaseMinderUpdateParams>({
    projectId: appStore.currentProjectId,
    versionId: '',
    updateCaseList: [],
    updateModuleList: [],
    deleteResourceList: [],
    additionalNodeList: [],
  });
  const templateId = ref('');

  /**
   * 初始化用例模块树
   */
  async function initCaseTree() {
    try {
      loading.value = true;
      const res = await getCaseMinderTree({
        projectId: appStore.currentProjectId,
        moduleId: props.moduleId === 'all' ? '' : props.moduleId,
      });
      caseTree.value = mapTree<MinderJsonNode>(res, (e) => ({
        ...e,
        data: {
          id: e.id,
          text: e.name,
          resource: props.modulesCount[e.id] !== undefined ? [moduleTag] : e.data?.resource,
          expandState: e.level === 1 ? 'expand' : 'collapse',
          count: props.modulesCount[e.id],
          isNew: false,
        },
        children:
          props.modulesCount[e.id] > 0 && !e.children?.length
            ? [
                {
                  data: {
                    id: 'fakeNode',
                    text: 'fakeNode',
                    resource: ['fakeNode'],
                    isNew: false,
                  },
                },
              ]
            : e.children,
      }));
      importJson.value.root = {
        children: caseTree.value,
        data: {
          id: 'NONE',
          text: t('ms.minders.allModule'),
          resource: [moduleTag],
          disabled: true,
        },
      };
      window.minder.importJson(importJson.value);
      if (props.moduleId !== 'all') {
        nextTick(() => {
          minderStore.dispatchEvent(MinderEventName.ENTER_NODE, undefined, undefined, undefined, [
            window.minder.getNodeById(props.moduleId),
          ]);
        });
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  watchEffect(() => {
    initCaseTree();
  });

  const baseInfoRef = ref<InstanceType<typeof baseInfo>>();

  /**
   * 解析用例节点信息
   * @param node 用例节点
   */
  function getCaseNodeInfo(node: MinderJsonNode) {
    let textStep: MinderJsonNode | undefined; // 文本描述
    let prerequisiteNode: MinderJsonNode | undefined; // 前置条件
    let remarkNode: MinderJsonNode | undefined; // 备注
    const stepNodes: MinderJsonNode[] = []; // 步骤描述
    node.children?.forEach((item) => {
      if (item.data?.resource?.includes(textDescTag)) {
        textStep = item;
      } else if (item.data?.resource?.includes(stepTag)) {
        stepNodes.push(item);
      } else if (item.data?.resource?.includes(prerequisiteTag)) {
        prerequisiteNode = item;
      } else if (item.data?.resource?.includes(remarkTag)) {
        remarkNode = item;
      }
    });
    const steps: FeatureCaseMinderStepItem[] = stepNodes.map((child, i) => {
      return {
        id: child.data?.id || getGenerateId(),
        num: i,
        desc: child.data?.text || '',
        result: child.children?.[0].data?.text || '',
      };
    });
    return {
      prerequisite: prerequisiteNode?.data?.text || '',
      caseEditType: steps.length > 0 ? 'STEP' : ('TEXT' as FeatureCaseMinderEditType),
      steps: JSON.stringify(steps),
      textDescription: textStep?.data?.text || '',
      expectedResult: textStep?.children?.[0]?.data?.text || '',
      description: remarkNode?.data?.text || '',
    };
  }

  /**
   * 获取节点的移动信息
   * @param node 节点
   * @param parent 父节点
   */
  function getNodeMoveInfo(nodeIndex: number, parent?: MinderJsonNode): { moveMode: MoveMode; targetId?: string } {
    const moveMode = nodeIndex === 0 ? 'BEFORE' : 'AFTER'; // 除了第一个以外，其他都是在目标节点后面插入
    return {
      moveMode,
      targetId:
        moveMode === 'BEFORE'
          ? parent?.children?.[1]?.data?.id
          : parent?.children?.[(nodeIndex || parent.children.length - 1) - 1]?.data?.id,
    };
  }

  /**
   * 生成脑图保存的入参
   */
  function makeMinderParams(fullJson: MinderJson): FeatureCaseMinderUpdateParams {
    filterTree(fullJson.root.children, (node, nodeIndex, parent) => {
      if (node.data.isNew !== false || node.data.changed === true) {
        if (node.data.resource?.includes(moduleTag)) {
          // 处理模块节点
          tempMinderParams.value.updateModuleList.push({
            id: node.data.id,
            name: node.data.text,
            parentId: parent?.data.id || 'NONE',
            type: node.data.isNew !== false ? 'ADD' : 'UPDATE',
            ...getNodeMoveInfo(nodeIndex, parent as MinderJsonNode),
          });
        } else if (node.data.resource?.includes(caseTag)) {
          // 处理用例节点
          const caseNodeInfo = getCaseNodeInfo(node as MinderJsonNode);
          const caseBaseInfo = baseInfoRef.value?.makeParams();
          tempMinderParams.value.updateCaseList.push({
            id: node.data.id,
            moduleId: parent?.data.id || '',
            type: node.data.isNew !== false ? 'ADD' : 'UPDATE',
            templateId: templateId.value,
            tags: caseBaseInfo?.tags || [],
            customFields: caseBaseInfo?.customFields || [],
            name: caseBaseInfo?.name || node.data.text,
            ...getNodeMoveInfo(nodeIndex, parent as MinderJsonNode),
            ...caseNodeInfo,
          });
          return false; // 用例的子孙节点已经处理过，跳过
        } else if (!node.data.resource || node.data.resource.length === 0) {
          // 处理文本节点
          tempMinderParams.value.additionalNodeList.push({
            id: node.data.id,
            parentId: parent?.data.id || 'NONE',
            type: node.data.isNew !== false ? 'ADD' : 'UPDATE',
            name: node.data.text,
            ...getNodeMoveInfo(nodeIndex, parent as MinderJsonNode),
          });
        }
      }

      return true;
    });
    return tempMinderParams.value;
  }

  async function handleMinderSave(fullJson: MinderJson, callback: () => void) {
    try {
      loading.value = true;
      await saveCaseMinder(makeMinderParams(fullJson));
      Message.success(t('common.saveSuccess'));
      initCaseTree();
      callback();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 已选中节点的可替换标签判断
   * @param node 选中节点
   */
  function replaceableTags(nodes: MinderJsonNode[]) {
    if (nodes.length > 1) {
      // 选中的节点大于 1 时
      if (nodes.some((e) => (e.data?.resource || []).length > 0)) {
        // 批量选中的节点已经打了标签，不可替换
        return [];
      }
      if (nodes.every((e) => (e.data?.resource || []).length === 0)) {
        // 批量选中的节点都没有打标签，可替换为模块标签
        return [moduleTag];
      }
    }
    const node = nodes[0];
    if (
      Object.keys(node.data || {}).length === 0 ||
      node.data?.id === 'root' ||
      (node.parent?.data?.resource || []).length === 0
    ) {
      // 没有数据的节点、默认模块节点、父节点为文本节点的节点不可替换标签
      return [];
    }
    if (node.data?.resource?.some((e) => topTags.includes(e))) {
      // 选中节点属于顶级节点，可替换为除自身外的顶级标签
      return !node.children || node.children.length === 0
        ? topTags.filter((tag) => !node.data?.resource?.includes(tag))
        : [];
    }
    if (node.data?.resource?.some((e) => descTags.includes(e))) {
      // 选中节点属于描述节点，可替换为除自身外的描述标签
      if (
        node.data.resource.includes(stepTag) &&
        (node.parent?.children?.filter((e) => e.data?.resource?.includes(stepTag)) || []).length > 1
      ) {
        // 如果当前节点是步骤描述，则需要判断是否有其他步骤描述节点，如果有，则不可替换为文本描述
        return [];
      }
      return descTags.filter((tag) => !node.data?.resource?.includes(tag));
    }
    if ((!node.data?.resource || node.data.resource.length === 0) && node.parent?.data?.resource?.includes(caseTag)) {
      // 选中节点无标签，且父节点为用例节点，可替换用例下级标签
      return caseChildTags;
    }
    if ((!node.data?.resource || node.data.resource.length === 0) && node.parent?.data?.resource?.includes(moduleTag)) {
      // 选中节点是文本节点、选中节点的父节点是模块节点
      if (
        (node.children &&
          (node.children.some((e) => e.data?.resource?.includes(caseTag)) ||
            node.children.some((e) => e.data?.resource?.includes(moduleTag)))) ||
        node.parent?.data?.id === 'NONE'
      ) {
        // 如果选中节点子级含有用例节点或模块节点，或者选中节点的父节点是根节点 NONE，只能将节点标记为模块节点
        return [moduleTag];
      }
      if (!node.children || node.children.length === 0) {
        // 如果选中节点无子级，可标记为用例节点或模块节点
        return topTags;
      }
    }
    return [];
  }

  /**
   * 执行插入节点
   * @param command 插入命令
   * @param node 目标节点
   */
  function execInert(command: string, node?: MinderJsonNodeData) {
    if (window.minder.queryCommandState(command) !== -1) {
      window.minder.execCommand(command, node);
      nextTick(() => {
        const newNode: MinderJsonNode = window.minder.getSelectedNode();
        if (!newNode.data) {
          newNode.data = {
            id: getGenerateId(),
            text: '',
          };
        }
        newNode.data.isNew = true; // 新建的节点标记为新建
        if (newNode.data?.resource?.some((e) => caseOffspringTags.includes(e))) {
          // 用例子孙节点更新，标记用例节点变化
          if (newNode.parent?.data?.resource?.includes(caseTag)) {
            newNode.parent.data.changed = true;
          } else if (newNode.parent?.parent?.data?.resource?.includes(caseTag)) {
            // 期望结果是第三层节点
            newNode.parent.parent.data.changed = true;
          }
        }
      });
    }
  }

  /**
   * 插入前置条件
   * @param node 目标节点
   * @param type 插入类型
   */
  function inertPrecondition(node: MinderJsonNode, type: string) {
    const child: MinderJsonNode = {
      parent: node,
      data: {
        id: getGenerateId(),
        text: prerequisiteTag,
        resource: [prerequisiteTag],
        expandState: 'expand',
        isNew: true,
      },
      children: [],
    };
    execInert(type, child.data);
  }

  /**
   * 插入备注
   * @param node 目标节点
   * @param type 插入类型
   */
  function insetRemark(node: MinderJsonNode, type: string) {
    const child = {
      parent: node,
      data: {
        id: getGenerateId(),
        text: remarkTag,
        resource: [remarkTag],
        isNew: true,
      },
      children: [],
    };
    execInert(type, child.data);
  }

  /**
   * 插入步骤描述
   * @param node 目标节点
   * @param type 插入类型
   */
  function insetStepDesc(node: MinderJsonNode, type: string) {
    const child = {
      parent: node,
      data: {
        id: getGenerateId(),
        text: stepTag,
        resource: [stepTag],
        isNew: true,
      },
      children: [],
    };
    const sibling = {
      parent: child,
      data: {
        id: getGenerateId(),
        text: stepExpectTag,
        resource: [stepExpectTag],
        isNew: true,
      },
    };
    execInert(type, child.data);
    nextTick(() => {
      execInert('AppendChildNode', sibling.data);
    });
  }

  /**
   * 插入预期结果
   * @param node 目标节点
   * @param type 插入类型
   */
  function insertExpect(node: MinderJsonNode, type: string) {
    const child = {
      parent: node,
      data: {
        id: getGenerateId(),
        text: stepExpectTag,
        resource: [stepExpectTag],
        isNew: true,
      },
      children: [],
    };
    execInert(type, child.data);
  }

  /**
   * 插入指定的节点
   * @param type 插入类型
   * @param value 节点类型
   */
  function insertSpecifyNode(type: string, value: string) {
    execInert(type, {
      id: getGenerateId(),
      text: value !== t('ms.minders.text') ? value : '',
      resource: value !== t('ms.minders.text') ? [value] : [],
      expandState: 'expand',
      isNew: true,
    });
  }

  /**
   * 插入节点
   * @param node 目标节点
   * @param type 插入类型
   * @param value 插入值
   */
  function insertNode(node: MinderJsonNode, type: string, value?: string) {
    switch (type) {
      case 'AppendChildNode':
        if (value) {
          insertSpecifyNode('AppendChildNode', value);
          break;
        }
        if (node.data?.resource?.includes(moduleTag)) {
          execInert('AppendChildNode');
        } else if (node.data?.resource?.includes(caseTag)) {
          // 给用例插入子节点
          if (!node.children || node.children.length === 0) {
            // 当前用例还没有子节点，默认添加一个前置条件
            inertPrecondition(node, type);
          } else if (node.children.length > 0) {
            // 当前用例有子节点
            let hasPreCondition = false;
            let hasTextDesc = false;
            let hasRemark = false;
            for (let i = 0; i < node.children.length; i++) {
              const child = node.children[i];
              if (child.data?.resource?.includes(prerequisiteTag)) {
                hasPreCondition = true;
              } else if (child.data?.resource?.includes(textDescTag)) {
                hasTextDesc = true;
              } else if (child.data?.resource?.includes(remarkTag)) {
                hasRemark = true;
              }
            }
            if (!hasPreCondition) {
              // 没有前置条件，则默认添加一个前置条件
              inertPrecondition(node, type);
            } else if (!hasRemark) {
              // 没有备注，则默认添加一个备注
              insetRemark(node, type);
            } else if (!hasTextDesc) {
              // 没有文本描述，则默认添加一个步骤描述
              insetStepDesc(node, type);
            }
          }
        } else if (
          (node.data?.resource?.includes(stepTag) || node.data?.resource?.includes(textDescTag)) &&
          (!node.children || node.children.length === 0)
        ) {
          // 当前节点是步骤描述或文本描述，且没有子节点，则默认添加一个预期结果
          insertExpect(node, 'AppendChildNode');
        } else if (node.data?.resource?.includes(prerequisiteTag) && (!node.children || node.children.length === 0)) {
          // 当前节点是前置条件，则默认添加一个文本节点
          execInert('AppendChildNode');
        } else {
          // 文本节点下可添加文本节点
          execInert('AppendChildNode');
        }
        break;
      case 'AppendSiblingNode':
        if (value) {
          insertSpecifyNode('AppendSiblingNode', value);
          break;
        }
        if (node.parent?.data?.resource?.includes(caseTag) && node.parent?.children) {
          // 当前节点的父节点是用例
          let hasPreCondition = false;
          let hasTextDesc = false;
          let hasRemark = false;
          for (let i = 0; i < node.parent.children.length; i++) {
            const sibling = node.parent.children[i];
            if (sibling.data?.resource?.includes(prerequisiteTag)) {
              hasPreCondition = true;
            } else if (sibling.data?.resource?.includes(remarkTag)) {
              hasRemark = true;
            } else if (sibling.data?.resource?.includes(textDescTag)) {
              hasTextDesc = true;
            }
          }
          if (!hasPreCondition) {
            // 没有前置条件，则默认添加一个前置条件
            inertPrecondition(node, type);
          } else if (!hasRemark) {
            // 没有备注，则默认添加一个备注
            insetRemark(node, type);
          } else if (!hasTextDesc) {
            // 没有文本描述，则默认添加一个步骤描述
            insetStepDesc(node, type);
          }
        } else if (node.parent?.data?.resource?.includes(moduleTag) || !node.parent?.data?.resource) {
          // 当前节点的父节点是模块或没有标签，则默认添加一个文本节点
          execInert('AppendSiblingNode');
        }
        break;
      default:
        break;
    }
  }

  /**
   * 检查节点是否可打优先级
   */
  function priorityDisableCheck(node: MinderJsonNode) {
    if (node.data?.resource?.includes(caseTag)) {
      return false;
    }
    return true;
  }

  const baseInfoLoading = ref(false);

  const formRules = ref<FormItem[]>([]);

  const extraVisible = ref<boolean>(false);
  const activeCase = ref<Record<string, any>>({});
  const extractContentTabList = computed(() => {
    const fullTabList = [
      {
        label: t('common.baseInfo'),
        value: 'baseInfo',
      },
      {
        label: t('caseManagement.featureCase.attachment'),
        value: 'attachment',
      },
      {
        value: 'comments',
        label: t('caseManagement.featureCase.comments'),
      },
      {
        value: 'bug',
        label: t('caseManagement.featureCase.bug'),
      },
    ];
    if (!activeCase.value.isNew) {
      return fullTabList;
    }
    return fullTabList.filter((item) => item.value === 'baseInfo');
  });
  const activeExtraKey = ref<'baseInfo' | 'attachment' | 'comments' | 'bug'>('baseInfo');

  const fileList = ref<MsFileItem[]>([]);
  const checkUpdateFileIds = ref<string[]>([]);

  const getListFunParams = ref<TableQueryParams>({
    combine: {
      hiddenIds: [],
    },
  });

  // 监视文件列表处理关联和本地文件
  watch(
    () => fileList.value,
    (val) => {
      if (val) {
        getListFunParams.value.combine.hiddenIds = fileList.value.filter((item) => !item.local).map((item) => item.uid);
      }
    },
    { deep: true }
  );

  /**
   * 初始化用例详情
   * @param data 节点数据/用例数据
   */
  async function initCaseDetail(data?: MinderJsonNodeData | Record<string, any>) {
    try {
      baseInfoLoading.value = true;
      const res = await getCaseDetail(data?.id || activeCase.value.id);
      activeCase.value = res;
      const fileIds = (res.attachments || []).map((item: any) => item.id) || [];
      if (fileIds.length) {
        checkUpdateFileIds.value = await checkFileIsUpdateRequest(fileIds);
      }
      if (res.attachments) {
        // 处理文件列表
        fileList.value = res.attachments
          .map((fileInfo: any) => {
            return {
              ...fileInfo,
              name: fileInfo.fileName,
              isUpdateFlag: checkUpdateFileIds.value.includes(fileInfo.id),
            };
          })
          .map((fileInfo: any) => {
            return convertToFile(fileInfo);
          });
      }
      formRules.value = initFormCreate(res.customFields, ['FUNCTIONAL_CASE:READ+UPDATE']);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      baseInfoLoading.value = false;
    }
  }

  function resetExtractInfo() {
    activeCase.value = {};
    fileList.value = [];
  }

  function handleBaseInfoCancel() {
    extraVisible.value = false;
    resetExtractInfo();
  }

  function handleContentChange(node: MinderJsonNode) {
    if (node?.data) {
      const { resource } = node.data;
      // 用例下的子节点更改，触发用例更改
      if (
        resource?.includes(prerequisiteTag) ||
        resource?.includes(stepTag) ||
        resource?.includes(textDescTag) ||
        resource?.includes(remarkTag)
      ) {
        if (node.parent?.data) {
          node.parent.data.changed = true;
        }
      } else if (node.parent?.parent?.data?.resource?.includes(caseTag)) {
        // 用例下子节点的子节点更改，触发用例更改
        node.parent.parent.data.changed = true;
      }
    }
  }

  const insertSiblingMenus = ref<InsertMenuItem[]>([]);
  const insertSonMenus = ref<InsertMenuItem[]>([]);

  /**
   * 检测节点可展示的菜单项
   * @param node 选中节点
   */
  function checkNodeCanShowMenu(node: MinderJsonNode) {
    const { data } = node;
    if (data?.resource?.includes(moduleTag)) {
      // 模块节点
      if (data?.id === 'NONE' || node.type === 'root' || node.parent?.data?.id === 'NONE') {
        // 脑图根节点、NONE虚拟节点、父节点为NONE的模块节点，不能插入同级节点
        insertSiblingMenus.value = [];
        if (data?.id === 'NONE') {
          // NONE模块节点下只能插入模块节点
          insertSonMenus.value = [
            {
              label: moduleTag,
              value: moduleTag,
            },
          ];
        } else {
          if (node.parent?.data?.id === 'NONE') {
            // 父节点为NONE的模块节点同级可以插入模块节点
            insertSiblingMenus.value = [
              {
                label: moduleTag,
                value: moduleTag,
              },
            ];
          }
          // 非 NONE模块节点下可以插入模块、用例、文本节点
          insertSonMenus.value = [
            {
              label: moduleTag,
              value: moduleTag,
            },
            {
              label: caseTag,
              value: caseTag,
            },
            {
              label: t('ms.minders.text'),
              value: t('ms.minders.text'),
            },
          ];
        }
      } else {
        // 正常模块节点同级可插入模块、用例、文本节点
        insertSiblingMenus.value = [
          {
            label: moduleTag,
            value: moduleTag,
          },
          {
            label: caseTag,
            value: caseTag,
          },
          {
            label: t('ms.minders.text'),
            value: t('ms.minders.text'),
          },
        ];
        // 正常模块节点下可插入模块、用例、文本节点
        insertSonMenus.value = [
          {
            label: moduleTag,
            value: moduleTag,
          },
          {
            label: caseTag,
            value: caseTag,
          },
          {
            label: t('ms.minders.text'),
            value: t('ms.minders.text'),
          },
        ];
      }
    } else if (data?.resource?.includes(caseTag)) {
      // 用例节点同级可插入模块、用例、文本节点
      insertSiblingMenus.value = [
        {
          label: moduleTag,
          value: moduleTag,
        },
        {
          label: caseTag,
          value: caseTag,
        },
        {
          label: t('ms.minders.text'),
          value: t('ms.minders.text'),
        },
      ];
      insertSonMenus.value = caseChildTags.map((tag) => ({
        label: tag,
        value: tag,
      }));
      if (node.children?.some((child) => child.data?.resource?.includes(stepTag))) {
        // 用例节点下有步骤描述节点，不可插入文本描述节点
        insertSonMenus.value = insertSonMenus.value.filter((e) => e.value !== textDescTag);
      } else if (node.children?.some((child) => child.data?.resource?.includes(textDescTag))) {
        // 用例节点下有文本描述节点，不可插入步骤描述和文本描述节点
        insertSonMenus.value = insertSonMenus.value.filter((e) => e.value !== stepTag && e.value !== textDescTag);
      }
      if (node.children?.some((child) => child.data?.resource?.includes(prerequisiteTag))) {
        // 用例节点下有前置条件节点，不可插入前置条件节点
        insertSonMenus.value = insertSonMenus.value.filter((e) => e.value !== prerequisiteTag);
      }
      if (node.children?.some((child) => child.data?.resource?.includes(remarkTag))) {
        // 用例节点下有备注节点，不可插入备注节点
        insertSonMenus.value = insertSonMenus.value.filter((e) => e.value !== remarkTag);
      }
    } else if (data?.resource?.some((tag) => caseChildTags.includes(tag))) {
      // 用例下的子节点
      insertSiblingMenus.value = caseChildTags.map((tag) => ({
        label: tag,
        value: tag,
      }));
      if (node.parent?.children?.some((child) => child.data?.resource?.includes(stepTag))) {
        // 用例节点下有步骤描述节点，不可插入文本描述节点
        insertSiblingMenus.value = insertSiblingMenus.value.filter((e) => e.value !== textDescTag);
      } else if (node.parent?.children?.some((child) => child.data?.resource?.includes(textDescTag))) {
        // 用例节点下有文本描述节点，不可插入步骤描述和文本描述节点
        insertSiblingMenus.value = insertSiblingMenus.value.filter(
          (e) => e.value !== stepTag && e.value !== textDescTag
        );
      }
      if (node.parent?.children?.some((child) => child.data?.resource?.includes(prerequisiteTag))) {
        // 用例节点下有前置条件节点，不可插入前置条件节点
        insertSiblingMenus.value = insertSiblingMenus.value.filter((e) => e.value !== prerequisiteTag);
      }
      if (node.parent?.children?.some((child) => child.data?.resource?.includes(remarkTag))) {
        // 用例节点下有备注节点，不可插入备注节点
        insertSiblingMenus.value = insertSiblingMenus.value.filter((e) => e.value !== remarkTag);
      }
      if (
        (data?.resource?.includes(textDescTag) || data?.resource?.includes(stepTag)) &&
        (!node.children || node.children.length === 0)
      ) {
        // 文本描述和步骤描述节点无子级时，子级可插入预期结果
        insertSonMenus.value = [
          {
            label: stepExpectTag,
            value: stepExpectTag,
          },
        ];
      } else {
        insertSonMenus.value = [];
      }
    } else {
      insertSiblingMenus.value = [];
      insertSonMenus.value = [];
    }
  }

  const showDetailMenu = ref(false);
  const canShowEnterNode = ref(false);
  /**
   * 处理脑图节点激活/点击
   * @param node 被激活/点击的节点
   */
  async function handleNodeSelect(node: MinderJsonNode) {
    checkNodeCanShowMenu(node);
    const { data } = node;
    if (
      data?.resource?.includes(moduleTag) &&
      (node.children || []).length > 0 &&
      node.type !== 'root' &&
      !data.isNew
    ) {
      // 模块节点且有子节点且非根节点且非新建节点，可展示进入节点菜单
      canShowEnterNode.value = true;
    } else {
      canShowEnterNode.value = false;
    }
    if (data?.resource && data.resource.includes(caseTag)) {
      // 用例节点才展示详情按钮
      showDetailMenu.value = true;
    } else if (data?.resource?.includes(moduleTag) && data.count > 0 && data.isLoaded !== true) {
      // 模块节点且有用例且未加载过用例数据
      try {
        loading.value = true;
        showDetailMenu.value = false;
        extraVisible.value = false;
        const res = await getCaseMinder({
          projectId: appStore.currentProjectId,
          moduleId: data.id,
        });
        const fakeNode = node.children?.find((e) => e.data?.id === undefined); // 移除占位的虚拟节点
        if (fakeNode) {
          window.minder.removeNode(fakeNode);
        }
        if ((!res || res.length === 0) && node.children?.length) {
          // 如果模块下没有用例且有别的模块节点，正常展开
          node.expand();
          node.renderTree();
          window.minder.layout();
          return;
        }
        // TODO:递归渲染存在的子节点
        res.forEach((e) => {
          // 用例节点
          const child = window.minder.createNode(
            {
              ...e.data,
              isNew: false,
            },
            node
          );
          child.render();
          e.children?.forEach((item) => {
            // 前置/步骤/备注节点
            const grandChild = window.minder.createNode(
              {
                ...item.data,
                isNew: false,
              },
              child
            );
            grandChild.render();
            item.children?.forEach((subItem) => {
              // 预期结果节点
              const greatGrandChild = window.minder.createNode(
                {
                  ...subItem.data,
                  isNew: false,
                },
                grandChild
              );
              greatGrandChild.render();
            });
          });
          child.expand();
          child.renderTree();
        });
        node.expand();
        node.renderTree();
        window.minder.layout();
        window.minder.execCommand('camera', node, 600);
        if (node.data) {
          node.data.isLoaded = true;
        }
        // 加载完用例数据后，更新当前importJson数据
        importJson.value = window.minder.exportJson();
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        loading.value = false;
      }
    } else {
      // 文本节点或已加载过用例数据的模块节点
      extraVisible.value = false;
      showDetailMenu.value = false;
      resetExtractInfo();
      if (node.children && node.children.length > 0) {
        node.expand();
        node.renderTree();
        window.minder.layout();
      }
    }
  }

  /**
   * 切换用例详情显示
   */
  async function toggleDetail() {
    extraVisible.value = !extraVisible.value;
    const node: MinderJsonNode = window.minder.getSelectedNode();
    const { data } = node;
    if (extraVisible.value) {
      if (data?.resource && data.resource.includes(caseTag)) {
        activeExtraKey.value = 'baseInfo';
        resetExtractInfo();
        if (data.isNew === false) {
          // 非新用例节点才能加载详情
          initCaseDetail(data);
        } else {
          activeCase.value = {
            id: data.id,
            name: data.text,
            isNew: true,
          };
        }
      }
    }
  }

  /**
   * 标签编辑后，如果将标签修改为模块，则删除已添加的优先级
   * @param node 选中节点
   * @param tag 更改后的标签
   */
  function afterTagEdit(nodes: MinderJsonNode[], tag: string) {
    nodes.forEach((node, index) => {
      if (tag === moduleTag && node.data) {
        // 排除是从用例节点切换到模块节点的数据
        tempMinderParams.value.updateCaseList = tempMinderParams.value.updateCaseList.filter(
          (e) => e.id !== node.data?.id
        );
        node.data.isNew = true;
        window.minder.execCommand('priority');
        if (index === nodes.length - 1) {
          nextTick(() => {
            handleNodeSelect(node);
          });
        }
      } else if (node.data?.resource?.includes(caseTag)) {
        // 排除是从模块节点切换到用例节点的数据
        tempMinderParams.value.updateModuleList = tempMinderParams.value.updateModuleList.filter(
          (e) => e.id !== node.data?.id
        );
        node.data.isNew = true;
        if (index === nodes.length - 1) {
          nextTick(() => {
            handleNodeSelect(node);
          });
        }
      } else if (node.data?.resource?.some((e) => caseOffspringTags.includes(e))) {
        // 用例子孙节点更新，标记用例节点变化
        if (node.parent?.data?.resource?.includes(caseTag)) {
          node.parent.data.changed = true;
        } else if (node.parent?.parent?.data?.resource?.includes(caseTag)) {
          // 期望结果是第三层节点
          node.parent.parent.data.changed = true;
        }
      }
    });
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
          // TODO:循环优化
          nodes.forEach((node) => {
            if (!caseOffspringTags.some((e) => node.data?.resource?.includes(e))) {
              // 非用例下的子孙节点的移除，才加入删除资源队列
              tempMinderParams.value.deleteResourceList.push({
                id: node.data?.id || getGenerateId(),
                type: node.data?.resource?.[0] || moduleTag,
              });
            }
            if (node.data?.resource?.includes(caseTag)) {
              // 删除用例节点
              tempMinderParams.value.updateCaseList = tempMinderParams.value.updateCaseList.filter(
                (e) => e.id !== node.data?.id
              );
            } else if (node.data?.resource?.includes(moduleTag)) {
              // 删除模块节点
              tempMinderParams.value.updateModuleList = tempMinderParams.value.updateModuleList.filter(
                (e) => e.id !== node.data?.id
              );
            } else if (!node.data?.resource) {
              // 删除文本节点
              tempMinderParams.value.additionalNodeList = tempMinderParams.value.additionalNodeList.filter(
                (e) => e.id !== node.data?.id
              );
            }
          });
          break;
        default:
          break;
      }
    }
  }

  function canShowMoreMenu() {
    if (window.minder) {
      const node: MinderJsonNode = window.minder.getSelectedNode();
      return node?.data?.id !== 'NONE';
    }
    return false;
  }

  function canShowPriorityMenu() {
    if (window.minder) {
      const node: MinderJsonNode = window.minder.getSelectedNode();
      return node?.data?.resource?.includes(caseTag);
    }
    return false;
  }

  /**
   * 是否停止拖拽动作
   * @param dragNode 拖动节点
   * @param dropNode 目标节点
   * @param mode 拖拽模式
   */
  function stopDrag(
    dragNodes: MinderJsonNode | MinderJsonNode[],
    dropNode: MinderJsonNode,
    mode: 'movetoparent' | 'arrange'
  ) {
    if (!Array.isArray(dragNodes)) {
      dragNodes = [dragNodes];
    }
    for (let i = 0; i < dragNodes.length; i++) {
      const dragNode = (dragNodes as MinderJsonNode[])[i];
      if (mode === 'movetoparent') {
        // 拖拽到目标节点内
        if (dragNode.data?.resource?.includes(caseTag) && dropNode.data?.id === 'NONE') {
          // 用例不能拖拽到根模块节点内
          return true;
        }
        if (
          (dragNode.data?.resource?.includes(moduleTag) || dragNode.data?.resource?.includes(caseTag)) &&
          dropNode.data?.resource?.includes(moduleTag)
        ) {
          // 模块、用例只能拖拽到模块节点内
          if (dragNode.data) {
            dragNode.data.changed = true;
          }
          return false;
        }
        if (!dragNode.data?.resource && (dropNode.data?.resource?.includes(moduleTag) || !dropNode.data?.resource)) {
          // 文本节点只能拖拽到模块、文本节点内
          if (dragNode.data) {
            dragNode.data.changed = true;
          }
          return false;
        }
        if (
          dragNode.data?.resource?.some((e) => caseChildTags.includes(e)) &&
          dropNode.data?.resource?.includes(caseTag) &&
          dragNode.parent?.data?.id === dropNode.data?.id
        ) {
          // 一个用例下的子节点只能拖拽到它自身内
          if (dragNode.parent?.data) {
            dragNode.parent.data.changed = true;
          }
          return false;
        }
      } else if (mode === 'arrange') {
        // 拖拽到目标节点前后
        if (
          (dragNode.data?.resource?.includes(moduleTag) ||
            dragNode.data?.resource?.includes(caseTag) ||
            !dragNode.data?.resource) &&
          (dropNode.data?.resource?.includes(moduleTag) ||
            dropNode.data?.resource?.includes(caseTag) ||
            !dropNode.data?.resource)
        ) {
          if (dragNode.data) {
            dragNode.data.changed = true;
          }
          // 模块、用例、文本节点只能拖拽到模块、用例、文本节点前后
          return false;
        }
        if (dragNode.data?.resource?.includes(stepTag) && dropNode.data?.resource?.includes(stepTag)) {
          if (dragNode.parent?.data) {
            dragNode.parent.data.changed = true;
          }
          // 用例节点下的步骤节点之间拖拽排序
          return false;
        }
      }
    }
    return true;
  }

  /**
   * 是否停止粘贴动作
   */
  function stopPaste() {
    const nodes = minderStore.clipboard;
    if (window.minder) {
      const node: MinderJsonNode = window.minder.getSelectedNode();
      if (!node) {
        return true;
      }
      if (node.data?.resource?.includes(moduleTag)) {
        //  NONE 虚拟模块下，只能粘贴模块
        if (node.data?.id === 'NONE' && nodes.every((e) => e.data?.resource?.includes(moduleTag))) {
          return false;
        }
        // 正常模块下，只能粘贴模块、用例、文本节点
        if (
          node.data?.id !== 'NONE' &&
          nodes.some(
            (e) => !e.data?.resource || e.data.resource.includes(moduleTag) || e.data.resource.includes(caseTag)
          )
        ) {
          return false;
        }
      }
      if (node.data?.resource?.includes(caseTag)) {
        if (nodes.every((e) => caseChildTags.some((item) => e.data?.resource?.includes(item)))) {
          // 用例节点下只能粘贴用例子节点
          if (
            nodes.length >= 1 &&
            nodes.every((e) => e.data?.resource?.includes(stepTag)) &&
            !node.children?.some((child) => child.data?.resource?.includes(textDescTag))
          ) {
            // 粘贴的节点数大于等于 1 时，只能是粘贴步骤描述节点，且当前用例下无文本描述节点
            return false;
          }
          if (nodes.length === 1) {
            // 粘贴的节点数是 1 时
            if (
              node.children?.every((child) => !child.data?.resource?.includes(prerequisiteTag)) &&
              nodes[0].data?.resource?.includes(prerequisiteTag)
            ) {
              // 用例下无前置条件且粘贴的节点是前置条件
              return false;
            }
            if (
              node.children?.every((child) => !child.data?.resource?.includes(remarkTag)) &&
              nodes[0].data?.resource?.includes(remarkTag)
            ) {
              // 用例下无备注且粘贴的节点是备注
              return false;
            }
            if (
              node.children?.every((child) => !child.data?.resource?.includes(textDescTag)) &&
              nodes[0].data?.resource?.includes(textDescTag)
            ) {
              // 用例下无文本描述且粘贴的节点是文本描述
              return false;
            }
          }
        }
      }
      if ([stepTag, textDescTag].some((tag) => node.data?.resource?.includes(tag))) {
        // 用例下的文本描述和步骤描述节点
        if (node.data?.resource?.includes(stepExpectTag)) {
          // 粘贴的是期望结果节点
          return false;
        }
      }
    }
    return true;
  }

  /**
   * 脑图命令执行前拦截
   * @param event 命令执行事件
   */
  function handleBeforeExecCommand(event: MinderEvent) {
    if (event.commandName === 'movetoparent') {
      // 拖拽到节点内拦截
      if (stopDrag(event.commandArgs[0] as MinderJsonNode, event.commandArgs[1] as MinderJsonNode, 'movetoparent')) {
        event.stopPropagation();
      }
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
      if (stopDrag(dragNodes, dropNode, 'arrange')) {
        event.stopPropagation();
      }
    } else if (event.commandName === 'paste') {
      if (stopPaste()) {
        event.stopPropagation();
      }
    } else if (event.commandName === 'cut') {
      minderStore.clipboard.forEach((node) => {
        if (node.parent && node.parent.data?.resource?.includes(caseTag)) {
          // 用例子节点更改
          node.parent.data.changed = true;
        } else if (node.parent?.parent && node.parent.parent.data?.resource?.includes(caseTag)) {
          // 用例孙子节点更改
          node.parent.parent.data.changed = true;
        }
      });
    }
  }
</script>

<style lang="less" scoped></style>
