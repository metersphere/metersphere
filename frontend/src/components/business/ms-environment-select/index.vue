<template>
  <div>
    <MsSelect
      v-model:model-value="currentEnv"
      :options="envOptions"
      :loading="envLoading"
      allow-search
      :size="props.size"
      @popup-visible-change="popupVisibleChange"
    >
      <template #prefix>
        <div :class="[`flex cursor-pointer ${props.size === 'mini' ? 'p-[4px]' : 'p-[8px]'}`]" @click.stop="goEnv">
          <svg-icon width="14px" height="14px" :name="'icon_env'" class="text-[var(--color-text-4)]" />
        </div>
      </template>
    </MsSelect>
  </div>
</template>

<script setup lang="ts">
  import { SelectOptionData } from '@arco-design/web-vue';

  import MsSelect from '@/components/business/ms-select';

  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';

  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';

  const props = defineProps<{
    env?: string;
    size?: 'mini' | 'small' | 'medium' | 'large';
  }>();

  const appStore = useAppStore();
  const { openNewPage } = useOpenNewPage();

  const envLoading = ref(false);
  const envOptions = ref<SelectOptionData[]>([]);
  const currentEnv = computed({
    get: () => appStore.currentEnvConfig?.id || '',
    set: (val: string) => {
      appStore.setEnvConfig(val);
    },
  });

  watch(
    () => props.env,
    (val) => {
      if (val) {
        appStore.setEnvConfig(val);
      }
    },
    {
      immediate: true,
    }
  );

  async function initEnvList() {
    try {
      envLoading.value = true;
      await appStore.initEnvList(props.env);
      envOptions.value = appStore.envList.map((item) => ({
        label: item.name,
        value: item.id,
      }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      envLoading.value = false;
    }
  }

  async function popupVisibleChange(visible: boolean) {
    if (visible) {
      await initEnvList();
    }
  }

  function goEnv() {
    openNewPage(ProjectManagementRouteEnum.PROJECT_MANAGEMENT_ENVIRONMENT_MANAGEMENT);
  }

  onBeforeMount(() => {
    initEnvList();
  });
</script>

<style lang="less" scoped>
  :deep(.arco-select) {
    padding-right: 8px;
    padding-left: 0;
    width: 200px;
    &.arco-select-view-size-mini {
      width: 113px;
    }
  }

  .ms-input-group--prepend();
  :deep(.arco-select-view-prefix) {
    margin-right: 8px;
    padding-right: 0;
    border-right: 1px solid var(--color-text-input-border);
  }
</style>
