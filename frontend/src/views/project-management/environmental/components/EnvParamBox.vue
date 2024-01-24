<template>
  <div class="page">
    <div class="header">
      <a-form ref="envForm" layout="vertical" :model="form">
        <a-form-item
          class="mb-[16px]"
          asterisk-position="end"
          field="name"
          :label="t('project.environmental.envName')"
          :rules="[{ required: true, message: t('project.environmental.envNameRequired') }]"
        >
          <a-input
            v-model="form.name"
            show-word-limit
            :max-length="255"
            class="w-[732px]"
            :placeholder="t('project.environmental.envNamePlaceholder')"
            @blur="store.currentEnvDetailInfo.name = form.name"
          />
        </a-form-item>
      </a-form>
      <a-tabs v-model:active-key="activeKey" class="no-content">
        <a-tab-pane v-for="item of contentTabList" :key="item.value" :title="item.label" />
      </a-tabs>
    </div>
    <a-divider :margin="0" class="!mb-[16px]" />
    <div class="px-[24px]">
      <EnvParamsTab v-if="activeKey === 'envParams'" />
      <HttpTab v-else-if="activeKey === 'http'" />
      <DataBaseTab v-else-if="activeKey === 'database'" />
      <HostTab v-else-if="activeKey === 'host'" />
      <TcpTab v-else-if="activeKey === 'tcp'" />
      <PreTab v-else-if="activeKey === 'pre'" />
      <PostTab v-else-if="activeKey === 'post'" />
      <AssertTab v-else-if="activeKey === 'assert'" />
    </div>

    <div class="footer" :style="{ width: '100%' }">
      <a-button @click="handleReset">{{ t('common.cancel') }}</a-button>
      <a-button :disabled="!canSave" type="primary" @click="handleSave">{{ t('common.save') }}</a-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { Message } from '@arco-design/web-vue';
  import { isEqual } from 'lodash-es';

  import AssertTab from './envParams/AssertTab.vue';
  import DataBaseTab from './envParams/DatabaseTab.vue';
  import EnvParamsTab from './envParams/EnvParamsTab.vue';
  import HostTab from './envParams/HostTab.vue';
  import HttpTab from './envParams/HttpTab.vue';
  import PostTab from './envParams/PostTab.vue';
  import PreTab from './envParams/PreTab.vue';
  import TcpTab from './envParams/TcpTab.vue';

  import { updateOrAddEnv } from '@/api/modules/project-management/envManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  const activeKey = ref('assert');
  const envForm = ref();
  const canSave = ref(false);
  const { t } = useI18n();
  const loading = ref(false);

  const store = useProjectEnvStore();

  const form = reactive({
    name: '',
  });

  const contentTabList = [
    {
      value: 'envParams',
      label: t('project.environmental.envParams'),
    },
    {
      value: 'http',
      label: 'HTTP',
    },
    {
      value: 'database',
      label: t('project.environmental.database'),
    },
    {
      value: 'host',
      label: 'Host',
    },
    {
      value: 'tcp',
      label: 'TCP',
    },
    {
      value: 'pre',
      label: t('project.environmental.pre'),
    },
    {
      value: 'post',
      label: t('project.environmental.post'),
    },
    {
      value: 'assert',
      label: t('project.environmental.assert'),
    },
    {
      value: 'display',
      label: t('project.environmental.displaySetting'),
    },
  ];
  const handleReset = () => {
    envForm.value?.resetFields();
    store.initEnvDetail();
  };
  const handleSave = async () => {
    await envForm.value?.validate(async (valid) => {
      if (!valid) {
        try {
          loading.value = true;
          store.currentEnvDetailInfo.mock = true;
          const res = await updateOrAddEnv({ fileList: [], request: store.currentEnvDetailInfo });
          store.currentEnvDetailInfo = res;
          Message.success(t('common.saveSuccess'));
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          loading.value = false;
        }
      }
    });
  };
  watchEffect(() => {
    if (store.currentId) {
      store.initEnvDetail();
    }
  });

  watchEffect(() => {
    if (store.currentEnvDetailInfo) {
      const { currentEnvDetailInfo } = store;
      form.name = currentEnvDetailInfo.name;
    }
  });
  watchEffect(() => {
    if (store.currentEnvDetailInfo) {
      const { currentEnvDetailInfo, backupEnvDetailInfo } = store;
      canSave.value = !isEqual(currentEnvDetailInfo, backupEnvDetailInfo);
    }
  });
</script>

<style lang="less" scoped>
  .page {
    transform: scale3d(1, 1, 1);
    .header {
      padding: 24px 24px 0;
    }
    .no-content {
      :deep(.arco-tabs-content) {
        padding-top: 0;
      }
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
