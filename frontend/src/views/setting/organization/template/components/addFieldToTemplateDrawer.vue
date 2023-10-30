<template>
  <MsDrawer
    v-model:visible="showAddDrawer"
    :title="t('system.orgTemplate.createField')"
    :ok-text="t('system.orgTemplate.save')"
    :ok-loading="drawerLoading"
    :width="800"
    min-width="800px"
    unmount-on-close
    :show-continue="false"
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
          <div class="optional-panel p-4">
            <div class="mb-2 font-medium text-[var(--color-text-3)]">{{ t('system.orgTemplate.systemField') }}</div>
            <div>
              <div>
                <a-checkbox-group v-model="selectSystemIds" class="checkboxContainer">
                  <div v-for="field in systemField" :key="field.id" class="item checkbox">
                    <a-checkbox :value="field.id" :disabled="field.internal"
                      ><a-tooltip :content="field.name">
                        <div>{{ field.name }}</div></a-tooltip
                      ></a-checkbox
                    >
                  </div>
                </a-checkbox-group>
              </div>
            </div>
            <div class="my-2 mt-8 font-medium text-[var(--color-text-3)]">{{
              t('system.orgTemplate.customField')
            }}</div>
            <div>
              <a-checkbox-group v-model="selectCustomIds" class="checkboxContainer">
                <div v-for="field in customField" :key="field.id" class="item">
                  <a-checkbox :value="field.id"
                    ><a-tooltip :content="field.name">
                      <div class="checkbox">{{ field.name }}</div></a-tooltip
                    ></a-checkbox
                  >
                </div>
              </a-checkbox-group>
            </div>
            <EditFieldDrawer ref="fieldDrawerRef" v-model:visible="showFieldDrawer" @success="okHandler" />
            <div>
              <a-button class="mt-1 px-0" type="text" :disabled="totalData.length > 20" @click="createField">
                <template #icon>
                  <icon-plus class="text-[14px]" />
                </template>
                {{ t('system.orgTemplate.addField') }}
              </a-button>
            </div>
          </div>
        </div>
        <div class="selected-field w-[272px]">
          <div class="optional-header">
            <div class="font-medium">{{ t('system.orgTemplate.selectedField') }}</div>
            <MsButton @click="clearHandler">{{ t('system.orgTemplate.clear') }}</MsButton>
          </div>
          <div class="selected-list p-4">
            <Draggable tag="div" :list="selectedList" ghost-class="ghost" item-key="dateIndex">
              <template #item="{ element }">
                <div class="selected-item">
                  <a-tooltip :content="element.name">
                    <span>
                      <MsIcon type="icon-icon_drag" class="mt-[3px] text-[16px] text-[var(--color-text-4)]"
                    /></span>
                    <span class="one-line-text ml-2 w-[270px]">{{ element.name }}</span>
                  </a-tooltip>
                  <icon-close
                    v-if="!element.internal"
                    :style="{ 'font-size': '14px' }"
                    class="cursor-pointer text-[var(--color-text-3)]"
                    @click="removeSelectedField(element.id)"
                  />
                </div>
              </template>
            </Draggable>
          </div>
        </div>
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import EditFieldDrawer from './editFieldDrawer.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { DefinedFieldItem } from '@/models/setting/template';

  import Draggable from 'vuedraggable';

  const { t } = useI18n();

  const showAddDrawer = ref<boolean>(false);
  const drawerLoading = ref<boolean>(false);

  const props = defineProps<{
    visible: boolean;
    totalData: DefinedFieldItem[]; // 所有字段
    tableSelectData: DefinedFieldItem[]; // 表格选择字段
  }>();

  const emit = defineEmits(['confirm', 'update:visible', 'update-data']);

  const totalList = ref<DefinedFieldItem[]>([]);

  // 计算系统字段
  const systemField = computed(() => {
    return totalList.value.filter((item: any) => item.internal);
  });

  // 计算自定义字段
  const customField = computed(() => {
    return totalList.value.filter((item: any) => !item.internal);
  });

  const selectSystemIds = ref<string[]>([]);
  const selectCustomIds = ref<string[]>([]);

  // 监视回显字段
  watch(
    () => props.tableSelectData,
    (val) => {
      const sysField = props.tableSelectData.filter((item) => item.internal);
      const cusField = props.tableSelectData.filter((item) => !item.internal);
      selectSystemIds.value = sysField.map((item) => item.id);
      selectCustomIds.value = cusField.map((item) => item.id);
    }
  );

  // 计算当前全部选择的字段id
  const totalIds = computed(() => {
    return [...new Set([...selectSystemIds.value, ...selectCustomIds.value])];
  });

  const selectedList = ref<DefinedFieldItem[]>([]);

  // 计算当前系统字段是否全选
  const isCheckSystemIdsAll = computed(() => {
    return systemField.value.length === selectSystemIds.value.length;
  });
  // 计算当前自定义字段是否全选
  const isCheckCustomIdsAll = computed(() => {
    return customField.value.length === selectCustomIds.value.length;
  });
  // 计算是否全选
  const isCheckedAll = computed(() => {
    return isCheckSystemIdsAll.value && isCheckCustomIdsAll.value;
  });
  // 计算是否半选
  const indeterminate = computed(() => {
    return selectSystemIds.value.length + selectCustomIds.value.length === selectCustomIds.value.length;
  });

  // 全选
  const handleChangeAll = (value: any) => {
    if (value) {
      selectSystemIds.value = systemField.value.map((item) => item.id);
      selectCustomIds.value = customField.value.map((item) => item.id);
    } else {
      selectCustomIds.value = [];
    }
  };

  // 监视选择列表顺序按照选择列表排序
  watch(
    () => totalIds.value,
    (val) => {
      const res = totalList.value.filter((item) => val.indexOf(item.id) > -1);
      const result = res.sort((a, b) => {
        return val.indexOf(a.id) - val.indexOf(b.id);
      });
      selectedList.value = result;
    }
  );

  watchEffect(() => {
    selectedList.value = props.tableSelectData;
    const sysField = props.tableSelectData.filter((item) => item.internal);
    const cusField = props.tableSelectData.filter((item) => !item.internal);
    selectSystemIds.value = sysField.map((item) => item.id);
    selectCustomIds.value = cusField.map((item) => item.id);
    selectedList.value = customField.value;
  });

  // 移除已选择字段
  const removeSelectedField = (id: string) => {
    selectCustomIds.value = selectCustomIds.value.filter((item) => item !== id);
    selectedList.value = selectedList.value.filter((item) => item.id !== id);
  };

  // 新增字段
  const showFieldDrawer = ref<boolean>(false);
  const createField = () => {
    showFieldDrawer.value = true;
  };

  // 清空
  const clearHandler = () => {
    selectCustomIds.value = [];
  };

  const handleDrawerCancel = () => {
    showAddDrawer.value = false;
    selectCustomIds.value = props.tableSelectData.filter((item) => !item.internal).map((item) => item.id);
    selectSystemIds.value = props.tableSelectData.filter((item) => item.internal).map((item) => item.id);
  };

  const handleDrawerConfirm = () => {
    emit('confirm', selectedList.value);
    showAddDrawer.value = false;
  };

  const okHandler = () => {
    // eslint-disable-next-line vue/custom-event-name-casing
    emit('update-data');
  };

  watch(
    () => showAddDrawer.value,
    (val) => {
      emit('update:visible', val);
    }
  );
  watch(
    () => props.visible,
    (val) => {
      showAddDrawer.value = val;
    }
  );
  watchEffect(() => {
    totalList.value = props.totalData;
  });
</script>

<style scoped lang="less">
  :deep(.arco-drawer) {
    width: 65% !important;
  }
  .panel-wrapper {
    width: 100%;
    height: 100%;
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
        .optional-header {
          padding: 0 16px;
          height: 54px;
          color: var(--color-text-3);
          background: var(--color-text-n9);
          @apply flex items-center justify-between;
        }
      }
      // 已选字段
      .selected-field {
        width: 272px;
        .optional-header {
          padding: 0 16px;
          height: 54px;
          color: var(--color-text-3);
          background: var(--color-text-n9);
          @apply flex items-center justify-between;
        }
        .selected-list {
          .selected-item {
            height: 36px;
            background: var(--color-bg-3);
            cursor: move;
            @apply mb-2 flex items-center justify-between rounded px-2;
          }
        }
      }
    }
  }
  .checkboxContainer {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(116px, 1fr));
    grid-gap: 16px;
    .checkbox {
      width: 90px;
      white-space: nowrap;
      @apply overflow-hidden text-ellipsis;
    }
  }
  .ghost {
    border: 1px dashed rgba(var(--primary-5));
    background-color: rgba(var(--primary-1));
  }
</style>
