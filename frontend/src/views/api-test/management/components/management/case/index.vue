<template>
  <div class="flex flex-1 flex-col overflow-hidden">
    <div v-show="activeApiTab.id === 'all'" class="flex-1 overflow-hidden">
      <caseTable
        :is-api="false"
        :active-module="props.activeModule"
        :protocol="props.protocol"
        @open-case-tab="openCaseTab"
      />
    </div>
    <div v-if="activeApiTab.id !== 'all'" class="flex-1 overflow-hidden">
      <caseDetail
        :detail="activeApiTab"
        :module-tree="props.moduleTree"
        :protocol="props.protocol"
        @update-follow="activeApiTab.follow = !activeApiTab.follow"
        @load-case="(id: string) => openOrUpdateCaseTab(false, id)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { cloneDeep } from 'lodash-es';

  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import caseTable from './caseTable.vue';

  import { getCaseDetail } from '@/api/modules/api-test/management';

  import { ApiCaseDetail } from '@/models/apiTest/management';
  import { ModuleTreeNode } from '@/models/common';

  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import { parseRequestBodyFiles } from '@/views/api-test/components/utils';

  // 非首屏渲染的大量内容的组件异步导入
  const caseDetail = defineAsyncComponent(() => import('./caseDetail.vue'));

  const props = defineProps<{
    activeModule: string;
    protocol: string;
    moduleTree: ModuleTreeNode[]; // 模块树
  }>();

  const apiTabs = defineModel<RequestParam[]>('apiTabs', {
    required: true,
  });

  const activeApiTab = defineModel<RequestParam>('activeApiTab', {
    required: true,
  });

  const defaultCaseParams = inject<RequestParam>('defaultCaseParams');

  const loading = ref(false);
  async function openOrUpdateCaseTab(isOpen: boolean, id: string) {
    try {
      loading.value = true;
      const res = await getCaseDetail(id);
      const parseRequestBodyResult = parseRequestBodyFiles(res.request.body); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件;
      // if (res.protocol === 'HTTP') { // TODO: 后端没protocol字段，问一下
      // parseRequestBodyResult = parseRequestBodyFiles(res.request.body); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件
      // }
      const tabItemInfo = {
        ...cloneDeep(defaultCaseParams as RequestParam),
        ...({
          ...res.request,
          ...res,
          // responseDefinition: res.response.map((e) => ({ ...e, responseActiveTab: ResponseComposition.BODY })), // TODO: 后端没response字段，问一下
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
        apiTabs.value[index] = tabItemInfo;
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

  async function openCaseTab(apiInfo: ApiCaseDetail) {
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
</script>
