import { ref, watch } from 'vue';

import type { MsTableProps } from '@/components/pure/ms-table/type';
import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

import { findNodeByKey } from '@/utils';

import { SelectAllEnum } from '@/enums/tableEnum';

import type { moduleKeysType } from './types';

export default function useModuleSelections<T>(
  innerSelectedModulesMaps: Record<string, moduleKeysType>,
  propsRes: MsTableProps<T>,
  modulesTree: MsTreeNodeData[]
) {
  const moduleSelectedMap = ref<Record<string, string[]>>({});

  const moduleTree = ref<MsTreeNodeData[]>(modulesTree);

  // 初始化表格数据的选择
  function initTableDataSelected() {
    propsRes.selectedKeys = new Set([]);
    propsRes.excludeKeys = new Set([]);
    const allSelectIds: Set<string> = new Set();
    propsRes.data.forEach((item: any) => {
      const selectAllProps = innerSelectedModulesMaps[item.moduleId];
      if (
        selectAllProps &&
        selectAllProps.selectAll &&
        !selectAllProps.selectIds.size &&
        !selectAllProps.excludeIds.size
      ) {
        (moduleSelectedMap.value[item.moduleId] || []).forEach((id) => allSelectIds.add(id));
      } else if (selectAllProps && !selectAllProps.selectAll && selectAllProps.selectIds.size) {
        selectAllProps.selectIds.forEach((id) => allSelectIds.add(id));
      }
    });

    propsRes.selectedKeys = new Set([...allSelectIds]);
  }

  // 初始化模块分类
  function initPropsDataSort() {
    propsRes.data.forEach((item: any) => {
      if (!moduleSelectedMap.value[item.moduleId]) {
        moduleSelectedMap.value[item.moduleId] = [item.id];
      }
      if (!moduleSelectedMap.value[item.moduleId].includes(item.id)) {
        moduleSelectedMap.value[item.moduleId].push(item.id);
      }
    });
  }

  // 单选或复选时处理全选状态
  function setSelectedAll(moduleId: string) {
    innerSelectedModulesMaps[moduleId].selectAll = true;
    innerSelectedModulesMaps[moduleId].selectIds = new Set([]);
    innerSelectedModulesMaps[moduleId].excludeIds = new Set([]);
    const selectedProp = innerSelectedModulesMaps[moduleId];
    if (selectedProp) {
      if (selectedProp.selectAll && !selectedProp.selectIds.size && !selectedProp.excludeIds.size) {
        moduleSelectedMap.value[moduleId].forEach((key) => {
          propsRes.selectedKeys.add(key);
        });
      }
    }
  }

  // 初始化节点，防止选择时报错
  function setUnSelectNode(moduleId: string, key = 'id') {
    if (!innerSelectedModulesMaps[moduleId]) {
      const node = findNodeByKey<MsTreeNodeData>(moduleTree.value, moduleId, key);
      innerSelectedModulesMaps[moduleId] = {
        selectAll: false,
        selectIds: new Set(),
        excludeIds: new Set(),
        count: node?.count || 0,
      };
    }
  }

  // 设置最新状态
  function setSelectedModuleStatus(moduleId: string) {
    const selectedProps = innerSelectedModulesMaps[moduleId];
    if (selectedProps) {
      const { selectIds: selectModuleIds, count, excludeIds } = selectedProps;
      if (selectModuleIds.size < count) {
        innerSelectedModulesMaps[moduleId].selectAll = false;
      }
      if (selectModuleIds.size === count) {
        setSelectedAll(moduleId);
      }
      if (excludeIds.size === count) {
        innerSelectedModulesMaps[moduleId].selectAll = false;
        innerSelectedModulesMaps[moduleId].selectIds = new Set([]);
        innerSelectedModulesMaps[moduleId].excludeIds = new Set([]);
      }
    }
  }

  // 更新选择数据
  function updateSelectModule(moduleId: string, id: string) {
    const selectedProps = innerSelectedModulesMaps[moduleId];
    if (selectedProps) {
      const selectedSet = selectedProps.selectIds;
      const excludedSet = selectedProps.excludeIds;
      const isSelectAllModule =
        selectedProps.selectAll && !selectedProps.selectIds.size && !selectedProps.excludeIds.size;

      if (isSelectAllModule && moduleId === 'all') {
        Object.entries(moduleSelectedMap.value).forEach(([item, allSelectIds]) => {
          allSelectIds.forEach((key) => {
            selectedProps.selectIds.add(key);
          });
        });
      } else if (isSelectAllModule && moduleId !== 'all') {
        moduleSelectedMap.value[moduleId].forEach((key) => {
          selectedProps.selectIds.add(key);
        });
      }

      if (selectedSet.has(id)) {
        selectedProps.excludeIds.add(id);
        selectedProps.selectIds.delete(id);
      } else if (excludedSet.has(id)) {
        selectedProps.excludeIds.delete(id);
        selectedProps.selectIds.add(id);
      } else if (!selectedSet.has(id) && !excludedSet.has(id)) {
        selectedProps.selectIds.add(id);
      }

      innerSelectedModulesMaps[moduleId] = selectedProps;
      setSelectedModuleStatus(moduleId);
    }
  }

  function rowSelectChange(record: Record<string, any>) {
    const { moduleId } = record;
    setUnSelectNode(moduleId);
    updateSelectModule(moduleId, record.id);
    updateSelectModule('all', record.id);
  }

  function selectAllChange(v: SelectAllEnum) {
    const { data } = propsRes;
    if (v === 'current') {
      propsRes.selectedKeys = new Set([]);
      data.forEach((item: any) => {
        const { moduleId } = item;
        setUnSelectNode(moduleId);
        const lastSelectedProps = innerSelectedModulesMaps[moduleId];
        if (!lastSelectedProps.selectAll) {
          innerSelectedModulesMaps[moduleId].selectIds.add(item.id);
          innerSelectedModulesMaps[moduleId].excludeIds.delete(item.id);
          setSelectedModuleStatus(moduleId);
        }
        updateSelectModule('all', item.id);
      });
    } else {
      data.forEach((item: any) => {
        const { moduleId } = item;
        setUnSelectNode(moduleId);
        innerSelectedModulesMaps[item.moduleId].selectIds.delete(item.id);
        innerSelectedModulesMaps[item.moduleId].excludeIds.add(item.id);
        setSelectedModuleStatus(moduleId);
        updateSelectModule('all', item.id);
      });
    }
  }

  function setModuleTree(tree: MsTreeNodeData[]) {
    moduleTree.value = tree;
  }

  function clearSelector() {
    Object.keys(innerSelectedModulesMaps).forEach((key) => {
      delete innerSelectedModulesMaps[key];
    });
  }

  watch(
    () => propsRes.data,
    async () => {
      await initPropsDataSort();
      initTableDataSelected();
    },
    {
      deep: true,
      immediate: true,
    }
  );

  watch(
    () => innerSelectedModulesMaps,
    () => {
      initTableDataSelected();
    },
    {
      deep: true,
    }
  );

  return {
    moduleSelectedMap,
    rowSelectChange,
    selectAllChange,
    initTableDataSelected,
    initPropsDataSort,
    setUnSelectNode,
    setSelectedModuleStatus,
    setSelectedAll,
    updateSelectModule,
    clearSelector,
    setModuleTree,
  };
}
