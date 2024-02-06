<template>
  <div class="page">
    <template v-if="store.currentGroupId">
      <div class="header">
        <a-form ref="envGroupForm" layout="vertical" :model="form">
          <a-form-item
            class="mb-[16px]"
            asterisk-position="end"
            field="name"
            :label="t('project.environmental.group.envGroupName')"
            :rules="[{ required: true, message: t('project.environmental.group.envGroupNameIsRequire') }]"
          >
            <a-input
              v-model="form.name"
              :max-length="255"
              class="w-[732px]"
              :placeholder="t('project.environmental.group.envGroupPlaceholder')"
            />
          </a-form-item>
          <a-form-item
            class="mb-[16px]"
            asterisk-position="end"
            field="description"
            :label="t('project.environmental.group.desc')"
          >
            <a-textarea
              v-model="form.description"
              :max-length="1000"
              auto-size
              class="w-[732px]"
              :placeholder="t('common.pleaseInput')"
            />
          </a-form-item>
        </a-form>
        <paramsTable
          v-model:params="innerParams"
          :show-setting="false"
          :columns="columns"
          @change="handleParamTableChange"
        />
      </div>
      <div class="footer" :style="{ width: '100%' }">
        <a-button :disabled="!canSave" @click="handleReset">{{ t('common.cancel') }}</a-button>
        <a-button :disabled="!canSave" type="primary" @click="handleSave">{{ t('common.save') }}</a-button>
      </div>
    </template>
    <template v-else>
      <div class="flex h-[400px] items-center justify-center">
        <a-empty :description="t('common.noData')" />
      </div>
    </template>
  </div>
</template>

<script lang="ts" setup>
  import paramsTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { groupProjectEnv, listEnv } from '@/api/modules/project-management/envManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  import { EnvListItem, ProjectOptionItem } from '@/models/projectManagement/environmental';

  const { t } = useI18n();

  const envGroupForm = ref();
  const form = reactive({
    name: '',
    description: '',
  });
  const store = useProjectEnvStore();
  const appStore = useAppStore();

  const sourceProjectOptions = ref<ProjectOptionItem[]>([]);
  const projectOptions = computed(() => {
    return sourceProjectOptions.value;
  });
  const environmentOptions = ref<EnvListItem[]>([]);

  const columns = computed<ParamTableColumn[]>(() => [
    {
      title: 'project.environmental.project',
      dataIndex: 'projectId',
      slotName: 'project',
      options: projectOptions.value,
    },
    {
      title: 'project.environmental.env',
      dataIndex: 'environmentId',
      slotName: 'environment',
      options: environmentOptions.value,
    },
    {
      title: 'project.environmental.host',
      dataIndex: 'host',
      slotName: 'host',
    },
    {
      title: 'project.environmental.desc',
      dataIndex: 'desc',
      slotName: 'desc',
    },
    {
      title: '',
      slotName: 'operation',
      width: 50,
    },
  ]);

  const innerParams = ref<any[]>([]);

  const canSave = ref(true);

  const handleReset = () => {
    envGroupForm.value?.resetFields();
  };

  const handleSave = () => {
    envGroupForm.value?.validate(async (valid) => {
      if (valid) {
        console.log('form', form);
      }
    });
  };
  // 获取项目的options
  const initProjectOptions = async () => {
    const res = await groupProjectEnv();
    sourceProjectOptions.value = res;
  };
  // 获取环境的options
  const initEnvOptions = async () => {
    const res = await listEnv({ projectId: appStore.currentProjectId, keyword: '' });
    environmentOptions.value = res;
  };

  function handleParamTableChange(resultArr: any[]) {
    innerParams.value = [...resultArr];
  }
  onMounted(() => {
    initProjectOptions();
    initEnvOptions();
  });
</script>

<style lang="less" scoped>
  .page {
    transform: scale3d(1, 1, 1);
    .header {
      padding: 24px 24px 0;
    }
    .footer {
      gap: 16px;
      position: fixed;
      right: 0;
      bottom: 0;
      z-index: 999;
      display: flex;
      justify-content: flex-end;
      padding: 24px;
      box-shadow: 0 -1px 4px rgb(2 2 2 / 10%);
    }
  }
</style>
