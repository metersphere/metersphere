<template>
  <MsFormTable
    ref="formTableRef"
    v-model:originalSelectedKeys="selectedKeys"
    v-model:expanded-keys="expandKeys"
    :data="data"
    :columns="columns"
    show-empty-tree
    :selectable="false"
    :row-selection="rowSelection"
    :table-key="TableKeyEnum.JSON_SCHEMA"
    :scroll="{ x: '100%' }"
    :disabled="props.disabled"
    show-setting
    class="ms-json-schema"
    @row-select="handleSelect"
    @select-all="handleSelectAll"
    @form-change="emitChange('change')"
  >
    <template #batchAddTitle>
      <MsButton v-if="!props.disabled" type="text" size="mini" class="!mr-0" @click="batchAdd">
        {{ t('apiTestDebug.batchAdd') }}
      </MsButton>
    </template>
    <template #typeTitle="{ columnConfig }">
      <div class="pl-[30px] text-[var(--color-text-3)]">{{ t(columnConfig.title as string) }}</div>
    </template>
    <template #title="{ record, columnConfig, rowIndex }">
      <span v-if="record.id === 'root'" class="px-[8px]">root</span>
      <span v-else-if="record.parent?.type === 'array'" class="px-[8px]">{{ rowIndex }}</span>
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
          :placeholder="t(columnConfig.locale || 'common.pleaseInput')"
          class="ms-form-table-input ms-form-table-input--hasPlaceholder"
          :max-length="255"
          size="medium"
          @input="addLineIfLast(record, rowIndex)"
        />
      </a-popover>
    </template>
    <template #type="{ record }">
      <a-tooltip
        v-if="record.id !== 'root' && !props.disabled"
        :content="t(record.required ? 'apiTestDebug.paramRequired' : 'apiTestDebug.paramNotRequired')"
      >
        <div
          class="ms-form-table-required-button"
          :class="[
            record.required ? 'ms-form-table-required-button--required' : '',
            props.disabled ? 'ms-form-table-required-button--disabled' : '',
          ]"
          @click="toggleRequired(record)"
        >
          <article>*</article>
        </div>
      </a-tooltip>
      <div v-else class="w-[38px]"></div>
      <a-select
        v-model:model-value="record.type"
        :options="getTypeOptions(record)"
        class="ms-form-table-input w-full"
        size="medium"
        :disabled="props.disabled"
        @change="handleTypeChange(record)"
      />
    </template>
    <template #example="{ record }">
      <div v-if="['object', 'array', 'null'].includes(record.type)" class="ms-form-table-td-text">-</div>
      <MsParamsInput
        v-else
        v-model:value="record.example"
        size="medium"
        :disabled="props.disabled"
        @set-params="() => quickInputParams(record)"
        @change="emitChange('exampleInput')"
      />
    </template>
    <template #minLength="{ record }">
      <a-input-number
        v-if="record.type === 'string'"
        v-model:model-value="record.minLength"
        class="ms-form-table-input-number"
        :min="0"
        :precision="0"
        size="medium"
        :disabled="props.disabled"
        model-event="input"
        @change="emitChange('minLengthInput')"
      />
      <div v-else class="ms-form-table-td-text">-</div>
    </template>
    <template #maxLength="{ record }">
      <a-input-number
        v-if="record.type === 'string'"
        v-model:model-value="record.maxLength"
        class="ms-form-table-input-number"
        :min="0"
        :precision="0"
        size="medium"
        :disabled="props.disabled"
        model-event="input"
        @change="emitChange('maxLengthInput')"
      />
      <div v-else class="ms-form-table-td-text">-</div>
    </template>
    <template #minimum="{ record }">
      <a-input-number
        v-if="record.type === 'number'"
        v-model:model-value="record.minimum"
        class="ms-form-table-input-number"
        size="medium"
        :disabled="props.disabled"
        model-event="input"
        @change="emitChange('minimumInput')"
      />
      <a-input-number
        v-else-if="record.type === 'integer'"
        v-model:model-value="record.minimum"
        class="ms-form-table-input-number"
        size="medium"
        :step="1"
        :precision="0"
        :disabled="props.disabled"
        model-event="input"
        @change="emitChange('minimumInput')"
      />
      <div v-else class="ms-form-table-td-text">-</div>
    </template>
    <template #maximum="{ record }">
      <a-input-number
        v-if="record.type === 'number'"
        v-model:model-value="record.maximum"
        class="ms-form-table-input-number"
        size="medium"
        :disabled="props.disabled"
        model-event="input"
        @change="emitChange('maximumInput')"
      />
      <a-input-number
        v-else-if="record.type === 'integer'"
        v-model:model-value="record.maximum"
        class="ms-form-table-input-number"
        size="medium"
        :step="1"
        :precision="0"
        :disabled="props.disabled"
        model-event="input"
        @change="emitChange('maximumInput')"
      />
      <div v-else class="ms-form-table-td-text">-</div>
    </template>
    <template #minItems="{ record }">
      <a-input-number
        v-if="record.type === 'array'"
        v-model:model-value="record.minItems"
        class="ms-form-table-input-number"
        size="medium"
        :min="0"
        :step="1"
        :precision="0"
        :disabled="props.disabled"
        model-event="input"
        @change="emitChange('minItemsInput')"
      />
      <div v-else class="ms-form-table-td-text">-</div>
    </template>
    <template #maxItems="{ record }">
      <a-input-number
        v-if="record.type === 'array'"
        v-model:model-value="record.maxItems"
        class="ms-form-table-input-number"
        size="medium"
        :min="0"
        :step="1"
        :precision="0"
        :disabled="props.disabled"
        model-event="input"
        @change="emitChange('maxItemsInput')"
      />
      <div v-else class="ms-form-table-td-text">-</div>
    </template>
    <template #defaultValue="{ record }">
      <a-input-number
        v-if="record.type === 'number'"
        v-model:model-value="record.defaultValue"
        class="ms-form-table-input-number"
        size="medium"
        :disabled="props.disabled"
        model-event="input"
        @change="emitChange('defaultValueInput')"
      />
      <a-input-number
        v-else-if="record.type === 'integer'"
        v-model:model-value="record.defaultValue"
        class="ms-form-table-input-number"
        size="medium"
        :step="1"
        :precision="0"
        :disabled="props.disabled"
        model-event="input"
        @change="emitChange('defaultValueInput')"
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
        :disabled="props.disabled"
        @change="emitChange('defaultValueInput')"
      />
      <div v-else-if="['object', 'array', 'null'].includes(record.type)" class="ms-form-table-td-text"> - </div>
      <a-input
        v-else
        v-model:model-value="record.defaultValue"
        :placeholder="t('common.pleaseInput')"
        class="ms-form-table-input"
        :disabled="props.disabled"
        @change="emitChange('defaultValueInput')"
      />
    </template>
    <template #enumValues="{ record }">
      <div v-if="['object', 'array', 'null', 'boolean'].includes(record.type)" class="ms-form-table-td-text">-</div>
      <MsQuickInput
        v-else
        v-model:model-value="record.enumValues"
        :title="t('ms.json.schema.enum')"
        :popover-title="record.enumValues ? JSON.stringify(record.enumValues.split('\n')) : ''"
        class="ms-form-table-input"
        type="textarea"
        :disabled="props.disabled"
        @change="emitChange('enumValuesInput')"
      >
      </MsQuickInput>
    </template>
    <template #format="{ record }">
      <a-select
        v-if="record.type === 'string'"
        v-model:model-value="record.format"
        :options="formatOptions"
        :disabled="props.disabled"
        allow-clear
        class="ms-form-table-input"
        @change="emitChange('enumValuesInput')"
      ></a-select>
    </template>
    <template #pattern="{ record }">
      <a-input
        v-if="record.type === 'string'"
        v-model:model-value="record.pattern"
        :disabled="props.disabled"
        class="ms-form-table-input"
        @change="emitChange('enumValuesInput')"
      ></a-input>
    </template>
    <template #action="{ record, rowIndex }">
      <div class="flex w-full items-center gap-[8px]">
        <a-tooltip :content="t('common.advancedSettings')">
          <MsButton type="icon" class="ms-json-schema-icon-button" @click="openSetting(record)">
            <icon-settings class="text-[var(--color-text-4)]" />
          </MsButton>
        </a-tooltip>
        <a-tooltip
          v-if="['object', 'array'].includes(record.type) && !props.disabled"
          :content="t('ms.json.schema.addChild')"
        >
          <MsButton type="icon" class="ms-json-schema-icon-button" @click="addChild(record)">
            <MsIcon type="icon-icon_add_outlined" class="text-[var(--color-text-4)]" />
          </MsButton>
        </a-tooltip>
        <a-tooltip v-if="record.id !== 'root' && !props.disabled" :content="t('common.delete')">
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
    :footer="!props.disabled"
    @confirm="applySetting"
  >
    <a-form ref="settingFormRef" :model="activeRecord" :disabled="props.disabled" layout="vertical">
      <a-form-item
        field="title"
        :label="t('ms.json.schema.name')"
        :rules="[
          {
            required: true,
            message: t('ms.json.schema.nameNotNull'),
          },
          {
            validator: (value, callback) => {
              validRepeat(value, callback);
            },
          },
        ]"
        asterisk-position="end"
      >
        <a-input
          v-model:model-value="activeRecord.title"
          :max-length="255"
          :placeholder="t('common.pleaseInput')"
          :disabled="activeRecord.id === 'root'"
          @change="handleSettingFormChange"
        />
      </a-form-item>
      <a-form-item :label="t('common.desc')">
        <a-textarea
          v-model:model-value="activeRecord.description"
          :placeholder="t('common.pleaseInput')"
          @change="handleSettingFormChange"
        />
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
                model-event="input"
                @change="handleSettingFormChange"
              />
            </a-form-item>
            <a-form-item :label="t('ms.json.schema.maxLength')" class="w-[144px]">
              <a-input-number
                v-model:model-value="activeRecord.maxLength"
                mode="button"
                :min="0"
                :step="1"
                :precision="0"
                model-event="input"
                @change="handleSettingFormChange"
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
                model-event="input"
                @change="handleSettingFormChange"
              />
              <a-input-number
                v-else
                v-model:model-value="activeRecord.minimum"
                mode="button"
                model-event="input"
                @change="handleSettingFormChange"
              />
            </a-form-item>
            <a-form-item :label="t('ms.json.schema.maximum')" class="w-[144px]">
              <a-input-number
                v-if="activeRecord.type === 'integer'"
                v-model:model-value="activeRecord.maximum"
                mode="button"
                :step="1"
                :precision="0"
                model-event="input"
                @change="handleSettingFormChange"
              />
              <a-input-number
                v-else
                v-model:model-value="activeRecord.maximum"
                mode="button"
                model-event="input"
                @change="handleSettingFormChange"
              />
            </a-form-item>
          </template>
          <a-form-item :label="t('ms.json.schema.default')" class="flex-1">
            <a-input-number
              v-if="activeRecord.type === 'number'"
              v-model:model-value="activeRecord.defaultValue"
              mode="button"
              :placeholder="t('common.pleaseInput')"
              model-event="input"
              @change="handleSettingFormChange"
            />
            <a-input-number
              v-else-if="activeRecord.type === 'integer'"
              v-model:model-value="activeRecord.defaultValue"
              mode="button"
              :placeholder="t('common.pleaseInput')"
              :step="1"
              :precision="0"
              model-event="input"
              @change="handleSettingFormChange"
            />
            <a-input
              v-else
              v-model:model-value="activeRecord.defaultValue"
              :placeholder="t('common.pleaseInput')"
              @change="handleSettingFormChange"
            />
          </a-form-item>
        </div>
        <template v-if="activeRecord.type !== 'boolean'">
          <a-form-item :label="t('ms.json.schema.enum')">
            <a-textarea
              v-model:model-value="activeRecord.enumValues"
              :placeholder="t('ms.json.schema.enumPlaceholder')"
              @change="handleSettingFormChange"
            />
          </a-form-item>
          <template v-if="activeRecord.type === 'string'">
            <a-form-item :label="t('ms.json.schema.regex')">
              <a-input
                v-model:model-value="activeRecord.pattern"
                :placeholder="t('ms.json.schema.regexPlaceholder', { reg: '/<title(.*?)</title>' })"
                @change="handleSettingFormChange"
              />
            </a-form-item>
            <a-form-item id="jsonSchemaFormatItem" class="relative" :label="t('ms.json.schema.format')">
              <a-select
                v-model:model-value="activeRecord.format"
                :placeholder="t('common.pleaseSelect')"
                :options="formatOptions"
                popup-container="#jsonSchemaFormatItem"
                allow-clear
                @change="handleSettingFormChange"
              />
            </a-form-item>
          </template>
        </template>
      </template>
      <div v-if="activeRecord.type === 'array'" class="flex items-center gap-[24px]">
        <a-form-item :label="t('ms.json.schema.minItems')" class="w-[144px]">
          <a-input-number
            v-model:model-value="activeRecord.minItems"
            mode="button"
            :min="0"
            :step="1"
            :precision="0"
            model-event="input"
            @change="handleSettingFormChange"
          />
        </a-form-item>
        <a-form-item :label="t('ms.json.schema.maxItems')" class="w-[144px]">
          <a-input-number
            v-model:model-value="activeRecord.maxItems"
            mode="button"
            :min="0"
            :step="1"
            :precision="0"
            model-event="input"
            @change="handleSettingFormChange"
          />
        </a-form-item>
      </div>
      <div>
        <div class="mb-[8px]">{{ t('ms.json.schema.preview') }}</div>
        <MsCodeEditor
          v-model:model-value="activePreviewJsonSchemaValue"
          theme="vs"
          height="500px"
          :show-full-screen="false"
          :language="LanguageEnum.JSON"
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
    <a-spin class="block h-full w-full" :loading="batchAddLoading">
      <MsCodeEditor
        ref="batchAddCodeEditorRef"
        v-model:model-value="batchAddValue"
        theme="vs"
        height="100%"
        :language="LanguageEnum.JSON"
        :show-full-screen="false"
        show-code-format
      >
        <template #leftTitle>
          <a-radio-group v-model:model-value="batchAddType" type="button" @change="handleBatchAddTypeChange">
            <a-radio value="json">Json</a-radio>
            <a-radio value="schema">JsonSchema</a-radio>
          </a-radio-group>
        </template>
      </MsCodeEditor>
    </a-spin>
  </MsDrawer>
  <MsDrawer
    v-model:visible="previewDrawerVisible"
    :width="600"
    :title="t('common.preview')"
    :footer="false"
    @close="previewShowType = 'json'"
  >
    <a-spin class="block h-full w-full" :loading="previewDrawerLoading">
      <MsCodeEditor
        :model-value="activePreviewValue"
        theme="vs"
        height="100%"
        :language="LanguageEnum.JSON"
        :show-full-screen="false"
        read-only
      >
        <template #leftTitle>
          <a-radio-group v-model:model-value="previewShowType" type="button">
            <a-radio value="json">Json</a-radio>
            <a-radio value="schema">JsonSchema</a-radio>
          </a-radio-group>
        </template>
      </MsCodeEditor>
    </a-spin>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { FormInstance, SelectOptionData, TableData, TableRowSelection } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsFormTable, { FormTableColumn } from '@/components/pure/ms-form-table/index.vue';
  import MsParamsInput from '@/components/business/ms-params-input/index.vue';
  import MsQuickInput from '@/components/business/ms-quick-input/index.vue';

  import { convertJsonSchemaToJson } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import { getGenerateId, traverseTree } from '@/utils';

  import { TableKeyEnum } from '@/enums/tableEnum';

  import { JsonSchema, JsonSchemaItem, JsonSchemaTableItem } from './types';
  import {
    parseJsonToJsonSchemaTableData,
    parseSchemaToJsonSchemaTableData,
    parseTableDataToJsonSchema,
  } from './utils';

  const props = defineProps<{
    disabled?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'change', value: JsonSchemaTableItem[]): void;
  }>();

  const { t } = useI18n();

  const defaultItem: JsonSchemaTableItem = {
    id: '',
    type: 'string',
    title: '',
    example: '',
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
  const data = defineModel<JsonSchemaTableItem[]>('data', {
    default: () => [],
  });
  const expandKeys = defineModel<string[]>('expandKeys', {
    default: () => ['root'],
  });
  const selectedKeys = defineModel<string[]>('selectedKeys', {
    default: () => ['root'],
  });
  const rowSelection = computed<TableRowSelection | undefined>(() => {
    if (props.disabled) {
      return undefined;
    }
    return {
      type: 'checkbox',
      showCheckedAll: true,
      checkStrictly: true,
      width: 32,
    };
  });
  const formTableRef = ref<InstanceType<typeof MsFormTable>>();

  // 初始化根节点
  watchEffect(() => {
    if (data.value.length === 0) {
      data.value = [
        {
          id: 'root',
          title: 'root',
          type: 'object',
          example: '',
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
      ];
      if (selectedKeys.value.length === 0) {
        selectedKeys.value = ['root'];
      }
    }
  });

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

  const columns = computed(() => {
    const staticColumns: FormTableColumn[] = [
      {
        title: t('ms.json.schema.name'),
        dataIndex: 'title',
        slotName: 'title',
        inputType: 'input',
        size: 'medium',
        columnSelectorDisabled: true,
        fixed: 'left',
        needValidRepeat: true,
        sortIndex: 1,
      },
      {
        title: t('ms.json.schema.type'),
        dataIndex: 'type',
        slotName: 'type',
        titleSlotName: 'typeTitle',
        size: 'medium',
        width: 120,
        addLineDisabled: true,
        columnSelectorDisabled: true,
      },
      {
        title: t('ms.json.schema.value'),
        dataIndex: 'example',
        slotName: 'example',
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
        title: t('ms.json.schema.minItems'),
        dataIndex: 'minItems',
        slotName: 'minItems',
        inputType: 'inputNumber',
        size: 'medium',
        min: 0,
        precision: 0,
        addLineDisabled: true,
        showInTable: false,
        width: 120,
      },
      {
        title: t('ms.json.schema.maxItems'),
        dataIndex: 'maxItems',
        slotName: 'maxItems',
        inputType: 'inputNumber',
        size: 'medium',
        min: 0,
        precision: 0,
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
        dataIndex: 'enumValues',
        slotName: 'enumValues',
        inputType: 'textarea',
        size: 'medium',
        addLineDisabled: true,
        showInTable: false,
      },
      {
        title: t('ms.json.schema.regex'),
        dataIndex: 'pattern',
        slotName: 'pattern',
        size: 'medium',
        addLineDisabled: true,
        showInTable: false,
        isNull: (record) => record.type !== 'string',
      },
      {
        title: t('ms.json.schema.format'),
        dataIndex: 'format',
        slotName: 'format',
        size: 'medium',
        showInTable: false,
        isNull: (record) => record.type !== 'string',
        width: 100,
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
    const requiredColumn: FormTableColumn[] = [
      {
        title: t('msFormTable.paramRequired'),
        dataIndex: 'required',
        slotName: 'required',
        size: 'medium',
        inputType: 'text',
        columnSelectorDisabled: true,
        valueFormat: (record) => {
          return record.required ? t('common.yes') : t('common.no');
        },
        width: 68,
      },
    ];
    if (props.disabled) {
      return [...requiredColumn, ...staticColumns];
    }
    return staticColumns;
  });

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  function emitChange(from: string) {
    emit('change', data.value);
  }

  /**
   * 获取类型选项，根节点只能是 object 或 array
   */
  function getTypeOptions(record: JsonSchemaTableItem) {
    if (record.id === 'root') {
      return typeOptions.filter((item) => ['object', 'array'].includes(item.value as string));
    }
    return typeOptions;
  }

  /**
   * 处理类型变化
   */
  function handleTypeChange(record: JsonSchemaTableItem) {
    if (record.type === 'object' || record.type === 'array') {
      if (!record.children) {
        // 没有子节点，初始化
        record.children = [];
      }
      if (record.type === 'array') {
        record.children.forEach((e, i) => {
          e.title = `${i}`;
        });
      }
    } else {
      record.children = undefined;
    }
    emitChange('typeChange');
  }

  function toggleRequired(record: JsonSchemaTableItem) {
    if (props.disabled) {
      return;
    }
    record.required = !record.required;
    emitChange('toggleRequired');
  }

  /**
   * 添加子节点
   */
  function addChild(record: JsonSchemaTableItem) {
    if (!record.children) {
      record.children = [];
    }
    const child = {
      ...cloneDeep(defaultItem),
      id: getGenerateId(),
      title: record.type === 'array' ? `${record.children.length}` : '',
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
    data.value = [...data.value];
    emitChange('addChild');
  }

  /**
   * 删除行
   */
  function deleteLine(record: JsonSchemaTableItem, rowIndex: number) {
    if (record.parent?.children) {
      record.parent.children.splice(rowIndex, 1);
    } else {
      data.value.splice(rowIndex, 1);
    }
    emitChange('deleteLine');
  }

  /**
   * 行选择处理
   */
  function handleSelect(rowKeys: (string | number)[], rowKey: string | number, record: TableData) {
    nextTick(() => {
      record.enable = !selectedKeys.value.includes(record.id);
      if (record.enable && record.children && record.children.length > 0) {
        // 选中父节点时，选中子孙节点
        traverseTree<JsonSchemaTableItem>(record.children, (item) => {
          item.enable = true;
          if (!selectedKeys.value.includes(item.id)) {
            selectedKeys.value.push(item.id);
          }
        });
      }
      emitChange('select');
    });
  }

  function addLineIfLast(record: TableData, rowIndex: number) {
    formTableRef.value?.validateAndUpdateErrorMessageList(); // 输入名称，校验重复名
    const firstLevelLastChild = data.value[0].children?.[data.value[0].children.length - 1]; // root 的最后一个子节点
    // 当前输入的是最后一行，且 root 的最后一个子节点不是默认行数据，则自动新增一行默认数据
    if (
      rowIndex === (record.parent || data.value[0]).children.length - 1 &&
      (firstLevelLastChild?.title !== '' || firstLevelLastChild?.type !== 'string')
    ) {
      addChild(data.value[0]);
    }
    emitChange('titleInput');
  }

  function handleSelectAll(checked: boolean) {
    traverseTree<JsonSchemaTableItem>(data.value, (item) => {
      item.enable = checked;
    });
    emitChange('selectAll');
  }

  const batchAddDrawerVisible = ref(false);
  const batchAddType = ref<'json' | 'schema'>('json');
  const batchAddLoading = ref(false);
  const batchAddCodeEditorRef = ref<InstanceType<typeof MsCodeEditor>>();
  const batchAddCurrentJson = ref('');
  const batchAddCurrentSchema = ref('');
  const batchAddValue = ref(batchAddType.value === 'json' ? batchAddCurrentJson.value : batchAddCurrentSchema.value);

  function handleBatchAddTypeChange(value: string | number | boolean) {
    if (value === 'json') {
      batchAddValue.value = batchAddCurrentJson.value;
    } else {
      batchAddValue.value = batchAddCurrentSchema.value;
    }
    nextTick(() => {
      batchAddCodeEditorRef.value?.format();
    });
  }

  async function batchAdd() {
    batchAddLoading.value = true;
    let schema: JsonSchema | JsonSchemaItem | undefined;
    try {
      // 先将表格数据转换为 json schema格式
      schema = parseTableDataToJsonSchema(data.value[0] as JsonSchemaTableItem);
      batchAddCurrentSchema.value = JSON.stringify(schema);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      batchAddCurrentSchema.value = t('ms.json.schema.convertFailed');
      batchAddLoading.value = false;
      return;
    }
    try {
      // 再将 json schema 转换为 json 格式
      const res = await convertJsonSchemaToJson(schema as JsonSchema);
      batchAddCurrentJson.value = res;
      handleBatchAddTypeChange(batchAddType.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      batchAddCurrentJson.value = t('ms.json.schema.convertFailed');
    } finally {
      batchAddLoading.value = false;
    }
    batchAddDrawerVisible.value = true;
  }

  function applyBatchAdd() {
    try {
      let res: { result: JsonSchemaTableItem[]; ids: Array<string> } = { result: [], ids: [] };
      if (batchAddType.value === 'json') {
        res = parseJsonToJsonSchemaTableData(batchAddValue.value);
      } else {
        res = parseSchemaToJsonSchemaTableData(batchAddValue.value);
      }
      if (res.result.length > 0) {
        data.value = res.result;
        selectedKeys.value = res.ids;
      }
      batchAddDrawerVisible.value = false;
      emitChange('batchAdd');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const settingDrawerVisible = ref(false);
  const activeRecord = ref<any>({});
  const previewShowType = ref<'json' | 'schema'>('json');
  const activePreviewJsonValue = ref('');
  const activePreviewJsonSchemaValue = ref('');
  const activePreviewValue = computed(() => {
    return previewShowType.value === 'json' ? activePreviewJsonValue.value : activePreviewJsonSchemaValue.value;
  });

  async function openSetting(record: JsonSchemaTableItem) {
    // 浅拷贝，以保留 parent 和 children 的引用
    activeRecord.value = {
      ...record,
    };
    try {
      const schema = parseTableDataToJsonSchema(record, record.id === 'root');
      activePreviewJsonSchemaValue.value = schema ? JSON.stringify(schema) : '';
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      activePreviewJsonSchemaValue.value = t('ms.json.schema.convertFailed');
    } finally {
      settingDrawerVisible.value = true;
    }
  }

  function handleSettingFormChange() {
    activePreviewJsonSchemaValue.value = JSON.stringify(
      parseTableDataToJsonSchema(activeRecord.value, activeRecord.value.id === 'root')
    );
  }

  async function validRepeat(value: string, callback: (error?: string) => void) {
    if (activeRecord.value.parent) {
      (activeRecord.value.parent.children as Record<string, any>[])?.forEach((row) => {
        if (row.title.length && row.title === value && row.id !== activeRecord.value.id) {
          callback(`${t('ms.json.schema.name')}${t('msFormTable.paramRepeatMessage')}`);
        }
      });
    }
    callback();
  }

  const settingFormRef = ref<FormInstance>();

  /**
   * 应用设置
   */
  function applySetting() {
    settingFormRef.value?.validate((errors) => {
      if (!errors) {
        if (activeRecord.value.id === 'root') {
          data.value = [{ ...activeRecord.value }];
        } else {
          const brothers = activeRecord.value.parent?.children || [];
          const index = brothers.findIndex((item: any) => item.id === activeRecord.value.id);
          if (index > -1) {
            brothers.splice(index, 1, { ...activeRecord.value });
          }
        }
        settingDrawerVisible.value = false;
        emitChange('applySetting');
      }
    });
  }

  const showQuickInputParam = ref(false);
  const activeQuickInputRecord = ref<JsonSchemaTableItem>();
  const quickInputParamValue = ref('');

  function quickInputParams(record: JsonSchemaTableItem) {
    activeQuickInputRecord.value = record;
    showQuickInputParam.value = true;
    quickInputParamValue.value = record.example;
  }

  function clearQuickInputParam() {
    activeQuickInputRecord.value = undefined;
    quickInputParamValue.value = '';
  }

  function applyQuickInputParam() {
    if (activeQuickInputRecord.value) {
      activeQuickInputRecord.value.example = quickInputParamValue.value;
    }
    showQuickInputParam.value = false;
    clearQuickInputParam();
    emitChange('quickInputParam');
  }

  const previewDrawerVisible = ref(false);
  const previewDrawerLoading = ref(false);

  /**
   * 预览 schema
   */
  async function previewSchema() {
    previewDrawerVisible.value = true;
    previewDrawerLoading.value = true;
    let schema: JsonSchema | JsonSchemaItem | undefined;
    try {
      // 先将表格数据转换为 json schema格式
      schema = parseTableDataToJsonSchema(data.value[0] as JsonSchemaTableItem);
      activePreviewJsonSchemaValue.value = JSON.stringify(schema);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      activePreviewJsonSchemaValue.value = t('ms.json.schema.convertFailed');
      previewDrawerLoading.value = false;
      return;
    }
    try {
      // 再将 json schema 转换为 json 格式
      const res = await convertJsonSchemaToJson(schema as JsonSchema);
      activePreviewJsonValue.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      activePreviewJsonValue.value = t('ms.json.schema.convertFailed');
    } finally {
      previewDrawerLoading.value = false;
    }
  }

  defineExpose({
    previewSchema,
  });
</script>

<style lang="less">
  .ms-json-schema {
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
