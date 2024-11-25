<template>
  <a-modal
    v-model:visible="showModalVisible"
    class="ms-modal-form ms-modal-small"
    title-align="start"
    :mask-closable="false"
    @close="handleCancel"
  >
    <template #title>
      {{
        props.taskConfig || props.isBatch
          ? t(
              props.isBatch
                ? 'testPlan.testPlanIndex.batchUpdateScheduledTask'
                : 'testPlan.testPlanIndex.updateScheduledTask'
            )
          : t('testPlan.testPlanIndex.createScheduledTask')
      }}
      <div v-if="isBatch" class="float-right flex text-[var(--color-text-4)]">
        {{
          t('common.selectedCount', {
            count: props.batchParams?.currentSelectCount || props.batchParams?.selectedIds?.length,
          })
        }}
      </div>
    </template>
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item :label="t('testPlan.testPlanIndex.triggerTime')" asterisk-position="end" class="mb-0">
        <MsCronSelect v-model:model-value="form.cron" />
      </a-form-item>
      <!-- TOTO 环境暂时不上 -->
      <!-- <a-radio-group v-model="form.env" class="mb-4">
        <a-radio value="">
          {{ t('testPlan.testPlanIndex.defaultEnv') }}
          <span class="float-right mx-1 mt-[1px]">
            <a-tooltip :content="t('testPlan.testPlanIndex.envTip')" position="top">
              <IconQuestionCircle class="h-[16px] w-[16px] text-[--color-text-4] hover:text-[rgb(var(--primary-5))]"
            /></a-tooltip>
          </span>
        </a-radio>
        <a-radio value="new"> {{ t('testPlan.testPlanIndex.newEnv') }}</a-radio>
      </a-radio-group> -->
      <a-radio-group v-if="props.type === testPlanTypeEnum.GROUP" v-model="form.runConfig.runMode">
        <a-radio value="SERIAL">{{ t('testPlan.testPlanIndex.serial') }}</a-radio>
        <a-radio value="PARALLEL">{{ t('testPlan.testPlanIndex.parallel') }}</a-radio>
      </a-radio-group>
      <!-- TODO 资源池暂时不做 -->
      <!-- <a-form-item :label="t('testPlan.testPlanIndex.resourcePool')" asterisk-position="end" class="mb-0">
        <a-select
          v-model="form.resourcePoolIds"
          :placeholder="t('common.pleaseSelect')"
          :field-names="{ value: 'id', label: 'name' }"
        >
          <template #label="{ data }">
            {{ data }}
          </template>
          <a-option v-for="item of resourcesList" :key="item.id" :value="item.id">
            <div class="flex w-full items-center justify-between">
              <div>
                {{ item.name }}
              </div>
              <div class="flex items-center">
                <span class="text-[var(--color-text-4)]">CPU</span>
                <span class="mx-2"> {{ item.cpuRate }}</span>
                <MsTag theme="outline" :type="item.status ? 'link' : 'success'" size="small">
                  {{ item.status ? t('testPlan.testPlanIndex.doing') : t('testPlan.testPlanIndex.inFreeTime') }}
                </MsTag>
              </div>
            </div>
          </a-option>
        </a-select>
      </a-form-item> -->
    </a-form>
    <template #footer>
      <div class="flex items-center justify-between">
        <div class="flex flex-row items-center justify-center">
          <a-switch v-model="form.enable" size="small" type="line" />
          <span class="ml-2">{{ t('testPlan.testPlanIndex.timingState') }}</span>
          <a-tooltip size="mini" position="top">
            <template #content>
              <div>{{ t('testPlan.testPlanIndex.timingStateEnable') }}</div>
              <div>{{ t('testPlan.testPlanIndex.timingStateClose') }}</div>
            </template>
            <div class="mx-1 flex items-center">
              <span class="mt-[2px]">
                <IconQuestionCircle
                  class="h-[16px] w-[16px] text-[--color-text-4] hover:text-[rgb(var(--primary-5))]"
                />
              </span>
            </div>
          </a-tooltip>
        </div>
        <div>
          <a-button type="secondary" class="mr-3" @click="handleCancel">{{ t('system.plugin.pluginCancel') }}</a-button>
          <a-button type="primary" :loading="confirmLoading" @click="handleCreate">
            {{ props.taskConfig || props.isBatch ? t('common.update') : t('common.create') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';
  import { type FormInstance, Message, type ValidatedError } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsCronSelect from '@/components/pure/ms-cron-select/index.vue';
  import { BatchActionQueryParams } from '@/components/pure/ms-table/type';

  import { batchConfigSchedule, configSchedule } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { CreateTask } from '@/models/testPlan/testPlan';
  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    taskConfig?: CreateTask;
    type: keyof typeof testPlanTypeEnum;
    sourceId?: string;
    isBatch?: boolean;
    batchParams?: BatchActionQueryParams;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'close'): void;
    (e: 'handleSuccess'): void;
  }>();

  const appStore = useAppStore();

  const showModalVisible = useVModel(props, 'visible', emit);

  const initForm: CreateTask = {
    resourceId: '',
    cron: '',
    enable: true,
    runConfig: { runMode: 'SERIAL' },
  };

  const form = ref<CreateTask>(cloneDeep(initForm));

  const confirmLoading = ref<boolean>(false);
  const formRef = ref<FormInstance | null>(null);

  function handleCancel() {
    showModalVisible.value = false;
    formRef.value?.resetFields();
    form.value = cloneDeep(initForm);
  }

  function handleCreate() {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        confirmLoading.value = true;
        try {
          if (props.isBatch && props.batchParams) {
            await batchConfigSchedule({
              ...props.batchParams,
              ...form.value,
              selectIds: props.batchParams.selectedIds,
              projectId: appStore.currentProjectId,
            });
          } else if (props.sourceId) {
            const params = {
              ...form.value,
              resourceId: props.sourceId,
            };
            await configSchedule(params);
          }
          handleCancel();
          emit('handleSuccess');
          Message.success(props.taskConfig || props.isBatch ? t('common.updateSuccess') : t('common.createSuccess'));
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          confirmLoading.value = false;
        }
      }
    });
  }

  watch(
    () => props.taskConfig,
    (val) => {
      if (val) {
        form.value = cloneDeep(val);
      }
    }
  );
</script>

<style scoped lang="less">
  :deep(.arco-select-option-content) {
    @apply w-full;
  }
</style>
