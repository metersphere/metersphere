<template>
  <MsDrawer
    v-model:visible="showDiffVisible"
    :title="t('case.apiAndCaseDiff')"
    width="100%"
    class="diff-modal"
    :footer="false"
    no-content-padding
    unmount-on-close
    :closable="false"
  >
    <template #title>
      <div class="flex w-full items-center justify-between">
        <div>{{ t('case.apiAndCaseDiff') }}</div>

        <div class="flex items-center text-[14px]">
          <div v-if="hasAnyPermission(['PROJECT_API_DEFINITION_CASE:READ+UPDATE'])" class="flex items-center">
            <div v-if="showSyncConfig" class="-mt-[2px] mr-[8px]"> {{ t('case.syncItem') }}</div>
            <a-checkbox-group v-if="showSyncConfig" v-model="checkType">
              <a-checkbox v-for="item of checkList" :key="item.value" :value="item.value">
                <div class="flex items-center">
                  {{ item.label }}
                  <a-tooltip v-if="item.tooltip" :content="item.tooltip" position="top">
                    <div class="flex items-center">
                      <icon-question-circle
                        class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                        size="16"
                      />
                    </div>
                  </a-tooltip>
                </div>
              </a-checkbox>
            </a-checkbox-group>
            <a-divider v-if="showSyncConfig" direction="vertical" :margin="0" class="!ml-[8px]" />
          </div>
          <a-button class="mx-[12px]" type="secondary" @click="cancel">{{ t('common.cancel') }}</a-button>
          <a-button
            v-if="showSyncConfig && hasAnyPermission(['PROJECT_API_DEFINITION_CASE:READ+UPDATE'])"
            class="mr-[12px]"
            type="outline"
            @click="clearThisChangeHandler"
          >
            {{ t('case.ignoreThisChange') }}
          </a-button>
          <a-button
            v-permission="['PROJECT_API_DEFINITION_CASE:READ+UPDATE']"
            type="primary"
            :loading="syncLoading"
            :disabled="!checkType.length"
            @click="confirmSync"
          >
            {{
              showSyncConfig && hasAnyPermission(['PROJECT_API_DEFINITION_CASE:READ+UPDATE'])
                ? t('case.apiSyncChange')
                : t('common.confirm')
            }}
          </a-button>
        </div>
      </div>
    </template>
    <!-- 图例 -->
    <div class="grid grid-cols-12 px-[16px]">
      <div class="col-span-5"></div>
      <div class="legend-container col-span-2">
        <div class="flex items-center">
          <div class="item mr-[8px]">
            <div class="legend add"></div>
            {{ t('case.diffAdd') }}
          </div>
          <div class="item">
            <div class="legend delete"></div>
            {{ t('common.delete') }}
          </div>
        </div>
      </div>
      <div class="col-span-5 flex items-center justify-end">
        <a-switch v-model:model-value="form.ignoreApiChange" :before-change="(val) => changeIgnore(val)" size="small" />
        <div class="ml-[8px]">{{ t('case.ignoreAllChange') }}</div>
        <a-divider v-if="showSyncConfig" direction="vertical" :margin="8"></a-divider>
        <a-switch v-if="showSyncConfig" v-model:model-value="form.deleteRedundantParam" size="small" />
        <div v-if="showSyncConfig" class="ml-[8px] font-normal text-[var(--color-text-1)]">
          {{ t('case.deleteNotCorrespondValue') }}
        </div>
      </div>
    </div>

    <!-- 对比 -->
    <div class="diff-container">
      <MsCard simple auto-height no-content-padding>
        <a-spin class="min-h-[calc(100vh-110px)] w-full p-4" :loading="loading">
          <div class="diff-normal">
            <div class="diff-item">
              <div class="flex">
                <a-tooltip
                  :content="`[${apiDetailInfo?.num}]${apiDetailInfo?.name}`"
                  :mouse-enter-delay="300"
                  position="br"
                >
                  <div class="title-type one-line-text"> [{{ apiDetailInfo?.num }}] {{ apiDetailInfo?.name }} </div>
                </a-tooltip>
              </div>

              <DiffItem
                :diff-distance-map="diffDistanceMap"
                mode="add"
                is-api
                :detail="apiDefinedRequest as RequestParam"
              />
            </div>
            <div class="diff-item">
              <div class="flex">
                <a-tooltip :content="`[${caseDetail?.num}] ${caseDetail?.name}`" :mouse-enter-delay="300" position="br">
                  <div class="title-type one-line-text"> [{{ caseDetail?.num }}] {{ caseDetail?.name }} </div>
                </a-tooltip>
              </div>
              <DiffItem :diff-distance-map="diffDistanceMap" mode="delete" :detail="caseDetail as RequestParam" />
            </div>
          </div>
          <DiffRequestBody
            :defined-detail="apiDefinedRequest as RequestParam"
            :case-detail="caseDetail as RequestParam"
          />
        </a-spin>
      </MsCard>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import DiffItem from './diffItem.vue';

  import {
    clearThisChange,
    diffDataRequest,
    getCaseDetail,
    getDefinitionDetail,
    getSyncedCaseDetail,
    ignoreEveryTimeChange,
  } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import { hasAnyPermission } from '@/utils/permission';

  import { EnableKeyValueParam, ExecuteRequestCommonParam } from '@/models/apiTest/common';
  import type { diffSyncParams, syncItem } from '@/models/apiTest/management';
  import { ApiDefinitionDetail } from '@/models/apiTest/management';
  import { RequestBodyFormat, RequestComposition } from '@/enums/apiEnum';

  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import { parseRequestBodyFiles } from '@/views/api-test/components/utils';

  const { t } = useI18n();

  const DiffRequestBody = defineAsyncComponent(() => import('./diffRequestBody.vue'));

  const props = defineProps<{
    activeApiCaseId: string;
    activeDefinedId: string;
  }>();

  const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'clearThisChange', isEvery: boolean): void;
    (e: 'sync', mergeRequest: RequestParam): void;
    (e: 'loadList'): void;
  }>();

  const showDiffVisible = defineModel<boolean>('visible', {
    required: true,
  });

  const checkList = ref([
    {
      value: RequestComposition.HEADER,
      label: t('apiTestDebug.header'),
    },
    {
      value: RequestComposition.BODY,
      label: t('apiTestDebug.body'),
      tooltip: t('case.onlySyncNewParamsOrValue'),
    },
    {
      value: RequestComposition.QUERY,
      label: RequestComposition.QUERY,
    },
    {
      value: RequestComposition.REST,
      label: RequestComposition.REST,
    },
  ]);

  const initForm = {
    deleteRedundantParam: false,
    syncItems: {
      header: false,
      body: false,
      query: false,
      rest: false,
    },
    ignoreApiChange: false,
  };

  const initCheckList = [
    RequestComposition.HEADER,
    RequestComposition.BODY,
    RequestComposition.QUERY,
    RequestComposition.REST,
  ];

  const checkType = ref([...initCheckList]);

  const form = ref({ ...initForm });

  const syncLoading = ref<boolean>(false);

  const defaultCaseParams = inject<RequestParam>('defaultCaseParams');
  const caseDetail = ref<Record<string, any>>({});

  const apiDetailInfo = ref<Record<string, any>>({});
  const apiDefinedRequest = ref<Record<string, any>>({});
  const syncCaseDetail = ref<Record<string, any>>({});
  const diffDistanceMap = ref<Record<string, any>>({});

  // 获取用例详情
  async function getCaseDetailInfo(id: string) {
    try {
      const res = await getCaseDetail(id);
      syncCaseDetail.value = res;
      const result = await diffDataRequest(id);
      const { caseRequest, apiRequest } = result;
      caseDetail.value = {
        ...caseDetail.value,
        ...caseRequest,
        num: caseDetail.value.num,
      };
      apiDefinedRequest.value = apiRequest;
      let parseRequestBodyResult;
      if (res.protocol === 'HTTP') {
        parseRequestBodyResult = parseRequestBodyFiles(res.request.body); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件
      }
      caseDetail.value = {
        ...cloneDeep(defaultCaseParams as RequestParam),
        ...({
          ...res,
          ...caseRequest,
          num: res.num,
          url: res.path,
          ...parseRequestBodyResult,
        } as Partial<TabItem>),
      };
      form.value.ignoreApiChange = caseDetail.value.ignoreApiChange;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function getApiDetail(apiDefinitionId: string) {
    try {
      const detail = await getDefinitionDetail(apiDefinitionId);
      apiDetailInfo.value = detail as ApiDefinitionDetail;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  /**
   * 设置对比
   * @params apiValue  接口数据
   * @params caseValue  用例数据
   * @params typeKey  用于增加间距Map的对应KEY\HEADER\QUERY等
   * @params nameKey  对应参数名称key
   */
  function setDiff(
    apiValue: Record<string, any>[],
    caseValue: Record<string, any>[],
    typeKey: string,
    nameKey = 'key'
  ) {
    const apiDefinedValue = (apiValue || []).filter((e) => e.key !== '') || [];

    const apiCaseValue = (caseValue || []).filter((e) => e.key !== '') || [];

    const caseValueMap = new Map();
    const apiValueMap = new Map();
    apiDefinedValue.forEach((item: any) => apiValueMap.set(item[nameKey], item));
    apiCaseValue.forEach((item: any) => caseValueMap.set(item[nameKey], item));

    const definedData: Record<string, any>[] = apiDefinedValue.map((item) => {
      if (!caseValueMap.has(item[nameKey])) {
        return {
          ...cloneDeep(item),
          diff: 'change',
        };
      }
      return item;
    });

    const caseData: Record<string, any>[] = apiCaseValue.map((item) => {
      if (!apiValueMap.has(item[nameKey])) {
        return {
          ...cloneDeep(item),
          diff: 'change',
        };
      }
      return item;
    });

    // 设置对比水平对齐间距绝对值
    const disAbs = Math.abs(caseData.length - definedData.length);
    diffDistanceMap.value[typeKey] = {
      case: caseData.length < definedData.length ? disAbs : 0,
      api: caseData.length > definedData.length ? disAbs : 0,
      display: caseData.length !== 0 || definedData.length !== 0, // 如果都为空则不展示
      showEmptyCaseTable: caseData.length !== 0, // 隐藏用例空表
      showEmptyApiTable: definedData.length !== 0, // 隐藏api空表
    };

    return {
      caseData,
      definedData,
    };
  }

  function getBodyData(bodyType: string) {
    switch (bodyType) {
      // FORM_DATA格式
      case RequestBodyFormat.FORM_DATA:
        const bodyFormDataDiffObj = setDiff(
          apiDefinedRequest.value.body.formDataBody?.formValues as any,
          caseDetail.value.body.formDataBody?.formValues,
          RequestBodyFormat.FORM_DATA
        );
        apiDefinedRequest.value.body.formDataBody.formValues =
          bodyFormDataDiffObj.definedData as ExecuteRequestCommonParam[];
        caseDetail.value.body.formDataBody.formValues = bodyFormDataDiffObj.caseData;
        break;
      // WWW_FORM格式
      case RequestBodyFormat.WWW_FORM:
        const bodyWwwFormDiffObj = setDiff(
          apiDefinedRequest.value.body.wwwFormBody?.formValues as any,
          caseDetail.value.body.wwwFormBody?.formValues,
          RequestBodyFormat.WWW_FORM
        );
        apiDefinedRequest.value.body.wwwFormBody.formValues =
          bodyWwwFormDiffObj.definedData as ExecuteRequestCommonParam[];
        caseDetail.value.body.wwwFormBody.formValues = bodyWwwFormDiffObj.caseData;
        break;
      default:
        break;
    }
  }

  // 处理对比数据
  function processData() {
    // 处理请求头
    const headersObj = setDiff(
      apiDefinedRequest.value?.headers as any,
      caseDetail.value.headers,
      RequestComposition.HEADER
    );
    if (apiDefinedRequest.value?.headers) {
      apiDefinedRequest.value.headers = headersObj.definedData as EnableKeyValueParam[];
      caseDetail.value.headers = headersObj.caseData;
    }
    // 处理query
    const queryDiffObj = setDiff(
      apiDefinedRequest.value?.query as any,
      caseDetail.value.query,
      RequestComposition.QUERY
    );
    if (apiDefinedRequest.value?.query) {
      apiDefinedRequest.value.query = queryDiffObj.definedData as ExecuteRequestCommonParam[];
      caseDetail.value.query = queryDiffObj.caseData;
    }
    // 处理rest
    const restDiffObj = setDiff(apiDefinedRequest.value?.rest as any, caseDetail.value.rest, RequestComposition.REST);
    if (apiDefinedRequest.value?.rest) {
      apiDefinedRequest.value.rest = restDiffObj.definedData as ExecuteRequestCommonParam[];
      caseDetail.value.rest = restDiffObj.caseData;
    }
    // 处理请求体
    getBodyData(RequestBodyFormat.FORM_DATA);
    getBodyData(RequestBodyFormat.WWW_FORM);
  }

  const loading = ref<boolean>(false);
  async function getRequestDetail(definedId: string, apiCaseId: string) {
    loading.value = true;
    try {
      await Promise.all([getApiDetail(definedId), getCaseDetailInfo(apiCaseId)]);
      processData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    } finally {
      loading.value = false;
    }
  }

  const ignoreThisChangeLoading = ref(false);
  // 忽略并清除本次变更
  async function clearThisChangeHandler() {
    if (props.activeApiCaseId) {
      ignoreThisChangeLoading.value = true;
      try {
        await clearThisChange(props.activeApiCaseId);
        getRequestDetail(props.activeDefinedId, props.activeApiCaseId);
        emit('clearThisChange', true);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        ignoreThisChangeLoading.value = false;
      }
    }
  }

  async function cancel() {
    if (!caseDetail.value.inconsistentWithApi) {
      try {
        await clearThisChange(props.activeApiCaseId);
        emit('loadList');
      } catch (error) {
        console.log(error);
      }
    }
    form.value = { ...initForm };
    checkType.value = [...initCheckList];
    showDiffVisible.value = false;
    emit('close');
  }

  // 是否显示配置项
  const showSyncConfig = computed(() => caseDetail.value.inconsistentWithApi && !form.value.ignoreApiChange);

  // 同步
  async function confirmSync() {
    if (
      !caseDetail.value.inconsistentWithApi ||
      form.value.ignoreApiChange ||
      !hasAnyPermission(['PROJECT_API_DEFINITION_CASE:READ+UPDATE'])
    ) {
      cancel();
      return;
    }
    syncLoading.value = true;
    try {
      // 处理同步类型参数
      checkType.value.forEach((e: any) => {
        const key = e.toLowerCase() as keyof syncItem;
        form.value.syncItems[key] = true;
      });
      const { id, request } = syncCaseDetail.value;

      const params: diffSyncParams = {
        ...form.value,
        apiCaseRequest: request,
        id,
      };
      const mergeRequest = await getSyncedCaseDetail(params);
      emit('sync', mergeRequest);
      cancel();
    } catch (error) {
      console.log(error);
    } finally {
      syncLoading.value = false;
    }
  }

  // 忽略每次变更
  async function changeIgnore(newValue: string | number | boolean) {
    try {
      await ignoreEveryTimeChange(props.activeApiCaseId, newValue as boolean);
      await getRequestDetail(props.activeDefinedId, props.activeApiCaseId);
      Message.success(newValue ? t('case.eachHasBeenIgnored') : t('case.eachHasBeenIgnoredClosed'));
      emit('clearThisChange', false);
      return false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watchEffect(() => {
    if (props.activeDefinedId && props.activeApiCaseId) {
      getRequestDetail(props.activeDefinedId, props.activeApiCaseId);
    }
  });
</script>

<style scoped lang="less">
  .legend-container {
    padding: 8px 0;
    @apply flex items-center justify-center;
    .item {
      @apply flex items-center;
      .legend {
        margin-right: 8px;
        width: 8px;
        height: 8px;
        border-radius: 2px;
        &.add {
          border: 0.5px solid rgb(var(--success-6));
        }
        &.delete {
          border: 0.5px solid rgb(var(--danger-6));
        }
      }
    }
  }
  .diff-container {
    padding: 0 16px;
    min-height: calc(100vh - 110px);
    .diff-normal {
      gap: 24px;
      @apply grid grid-cols-2;
      .diff-item {
        @apply flex-1;
        .title-type {
          color: var(--color-text-1);
          @apply font-medium;
        }
        .title {
          color: var(--color-text-1);
          @apply my-4;
        }
        .detail-item-title {
          margin-bottom: 8px;
          gap: 16px;
          @apply flex items-center justify-between;
          .detail-item-title-text {
            @apply font-medium;

            color: var(--color-text-1);
          }
        }
      }
    }
  }
  :deep(.arco-table-td-content) {
    padding: 5px 8px;
  }
  :deep(.ms-json-schema) .arco-table-td-content {
    padding: 0;
  }
</style>

<style lang="less">
  .diff-modal {
    .ms-drawer-body {
      background: var(--color-text-n9);
    }
  }
</style>
