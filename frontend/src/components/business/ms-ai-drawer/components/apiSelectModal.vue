<template>
  <a-modal
    v-model:visible="visible"
    :title="t('ms.ai.choseApi')"
    title-align="start"
    body-class="ms-ai-config-modal p-0"
    :width="800"
    :ok-text="t('common.confirm')"
    :ok-button-props="{
      disabled: selectedKeys.length === 0,
    }"
    unmount-on-close
    @before-ok="handleOk"
    @cancel="handleCancel"
  >
    <a-spin class="block h-full w-full" :loading="loading">
      <a-input
        v-model:model-value="moduleKeyword"
        :placeholder="t('apiTestManagement.searchTip')"
        class="mb-[16px]"
        allow-clear
      />
      <MsTree
        v-model:selected-keys="selectedKeys"
        :data="folderTree"
        :keyword="moduleKeyword"
        :default-expand-all="false"
        :expand-all="false"
        :empty-text="t('apiTestManagement.noMatchModuleAndApi')"
        :virtual-list-props="{
          height: 'calc(60vh - 190px)',
          threshold: 200,
          fixedSize: true,
          buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
        }"
        :field-names="{
          title: 'name',
          key: 'id',
          children: 'children',
          count: 'count',
        }"
        block-node
        :selectable="nodeSelectable"
      >
        <template #title="nodeData">
          <div v-if="nodeData.type === 'API'" class="inline-flex w-full cursor-pointer gap-[4px]">
            <apiMethodName :method="nodeData.attachInfo?.method || nodeData.attachInfo?.protocol" />
            <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
          </div>
          <div v-else :id="nodeData.id" class="inline-flex w-full gap-[8px]">
            <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
            <div class="ms-tree-node-count ml-[4px] text-[var(--color-text-brand)]">
              {{ modulesCount[nodeData.id] || 0 }}
            </div>
          </div>
        </template>
      </MsTree>
    </a-spin>
  </a-modal>
</template>

<script setup lang="ts">
  import MsTree from '@/components/business/ms-tree/index.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';

  import { getProtocolList } from '@/api/modules/api-test/common';
  import { getModuleCount, getModuleTree } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';
  import { mapTree, TreeNode } from '@/utils';

  import { ApiDefinitionGetModuleParams } from '@/models/apiTest/management';
  import { ModuleTreeNode } from '@/models/common';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';

  const { t } = useI18n();
  const appStore = useAppStore();
  const { openNewPage } = useOpenNewPage();

  const visible = defineModel<boolean>('visible', { required: true });

  const loading = ref<boolean>(false);
  const folderTree = ref<TreeNode<ModuleTreeNode>[]>([]);
  const selectedKeys = ref<string[]>([]);
  const moduleKeyword = ref<string>('');
  const modulesCount = ref<Record<string, number>>({});
  const allProtocolList = ref<string[]>([]);

  function nodeSelectable(node: TreeNode<ModuleTreeNode>) {
    return node.type === 'API';
  }

  async function initProtocolList() {
    try {
      const res = await getProtocolList(appStore.currentOrgId);
      allProtocolList.value = res.map((e) => e.protocol);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function initModuleCount(params: ApiDefinitionGetModuleParams) {
    try {
      loading.value = true;
      const res = await getModuleCount(params);
      modulesCount.value = res;
      folderTree.value = mapTree<ModuleTreeNode>(folderTree.value, (node) => {
        return {
          ...node,
          count: res[node.id] || 0,
        };
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 初始化模块树
   */
  async function initModules() {
    try {
      loading.value = true;
      const res = await getModuleTree({
        keyword: '',
        protocols: allProtocolList.value,
        projectId: appStore.currentProjectId,
        moduleIds: [],
      });
      folderTree.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  function handleOk() {
    visible.value = false;
    openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, {
      openAi: 'Y',
      id: selectedKeys.value[0],
    });
    return true;
  }

  function handleCancel() {
    visible.value = false;
    selectedKeys.value = [];
    moduleKeyword.value = '';
  }

  watch(
    () => visible.value,
    async (newVal) => {
      if (newVal) {
        await initProtocolList();
        await initModules();
        initModuleCount({
          keyword: '',
          protocols: allProtocolList.value,
          projectId: appStore.currentProjectId,
          moduleIds: [],
        });
      }
    }
  );
</script>

<style lang="less" scoped></style>
