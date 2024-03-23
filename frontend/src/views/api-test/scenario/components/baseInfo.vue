<template>
  <MsDescription :descriptions="descriptions"> </MsDescription>
</template>

<script setup lang="ts">
  import dayjs from 'dayjs';

  import MsDescription, { Description } from '@/components/pure/ms-description/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ScenarioDetail } from '@/models/apiTest/scenario';

  const props = defineProps<{
    scenario: ScenarioDetail;
  }>();

  const { t } = useI18n();

  const descriptions = computed<Description[]>(() => [
    {
      label: t('apiScenario.belongModule'),
      value: props.scenario.modulePath,
    },
    {
      label: t('apiScenario.table.columns.createUser'),
      value: props.scenario.createUser,
    },
    {
      label: t('apiScenario.table.columns.createTime'),
      value: dayjs(props.scenario.createTime).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      label: t('apiScenario.table.columns.updateTime'),
      value: dayjs(props.scenario.updateTime).format('YYYY-MM-DD HH:mm:ss'),
    },
  ]);
</script>

<style lang="less" scoped></style>
