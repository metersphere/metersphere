<template>
  <MsDrawer
    v-model:visible="showSettingVisible"
    :title="t('caseManagement.featureCase.detailDisplaySetting')"
    :width="480"
    unmount-on-close
    :footer="false"
    class="column-drawer"
    @cancel="handleCancel"
  >
    <div class="header mb-2 flex h-[22px] items-center justify-between">
      <div class="flex items-center text-[var(--color-text-4)]"
        >{{ t('caseManagement.featureCase.detailDisplaySetting') }}

        <a-tooltip>
          <template #content>
            <div>{{ t('caseManagement.featureCase.tabShowSetting') }} </div>
            <div>{{ t('caseManagement.featureCase.closeModuleTab') }}</div>
            <div>{{ t('caseManagement.featureCase.enableModuleTab') }}</div>
          </template>
          <span class="inline-block align-middle">
            <icon-question-circle class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
          /></span>
        </a-tooltip>
      </div>
      <MsButton :disabled="!hasChange" class="cursor-pointer text-[rgb(var(--primary-5))]" @click="handleReset"
        >{{ t('caseManagement.featureCase.recoverDefault') }}
      </MsButton>
    </div>

    <div class="ms-table-column-seletor">
      <div class="flex-col">
        <div v-for="item in nonCloseColumn" :key="item.value" class="column-item">
          <div>{{ t(item.label) }}</div>
          <a-switch v-model="item.isShow" disabled size="small" type="line" @change="handleSwitchChange" />
        </div>
      </div>
      <a-divider orientation="center" class="non-sort"
        ><span class="one-line-text text-[12px] text-[var(--color-text-4)]">{{
          t('project.environmental.nonClose')
        }}</span></a-divider
      >
      <VueDraggable
        v-model="couldCloseColumn"
        class="ms-assertion-body-left"
        ghost-class="ghost"
        handle=".column-drag-item"
      >
        <div v-for="element in couldCloseColumn" :key="element.value" class="column-drag-item">
          <div class="flex w-[90%] items-center">
            <MsIcon type="icon-icon_drag" class="sort-handle cursor-move text-[16px] text-[var(--color-text-4)]" />
            <span class="ml-[8px]">{{ t(element.label) }}</span>
          </div>
          <a-switch v-model="element.isShow" size="small" type="line" @change="handleSwitchChange" />
        </div>
      </VueDraggable>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { VueDraggable } from 'vue-draggable-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';

  import type { TabItemType } from '@/models/caseManagement/featureCase';

  const { t } = useI18n();

  const featureCaseStore = useFeatureCaseStore();

  const props = defineProps<{
    visible: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'initData'): void;
  }>();

  const showSettingVisible = ref<boolean>(false);

  const nonCloseColumn = ref<TabItemType[]>([]);

  const couldCloseColumn = ref<TabItemType[]>([]);

  const hasChange = ref(false);

  watch(
    () => props.visible,
    (val) => {
      showSettingVisible.value = val;
    }
  );

  watch(
    () => showSettingVisible.value,
    (val) => {
      emit('update:visible', val);
    }
  );

  const handleCancel = async () => {
    await featureCaseStore.setContentTabList([...nonCloseColumn.value, ...couldCloseColumn.value]);
    emit('initData');
  };

  const loadTab = async () => {
    const res = (await featureCaseStore.getContentTabList()) || [];
    nonCloseColumn.value = res.filter((item) => !item.canHide);
    couldCloseColumn.value = res.filter((item) => item.canHide);
  };

  const handleReset = () => {
    loadTab();
    hasChange.value = false;
  };

  const handleSwitchChange = () => {
    hasChange.value = true;
  };

  watch(
    () => props.visible,
    (value) => {
      if (value) {
        hasChange.value = false;
      }
    }
  );

  onBeforeMount(() => {
    loadTab();
  });
</script>

<style scoped lang="less">
  .itemTab {
    height: 38px;
    @apply flex items-center justify-between p-3;
  }
</style>

<style>
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
    padding: 8px 16px;
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
