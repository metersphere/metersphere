<template>
  <MsDrawer
    v-model:visible="visible"
    :ok-text="t('common.export')"
    :ok-loading="exportLoading"
    :width="800"
    min-width="800px"
    unmount-on-close
    no-content-padding
    :show-continue="false"
    :ok-disabled="!selectedList.length"
    :footer="false"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <template #title>
      <slot name="title">
        <div v-if="props.drawerTitleProps">
          <span class="text-[var(--color-text-1)]">{{ props.drawerTitleProps.title }}</span>
          <span v-if="props.drawerTitleProps.count" class="ml-1 text-[var(--color-text-4)]">
            {{ t('common.selectedCount', { count: props.drawerTitleProps.count }) }}
          </span>
        </div>
      </slot>
    </template>
    <div class="panel-wrapper">
      <div class="inner-wrapper">
        <div class="optional-field">
          <div class="optional-header">
            <div class="font-medium">
              <a-checkbox :model-value="isCheckedAll" :indeterminate="indeterminate" @change="handleChangeAll">
                <span class="font-medium text-[var(--color-text-3)]">
                  {{ props.titleProps?.selectableTitle || t('system.orgTemplate.optionalField') }}
                </span>
              </a-checkbox>
            </div>
          </div>
          <div class="p-[16px]">
            <div class="text-[var(--color-text-4)]">
              <a-checkbox
                :model-value="isCheckedSystemAll"
                :indeterminate="systemIndeterminate"
                @change="(value) => handleChange(value as boolean, 'system')"
              >
                <span class="font-medium text-[var(--color-text-4)]">
                  {{ props.titleProps?.systemTitle || t('ms-export-drawer.systemFiled') }}
                </span>
              </a-checkbox>
            </div>
            <a-checkbox-group :model-value="selectedSystemIds" @change="(value) => handleGroupChange(value, 'system')">
              <div v-if="systemList.length" class="mb-[32px]">
                <div class="flex flex-row flex-wrap">
                  <a-checkbox
                    v-for="item in systemList"
                    :key="item.key"
                    :value="item.key"
                    class="mt-[8px] w-[95px] pl-[0px]"
                    :disabled="item.key === 'name'"
                  >
                    <a-tooltip :content="item.text" position="top">
                      <div class="one-line-text max-w-[80px]">{{ item.text }}</div>
                    </a-tooltip>
                  </a-checkbox>
                </div>
              </div>
            </a-checkbox-group>
            <div v-if="customList.length" class="text-[var(--color-text-4)]">
              <a-checkbox
                :model-value="isCheckedCustomAll"
                :indeterminate="customIndeterminate"
                @change="(value) => handleChange(value as boolean, 'custom')"
              >
                <span class="font-medium text-[var(--color-text-4)]">
                  {{ t('ms-export-drawer.customFiled') }}
                </span>
              </a-checkbox>
            </div>
            <a-checkbox-group
              v-if="customList.length"
              :model-value="selectedCustomIds"
              @change="(value) => handleGroupChange(value, 'custom')"
            >
              <div class="mb-[32px]">
                <div class="flex flex-row flex-wrap">
                  <a-checkbox
                    v-for="item in customList"
                    :key="item.key"
                    :value="item.key"
                    class="mt-[8px] w-[95px] pl-[0px]"
                  >
                    <a-tooltip :content="item.text" position="top">
                      <div class="one-line-text max-w-[80px]">{{ item.text }}</div>
                    </a-tooltip>
                  </a-checkbox>
                </div>
              </div>
            </a-checkbox-group>
            <div v-if="otherList.length" class="flex flex-row items-center gap-[4px]">
              <div class="text-[var(--color-text-4)]">
                <a-checkbox
                  :model-value="isCheckedOtherAll"
                  :indeterminate="otherIndeterminate"
                  @change="(value) => handleChange(value as boolean, 'other')"
                >
                  <span class="font-medium text-[var(--color-text-4)]">
                    {{ t('ms-export-drawer.otherFiled') }}
                  </span>
                  <a-tooltip :content="t('ms-export-drawer.otherTip')" position="right">
                    <icon-question-circle
                      class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                    />
                  </a-tooltip>
                </a-checkbox>
              </div>
            </div>
            <a-checkbox-group
              v-if="otherList.length"
              :model-value="selectedOtherIds"
              @change="(value) => handleGroupChange(value, 'other')"
            >
              <div class="mb-[32px]">
                <div class="flex flex-row flex-wrap">
                  <a-checkbox
                    v-for="item in otherList"
                    :key="item.key"
                    :value="item.key"
                    class="mt-[8px] w-[95px] pl-[0px]"
                  >
                    <a-tooltip :content="item.text" position="top">
                      <div class="one-line-text max-w-[80px]">{{ item.text }}</div>
                    </a-tooltip>
                  </a-checkbox>
                </div>
              </div>
            </a-checkbox-group>
          </div>
        </div>
        <div>
          <div class="optional-header min-w-[270px]">
            <div class="font-medium">
              {{ props.titleProps?.selectedTitle || t('system.orgTemplate.selectedField') }}
              ({{ selectedList.length }})
            </div>
            <MsButton @click="handleReset">{{ t('common.clear') }}</MsButton>
          </div>
          <div class="selected-panel p-[16px]">
            <VueDraggable v-model="selectedList" ghost-class="ghost">
              <div
                v-for="element in selectedList"
                :key="element.key"
                class="mb-[8px] flex flex-row items-center gap-[4px] bg-[var(--color-text-n9)] p-[8px]"
              >
                <a-tooltip :content="element.text">
                  <div> <MsIcon type="icon-icon_drag" class="mt-[3px] text-[16px] text-[var(--color-text-4)]" /></div>
                  <div class="one-line-text ml-2 w-[178px]">{{ element.text }}</div>
                </a-tooltip>
                <icon-close
                  v-if="element.key !== 'name'"
                  :style="{ 'font-size': '14px' }"
                  class="cursor-pointer text-[var(--color-text-3)]"
                  @click="removeSelectedField(element.key)"
                />
              </div>
            </VueDraggable>
          </div>
        </div>
      </div>
    </div>
    <div class="footer !ml-[10px] w-[calc(100%-10px)]">
      <div class="flex items-center">
        <slot name="footerLeft"></slot>
      </div>
      <div class="flex items-center">
        <slot name="footerRight">
          <a-button type="secondary" :disabled="props.exportLoading" class="mr-[12px]" @click="handleDrawerCancel">
            {{ t('common.cancel') }}
          </a-button>
          <a-button
            :loading="props.exportLoading"
            type="primary"
            :disabled="!selectedList.length"
            @click="handleDrawerConfirm"
          >
            {{ t('common.export') }}
          </a-button>
        </slot>
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-模板-模板管理-创建模板-添加字段到模板抽屉
   */
  import { computed, ref } from 'vue';
  import { VueDraggable } from 'vue-draggable-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { EnvListItem } from '@/models/projectManagement/environmental';

  import { MsExportDrawerMap, MsExportDrawerOption } from './types';

  const { t } = useI18n();

  interface MsExportDrawerProps {
    visible: boolean;
    allData: MsExportDrawerMap;
    // 默认选中的字段 keys
    defaultSelectedKeys?: string[];
    isArrayColumn?: boolean;
    arrayColumn?: EnvListItem[];
    exportLoading: boolean;
    titleProps?: {
      selectableTitle: string; // 可选字段
      systemTitle: string; // 已选字段| 环境
      selectedTitle: string; // 已选字段| 环境
    };
    drawerTitleProps?: {
      title: string;
      count?: number;
    };
    disabledCancelKeys?: string[]; // 禁止取消系统字段
  }

  const props = withDefaults(defineProps<MsExportDrawerProps>(), {
    visible: false,
    exportLoading: false,
    defaultSelectedKeys: () => ['name', 'id', 'title', 'status', 'handle_user', 'content'],
    disabledCancelKeys: () => [],
  });

  const selectedList = ref<MsExportDrawerOption[]>([]); // 已选字段

  const selectedSystemIds = ref<string[]>([]); // 已选系统字段id
  const selectedCustomIds = ref<string[]>([]); // 已选自定义字段id
  const selectedOtherIds = ref<string[]>([]); // 已选其他字段id

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'update:exportLoading', value: boolean): void;
    (e: 'confirm', value: MsExportDrawerOption[]): void;
  }>();

  const visible = computed({
    get() {
      return props.visible;
    },
    set(value) {
      emit('update:visible', value);
    },
  });

  const exportLoading = computed({
    get() {
      return props.exportLoading;
    },
    set(value) {
      emit('update:exportLoading', value);
    },
  });

  const systemList = computed(() => {
    if (props.isArrayColumn && props.arrayColumn) {
      return props.arrayColumn.map((item) => {
        return {
          key: item.id,
          text: item.name,
          columnType: 'system',
        };
      });
    }
    const { systemColumns } = props.allData;
    if (systemColumns) {
      return Object.keys(systemColumns).map((key) => {
        return {
          key,
          text: systemColumns[key],
          columnType: 'system',
        };
      });
    }
    return [];
  });

  const customList = computed(() => {
    const { customColumns } = props.allData;
    if (customColumns) {
      return Object.keys(customColumns).map((key) => {
        return {
          key,
          text: customColumns[key],
          columnType: 'custom',
        };
      });
    }
    return [];
  });

  const otherList = computed(() => {
    const { otherColumns } = props.allData;
    if (otherColumns) {
      return Object.keys(otherColumns).map((key) => {
        return {
          key,
          text: otherColumns[key],
          columnType: 'other',
        };
      });
    }
    return [];
  });

  const allList = computed(() => {
    return [...systemList.value, ...customList.value, ...otherList.value];
  });

  const selectSystemIds = computed(() => systemList.value.map((e) => e.key));

  const selectCustomIds = computed(() => customList.value.map((e) => e.key));

  const selectOtherIds = computed(() => otherList.value.map((e) => e.key));

  const handleReset = () => {
    selectedList.value = allList.value.filter((item) => props.defaultSelectedKeys.includes(item.key));
  };

  const handleDrawerConfirm = () => {
    emit('confirm', selectedList.value);
    handleReset();
  };

  const handleDrawerCancel = () => {
    visible.value = false;
    handleReset();
  };

  const isCheckedAll = computed(() => {
    return selectedList.value.length === allList.value.length;
  });

  const indeterminate = computed(() => {
    return selectedList.value.length > 0 && selectedList.value.length < allList.value.length;
  });

  // 系统字段全选
  const isCheckedSystemAll = computed(
    () =>
      selectedList.value.length > 0 && selectSystemIds.value.every((id) => selectedList.value.some((e) => e.key === id))
  );

  // 自定义字段全选
  const isCheckedCustomAll = computed(
    () =>
      selectedList.value.length > 0 && selectCustomIds.value.every((id) => selectedList.value.some((e) => e.key === id))
  );

  // 其他字段全选
  const isCheckedOtherAll = computed(() => {
    return (
      selectedList.value.length > 0 && selectOtherIds.value.every((id) => selectedList.value.some((e) => e.key === id))
    );
  });

  // 系统字段半选
  const systemIndeterminate = computed(
    () =>
      selectedList.value.length > 0 &&
      selectedList.value.some((e) => selectSystemIds.value.includes(e.key)) &&
      !isCheckedSystemAll.value
  );

  // 自定义字段半选
  const customIndeterminate = computed(
    () =>
      selectedList.value.length > 0 &&
      selectedList.value.some((e) => selectCustomIds.value.includes(e.key)) &&
      !isCheckedCustomAll.value
  );

  // 其他字段半选
  const otherIndeterminate = computed(() => {
    return (
      selectedList.value.length > 0 &&
      selectedList.value.some((e) => selectOtherIds.value.includes(e.key)) &&
      !isCheckedOtherAll.value
    );
  });

  // 全选所有可选字段
  const handleChangeAll = (value: boolean | (string | number | boolean)[]) => {
    if (value) {
      selectedList.value = allList.value;
    } else {
      // 全选取消时不允许取消
      selectedList.value = selectedList.value.filter((e) => props.disabledCancelKeys.includes(e.key));
    }
  };

  const removeSelectedField = (id: string) => {
    selectedList.value = selectedList.value.filter((item) => item.key !== id);
  };

  const selectedItemIds = computed(() => [
    ...selectedSystemIds.value,
    ...selectedCustomIds.value,
    ...selectedOtherIds.value,
  ]);

  // 更新选择的列表
  function updateSelectedList() {
    selectedList.value = allList.value.filter((item) => selectedItemIds.value.includes(item.key));
  }

  // 获取全部对应类型ids
  function getIdsByType(type: string) {
    switch (type) {
      case 'system':
        return selectSystemIds.value;
      case 'custom':
        return selectCustomIds.value;
      case 'other':
        return selectOtherIds.value;
      default:
        return [];
    }
  }

  // 更新选择的字段ID
  function updateSelectedIds(type: string, ids: string[]) {
    switch (type) {
      case 'system':
        selectedSystemIds.value = ids;
        break;
      case 'custom':
        selectedCustomIds.value = ids;
        break;
      case 'other':
        selectedOtherIds.value = ids;
        break;
      default:
        break;
    }
    updateSelectedList();
  }

  // 选择组
  const handleGroupChange = (value: (string | number | boolean)[], type: string) => {
    const selectedValue: string[] = value as string[];
    updateSelectedIds(type, selectedValue);
  };

  // 全选字段类型
  function handleChange(value: boolean, type: string) {
    // 如果有禁止取消则取消时候不取消
    const ids = value ? getIdsByType(type) : [...props.disabledCancelKeys];
    updateSelectedIds(type, ids);
  }

  watchEffect(() => {
    const selectSysIds = selectedList.value.filter((e) => e.columnType === 'system').map((e) => e.key);
    const selectCusIds = selectedList.value.filter((e) => e.columnType === 'custom').map((e) => e.key);
    const selectOthIds = selectedList.value.filter((e) => e.columnType === 'other').map((e) => e.key);
    selectedSystemIds.value = selectSysIds;
    selectedCustomIds.value = selectCusIds;
    selectedOtherIds.value = selectOthIds;
  });

  watchEffect(() => {
    handleReset();
  });
</script>

<style scoped lang="less">
  :deep(.arco-drawer) {
    width: 65% !important;
  }
  .panel-wrapper {
    display: flex;
    width: 100%;
    height: calc(100vh - 112px);
    flex-flow: row nowrap;
    .inner-wrapper {
      display: flex;
      overflow: hidden;
      width: 100%;
      height: calc(100vh - 112px);
      border: 1px solid var(--color-text-n8);
      // 可选字段
      .optional-field {
        flex-grow: 1;
        height: 100%;
        border-right: 1px solid var(--color-text-n8);
      }
      .optional-header {
        padding: 0 16px;
        height: 54px;
        color: var(--color-text-3);
        background: var(--color-text-n9);
        @apply flex flex-row items-center justify-between;
      }
      .selected-panel {
        height: calc(100vh - 152px);
        @apply overflow-y-auto;
        .ms-scroll-bar();
      }
    }
  }
  .ghost {
    border: 1px dashed rgba(var(--primary-5));
    background-color: rgba(var(--primary-1));
  }
  :deep(.arco-checkbox-group .arco-checkbox) {
    margin-top: 10px;
    margin-right: 20px;
  }
  .footer {
    @apply flex items-center justify-between;

    margin: auto -16px -16px;
    padding: 12px 16px;
    box-shadow: 0 -1px 4px 0 rgb(31 35 41 / 10%);
  }
</style>
