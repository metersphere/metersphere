<template>
  <div>
    <MsDrawer
      v-model:visible="visible"
      :width="960"
      :title="t('apiTestManagement.importApi')"
      :closable="false"
      :ok-disabled="disabledConfirm"
      :ok-text="t('common.import')"
      :ok-loading="importLoading"
      disabled-width-drag
      desc
      @confirm="confirmImport"
      @cancel="cancelImport"
    >
      <div v-if="importType === 'file'" class="mb-[16px] flex items-center gap-[16px]">
        <div
          v-for="item of platformList"
          :key="item.value"
          :class="`import-item ${importForm.platform === item.value ? 'import-item--active' : ''}`"
          @click="() => setActiveImportFormat(item.value)"
        >
          <div class="text-[var(--color-text-1)]">{{ item.name }}</div>
        </div>
      </div>
      <a-form ref="importFormRef" :model="importForm" layout="vertical">
        <a-form-item
          v-if="importForm.platform === RequestImportFormat.SWAGGER"
          :label="t('apiTestManagement.importType')"
        >
          <a-radio-group v-model:model-value="importForm.type" type="button">
            <a-radio :value="RequestImportType.API">{{ t('apiTestManagement.fileImport') }}</a-radio>
            <a-radio :value="RequestImportType.SCHEDULE">{{ t('apiTestManagement.timeImport') }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <template v-if="importForm.type === RequestImportType.API">
          <a-form-item :label="t('apiTestManagement.belongModule')">
            <a-tree-select
              v-model:modelValue="importForm.moduleId"
              :data="innerModuleTree"
              class="w-[436px]"
              :field-names="{ title: 'name', key: 'id', children: 'children' }"
              :draggable="false"
              allow-search
              allow-clear
              :filter-tree-node="filterTreeNode"
              :fallback-option="(key: string | number) => ({
                name: t('apiTestManagement.moduleNotExist'),
                key,
                disabled: true,
              })"
            >
              <template #tree-slot-title="node">
                <a-tooltip :content="`${node.name}`" position="tl">
                  <div class="one-line-text w-[300px]">{{ node.name }}</div>
                </a-tooltip>
              </template>
            </a-tree-select>
          </a-form-item>
          <a-form-item :label="t('apiTestManagement.importMode')">
            <a-radio-group v-model:model-value="importForm.coverData">
              <a-radio :value="true">
                <div class="flex items-center gap-[2px]">
                  {{ t('apiTestManagement.cover') }}
                  <a-tooltip position="right">
                    <icon-question-circle
                      class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                      size="16"
                    />
                    <template #content>
                      <div>{{ t('apiTestManagement.importModeTip1') }}</div>
                      <div>{{ t('apiTestManagement.importModeTip2') }}</div>
                      <div>{{ t('apiTestManagement.importModeTip3') }}</div>
                      <div>{{ t('apiTestManagement.importModeTip4') }}</div>
                    </template>
                  </a-tooltip>
                </div>
              </a-radio>
              <a-radio :value="false">
                <div class="flex items-center gap-[2px]">
                  {{ t('apiTestManagement.uncover') }}
                  <a-tooltip position="right">
                    <icon-question-circle
                      class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                      size="16"
                    />
                    <template #content>
                      <div>{{ t('apiTestManagement.importModeTip5') }}</div>
                      <div>{{ t('apiTestManagement.importModeTip6') }}</div>
                      <div>{{ t('apiTestManagement.importModeTip7') }}</div>
                    </template>
                  </a-tooltip>
                </div>
              </a-radio>
            </a-radio-group>
          </a-form-item>
          <div v-if="importForm.coverData" class="mb-[16px] flex items-center gap-[4px]">
            <a-switch v-model:model-value="importForm.coverModule" size="small" />
            {{ t('apiTestManagement.syncUpdateDirectory') }}
          </div>
          <div
            v-if="[RequestImportFormat.MeterSphere, RequestImportFormat.Postman].includes(importForm.platform)"
            class="mb-[16px] flex items-center gap-[4px]"
          >
            <a-switch v-model:model-value="importForm.syncCase" size="small" />
            {{ t('apiTestManagement.syncImportCase') }}
          </div>
          <div
            v-if="importForm.platform === RequestImportFormat.MeterSphere"
            class="mb-[16px] flex items-center gap-[4px]"
          >
            <a-switch v-model:model-value="importForm.syncMock" size="small" />
            {{ t('apiTestManagement.syncImportMock') }}
          </div>
          <a-form-item
            v-if="importForm.platform === RequestImportFormat.SWAGGER"
            :label="t('apiTestManagement.importMethod')"
          >
            <a-radio-group v-model:model-value="importType" type="button">
              <a-radio value="file">{{ t('apiTestManagement.fileImport') }}</a-radio>
              <a-radio value="swaggerUrl">{{ t('apiTestManagement.urlImport') }}</a-radio>
            </a-radio-group>
          </a-form-item>
          <MsUpload
            v-if="importType === 'file'"
            v-model:file-list="fileList"
            :accept="uploadAccept"
            :auto-upload="false"
            draggable
            size-unit="MB"
            class="w-full"
          >
            <template #subText>
              <div v-if="importForm.platform === RequestImportFormat.SWAGGER" class="flex">
                {{ t('apiTestManagement.importSwaggerFileTip1') }}
                <span class="text-[rgb(var(--warning-6))]" @click.stop="openLink">
                  {{ t('apiTestManagement.importSwaggerFileTip2') }}
                </span>
                {{ t('apiTestManagement.importSwaggerFileTip3', { size: appStore.getFileMaxSize }) }}
              </div>
              <div v-else-if="importForm.platform === RequestImportFormat.Postman" class="flex">
                {{ t('apiTestManagement.importPostmanFileTip') }}
              </div>
            </template>
          </MsUpload>
          <template v-else>
            <a-form-item
              field="swaggerUrl"
              label="SwaggerURL"
              asterisk-position="end"
              :rules="[{ required: true, message: t('apiTestManagement.swaggerURLRequired') }]"
            >
              <a-input
                v-model:model-value="importForm.swaggerUrl"
                :placeholder="t('apiTestManagement.urlImportPlaceholder')"
                class="w-[700px]"
                allow-clear
              ></a-input>
            </a-form-item>
            <a-form-item field="token" label="token" asterisk-position="end">
              <a-textarea
                v-model:model-value="importForm.swaggerToken"
                :auto-size="{ minRows: 1 }"
                class="w-[700px]"
                allow-clear
              />
            </a-form-item>
            <div class="mb-[16px] flex items-center gap-[8px]">
              <a-switch v-model:model-value="importForm.authSwitch" type="line" size="small"></a-switch>
              {{ t('apiTestManagement.basicAuth') }}
            </div>
            <template v-if="importForm.authSwitch">
              <a-form-item
                field="authUsername"
                :label="t('apiTestManagement.account')"
                asterisk-position="end"
                :rules="[{ required: true, message: t('apiTestManagement.accountRequired') }]"
              >
                <a-input
                  v-model:model-value="importForm.authUsername"
                  :placeholder="t('common.pleaseInput')"
                  class="w-[500px]"
                  allow-clear
                ></a-input>
              </a-form-item>
              <a-form-item
                field="authPassword"
                :label="t('apiTestManagement.password')"
                asterisk-position="end"
                :rules="[{ required: true, message: t('apiTestManagement.passwordRequired') }]"
                autocomplete="new-password"
              >
                <a-input-password
                  v-model:model-value="importForm.authPassword"
                  :placeholder="t('common.pleaseInput')"
                  class="w-[500px]"
                  autocomplete="new-password"
                  allow-clear
                ></a-input-password>
              </a-form-item>
            </template>
          </template>
        </template>
        <template v-else>
          <a-form-item
            field="name"
            :label="t('apiTestManagement.taskName')"
            :rules="[{ required: true, message: t('apiTestManagement.taskNameRequired') }]"
          >
            <div class="flex w-full items-center gap-[8px]">
              <a-input
                v-model:model-value="importForm.name"
                :placeholder="t('apiTestManagement.taskNamePlaceholder')"
                :max-length="255"
                class="w-[550px]"
              ></a-input>
              <MsButton type="text" @click="emit('openTaskDrawer')">
                {{ t('apiTestManagement.timeTaskList') }}
              </MsButton>
            </div>
          </a-form-item>
          <a-form-item
            field="swaggerUrl"
            label="SwaggerURL"
            asterisk-position="end"
            :rules="[{ required: true, message: t('apiTestManagement.swaggerURLRequired') }]"
          >
            <a-input
              v-model:model-value="importForm.swaggerUrl"
              :placeholder="t('apiTestManagement.urlImportPlaceholder')"
              class="w-[550px]"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item field="token" label="token" asterisk-position="end">
            <a-textarea
              v-model:model-value="importForm.swaggerToken"
              :auto-size="{ minRows: 1 }"
              class="w-[700px]"
              allow-clear
            />
          </a-form-item>
          <div class="mb-[16px] flex items-center gap-[8px]">
            <a-switch v-model:model-value="importForm.authSwitch" type="line" size="small"></a-switch>
            {{ t('apiTestManagement.basicAuth') }}
          </div>
          <template v-if="importForm.authSwitch">
            <a-form-item
              field="authUsername"
              :label="t('apiTestManagement.account')"
              :rules="[{ required: true, message: t('apiTestManagement.accountRequired') }]"
              asterisk-position="end"
            >
              <a-input
                v-model:model-value="importForm.authUsername"
                :placeholder="t('common.pleaseInput')"
                class="w-[500px]"
                allow-clear
              />
            </a-form-item>
            <a-form-item
              field="authPassword"
              :label="t('apiTestManagement.password')"
              :rules="[{ required: true, message: t('apiTestManagement.passwordRequired') }]"
              asterisk-position="end"
              autocomplete="new-password"
            >
              <a-input-password
                v-model:model-value="importForm.authPassword"
                :placeholder="t('common.pleaseInput')"
                class="w-[500px]"
                autocomplete="new-password"
                allow-clear
              />
            </a-form-item>
          </template>
          <a-form-item :label="t('apiTestManagement.belongModule')">
            <a-tree-select
              v-model:modelValue="importForm.moduleId"
              :data="innerModuleTree"
              class="w-[500px]"
              :field-names="{ title: 'name', key: 'id', children: 'children' }"
              allow-search
              :filter-tree-node="filterTreeNode"
              :fallback-option="(key: string | number) => ({
                name: t('apiTestManagement.moduleNotExist'),
                key,
                disabled: true,
              })"
            >
              <template #tree-slot-title="node">
                <a-tooltip :content="`${node.name}`" position="tl">
                  <div class="one-line-text w-[300px]">{{ node.name }}</div>
                </a-tooltip>
              </template>
            </a-tree-select>
          </a-form-item>
          <a-form-item>
            <template #label>
              <div class="flex items-center gap-[2px]">
                {{ t('apiTestManagement.importMode') }}
                <a-tooltip position="right">
                  <icon-question-circle
                    class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                    size="16"
                  />
                  <template #content>
                    <div>{{ t('apiTestManagement.importModeTip1') }}</div>
                    <div>{{ t('apiTestManagement.importModeTip2') }}</div>
                    <div>{{ t('apiTestManagement.importModeTip3') }}</div>
                    <div>{{ t('apiTestManagement.importModeTip4') }}</div>
                    <div class="h-[22px] w-full"></div>
                    <div>{{ t('apiTestManagement.importModeTip5') }}</div>
                    <div>{{ t('apiTestManagement.importModeTip6') }}</div>
                    <div>{{ t('apiTestManagement.importModeTip7') }}</div>
                  </template>
                </a-tooltip>
              </div>
            </template>
            <a-select v-model:model-value="importForm.coverData" class="w-[240px]">
              <a-option :value="true">{{ t('apiTestManagement.cover') }}</a-option>
              <a-option :value="false">{{ t('apiTestManagement.uncover') }}</a-option>
            </a-select>
          </a-form-item>
          <a-form-item :label="t('apiTestManagement.syncFrequency')">
            <MsCronSelect v-model:model-value="cronValue" class="min-w-[240px]" />
          </a-form-item>
        </template>
      </a-form>
    </MsDrawer>
  </div>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCronSelect from '@/components/pure/ms-cron-select/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import type { MsFileItem } from '@/components/pure/ms-upload/types';

  import { createDefinitionSchedule, importDefinition } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useUserStore from '@/store/modules/user';
  import { filterTree, filterTreeNode, TreeNode } from '@/utils';

  import type { ImportApiDefinitionParams, ImportApiDefinitionRequest } from '@/models/apiTest/management';
  import type { ModuleTreeNode } from '@/models/common';
  import { RequestImportFormat, RequestImportType } from '@/enums/apiEnum';

  const props = defineProps<{
    visible: boolean;
    moduleTree: ModuleTreeNode[];
    activeModule: string;
  }>();
  const emit = defineEmits(['update:visible', 'done', 'openTaskDrawer']);

  const { t } = useI18n();
  const appStore = useAppStore();
  const userStore = useUserStore();

  const visible = useVModel(props, 'visible', emit);
  const innerModuleTree = ref<TreeNode<ModuleTreeNode>[]>([]);
  const importType = ref<'file' | 'time'>('file');
  const platformList = [
    {
      name: 'Swagger',
      value: RequestImportFormat.SWAGGER,
    },
    {
      name: 'Postman',
      value: RequestImportFormat.Postman,
    },
    {
      name: 'Har',
      value: RequestImportFormat.Har,
    },
    {
      name: 'Jmeter',
      value: RequestImportFormat.Jmeter,
    },
    {
      name: 'MeterSphere',
      value: RequestImportFormat.MeterSphere,
    },
  ];
  const fileList = ref<MsFileItem[]>([]);
  const defaultForm: ImportApiDefinitionRequest = {
    platform: RequestImportFormat.SWAGGER,
    name: '',
    moduleId: '',
    coverData: false,
    syncCase: true,
    syncMock: true,
    coverModule: false,
    swaggerUrl: '',
    swaggerToken: '',
    authSwitch: false,
    authUsername: '',
    authPassword: '',
    type: RequestImportType.API,
    userId: userStore.id || '',
    protocol: 'HTTP',
    projectId: appStore.currentProjectId,
  };
  const importForm = ref({ ...defaultForm });
  const importFormRef = ref<FormInstance>();
  const uploadAccept = computed(() => {
    if ([RequestImportFormat.SWAGGER, RequestImportFormat.Postman].includes(importForm.value.platform)) {
      return 'json';
    }
    if (importForm.value.platform === RequestImportFormat.Har) {
      return 'har';
    }
    if (importForm.value.platform === RequestImportFormat.Jmeter) {
      return 'jmx';
    }
    return 'json';
  });

  watch(
    () => visible.value,
    (val) => {
      if (val) {
        importForm.value.moduleId = props.activeModule !== 'all' ? props.activeModule : '';
        innerModuleTree.value = filterTree(props.moduleTree, (node) => node.type === 'MODULE');
      }
    },
    {
      immediate: true,
    }
  );

  const moreSettingActive = ref<number[]>([]);
  const disabledConfirm = computed(() => {
    if (importForm.value.type === RequestImportType.API) {
      if (importType.value === 'file') {
        return !fileList.value.length;
      }
      return !importForm.value.swaggerUrl;
    }
    return !importForm.value.name || !importForm.value.swaggerUrl;
  });

  const cronValue = ref('0 0 0/1 * * ?');
  const importLoading = ref(false);

  function setActiveImportFormat(format: RequestImportFormat) {
    importForm.value.platform = format;
    if (format !== RequestImportFormat.SWAGGER) {
      importType.value = 'file';
      importForm.value.type = RequestImportType.API;
    }
  }

  function cancelImport() {
    visible.value = false;
    importForm.value = { ...defaultForm };
    importFormRef.value?.resetFields();
    importType.value = 'file';
    fileList.value = [];
    moreSettingActive.value = [];
  }

  async function importDefinitionByFile() {
    try {
      importLoading.value = true;
      let params: ImportApiDefinitionParams;
      if (importType.value === 'file') {
        params = {
          file: fileList.value[0].file || null,
          request: {
            type: importForm.value.type,
            platform: importForm.value.platform,
            userId: userStore.id || '',
            projectId: appStore.currentProjectId,
            coverModule: importForm.value.coverModule,
            coverData: importForm.value.coverData,
            syncCase: importForm.value.syncCase,
            syncMock: importForm.value.syncMock,
            protocol: importForm.value.protocol,
            moduleId: importForm.value.moduleId,
            authSwitch: importForm.value.authSwitch,
          },
        };
      } else {
        params = {
          file: null,
          request: {
            type: importForm.value.type,
            platform: importForm.value.platform,
            userId: userStore.id || '',
            projectId: appStore.currentProjectId,
            coverModule: importForm.value.coverModule,
            coverData: importForm.value.coverData,
            syncCase: importForm.value.syncCase,
            syncMock: importForm.value.syncMock,
            protocol: importForm.value.protocol,
            moduleId: importForm.value.moduleId,
            swaggerUrl: importForm.value.swaggerUrl,
            swaggerToken: importForm.value.swaggerToken,
            authSwitch: importForm.value.authSwitch,
            authUsername: importForm.value.authUsername,
            authPassword: importForm.value.authPassword,
          },
        };
      }
      await importDefinition(params);
      Message.success(t('common.importSuccess'));
      emit('done');
      cancelImport();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      importLoading.value = false;
    }
  }

  async function importDefinitionBySchedule() {
    try {
      importLoading.value = true;
      await createDefinitionSchedule({
        type: importForm.value.type,
        platform: importForm.value.platform,
        userId: userStore.id || '',
        projectId: appStore.currentProjectId,
        coverModule: importForm.value.coverModule,
        coverData: importForm.value.coverData,
        syncCase: importForm.value.syncCase,
        syncMock: importForm.value.syncMock,
        protocol: importForm.value.protocol,
        moduleId: importForm.value.moduleId,
        swaggerUrl: importForm.value.swaggerUrl,
        swaggerToken: importForm.value.swaggerToken,
        authSwitch: importForm.value.authSwitch,
        authUsername: importForm.value.authUsername,
        authPassword: importForm.value.authPassword,
        value: cronValue.value,
        name: importForm.value.name,
      });
      Message.success(t('apiTestManagement.createTaskSuccess'));
      importForm.value = { ...defaultForm };
      importFormRef.value?.resetFields();
      importType.value = 'time';
      fileList.value = [];
      moreSettingActive.value = [];
      emit('openTaskDrawer');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      importLoading.value = false;
    }
  }

  function confirmImport() {
    importFormRef.value?.validate((errors) => {
      if (!errors) {
        if (importForm.value.type === RequestImportType.API) {
          importDefinitionByFile();
        } else {
          importDefinitionBySchedule();
        }
      }
    });
  }

  function openLink() {
    window.open('https://converter.swagger.io/', '_blank');
  }
</script>

<style lang="less" scoped>
  .import-item {
    @apply flex cursor-pointer items-center;

    padding: 8px;
    width: 200px;
    border: 1px solid var(--color-text-n8);
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-fff);
    gap: 6px;
  }
  .import-item--active {
    border: 1px solid rgb(var(--primary-5));
    background-color: rgb(var(--primary-1));
  }
  :deep(.arco-form-item) {
    margin-bottom: 16px;
  }
  :deep(.arco-select-view-value::after) {
    @apply hidden;
  }
</style>
