<template>
  <a-spin class="min-h-[400px] w-full" :loading="loading">
    <MsTree
      v-model:focus-node-key="focusNodeKey"
      :selected-keys="props.selectedKeys"
      :data="folderTree"
      :keyword="moduleKeyword"
      :expand-all="props.isExpandAll"
      :empty-text="t('project.fileManagement.noFolder')"
      :virtual-list-props="virtualListProps"
      :draggable="false"
      :field-names="{
        title: 'name',
        key: 'id',
        children: 'children',
        count: 'count',
      }"
      block-node
      title-tooltip-position="left"
      @select="folderNodeSelect"
    >
      <template #title="nodeData">
        <div class="inline-flex w-full gap-[8px]">
          <div class="one-line-text w-full text-[var(--color-text-1)]">
            <MsIcon type="icon-icon_folder_filled1" size="14" class="mr-1 text-[var(--color-text-4)]" />
            {{ nodeData.name }}
          </div>
          <div class="ms-tree-node-count ml-[4px] text-[var(--color-text-brand)]">{{ nodeData.count || 0 }}</div>
        </div>
      </template>
    </MsTree>
  </a-spin>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue';
  import { useVModel } from '@vueuse/core';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { mapTree } from '@/utils';

  import { ModuleTreeNode } from '@/models/common';

  const appStore = useAppStore();

  const { t } = useI18n();

  const props = defineProps<{
    isExpandAll: boolean;
    selectedKeys?: Array<string | number>; // 选中的节点 key
    modulesCount?: Record<string, number>; // 模块数量统计对象
    showType?: string; // 显示类型
    getTreeRequest: (params: any) => Promise<ModuleTreeNode[]>; // 获取模块树接口
    activeFolder: string | number;
    groupKeyword: string; // 搜索关键字
  }>();

  const emit = defineEmits([
    'update:selectedKeys',
    'init',
    'folderNodeSelect',
    'update:activeFolder',
    'update:groupKeyword',
  ]);

  const folderTree = ref<ModuleTreeNode[]>([]);
  const focusNodeKey = ref<string | number>('');
  const moduleKeyword = useVModel(props, 'groupKeyword', emit);
  const loading = ref(false);

  const virtualListProps = computed(() => {
    return {
      height: 'calc(100vh - 296px)',
      threshold: 200,
      fixedSize: true,
      buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
    };
  });

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(_selectedKeys: (string | number)[], node: MsTreeNodeData) {
    const offspringIds: string[] = [];
    mapTree(node.children || [], (e) => {
      offspringIds.push(e.id);
      return e;
    });

    emit('folderNodeSelect', _selectedKeys, offspringIds);
  }

  const selectedKeys = ref(props.selectedKeys || []);

  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */
  async function initModules(isSetDefaultKey = false) {
    try {
      loading.value = true;
      const res = await props.getTreeRequest(appStore.currentProjectId);
      folderTree.value = mapTree<ModuleTreeNode>(res, (e) => {
        return {
          ...e,
          hideMoreAction: e.id === 'root',
          draggable: false,
          disabled: false,
          count: props.modulesCount?.[e.id] || 0,
        };
      });
      if (isSetDefaultKey) {
        selectedKeys.value = [folderTree.value[0].id];
        emit('update:activeFolder', folderTree.value[0].id);
      }
      emit('init', folderTree.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  watch(
    () => props.showType,
    (val) => {
      if (val === 'Module') {
        initModules();
      }
    },
    {
      immediate: true,
    }
  );

  /**
   * 初始化模块文件数量
   */
  watch(
    () => props.modulesCount,
    (obj) => {
      folderTree.value = mapTree<ModuleTreeNode>(folderTree.value, (node) => {
        return {
          ...node,
          count: obj?.[node.id] || 0,
        };
      });
    }
  );
</script>

<style scoped lang="less"></style>
