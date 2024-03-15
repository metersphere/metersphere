<template>
  <div>
    <a-select
      v-model:model-value="currentEnv"
      :options="envOptions"
      class="!w-[200px] pl-0 pr-[8px]"
      :loading="envLoading"
      allow-search
      @change="initEnvironment"
    >
      <template #prefix>
        <div class="flex cursor-pointer p-[8px]" @click.stop="goEnv">
          <icon-location class="text-[var(--color-text-4)]" />
        </div>
      </template>
    </a-select>
  </div>
</template>

<script setup lang="ts">
  import { SelectOptionData } from '@arco-design/web-vue';

  import { getEnvironment, getEnvList } from '@/api/modules/api-test/common';
  import router from '@/router';
  import useAppStore from '@/store/modules/app';

  import { EnvConfig } from '@/models/projectManagement/environmental';
  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';

  const appStore = useAppStore();

  const currentEnv = ref('');

  const currentEnvConfig = ref<EnvConfig>();
  const envLoading = ref(false);
  const envOptions = ref<SelectOptionData[]>([]);

  async function initEnvironment() {
    try {
      currentEnvConfig.value = await getEnvironment(currentEnv.value);
      currentEnvConfig.value.id = currentEnv.value;
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
      currentEnv.value = res[0]?.id || '';
      initEnvironment();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      envLoading.value = false;
    }
  }

  function goEnv() {
    router.push({
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_ENVIRONMENT_MANAGEMENT,
    });
  }

  onBeforeMount(() => {
    initEnvList();
  });

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
