<template>
  <div class="mb-[8px] flex items-center justify-between">
    <a-dropdown @select="addPrecondition">
      <a-button type="outline">
        <template #icon>
          <icon-plus :size="14" />
        </template>
        {{ t('apiTestDebug.precondition') }}
      </a-button>
      <template #content>
        <a-doption value="script">{{ t('apiTestDebug.script') }}</a-doption>
        <a-doption value="sql">{{ t('apiTestDebug.sql') }}</a-doption>
        <a-doption value="waitTime">{{ t('apiTestDebug.waitTime') }}</a-doption>
      </template>
    </a-dropdown>
    <div class="flex items-center">
      <a-switch v-model:model-value="openGlobalPrecondition" size="small" type="line"></a-switch>
      <div class="ml-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.openGlobalPrecondition') }}</div>
      <a-tooltip :content="t('apiTestDebug.openGlobalPreconditionTip')" position="top">
        <icon-question-circle
          class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
          size="16"
        />
      </a-tooltip>
    </div>
  </div>
  <div v-show="preconditions.length > 0" class="flex h-[calc(100%-110px)] gap-[8px]">
    <div class="h-full w-[20%] min-w-[220px]">
      <MsList
        v-model:active-item-key="activeItem.id"
        v-model:focus-item-key="focusItemKey"
        v-model:data="preconditions"
        mode="static"
        item-key-field="id"
        :item-border="false"
        class="h-full rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]"
        item-class="mb-[4px] bg-white !p-[4px_8px]"
        :item-more-actions="itemMoreActions"
        active-item-class="!bg-[rgb(var(--primary-1))] text-[rgb(var(--primary-5))]"
        :virtual-list-props="{ height: '100%', fixedSize: true }"
        draggable
        @item-click="handlePreconditionItemClick"
        @more-action-select="handlePreconditionMoreActionSelect"
        @more-actions-close="focusItemKey = ''"
      >
        <template #title="{ item, index }">
          <div class="flex items-center gap-[4px]">
            <div
              :class="`flex h-[16px] w-[16px] items-center justify-center rounded-full ${
                activeItem.id === item.id ? ' bg-white' : 'bg-[var(--color-text-n8)]'
              }`"
            >
              {{ index + 1 }}
            </div>
            <div>{{ typeMap[item.type] }}</div>
          </div>
        </template>
        <template #itemRight="{ item }">
          <a-switch v-model:model-value="item.enable" size="small" type="line"></a-switch>
        </template>
      </MsList>
    </div>
    <div class="precondition-content">
      <!-- 前置条件-脚本操作 -->
      <template v-if="activeItem.type === 'script'">
        <a-radio-group v-model:model-value="activeItem.scriptType" size="small" class="mb-[16px]">
          <a-radio value="manual">{{ t('apiTestDebug.manual') }}</a-radio>
          <a-radio value="quote">{{ t('apiTestDebug.quote') }}</a-radio>
        </a-radio-group>
        <div
          v-if="activeItem.scriptType === 'manual'"
          class="relative rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]"
        >
          <div v-if="isShowEditScriptNameInput" class="absolute left-[12px] z-10 w-[calc(100%-24px)]">
            <a-input
              ref="scriptNameInputRef"
              v-model:model-value="activeItem.name"
              :placeholder="t('apiTestDebug.preconditionScriptNamePlaceholder')"
              :max-length="255"
              show-word-limit
              size="small"
              @press-enter="isShowEditScriptNameInput = false"
              @blur="isShowEditScriptNameInput = false"
            />
          </div>
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <a-tooltip :content="activeItem.name">
                <div class="script-name-container">
                  <div class="one-line-text mr-[4px] max-w-[110px] font-medium text-[var(--color-text-1)]">
                    {{ activeItem.name }}
                  </div>
                  <MsIcon
                    type="icon-icon_edit_outlined"
                    class="edit-script-name-icon"
                    @click="showEditScriptNameInput"
                  />
                </div>
              </a-tooltip>
              <a-popover class="h-auto" position="top">
                <div class="text-[rgb(var(--primary-5))]">{{ t('apiTestDebug.scriptEx') }}</div>
                <template #content>
                  <div class="mb-[8px] flex items-center justify-between">
                    <div class="text-[14px] font-medium text-[var(--color-text-1)]">
                      {{ t('apiTestDebug.scriptEx') }}
                    </div>
                    <a-button
                      type="outline"
                      class="arco-btn-outline--secondary p-[0_8px]"
                      size="mini"
                      @click="copyScriptEx"
                    >
                      {{ t('common.copy') }}
                    </a-button>
                  </div>
                  <div class="flex h-[412px]">
                    <MsCodeEditor
                      v-model:model-value="scriptEx"
                      class="flex-1"
                      theme="MS-text"
                      width="500px"
                      height="388px"
                      :show-full-screen="false"
                      :show-theme-change="false"
                      read-only
                    >
                    </MsCodeEditor>
                  </div>
                </template>
              </a-popover>
            </div>
            <div class="flex items-center gap-[8px]">
              <a-button type="outline" class="arco-btn-outline--secondary p-[0_8px]" size="mini">
                <template #icon>
                  <MsIcon type="icon-icon_undo_outlined" class="text-var(--color-text-4)" size="12" />
                </template>
                {{ t('common.revoke') }}
              </a-button>
              <a-button type="outline" class="arco-btn-outline--secondary p-[0_8px]" size="mini" @click="clearScript">
                <template #icon>
                  <MsIcon type="icon-icon_clear" class="text-var(--color-text-4)" size="12" />
                </template>
                {{ t('common.clear') }}
              </a-button>
              <a-button
                type="outline"
                class="arco-btn-outline--secondary p-[0_8px]"
                size="mini"
                @click="() => copyPrecondition(activeItem)"
              >
                {{ t('common.copy') }}
              </a-button>
              <a-button
                type="outline"
                class="arco-btn-outline--secondary p-[0_8px]"
                size="mini"
                @click="() => deletePrecondition(activeItem)"
              >
                {{ t('common.delete') }}
              </a-button>
            </div>
          </div>
        </div>
        <div v-else class="flex h-[calc(100%-47px)] flex-col">
          <div class="mb-[16px] flex w-full items-center bg-[var(--color-text-n9)] p-[12px]">
            <div class="text-[var(--color-text-2)]">
              {{ activeItem.quoteScript.name || '-' }}
            </div>
            <a-divider margin="8px" direction="vertical" />
            <MsButton type="text" class="font-medium">
              {{ t('apiTestDebug.quote') }}
            </MsButton>
          </div>
          <a-radio-group v-model:model-value="commonScriptShowType" size="small" type="button" class="mb-[8px] w-fit">
            <a-radio value="parameters">{{ t('apiTestDebug.parameters') }}</a-radio>
            <a-radio value="scriptContent">{{ t('apiTestDebug.scriptContent') }}</a-radio>
          </a-radio-group>
          <MsBaseTable v-show="commonScriptShowType === 'parameters'" v-bind="propsRes" v-on="propsEvent">
            <template #value="{ record }">
              <a-tooltip :content="t(record.required ? 'apiTestDebug.paramRequired' : 'apiTestDebug.paramNotRequired')">
                <div
                  :class="[
                    record.required ? '!text-[rgb(var(--danger-5))]' : '!text-[var(--color-text-brand)]',
                    '!mr-[4px] !p-[4px]',
                  ]"
                >
                  <div>*</div>
                </div>
              </a-tooltip>
              {{ record.type }}
            </template>
          </MsBaseTable>
          <div v-show="commonScriptShowType === 'scriptContent'" class="h-[calc(100%-76px)]">
            <MsCodeEditor
              v-model:model-value="activeItem.quoteScript.script"
              theme="MS-text"
              height="100%"
              :show-full-screen="false"
              :show-theme-change="false"
              read-only
            >
            </MsCodeEditor>
          </div>
        </div>
      </template>
      <!-- 前置条件-SQL操作 -->
      <template v-else-if="activeItem.type === 'sql'">
        <div class="mb-[16px]">
          <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('common.desc') }}</div>
          <a-input
            v-model:model-value="activeItem.desc"
            :placeholder="t('apiTestDebug.commonPlaceholder')"
            :max-length="255"
            show-word-limit
          />
        </div>
        <div class="mb-[16px] flex w-full items-center bg-[var(--color-text-n9)] p-[12px]">
          <div class="text-[var(--color-text-2)]">
            {{ activeItem.sqlSource.name || '-' }}
          </div>
          <a-divider margin="8px" direction="vertical" />
          <MsButton type="text" class="font-medium">
            {{ t('apiTestDebug.quoteSource') }}
          </MsButton>
        </div>
        <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.sqlScript') }}</div>
        <div class="mb-[16px] h-[400px]">
          <MsCodeEditor
            v-model:model-value="activeItem.sqlSource.script"
            theme="MS-text"
            height="376px"
            :show-full-screen="false"
            :show-theme-change="false"
            read-only
          >
          </MsCodeEditor>
        </div>
        <div class="mb-[16px]">
          <div class="mb-[8px] flex items-center text-[var(--color-text-1)]">
            {{ t('apiTestDebug.storageType') }}
            <a-tooltip position="right">
              <icon-question-circle
                class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
              <template #content>
                <div>{{ t('apiTestDebug.storageTypeTip1') }}</div>
                <div>{{ t('apiTestDebug.storageTypeTip2') }}</div>
              </template>
            </a-tooltip>
          </div>
          <a-radio-group
            v-model:model-value="activeItem.sqlSource.storageType"
            size="small"
            type="button"
            class="w-fit"
          >
            <a-radio value="column">{{ t('apiTestDebug.storageByCol') }}</a-radio>
            <a-radio value="result">{{ t('apiTestDebug.storageByResult') }}</a-radio>
          </a-radio-group>
        </div>
        <div v-if="activeItem.sqlSource.storageType === 'column'" class="mb-[16px]">
          <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.storageByCol') }}</div>
          <a-input
            v-model:model-value="activeItem.sqlSource.storageByCol"
            :placeholder="t('apiTestDebug.storageByColPlaceholder', { a: '{id_1}', b: '{username_1}' })"
          />
        </div>
        <div v-else class="mb-[16px]">
          <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.storageByResult') }}</div>
          <a-input
            v-model:model-value="activeItem.sqlSource.storageByResult"
            :placeholder="t('apiTestDebug.storageByResultPlaceholder', { a: '${result}' })"
          />
        </div>
        <div class="mb-[16px]">
          <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('apiTestDebug.extractParameter') }}</div>
          <paramTable
            v-model:params="activeItem.sqlSource.params"
            :columns="sqlSourceColumns"
            :selectable="false"
            @change="handleParamTableChange"
          />
        </div>
      </template>
      <!-- 前置条件-等待时间 -->
      <div v-else>
        <div class="mb-[8px] flex items-center">
          {{ t('apiTestDebug.waitTime') }}
          <div class="text-[var(--color-text-4)]">(ms)</div>
        </div>
        <a-input-number v-model:model-value="activeItem.time" mode="button" :step="100" :min="0" class="w-[160px]" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useClipboard, useVModel } from '@vueuse/core';
  import { InputInstance, Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsList from '@/components/pure/ms-list/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import paramTable, { type ParamTableColumn } from '../../../components/paramTable.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    params: any[];
    layout: 'horizontal' | 'vertical';
    secondBoxHeight: number;
  }>();
  const emit = defineEmits<{
    (e: 'update:params', params: any[]): void;
    (e: 'change'): void;
  }>();

  const { t } = useI18n();
  // 是否开启全局前置条件
  const openGlobalPrecondition = ref(false);
  const preconditions = useVModel(props, 'params', emit);
  // 当前聚焦的前置条件
  const focusItemKey = ref<any>('');
  // 当前选中的前置条件
  const activeItem = ref(preconditions.value[0] || {});
  const typeMap = {
    script: t('apiTestDebug.script'),
    sql: t('apiTestDebug.sql'),
    waitTime: t('apiTestDebug.waitTime'),
  };
  const itemMoreActions: ActionsItem[] = [
    {
      label: 'common.copy',
      eventTag: 'copy',
    },
    {
      label: 'project.fileManagement.delete',
      eventTag: 'delete',
    },
  ];
  const scriptEx = ref(`2023-12-04 11:19:28 INFO 9026fd6a 1-1 Thread started: 9026fd6a 1-1
2023-12-04 11:19:28 ERROR 9026fd6a 1-1 Problem in JSR223 script JSR223Sampler, message: {}
In file: inline evaluation of: prev.getResponseCode() import java.net.URI; import org.apache.http.client.method . . . '' Encountered "import" at line 2, column 1.
in inline evaluation of: prev.getResponseCode() import java.net.URI; import org.apache.http.client.method . . . '' at line number 2
javax.script.ScriptException '' at line number 2
javax.script.ScriptException '' at line number 2
javax.script.ScriptException '' at line number 2
javax.script.ScriptException '' at line number 2
javax.script.ScriptException '' at line number 2
javax.script.ScriptException
org.apache.http.client.method . . . '' at line number 2
`);
  const { copy, isSupported } = useClipboard();

  function copyScriptEx() {
    if (isSupported) {
      copy(scriptEx.value);
      Message.success(t('apiTestDebug.scriptExCopySuccess'));
    } else {
      Message.warning(t('apiTestDebug.copyNotSupport'));
    }
  }

  /**
   * 添加前置条件
   * @param value script | sql | waitTime
   */
  function addPrecondition(value: string | number | Record<string, any> | undefined) {
    const id = new Date().getTime();
    switch (value) {
      case 'script':
        preconditions.value.push({
          id,
          type: 'script',
          name: t('apiTestDebug.preconditionScriptName'),
          scriptType: 'manual',
          enable: true,
          script: '',
          quoteScript: {
            name: '',
            script: scriptEx,
          },
        });
        break;
      case 'sql':
        preconditions.value.push({
          id,
          type: 'sql',
          desc: '',
          enable: true,
          sqlSource: {
            name: '',
            script: scriptEx,
            storageType: 'column',
            params: [],
          },
        });
        break;
      case 'waitTime':
        preconditions.value.push({
          id,
          type: 'waitTime',
          enable: true,
          time: 1000,
        });
        break;

      default:
        break;
    }
    activeItem.value = preconditions.value[preconditions.value.length - 1];
    emit('change');
  }

  function handlePreconditionItemClick(item: any) {
    activeItem.value = item;
  }

  /**
   * 复制前置条件
   * @param item 前置条件项
   */
  function copyPrecondition(item: Record<string, any>) {
    const copyItem = {
      ...item,
      id: new Date().getTime(),
    };
    preconditions.value.push(copyItem);
    activeItem.value = copyItem;
    emit('change');
  }

  function deletePrecondition(item: Record<string, any>) {
    preconditions.value = preconditions.value.filter((precondition) => precondition.id !== item.id);
    if (activeItem.value.id === item.id) {
      [activeItem.value] = preconditions.value;
    }
    emit('change');
  }

  /**
   * 前置条件列表项-选择更多操作项
   * @param event
   * @param item
   */
  function handlePreconditionMoreActionSelect(event: ActionsItem, item: Record<string, any>) {
    if (event.eventTag === 'copy') {
      copyPrecondition(item);
    } else if (event.eventTag === 'delete') {
      deletePrecondition(item);
    }
  }

  function clearScript() {
    activeItem.value.script = '';
  }

  // 是否显示前置脚本名称编辑框
  const isShowEditScriptNameInput = ref(false);
  const scriptNameInputRef = ref<InputInstance>();

  function showEditScriptNameInput() {
    isShowEditScriptNameInput.value = true;
    nextTick(() => {
      scriptNameInputRef.value?.focus();
    });
  }

  const commonScriptShowType = ref<'parameters' | 'scriptContent'>('parameters');
  const heightUsed = ref<number | undefined>(undefined);
  const scroll = computed(() => (props.layout === 'horizontal' ? { x: '700px' } : { x: '100%' }));
  const columns: MsTableColumn = [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'name',
      showTooltip: true,
    },
    {
      title: 'apiTestDebug.paramValue',
      dataIndex: 'value',
      slotName: 'value',
    },
    {
      title: 'apiTestDebug.desc',
      dataIndex: 'desc',
      showTooltip: true,
    },
  ];
  const { propsRes, propsEvent } = useTable(() => Promise.resolve([]), {
    scroll: scroll.value,
    heightUsed: heightUsed.value,
    columns,
  });
  propsRes.value.data = [
    {
      id: new Date().getTime(),
      required: false,
      name: 'asdasd',
      type: 'string',
      value: '',
      desc: '',
    },
    {
      id: new Date().getTime(),
      required: true,
      name: '23d23d',
      type: 'string',
      value: '',
      desc: '',
    },
  ] as any;
  watch(
    () => props.layout,
    (val) => {
      heightUsed.value = val === 'horizontal' ? 422 : 422 + props.secondBoxHeight;
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props.secondBoxHeight,
    (val) => {
      if (props.layout === 'vertical') {
        heightUsed.value = 422 + val;
      }
    },
    {
      immediate: true,
    }
  );

  const sqlSourceColumns: ParamTableColumn[] = [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'name',
      slotName: 'name',
    },
    {
      title: 'apiTestDebug.paramValue',
      dataIndex: 'value',
      slotName: 'value',
      isNormal: true,
    },
    {
      title: '',
      slotName: 'operation',
      width: 50,
    },
  ];

  function handleParamTableChange(resultArr: any[], isInit?: boolean) {
    activeItem.value.sqlSource.params = [...resultArr];
    if (!isInit) {
      emit('change');
    }
  }
</script>

<style lang="less" scoped>
  .precondition-content {
    @apply flex-1 overflow-y-auto;
    .ms-scroll-bar();

    padding: 16px;
    border: 1px solid rgb(var(--color-text-n8));
    border-radius: var(--border-radius-small);
  }
  .script-name-container {
    @apply flex items-center;

    margin-right: 16px;
    &:hover {
      .edit-script-name-icon {
        @apply visible;
      }
    }
    .edit-script-name-icon {
      @apply invisible cursor-pointer;

      color: rgb(var(--primary-5));
    }
  }
  :deep(.arco-table-th) {
    background-color: var(--color-text-n9);
  }
  :deep(.arco-table-cell) {
    padding: 16px 12px;
  }
</style>
