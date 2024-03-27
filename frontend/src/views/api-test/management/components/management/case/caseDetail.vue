<template>
  <div class="h-full w-full overflow-hidden">
    <a-tabs v-model:active-key="activeKey" class="h-full px-[16px]" animation lazy-load @change="changeActiveKey">
      <template #extra>
        <div class="flex gap-[12px]">
          <environmentSelect v-if="props.isDrawer" v-model:current-env="environmentIdByDrawer" />
          <executeButton
            ref="executeRef"
            v-permission="['PROJECT_API_DEFINITION_CASE:READ+EXECUTE']"
            :execute-loading="caseDetail.executeLoading"
            @stop-debug="stopDebug"
            @execute="handleExecute"
          />
          <a-dropdown position="br" :hide-on-select="false" @select="handleSelect">
            <a-button v-if="!props.isDrawer" type="outline">{{ t('common.operation') }}</a-button>
            <template #content>
              <a-doption v-permission="['PROJECT_API_DEFINITION_CASE:READ+UPDATE']" value="edit">
                <MsIcon type="icon-icon_edit_outlined" />
                {{ t('common.edit') }}
              </a-doption>
              <a-doption value="share">
                <MsIcon type="icon-icon_share1" />
                {{ t('common.share') }}
              </a-doption>
              <a-doption v-permission="['PROJECT_API_DEFINITION_CASE:READ+UPDATE']" value="fork">
                <MsIcon
                  :type="caseDetail.follow ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
                  :class="`${caseDetail.follow ? 'text-[rgb(var(--warning-6))]' : ''}`"
                />
                {{ t('common.fork') }}
              </a-doption>
              <a-divider v-permission="['PROJECT_API_DEFINITION_CASE:READ+DELETE']" margin="4px" />
              <a-doption
                v-permission="['PROJECT_API_DEFINITION_CASE:READ+DELETE']"
                value="delete"
                class="error-6 text-[rgb(var(--danger-6))]"
              >
                <MsIcon type="icon-icon_delete-trash_outlined" class="text-[rgb(var(--danger-6))]" />
                {{ t('common.delete') }}
              </a-doption>
            </template>
          </a-dropdown>
        </div>
      </template>
      <a-tab-pane key="detail" :title="t('case.detail')" class="px-[18px] py-[16px]">
        <MsDetailCard :title="`【${caseDetail.num}】${caseDetail.name}`" :description="description" class="mb-[8px]">
          <template #type="{ value }">
            <apiMethodName :method="value as RequestMethods" tag-size="small" is-tag />
          </template>
          <template #priority="{ value }">
            <caseLevel :case-level="value as CaseLevel" />
          </template>
        </MsDetailCard>
        <detailTab
          :detail="caseDetail"
          :protocols="protocols as ProtocolItem[]"
          :is-priority-local-exec="isPriorityLocalExec"
          is-case
          @execute="handleExecute"
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
  <createAndEditCaseDrawer ref="createAndEditCaseDrawerRef" v-bind="$attrs" />
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsDetailCard from '@/components/pure/ms-detail-card/index.vue';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import detailTab from '../api/preview/detail.vue';
  import createAndEditCaseDrawer from './createAndEditCaseDrawer.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import environmentSelect from '@/views/api-test/components/environmentSelect.vue';
  import executeButton from '@/views/api-test/components/executeButton.vue';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import TabCaseChangeHistory from '@/views/api-test/management/components/management/case/tabContent/tabCaseChangeHistory.vue';
  import TabCaseDependency from '@/views/api-test/management/components/management/case/tabContent/tabCaseDependency.vue';
  import TabCaseExecuteHistory from '@/views/api-test/management/components/management/case/tabContent/tabCaseExecuteHistory.vue';

  import { localExecuteApiDebug } from '@/api/modules/api-test/common';
  import { debugCase, deleteCase, runCase, toggleFollowCase } from '@/api/modules/api-test/management';
  import { getSocket } from '@/api/modules/project-management/commonScript';
  import useModal from '@/hooks/useModal';
  import { getGenerateId } from '@/utils';

  import { ProtocolItem } from '@/models/apiTest/common';
  import { EnvConfig } from '@/models/projectManagement/environmental';
  import { RequestMethods } from '@/enums/apiEnum';

  import { defaultResponse } from '@/views/api-test/components/config';

  const props = defineProps<{
    isDrawer?: boolean; // 抽屉
    detail: RequestParam;
  }>();
  const emit = defineEmits<{
    (e: 'updateFollow'): void;
    (e: 'deleteCase', id: string): void;
  }>();

  const { copy, isSupported } = useClipboard();
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
      key: 'priority',
      locale: 'case.caseLevel',
      value: caseDetail.value.priority,
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
      await toggleFollowCase(caseDetail.value.id);
      Message.success(caseDetail.value.follow ? t('common.unFollowSuccess') : t('common.followSuccess'));
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
      copy(`${window.location.href}&cId=${caseDetail.value.id}`);
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

  function handleSelect(val: string | number | Record<string, any> | undefined) {
    switch (val) {
      case 'edit':
        editCase();
        break;
      case 'share':
        share();
        break;
      case 'fork':
        follow();
        break;
      case 'delete':
        handleDelete();
        break;
      default:
        break;
    }
  }

  const protocols = inject<Ref<ProtocolItem[]>>('protocols');

  const currentEnvConfigByInject = inject<Ref<EnvConfig>>('currentEnvConfig');
  const environmentId = computed(() =>
    props.isDrawer ? environmentIdByDrawer.value : currentEnvConfigByInject?.value?.id
  );

  const executeHistoryRef = ref<InstanceType<typeof TabCaseExecuteHistory>>();
  function changeActiveKey(val: string | number) {
    if (val === 'executeHistory') {
      executeHistoryRef.value?.loadExecuteList();
    }
  }

  const executeRef = ref<InstanceType<typeof executeButton>>();
  const isPriorityLocalExec = computed(() => executeRef.value?.isPriorityLocalExec ?? false);

  const reportId = ref('');
  const websocket = ref<WebSocket>();
  const temporaryResponseMap = {}; // 缓存websocket返回的报告内容，避免执行接口后切换tab导致报告丢失
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
  async function handleExecute(executeType?: 'localExec' | 'serverExec') {
    try {
      caseDetail.value.executeLoading = true;
      caseDetail.value.response = cloneDeep(defaultResponse);
      reportId.value = getGenerateId();
      caseDetail.value.reportId = reportId.value; // 存储报告ID
      let res;
      const params = {
        id: caseDetail.value.id as string,
        environmentId: environmentId.value,
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
  function stopDebug() {
    websocket.value?.close();
    caseDetail.value.executeLoading = false;
    executeCase.value = false;
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
        @apply px-0;
      }
      .arco-tabs-content-list {
        @apply h-full;
      }
      .arco-tabs-content-list .arco-tabs-content-item:nth-of-type(1) .arco-tabs-pane {
        @apply h-full overflow-hidden;
      }
      .arco-collapse {
        height: calc(100% - 85px);
      }
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
