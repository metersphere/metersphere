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
                @init="handleModuleInit"
              ></scenarioModuleTree>
              <div class="b-0 absolute w-[88%]">
                <a-divider class="!my-0 !mb-2" />
                <div class="case h-[38px]">
                  <div class="flex items-center" :class="getActiveClass('recycle')" @click="setActiveFolder('recycle')">
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
            <!--            <CaseTable-->
            <!--              :active-folder="activeFolder"-->
            <!--              :offspring-ids="offspringIds"-->
            <!--              :active-folder-type="activeCaseType"-->
            <!--              :modules-count="modulesCount"-->
            <!--              @init="initModulesCount"-->
            <!--              @import="importCase"-->
            <!--            ></CaseTable>-->

            <!--            <management-->
            <!--                ref="managementRef"-->
            <!--                :module-tree="folderTree"-->
            <!--                :active-module="activeModule"-->
            <!--                :offspring-ids="offspringIds"-->
            <!--                :protocol="protocol"-->
            <!--            />-->
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

  import { computed, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import scenarioModuleTree from './components/scenarioModuleTree.vue';

  import { useI18n } from '@/hooks/useI18n';
  import router from '@/router';

  import { ModuleTreeNode } from '@/models/common';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';

  const { t } = useI18n();
  const folderTree = ref<ModuleTreeNode[]>([]);
  const folderTreePathMap = ref<Record<string, any>>({});
  const activeFolder = ref<string>('all');

  const addSubVisible = ref(false);

  const isShowScenario = ref(false);

  // 获取激活用例类型样式
  const getActiveClass = (type: string) => {
    return activeFolder.value === type ? 'folder-text case-active' : 'folder-text';
  };

  const modulesCount = computed(() => {
    // featureCaseStore.modulesCount
    return { all: 0 };
  });

  //  全部展开
  const isExpandAll = ref(false);

  // 全部展开或折叠
  const expandHandler = () => {
    isExpandAll.value = !isExpandAll.value;
  };

  /**
   * 设置根模块名称列表
   * @param names 根模块名称列表
   */
  const rootModulesName = ref<string[]>([]); // 根模块名称列表
  function setRootModules(names: string[]) {
    rootModulesName.value = names;
  }

  const confirmLoading = ref(false);
  const confirmRef = ref();
  const scenarioModuleTreeRef = ref();
  // 添加子模块
  const confirmHandler = async () => {
    try {
      confirmLoading.value = true;
      const { field } = confirmRef.value.form;
      if (!confirmRef.value.isPass) {
        return;
      }
      // const params: CreateOrUpdateModule = {
      //   projectId: currentProjectId.value,
      //   name: field,
      //   parentId: 'NONE',
      // };
      // await createCaseModuleTree(params);
      Message.success(t('caseManagement.featureCase.addSuccess'));
      scenarioModuleTreeRef.value.initModules();
      addSubVisible.value = false;
    } catch (error) {
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  };

  // 激活节点类型
  const activeNodeType = ref<'folder' | 'module'>('folder');

  // 设置当前激活用例类型公共用例|全部用例|回收站
  const setActiveFolder = (type: string) => {
    activeFolder.value = type;
    if (['public', 'all', 'recycle'].includes(type)) {
      activeNodeType.value = 'folder';
    }
    if (type === 'recycle') {
      router.push({
        name: ApiTestRouteEnum.API_TEST_SCENARIO_RECYCLE,
      });
    }
  };

  function handleModuleInit(tree, _protocol: string, pathMap: Record<string, any>) {
    folderTree.value = tree;
    folderTreePathMap.value = pathMap;
  }
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
