<template>
  <MsDrawer
    v-model:visible="currentVisible"
    :title="t('project.menu.CASE_RELATED')"
    :destroy-on-close="true"
    :closable="true"
    :mask-closable="false"
    :get-container="false"
    :body-style="{ padding: '0px' }"
    :width="680"
    :ok-loading="okLoading"
    :ok-disabled="okDisabled"
    @cancel="handleCancel(false)"
    @confirm="handleConfirm"
  >
    <a-form ref="formRef" class="rounded-[4px]" :model="form" layout="vertical">
      <a-form-item field="platformKey" :label="t('project.menu.platformLabel')">
        <a-select
          v-model="form.platformKey"
          allow-clear
          :disabled="platformDisabled"
          :options="platformOption"
          :placeholder="platformDisabled ? t('project.menu.platformPlaceholder') : ''"
          :field-names="{ label: 'name', value: 'id' }"
          @change="handlePlatformChange"
        >
        </a-select>
      </a-form-item>
      <!-- form-create -->
      <MsFormCreate
        v-if="platformRules && platformRules.length"
        :form-rule="platformRules"
        :form-create-key="FormCreateKeyEnum.PROJECT_DEFECT_SYNC_TEMPLATE"
      />
      <!-- 同步机制 -->
      <a-form-item field="MECHANISM" :label="t('project.menu.syncMechanism')">
        <a-space>
          <a-radio-group v-model="form.MECHANISM">
            <a-radio value="increment">
              <div class="flex flex-row items-center gap-[4px]">
                <span class="text-[var(--color-text-1)]">{{ t('project.menu.incrementalSync') }}</span>
                <a-tooltip :content="t('project.menu.incrementalSyncTip')" position="top">
                  <div>
                    <MsIcon class="ml-[4px] text-[var(--color-text-4)]" type="icon-icon-maybe_outlined" />
                  </div>
                </a-tooltip>
              </div>
            </a-radio>
            <a-radio value="full">
              <div class="flex flex-row items-center gap-[4px]">
                <span class="text-[var(--color-text-1)]">{{ t('project.menu.fullSync') }}</span>
                <a-tooltip :content="t('project.menu.fullSyncTip')" position="bl">
                  <div>
                    <MsIcon class="ml-[4px] text-[var(--color-text-4)]" type="icon-icon-maybe_outlined" />
                  </div>
                </a-tooltip>
              </div>
            </a-radio>
          </a-radio-group>
        </a-space>
      </a-form-item>
      <!-- 同步频率 -->
      <a-form-item field="CRON_EXPRESSION" :label="t('project.menu.CRON_EXPRESSION')">
        <a-select v-model="form.CRON_EXPRESSION">
          <a-option v-for="data in frequencyOption" :key="data.value" :value="data.value">
            <span class="text-[var(--color-text-1)]">
              {{ data.label }}
            </span>
            <span v-if="data.extra" class="text-[var(--color-text-4)]"> {{ data.extra }} </span>
          </a-option>
          <a-option value="custom">
            <div class="border-t-1 cursor-pointer text-[rgb(var(--primary-5))]">{{
              t('project.menu.defect.customLabel')
            }}</div>
          </a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <template #footerLeft>
      <div class="flex flex-row items-center gap-[4px]">
        <a-switch size="small" />
        <span class="text-[var(--color-text-1)]">
          {{ t('project.menu.status') }}
        </span>
        <a-tooltip position="tl" :content-style="{ maxWidth: '500px' }">
          <template #content>
            <div class="flex flex-col">
              <div>{{ t('project.menu.defect.enableTip') }}</div>
              <div class="flex flex-nowrap">{{ t('project.menu.defect.closeTip') }}</div>
            </div>
          </template>
          <div>
            <MsIcon class="ml-[4px] text-[var(--color-text-4)]" type="icon-icon-maybe_outlined" />
          </div>
        </a-tooltip>
      </div>
    </template>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/form-create.vue';
  import type { FormItem } from '@/components/pure/ms-form-create/types';

  import {
    getPlatformInfo,
    getPlatformOptions,
    postSaveDefectSync,
  } from '@/api/modules/project-management/menuManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { PoolOption, SelectValue } from '@/models/projectManagement/menuManagement';
  import { MenuEnum } from '@/enums/commonEnum';
  import { FormCreateKeyEnum } from '@/enums/formCreateEnum';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
  }>();
  const currentVisible = ref<boolean>(props.visible);
  const platformOption = ref<PoolOption[]>([]);
  const frequencyOption = ref([
    { label: '0 0 0/1 * * ?', extra: '（每隔1小时）', value: '1H' },
    { label: '0 0 0/6 * * ?', extra: '（每隔6小时）', value: '6H' },
    { label: '0 0 0/12 * * ?', extra: '（每隔12小时）', value: '12H' },
    { label: '0 0 0 * * ?', extra: '（每隔一天）', value: '1D' },
  ]);

  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const currentOrgId = computed(() => appStore.currentOrgId);
  const platformDisabled = computed(() => platformOption.value.length === 0);
  const okLoading = ref(false);

  const formRef = ref<FormInstance>();
  const platformRules = ref<FormItem[]>([]);

  const form = reactive({
    platformKey: '',
    MECHANISM: '', // 同步机制
    SYNC_ENABLE: 'false', // 同步开关
    CRON_EXPRESSION: '', // 同步频率
    organizationId: '',
    projectKey: '',
    projectId: '',
    azureId: '',
    bugType: '',
  });

  const okDisabled = computed(() => !form.platformKey);

  const emit = defineEmits<{
    (e: 'cancel', shouldSearch: boolean): void;
  }>();

  const handleCancel = (shouldSearch: boolean) => {
    emit('cancel', shouldSearch);
  };
  const handlePlatformChange = async (value: SelectValue) => {
    try {
      if (value) {
        const res = await getPlatformInfo(value as string, MenuEnum.bugManagement);
        platformRules.value = res.formItems;
      } else {
        platformRules.value = [];
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  const handleConfirm = async () => {
    await formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      try {
        okLoading.value = true;
        await postSaveDefectSync(form, currentProjectId.value);
        Message.success(t('common.createSuccess'));
        handleCancel(true);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
      } finally {
        okLoading.value = false;
      }
    });
  };

  const initPlatformOption = async () => {
    try {
      const res = await getPlatformOptions(currentOrgId.value, MenuEnum.bugManagement);
      if (res) {
        platformOption.value = res;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  watch(
    () => props.visible,
    (val) => {
      currentVisible.value = val;
      if (val) {
        initPlatformOption();
      } else {
        formRef.value?.resetFields();
        platformRules.value = [];
      }
    }
  );
</script>
