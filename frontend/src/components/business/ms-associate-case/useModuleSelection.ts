import { ref, watch } from 'vue';

import type { MsTableProps } from '@/components/pure/ms-table/type';
import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

import { findNodeByKey } from '@/utils';

import { SelectAllEnum } from '@/enums/tableEnum';

import type { moduleKeysType } from './types';

export interface SelectedModuleProps {
  modulesTree: MsTreeNodeData[];
  moduleCount: Record<string, any>;
}

export default function useModuleSelections<T>(
  innerSelectedModulesMaps: Record<string, moduleKeysType>,
  propsRes: MsTableProps<T>,
  tableSelectedProps: SelectedModuleProps
) {
  const moduleSelectedMap = ref<Record<string, string[]>>({});
  const { modulesTree, moduleCount } = tableSelectedProps;
  const moduleTree = ref<MsTreeNodeData[]>(modulesTree);
  const allModuleTotal = ref<Record<string, any>>(moduleCount);

  // 初始化节点，防止选择时报错
  function setUnSelectNode(moduleId: string, key = 'id') {
    if (!innerSelectedModulesMaps[moduleId]) {
      const node = findNodeByKey<MsTreeNodeData>(moduleTree.value, moduleId, key);
      innerSelectedModulesMaps[moduleId] = {
        selectAll: false,
        selectIds: new Set(),
        excludeIds: new Set(),
        count: moduleId === 'all' ? allModuleTotal.value.all : node?.count ?? 0,
      };
    }
  }

  // 初始化表格数据的选择
  function initTableDataSelected() {
    propsRes.selectedKeys = new Set([]);
    propsRes.excludeKeys = new Set([]);
    const allSelectIds: Set<string> = new Set();
    if (!innerSelectedModulesMaps.all) {
      setUnSelectNode('all');
    }
    propsRes.data.forEach((item: any) => {
      const selectAllProps = innerSelectedModulesMaps[item.moduleId];
      // 首次上来默认全选则追加moduleSelectedMap里边所对应moduleId所有的Ids
      if (
        selectAllProps &&
        selectAllProps.selectAll &&
        !selectAllProps.selectIds.size &&
        !selectAllProps.excludeIds.size
      ) {
        (moduleSelectedMap.value[item.moduleId] || []).forEach((id) => {
          allSelectIds.add(id);
          innerSelectedModulesMaps.all.selectIds.add(id);
          innerSelectedModulesMaps.all.excludeIds.delete(id);
        });
      }
      // 有选择则从选择里边的去回显选项项
      if (selectAllProps && selectAllProps.selectIds.size) {
        selectAllProps.selectIds.forEach((id) => allSelectIds.add(id));
      }
      // 有排除的则从全部的里边排除掉排除的进行回显选择项
      // 确保单独更新行选中或者取消能精确判断是否已经选中或排除
      if (selectAllProps && selectAllProps.selectAll && selectAllProps.excludeIds.size) {
        (moduleSelectedMap.value[item.moduleId] || []).forEach((id) => {
          if (!selectAllProps.excludeIds.has(id)) {
            allSelectIds.add(id);
            innerSelectedModulesMaps[item.moduleId].selectIds.add(id);
            innerSelectedModulesMaps.all.selectIds.add(id);
          } else {
            innerSelectedModulesMaps[item.moduleId].excludeIds.add(id);
            innerSelectedModulesMaps.all.excludeIds.add(id);
          }
        });
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

  // 重置模块参数
  function resetModule(moduleId: string, isChecked: boolean) {
    innerSelectedModulesMaps[moduleId].selectAll = isChecked;
    innerSelectedModulesMaps[moduleId].selectIds = new Set([]);
    innerSelectedModulesMaps[moduleId].excludeIds = new Set([]);
  }

  // 单选或复选时处理全选状态
  function setSelectedAll(moduleId: string) {
    resetModule(moduleId, true);
    const selectedProp = innerSelectedModulesMaps[moduleId];
    const isFirstSelectedAll =
      selectedProp && selectedProp.selectAll && !selectedProp.selectIds.size && !selectedProp.excludeIds.size;
    if (isFirstSelectedAll) {
      moduleSelectedMap.value[moduleId].forEach((key) => {
        propsRes.selectedKeys.add(key);
      });
    }
  }

  // 设置最新状态
  function setSelectedModuleStatus(moduleId: string) {
    const selectedProps = innerSelectedModulesMaps[moduleId];
    if (selectedProps) {
      const { selectIds: selectModuleIds, count, excludeIds, selectAll } = selectedProps;

      // 如果排除的数量等于总count则置为false且清空
      if (excludeIds.size === count) {
        resetModule(moduleId, false);
      }
      // 如果选中的数量等于总count则置为true且清空
      if (selectModuleIds.size === count) {
        resetModule(moduleId, true);
      }
      // 全选无排除则全选为了上来全选取消一条再选择则为全选
      if (selectAll && !excludeIds.size) {
        resetModule(moduleId, true);
      }
      // 右侧表格选择后再排除此刻设置为false且清空已选项
      if (!selectAll && !selectModuleIds.size) {
        resetModule(moduleId, false);
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
      // 初始化全选优先级最高，若全选则按照模块右侧列表全部分类追加集合
      if (isSelectAllModule && moduleId === 'all') {
        Object.entries(moduleSelectedMap.value).forEach(([item, allSelectIds]) => {
          allSelectIds.forEach((key) => {
            selectedProps.selectIds.add(key);
          });
        });
        // 只选择某模块则追加某模块的集合
      } else if (isSelectAllModule && moduleId !== 'all') {
        moduleSelectedMap.value[moduleId].forEach((key) => {
          selectedProps.selectIds.add(key);
        });
      }
      // 在更新上边的ids集合后进行判断是选择还是取消
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
  // 单独选择或取消行
  function rowSelectChange(record: Record<string, any>) {
    const { moduleId } = record;
    setUnSelectNode(moduleId);
    updateSelectModule(moduleId, record.id);
    updateSelectModule('all', record.id);
  }
  // 全选当前页或者取消当前页
  function selectAllChange(v: SelectAllEnum) {
    const { data } = propsRes;
    if (v === 'current') {
      propsRes.selectedKeys = new Set([]);
      data.forEach((item: any) => {
        const { moduleId } = item;
        setUnSelectNode(moduleId);
        const lastSelectedProps = innerSelectedModulesMaps[moduleId];
        // 数据为data当前分页的数据
        if (lastSelectedProps) {
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
        const selectedProps = innerSelectedModulesMaps[moduleId];
        if (selectedProps) {
          const resultIds = (moduleSelectedMap.value[moduleId] || []).filter((id) => !selectedProps.excludeIds.has(id));
          // 取消加了排除ids此刻全选状态为false，并且排除当前页ids，则从所有列表里边收集到模块ids
          resultIds.forEach((e) => {
            innerSelectedModulesMaps[moduleId].excludeIds.delete(e);
          });
        }
        setSelectedModuleStatus(moduleId);
        updateSelectModule('all', item.id);
      });
    }
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

  // 设置模块树
  function setModuleTree(tree: MsTreeNodeData[]) {
    moduleTree.value = tree;
  }

  // 设置模块
  function setModuleCount(count: Record<string, any>) {
    allModuleTotal.value = count;
  }

  watch(
    () => tableSelectedProps.modulesTree,
    (val) => {
      setModuleTree(val);
    },
    {
      immediate: true,
      deep: true,
    }
  );

  watch(
    () => tableSelectedProps.moduleCount,
    (val) => {
      if (val) {
        setModuleCount(val);
        innerSelectedModulesMaps.all = {
          selectAll: false,
          selectIds: new Set(),
          excludeIds: new Set(),
          count: val.all || 0,
        };
      }
    },
    {
      immediate: true,
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
  };
}
