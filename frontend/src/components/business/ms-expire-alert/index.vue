<template>
  <div v-expire class="mb-2" :class="props.styleClass">
    <a-alert type="warning">{{
      licenseStore.expiredDays >= 0 && licenseStore.expiredDays <= 30
        ? t('system.authorized.LicenseExpirationPromptLessThanThirty', { day: licenseStore.expiredDays })
        : t('system.authorized.LicenseExpirationPromptGreaterThanThirty')
    }}</a-alert>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import useLicenseStore from '@/store/modules/setting/license';

  const licenseStore = useLicenseStore();
  const { t } = useI18n();

  const props = defineProps<{
    styleClass?: string[];
  }>();
</script>

<style scoped></style>
