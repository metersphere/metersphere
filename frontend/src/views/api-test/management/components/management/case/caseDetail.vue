<template>
  <div class="flex h-full w-full flex-col overflow-hidden">
    <div class="p-[16px]">
      <MsDetailCard :title="`[${caseDetail.num}] ${caseDetail.name}`" :description="description" class="mb-[8px]">
        <template #titlePrefix>
          <MsAiTag v-if="caseDetail.aiCreate" />
          <caseLevel :case-level="caseDetail.priority as CaseLevel" />
        </template>
        <template v-if="!props.isDrawer" #titleAppend>
          <a-tooltip :content="t('report.detail.api.copyLink')">
            <MsIcon
              type="icon-icon_unlink"
              class="cursor-pointer text-[var(--color-text-4)] hover:bg-[var(--color-bg-3)]"
              :size="16"
              @click="share"
            />
          </a-tooltip>
          <a-tooltip :content="t(caseDetail.follow ? 'common.forked' : 'common.notForked')">
            <MsIcon
              v-permission="['PROJECT_API_DEFINITION_CASE:READ+UPDATE']"
              :loading="followLoading"
              :type="caseDetail.follow ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
              :class="`${
                caseDetail.follow
                  ? 'text-[rgb(var(--warning-6))]'
                  : 'text-[var(--color-text-4)] hover:bg-[var(--color-bg-3)]'
              }`"
              class="cursor-pointer"
              :size="16"
              @click="follow"
            />
          </a-tooltip>
        </template>
        <template #titleRight>
          <div class="flex gap-[8px]">
            <a-button
              v-permission="['PROJECT_API_DEFINITION_CASE:READ+DELETE']"
              type="outline"
              class="arco-btn-outline--secondary"
              size="small"
              @click="handleDelete"
            >
              {{ t('common.delete') }}
            </a-button>
            <a-button
              v-if="!props.isDrawer"
              v-permission="['PROJECT_API_DEFINITION_CASE:READ+UPDATE']"
              type="outline"
              size="small"
              @click="editCase"
            >
              {{ t('common.edit') }}
            </a-button>
            <executeButton
              ref="executeRef"
              v-permission="['PROJECT_API_DEFINITION_CASE:READ+EXECUTE']"
              size="small"
              :execute-loading="caseDetail.executeLoading"
              @stop-debug="stopDebug"
              @execute="handleExecute"
            />
          </div>
        </template>
        <template #type="{ value }">
          <apiMethodName :method="value as RequestMethods" tag-size="small" is-tag />
        </template>
      </MsDetailCard>
    </div>

    <a-tabs
      v-model:active-key="activeKey"
      class="no-left-tab flex-1 px-[16px]"
      animation
      lazy-load
      @change="changeActiveKey"
    >
      <template #extra>
        <div class="flex gap-[12px]">
          <MsEnvironmentSelect v-if="props.isDrawer" :env="environmentIdByDrawer" />
        </div>
      </template>
      <a-tab-pane key="detail" :title="t('case.detail')" class="px-[18px] py-[16px]">
        <detailTab
          :detail="caseDetail"
          :protocols="protocols as ProtocolItem[]"
          :is-priority-local-exec="isPriorityLocalExec"
          is-case
          @execute="handleExecute"
          @show-diff="showDiffDrawer"
        />
        <DifferentDrawer
          v-model:visible="showDifferentDrawer"
          :active-api-case-id="activeApiCaseId"
          :active-defined-id="activeDefinedId"
          @close="closeDifferent"
          @clear-this-change="(isEvery:boolean)=>brashChangeHandler(isEvery)"
          @sync="syncParamsHandler"
          @load-list="emit('loadCase', props.detail.id as string)"
        />
      </a-tab-pane>
      <a-tab-pane key="reference" :title="t('apiTestManagement.reference')" class="px-[18px] py-[16px]">
        <tab-case-dependency :source-id="caseDetail.id" />
      </a-tab-pane>
      <a-tab-pane key="executeHistory" :title="t('apiTestManagement.executeHistory')" class="px-[18px] py-[16px]">
        <tab-case-execute-history
          ref="executeHistoryRef"
          :source-id="caseDetail.id"
          module-type="API_REPORT"
          :protocol="caseDetail.protocol"
        />
      </a-tab-pane>
      <!-- <a-tab-pane key="dependencies" :title="t('apiTestManagement.dependencies')" class="px-[18px] py-[16px]">
        </a-tab-pane> -->
      <a-tab-pane key="changeHistory" :title="t('apiTestManagement.changeHistory')" class="px-[18px] py-[16px]">
        <tab-case-change-history :source-id="caseDetail.id" />
      </a-tab-pane>
    </a-tabs>
  </div>
  <createAndEditCaseDrawer ref="createAndEditCaseDrawerRef" v-bind="$attrs" @load-case="loadCaseDetail" />
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsAiTag from '@/components/pure/ms-ai-tag/index.vue';
  import MsDetailCard from '@/components/pure/ms-detail-card/index.vue';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import MsEnvironmentSelect from '@/components/business/ms-environment-select/index.vue';
  import detailTab from '../api/preview/detail.vue';
  import createAndEditCaseDrawer from './createAndEditCaseDrawer.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import executeButton from '@/views/api-test/components/executeButton.vue';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import DifferentDrawer from '@/views/api-test/management/components/management/case/differentDrawer.vue';
  import TabCaseChangeHistory from '@/views/api-test/management/components/management/case/tabContent/tabCaseChangeHistory.vue';
  import TabCaseDependency from '@/views/api-test/management/components/management/case/tabContent/tabCaseDependency.vue';
  import TabCaseExecuteHistory from '@/views/api-test/management/components/management/case/tabContent/tabCaseExecuteHistory.vue';

  import { localExecuteApiDebug, stopLocalExecute } from '@/api/modules/api-test/common';
  import {
    debugCase,
    deleteCase,
    runCase,
    toggleFollowCase,
    toggleUnFollowCase,
  } from '@/api/modules/api-test/management';
  import { projectStopTask } from '@/api/modules/taskCenter/project';
  import useModal from '@/hooks/useModal';
  import useWebsocket from '@/hooks/useWebsocket';
  import useAppStore from '@/store/modules/app';
  import { getGenerateId } from '@/utils';

  import { ProtocolItem } from '@/models/apiTest/common';
  import { RequestMethods, ScenarioStepType } from '@/enums/apiEnum';

  import { defaultResponse } from '@/views/api-test/components/config';

  const props = defineProps<{
    isDrawer?: boolean; // 抽屉
    detail: RequestParam;
  }>();

  const emit = defineEmits<{
    (e: 'updateFollow'): void;
    (e: 'deleteCase', id: string): void;
    (e: 'loadCase', id: string): void;
  }>();

  const appStore = useAppStore();
  const { copy, isSupported } = useClipboard({ legacy: true });
  const { t } = useI18n();
  const { openModal } = useModal();

  const executeCase = defineModel<boolean>('executeCase', { default: false });
  const caseDetail = ref<RequestParam>(cloneDeep(props.detail)); // props.detail是嵌套的引用类型，防止不必要的修改来源影响props.detail的数据
  const environmentIdByDrawer = ref(props.detail.environmentId);

  const activeKey = ref('detail');

  const description = computed(() => [
    {
      key: 'type',
      locale: 'apiTestManagement.apiType',
      value: caseDetail.value.method,
    },
    {
      key: 'name',
      locale: 'case.belongingApi',
      value: `[${caseDetail.value.apiDefinitionNum}] ${caseDetail.value.apiDefinitionName}`,
    },
    {
      key: 'path',
      locale: 'apiTestManagement.path',
      value: caseDetail.value.url || caseDetail.value.path,
    },
    {
      key: 'tags',
      locale: 'common.tag',
      value: caseDetail.value.tags,
    },
  ]);

  const followLoading = ref(false);
  async function follow() {
    try {
      followLoading.value = true;
      if (caseDetail.value.follow) {
        await toggleUnFollowCase(caseDetail.value.id);
      } else {
        await toggleFollowCase(caseDetail.value.id);
      }
      Message.success(caseDetail.value.follow ? t('common.unFollowSuccess') : t('common.followSuccess'));
      caseDetail.value.follow = !caseDetail.value.follow;
      emit('updateFollow');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      followLoading.value = false;
    }
  }

  function share() {
    if (isSupported) {
      const url = window.location.href;
      const dIdParam = `&cId=${caseDetail.value.id}`;
      const copyUrl = url.includes('cId') ? url.split('&cId')[0] : url;
      copy(`${copyUrl}${dIdParam}`);
      Message.success(t('apiTestManagement.shareUrlCopied'));
    } else {
      Message.error(t('common.copyNotSupport'));
    }
  }

  const createAndEditCaseDrawerRef = ref<InstanceType<typeof createAndEditCaseDrawer>>();
  function editCase() {
    createAndEditCaseDrawerRef.value?.open(caseDetail.value.apiDefinitionId, caseDetail.value, false);
  }

  function handleDelete() {
    openModal({
      type: 'error',
      title: t('apiTestManagement.deleteApiTipTitle', { name: caseDetail.value.name }),
      content: t('case.deleteCaseTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteCase(caseDetail.value.id as string);
          emit('deleteCase', caseDetail.value.id as string);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const protocols = inject<Ref<ProtocolItem[]>>('protocols');

  const executeHistoryRef = ref<InstanceType<typeof TabCaseExecuteHistory>>();
  function changeActiveKey(val: string | number) {
    if (val === 'executeHistory') {
      executeHistoryRef.value?.loadExecuteList();
    }
  }
  watch(
    () => caseDetail.value.id,
    () => {
      if (activeKey.value === 'executeHistory') {
        executeHistoryRef.value?.loadExecuteList(caseDetail.value.id as string);
      }
    }
  );

  const executeRef = ref<InstanceType<typeof executeButton>>();
  const isPriorityLocalExec = computed(() => executeRef.value?.isPriorityLocalExec ?? false);

  const reportId = ref('');
  const websocket = ref<WebSocket>();
  const temporaryResponseMap: Record<string, any> = {}; // 缓存websocket返回的报告内容，避免执行接口后切换tab导致报告丢失
  // 开启websocket监听，接收执行结果
  async function debugSocket(executeType?: 'localExec' | 'serverExec') {
    const { createSocket, websocket: _websocket } = useWebsocket({
      reportId: reportId.value,
      socketUrl: executeType === 'localExec' ? '/ws/debug' : '',
      host: executeType === 'localExec' ? executeRef.value?.localExecuteUrl : '',
      onMessage: (event) => {
        const data = JSON.parse(event.data);
        if (data.msgType === 'EXEC_RESULT') {
          if (caseDetail.value.reportId === data.reportId) {
            // 判断当前查看的tab是否是当前返回的报告的tab，是的话直接赋值
            caseDetail.value.response = data.taskResult; // 渲染出用例详情和创建用例抽屉的响应数据
            caseDetail.value.executeLoading = false;
            executeCase.value = false;
          } else {
            // 不是则需要把报告缓存起来，等切换到对应的tab再赋值
            temporaryResponseMap[data.reportId] = data.taskResult;
          }
        } else if (data.msgType === 'EXEC_END') {
          // 执行结束，关闭websocket
          websocket.value?.close();
          caseDetail.value.executeLoading = false;
          executeCase.value = false;
        }
      },
    });
    await createSocket();
    websocket.value = _websocket.value;
  }
  const executeTaskId = ref('');
  async function handleExecute(executeType?: 'localExec' | 'serverExec') {
    try {
      caseDetail.value.executeLoading = true;
      caseDetail.value.response = cloneDeep(defaultResponse);
      reportId.value = getGenerateId();
      caseDetail.value.reportId = reportId.value; // 存储报告ID
      let res;
      const params = {
        id: caseDetail.value.id as string,
        environmentId: appStore.currentEnvConfig?.id || '',
        frontendDebug: executeType === 'localExec',
        reportId: reportId.value,
        apiDefinitionId: caseDetail.value.apiDefinitionId,
        request: caseDetail.value.request,
        projectId: caseDetail.value.projectId,
        linkFileIds: caseDetail.value.linkFileIds,
        uploadFileIds: caseDetail.value.uploadFileIds,
      };
      await debugSocket(executeType); // 开启websocket
      if (executeType === 'serverExec') {
        // 已创建的服务端
        res = await runCase(params);
      } else {
        res = await debugCase(params);
      }
      if (executeType === 'localExec') {
        await localExecuteApiDebug(executeRef.value?.localExecuteUrl as string, res);
      }
      executeTaskId.value = res.taskInfo?.taskId;
      // 执行完更新执行历史
      executeHistoryRef.value?.loadExecuteList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      caseDetail.value.executeLoading = false;
      executeCase.value = false;
    }
  }
  async function stopDebug() {
    try {
      if (caseDetail.value.frontendDebug) {
        await stopLocalExecute(executeRef.value?.localExecuteUrl || '', reportId.value, ScenarioStepType.API_CASE);
      } else {
        await projectStopTask(executeTaskId.value);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
    websocket.value?.close();
    caseDetail.value.executeLoading = false;
    executeCase.value = false;
  }

  // 定义id
  const activeDefinedId = ref<string>('');
  // 用例id
  const activeApiCaseId = ref<string>('');

  const showDifferentDrawer = ref<boolean>(false);
  // 查看diff对比
  function showDiffDrawer() {
    activeApiCaseId.value = caseDetail.value.id as string;
    activeDefinedId.value = caseDetail.value.apiDefinitionId;
    showDifferentDrawer.value = true;
  }

  function closeDifferent() {
    showDifferentDrawer.value = false;
    activeApiCaseId.value = '';
    activeDefinedId.value = '';
  }

  // 忽略本次变更
  async function brashChangeHandler(isClose = false) {
    emit('loadCase', props.detail.id as string);
    if (isClose) {
      showDifferentDrawer.value = false;
    }
  }

  function loadCaseDetail() {
    emit('loadCase', props.detail.id as string);
  }

  // 同步参数
  function syncParamsHandler(mergedRequestParam: RequestParam) {
    caseDetail.value = { ...caseDetail.value, ...mergedRequestParam };
    createAndEditCaseDrawerRef.value?.open(caseDetail.value.apiDefinitionId, caseDetail.value, false);
  }

  watch(
    () => props.detail,
    () => {
      caseDetail.value = cloneDeep(props.detail); // props.detail是嵌套的引用类型，防止不必要的修改来源影响props.detail的数据
      environmentIdByDrawer.value = props.detail.environmentId;
    },
    { immediate: true }
  );

  onMounted(async () => {
    if (executeCase.value && !caseDetail.value.executeLoading) {
      // 如果是执行操作打开用例详情，且该用例不在执行状态中，则立即执行
      handleExecute(isPriorityLocalExec.value ? 'localExec' : 'serverExec');
    }
  });

  defineExpose({
    editCase,
    share,
    follow,
    handleDelete,
  });
</script>

<style lang="less" scoped>
  .arco-tabs {
    @apply flex flex-col;
    :deep(.arco-tabs-nav) {
      border-bottom: 1px solid var(--color-text-n8);
      &-extra {
        line-height: 32px;
      }
    }
    :deep(.arco-tabs-content) {
      @apply flex-1 pt-0;
      .arco-tabs-content-item {
        @apply overflow-y-auto px-0;
        .ms-scroll-bar();
      }
      .arco-tabs-content-list {
        @apply h-full;
      }
      .arco-collapse {
        height: calc(100% - 85px);
      }
    }
  }
  :deep(.no-left-tab) {
    .arco-tabs-tab {
      margin: 0 32px 0 0;
    }
  }
  :deep(.ms-detail-card-desc) {
    gap: 16px;
    flex-wrap: nowrap !important;
    & > div:nth-of-type(n) {
      width: auto;
      max-width: 30%;
    }
  }
  .error-6 {
    color: rgb(var(--danger-6));
    &:hover {
      color: rgb(var(--danger-6));
    }
  }
  :deep(.monaco-editor) {
    height: 212px !important;
  }
</style>
