<template>
  <MsDrawer
    :visible="innerVisible"
    :width="480"
    unmount-on-close
    :footer="false"
    :title="t('project.environmental.displaySetting')"
    @cancel="handleCancel"
  >
    <div class="ms-table-column-seletor">
      <div class="mb-2 flex items-center justify-between">
        <div class="text-[var(--color-text-4)]">{{ t('project.environmental.displaySetting') }}</div>
        <MsButton v-if="hasChange" @click="handleReset">{{ t('msTable.columnSetting.resetDefault') }}</MsButton>
      </div>
      <div class="flex-col">
        <div v-for="item in nonCloseColumn" :key="item.value" class="column-item">
          <div>{{ t(item.label) }}</div>
          <a-switch v-model="item.isShow" disabled size="small" type="line" @change="handleSwitchChange" />
        </div>
      </div>
      <a-divider orientation="center" class="non-sort"
        ><span class="one-line-text text-xs text-[var(--color-text-4)]">{{
          t('project.environmental.nonClose')
        }}</span></a-divider
      >
      <div v-for="element in couldCloseColumn" :key="element.value" class="column-drag-item">
        <div class="flex w-[90%] items-center">
          <span class="ml-[8px]">{{ t(element.label) }}</span>
        </div>
        <a-switch v-model="element.isShow" size="small" type="line" @change="handleSwitchChange" />
      </div>
    </div>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { onBeforeMount, ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  import { ContentTabItem } from '@/models/projectManagement/environmental';

  const store = useProjectEnvStore();
  const { t } = useI18n();
  const innerVisible = defineModel<boolean>('visible', { default: false });
  const nonCloseColumn = ref<ContentTabItem[]>([]);
  // 可以拖拽的列
  const couldCloseColumn = ref<ContentTabItem[]>([]);
  // 是否有改动
  const hasChange = ref(false);

  const emit = defineEmits<{
    (e: 'initData'): void;
  }>();

  const handleCancel = async () => {
    await store.setContentTabList([...nonCloseColumn.value, ...couldCloseColumn.value]);
    emit('initData');
  };

  const loadColumn = async () => {
    const res = (await store.getContentTabList()) || [];
    nonCloseColumn.value = res.filter((item) => !item.canHide);
    couldCloseColumn.value = res.filter((item) => item.canHide);
  };

  const handleReset = () => {
    loadColumn();
    hasChange.value = false;
  };

  const handleSwitchChange = () => {
    hasChange.value = true;
  };

  onBeforeMount(() => {
    loadColumn();
  });
</script>

<style lang="less" scoped>
  :deep(.arco-divider-horizontal) {
    margin: 16px 0;
    border-bottom-color: var(--color-text-n8);
  }
  :deep(.arco-divider-text) {
    padding: 0 8px;
  }
  .mode-button {
    display: flex;
    flex-flow: row nowrap;
    align-items: center;
    .active-color {
      color: rgba(var(--primary-5));
    }
    .mode-button-title {
      margin-left: 4px;
    }
  }
  .column-item {
    display: flex;
    flex-flow: row nowrap;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px 8px 36px;
    &:hover {
      border-radius: 6px;
      background: var(--color-text-n9);
    }
  }
  .column-drag-item {
    display: flex;
    flex-flow: row nowrap;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    &:hover {
      border-radius: 6px;
      background-color: var(--color-text-n9);
    }
  }
  .ghost {
    border: 1px dashed rgba(var(--primary-5));
    background-color: rgba(var(--primary-1));
  }
  .non-sort {
    font-size: 12px;
    line-height: 16px;
  }
</style>
