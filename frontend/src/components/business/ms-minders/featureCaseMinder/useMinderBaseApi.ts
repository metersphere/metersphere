import usePriority from '@/components/pure/ms-minder-editor/hooks/useMinderPriority';
import type {
  MinderEvent,
  MinderJsonNode,
  MinderJsonNodeData,
  MinderMenuItem,
} from '@/components/pure/ms-minder-editor/props';

import { useI18n } from '@/hooks/useI18n';
import useMinderStore from '@/store/modules/components/minder-editor';
import { getGenerateId } from '@/utils';

/**
 * 封装用例脑图基础功能，包含菜单显隐判断、节点插入、节点替换、节点拖拽等
 * @returns API 集合
 */
export default function useMinderBaseApi({ hasEditPermission }: { hasEditPermission?: boolean }) {
  const { t } = useI18n();
  const minderStore = useMinderStore();
  const { setPriority } = usePriority({ priorityStartWithZero: true, priorityPrefix: 'P' });

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

  /**
   * 是否可展示浮动菜单
   */
  function canShowFloatMenu() {
    if (window.minder) {
      const node: MinderJsonNode = window.minder.getSelectedNode();
      if (node?.data?.type === 'tmp') {
        // 临时节点不展示浮动菜单
        return false;
      }
      if (!hasEditPermission) {
        if (node?.data?.resource?.includes(caseTag)) {
          // 没有编辑权限情况下，用例节点可展示浮动菜单（需要展示详情按钮）
          return true;
        }
        return false;
      }
    }
    return true;
  }

  const insertSiblingMenus = ref<MinderMenuItem[]>([]);
  const insertSonMenus = ref<MinderMenuItem[]>([]);

  /**
   * 检测节点可展示的菜单项
   * @param node 选中节点
   */
  function checkNodeCanShowMenu(node: MinderJsonNode) {
    if (!hasEditPermission) {
      insertSiblingMenus.value = [];
      insertSonMenus.value = [];
      return;
    }
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
        // 用例节点下有前置操作节点，不可插入前置操作节点
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
        // 用例节点下有前置操作节点，不可插入前置操作节点
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

  /**
   * 是否可展示更多菜单
   */
  function canShowMoreMenu() {
    if (!hasEditPermission) {
      return false;
    }
    if (window.minder) {
      const node: MinderJsonNode = window.minder.getSelectedNode();
      // 选中节点不为虚拟根节点时，可展示更多菜单
      return node?.data?.id !== 'NONE';
    }
    return false;
  }

  /**
   * 是否可展示优先级菜单
   */
  function canShowPriorityMenu() {
    if (!hasEditPermission) {
      return false;
    }
    if (window.minder) {
      const nodes: MinderJsonNode[] = window.minder.getSelectedNodes();
      // 选中节点是用例节点时，可展示优先级菜单
      return nodes.every((node) => !!node.data?.resource?.includes(caseTag));
    }
    return false;
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
    if (node.parent?.data?.id === 'NONE') {
      // 父节点为NONE虚拟根节点下只能替换为模块标签
      return [moduleTag];
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
   * 检查节点是否可打优先级
   */
  function priorityDisableCheck(node: MinderJsonNode) {
    if (!hasEditPermission) {
      return false;
    }
    if (node.data?.resource?.includes(caseTag)) {
      return false;
    }
    return true;
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
   * 插入指定的节点
   * @param type 插入类型
   * @param value 节点类型
   */
  function insertSpecifyNode(type: string, value: string) {
    const nodeId = getGenerateId();
    execInert(type, {
      id: nodeId,
      text: value !== t('ms.minders.text') ? value : '',
      resource: value !== t('ms.minders.text') ? [value] : [],
      expandState: 'expand',
      isNew: true,
      priority: value === caseTag ? 1 : undefined,
    });
    if (value === caseTag) {
      // 用例节点插入后，插入子节点
      setPriority('1');
      nextTick(() => {
        insertSpecifyNode('AppendChildNode', prerequisiteTag);
        // 上面插入了子节点前置条件后会选中该节点，所以下面插入同级
        insertSpecifyNode('AppendSiblingNode', stepTag);
        insertSpecifyNode('AppendChildNode', stepExpectTag);
        window.minder.selectById(nodeId);
        insertSpecifyNode('AppendChildNode', remarkTag);
        setTimeout(() => {
          // 取消选中备注节点，选中用例节点
          const remarkNode: MinderJsonNode = window.minder.getSelectedNode();
          window.minder.toggleSelect(remarkNode);
          window.minder.selectById(nodeId);
        }, 0);
      });
    } else if (value === stepTag) {
      // 步骤描述节点插入后，插入子节点
      nextTick(() => {
        insertSpecifyNode('AppendChildNode', stepExpectTag);
        nextTick(() => {
          // 取消选中期望结果节点，选中步骤描述节点
          const expectNode: MinderJsonNode = window.minder.getSelectedNode();
          window.minder.toggleSelect(expectNode);
          window.minder.selectById(nodeId);
        });
      });
    } else if (value === textDescTag) {
      // 文本描述节点插入后，插入子节点
      nextTick(() => {
        insertSpecifyNode('AppendChildNode', stepExpectTag);
        nextTick(() => {
          // 取消选中期望结果节点，选中文本描述节点
          const expectNode: MinderJsonNode = window.minder.getSelectedNode();
          window.minder.toggleSelect(expectNode);
          window.minder.selectById(nodeId);
        });
      });
    }
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
        if (node.data?.resource?.includes(moduleTag)) {
          if (value) {
            insertSpecifyNode('AppendChildNode', value);
          } else {
            execInert('AppendChildNode');
          }
        } else if (node.data?.resource?.includes(caseTag)) {
          // 给用例插入子节点
          if (value) {
            insertSpecifyNode('AppendChildNode', value);
          } else if (!node.children || node.children.length === 0) {
            // 当前用例还没有子节点，默认添加一个前置操作
            insertSpecifyNode('AppendChildNode', prerequisiteTag);
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
              // 没有前置操作，则默认添加一个前置操作
              insertSpecifyNode('AppendChildNode', prerequisiteTag);
            } else if (!hasRemark) {
              // 没有备注，则默认添加一个备注
              insertSpecifyNode('AppendChildNode', remarkTag);
            } else if (!hasTextDesc) {
              // 没有文本描述，则默认添加一个步骤描述
              insertSpecifyNode('AppendChildNode', stepTag);
            }
          }
        } else if (
          (node.data?.resource?.includes(stepTag) || node.data?.resource?.includes(textDescTag)) &&
          (!node.children || node.children.length === 0)
        ) {
          // 当前节点是步骤描述或文本描述，且没有子节点，则默认添加一个预期结果
          insertSpecifyNode('AppendChildNode', value || stepExpectTag);
        } else if (
          (!node.data?.resource || node.data?.resource?.length === 0) &&
          (!node.parent?.data?.resource || node.parent?.data?.resource?.length === 0)
        ) {
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
            // 没有前置操作，则默认添加一个前置操作
            insertSpecifyNode('AppendSiblingNode', prerequisiteTag);
          } else if (!hasRemark) {
            // 没有备注，则默认添加一个备注
            insertSpecifyNode('AppendSiblingNode', remarkTag);
          } else if (!hasTextDesc) {
            // 没有文本描述，则默认添加一个步骤描述
            insertSpecifyNode('AppendSiblingNode', stepTag);
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
        if (node.data?.id === 'root') {
          // 未规划节点下只能粘贴用例
          if (!nodes.every((e) => e.data?.resource?.includes(caseTag))) {
            return true;
          }
        }
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
              // 用例下无前置操作且粘贴的节点是前置操作
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
        if (
          node.children?.length === 0 &&
          minderStore.clipboard.every((e) => e.data?.resource?.includes(stepExpectTag))
        ) {
          // 粘贴的是期望结果节点
          return false;
        }
      }
    }
    return true;
  }

  /**
   * 是否可展示更多菜单节点操作(复制、剪切、粘贴、删除)
   */
  function canShowMoreMenuNodeOperation() {
    if (window.minder) {
      const node: MinderJsonNode = window.minder.getSelectedNode();
      if (node?.data?.id === 'NONE') {
        // 虚拟根节点不展示节点操作
        return false;
      }
      if (node?.data?.resource?.includes(moduleTag) && node?.data?.id === 'root') {
        // 根模块节点不展示节点操作
        return false;
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

  /**
   * 处理节点文本变更
   * @param node 变更节点
   */
  function handleContentChange(node?: MinderJsonNode) {
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

  return {
    caseTag,
    moduleTag,
    topTags,
    stepTag,
    textDescTag,
    prerequisiteTag,
    stepExpectTag,
    remarkTag,
    descTags,
    caseChildTags,
    caseOffspringTags,
    insertSiblingMenus,
    insertSonMenus,
    insertNode,
    handleBeforeExecCommand,
    stopPaste,
    checkNodeCanShowMenu,
    canShowFloatMenu,
    canShowMoreMenu,
    canShowPriorityMenu,
    handleContentChange,
    replaceableTags,
    priorityDisableCheck,
    canShowMoreMenuNodeOperation,
  };
}
