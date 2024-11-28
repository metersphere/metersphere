<template>
  <div class="page">
    <template v-if="store.currentGroupId">
      <div class="header">
        <a-form
          ref="envGroupForm"
          :disabled="!hasAnyPermission(['PROJECT_ENVIRONMENT:READ+UPDATE'])"
          layout="vertical"
          :model="form"
        >
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
          <a-form-item class="mb-[16px]" asterisk-position="end" field="description" :label="t('common.desc')">
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
          :params="innerParams"
          :show-setting="false"
          :columns="columns"
          :selectable="false"
          :default-param-item="defaultParamItem"
          @change="handleParamTableChange"
        />
      </div>
      <div v-permission="['PROJECT_ENVIRONMENT:READ+UPDATE']" class="footer" :style="{ width: '100%' }">
        <a-button :disabled="!canSave" @click="handleReset">{{ t('common.cancel') }}</a-button>
        <a-button :disabled="!canSave" :loading="loading" type="primary" @click="handleSave">
          {{ t('common.save') }}
        </a-button>
      </div>
    </template>
    <template v-else>
      <div class="flex h-[400px] items-center justify-center">
        <a-empty :description="t('common.noData')" />
      </div>
    </template>
  </div>
</template>

<script lang="ts" setup async>
  import { Message, ValidatedError } from '@arco-design/web-vue';

  import paramsTable, { type ParamTableColumn } from '@/views/api-test/components/paramTable.vue';

  import { getGroupDetailEnv, groupAddEnv, groupUpdateEnv } from '@/api/modules/project-management/envManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useProjectEnvStore, { NEW_ENV_GROUP } from '@/store/modules/setting/useProjectEnvStore';
  import { hasAnyPermission } from '@/utils/permission';

  import { EnvListItem } from '@/models/projectManagement/environmental';

  const { t } = useI18n();
  const appStore = useAppStore();

  const envGroupForm = ref();
  const emit = defineEmits<{
    (e: 'saveOrUpdate', id: string): void;
  }>();
  const form = reactive({
    name: '',
    description: '',
  });
  const store = useProjectEnvStore();

  const defaultParamItem = {
    projectId: '',
    environmentId: '',
    host: '',
    description: '',
    enable: true,
  };
  const columns = computed<ParamTableColumn[]>(() => [
    {
      title: 'project.environmental.project',
      dataIndex: 'projectId',
      slotName: 'project',
      width: 200,
    },
    {
      title: 'project.environmental.env',
      dataIndex: 'environmentId',
      slotName: 'environment',
      width: 300,
    },
    {
      title: 'project.environmental.host',
      dataIndex: 'host',
      slotName: 'host',
      showTooltip: true,
      isTag: true,
      width: 456,
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      slotName: 'description',
    },
    {
      title: '',
      slotName: 'operation',
      width: 50,
    },
  ]);

  const innerParams = ref<Record<string, any>[]>([]);

  const canSave = ref(true);

  const handleReset = () => {
    envGroupForm.value?.resetFields();
    innerParams.value = [];
    emit('saveOrUpdate', '');
  };

  const initDetail = async (id: string) => {
    if (id === NEW_ENV_GROUP) {
      form.name = '';
      form.description = '';
      innerParams.value = [];
      return;
    }
    const detail = await getGroupDetailEnv(id);
    if (detail) {
      form.name = detail.name;
      form.description = detail.description;
      innerParams.value = detail.environmentGroupInfo || [];
    }
  };

  const loading = ref<boolean>(false);
  const handleSave = () => {
    envGroupForm.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      loading.value = true;
      try {
        const id = store.currentGroupId === NEW_ENV_GROUP ? undefined : store.currentGroupId;
        const envGroupProject = innerParams.value.filter((item) => item.projectId && item.environmentId);
        const params = {
          id,
          name: form.name,
          description: form.description,
          projectId: appStore.currentProjectId,
          envGroupProject,
        };
        let res: EnvListItem;
        if (id) {
          res = await groupUpdateEnv(params);
          Message.success(t('common.saveSuccess'));
          initDetail(res.id);
        } else {
          res = await groupAddEnv(params);
        }
        emit('saveOrUpdate', res.id);
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error(e);
      } finally {
        loading.value = false;
      }
    });
  };

  function handleParamTableChange(resultArr: any[]) {
    innerParams.value = [...resultArr];
  }
  watchEffect(() => {
    if (store.currentGroupId) {
      initDetail(store.currentGroupId);
    }
  });

  defineExpose({
    initDetail,
  });
</script>

<style lang="less" scoped>
  .page {
    position: relative;
    transform: scale3d(1, 1, 1);
    height: 100%;
    .header {
      overflow-y: auto;
      padding: 16px 16px 0;
      max-height: calc(100% - 100px);
      background-color: var(--color-text-fff);
    }
    .footer {
      position: fixed;
      right: 0;
      bottom: 0;
      z-index: 999;
      display: flex;
      justify-content: flex-end;
      padding: 16px;
      background-color: var(--color-text-fff);
      box-shadow: 0 -1px 4px rgb(2 2 2 / 10%);
      gap: 16px;
    }
  }
</style>
