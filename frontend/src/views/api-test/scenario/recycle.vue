<template>
  <MsCard has-breadcrumb no-content-padding simple>
    <MsSplitBox :size="300" :max="0.5">
      <template #first>
        <div class="h-full p-[16px]">
          <recycleTree
            ref="recycleTreeRef"
            :is-show-scenario="isShowScenario"
            @folder-node-select="handleNodeSelect"
            @init="handleModuleInit"
          ></recycleTree>
        </div>
      </template>
      <template #second>
        <div class="p-[24px_24px_8px_24px]">
          <MsEditableTab
            v-model:active-tab="activeApiTab"
            v-model:tabs="apiTabs"
            class="flex-1 overflow-hidden"
            :show-add="false"
            :readonly="true"
            @add="newTab"
          >
            <template #label="{ tab }">
              <a-tooltip :content="tab.label" :mouse-enter-delay="500">
                <div class="one-line-text max-w-[144px]">
                  {{ tab.label }}
                </div>
              </a-tooltip>
            </template>
          </MsEditableTab>
        </div>
        <a-divider class="!my-0" />
        <div v-if="activeApiTab.id === 'all'" class="pageWrap">
          <RecycleTable
            ref="apiTableRef"
            :active-module="activeModule"
            :offspring-ids="offspringIds"
            @refresh-module-tree="refreshTree"
          />
        </div>
      </template>
    </MsSplitBox>
    <!-- <detail v-else v-model:scenario="activeApiTab"></detail> -->
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 接口测试-接口场景回收站
   */

  import { ref } from 'vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  // import detail from './detail/index.vue';
  import RecycleTable from '@/views/api-test/scenario/recycle/recycleTable.vue';
  import recycleTree from '@/views/api-test/scenario/recycle/recycleTree.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ApiScenarioGetModuleParams } from '@/models/apiTest/scenario';
  import { ModuleTreeNode } from '@/models/common';

  const { t } = useI18n();

  const apiTabs = ref<any[]>([
    {
      id: 'all',
      label: t('api_scenario.recycle.list'),
      closable: false,
    },
  ]);
  const activeApiTab = ref<any>(apiTabs.value[0]);

  function newTab() {
    apiTabs.value.push({
      id: `newTab${apiTabs.value.length}`,
      label: `New Tab ${apiTabs.value.length}`,
      closable: true,
    });
    activeApiTab.value = apiTabs.value[apiTabs.value.length - 1];
  }

  const folderTree = ref<ModuleTreeNode[]>([]);
  const folderTreePathMap = ref<Record<string, any>>({});
  const activeModule = ref<string>('all');
  const activeFolder = ref<string>('all');
  const offspringIds = ref<string[]>([]);
  const isShowScenario = ref(false);

  const recycleTreeRef = ref<InstanceType<typeof recycleTree>>();

  function handleModuleInit(tree: any, _protocol: string, pathMap: Record<string, any>) {
    folderTree.value = tree;
    folderTreePathMap.value = pathMap;
  }

  function handleNodeSelect(keys: string[], _offspringIds: string[]) {
    [activeModule.value] = keys;
    offspringIds.value = _offspringIds;
  }

  function refreshTree(params: ApiScenarioGetModuleParams) {
    recycleTreeRef.value?.initModuleCount(params);
  }
</script>

<style scoped lang="less">
  .pageWrap {
    height: calc(100% - 65px);
    border-radius: var(--border-radius-large);
    background-color: var(--color-text-fff);
    .case {
      padding: 8px 4px;
      border-radius: var(--border-radius-small);
      @apply flex cursor-pointer  items-center justify-between;
      &:hover {
        background-color: rgb(var(--primary-1));
      }
      .folder-icon {
        margin-right: 4px;
        color: var(--color-text-4);
      }
      .folder-name {
        color: var(--color-text-1);
      }
      .folder-count {
        margin-left: 4px;
        color: var(--color-text-4);
      }
      .case-active {
        .folder-icon,
        .folder-name,
        .folder-count {
          color: rgb(var(--primary-5));
        }
      }
      .back {
        margin-right: 8px;
        width: 20px;
        height: 20px;
        border: 1px solid #ffffff;
        background: linear-gradient(90deg, rgb(var(--primary-9)) 3.36%, #ffffff 100%);
        box-shadow: 0 0 7px rgb(15 0 78 / 9%);
        .arco-icon {
          color: rgb(var(--primary-5));
        }
        @apply flex cursor-pointer items-center rounded-full;
      }
    }
  }
  .recycle {
    @apply absolute bottom-0   pb-4;

    background-color: var(--color-text-fff);
    :deep(.arco-divider-horizontal) {
      margin: 8px 0;
    }
    .recycle-bin {
      @apply bottom-0 flex items-center;

      background-color: var(--color-text-fff);
      .recycle-count {
        margin-left: 4px;
        color: var(--color-text-4);
      }
    }
  }
</style>
