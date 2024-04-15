<template>
  <div>
    <a-select
      v-model:model-value="innerCurrentEnv"
      :options="envOptions"
      class="!w-[200px] pl-0 pr-[8px]"
      :loading="envLoading"
      allow-search
      @change="(val) => initEnvironment(val as string)"
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

  const props = withDefaults(
    defineProps<{
      currentEnv?: string;
      setDefaultEnv?: boolean; // 是否设置默认选中环境，当传入的currentEnv为空时根据此属性判断是否需要将currentEnv设置为第一个环境
    }>(),
    {
      setDefaultEnv: true,
    }
  );
  const emit = defineEmits<{
    (e: 'update:currentEnv', val: string): void;
  }>();

  const appStore = useAppStore();
  const { openNewPage } = useOpenNewPage();

  const innerCurrentEnv = ref(props.currentEnv || '');
  const currentEnvConfig = defineModel<EnvConfig>('currentEnvConfig', {
    default: {},
  });
  const envLoading = ref(false);
  const envOptions = ref<SelectOptionData[]>([]);

  async function initEnvironment(id: string) {
    try {
      const res = await getEnvironment(id);
      currentEnvConfig.value = {
        ...res,
        id,
        name: envOptions.value.find((item) => item.value === id)?.label || '',
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
      if (!innerCurrentEnv.value) {
        innerCurrentEnv.value = res[0]?.id;
      }
      initEnvironment(innerCurrentEnv.value || res[0]?.id);
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

  watch(
    () => props.currentEnv,
    (val) => {
      if (!val) {
        if (props.setDefaultEnv) {
          innerCurrentEnv.value = (envOptions.value[0]?.value as string) || '';
          initEnvironment((envOptions.value[0]?.value as string) || '');
        }
      } else {
        innerCurrentEnv.value = val;
        initEnvironment(val);
      }
    }
  );

  watch(
    () => innerCurrentEnv.value,
    (val) => {
      emit('update:currentEnv', val);
    }
  );

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
