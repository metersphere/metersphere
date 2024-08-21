<template>
  <MsTag
    v-if="props.inconsistentWithApi"
    class="cursor-pointer font-normal"
    :type="props.ignoreApiDiff ? 'default' : 'warning'"
    theme="light"
    :tooltip-disabled="true"
    max-width="160px"
    @click.stop="showDiffDrawer"
  >
    <template #icon>
      <div class="!text-[var(--color-text-4)]">
        <MsIcon v-if="props.ignoreApiDiff" type="icon-icon_warning_filled" size="16" />
        <MsIcon v-else class="!text-[var(--color-text-4)]" type="icon-icon_warning_colorful" size="16" />
      </div>
    </template>
    <span class="ml-[4px]"> {{ statusText }}</span>
  </MsTag>
</template>

<script setup lang="ts">
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{
    inconsistentWithApi?: boolean;
    ignoreApiDiff?: boolean;
    ignoreApiChange?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'showDiff'): void;
  }>();

  function showDiffDrawer() {
    emit('showDiff');
  }

  const statusText = computed(() => {
    // 忽略每次变更
    if (props.ignoreApiChange) {
      return t('case.eachHasBeenIgnored');
    }
    // 忽略本次变更
    if (props.ignoreApiDiff) {
      return t('case.haveIgnoredTheChange');
    }
    // 与接口定义不一致
    if (props.inconsistentWithApi) {
      return t('case.definitionInconsistent');
    }
  });
</script>

<style scoped></style>
