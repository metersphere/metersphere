<template>
  <MsDrawer
    v-model:visible="visible"
    :ok-text="t('common.export')"
    :ok-loading="drawerLoading"
    :width="800"
    min-width="800px"
    unmount-on-close
    :show-continue="false"
    :ok-disabled="!selectedList.length"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <div class="panel-wrapper">
      <div class="inner-wrapper">
        <div class="optional-field">
          <div class="optional-header">
            <div class="font-medium">{{ t('system.orgTemplate.optionalField') }}</div>
            <a-checkbox :model-value="isCheckedAll" :indeterminate="indeterminate" @change="handleChangeAll">
              <span class="font-medium text-[var(--color-text-3)]">{{ t('system.orgTemplate.selectAll') }}</span>
            </a-checkbox>
          </div>
          <div class="p-[16px]">
            <a-checkbox-group :model-value="selectedIds" @change="handleGroupChange">
              <div v-if="systemList.length" class="mb-[32px]">
                <div class="text-[var(--color-text-4)]">{{ t('ms-export-drawer.systemFiled') }}</div>
                <div class="flex flex-row flex-wrap">
                  <a-checkbox
                    v-for="item in systemList"
                    :key="item.key"
                    :value="item.key"
                    class="mt-[8px] w-[95px] pl-[0px]"
                    :disabled="item.key === 'name'"
                  >
                    <a-tooltip :content="item.text">
                      <span class="one-line-text">{{ item.text }}</span>
                    </a-tooltip>
                  </a-checkbox>
                </div>
              </div>
              <div v-if="customList.length" class="mb-[32px]">
                <div class="text-[var(--color-text-4)]">{{ t('ms-export-drawer.customFiled') }}</div>
                <div class="flex flex-row flex-wrap">
                  <a-checkbox
                    v-for="item in customList"
                    :key="item.key"
                    :value="item.key"
                    class="mt-[8px] w-[95px] pl-[0px]"
                  >
                    <a-tooltip :content="item.text">
                      <span class="one-line-text">{{ item.text }}</span>
                    </a-tooltip>
                  </a-checkbox>
                </div>
              </div>
              <div v-if="otherList.length" class="mb-[32px]">
                <div class="flex flex-row items-center gap-[4px]">
                  <div class="text-[var(--color-text-4)]"> {{ t('ms-export-drawer.otherFiled') }}</div>
                  <a-tooltip :content="t('ms-export-drawer.otherTip')" position="right">
                    <icon-question-circle class="text-[rgb(var(--primary-5))]" />
                  </a-tooltip>
                </div>
                <div class="flex flex-row flex-wrap">
                  <a-checkbox
                    v-for="item in otherList"
                    :key="item.key"
                    :value="item.key"
                    class="mt-[8px] w-[95px] pl-[0px]"
                  >
                    <a-tooltip :content="item.text">
                      <div class="one-line-text">
                        {{ item.text }}
                      </div>
                    </a-tooltip>
                  </a-checkbox>
                </div>
              </div>
            </a-checkbox-group>
          </div>
        </div>
        <div>
          <div class="optional-header min-w-[270px]">
            <div class="font-medium">{{ t('system.orgTemplate.selectedField') }}</div>
            <MsButton @click="handleReset">{{ t('system.orgTemplate.clear') }}</MsButton>
          </div>
          <div class="p-[16px]">
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
    <template #title>
      <slot name="title"></slot>
    </template>
  </MsDrawer>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-模版-模版管理-创建模板-添加字段到模板抽屉
   */
  import { computed, ref } from 'vue';
  import { VueDraggable } from 'vue-draggable-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { MsExportDrawerMap, MsExportDrawerOption } from './types';

  const { t } = useI18n();

  const drawerLoading = ref<boolean>(false);

  interface MsExportDrawerProps {
    visible: boolean;
    allData: MsExportDrawerMap;
    // 默认选中的字段 keys
    defaultSelectedKeys?: string[];
  }

  const props = withDefaults(defineProps<MsExportDrawerProps>(), {
    visible: false,
    defaultSelectedKeys: () => ['name', 'id', 'title', 'status', 'handle_user', 'content'],
  });

  const selectedList = ref<MsExportDrawerOption[]>([]); // 已选字段
  const selectedIds = ref<string[]>([]); // 已选字段id

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
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

  const systemList = computed(() => {
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

  const handleReset = () => {
    selectedList.value = allList.value.filter((item) => props.defaultSelectedKeys.includes(item.key));
  };

  const handleDrawerConfirm = () => {
    emit('confirm', selectedList.value);
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

  const handleChangeAll = (value: boolean | (string | number | boolean)[]) => {
    if (value) {
      selectedList.value = allList.value;
    } else {
      selectedList.value = [];
    }
  };

  const removeSelectedField = (id: string) => {
    selectedList.value = selectedList.value.filter((item) => item.key !== id);
  };

  const handleGroupChange = (value: (string | number | boolean)[]) => {
    selectedList.value = allList.value.filter((item) => value.includes(item.key));
  };

  watchEffect(() => {
    selectedIds.value = selectedList.value.map((item) => item.key);
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
    height: 100%;
    flex-flow: row nowrap;
    .inner-wrapper {
      display: flex;
      overflow: hidden;
      width: 100%;
      height: 100%;
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
    }
  }
  .ghost {
    border: 1px dashed rgba(var(--primary-5));
    background-color: rgba(var(--primary-1));
  }
</style>
