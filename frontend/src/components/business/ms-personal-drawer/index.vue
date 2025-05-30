<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="t('ms.personal')"
    :width="1200"
    :footer="false"
    unmount-on-close
    no-content-padding
  >
    <div class="flex h-full w-full">
      <div class="h-full w-[208px] bg-[var(--color-text-n9)]">
        <MsMenuPanel
          class="h-full !rounded-none bg-[var(--color-text-n9)] p-[16px_24px]"
          :default-key="activeMenu"
          :menu-list="menuList"
          active-class="!bg-transparent font-medium"
          @toggle-menu="(val) => (activeMenu = val)"
        />
      </div>
      <div :class="`w-[calc(100%-208px)] ${activeMenu === 'modelConfig' ? 'p-0' : 'p-[24px]'}`">
        <baseInfo v-if="activeMenu === 'baseInfo'" />
        <setPsw v-else-if="activeMenu === 'setPsw'" />
        <apiKey v-else-if="activeMenu === 'apiKey'" />
        <localExec v-else-if="activeMenu === 'local'" />
        <tripartite v-else-if="activeMenu === 'tripartite'" />
        <modelConfig v-else-if="activeMenu === 'modelConfig'" model-key="personal" />
      </div>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsMenuPanel from '@/components/pure/ms-menu-panel/index.vue';
  import modelConfig from '@/components/business/ms-personal-drawer/components/modelConfig.vue';
  import apiKey from './components/apiKey.vue';
  import baseInfo from './components/baseInfo.vue';
  import localExec from './components/localExec.vue';
  import setPsw from './components/setPsw.vue';
  import tripartite from './components/tripartite.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    visible: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
  }>();

  const { t } = useI18n();

  const innerVisible = useVModel(props, 'visible', emit);
  const activeMenu = ref('baseInfo');

  const menuList = ref([
    {
      name: 'personal',
      title: t('ms.personal.info'),
      level: 1,
    },
    {
      name: 'baseInfo',
      title: t('ms.personal.baseInfo'),
      level: 2,
    },
    {
      name: 'setPsw',
      title: t('ms.personal.setPsw'),
      level: 2,
    },
    {
      name: 'setting',
      title: t('ms.personal.setting'),
      level: 1,
    },
    {
      name: 'apiKey',
      title: t('ms.personal.apiKey'),
      level: 2,
      permission: ['SYSTEM_PERSONAL_API_KEY:READ'],
    },
    {
      name: 'local',
      title: t('ms.personal.localExecution'),
      level: 2,
    },
    {
      name: 'tripartite',
      title: t('ms.personal.tripartite'),
      level: 2,
    },
    {
      name: 'modelConfig',
      title: t('system.config.modelConfig.modelConfigSet'),
      level: 2,
    },
  ]);
</script>

<style lang="less" scoped></style>
