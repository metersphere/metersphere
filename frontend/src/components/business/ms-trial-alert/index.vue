<template>
  <div v-if="!licenseStore.hasLicense()" class="mb-2">
    <a-alert type="warning">
      <div class="flex items-center">
        {{ props.tipContent }}
        <MsButton class="ml-2" type="text" @click="goTry">
          {{ t('system.authorized.applyTrial') }}
        </MsButton>
      </div>
    </a-alert>
  </div>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useLicenseStore from '@/store/modules/setting/license';

  const licenseStore = useLicenseStore();
  const { t } = useI18n();
  const props = defineProps<{
    tipContent: string;
  }>();

  function goTry() {
    window.open('https://jinshuju.net/f/CzzAOe', '_blank');
  }
</script>

<style scoped></style>
