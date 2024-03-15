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
    <div v-show="activeApiTab.id !== 'all'" class="flex-1 overflow-hidden">
      <caseDetail :active-api-tab="activeApiTab" :module-tree="props.moduleTree" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { cloneDeep } from 'lodash-es';

  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import caseDetail from './caseDetail.vue';
  import caseTable from './caseTable.vue';

  import { getCaseDetail } from '@/api/modules/api-test/management';

  import { ApiCaseDetail } from '@/models/apiTest/management';
  import { ModuleTreeNode } from '@/models/common';
  import { RequestAuthType, RequestComposition, RequestMethods, ResponseComposition } from '@/enums/apiEnum';

  import { defaultBodyParams, defaultResponse, defaultResponseItem } from '@/views/api-test/components/config';
  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import { parseRequestBodyFiles } from '@/views/api-test/components/utils';

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

  const initDefaultId = `case-${Date.now()}`;
  const defaultCaseParams: RequestParam = {
    id: initDefaultId,
    moduleId: props.activeModule === 'all' ? 'root' : props.activeModule,
    protocol: 'HTTP',
    tags: [],
    description: '',
    url: '',
    activeTab: RequestComposition.HEADER,
    closable: true,
    method: RequestMethods.GET,
    headers: [],
    body: cloneDeep(defaultBodyParams),
    query: [],
    rest: [],
    polymorphicName: '',
    name: '',
    path: '',
    projectId: '',
    uploadFileIds: [],
    linkFileIds: [],
    authConfig: {
      authType: RequestAuthType.NONE,
      basicAuth: {
        userName: '',
        password: '',
      },
      digestAuth: {
        userName: '',
        password: '',
      },
    },
    children: [
      {
        polymorphicName: 'MsCommonElement', // 协议多态名称，写死MsCommonElement
        assertionConfig: {
          enableGlobal: false,
          assertions: [],
        },
        postProcessorConfig: {
          enableGlobal: false,
          processors: [],
        },
        preProcessorConfig: {
          enableGlobal: false,
          processors: [],
        },
      },
    ],
    otherConfig: {
      connectTimeout: 60000,
      responseTimeout: 60000,
      certificateAlias: '',
      followRedirects: true,
      autoRedirects: false,
    },
    responseActiveTab: ResponseComposition.BODY,
    response: cloneDeep(defaultResponse),
    responseDefinition: [cloneDeep(defaultResponseItem)],
    isNew: true,
    mode: 'case',
    executeLoading: false,
    preDependency: [], // 前置依赖
    postDependency: [], // 后置依赖
  };

  function addTab(defaultProps?: Partial<TabItem>) {
    apiTabs.value.push({
      ...cloneDeep(defaultCaseParams),
      ...defaultProps,
    });
    activeApiTab.value = apiTabs.value[apiTabs.value.length - 1];
  }

  const loading = ref(false);
  async function openCaseTab(apiInfo: ApiCaseDetail) {
    const isLoadedTabIndex = apiTabs.value.findIndex(
      (e) => e.id === (typeof apiInfo === 'string' ? apiInfo : apiInfo.id)
    );
    if (isLoadedTabIndex > -1) {
      // 如果点击的请求在tab中已经存在，则直接切换到该tab
      activeApiTab.value = apiTabs.value[isLoadedTabIndex] as RequestParam;
      return;
    }
    try {
      loading.value = true;
      const res = await getCaseDetail(typeof apiInfo === 'string' ? apiInfo : apiInfo.id);
      const parseRequestBodyResult = parseRequestBodyFiles(res.request.body); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件;
      // if (res.protocol === 'HTTP') { // TODO: 后端没protocol字段，问一下
      // parseRequestBodyResult = parseRequestBodyFiles(res.request.body); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件
      // }
      addTab({
        ...res.request,
        ...res,
        response: cloneDeep(defaultResponse),
        // responseDefinition: res.response.map((e) => ({ ...e, responseActiveTab: ResponseComposition.BODY })), // TODO: 后端没response字段，问一下
        url: res.path,
        ...parseRequestBodyResult,
      });
      nextTick(() => {
        loading.value = false; // 等待内容渲染出来再隐藏loading
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      loading.value = false;
    }
  }
</script>
