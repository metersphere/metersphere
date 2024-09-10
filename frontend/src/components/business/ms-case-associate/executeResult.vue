<template>
  <div v-if="props.executeResult" class="flex items-center">
    <MsIcon
      :type="lastExecuteResultMap[props.executeResult]?.icon || ''"
      class="mr-1"
      :size="16"
      :style="{ color: lastExecuteResultMap[props.executeResult]?.color }"
    ></MsIcon>
    <span class="text-[14px]">{{ t(lastExecuteResultMap[props.executeResult]?.statusText || '-') }}</span>
  </div>
</template>

<script setup lang="ts">
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { LastExecuteResults } from '@/enums/caseEnum';

  import { lastExecuteResultMap } from './utils';

  const { t } = useI18n();

  const props = defineProps<{
    executeResult?: LastExecuteResults;
  }>();

  // const status = computed(() => {
  //   if (props.executeResult) {
  //     const config = lastExecuteResultMap[props.executeResult];
  //     if (config) {
  //       return {
  //         text: t(config?.statusText || ''),
  //       };
  //     }
  //   }
  // });
</script>

<style lang="less" scoped></style>
