<template>
  <div>
    <MsDrawer
      v-model:visible="visible"
      width="100%"
      :popup-container="props.popupContainer"
      :closable="false"
      :ok-disabled="disabledConfirm"
      :ok-text="t('common.import')"
      :ok-loading="importLoading"
      disabled-width-drag
      desc
      no-title
      @confirm="confirmImport"
      @cancel="cancelImport"
    >
      <div class="flex items-center justify-between p-[12px_8px]">
        <div class="font-medium text-[var(--color-text-1)]">{{ t('apiTestManagement.importApi') }}</div>
        <a-radio-group v-model:model-value="importForm.type" type="button">
          <a-radio :value="RequestImportType.API">{{ t('apiTestManagement.fileImport') }}</a-radio>
          <a-radio :value="RequestImportType.SCHEDULE">{{ t('apiTestManagement.timeImport') }}</a-radio>
        </a-radio-group>
      </div>
      <div
        v-if="importType === 'file'"
        class="my-[16px] flex items-center gap-[16px] rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[16px]"
      >
        <div
          v-for="item of platformList"
          :key="item.value"
          :class="`import-item ${importForm.platform === item.value ? 'import-item--active' : ''}`"
          @click="() => setActiveImportFormat(item.value)"
        >
          <div class="flex h-[24px] w-[24px] items-center justify-center rounded-[var(--border-radius-small)] bg-white">
            <MsIcon :type="item.icon" :class="`text-[${item.iconColor}]`" :size="18" />
          </div>
          <div class="text-[var(--color-text-1)]">{{ item.name }}</div>
        </div>
      </div>
      <a-form ref="importFormRef" :model="importForm" layout="vertical">
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
          <a-collapse v-model:active-key="moreSettingActive" :bordered="false" :show-expand-icon="false">
            <a-collapse-item :key="1">
              <template #header>
                <MsButton
                  type="text"
                  @click="() => (moreSettingActive.length > 0 ? (moreSettingActive = []) : (moreSettingActive = [1]))"
                >
                  {{ t('apiTestDebug.moreSetting') }}
                  <icon-down v-if="moreSettingActive.length > 0" class="text-rgb(var(--primary-5))" />
                  <icon-right v-else class="text-rgb(var(--primary-5))" />
                </MsButton>
              </template>
              <div class="mt-[16px]">
                <a-checkbox v-model:model-value="importForm.syncCase" class="mr-[24px]">
                  {{ t('apiTestManagement.syncImportCase') }}
                </a-checkbox>
                <a-checkbox v-if="importForm.coverData" v-model:model-value="importForm.coverModule">
                  {{ t('apiTestManagement.syncUpdateDirectory') }}
                </a-checkbox>
              </div>
            </a-collapse-item>
          </a-collapse>
          <a-form-item :label="t('apiTestManagement.importType')" class="mt-[8px]">
            <a-radio-group v-model:model-value="importType" type="button">
              <a-radio value="file">{{ t('apiTestManagement.fileImport') }}</a-radio>
              <a-radio value="swaggerUrl">{{ t('apiTestManagement.urlImport') }}</a-radio>
            </a-radio-group>
          </a-form-item>
          <MsUpload
            v-if="importType === 'file'"
            v-model:file-list="fileList"
            accept="json"
            :auto-upload="false"
            draggable
            size-unit="MB"
            class="w-full"
          >
            <template #subText>
              <div class="flex">
                {{ t('apiTestManagement.importSwaggerFileTip1') }}
                <span class="text-[rgb(var(--warning-6))]" @click.stop="openLink">{{
                  t('apiTestManagement.importSwaggerFileTip2')
                }}</span>
                {{ t('apiTestManagement.importSwaggerFileTip3') }}
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
              <MsButton type="text" @click="taskDrawerVisible = true">
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
    <MsDrawer v-model:visible="taskDrawerVisible" :width="960" :title="t('apiTestManagement.timeTask')" :footer="false">
      <div class="mb-[16px] flex items-center justify-end">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('apiTestManagement.searchTaskPlaceholder')"
          allow-clear
          class="mr-[8px] w-[240px]"
          @search="loadTaskList"
          @press-enter="loadTaskList"
          @clear="loadTaskList"
        />
      </div>
      <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
        <template #action="{ record }">
          <a-switch
            v-model:modelValue="record.enable"
            type="line"
            size="small"
            :before-change="() => handleBeforeEnableChange(record)"
          ></a-switch>
        </template>
      </ms-base-table>
    </MsDrawer>
  </div>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';
  import { FormInstance, Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCronSelect from '@/components/pure/ms-cron-select/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import type { MsFileItem } from '@/components/pure/ms-upload/types';

  import {
    createDefinitionSchedule,
    importDefinition,
    switchDefinitionSchedule,
  } from '@/api/modules/api-test/management';
  import { getScheduleProApiCaseList } from '@/api/modules/project-management/taskCenter';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useUserStore from '@/store/modules/user';
  import { filterTree, filterTreeNode, TreeNode } from '@/utils';

  import type { ImportApiDefinitionParams, ImportApiDefinitionRequest } from '@/models/apiTest/management';
  import type { ModuleTreeNode } from '@/models/common';
  import { TimingTaskCenterApiCaseItem } from '@/models/projectManagement/taskCenter';
  import { RequestImportFormat, RequestImportType } from '@/enums/apiEnum';
  import { TaskCenterEnum } from '@/enums/taskCenter';

  const props = defineProps<{
    visible: boolean;
    moduleTree: ModuleTreeNode[];
    popupContainer?: string;
    activeModule: string;
  }>();
  const emit = defineEmits(['update:visible', 'done']);

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
      icon: 'icon-icon_swagger',
      iconColor: 'rgb(var(--success-7))',
    },
  ];
  const fileList = ref<MsFileItem[]>([]);
  const defaultForm: ImportApiDefinitionRequest = {
    platform: RequestImportFormat.SWAGGER,
    name: '',
    moduleId: '',
    coverData: false,
    syncCase: true,
    coverModule: false,
    swaggerUrl: '',
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
  const taskDrawerVisible = ref(false);

  function setActiveImportFormat(format: RequestImportFormat) {
    importForm.value.platform = format;
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
            protocol: importForm.value.protocol,
            moduleId: importForm.value.moduleId,
            swaggerUrl: importForm.value.swaggerUrl,
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
        protocol: importForm.value.protocol,
        moduleId: importForm.value.moduleId,
        swaggerUrl: importForm.value.swaggerUrl,
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
      taskDrawerVisible.value = true;
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

  const keyword = ref('');
  const columns: MsTableColumn = [
    {
      title: 'project.taskCenter.resourceID',
      dataIndex: 'resourceNum',
      slotName: 'resourceNum',
      width: 140,
      showInTable: true,
      showTooltip: true,
    },
    {
      title: 'project.taskCenter.resourceName',
      slotName: 'resourceName',
      dataIndex: 'resourceName',
      width: 200,
      showDrag: true,
      showTooltip: true,
    },
    {
      title: 'project.taskCenter.swaggerUrl',
      slotName: 'swaggerUrl',
      dataIndex: 'swaggerUrl',
      width: 300,
      showDrag: false,
      showTooltip: true,
      columnSelectorDisabled: true,
      showInTable: true,
    },
    {
      title: 'apiTestManagement.taskRunRule',
      dataIndex: 'value',
      width: 140,
    },
    {
      title: 'apiTestManagement.taskNextRunTime',
      dataIndex: 'nextTime',
      showTooltip: true,
      width: 180,
    },
    {
      title: 'apiTestManagement.taskOperator',
      dataIndex: 'createUserName',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'apiTestManagement.taskOperationTime',
      dataIndex: 'createTime',
      width: 180,
    },
    {
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 80,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    getScheduleProApiCaseList,
    {
      columns,
      scroll: { x: '100%' },
    },
    (item) => ({
      ...item,
      operationTime: dayjs(item.operationTime).format('YYYY-MM-DD HH:mm:ss'),
      nextTime: item.nextTime ? dayjs(item.nextTime).format('YYYY-MM-DD HH:mm:ss') : '-',
    })
  );
  function loadTaskList() {
    setLoadListParams({
      keyword: keyword.value,
      moduleType: TaskCenterEnum.API_IMPORT,
    });
    loadList();
  }

  watch(
    () => taskDrawerVisible.value,
    (value) => {
      if (value) {
        loadTaskList();
      }
    }
  );

  async function handleBeforeEnableChange(record: TimingTaskCenterApiCaseItem) {
    try {
      await switchDefinitionSchedule(record.id);
      Message.success(
        t(record.enable ? 'apiTestManagement.disableTaskSuccess' : 'apiTestManagement.enableTaskSuccess')
      );
      return true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return false;
    }
  }

  function openLink() {
    window.open('https://converter.swagger.io/', '_blank');
  }
</script>

<style lang="less" scoped>
  .import-item {
    @apply flex cursor-pointer items-center bg-white;

    padding: 8px;
    gap: 6px;
    width: 200px;
    border-radius: var(--border-radius-small);
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
