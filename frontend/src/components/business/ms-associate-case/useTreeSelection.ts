import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

import type { moduleKeysType } from './types';

export interface SelectedModuleProps {
  modulesTree: MsTreeNodeData[];
  moduleCount: Record<string, any>;
}

export default function useTreeSelection(selectedModuleProps: SelectedModuleProps) {
  const { modulesTree, moduleCount } = selectedModuleProps;

  const moduleTree = ref<MsTreeNodeData[]>(modulesTree);

  const modulesCount = ref<Record<string, any>>(moduleCount);
  // 模块全选keys
  const checkedKeys = ref<Array<string | number>>([]);
  // 模块半选keys
  const halfCheckedKeys = ref<Array<string | number>>([]);
  // 模块配置集合
  const selectedModulesMaps = ref<Record<string, moduleKeysType>>({});
  // 是否全选
  const isCheckedAll = ref<boolean>(false);
  // 是否半选
  const indeterminate = ref<boolean>(false);
  // 设置节点
  function setNode(nodeData: MsTreeNodeData, checked: boolean) {
    selectedModulesMaps.value[nodeData.id] = {
      selectAll: checked,
      selectIds: new Set(),
      excludeIds: new Set(),
      count: nodeData.count,
    };
  }

  // 选中当前节点 && 取消当前节点
  function selectParent(nodeData: MsTreeNodeData, isSelected: boolean) {
    setNode(nodeData, !isSelected);
  }

  // 初始化左侧模块节点选中当前以及子节点
  function processAllCurrentNode(node: MsTreeNodeData, check: boolean) {
    if (node.children && node.children.length) {
      node.children.forEach((childrenNode: MsTreeNodeData) => processAllCurrentNode(childrenNode, check));
    }
    setNode(node, check);
  }

  // 选中当前节点以及子节点
  function checkNode(_checkedKeys: Array<string | number>, checkedData: MsTreeNodeData) {
    const { checked, node } = checkedData;
    processAllCurrentNode(node, checked);
  }

  // 全选全部&取消全选
  function setSelectAll(tree: MsTreeNodeData[], checkedAll: boolean) {
    tree.forEach((node) => {
      processAllCurrentNode(node, checkedAll);

      if (node.children) {
        setSelectAll(node.children, checkedAll);
      }
    });
  }
  // 选中全部所有的模块
  function checkAllModule(value: boolean | (string | number | boolean)[], ev: Event) {
    const checkAll = value as boolean;
    selectedModulesMaps.value.all = {
      selectAll: checkAll,
      selectIds: new Set(),
      excludeIds: new Set(),
      count: modulesCount.value.all,
    };
    // 设置所有的模块选中
    setSelectAll(moduleTree.value, checkAll);
    const lastProps = selectedModulesMaps.value.all;
    selectedModulesMaps.value.all.selectAll = lastProps.selectAll;
    selectedModulesMaps.value.all.selectIds = new Set([]);
    selectedModulesMaps.value.all.excludeIds = new Set([]);
  }
  // TODO 待优化 手动触发计算，或者每次增量更新
  const totalCount = computed(() => {
    return Object.keys(selectedModulesMaps.value).reduce((total, key) => {
      const module = selectedModulesMaps.value[key];
      if (key !== 'all') {
        // 全选
        if (module.selectAll && !module.excludeIds.size && !module.selectIds.size) {
          total += module.count;
          // 具有排除项则半选
        } else if (module.selectAll && module.excludeIds.size) {
          total += module.count - module.excludeIds.size;
          // 初始化上来选择右侧列表计算selectIds.size为和
        } else if (!module.selectAll) {
          total += module.selectIds.size;
        }
      }
      return total;
    }, 0);
  });

  watch(
    () => selectedModulesMaps.value,
    (val) => {
      const checkedKeysSet = new Set(checkedKeys.value);
      const halfCheckedKeysSet = new Set(halfCheckedKeys.value);

      if (!Object.keys(val).length) {
        checkedKeysSet.clear();
        halfCheckedKeysSet.clear();
        isCheckedAll.value = false;
        indeterminate.value = false;
      }

      Object.entries(val).forEach(([moduleId, selectedProps]) => {
        const { selectAll: selectIdsAll, selectIds, count, excludeIds } = selectedProps;
        if (selectedProps) {
          // 全选状态则其他参数的size都为0
          if (selectIdsAll && !selectIds.size && !excludeIds.size) {
            checkedKeysSet.add(moduleId);
          } else {
            checkedKeysSet.delete(moduleId);
          }

          // 半选状态
          if (excludeIds.size || (selectIds.size > 0 && selectIds.size < count)) {
            halfCheckedKeysSet.add(moduleId);
          } else {
            halfCheckedKeysSet.delete(moduleId);
          }
        }
      });

      // 更新 checkedKeys 和 halfCheckedKeys
      checkedKeys.value = Array.from(checkedKeysSet);
      halfCheckedKeys.value = Array.from(halfCheckedKeysSet);

      // 更新全选和半选状态
      const isAllCheckedModuleProps = val.all;

      if (isAllCheckedModuleProps) {
        if (totalCount.value === isAllCheckedModuleProps.count && isAllCheckedModuleProps.selectAll) {
          isCheckedAll.value = true;
        } else {
          isCheckedAll.value = false;
        }

        if (totalCount.value === 0) {
          indeterminate.value = false;
        } else if (totalCount.value < isAllCheckedModuleProps.count) {
          indeterminate.value = true;
        } else {
          indeterminate.value = false;
        }
      }
    },
    { deep: true }
  );

  // 设置模块树
  function setModuleTree(tree: MsTreeNodeData[]) {
    moduleTree.value = tree;
  }

  // 设置模块
  function setModuleCount(count: Record<string, any>) {
    modulesCount.value = count;
  }

  // 清空
  function clearSelector() {
    Object.keys(selectedModulesMaps.value).forEach((key) => {
      delete selectedModulesMaps.value[key];
    });
    checkedKeys.value = [];
    isCheckedAll.value = false;
  }

  watch(
    () => selectedModuleProps.modulesTree,
    (val) => {
      setModuleTree(val);
    },
    {
      immediate: true,
      deep: true,
    }
  );

  watch(
    () => selectedModuleProps.moduleCount,
    (val) => {
      setModuleCount(val);
    },
    {
      immediate: true,
      deep: true,
    }
  );

  return {
    selectedModulesMaps,
    checkedKeys,
    halfCheckedKeys,
    isCheckedAll,
    indeterminate,
    selectParent,
    processAllCurrentNode,
    checkNode,
    setSelectAll,
    checkAllModule,
    totalCount,
    clearSelector,
  };
}
