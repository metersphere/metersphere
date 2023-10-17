<template>
  <MsDrawer
    v-model:visible="currentVisible"
    :title="t('project.menu.BUG_SYNC')"
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
        >
        </a-select>
      </a-form-item>
      <!-- jira start -->
      <a-form-item field="projectKey" :label="t('project.menu.projectKey')">
        <a-input v-model="form.projectKey" :placeholder="t('project.menu.pleaseInputJiraKey')" />
        <template #extra>
          <div class="flex flex-row items-center gap-[4px]">
            <span class="text-[var(--color-text-4)]">{{ t('project.menu.howGetJiraKey') }}</span>
            <span class="cursor-pointer text-[rgb(var(--primary-5))]">{{ t('project.menu.preview') }}</span>
          </div>
        </template>
      </a-form-item>
      <a-form-item field="defectType" :label="t('project.menu.defectType')">
        <a-select :options="jiraDefectOption" :field-names="{ value: 'id', label: 'name' }"></a-select>
      </a-form-item>
      <!-- jira end -->
      <a-form-item field="organizationId" :label="t('project.menu.organizationId')">
        <a-input v-model="form.organizationId" />
      </a-form-item>
      <a-form-item field="projectId" :label="t('project.menu.projectId')">
        <a-input v-model="form.projectId" />
      </a-form-item>
      <a-form-item field="azureId" :label="t('project.menu.azureId')">
        <a-input v-model="form.azureId" />
      </a-form-item>
      <a-form-item field="MECHANISM" :label="t('project.menu.syncMechanism')">
        <a-space>
          <a-radio-group v-model="form.MECHANISM">
            <a-radio value="increment">
              <div class="flex flex-row items-center gap-[4px]">
                <span class="text-[var(--color-text-1)]">{{ t('project.menu.MECHANISM') }}</span>
                <a-tooltip :content="t('project.menu.MECHANISM_TIP')" position="right">
                  <div>
                    <MsIcon class="ml-[4px] text-[var(--color-text-4)]" type="icon-icon-maybe_outlined" />
                  </div>
                </a-tooltip>
              </div>
            </a-radio>
            <a-radio value="full">
              <div class="flex flex-row items-center gap-[4px]">
                <span class="text-[var(--color-text-1)]">{{ t('project.menu.MECHANISM') }}</span>
                <a-tooltip :content="t('project.menu.MECHANISM_TIP')" position="right">
                  <div>
                    <MsIcon class="ml-[4px] text-[var(--color-text-4)]" type="icon-icon-maybe_outlined" />
                  </div>
                </a-tooltip>
              </div>
            </a-radio>
          </a-radio-group>
        </a-space>
      </a-form-item>
      <a-form-item field="CRON_EXPRESSION" :label="t('project.menu.CRON_EXPRESSION')">
        <a-select v-model="form.CRON_EXPRESSION">
          <a-option v-for="data in frequencyOption" :key="data.value" :value="data.value">
            <span class="text-[var(--color-text-1)]">
              {{ data.label }}
            </span>
            <span v-if="data.extra" class="text-[var(--color-text-4)]"> {{ data.extra }} </span>
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
        <a-tooltip :content="t('project.menu.API_ERROR_REPORT_RULE_TIP')" position="right">
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

  import { getPlatformOptions, postSaveDefectSync } from '@/api/modules/project-management/menuManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { PoolOption } from '@/models/projectManagement/menuManagement';
  import { MenuEnum } from '@/enums/commonEnum';

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
  const jiraDefectOption = ref<PoolOption[]>([]);

  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const currentOrgId = computed(() => appStore.currentOrgId);
  const platformDisabled = computed(() => platformOption.value.length === 0);
  const okLoading = ref(false);

  const formRef = ref<FormInstance>();

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
      console.log(error);
    }
  };

  watch(
    () => props.visible,
    (val) => {
      currentVisible.value = val;
      if (val) {
        initPlatformOption();
      }
    }
  );
</script>
