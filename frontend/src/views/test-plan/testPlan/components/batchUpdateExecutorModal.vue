<template>
  <a-modal
    v-model:visible="visible"
    title-align="start"
    body-class="p-0"
    :cancel-button-props="{ disabled: batchLoading }"
    :ok-loading="batchLoading"
    :ok-button-props="{ disabled: batchUpdateExecutorDisabled }"
    :ok-text="t('common.update')"
    @before-ok="handleBatchUpdateExecutor"
    @close="resetBatchForm"
  >
    <template #title>
      {{ t('testPlan.featureCase.batchChangeExecutor') }}
      <div class="ml-1 text-[var(--color-text-4)]">
        {{
          t('testPlan.testPlanIndex.selectedCount', {
            count: props.count,
          })
        }}
      </div>
    </template>
    <a-form ref="batchUpdateExecutorFormRef" :model="batchUpdateExecutorForm" layout="vertical">
      <a-form-item
        field="userId"
        :label="t('testPlan.featureCase.executor')"
        :rules="[{ required: true, message: t('testPlan.featureCase.requestExecutorRequired') }]"
        asterisk-position="end"
        class="mb-0"
      >
        <MsSelect
          v-model:modelValue="batchUpdateExecutorForm.userId"
          mode="static"
          :placeholder="t('common.pleaseSelect')"
          :loading="executorLoading"
          :options="userOptions"
          :search-keys="['label']"
          allow-search
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
  import { FormInstance, Message, SelectOptionData } from '@arco-design/web-vue';

  import MsSelect from '@/components/business/ms-select';

  import { GetTestPlanUsers } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ReviewUserItem } from '@/models/caseManagement/caseReview';
  import type { BatchUpdateApiCaseExecutorParams, BatchUpdateCaseExecutorParams } from '@/models/testPlan/testPlan';

  const props = defineProps<{
    count: number;
    params?: BatchUpdateCaseExecutorParams | BatchUpdateApiCaseExecutorParams;
    batchUpdateExecutor: (...args: any) => Promise<any>; // 更新执行人接口
  }>();

  const emit = defineEmits<{
    (e: 'loadList'): void;
  }>();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });

  const { t } = useI18n();
  const appStore = useAppStore();

  const batchUpdateExecutorFormRef = ref<FormInstance>();
  const batchLoading = ref(false);
  const batchUpdateExecutorForm = ref<{ userId: string }>({ userId: '' });
  const batchUpdateExecutorDisabled = computed(() => !batchUpdateExecutorForm.value.userId.length);
  const userOptions = ref<SelectOptionData[]>([]);
  const executorLoading = ref(false);
  async function initUserOptions() {
    try {
      executorLoading.value = true;
      const res = await GetTestPlanUsers(appStore.currentProjectId, '');
      userOptions.value = res.map((e: ReviewUserItem) => ({ label: e.name, value: e.id }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      executorLoading.value = false;
    }
  }

  async function handleBatchUpdateExecutor(done: (closed: boolean) => void) {
    batchUpdateExecutorFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          batchLoading.value = true;
          await props.batchUpdateExecutor({
            ...props.params,
            ...batchUpdateExecutorForm.value,
          });
          Message.success(t('common.updateSuccess'));
          emit('loadList');
          done(true);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
          done(false);
        } finally {
          batchLoading.value = false;
          done(false);
        }
      }
    });
  }

  function resetBatchForm() {
    batchUpdateExecutorForm.value = { userId: '' };
  }

  onBeforeMount(() => {
    initUserOptions();
  });
</script>
