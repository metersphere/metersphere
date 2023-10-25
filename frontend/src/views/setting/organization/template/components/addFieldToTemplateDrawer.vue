<template>
  <MsDrawer
    v-model:visible="showAddDrawer"
    :title="t('system.orgTemplate.createField')"
    :ok-text="t('system.orgTemplate.save')"
    :ok-loading="drawerLoading"
    :width="800"
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
            <a-checkbox :model-value="checkedAll" :indeterminate="indeterminate" @change="handleChangeAll">
              <span class="font-medium text-[var(--color-text-3)]">{{ t('system.orgTemplate.selectAll') }}</span>
            </a-checkbox>
          </div>
          <div class="optional-panel p-4">
            <div class="mb-2 font-medium text-[var(--color-text-3)]">{{ t('system.orgTemplate.systemField') }}</div>
            <div>
              <a-checkbox-group v-model="selectSystemData">
                <a-grid :cols="4" :col-gap="16" :row-gap="4">
                  <a-grid-item v-for="field in systemField" :key="field.id">
                    <a-checkbox :value="field.id" :disabled="field.internal"
                      ><a-tooltip :content="field.name">
                        <div class="checkbox">{{ field.name }}</div></a-tooltip
                      ></a-checkbox
                    >
                  </a-grid-item>
                </a-grid>
              </a-checkbox-group>
            </div>
            <div class="my-2 mt-8 font-medium text-[var(--color-text-3)]">{{
              t('system.orgTemplate.customField')
            }}</div>
            <a-checkbox-group v-model="selectCustomField">
              <a-grid :cols="4" :col-gap="16" :row-gap="4">
                <a-grid-item v-for="field in customField" :key="field.id">
                  <a-checkbox :value="field.id"
                    ><a-tooltip :content="field.name">
                      <div class="checkbox">{{ field.name }}</div></a-tooltip
                    ></a-checkbox
                  >
                </a-grid-item>
              </a-grid>
            </a-checkbox-group>
            <EditFieldDrawer ref="fieldDrawerRef" v-model:visible="showFieldDrawer" @success="okHandler" />
            <a-button class="mt-1 px-0" type="text" @click="createField">
              <template #icon>
                <icon-plus class="text-[14px]" />
              </template>
              {{ t('system.orgTemplate.addField') }}
            </a-button>
          </div>
        </div>
        <div class="selected-field">
          <div class="optional-header">
            <div class="font-medium">{{ t('system.orgTemplate.selectedField') }}</div>
            <MsButton @click="clearHandler">{{ t('system.orgTemplate.clear') }}</MsButton>
          </div>
          <div class="selected-list p-4">
            <MsList
              :data="selectedFields"
              :bordered="false"
              :split="false"
              item-border
              no-hover
              :virtual-list-props="{
                height: 'calc(100vh - 226px)',
              }"
            >
              <template #item="{ item }">
                <div class="selected-item">
                  <a-tooltip :content="item.name">
                    <span class="one-line-text w-[270px]">{{ item.name }}</span>
                  </a-tooltip>
                  <icon-close
                    v-if="!item.internal"
                    :style="{ 'font-size': '14px' }"
                    class="cursor-pointer text-[var(--color-text-3)]"
                    @click="removeSelectedField(item.id)"
                  />
                </div>
              </template>
            </MsList>
          </div>
        </div>
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsList from '@/components/pure/ms-list/index.vue';
  import EditFieldDrawer from './editFieldDrawer.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { DefinedFieldItem } from '@/models/setting/template';

  const { t } = useI18n();

  const showAddDrawer = ref<boolean>(false);
  const drawerLoading = ref<boolean>(false);

  const props = defineProps<{
    visible: boolean;
    selectedData: DefinedFieldItem[]; // 已选字段
    systemData: DefinedFieldItem[]; // 所有系统字段
    customData: DefinedFieldItem[]; // 所有自定义字段
  }>();

  const emit = defineEmits(['confirm', 'update:visible', 'update']);

  // 模板系统字段
  const systemField = ref<DefinedFieldItem[]>([]);
  // 自定义字段
  const customField = ref<DefinedFieldItem[]>([]);
  // 所有模板总字段
  const totalTemplateField = ref<DefinedFieldItem[]>([]);
  // 可选系统字段
  const selectSystemData = ref<string[]>([]);
  // 可选自定义字段
  const selectCustomField = ref<string[]>([]);
  // 半选状态
  const indeterminate = ref<boolean>(false);

  const checkedAll = ref<boolean>(false);

  // 全选
  const handleChangeAll = (value: any) => {
    indeterminate.value = false;
    if (value) {
      checkedAll.value = true;
      selectSystemData.value = systemField.value.map((item) => item.id);
      selectCustomField.value = customField.value.map((item) => item.id);
    } else {
      checkedAll.value = false;
      selectSystemData.value = [];
      selectCustomField.value = [];
    }
  };

  // 已选择列表
  const selectedFields = ref<DefinedFieldItem[]>([]);
  const getSelectedField = () => {
    const totalSelectIds = [...selectSystemData.value, ...selectCustomField.value];
    selectedFields.value = totalTemplateField.value.filter((item) => totalSelectIds.indexOf(item.id) > -1);
  };

  // 处理全选&半选&未选
  const isCheckedAll = () => {
    totalTemplateField.value = [...props.systemData, ...props.customData];
    systemField.value = props.systemData;
    customField.value = props.customData;
    const systemAll = selectSystemData.value.length === systemField.value.length;
    const customAll = selectCustomField.value.length === customField.value.length;
    if (systemAll && customAll) {
      checkedAll.value = true;
      indeterminate.value = false;
    } else if (selectCustomField.value.length === 0 || selectCustomField.value.length === 0) {
      checkedAll.value = false;
      indeterminate.value = false;
    } else {
      checkedAll.value = false;
      indeterminate.value = true;
    }
  };

  watchEffect(() => {
    isCheckedAll();
    // 已经选择的列表
    getSelectedField();
  });

  // 移除已选择字段
  const removeSelectedField = (id: string) => {
    selectSystemData.value = selectSystemData.value.filter((item) => item !== id);
    selectCustomField.value = selectCustomField.value.filter((item) => item !== id);
  };

  // 新增字段
  const showFieldDrawer = ref<boolean>(false);
  const createField = () => {
    showFieldDrawer.value = true;
  };

  // 清空
  const clearHandler = () => {
    selectSystemData.value = [];
    selectCustomField.value = [];
  };

  const handleDrawerCancel = () => {
    showAddDrawer.value = false;
  };

  const handleDrawerConfirm = () => {
    emit('confirm', selectedFields.value);
    showAddDrawer.value = false;
  };

  const okHandler = () => {
    emit('update');
  };

  // 针对外部操作的进行回显
  const showSelectField = () => {
    const selectIds = props.selectedData.map((item) => item.id);
    // 回显系统字段
    selectSystemData.value = systemField.value.filter((item) => selectIds.indexOf(item.id) > -1).map((item) => item.id);
    // 回显自定义字段
    selectCustomField.value = customField.value
      .filter((item) => selectIds.indexOf(item.id) > -1)
      .map((item) => item.id);
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

  defineExpose({
    removeSelectedField,
    showSelectField,
  });
</script>

<style scoped lang="less">
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
        .optional-panel {
          .checkbox {
            width: 74px;
            white-space: nowrap;
            @apply overflow-hidden text-ellipsis;
          }
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
            @apply mb-2 flex items-center justify-between rounded px-2;
          }
        }
      }
    }
  }
</style>
