<template>
  <div>
    <a-select
      v-model:model-value="currentEnv"
      :options="envOptions"
      class="!w-[200px] pl-0 pr-[8px]"
      :loading="envLoading"
      allow-search
      @change="initEnvironment"
      @popup-visible-change="popupVisibleChange"
    >
      <template #prefix>
        <div class="flex cursor-pointer p-[8px]" @click.stop="goEnv">
          <svg-icon width="14px" height="14px" :name="'icon_env'" class="text-[var(--color-text-4)]" />
        </div>
      </template>
    </a-select>
  </div>
</template>

<script setup lang="ts">
  import { SelectOptionData } from '@arco-design/web-vue';

  import { getEnvironment, getEnvList } from '@/api/modules/api-test/common';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';

  import { EnvConfig } from '@/models/projectManagement/environmental';
  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';

  const appStore = useAppStore();
  const { openNewPage } = useOpenNewPage();

  const currentEnv = defineModel<string>('currentEnv', { default: '' });
  const currentEnvConfig = defineModel<EnvConfig>('currentEnvConfig', {
    default: {},
  });
  const envLoading = ref(false);
  const envOptions = ref<SelectOptionData[]>([]);

  async function initEnvironment() {
    try {
      const res = await getEnvironment(currentEnv.value);
      currentEnvConfig.value = {
        ...res,
        id: currentEnv.value,
        name: envOptions.value.find((item) => item.value === currentEnv.value)?.label || '',
      };
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function initEnvList() {
    try {
      envLoading.value = true;
      const res = await getEnvList(appStore.currentProjectId);
      envOptions.value = res.map((item) => ({
        label: item.name,
        value: item.id,
      }));
      currentEnv.value = currentEnv.value.length ? currentEnv.value : res[0]?.id;
      if (currentEnv.value) {
        await initEnvironment();
      }
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

  defineExpose({
    currentEnvConfig,
  });
</script>

<style lang="less" scoped>
  .ms-input-group--prepend();
  :deep(.arco-select-view-prefix) {
    margin-right: 8px;
    padding-right: 0;
    border-right: 1px solid var(--color-text-input-border);
  }
</style>
