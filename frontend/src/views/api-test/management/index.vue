<template>
  <MsCard simple no-content-padding>
    <MsSplitBox :size="0.25" :max="0.5">
      <template #first>
        <div class="p-[24px]">
          <moduleTree
            @init="(val) => (folderTree = val)"
            @new-api="newApi"
            @change="(val) => (activeModule = val)"
            @import="importDrawerVisible = true"
            @folder-node-select="(keys, _offspringIds) => (offspringIds = _offspringIds)"
          />
        </div>
        <!-- <div class="b-0 absolute w-[88%]">
                <a-divider class="!my-0 !mb-2" />
                <div class="case h-[38px]">
                  <div class="flex items-center" :class="getActiveClass('recycle')" @click="setActiveFolder('recycle')">
                    <MsIcon type="icon-icon_delete-trash_outlined" class="folder-icon" />
                    <div class="folder-name mx-[4px]">{{ t('caseManagement.featureCase.recycle') }}</div>
                    <div class="folder-count">({{ recycleModulesCount.all || 0 }})</div></div
                  >
                </div>
              </div> -->
      </template>
      <template #second>
        <div class="relative flex h-full flex-col">
          <div
            id="managementContainer"
            :class="['absolute z-[101] h-full w-full', importDrawerVisible ? '' : 'invisible']"
            style="transition: all 0.3s"
          >
            <importApi
              v-model:visible="importDrawerVisible"
              :module-tree="folderTree"
              popup-container="#managementContainer"
            />
          </div>
          <management
            :module="activeModule"
            :all-count="allCount"
            :active-module="activeModule"
            :offspring-ids="offspringIds"
          />
        </div>
      </template>
    </MsSplitBox>
  </MsCard>
</template>

<script lang="ts" setup>
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import importApi from './components/import.vue';
  import management from './components/management/index.vue';
  import moduleTree from './components/moduleTree.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ModuleTreeNode } from '@/models/projectManagement/file';

  const { t } = useI18n();

  const activeModule = ref<string>('all');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const allCount = ref(0);
  const importDrawerVisible = ref(false);
  const offspringIds = ref<string[]>([]);

  function newApi() {
    // debugRef.value?.addDebugTab();
  }
</script>

<style lang="less" scoped></style>
