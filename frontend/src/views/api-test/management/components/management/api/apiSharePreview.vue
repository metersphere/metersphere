<template>
  <div class="h-[calc(100%-64px)]">
    <ApiPreview
      :detail="activeApiDetail"
      :protocols="props.selectedProtocols"
      @update-follow="activeApiDetail.follow = !activeApiDetail.follow"
      @export-share="handlerExportShare"
    />
  </div>
  <div class="doc-toggle-footer">
    <div v-if="props?.previousNode" class="doc-toggle cursor-pointer" @click="toggleApiDetail('prev')">
      <MsIcon
        type="icon-icon_pull-left_outlined"
        :class="` text-[var(--color-text-4)] ${props.previousNode ? 'hover:text-[rgb(var(--primary-5))]' : ''}`"
        :size="16"
      />
      <apiMethodName
        :method="
          props?.previousNode?.attachInfo.protocol === 'HTTP'
            ? props.previousNode?.attachInfo.method ?? ''
            : props.previousNode?.attachInfo.protocol ?? ''
        "
        class="mr-[4px]"
      />

      <a-tooltip :content="`${props.previousNode?.name}`" position="tl" :disabled="!props.previousNode?.name">
        <div class="doc-toggle-name one-line-text">
          {{ props.previousNode?.name }}
        </div>
      </a-tooltip>
    </div>
    <div v-else :class="`doc-toggle ${props?.previousNode ? 'cursor-pointer' : ''}`"></div>
    <div v-if="props?.nextNode" class="doc-toggle cursor-pointer justify-end" @click="toggleApiDetail('next')">
      <apiMethodName
        :method="
          props?.nextNode?.attachInfo.protocol === 'HTTP'
            ? props.nextNode?.attachInfo.method ?? ''
            : props.nextNode?.attachInfo.protocol ?? ''
        "
        class="mr-[4px]"
      />
      <a-tooltip :content="`${props?.nextNode?.name}`" position="tr">
        <div class="doc-toggle-name one-line-text">
          {{ props?.nextNode?.name }}
        </div>
      </a-tooltip>

      <MsIcon
        type="icon-icon_pull-right_outlined"
        :class="` text-[var(--color-text-4)] ${props?.nextNode ? 'hover:text-[rgb(var(--primary-5))]' : ''}`"
        :size="16"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { cloneDeep } from 'lodash-es';

  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import ApiPreview from '@/views/api-test/management/components/management/api/preview/index.vue';

  import { getShareDefinitionDetail } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ProtocolItem } from '@/models/apiTest/common';
  import { ModuleTreeNode } from '@/models/common';
  import {
    RequestAuthType,
    RequestComposition,
    RequestDefinitionStatus,
    RequestMethods,
    ResponseComposition,
  } from '@/enums/apiEnum';

  import { defaultBodyParams, defaultResponse, defaultResponseItem } from '@/views/api-test/components/config';
  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import { parseRequestBodyFiles } from '@/views/api-test/components/utils';

  const appStore = useAppStore();
  const { t } = useI18n();

  const props = defineProps<{
    apiInfo?: ModuleTreeNode | null;
    previousNode?: ModuleTreeNode | null;
    nextNode?: ModuleTreeNode | null;
    selectedProtocols: ProtocolItem[];
  }>();

  const emit = defineEmits<{
    (e: 'toggleDetail', type: string): void;
    (e: 'exportShare', all: boolean): void;
  }>();

  const initDefaultId = `definition-${Date.now()}`;
  const defaultDefinitionParams: RequestParam = {
    type: 'api',
    definitionActiveKey: 'definition',
    id: initDefaultId,
    moduleId: '',
    protocol: 'HTTP',
    tags: [],
    status: RequestDefinitionStatus.PROCESSING,
    description: '',
    url: '',
    activeTab: RequestComposition.HEADER,
    label: t('apiTestDebug.newApi'),
    closable: true,
    method: RequestMethods.GET,
    unSaved: false,
    headers: [],
    body: cloneDeep(defaultBodyParams),
    query: [],
    rest: [],
    polymorphicName: '',
    name: '',
    num: '',
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
          enableGlobal: true,
          assertions: [],
        },
        postProcessorConfig: {
          enableGlobal: true,
          processors: [],
        },
        preProcessorConfig: {
          enableGlobal: true,
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
    mode: 'definition',
    executeLoading: false,
    preDependency: [], // 前置依赖
    postDependency: [], // 后置依赖
    errorMessageInfo: {},
  };

  const loading = ref(false);

  const activeApiDetail = ref<RequestParam>(cloneDeep(defaultDefinitionParams));

  async function initDetail() {
    if (props.apiInfo && props.apiInfo.id) {
      try {
        appStore.showLoading();
        loading.value = true;
        const res = await getShareDefinitionDetail(props.apiInfo.id);
        appStore.hideLoading();
        let parseRequestBodyResult;
        if (res.protocol === 'HTTP') {
          parseRequestBodyResult = parseRequestBodyFiles(res.request.body, res.response); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件
        }
        const { request } = res;
        const defaultProps: Partial<TabItem> = {
          label: res.name,
          ...res,
          ...request,
          name: res.name || '-',
          num: res.num || '-',
          response: cloneDeep(defaultResponse),
          responseDefinition: res.response.map((e) => ({ ...e, responseActiveTab: ResponseComposition.BODY })),
          url: res.path,
          definitionActiveKey: 'preview',
          ...parseRequestBodyResult,
        };
        activeApiDetail.value = {
          ...cloneDeep(defaultDefinitionParams),
          ...defaultProps,
        };
        nextTick(() => {
          loading.value = false;
        });
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
        loading.value = false;
        appStore.hideLoading();
      }
    }
  }

  // 切换上一条&下一条
  function toggleApiDetail(type: string) {
    emit('toggleDetail', type);
  }
  // 导出分享
  function handlerExportShare() {
    emit('exportShare', false);
  }

  watch(
    () => props.apiInfo,
    (val) => {
      if (val) {
        activeApiDetail.value.protocol = val.attachInfo.protocol;
        initDetail();
      }
    },
    { deep: true }
  );
</script>

<style scoped lang="less">
  .doc-toggle-footer {
    position: absolute;
    bottom: 0;
    z-index: 99;
    padding: 16px;

    @apply flex w-full items-center justify-between;

    background-color: var(--color-text-fff);
    .doc-toggle {
      max-width: 50%;
      @apply flex flex-1 items-center gap-2;
      .doc-toggle-name {
        max-width: 300px;
      }
    }
  }
</style>
