<template>
  <a-modal
    v-model:visible="visible"
    :title="t('common.export')"
    title-align="start"
    class="ms-modal-upload ms-modal-medium"
    :width="400"
  >
    <div class="mb-[16px] flex items-center gap-[8px]">
      <a-switch v-model:model-value="exportTypeRadio" size="small"></a-switch>
      {{ t('apiScenario.export.type.all') }}
      <a-tooltip :content="t('apiScenario.export.simple.tooltip')" position="tl">
        <icon-question-circle
          class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
          size="16"
        />
        <template #content>
          <div>{{ t('apiScenario.export.simple.tooltip1') }}</div>
          <div>{{ t('apiScenario.export.simple.tooltip2') }}</div>
        </template>
      </a-tooltip>
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

  import { exportScenario, getScenarioDownloadFile, stopScenarioExport } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useWebsocket from '@/hooks/useWebsocket';
  import useAppStore from '@/store/modules/app';
  import { downloadByteFile, getGenerateId } from '@/utils';

  const appStore = useAppStore();
  const { t } = useI18n();

  const props = defineProps<{
    batchParams: BatchActionQueryParams;
    conditionParams: Record<string, any> | (() => Record<string, any>);
    sorter?: Record<string, any>;
    isShare?: boolean; // 分享文档id
  }>();

  const visible = defineModel<boolean>('visible', { required: true });

  const exportLoading = ref(false);
  const exportTypeRadio = ref(false);
  function cancelExport() {
    visible.value = false;
  }
  const websocket = ref<WebSocket>();
  const reportId = ref('');
  const isShowExportingMessage = ref(false); // 正在导出提示显示中
  const exportingMessage = ref();

  // 下载文件
  async function downloadFile(id: string) {
    try {
      const response = await getScenarioDownloadFile(appStore.currentProjectId, id);
      downloadByteFile(response, 'metersphere-scenaro.zip');
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

  // 取消导出
  async function stopExport(taskId: string) {
    try {
      await stopScenarioExport(taskId);
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
      const res = await exportScenario(
        {
          exportAllRelatedData: exportTypeRadio.value,
          selectIds: selectedIds || [],
          selectAll: !!selectAll,
          excludeIds: excludeIds || [],
          ...batchConditionParams,
          sort: props.sorter || {},
          fileId: reportId.value,
        },
        'METERSPHERE'
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
