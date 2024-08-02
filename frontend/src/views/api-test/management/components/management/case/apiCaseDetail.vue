<template>
  <MsCard :header-min-width="1100" :min-width="150" auto-height hide-footer no-content-padding hide-divider hide-back>
    <template #headerLeft>
      <div class="flex items-center gap-4">
        <caseLevel :case-level="caseDetail.priority as CaseLevel" />
        <div class="one-line-text max-w-[300px] font-medium text-[var(--color-text-1)]">
          {{ `[${caseDetail.num}] ${caseDetail.name}` }}
        </div>

        <a-tooltip :content="t(caseDetail.follow ? 'common.forked' : 'common.notForked')">
          <MsIcon
            v-permission="['PROJECT_API_DEFINITION_CASE:READ+UPDATE']"
            :loading="followLoading"
            :type="caseDetail.follow ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
            :class="`${caseDetail.follow ? 'text-[rgb(var(--warning-6))]' : 'text-[var(--color-text-4)]'}`"
            class="cursor-pointer"
            :size="16"
            @click="follow"
          />
        </a-tooltip>

        <a-tooltip :content="t('report.detail.api.copyLink')">
          <MsIcon type="icon-icon_share1" class="cursor-pointer text-[var(--color-text-4)]" :size="16" @click="share" />
        </a-tooltip>
      </div>
    </template>
    <template #headerRight>
      <div class="flex gap-[12px]">
        <MsEnvironmentSelect :env="environmentIdByDrawer" />
        <executeButton
          ref="executeRef"
          v-permission="['PROJECT_API_DEFINITION_CASE:READ+EXECUTE']"
          :execute-loading="caseDetail.executeLoading"
          @stop-debug="stopDebug"
          @execute="handleExecute"
        />
        <a-dropdown-button type="outline" @click="editCase">
          {{ t('common.edit') }}
          <template #icon>
            <icon-down />
          </template>
          <template #content>
            <a-doption
              v-permission="['PROJECT_API_DEFINITION_CASE:READ+DELETE']"
              value="delete"
              class="error-6 text-[rgb(var(--danger-6))]"
              @click="handleDelete"
            >
              <MsIcon type="icon-icon_delete-trash_outlined1" class="text-[rgb(var(--danger-6))]" />
              {{ t('common.delete') }}
            </a-doption>
          </template>
        </a-dropdown-button>
      </div>
    </template>
    <createAndEditCaseDrawer
      ref="createAndEditCaseDrawerRef"
      v-bind="$attrs"
      @load-case="(id)=>getCaseDetailInfo(id as string)"
    />
    <a-divider :margin="0"></a-divider>
    <MsTab
      v-model:active-key="activeKey"
      :show-badge="false"
      :content-tab-list="tabList"
      no-content
      class="relative mx-[16px] border-b"
    />
  </MsCard>
  <MsCard class="mt-[16px]" :special-height="174" simple>
    <div v-if="activeKey === 'detail'">
      <MsDetailCard :title="t('common.baseInfo')" :description="description" class="mb-[8px]">
        <template #type="{ value }">
          <apiMethodName v-if="value" :method="value as RequestMethods" tag-size="small" is-tag />
        </template>
      </MsDetailCard>
      <detailTab
        :detail="caseDetail as RequestParam"
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
      />
    </div>
    <tab-case-dependency v-else-if="activeKey === 'reference'" :source-id="caseDetail.id" />
    <tab-case-execute-history
      v-else-if="activeKey === 'executeHistory'"
      ref="executeHistoryRef"
      :source-id="caseDetail.id"
      module-type="API_REPORT"
      :protocol="caseDetail.protocol"
    />
    <tab-case-change-history v-else :source-id="caseDetail.id" />
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsDetailCard from '@/components/pure/ms-detail-card/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import MsEnvironmentSelect from '@/components/business/ms-environment-select/index.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import executeButton from '@/views/api-test/components/executeButton.vue';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import detailTab from '@/views/api-test/management/components/management/api/preview/detail.vue';
  import createAndEditCaseDrawer from '@/views/api-test/management/components/management/case/createAndEditCaseDrawer.vue';
  import DifferentDrawer from '@/views/api-test/management/components/management/case/differentDrawer.vue';
  import TabCaseChangeHistory from '@/views/api-test/management/components/management/case/tabContent/tabCaseChangeHistory.vue';
  import TabCaseDependency from '@/views/api-test/management/components/management/case/tabContent/tabCaseDependency.vue';
  import TabCaseExecuteHistory from '@/views/api-test/management/components/management/case/tabContent/tabCaseExecuteHistory.vue';

  import { getProtocolList, localExecuteApiDebug, stopExecute, stopLocalExecute } from '@/api/modules/api-test/common';
  import { debugCase, deleteCase, getCaseDetail, runCase, toggleFollowCase } from '@/api/modules/api-test/management';
  import { getSocket } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import { getGenerateId } from '@/utils';

  import { ProtocolItem } from '@/models/apiTest/common';
  import { RequestMethods, ScenarioStepType } from '@/enums/apiEnum';

  import { defaultCaseParams, defaultResponse } from '@/views/api-test/components/config';
  import { parseRequestBodyFiles } from '@/views/api-test/components/utils';

  const { openModal } = useModal();
  const appStore = useAppStore();

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();

  const activeKey = ref('detail');

  const { copy, isSupported } = useClipboard({ legacy: true });

  const caseDetail = ref<RequestParam>(cloneDeep(defaultCaseParams));
  const environmentIdByDrawer = ref('');
  const followLoading = ref(false);
  async function follow() {
    try {
      followLoading.value = true;
      await toggleFollowCase(caseDetail.value.id);
      Message.success(caseDetail.value.follow ? t('common.unFollowSuccess') : t('common.followSuccess'));
      caseDetail.value.follow = !caseDetail.value.follow;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      followLoading.value = false;
    }
  }

  const executeRef = ref<InstanceType<typeof executeButton>>();
  const reportId = ref('');
  const executeCase = ref<boolean>(false);
  const websocket = ref<WebSocket>();
  async function stopDebug() {
    try {
      if (caseDetail.value.frontendDebug) {
        await stopLocalExecute(executeRef.value?.localExecuteUrl || '', reportId.value, ScenarioStepType.API_CASE);
      } else {
        await stopExecute(reportId.value, ScenarioStepType.API_CASE);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
    websocket.value?.close();
    caseDetail.value.executeLoading = false;
    executeCase.value = false;
  }
  const temporaryResponseMap: Record<string, any> = {}; // 缓存websocket返回的报告内容，避免执行接口后切换tab导致报告丢失
  const executeHistoryRef = ref<InstanceType<typeof TabCaseExecuteHistory>>();

  // 开启websocket监听，接收执行结果
  function debugSocket(executeType?: 'localExec' | 'serverExec') {
    websocket.value = getSocket(
      reportId.value,
      executeType === 'localExec' ? '/ws/debug' : '',
      executeType === 'localExec' ? executeRef.value?.localExecuteUrl : ''
    );
    websocket.value.addEventListener('message', (event) => {
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
    });
  }
  // 执行
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
      debugSocket(executeType); // 开启websocket
      if (executeType === 'serverExec') {
        // 已创建的服务端
        res = await runCase(params);
      } else {
        res = await debugCase(params);
      }
      if (executeType === 'localExec') {
        await localExecuteApiDebug(executeRef.value?.localExecuteUrl as string, res);
      }
      // 执行完更新执行历史
      executeHistoryRef.value?.loadExecuteList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      caseDetail.value.executeLoading = false;
      executeCase.value = false;
    }
  }
  const createAndEditCaseDrawerRef = ref<InstanceType<typeof createAndEditCaseDrawer>>();
  function editCase() {
    createAndEditCaseDrawerRef.value?.open(
      caseDetail.value.apiDefinitionId,
      caseDetail.value as unknown as RequestParam,
      false
    );
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
          // TODO 删除后这里返回原本的页面
          router.back();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  provide('defaultCaseParams', readonly(defaultCaseParams));

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

  async function getCaseDetailInfo(id: string) {
    try {
      const res = await getCaseDetail(id);
      let parseRequestBodyResult;
      if (res.protocol === 'HTTP') {
        parseRequestBodyResult = parseRequestBodyFiles(res.request.body); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件
      }
      caseDetail.value = {
        ...cloneDeep(defaultCaseParams as RequestParam),
        ...({
          ...res.request,
          ...res,
          url: res.path,
          ...parseRequestBodyResult,
        } as Partial<TabItem>),
      };
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const description = computed(() => [
    {
      key: 'type',
      locale: 'apiTestManagement.apiType',
      value: caseDetail.value.method,
    },
    {
      key: 'name',
      locale: 'case.belongingApi',
      value: `[${caseDetail.value.num}] ${caseDetail.value.name}`,
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

  const tabList = ref([
    {
      value: 'detail',
      label: t('case.detail'),
    },
    {
      value: 'reference',
      label: t('apiTestManagement.reference'),
    },
    {
      value: 'executeHistory',
      label: t('apiTestManagement.executeHistory'),
    },
    {
      value: 'changeHistory',
      label: t('apiTestManagement.changeHistory'),
    },
  ]);

  const protocols = ref<ProtocolItem[]>([]);
  provide('protocols', readonly(protocols));
  async function initProtocolList() {
    try {
      protocols.value = await getProtocolList(appStore.currentOrgId);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
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

  const isPriorityLocalExec = computed(() => executeRef.value?.isPriorityLocalExec ?? false);

  onBeforeMount(() => {
    initProtocolList();
    const caseId = route.query.id;
    getCaseDetailInfo(caseId as string);
  });
</script>

<style scoped lang="less">
  :deep(.ms-detail-card-desc) {
    gap: 16px;
    flex-wrap: nowrap !important;
    & > div:nth-of-type(n) {
      width: auto;
      max-width: 30%;
    }
  }
</style>
