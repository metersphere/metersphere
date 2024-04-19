<template>
  <div v-expire class="mb-4">
    <a-alert type="warning">{{
      licenseStore.expiredDays >= 0 && licenseStore.expiredDays <= 30
        ? t('system.authorized.LicenseExpirationPromptLessThanThirty', { day: licenseStore.expiredDays })
        : t('system.authorized.LicenseExpirationPromptGreaterThanThirty')
    }}</a-alert>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';
  import useLicenseStore from '@/store/modules/setting/license';

  const licenseStore = useLicenseStore();
  const { t } = useI18n();
</script>

<style scoped></style>
