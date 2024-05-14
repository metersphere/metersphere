<template>
  <MsMinderEditor
    :tags="tags"
    :import-json="props.importJson"
    :replaceable-tags="replaceableTags"
    :insert-node="insertNode"
    :priority-disable-check="priorityDisableCheck"
    :after-tag-edit="afterTagEdit"
    single-tag
    tag-enable
    sequence-enable
    @click="handleNodeClick"
  >
  </MsMinderEditor>
</template>

<script setup lang="ts">
  import MsMinderEditor from '@/components/pure/ms-minder-editor/minderEditor.vue';
  import type { MinderJson, MinderJsonNode, MinderJsonNodeData } from '@/components/pure/ms-minder-editor/props';

  import { useI18n } from '@/hooks/useI18n';
  import { getGenerateId } from '@/utils';

  const props = defineProps<{
    importJson: MinderJson;
  }>();

  const { t } = useI18n();

  const caseTag = t('common.case');
  const moduleTag = t('common.module');
  const topTags = [moduleTag, caseTag];
  const descTags = [t('ms.minders.stepDesc'), t('ms.minders.textDesc')];
  const tags = [...topTags, t('ms.minders.precondition'), ...descTags, t('ms.minders.stepExpect'), t('common.remark')];
  const visible = ref<boolean>(false);
  const nodeData = ref<any>({});

  function handleNodeClick(data: any) {
    if (data.resource && data.resource.includes(caseTag)) {
      visible.value = true;
      nodeData.value = data;
    }
  }

  /**
   * 已选中节点的可替换标签判断
   * @param node 选中节点
   */
  function replaceableTags(node: MinderJsonNode) {
    if (node.data?.resource?.some((e) => topTags.includes(e))) {
      // 选中节点属于顶级节点，可替换为除自身外的顶级标签
      return !node.children || node.children.length === 0
        ? topTags.filter((tag) => !node.data?.resource?.includes(tag))
        : [];
    }
    if (node.data?.resource?.some((e) => descTags.includes(e))) {
      // 选中节点属于描述节点，可替换为除自身外的描述标签
      return descTags.filter((tag) => !node.data?.resource?.includes(tag));
    }
    if (
      (!node.data?.resource || node.data?.resource?.length === 0) &&
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
</script>

<style lang="less" scoped></style>
