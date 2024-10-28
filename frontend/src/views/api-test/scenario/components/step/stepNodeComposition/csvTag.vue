<template>
  <a-popover
    v-model:popup-visible="popoverVisible"
    position="bl"
    :disabled="csvList.length === 0 || props.step.isRefScenarioStep"
    content-class="csv-popover"
    arrow-class="hidden"
    :popup-offset="0"
  >
    <div
      v-if="props.step.stepType === ScenarioStepType.LOOP_CONTROLLER && csvList.length"
      class="csv-tag"
      :disabled="props.disabled"
    >
      {{ `CSV ${csvList.length}` }}
    </div>
    <template #content>
      <template v-if="alreadyDeleteFiles.length > 0">
        <div class="flex items-center">
          <div class="flex flex-1 items-center gap-[4px] leading-[18px]">
            <icon-exclamation-circle-fill class="!text-[rgb(var(--warning-6))]" :size="14" />
            <div class="text-[var(--color-text-4)]">{{ t('ms.add.attachment.alreadyDelete') }}</div>
          </div>
          <MsButton
            type="text"
            :disabled="props.disabled"
            size="mini"
            @click="
              emit(
                'removeDeleted',
                alreadyDeleteFiles.map((e) => e.id)
              )
            "
          >
            {{ t('ms.add.attachment.quickClear') }}
          </MsButton>
        </div>
        <div
          v-for="csv of alreadyDeleteFiles"
          :key="csv.id"
          class="flex items-center justify-between py-[4px] leading-[18px]"
        >
          <a-tooltip :content="csv.name">
            <div class="one-line-text w-[142px] text-[var(--color-text-1)]">
              {{ csv.name }}
            </div>
          </a-tooltip>
          <div class="flex items-center gap-[8px]">
            <a-tooltip :content="t('common.replace')">
              <MsIcon
                type="icon-icon_update_rotatiorn"
                class="cursor-pointer hover:text-[rgb(var(--primary-5))]"
                :size="14"
                @click="() => emit('replace', csv.id)"
              />
            </a-tooltip>
            <a-tooltip :content="t('common.delete')">
              <MsIcon
                type="icon-icon_delete-trash_outlined1"
                class="cursor-pointer hover:text-[rgb(var(--primary-5))]"
                :size="14"
                @click="() => emit('remove', csv.id)"
              />
            </a-tooltip>
          </div>
        </div>
      </template>
      <template v-if="otherFiles.length > 0">
        <div v-if="alreadyDeleteFiles.length > 0" class="mt-[4px] text-[var(--color-text-4)]">
          {{ t('ms.add.attachment.other') }}
        </div>
        <div v-for="csv of otherFiles" :key="csv.id" class="flex items-center justify-between py-[4px] leading-[18px]">
          <a-tooltip :content="csv.name">
            <div class="one-line-text w-[142px] text-[var(--color-text-1)]">
              {{ csv.name }}
            </div>
          </a-tooltip>
          <div class="flex items-center gap-[8px]">
            <a-tooltip :content="t('common.replace')">
              <MsIcon
                type="icon-icon_update_rotatiorn"
                class="cursor-pointer hover:text-[rgb(var(--primary-5))]"
                :size="14"
                @click="() => emit('replace', csv.id)"
              />
            </a-tooltip>
            <a-tooltip :content="t('common.delete')">
              <MsIcon
                type="icon-icon_delete-trash_outlined1"
                class="cursor-pointer hover:text-[rgb(var(--primary-5))]"
                :size="14"
                @click="() => emit('remove', csv.id)"
              />
            </a-tooltip>
          </div>
        </div>
      </template>
    </template>
  </a-popover>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { CsvVariable, ScenarioStepItem } from '@/models/apiTest/scenario';
  import { ScenarioStepType } from '@/enums/apiEnum';

  const props = defineProps<{
    step: ScenarioStepItem;
    csvVariables: CsvVariable[];
    disabled: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'replace', id?: string): void;
    (e: 'remove', id?: string): void;
    (e: 'removeDeleted', ids: string[]): void;
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

  const alreadyDeleteFiles = computed(() => {
    return csvList.value.filter((item) => item.file.delete);
  });
  const otherFiles = computed(() => {
    return csvList.value.filter((item) => !item.file.delete);
  });

  watch(
    () => props.step.csvIds,
    (arr) => {
      if (!arr || arr.length === 0) {
        popoverVisible.value = false;
      }
    }
  );
</script>

<style lang="less">
  .csv-popover {
    padding: 6px 12px;
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
    &:not(:disabled):hover {
      border-color: rgb(var(--primary-5));
      color: rgb(var(--primary-5));
    }
  }
</style>
