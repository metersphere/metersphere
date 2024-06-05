<template>
  <a-select
    v-model="value"
    :placeholder="t('system.project.resourcePoolPlaceholder')"
    multiple
    allow-search
    allow-clear
    :options="options"
    :field-names="fieldNames"
  />
</template>

<script setup lang="ts">
  import { computed, ref, watchEffect } from 'vue';

  import { getPoolOptionsByOrg, getPoolOptionsByOrgOrSystem } from '@/api/modules/setting/organizationAndProject';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const options = ref([]);
  const fieldNames = { value: 'id', label: 'name' };
  const props = defineProps<{ organizationId?: string; modelValue: string[]; moduleIds?: string[]; isOrg?: boolean }>();
  const loading = ref(false);
  const emit = defineEmits<{
    (e: 'update:modelValue', value: string[]): void;
  }>();
  const value = computed({
    get() {
      return props.modelValue;
    },
    set(v) {
      emit('update:modelValue', v);
    },
  });
  const loadList = async (arr: string[], id?: string, isOrg?: boolean) => {
    try {
      loading.value = true;
      options.value = !isOrg ? await getPoolOptionsByOrgOrSystem(arr, id) : await getPoolOptionsByOrg(arr, id);
    } catch (error) {
      options.value = [];
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  };
  watchEffect(() => {
    loadList(props.moduleIds || [], props.organizationId, props.isOrg || false);
  });
</script>
