<template>
  <div class="flex flex-1 flex-col overflow-hidden">
    <keep-alive :include="cacheStore.cacheViews">
      <MsCacheWrapper
        v-if="activeApiTab.id === 'all' && currentTab === 'case'"
        class="flex-1 overflow-hidden"
        :cache-name="CacheTabTypeEnum.API_TEST_CASE_TABLE"
      >
        <caseTable
          ref="caseTableRef"
          key="API_TEST_CASE_TABLE"
          :offspring-ids="props.offspringIds"
          :is-api="false"
          :active-module="props.activeModule"
          :selected-protocols="props.selectedProtocols"
          @open-case-tab="openCaseTab"
          @open-case-tab-and-execute="openCaseTabAndExecute"
          @handle-adv-search="(val) => emit('handleAdvSearch', val)"
        />
      </MsCacheWrapper>
    </keep-alive>
    <div v-if="activeApiTab.id !== 'all'" class="flex-1 overflow-hidden">
      <caseDetail
        v-model:execute-case="caseExecute"
        :detail="activeApiTab"
        :module-tree="props.moduleTree"
        @delete-case="deleteCase"
        @update-follow="activeApiTab.follow = !activeApiTab.follow"
        @load-case="(id: string) => openOrUpdateCaseTab(false, id)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import { cloneDeep } from 'lodash-es';

  import MsCacheWrapper from '@/components/pure/ms-cache-wrapper/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import caseTable from './caseTable.vue';

  import { getCaseDetail } from '@/api/modules/api-test/management';
  import useCacheStore from '@/store/modules/cache/cache';

  import { ApiCaseDetail } from '@/models/apiTest/management';
  import { ModuleTreeNode } from '@/models/common';
  import { CacheTabTypeEnum } from '@/enums/cacheTabEnum';

  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import { parseRequestBodyFiles } from '@/views/api-test/components/utils';

  // 非首屏渲染的大量内容的组件异步导入
  const caseDetail = defineAsyncComponent(() => import('./caseDetail.vue'));
  const cacheStore = useCacheStore();

  const props = defineProps<{
    activeModule: string;
    selectedProtocols: string[];
    offspringIds: string[];
    moduleTree: ModuleTreeNode[]; // 模块树
    currentTab: string;
  }>();
  const emit = defineEmits<{
    (e: 'deleteCase', id: string): void;
    (e: 'handleAdvSearch', isStartAdvance: boolean): void;
  }>();

  const route = useRoute();

  const apiTabs = defineModel<RequestParam[]>('apiTabs', {
    required: true,
  });

  const activeApiTab = defineModel<RequestParam>('activeApiTab', {
    required: true,
  });

  const defaultCaseParams = inject<RequestParam>('defaultCaseParams');

  const caseExecute = ref(false);

  const loading = ref(false);
  async function openOrUpdateCaseTab(isOpen: boolean, id: string) {
    try {
      loading.value = true;
      const res = await getCaseDetail(id);
      let parseRequestBodyResult;
      if (res.protocol === 'HTTP') {
        parseRequestBodyResult = parseRequestBodyFiles(res.request.body); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件
      }
      const tabItemInfo = {
        ...cloneDeep(defaultCaseParams as RequestParam),
        ...({
          ...res.request,
          ...res,
          url: res.path,
          ...parseRequestBodyResult,
        } as Partial<TabItem>),
      };
      if (isOpen) {
        apiTabs.value.push(tabItemInfo);
        activeApiTab.value = apiTabs.value[apiTabs.value.length - 1];
      } else {
        // 更新数据
        const index = apiTabs.value.findIndex((item) => item.id === id);
        apiTabs.value[index] = cloneDeep(tabItemInfo);
        activeApiTab.value = tabItemInfo;
      }

      nextTick(() => {
        loading.value = false; // 等待内容渲染出来再隐藏loading
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      loading.value = false;
    }
  }

  async function openCaseTab(apiInfo: ApiCaseDetail | string) {
    caseExecute.value = false;
    const isLoadedTabIndex = apiTabs.value.findIndex(
      (e) => e.id === (typeof apiInfo === 'string' ? apiInfo : apiInfo.id)
    );
    if (isLoadedTabIndex > -1) {
      // 如果点击的请求在tab中已经存在，则直接切换到该tab
      activeApiTab.value = apiTabs.value[isLoadedTabIndex] as RequestParam;
      return;
    }
    await openOrUpdateCaseTab(true, typeof apiInfo === 'string' ? apiInfo : apiInfo.id);
  }

  async function openCaseTabAndExecute(apiInfo: ApiCaseDetail | string) {
    caseExecute.value = true;
    const isLoadedTabIndex = apiTabs.value.findIndex(
      (e) => e.id === (typeof apiInfo === 'string' ? apiInfo : apiInfo.id)
    );
    if (isLoadedTabIndex > -1) {
      // 如果点击的请求在tab中已经存在，则直接切换到该tab
      activeApiTab.value = apiTabs.value[isLoadedTabIndex] as RequestParam;
      return;
    }
    await openOrUpdateCaseTab(true, typeof apiInfo === 'string' ? apiInfo : apiInfo.id);
  }

  const caseTableRef = ref<InstanceType<typeof caseTable>>();
  function deleteCase(id: string) {
    emit('deleteCase', id);
    caseTableRef.value?.loadCaseList();
  }

  onBeforeMount(() => {
    if (route.query.tab === 'case' && route.query.id) {
      openCaseTab(route.query.id as string);
    }
  });

  const isAdvancedSearchMode = computed(() => caseTableRef.value?.isAdvancedSearchMode);
  defineExpose({
    openCaseTab,
    openCaseTabAndExecute,
    isAdvancedSearchMode,
  });
</script>
