<template>
  <MsFormTable
    v-model:originalSelectedKeys="selectedKeys"
    v-model:expanded-keys="expandKeys"
    :data="data"
    :columns="columns"
    show-empty-tree
    :selectable="false"
    :row-selection="{
      type: 'checkbox',
      showCheckedAll: true,
      checkStrictly: true,
      width: 32,
    }"
    :table-key="TableKeyEnum.JSON_SCHEMA"
    :scroll="{ x: 'max-content' }"
    show-setting
    class="ms-json-schema"
    @select="handleSelect"
  >
    <template #batchAddTitle>
      <MsButton type="text" size="mini" class="!mr-0" @click="batchAdd">
        {{ t('apiTestDebug.batchAdd') }}
      </MsButton>
    </template>
    <template #title="{ record, columnConfig }">
      <span v-if="record.title === 'root'" class="px-[8px]">root</span>
      <a-popover
        v-else
        position="tl"
        :disabled="!record.title || record.title.trim() === ''"
        class="ms-params-input-popover"
      >
        <template #content>
          <div class="ms-params-popover-title">
            {{ t('ms.json.schema.name') }}
          </div>
          <div class="ms-params-popover-value">
            {{ record.title }}
          </div>
        </template>
        <a-input
          v-model:model-value="record.title"
          :placeholder="t(columnConfig.locale)"
          class="ms-form-table-input"
          :max-length="255"
          size="medium"
        />
      </a-popover>
    </template>
    <template #type="{ record }">
      <a-tooltip
        v-if="record.id !== 'root'"
        :content="t(record.required ? 'apiTestDebug.paramRequired' : 'apiTestDebug.paramNotRequired')"
      >
        <MsButton
          type="icon"
          :class="[
            record.required ? '!text-[rgb(var(--danger-5))]' : '!text-[var(--color-text-brand)]',
            '!mr-[4px] !p-[4px]',
          ]"
          size="mini"
          @click="() => (record.required = !record.required)"
        >
          <div>*</div>
        </MsButton>
      </a-tooltip>
      <a-select
        v-model:model-value="record.type"
        :options="getTypeOptions(record)"
        class="ms-form-table-input w-full"
        size="medium"
        @change="handleTypeChange(record)"
      />
    </template>
    <template #value="{ record }">
      <MsParamsInput v-model:value="record.value" size="medium" @dblclick="() => quickInputParams(record)" />
    </template>
    <template #minLength="{ record }">
      <a-input-number
        v-if="record.type === 'string'"
        v-model:model-value="record.minLength"
        class="ms-form-table-input-number"
        :min="0"
        :precision="0"
        size="medium"
      />
      <div v-else class="ms-json-schema-td-text">-</div>
    </template>
    <template #maxLength="{ record }">
      <a-input-number
        v-if="record.type === 'string'"
        v-model:model-value="record.maxLength"
        class="ms-form-table-input-number"
        :min="0"
        :precision="0"
        size="medium"
      />
      <div v-else class="ms-json-schema-td-text">-</div>
    </template>
    <template #minimum="{ record }">
      <a-input-number
        v-if="record.type === 'number'"
        v-model:model-value="record.minimum"
        class="ms-form-table-input-number"
        size="medium"
      />
      <a-input-number
        v-else-if="record.type === 'integer'"
        v-model:model-value="record.minimum"
        class="ms-form-table-input-number"
        size="medium"
        :step="1"
        :precision="0"
      />
      <div v-else class="ms-json-schema-td-text">-</div>
    </template>
    <template #maximum="{ record }">
      <a-input-number
        v-if="record.type === 'number'"
        v-model:model-value="record.maximum"
        class="ms-form-table-input-number"
        size="medium"
      />
      <a-input-number
        v-else-if="record.type === 'integer'"
        v-model:model-value="record.maximum"
        class="ms-form-table-input-number"
        size="medium"
        :step="1"
        :precision="0"
      />
      <div v-else class="ms-json-schema-td-text">-</div>
    </template>
    <template #defaultValue="{ record }">
      <a-input-number
        v-if="record.type === 'number'"
        v-model:model-value="record.defaultValue"
        class="ms-form-table-input-number"
        size="medium"
      />
      <a-input-number
        v-else-if="record.type === 'integer'"
        v-model:model-value="record.defaultValue"
        class="ms-form-table-input-number"
        size="medium"
        :step="1"
        :precision="0"
      />
      <a-select
        v-else-if="record.type === 'boolean'"
        v-model:model-value="record.defaultValue"
        class="ms-form-table-input"
        size="medium"
        :options="[
          {
            label: 'true',
            value: true,
          },
          {
            label: 'false',
            value: false,
          },
        ]"
      />
      <div v-else-if="['object', 'array', 'null'].includes(record.type)" class="ms-json-schema-td-text"> - </div>
      <a-input
        v-else
        v-model:model-value="record.defaultValue"
        :placeholder="t('common.pleaseInput')"
        class="ms-form-table-input"
      />
    </template>
    <template #action="{ record, rowIndex }">
      <div class="flex w-full items-center gap-[8px]">
        <a-tooltip :content="t('common.advancedSettings')">
          <MsButton type="icon" class="ms-json-schema-icon-button" @click="openSetting(record)">
            <icon-settings class="text-[var(--color-text-4)]" />
          </MsButton>
        </a-tooltip>
        <a-tooltip v-if="['object', 'array'].includes(record.type)" :content="t('ms.json.schema.addChild')">
          <MsButton type="icon" class="ms-json-schema-icon-button" @click="addChild(record)">
            <MsIcon type="icon-icon_add_outlined" class="text-[var(--color-text-4)]" />
          </MsButton>
        </a-tooltip>
        <a-tooltip v-if="record.id !== 'root'" :content="t('common.delete')">
          <MsButton type="icon" class="ms-json-schema-icon-button" @click="deleteLine(record, rowIndex)">
            <icon-minus-circle class="cursor-pointer text-[var(--color-text-4)]" size="18" />
          </MsButton>
        </a-tooltip>
      </div>
    </template>
  </MsFormTable>
  <a-modal
    v-model:visible="showQuickInputParam"
    :title="t('ms.paramsInput.value')"
    :ok-text="t('apiTestDebug.apply')"
    :ok-button-props="{ disabled: !quickInputParamValue || quickInputParamValue.trim() === '' }"
    class="ms-modal-form"
    body-class="!p-0"
    :width="680"
    title-align="start"
    @ok="applyQuickInputParam"
    @close="clearQuickInputParam"
  >
    <MsCodeEditor
      v-if="showQuickInputParam"
      v-model:model-value="quickInputParamValue"
      theme="vs"
      height="300px"
      :show-full-screen="false"
    >
      <template #rightTitle>
        <div class="flex justify-between">
          <div class="text-[var(--color-text-4)]">
            {{ t('apiTestDebug.quickInputParamsTip') }}
          </div>
        </div>
      </template>
    </MsCodeEditor>
  </a-modal>
  <MsDrawer
    v-model:visible="settingDrawerVisible"
    :width="600"
    :title="t('ms.json.schema.advancedSettings')"
    :ok-text="t('common.save')"
    @confirm="applySetting"
  >
    <a-form ref="setting" :model="activeRecord" layout="vertical">
      <a-form-item
        :label="t('ms.json.schema.name')"
        :rules="[{ required: true, message: t('ms.json.schema.nameNotNull') }]"
        asterisk-position="end"
      >
        <a-input
          v-model:model-value="activeRecord.name"
          :max-length="255"
          :placeholder="t('common.pleaseInput')"
          :disabled="activeRecord.id === 'root'"
        />
      </a-form-item>
      <a-form-item :label="t('common.desc')">
        <a-textarea v-model:model-value="activeRecord.description" :placeholder="t('common.pleaseInput')" />
      </a-form-item>
      <template v-if="!['object', 'array', 'null'].includes(activeRecord.type)">
        <div class="flex items-center justify-between gap-[24px]">
          <template v-if="!['boolean', 'number', 'integer'].includes(activeRecord.type)">
            <a-form-item :label="t('ms.json.schema.minLength')" class="w-[144px]">
              <a-input-number
                v-model:model-value="activeRecord.minLength"
                mode="button"
                :min="0"
                :step="1"
                :precision="0"
              />
            </a-form-item>
            <a-form-item :label="t('ms.json.schema.maxLength')" class="w-[144px]">
              <a-input-number
                v-model:model-value="activeRecord.maxLength"
                mode="button"
                :min="0"
                :step="1"
                :precision="0"
              />
            </a-form-item>
          </template>
          <template v-else-if="['number', 'integer'].includes(activeRecord.type)">
            <a-form-item :label="t('ms.json.schema.minimum')" class="w-[144px]">
              <a-input-number
                v-if="activeRecord.type === 'integer'"
                v-model:model-value="activeRecord.minimum"
                mode="button"
                :step="1"
                :precision="0"
              />
              <a-input-number v-else v-model:model-value="activeRecord.minimum" mode="button" />
            </a-form-item>
            <a-form-item :label="t('ms.json.schema.maximum')" class="w-[144px]">
              <a-input-number
                v-if="activeRecord.type === 'integer'"
                v-model:model-value="activeRecord.maximum"
                mode="button"
                :step="1"
                :precision="0"
              />
              <a-input-number v-else v-model:model-value="activeRecord.maximum" mode="button" />
            </a-form-item>
          </template>
          <a-form-item :label="t('ms.json.schema.default')" class="flex-1">
            <a-input-number
              v-if="activeRecord.type === 'number'"
              v-model:model-value="activeRecord.defaultValue"
              mode="button"
              :placeholder="t('common.pleaseInput')"
            />
            <a-input-number
              v-else-if="activeRecord.type === 'integer'"
              v-model:model-value="activeRecord.defaultValue"
              mode="button"
              :placeholder="t('common.pleaseInput')"
              :step="1"
              :precision="0"
            />
            <a-input v-else v-model:model-value="activeRecord.defaultValue" :placeholder="t('common.pleaseInput')" />
          </a-form-item>
        </div>
        <template v-if="activeRecord.type !== 'boolean'">
          <a-form-item :label="t('ms.json.schema.enum')">
            <a-textarea v-model:model-value="activeRecord.enum" :placeholder="t('ms.json.schema.enumPlaceholder')" />
          </a-form-item>
          <a-form-item :label="t('ms.json.schema.regex')">
            <a-input v-model:model-value="activeRecord.regex" :placeholder="t('ms.json.schema.regexPlaceholder')" />
          </a-form-item>
          <a-form-item :label="t('ms.json.schema.format')">
            <a-select
              v-model:model-value="activeRecord.format"
              :placeholder="t('common.pleaseSelect')"
              :options="formatOptions"
            />
          </a-form-item>
        </template>
      </template>
      <div>
        <div class="mb-[8px]">{{ t('ms.json.schema.preview') }}</div>
        <MsCodeEditor
          v-model:model-value="activePreviewValue"
          theme="vs"
          height="300px"
          :show-full-screen="false"
          read-only
        >
        </MsCodeEditor>
      </div>
    </a-form>
  </MsDrawer>
  <MsDrawer
    v-model:visible="batchAddDrawerVisible"
    :width="600"
    :title="t('ms.json.schema.batchAdd')"
    :ok-text="t('common.add')"
    @confirm="applyBatchAdd"
  >
    <MsCodeEditor
      v-model:model-value="batchAddValue"
      theme="vs"
      height="100%"
      :language="LanguageEnum.JSON"
      :show-full-screen="false"
    >
      <template #leftTitle>
        <a-radio-group default-value="json" type="button" @change="batchAddValue = ''">
          <a-radio value="json">Json</a-radio>
          <a-radio value="jsonSchema">JsonSchema</a-radio>
        </a-radio-group>
      </template>
      <template #rightTitle>
        <div class="flex justify-between">
          <div class="text-[var(--color-text-4)]">
            {{ t('ms.json.schema.batchAddTip') }}
          </div>
        </div>
      </template>
    </MsCodeEditor>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsFormTable, { FormTableColumn } from '@/components/pure/ms-form-table/index.vue';
  import MsParamsInput from '@/components/business/ms-params-input/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { getGenerateId, traverseTree } from '@/utils';

  import { TableKeyEnum } from '@/enums/tableEnum';

  import { JsonSchemaItem } from './types';

  const { t } = useI18n();

  const defaultItem: JsonSchemaItem = {
    id: '',
    type: 'string',
    title: '',
    value: '',
    description: '',
    enable: true,
    defaultValue: '',
    maximum: undefined,
    minimum: undefined,
    maxLength: undefined,
    minLength: undefined,
    enumValues: '',
    pattern: undefined,
    format: undefined,
    children: undefined,
  };
  const data = defineModel<JsonSchemaItem[]>('data', {
    default: () => [
      {
        id: 'root',
        title: 'root',
        type: 'object',
        value: '',
        description: '',
        enable: true,
        defaultValue: '',
        maximum: undefined,
        minimum: undefined,
        maxLength: undefined,
        minLength: undefined,
        enumValues: '',
        pattern: undefined,
        format: undefined,
        required: false,
        children: [],
      },
    ],
  });
  const expandKeys = ref<string[]>(['root']);
  const selectedKeys = ref<string[]>(['root']);

  const typeOptions: SelectOptionData[] = [
    {
      label: 'object',
      value: 'object',
    },
    {
      label: 'array',
      value: 'array',
    },
    {
      label: 'string',
      value: 'string',
    },
    {
      label: 'number',
      value: 'number',
    },
    {
      label: 'integer',
      value: 'integer',
    },
    {
      label: 'boolean',
      value: 'boolean',
    },
    {
      label: 'null',
      value: 'null',
    },
  ];
  const formatOptions: SelectOptionData[] = [
    {
      label: 'date',
      value: 'date',
    },
    {
      label: 'date-time',
      value: 'date-time',
    },
    {
      label: 'email',
      value: 'email',
    },
    {
      label: 'hostname',
      value: 'hostname',
    },
    {
      label: 'ipv4',
      value: 'ipv4',
    },
    {
      label: 'ipv6',
      value: 'ipv6',
    },
    {
      label: 'url',
      value: 'url',
    },
  ];
  const columns: FormTableColumn[] = [
    {
      title: t('ms.json.schema.name'),
      dataIndex: 'title',
      slotName: 'title',
      inputType: 'input',
      size: 'medium',
      addLineDisabled: true,
      columnSelectorDisabled: true,
      fixed: 'left',
    },
    {
      title: t('ms.json.schema.type'),
      dataIndex: 'type',
      slotName: 'type',
      size: 'medium',
      width: 120,
      addLineDisabled: true,
      columnSelectorDisabled: true,
    },
    {
      title: t('ms.json.schema.value'),
      dataIndex: 'value',
      slotName: 'value',
      addLineDisabled: true,
      columnSelectorDisabled: true,
    },
    {
      title: t('common.desc'),
      dataIndex: 'description',
      slotName: 'description',
      inputType: 'quickInput',
      size: 'medium',
      addLineDisabled: true,
    },
    {
      title: t('ms.json.schema.minLength'),
      dataIndex: 'minLength',
      slotName: 'minLength',
      inputType: 'inputNumber',
      size: 'medium',
      min: 0,
      precision: 0,
      addLineDisabled: true,
      showInTable: false,
      width: 120,
    },
    {
      title: t('ms.json.schema.maxLength'),
      dataIndex: 'maxLength',
      slotName: 'maxLength',
      inputType: 'inputNumber',
      size: 'medium',
      min: 0,
      precision: 0,
      addLineDisabled: true,
      showInTable: false,
      width: 120,
    },
    {
      title: t('ms.json.schema.minimum'),
      dataIndex: 'minimum',
      slotName: 'minimum',
      inputType: 'inputNumber',
      size: 'medium',
      addLineDisabled: true,
      showInTable: false,
      width: 120,
    },
    {
      title: t('ms.json.schema.maximum'),
      dataIndex: 'maximum',
      slotName: 'maximum',
      inputType: 'inputNumber',
      size: 'medium',
      addLineDisabled: true,
      showInTable: false,
      width: 120,
    },
    {
      title: t('ms.json.schema.default'),
      dataIndex: 'defaultValue',
      slotName: 'defaultValue',
      inputType: 'input',
      size: 'medium',
      addLineDisabled: true,
      showInTable: false,
    },
    {
      title: t('ms.json.schema.enum'),
      dataIndex: 'enum',
      slotName: 'enum',
      inputType: 'textarea',
      size: 'medium',
      addLineDisabled: true,
      showInTable: false,
    },
    {
      title: t('ms.json.schema.regex'),
      dataIndex: 'regex',
      slotName: 'regex',
      inputType: 'input',
      size: 'medium',
      addLineDisabled: true,
      showInTable: false,
    },
    {
      title: t('ms.json.schema.format'),
      dataIndex: 'format',
      slotName: 'format',
      inputType: 'select',
      size: 'medium',
      options: formatOptions,
      addLineDisabled: true,
      showInTable: false,
    },
    {
      title: '',
      dataIndex: 'operation',
      slotName: 'action',
      titleSlotName: 'batchAddTitle',
      addLineDisabled: true,
      width: 100,
      showInTable: true,
      fixed: 'right',
    },
  ];

  /**
   * 获取类型选项，根节点只能是 object 或 array
   */
  function getTypeOptions(record: Record<string, any>) {
    if (record.name === 'root') {
      return typeOptions.filter((item) => ['object', 'array'].includes(item.value as string));
    }
    return typeOptions;
  }

  /**
   * 处理类型变化
   */
  function handleTypeChange(record: Record<string, any>) {
    if (record.type === 'object' || record.type === 'array') {
      if (!record.children) {
        // 没有子节点，初始化
        record.children = [];
      }
    } else {
      record.children = undefined;
    }
  }

  /**
   * 添加子节点
   */
  function addChild(record: Record<string, any>) {
    if (!record.children) {
      record.children = [];
    }
    const child = {
      ...cloneDeep(defaultItem),
      id: getGenerateId(),
      parent: record,
    };
    record.children.push(child);
    // 默认展开父节点
    if (!expandKeys.value.includes(record.id)) {
      expandKeys.value.push(record.id);
    }
    // 默认选中子节点
    if (!selectedKeys.value.includes(child.id)) {
      selectedKeys.value.push(child.id);
    }
  }

  /**
   * 删除行
   */
  function deleteLine(record: Record<string, any>, rowIndex: number) {
    if (record.parent) {
      record.parent.children.splice(rowIndex, 1);
    } else {
      data.value.splice(rowIndex, 1);
    }
  }

  /**
   * 行选择处理
   */
  function handleSelect(rowKeys: (string | number)[], rowKey: string | number, record: Record<string, any>) {
    nextTick(() => {
      if (record.enable && record.children && record.children.length > 0) {
        // 选中父节点时，选中子孙节点
        traverseTree(record.children, (item: Record<string, any>) => {
          item.enable = true;
          if (!selectedKeys.value.includes(item.id)) {
            selectedKeys.value.push(item.id);
          }
        });
      }
    });
  }

  const batchAddDrawerVisible = ref(false);
  const batchAddValue = ref('');

  function batchAdd() {
    batchAddDrawerVisible.value = true;
  }

  function applyBatchAdd() {
    batchAddDrawerVisible.value = false;
  }

  const settingDrawerVisible = ref(false);
  const activeRecord = ref<any>({});
  const activePreviewValue = ref('');

  function openSetting(record: Record<string, any>) {
    // 浅拷贝，以保留 parent 和 children 的引用
    activeRecord.value = {
      ...record,
    };
    settingDrawerVisible.value = true;
  }

  /**
   * 应用设置
   */
  function applySetting() {
    if (activeRecord.value.id === 'root') {
      data.value[0] = activeRecord.value;
    } else {
      const brothers = activeRecord.value.parent?.children || [];
      const index = brothers.findIndex((item: any) => item.id === activeRecord.value.id);
      if (index > -1) {
        brothers.splice(index, 1, activeRecord.value);
      }
    }
    settingDrawerVisible.value = false;
  }

  const showQuickInputParam = ref(false);
  const activeQuickInputRecord = ref<any>({});
  const quickInputParamValue = ref('');

  function quickInputParams(record: any) {
    activeQuickInputRecord.value = record;
    showQuickInputParam.value = true;
    quickInputParamValue.value = record.value;
  }

  function clearQuickInputParam() {
    activeQuickInputRecord.value = {};
    quickInputParamValue.value = '';
  }

  function applyQuickInputParam() {
    activeQuickInputRecord.value.value = quickInputParamValue.value;
    showQuickInputParam.value = false;
    clearQuickInputParam();
  }
</script>

<style lang="less">
  .ms-json-schema {
    .ms-json-schema-td-text {
      padding: 0 8px;
    }
    .ms-json-schema-icon-button {
      @apply !mr-0;
      &:hover {
        background-color: rgb(var(--primary-1)) !important;
        .arco-icon {
          color: rgb(var(--primary-4)) !important;
        }
      }
    }
  }
</style>
