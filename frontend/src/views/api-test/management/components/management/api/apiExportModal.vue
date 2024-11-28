<template>
  <a-modal
    v-model:visible="visible"
    :title="t('common.export')"
    title-align="start"
    class="ms-modal-upload ms-modal-medium"
    :width="400"
  >
    <div class="mb-[16px] flex gap-[8px]">
      <div
        v-for="item of platformList"
        :key="item.value"
        :class="`import-item ${exportPlatform === item.value ? 'import-item--active' : ''}`"
        @click="exportPlatform = item.value"
      >
        <div class="text-[var(--color-text-1)]">{{ item.name }}</div>
      </div>
    </div>
    <template v-if="exportPlatform === RequestExportFormat.MeterSphere">
      <div class="mb-[16px] flex items-center gap-[4px]">
        <a-switch v-model:model-value="exportApiCase" size="small" />
        {{ t('apiTestManagement.exportCase') }}
      </div>
      <div class="flex items-center gap-[4px]">
        <a-switch v-model:model-value="exportApiMock" size="small" />
        {{ t('apiTestManagement.exportMock') }}
      </div>
    </template>
    <div v-else-if="exportPlatform === RequestExportFormat.SWAGGER" class="text-[var(--color-text-4)]">
      {{ t('apiTestManagement.exportSwaggerTip') }}
    </div>
    <template #footer>
      <div class="flex justify-end">
        <a-button type="secondary" :disabled="exportLoading" @click="cancelExport">
          {{ t('common.cancel') }}
        </a-button>
        <a-button class="ml-3" type="primary" :loading="exportLoading" @click="exportApi">
          {{ t('common.export') }}
        </a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import type { BatchActionQueryParams } from '@/components/pure/ms-table/type';

  import {
    exportApiDefinition,
    exportShareApiDefinition,
    getApiDownloadFile,
    getShareApiDownloadFile,
    stopApiExport,
    stopShareApiExport,
  } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useWebsocket from '@/hooks/useWebsocket';
  import useAppStore from '@/store/modules/app';
  import { downloadByteFile, getGenerateId } from '@/utils';

  import { RequestExportFormat } from '@/enums/apiEnum';

  const appStore = useAppStore();
  const { t } = useI18n();

  const props = defineProps<{
    batchParams: BatchActionQueryParams;
    conditionParams: Record<string, any> | (() => Record<string, any>);
    sorter?: Record<string, any>;
    isShare?: boolean; // 分享文档id
  }>();

  const visible = defineModel<boolean>('visible', { required: true });

  const exportPlatform = ref(RequestExportFormat.SWAGGER);
  const exportApiCase = ref(false);
  const exportApiMock = ref(false);
  const exportLoading = ref(false);

  const platformList = [
    {
      name: 'Swagger',
      value: RequestExportFormat.SWAGGER,
    },
    {
      name: 'MeterSphere',
      value: RequestExportFormat.MeterSphere,
    },
  ];

  function cancelExport() {
    visible.value = false;
    exportPlatform.value = RequestExportFormat.SWAGGER;
  }

  const websocket = ref<WebSocket>();
  const reportId = ref('');
  const isShowExportingMessage = ref(false); // 正在导出提示显示中
  const exportingMessage = ref();

  const getApiDownload = computed(() => (props.isShare ? getShareApiDownloadFile : getApiDownloadFile));

  // 下载文件
  async function downloadFile(id: string) {
    try {
      const response = await getApiDownload.value(appStore.currentProjectId, id);
      downloadByteFile(response, 'metersphere-definition.zip');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  // 提示：导出成功
  function showExportSuccessfulMessage(id: string, count: number) {
    Message.success({
      content: () =>
        h('div', { class: 'flex flex-col gap-[8px] items-start' }, [
          h('div', { class: 'font-medium' }, t('common.exportSuccessful')),
          h('div', { class: 'flex items-center gap-[12px]' }, [
            h('div', t('caseManagement.featureCase.exportApiCount', { number: count })),
            h(
              MsButton,
              {
                type: 'text',
                onClick() {
                  downloadFile(id);
                },
              },
              { default: () => t('common.downloadFile') }
            ),
          ]),
        ]),
      duration: 10000, // 10s 自动关闭
      closable: true,
    });
  }
  // 开启websocket监听，接收结果
  async function startWebsocketGetExportResult() {
    const { createSocket, websocket: _websocket } = useWebsocket({
      reportId: reportId.value,
      socketUrl: '/ws/export',
      onMessage: (event) => {
        const data = JSON.parse(event.data);
        if (data.msgType === 'EXEC_RESULT') {
          exportingMessage.value.close();
          reportId.value = data.fileId;
          // taskId.value = data.taskId;
          if (data.isSuccessful) {
            showExportSuccessfulMessage(reportId.value, data.count);
          } else {
            Message.error({
              content: t('common.exportFailed'),
              duration: 999999999, // 一直展示，除非手动关闭
              closable: true,
            });
          }
          websocket.value?.close();
        }
      },
    });
    await createSocket();
    websocket.value = _websocket.value;
  }

  const stopShareApi = computed(() => (props.isShare ? stopShareApiExport : stopApiExport));
  // 取消导出
  async function stopExport(taskId: string) {
    try {
      await stopShareApi.value(taskId);
      exportingMessage.value.close();
      websocket.value?.close();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  // 提示：正在导出
  function showExportingMessage(taskId: string) {
    if (isShowExportingMessage.value) return;
    isShowExportingMessage.value = true;
    exportingMessage.value = Message.loading({
      content: () =>
        h('div', { class: 'flex items-center gap-[12px]' }, [
          h('div', t('common.exporting')),
          h(
            MsButton,
            {
              type: 'text',
              onClick() {
                stopExport(taskId);
              },
            },
            { default: () => t('common.cancel') }
          ),
        ]),
      duration: 999999999, // 一直展示，除非手动关闭
      closable: true,
      onClose() {
        isShowExportingMessage.value = false;
      },
    });
  }

  const exportApiRequest = computed(() => (props.isShare ? exportShareApiDefinition : exportApiDefinition));
  /**
   * 导出接口
   */
  async function exportApi() {
    try {
      exportLoading.value = true;
      reportId.value = getGenerateId();
      await startWebsocketGetExportResult();
      const batchConditionParams =
        typeof props.conditionParams === 'function' ? await props.conditionParams() : props.conditionParams;
      const { selectedIds, selectAll, excludeIds } = props.batchParams;
      const res = await exportApiRequest.value(
        {
          selectIds: selectedIds || [],
          selectAll: !!selectAll,
          excludeIds: excludeIds || [],
          ...batchConditionParams,
          exportApiCase: exportApiCase.value,
          exportApiMock: exportApiMock.value,
          sort: props.sorter || {},
          fileId: reportId.value,
        },
        exportPlatform.value
      );
      showExportingMessage(res);
      visible.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      exportLoading.value = false;
    }
  }
</script>

<style scoped lang="less">
  .import-item {
    @apply flex cursor-pointer items-center;

    padding: 8px;
    width: 150px;
    border: 1px solid var(--color-text-n8);
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-fff);
    gap: 6px;
  }
  .import-item--active {
    border: 1px solid rgb(var(--primary-5));
    background-color: rgb(var(--primary-1));
  }
</style>
