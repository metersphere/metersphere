<template>
  <MsDrawer
    v-model:visible="visible"
    width="100%"
    :popup-container="props.popupContainer"
    :closable="false"
    :ok-disabled="disabledConfirm"
    disabled-width-drag
    no-title
    @confirm="confirmImport"
    @cancel="cancelImport"
  >
    <template #title> </template>
    <div class="flex items-center justify-between p-[12px_8px]">
      <div class="font-medium text-[var(--color-text-1)]">{{ t('apiTestManagement.importApi') }}</div>
      <a-radio-group v-model:model-value="importType" type="button">
        <a-radio value="file">{{ t('apiTestManagement.fileImport') }}</a-radio>
        <a-radio value="time">{{ t('apiTestManagement.timeImport') }}</a-radio>
      </a-radio-group>
    </div>
    <div
      v-if="importType === 'file'"
      class="my-[16px] flex items-center gap-[16px] rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[16px]"
    >
      <div
        v-for="item of importFormatList"
        :key="item.value"
        :class="`import-item ${importFormat === item.value ? 'import-item--active' : ''}`"
        @click="() => setActiveImportFormat(item.value)"
      >
        <div class="flex h-[24px] w-[24px] items-center justify-center rounded-[var(--border-radius-small)] bg-white">
          <MsIcon :type="item.icon" :class="`text-[${item.iconColor}]`" :size="18" />
        </div>
        <div class="text-[var(--color-text-1)]">{{ item.name }}</div>
      </div>
    </div>
    <a-form ref="importFormRef" :model="importForm" layout="vertical">
      <template v-if="importType === 'file'">
        <a-form-item :label="t('apiTestManagement.belongModule')">
          <a-tree-select
            v-model:modelValue="importForm.module"
            :data="moduleTree"
            class="w-[436px]"
            :field-names="{ title: 'name', key: 'id', children: 'children' }"
            allow-search
          />
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
          <a-select v-model:model-value="importForm.mode" class="w-[240px]">
            <a-option value="cover">{{ t('apiTestManagement.cover') }}</a-option>
            <a-option value="uncover">{{ t('apiTestManagement.uncover') }}</a-option>
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
              <a-checkbox v-model:model-value="importForm.syncImportCase" class="mr-[24px]">
                {{ t('apiTestManagement.syncImportCase') }}
              </a-checkbox>
              <a-checkbox v-model:model-value="importForm.syncUpdateDirectory">
                {{ t('apiTestManagement.syncUpdateDirectory') }}
              </a-checkbox>
            </div>
          </a-collapse-item>
        </a-collapse>
        <a-form-item :label="t('apiTestManagement.importType')" class="mt-[8px]">
          <a-radio-group v-model:model-value="importForm.importType" type="button">
            <a-radio value="file">{{ t('apiTestManagement.fileImport') }}</a-radio>
            <a-radio value="url">{{ t('apiTestManagement.urlImport') }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <MsUpload
          v-if="importForm.importType === 'file'"
          v-model:file-list="importForm.file"
          accept="json"
          :auto-upload="false"
          draggable
          size-unit="MB"
          class="w-full"
        >
          <template #subText>
            <div class="flex">
              {{ t('apiTestManagement.importSwaggerFileTip1') }}
              <div class="text-[rgb(var(--warning-6))]">{{ t('apiTestManagement.importSwaggerFileTip2') }}</div>
              {{ t('apiTestManagement.importSwaggerFileTip3') }}
            </div>
          </template>
        </MsUpload>
        <template v-else>
          <a-form-item
            field="url"
            label="SwaggerURL"
            asterisk-position="end"
            :rules="[{ required: true, message: t('apiTestManagement.swaggerURLRequired') }]"
          >
            <a-input
              v-model:model-value="importForm.url"
              :placeholder="t('apiTestManagement.urlImportPlaceholder')"
              class="w-[700px]"
              allow-clear
            ></a-input>
          </a-form-item>
          <div class="mb-[16px] flex items-center gap-[8px]">
            <a-switch v-model:model-value="importForm.basicAuth" type="line" size="small"></a-switch>
            {{ t('apiTestManagement.basicAuth') }}
          </div>
          <template v-if="importForm.basicAuth">
            <a-form-item
              field="account"
              :label="t('apiTestManagement.account')"
              asterisk-position="end"
              :rules="[{ required: true, message: t('apiTestManagement.accountRequired') }]"
            >
              <a-input
                v-model:model-value="importForm.account"
                :placeholder="t('common.pleaseInput')"
                class="w-[500px]"
                allow-clear
              ></a-input>
            </a-form-item>
            <a-form-item
              field="password"
              :label="t('apiTestManagement.password')"
              asterisk-position="end"
              :rules="[{ required: true, message: t('apiTestManagement.passwordRequired') }]"
              autocomplete="new-password"
            >
              <a-input-password
                v-model:model-value="importForm.password"
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
          field="taskName"
          :label="t('apiTestManagement.taskName')"
          :rules="[{ required: true, message: t('apiTestManagement.taskNameRequired') }]"
        >
          <div class="flex w-full items-center gap-[8px]">
            <a-input
              v-model:model-value="importForm.taskName"
              :placeholder="t('apiTestManagement.taskNamePlaceholder')"
              :max-length="255"
              class="flex-1"
            ></a-input>
            <MsButton type="text">{{ t('apiTestManagement.timeTaskList') }}</MsButton>
          </div>
        </a-form-item>
        <a-form-item
          field="url"
          label="SwaggerURL"
          asterisk-position="end"
          :rules="[{ required: true, message: t('apiTestManagement.swaggerURLRequired') }]"
        >
          <a-input
            v-model:model-value="importForm.url"
            :placeholder="t('apiTestManagement.urlImportPlaceholder')"
            class="w-[700px]"
            allow-clear
          ></a-input>
        </a-form-item>
        <div class="mb-[16px] flex items-center gap-[8px]">
          <a-switch v-model:model-value="importForm.basicAuth" type="line" size="small"></a-switch>
          {{ t('apiTestManagement.basicAuth') }}
        </div>
        <template v-if="importForm.basicAuth">
          <a-form-item
            field="account"
            :label="t('apiTestManagement.account')"
            :rules="[{ required: true, message: t('apiTestManagement.accountRequired') }]"
            asterisk-position="end"
          >
            <a-input
              v-model:model-value="importForm.account"
              :placeholder="t('common.pleaseInput')"
              class="w-[500px]"
              allow-clear
            />
          </a-form-item>
          <a-form-item
            field="password"
            :label="t('apiTestManagement.password')"
            :rules="[{ required: true, message: t('apiTestManagement.passwordRequired') }]"
            asterisk-position="end"
            autocomplete="new-password"
          >
            <a-input-password
              v-model:model-value="importForm.password"
              :placeholder="t('common.pleaseInput')"
              class="w-[500px]"
              autocomplete="new-password"
              allow-clear
            />
          </a-form-item>
        </template>
        <a-form-item :label="t('apiTestManagement.belongModule')">
          <a-tree-select
            v-model:modelValue="importForm.module"
            :data="moduleTree"
            class="w-[436px]"
            :field-names="{ title: 'name', key: 'id', children: 'children' }"
            allow-search
          />
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
          <a-select v-model:model-value="importForm.mode" class="w-[240px]">
            <a-option value="cover">{{ t('apiTestManagement.cover') }}</a-option>
            <a-option value="uncover">{{ t('apiTestManagement.uncover') }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="t('apiTestManagement.syncFrequency')">
          <a-select v-model:model-value="importForm.syncFrequency" class="w-[240px]">
            <template #label="{ data }">
              <div class="flex items-center">
                {{ data.value }}
                <div class="ml-[4px] text-[var(--color-text-4)]">{{ data.label.split('?')[1] }}</div>
              </div>
            </template>
            <a-option v-for="item of syncFrequencyOptions" :key="item.value" :value="item.value" class="block">
              <div class="flex w-full items-center justify-between">
                {{ item.value }}
                <div class="ml-[4px] text-[var(--color-text-4)]">{{ item.label }}</div>
              </div>
            </a-option>
            <template #footer>
              <div class="flex items-center p-[4px_8px]">
                <MsButton type="text">{{ t('apiTestManagement.customFrequency') }}</MsButton>
              </div>
            </template>
          </a-select>
        </a-form-item>
      </template>
    </a-form>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { mapTree } from '@/utils';

  import { ModuleTreeNode } from '@/models/common';
  import { RequestImportFormat } from '@/enums/apiEnum';

  const props = defineProps<{
    visible: boolean;
    moduleTree: ModuleTreeNode[];
    popupContainer?: string;
  }>();
  const emit = defineEmits(['update:visible']);

  const { t } = useI18n();

  const visible = useVModel(props, 'visible', emit);
  const importType = ref<'file' | 'time'>('file');
  const importFormat = ref<keyof typeof RequestImportFormat>('SWAGGER');
  const importFormatList = [
    {
      name: 'Swagger',
      value: RequestImportFormat.SWAGGER,
      icon: 'icon-icon_swagger',
      iconColor: 'rgb(var(--success-7))',
    },
  ];

  function setActiveImportFormat(format: RequestImportFormat) {
    importFormat.value = format;
  }

  const defaultForm = {
    taskName: '',
    module: 'root',
    mode: 'cover',
    syncImportCase: true,
    syncUpdateDirectory: false,
    importType: 'file',
    file: [],
    url: '',
    basicAuth: false,
    account: '',
    password: '',
    syncFrequency: '0 0 0/1 * ?',
  };
  const importForm = ref({ ...defaultForm });
  const importFormRef = ref<FormInstance>();
  const moreSettingActive = ref<number[]>([]);
  const disabledConfirm = computed(() => {
    if (importType.value === 'file') {
      if (importForm.value.importType === 'file') {
        return !importForm.value.file.length;
      }
      return !importForm.value.url;
    }
    return !importForm.value.taskName || !importForm.value.url;
  });
  const moduleTree = computed(() => mapTree(props.moduleTree, (node) => ({ ...node, draggable: false })));
  const syncFrequencyOptions = [
    { label: t('apiTestManagement.timeTaskHour'), value: '0 0 0/1 * ?' },
    { label: t('apiTestManagement.timeTaskSixHour'), value: '0 0 0/6 * ?' },
    { label: t('apiTestManagement.timeTaskTwelveHour'), value: '0 0 0/12 * ?' },
    { label: t('apiTestManagement.timeTaskDay'), value: '0 0 0 * ?' },
  ];

  function cancelImport() {
    visible.value = false;
    importForm.value = { ...defaultForm };
    importFormRef.value?.resetFields();
  }

  function confirmImport() {
    importFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          Message.success(t('common.importSuccess'));
          cancelImport();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      }
    });
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
