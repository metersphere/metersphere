<template>
  <div class="mb-[16px] flex items-center">
    <div class="font-bold text-[var(--color-text-1)]">
      {{
        innerCaseEditType === 'STEP' ? t('system.orgTemplate.stepDescription') : t('system.orgTemplate.textDescription')
      }}
    </div>
    <div v-if="!props.isTestPlan" class="font-normal">
      <a-divider direction="vertical" />
      <a-dropdown :popup-max-height="false" @select="handleSelectType">
        <span class="changeType">{{ t('system.orgTemplate.changeType') }} <icon-down /></span>
        <template #content>
          <a-doption value="STEP" :class="getSelectTypeClass('STEP')">
            {{ t('system.orgTemplate.stepDescription') }}
          </a-doption>
          <a-doption value="TEXT" :class="getSelectTypeClass('TEXT')">
            {{ t('system.orgTemplate.textDescription') }}
          </a-doption>
        </template>
      </a-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{
    isTestPlan?: boolean;
  }>();

  const innerCaseEditType = defineModel<string>('caseEditType', {
    required: true,
  });

  // 更改类型
  const handleSelectType = (value: string | number | Record<string, any> | undefined) => {
    innerCaseEditType.value = value as string;
  };

  // 获取类型样式
  function getSelectTypeClass(type: string) {
    return innerCaseEditType.value === type ? ['bg-[rgb(var(--primary-1))]', '!text-[rgb(var(--primary-5))]'] : [];
  }
</script>

<style scoped></style>
