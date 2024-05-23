<template>
  <MsMinderEditor
    v-model:activeExtraKey="activeExtraKey"
    v-model:extra-visible="extraVisible"
    v-model:loading="loading"
    :tags="[]"
    :import-json="importJson"
    :replaceable-tags="replaceableTags"
    :insert-node="insertNode"
    :priority-disable-check="priorityDisableCheck"
    :after-tag-edit="afterTagEdit"
    :extract-content-tab-list="extractContentTabList"
    single-tag
    tag-enable
    sequence-enable
    @node-select="handleNodeSelect"
    @save="handleMinderSave"
  >
    <template #extractTabContent>
      <baseInfo v-if="activeExtraKey === 'baseInfo'" :loading="baseInfoLoading" :active-case="activeCase" />
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
  import { FormItem } from '@/components/pure/ms-form-create/types';
  import MsMinderEditor from '@/components/pure/ms-minder-editor/minderEditor.vue';
  import type { MinderJson, MinderJsonNode, MinderJsonNodeData } from '@/components/pure/ms-minder-editor/props';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import attachment from './attachment.vue';
  import baseInfo from './basInfo.vue';
  import bugList from './bugList.vue';
  import caseCommentList from './commentList.vue';

  import {
    checkFileIsUpdateRequest,
    getCaseDetail,
    getCaseMinder,
    getCaseModuleTree,
    saveCaseMinder,
  } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { getGenerateId, mapTree } from '@/utils';

  import { TableQueryParams } from '@/models/common';

  import { convertToFile, initFormCreate } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    moduleId: string;
    moduleName: string;
    modulesCount: Record<string, number>; // 模块数量
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const caseTag = t('common.case');
  const moduleTag = t('common.module');
  const topTags = [moduleTag, caseTag];
  const descTags = [t('ms.minders.stepDesc'), t('ms.minders.textDesc')];
  const importJson = ref<MinderJson>({
    root: {},
    template: 'default',
    treePath: [],
  });
  const caseTree = ref<MinderJsonNode[]>([]);
  const loading = ref(false);

  /**
   * 初始化用例模块树
   */
  async function initCaseTree() {
    try {
      loading.value = true;
      const res = await getCaseModuleTree({
        projectId: appStore.currentProjectId,
        moduleId: props.moduleId === 'all' ? '' : props.moduleId,
      });
      caseTree.value = mapTree<MinderJsonNode>(res, (e) => ({
        ...e,
        data: {
          id: e.id,
          text: e.name,
          resource: e.data?.id === 'fakeNode' ? [] : [moduleTag],
          expandState: e.level === 1 ? 'expand' : 'collapse',
          count: props.modulesCount[e.id],
        },
        children:
          props.modulesCount[e.id] > 0 && !e.children?.length
            ? [
                {
                  data: {
                    id: 'fakeNode',
                    text: 'fakeNode',
                    resource: ['fakeNode'],
                  },
                },
              ]
            : e.children,
      }));
      importJson.value.root = {
        children: caseTree.value,
        data: {
          id: 'all',
          text: t('ms.minders.allModule'),
          resource: [moduleTag],
        },
      };
      window.minder.importJson(importJson.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 初始化模块下脑图数据
   */
  async function initMinder() {
    try {
      loading.value = true;
      const res = await getCaseMinder({
        projectId: appStore.currentProjectId,
        moduleId: props.moduleId === 'all' ? '' : props.moduleId,
      });
      importJson.value.root.children = res;
      importJson.value.root.data = {
        id: props.moduleId === 'all' ? '' : props.moduleId,
        text: props.moduleName,
        resource: [t('common.module')],
      };
      window.minder.importJson(importJson.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  watchEffect(() => {
    if (props.moduleId === 'all') {
      initCaseTree();
    } else {
      initMinder();
    }
  });

  async function handleMinderSave(data: any) {
    try {
      await saveCaseMinder({
        projectId: appStore.currentProjectId,
        versionId: '',
        updateCaseList: data,
        updateModuleList: [],
        deleteResourceList: [],
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  /**
   * 已选中节点的可替换标签判断
   * @param node 选中节点
   */
  function replaceableTags(node: MinderJsonNode) {
    if (Object.keys(node.data || {}).length === 0 || node.data?.id === 'root') {
      // 没有数据的节点或默认模块节点不可替换
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
        node.data.resource.includes(t('ms.minders.stepDesc')) &&
        (node.parent?.children?.filter((e) => e.data?.resource?.includes(t('ms.minders.stepDesc'))) || []).length > 1
      ) {
        // 如果当前节点是步骤描述，则需要判断是否有其他步骤描述节点，如果有，则不可替换为文本描述
        return [];
      }
      return descTags.filter((tag) => !node.data?.resource?.includes(tag));
    }
    if (
      (!node.data?.resource || node.data.resource.length === 0) &&
      (!node.parent?.data?.resource ||
        node.parent?.data?.resource.length === 0 ||
        node.parent?.data?.resource?.some((e) => topTags.includes(e)))
    ) {
      // 选中节点无标签，且父节点为顶级节点，可替换为顶级标签
      // 如果选中节点子级含有用例节点或模块节点，则不可将选中节点标记为用例
      return node.children &&
        (node.children.some((e) => e.data?.resource?.includes(caseTag)) ||
          node.children.some((e) => e.data?.resource?.includes(moduleTag)))
        ? topTags.filter((e) => e !== caseTag)
        : topTags;
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
        text: t('ms.minders.precondition'),
        resource: [t('ms.minders.precondition')],
        expandState: 'expand',
      },
      children: [],
    };
    const sibling = {
      parent: child,
      data: {
        id: getGenerateId(),
        text: '',
        resource: [],
      },
    };
    execInert(type, child.data);
    nextTick(() => {
      execInert('AppendChildNode', sibling.data);
    });
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
        text: t('common.remark'),
        resource: [t('common.remark')],
      },
      children: [],
    };
    execInert(type, child.data);
  }

  // function insertTextDesc(node: MinderJsonNode, type: string) {
  //   const child = {
  //     parent: node,
  //     data: {
  //       id: getGenerateId(),
  //       text: t('ms.minders.textDesc'),
  //       resource: [t('ms.minders.textDesc')],
  //     },
  //     children: [],
  //   };
  //   const sibling = {
  //     parent: child,
  //     data: {
  //       id: getGenerateId(),
  //       text: t('ms.minders.stepExpect'),
  //       resource: [t('ms.minders.stepExpect')],
  //     },
  //   };
  //   execInert(type, {
  //     ...child,
  //     children: [sibling],
  //   });
  // }

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
        text: t('ms.minders.stepDesc'),
        resource: [t('ms.minders.stepDesc')],
      },
      children: [],
    };
    const sibling = {
      parent: child,
      data: {
        id: getGenerateId(),
        text: t('ms.minders.stepExpect'),
        resource: [t('ms.minders.stepExpect')],
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
        text: t('ms.minders.stepExpect'),
        resource: [t('ms.minders.stepExpect')],
      },
      children: [],
    };
    execInert(type, child.data);
  }

  /**
   * 插入节点
   * @param node 目标节点
   * @param type 插入类型
   */
  function insertNode(node: MinderJsonNode, type: string) {
    switch (type) {
      case 'AppendChildNode':
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
              if (child.data?.resource?.includes(t('ms.minders.precondition'))) {
                hasPreCondition = true;
              } else if (child.data?.resource?.includes(t('ms.minders.textDesc'))) {
                hasTextDesc = true;
              } else if (child.data?.resource?.includes(t('common.remark'))) {
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
          (node.data?.resource?.includes(t('ms.minders.stepDesc')) ||
            node.data?.resource?.includes(t('ms.minders.textDesc'))) &&
          (!node.children || node.children.length === 0)
        ) {
          // 当前节点是步骤描述或文本描述，且没有子节点，则默认添加一个预期结果
          insertExpect(node, 'AppendChildNode');
        } else if (node.data?.resource?.includes(t('ms.minders.precondition'))) {
          // 当前节点是前置条件，则默认添加一个文本节点
          execInert('AppendChildNode');
        }
        break;
      case 'AppendParentNode':
        execInert('AppendParentNode');
        break;
      case 'AppendSiblingNode':
        if (node.parent?.data?.resource?.includes(caseTag) && node.parent?.children) {
          // 当前节点的父节点是用例
          let hasPreCondition = false;
          let hasTextDesc = false;
          let hasRemark = false;
          for (let i = 0; i < node.parent.children.length; i++) {
            const sibling = node.parent.children[i];
            if (sibling.data?.resource?.includes(t('ms.minders.precondition'))) {
              hasPreCondition = true;
            } else if (sibling.data?.resource?.includes(t('common.remark'))) {
              hasRemark = true;
            } else if (sibling.data?.resource?.includes(t('ms.minders.textDesc'))) {
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

  /**
   * 标签编辑后，如果将标签修改为模块，则删除已添加的优先级
   * @param node 选中节点
   * @param tag 更改后的标签
   */
  function afterTagEdit(node: MinderJsonNode, tag: string) {
    if (tag === moduleTag && node.data) {
      window.minder.execCommand('priority');
    }
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
    if (activeCase.value.id) {
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
      formRules.value = initFormCreate(res.customFields, ['FUNCTIONAL_CASE:READ+UPDATE']);
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

  /**
   * 处理脑图节点激活/点击
   * @param node 被激活/点击的节点
   */
  async function handleNodeSelect(node: MinderJsonNode) {
    const { data } = node;
    if (data?.resource && data.resource.includes(caseTag)) {
      extraVisible.value = true;
      activeExtraKey.value = 'baseInfo';
      resetExtractInfo();
      initCaseDetail(data);
    } else if (data?.resource?.includes(moduleTag) && data.count > 0 && data.isLoaded !== true) {
      try {
        loading.value = true;
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
          const child = window.minder.createNode(e.data, node);
          child.render();
          e.children?.forEach((item) => {
            // 前置/步骤/备注节点
            const grandChild = window.minder.createNode(item.data, child);
            grandChild.render();
            item.children?.forEach((subItem) => {
              // 预期结果节点
              const greatGrandChild = window.minder.createNode(subItem.data, grandChild);
              greatGrandChild.render();
            });
          });
          child.expand();
          child.renderTree();
        });
        node.expand();
        node.renderTree();
        window.minder.layout();
        if (node.data) {
          node.data.isLoaded = true;
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        loading.value = false;
      }
    } else {
      extraVisible.value = false;
      resetExtractInfo();
    }
  }
</script>

<style lang="less" scoped></style>
