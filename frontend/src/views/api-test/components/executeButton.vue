<template>
  <a-dropdown-button
    v-if="!caseDetail?.executeLoading && !props.executeLoading"
    v-permission="['PROJECT_API_DEFINITION_CASE:READ+EXECUTE']"
    class="exec-btn"
    @click="() => execute(isPriorityLocalExec ? 'localExec' : 'serverExec')"
    @select="execute"
  >
    {{ isPriorityLocalExec ? t('apiTestDebug.localExec') : t('apiTestDebug.serverExec') }}
    <template #icon>
      <icon-down />
    </template>
    <template #content>
      <a-doption :value="isPriorityLocalExec ? 'serverExec' : 'localExec'">
        {{ isPriorityLocalExec ? t('apiTestDebug.serverExec') : t('apiTestDebug.localExec') }}
      </a-doption>
    </template>
  </a-dropdown-button>
  <a-button v-else type="primary" @click="stopDebug">{{ t('common.stop') }}</a-button>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { cloneDeep } from 'lodash-es';

  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import { localExecuteApiDebug } from '@/api/modules/api-test/common';
  import { debugCase, runCase } from '@/api/modules/api-test/management';
  import { getSocket } from '@/api/modules/project-management/commonScript';
  import useAppStore from '@/store/modules/app';
  import { getGenerateId } from '@/utils';

  import { LocalConfig } from '@/models/user';

  import { defaultResponse } from '@/views/api-test/components/config';

  const props = defineProps<{
    environmentId?: string;
    request?: (...args) => Record<string, any>;
    isCaseDetail?: boolean;
    executeCase?: boolean;
    executeLoading?: boolean;
    isEmit?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'execute', executeType?: 'localExec' | 'serverExec'): void;
    (e: 'stopDebug'): void;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();

  const caseDetail = defineModel<RequestParam>('detail', {
    required: false,
  });

  const apiLocalExec = inject<Ref<LocalConfig>>('apiLocalExec');
  const isPriorityLocalExec = ref(apiLocalExec?.value?.enable || false); // 是否优先本地执行
  const localExecuteUrl = ref(apiLocalExec?.value?.userUrl || '');
  const reportId = ref('');
  const websocket = ref<WebSocket>();
  const temporaryResponseMap = {}; // 缓存websocket返回的报告内容，避免执行接口后切换tab导致报告丢失

  /**
   * 开启websocket监听，接收执行结果
   */
  function debugSocket(executeType?: 'localExec' | 'serverExec') {
    websocket.value = getSocket(
      reportId.value,
      executeType === 'localExec' ? '/ws/debug' : '',
      executeType === 'localExec' ? localExecuteUrl.value : ''
    );
    websocket.value.addEventListener('message', (event) => {
      if (!caseDetail.value || props.isEmit) return;
      const data = JSON.parse(event.data);
      if (data.msgType === 'EXEC_RESULT') {
        if (caseDetail.value.reportId === data.reportId) {
          // 判断当前查看的tab是否是当前返回的报告的tab，是的话直接赋值
          caseDetail.value.response = data.taskResult; // 渲染出用例详情和创建用例抽屉的响应数据
          caseDetail.value.executeLoading = false;
        } else {
          // 不是则需要把报告缓存起来，等切换到对应的tab再赋值
          temporaryResponseMap[data.reportId] = data.taskResult;
        }
      } else if (data.msgType === 'EXEC_END') {
        // 执行结束，关闭websocket
        websocket.value?.close();
        caseDetail.value.executeLoading = false;
      }
    });
  }

  async function execute(executeType?: 'localExec' | 'serverExec') {
    if (!caseDetail.value || props.isEmit) {
      emit('execute', executeType);
      return;
    }
    try {
      caseDetail.value.executeLoading = true;
      caseDetail.value.response = cloneDeep(defaultResponse);
      const makeRequestParams = props.request && props.request(executeType); // 写在reportId之前，防止覆盖reportId
      reportId.value = getGenerateId();
      caseDetail.value.reportId = reportId.value; // 存储报告ID
      let res;
      const params = {
        environmentId: props.environmentId as string,
        frontendDebug: executeType === 'localExec',
        reportId: reportId.value,
      };
      debugSocket(executeType); // 开启websocket
      if (!(caseDetail.value.id as string).startsWith('c') && executeType === 'serverExec') {
        // 已创建的服务端
        res = await runCase({
          request: props.isCaseDetail ? caseDetail.value.request : makeRequestParams?.request,
          id: caseDetail.value.id as string,
          projectId: caseDetail.value.projectId,
          linkFileIds: caseDetail.value.linkFileIds,
          uploadFileIds: caseDetail.value.uploadFileIds,
          ...params,
        });
      } else {
        res = await debugCase({
          request: props.isCaseDetail ? caseDetail.value.request : makeRequestParams?.request,
          linkFileIds: makeRequestParams?.linkFileIds,
          uploadFileIds: makeRequestParams?.uploadFileIds,
          id: `case-${Date.now()}`,
          projectId: appStore.currentProjectId,
          ...params,
        });
      }
      if (executeType === 'localExec') {
        await localExecuteApiDebug(localExecuteUrl.value, res);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      caseDetail.value.executeLoading = false;
    }
  }

  function stopDebug() {
    if (!caseDetail.value || props.isEmit) {
      emit('stopDebug');
      return;
    }
    websocket.value?.close();
    caseDetail.value.executeLoading = false;
  }

  watch(
    () => props.executeCase,
    (val) => {
      if (val === true) {
        execute(isPriorityLocalExec.value ? 'localExec' : 'serverExec');
      }
    },
    { immediate: true }
  );

  defineExpose({
    isPriorityLocalExec,
    execute,
    localExecuteUrl,
  });
</script>

<style lang="less" scoped>
  .exec-btn :deep(.arco-btn) {
    color: white !important;
    background-color: rgb(var(--primary-5)) !important;
    .btn-base-primary-hover();
    .btn-base-primary-active();
    .btn-base-primary-disabled();
  }
</style>
