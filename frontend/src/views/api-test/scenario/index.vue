<template>
  <div class="rounded-2xl bg-white">
    <div class="p-[24px] pb-[16px]">
      <span>场景列表接口(标签页配置未实现)</span>
    </div>
    <a-divider class="!my-0" />
    <div class="pageWrap">
      <MsSplitBox :size="300" :max="0.5">
        <template #first>
          <div class="p-[24px] pb-0">
            <div class="feature-case h-[100%]">
              <scenarioModuleTree
                ref="scenarioModuleTreeRef"
                :is-show-scenario="isShowScenario"
                @folder-node-select="handleNodeSelect"
                @init="handleModuleInit"
              ></scenarioModuleTree>
              <div class="b-0 absolute w-[88%]">
                <a-divider class="!my-0 !mb-2" />
                <div class="case h-[38px]">
                  <div class="flex items-center" :class="getActiveClass('recycle')" @click="redirectRecycle()">
                    <MsIcon type="icon-icon_delete-trash_outlined" class="folder-icon" />
                    <div class="folder-name mx-[4px]">{{ t('apiScenario.tree.recycleBin') }}</div>
                    <!--                    <div class="folder-count">({{ recycleModulesCount.all || 0 }})</div></div-->
                    <div class="folder-count">({{ 0 }})</div></div
                  >
                </div>
              </div>
            </div>
          </div>
        </template>
        <template #second>
          <div class="p-[24px]">
            <!--            <apiTable ref="apiTableRef" :active-module="activeFolder" :offspring-ids="offspringIds" />-->
          </div>
        </template>
      </MsSplitBox>
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 接口测试-接口场景主页
   */

  import { ref } from 'vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import scenarioModuleTree from './components/scenarioModuleTree.vue';
  import ApiTable from '@/views/api-test/management/components/management/api/apiTable.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ModuleTreeNode } from '@/models/common';

  const { t } = useI18n();
  const folderTree = ref<ModuleTreeNode[]>([]);
  const folderTreePathMap = ref<Record<string, any>>({});
  const activeFolder = ref<string>('all');
  const activeModule = ref<string>('all');
  const offspringIds = ref<string[]>([]);

  const isShowScenario = ref(false);

  // 获取激活用例类型样式
  const getActiveClass = (type: string) => {
    return activeFolder.value === type ? 'folder-text case-active' : 'folder-text';
  };

  const scenarioModuleTreeRef = ref();

  function handleModuleInit(tree, _protocol: string, pathMap: Record<string, any>) {
    folderTree.value = tree;
    folderTreePathMap.value = pathMap;
  }

  function handleNodeSelect(keys: string[], _offspringIds: string[]) {
    [activeModule.value] = keys;
    offspringIds.value = _offspringIds;
  }

  function redirectRecycle() {}
</script>

<style scoped lang="less">
  .pageWrap {
    min-width: 1000px;
    height: calc(100vh - 166px);
    border-radius: var(--border-radius-large);
    @apply bg-white;
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
    @apply absolute bottom-0 bg-white  pb-4;
    :deep(.arco-divider-horizontal) {
      margin: 8px 0;
    }
    .recycle-bin {
      @apply bottom-0 flex items-center bg-white;
      .recycle-count {
        margin-left: 4px;
        color: var(--color-text-4);
      }
    }
  }
</style>
