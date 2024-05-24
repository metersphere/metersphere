<template>
  <a-popover
    v-model:popup-visible="popoverVisible"
    position="bl"
    :disabled="!props.step.csvIds || props.step.csvIds.length === 0 || props.step.isRefScenarioStep"
    content-class="csv-popover"
    arrow-class="hidden"
    :popup-offset="0"
  >
    <div v-if="props.step.stepType === ScenarioStepType.LOOP_CONTROLLER && props.step.csvIds?.length" class="csv-tag">
      {{ `CSV ${props.step.csvIds?.length}` }}
    </div>
    <template #content>
      <div class="mb-[4px] font-medium text-[var(--color-text-4)]">
        {{ `${t('apiScenario.csvQuote')}（${props.step.csvIds?.length}）` }}
      </div>
      <div v-for="csv of csvList" :key="csv.id" class="flex items-center justify-between px-[8px] py-[4px]">
        <a-tooltip :content="csv.name">
          <div class="one-line-text w-[142px] text-[var(--color-text-1)]">
            {{ csv.name }}
          </div>
        </a-tooltip>
        <div class="flex items-center">
          <MsButton type="text" size="mini" class="!mr-0" @click="() => emit('replace', csv.id)">
            {{ t('common.replace') }}
          </MsButton>
          <a-divider direction="vertical" :margin="8"></a-divider>
          <MsButton type="text" size="mini" class="!mr-0" @click="() => emit('remove', csv.id)">
            {{ t('common.remove') }}
          </MsButton>
        </div>
      </div>
    </template>
  </a-popover>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { CsvVariable, ScenarioStepItem } from '@/models/apiTest/scenario';
  import { ScenarioStepType } from '@/enums/apiEnum';

  const props = defineProps<{
    step: ScenarioStepItem;
    csvVariables: CsvVariable[];
  }>();
  const emit = defineEmits<{
    (e: 'replace', id?: string): void;
    (e: 'remove', id?: string): void;
  }>();

  const { t } = useI18n();

  const popoverVisible = ref(false);

  const csvList = computed(() => {
    if (props.step.csvIds) {
      return props.step.csvIds
        .map((id) => props.csvVariables.find((csv) => csv.id === id))
        .filter((e) => e !== undefined) as CsvVariable[];
    }
    return [];
  });
</script>

<style lang="less">
  .csv-popover {
    padding: 6px;
    .arco-popover-content {
      margin-top: 0;
    }
  }
</style>

<style lang="less" scoped>
  .csv-tag {
    @apply cursor-pointer bg-transparent;

    padding: 0 4px;
    font-size: 12px;
    border: 1px solid var(--color-text-input-border);
    border-radius: var(--border-radius-small);
    color: var(--color-text-4);
    line-height: 18px;
    &:hover {
      border-color: rgb(var(--primary-5));
      color: rgb(var(--primary-5));
    }
  }
</style>
