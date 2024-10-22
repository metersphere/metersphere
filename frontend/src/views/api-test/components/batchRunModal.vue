<template>
  <a-modal v-model:visible="showBatchExecute" title-align="start" class="ms-modal-upload ms-modal-medium" :width="480">
    <template #title>
      {{ t('report.trigger.batch.execution') }}
      <div class="text-[var(--color-text-4)]">
        {{
          t('case.batchModalSubTitle', {
            count: props.batchParams?.currentSelectCount || tableSelected.length,
          })
        }}
      </div>
    </template>

    <a-form ref="batchExecuteFormRef" class="ms-form rounded-[4px]" :model="batchExecuteForm" layout="vertical">
      <a-form-item field="defaultEnv" :label="t('case.execute.selectEnv')">
        <a-radio-group v-model="batchExecuteForm.defaultEnv">
          <a-radio value="true"
            >{{ t('case.execute.defaultEnv') }}
            <a-tooltip :content="t('case.execute.defaultEnvTip')" position="top">
              <icon-question-circle
                class="text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </a-radio>
          <a-radio value="false">{{ t('case.execute.newEnv') }}</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item
        v-if="batchExecuteForm.defaultEnv == 'false'"
        field="environmentId"
        :label="t('case.execute.newEnv')"
        :rules="[{ required: true, message: t('apiTestManagement.envRequired') }]"
        asterisk-position="end"
        required
      >
        <a-select v-model="batchExecuteForm.environmentId" :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of environmentList" :key="item.id" :value="item.id">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="runMode" :label="t('case.execute.model')">
        <a-radio-group v-model="batchExecuteForm.runMode">
          <a-radio value="SERIAL">{{ t('case.execute.serial') }}</a-radio>
          <a-radio value="PARALLEL">{{ t('case.execute.parallel') }}</a-radio>
        </a-radio-group>
      </a-form-item>
      <div v-if="batchExecuteForm.runMode == 'SERIAL'" class="ms-switch">
        <a-switch
          v-model="batchExecuteForm.stopOnFailure"
          type="line"
          class="ms-form-table-input-switch execute-form-table-input-switch"
          size="small"
        />
        <span class="ml-3 font-normal text-[var(--color-text-1)]">{{ t('case.execute.StopOnFailure') }}</span>
      </div>
      <a-form-item field="integratedReport" :label="t('case.execute.reportSetting')">
        <a-radio-group v-model="batchExecuteForm.integratedReport" type="button">
          <a-radio value="false">{{ t('case.execute.independentReporting') }}</a-radio>
          <a-radio value="true">{{ t('case.execute.CollectionReport') }}</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item
        v-if="batchExecuteForm.integratedReport == 'true'"
        field="integratedReportName"
        :label="t('case.execute.reportName')"
        :rules="[{ required: true, message: t('apiTestManagement.reportNameRequired') }]"
        :validate-trigger="['blur', 'input']"
        asterisk-position="end"
      >
        <a-input
          v-model="batchExecuteForm.integratedReportName"
          :max-length="255"
          :placeholder="t('formCreate.PleaseEnter')"
        />
      </a-form-item>
      <a-form-item
        field="poolId"
        :label="t('case.execute.pool')"
        asterisk-position="end"
        :rules="[{ required: true, message: t('apiTestManagement.poolRequired') }]"
      >
        <a-select v-model="batchExecuteForm.poolId" :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of resourcePoolList" :key="item.id" :value="item.id">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <template #footer>
      <a-button type="secondary" :disabled="batchExecuteLoading" @click="cancelBatchExecute">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="primary" :loading="batchExecuteLoading" @click="handleBatchExecuteCase">
        {{ t('system.log.operateType.execute') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import { BatchActionQueryParams } from '@/components/pure/ms-table/type';
  import MsRichMessage from '@/components/business/ms-rich-message/index.vue';

  import { getEnvList } from '@/api/modules/api-test/common';
  import { getPoolId, getPoolOption } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useGlobalStore from '@/store/modules/global';
  import { getGenerateId } from '@/utils';

  import { Environment } from '@/models/apiTest/management';
  import { ResourcePoolItem } from '@/models/setting/resourcePool';
  import { GlobalEventNameEnum } from '@/enums/commonEnum';
  import { TaskCenterEnum } from '@/enums/taskCenter';

  const globalStore = useGlobalStore();
  const { t } = useI18n();
  const batchExecuteFormRef = ref<FormInstance>();
  const batchExecuteForm = ref({
    defaultEnv: 'true',
    runMode: 'SERIAL',
    integratedReport: 'false',
    integratedReportName: '',
    stopOnFailure: false,
    poolId: '',
    grouped: false,
    environmentId: '',
  });

  const environmentList = ref<Environment[]>();
  const resourcePoolList = ref<ResourcePoolItem[]>();
  const defaultPoolId = ref<string>();
  const appStore = useAppStore();
  const showBatchExecute = ref(false);

  const props = withDefaults(
    defineProps<{
      tableSelected: (string | number)[];
      visible: boolean;
      batchParams?: BatchActionQueryParams;
      batchConditionParams: any;
      batchRunFunc: (a: any) => Promise<any>;
    }>(),
    {
      visible: false,
    }
  );

  // 初始化资源池列表
  async function initPoolList() {
    resourcePoolList.value = await getPoolOption(appStore.currentProjectId);
  }

  // 获取项目配置的默认资源池
  async function getDefaultPoolId() {
    try {
      defaultPoolId.value = await getPoolId(appStore.currentProjectId);
      batchExecuteForm.value.poolId = defaultPoolId.value || '';
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 初始化环境列表
  async function initEnvList() {
    environmentList.value = await getEnvList(appStore.currentProjectId);
  }

  const batchExecuteLoading = ref(false);

  const emit = defineEmits(['update:visible', 'finished']);

  function cancelBatchExecute() {
    showBatchExecute.value = false;
  }

  function resetBatchExecuteForm() {
    batchExecuteForm.value = {
      defaultEnv: 'true',
      runMode: 'SERIAL',
      integratedReport: 'false',
      integratedReportName: '',
      stopOnFailure: false,
      poolId: '',
      grouped: false,
      environmentId: '',
    };
  }

  function handleBatchExecuteCase() {
    batchExecuteFormRef.value?.validate(async (errors) => {
      const { batchParams } = props;
      const { batchConditionParams } = props;
      if (!errors) {
        try {
          batchExecuteLoading.value = true;
          await props?.batchRunFunc({
            selectIds: batchParams?.selectedIds || [],
            selectAll: !!batchParams?.selectAll,
            excludeIds: batchParams?.excludeIds || [],
            projectId: appStore.currentProjectId,
            ...batchConditionParams,
            runModeConfig: {
              runMode: batchExecuteForm.value.runMode,
              integratedReport: batchExecuteForm.value.integratedReport === 'true',
              integratedReportName: batchExecuteForm.value.integratedReportName,
              stopOnFailure: batchExecuteForm.value.stopOnFailure,
              poolId: batchExecuteForm.value.poolId,
              grouped: batchExecuteForm.value.grouped,
              environmentId: batchExecuteForm.value.environmentId,
            },
            apiDefinitionId: '',
            versionId: '',
            refId: '',
          });
          Message.success({
            content: () =>
              h(
                'div',
                {
                  style: {
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                  },
                },
                [
                  h(MsRichMessage, {
                    content: t('case.detail.execute.success'),
                    onGoDetail() {
                      globalStore.dispatchGlobalEvent({
                        id: getGenerateId(),
                        name: GlobalEventNameEnum.OPEN_TASK_CENTER,
                        params: {
                          tab: TaskCenterEnum.DETAIL,
                        },
                      });
                    },
                  }),
                ]
              ),
            duration: 5000,
            closable: true,
          });
          cancelBatchExecute();
          resetBatchExecuteForm();
          emit('finished');
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          resetBatchExecuteForm();
          batchExecuteLoading.value = false;
        }
      }
    });
  }

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        showBatchExecute.value = true;
        initEnvList();
        initPoolList();
        getDefaultPoolId();
      }
    },
    {
      immediate: true,
    }
  );

  watch(
    () => showBatchExecute.value,
    (val) => {
      emit('update:visible', val);
    }
  );
</script>

<style scoped lang="less">
  :deep(.arco-radio-group) {
    margin-left: -5px;
  }
  .ms-switch {
    display: flex;
    align-items: center;
    flex-direction: row;
    margin-bottom: 16px;
  }
</style>
