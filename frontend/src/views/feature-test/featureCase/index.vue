<template>
  <div class="mb-[16px]">
    <a-button type="primary" class="mr-[12px]"> {{ t('featureTest.featureCase.creatingCase') }} </a-button>
    <a-button type="outline"> {{ t('featureTest.featureCase.importCase') }} </a-button>
  </div>
  <div class="pageWrap">
    <MsSplitBox>
      <template #left>
        <div class="p-[24px]">
          <div class="feature-case">
            <div class="case h-[38px]">
              <div class="flex items-center" :class="getActiveClass('public')" @click="selectActive('public')">
                <MsIcon type="icon-icon_folder_outlined-1" class="folder-icon" />
                <div class="folder-name mx-[4px]">{{ t('featureTest.featureCase.publicCase') }}</div>
                <div class="folder-count">({{ publicCaseCount }})</div></div
              >
              <div class="back"><icon-arrow-right /></div>
            </div>
            <a-divider class="my-[8px]" />
            <a-input-search class="mb-4" :placeholder="t('featureTest.featureCase.searchTip')" />
            <div class="case h-[38px]">
              <div class="flex items-center" :class="getActiveClass('all')" @click="selectActive('all')">
                <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
                <div class="folder-name mx-[4px]">{{ t('featureTest.featureCase.allCase') }}</div>
                <div class="folder-count">(100)</div></div
              >
              <div class="ml-auto flex items-center">
                <a-tooltip
                  :content="
                    isExpandAll ? t('project.fileManagement.collapseAll') : t('project.fileManagement.expandAll')
                  "
                >
                  <MsButton type="icon" status="secondary" class="!mr-0 p-[4px]" @click="expandHandler">
                    <MsIcon :type="isExpandAll ? 'icon-icon_folder_collapse1' : 'icon-icon_folder_expansion1'" />
                  </MsButton>
                </a-tooltip>
                <ActionPopConfirm
                  operation-type="add"
                  :title="t('featureTest.featureCase.addSubModule')"
                  :all-names="[]"
                >
                  <MsButton type="icon" class="!mr-0 p-[2px]">
                    <MsIcon
                      type="icon-icon_create_planarity"
                      size="18"
                      class="text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
                    />
                  </MsButton>
                </ActionPopConfirm>
              </div>
            </div>
            <a-divider class="my-[8px]" />
            <FeatureCaseTree
              v-model:selected-keys="selectedKeys"
              :is-expand-all="isExpandAll"
              @case-node-select="caseNodeSelect"
            ></FeatureCaseTree>
          </div>
        </div>
      </template>
      <template #right>
        <div class="p-[24px]">
          <CaseTable></CaseTable>
        </div>
      </template>
    </MsSplitBox>
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 功能测试-功能用例
   */
  import { computed, ref } from 'vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import FeatureCaseTree from './components/featureCaseTree.vue';
  import ActionPopConfirm from './components/actionPopConfirm.vue';
  import CaseTable from './components/caseTable.vue';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const isExpandAll = ref(false);

  const activeCase = ref<string | number>('public'); // 激活用例

  const publicCaseCount = ref<number>(100); // 公共用例数量

  // 设置当前激活用例类型
  const selectActive = (type: string) => {
    activeCase.value = type;
  };

  // 获取激活用例样式
  const getActiveClass = (type: string) => {
    return activeCase.value === type ? 'folder-text case-active' : 'folder-text';
  };

  const expandHandler = () => {
    isExpandAll.value = !isExpandAll.value;
  };

  // 选中节点
  const selectedKeys = computed({
    get: () => [activeCase.value],
    set: (val) => val,
  });

  // 处理用例树节点选中
  function caseNodeSelect(keys: (string | number)[]) {
    [activeCase.value] = keys;
  }
</script>

<style scoped lang="less">
  .pageWrap {
    min-width: 1000px;
    height: calc(100vh - 136px);
    border-radius: var(--border-radius-large);
    @apply bg-white;
    .case {
      @apply flex cursor-pointer  items-center justify-between;
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
</style>
