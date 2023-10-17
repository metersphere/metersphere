<template>
  <a-select v-model="value" multiple allow-search allow-clear :options="options" :field-names="fieldNames" />
</template>

<script setup lang="ts">
  import { computed, ref, watchEffect } from 'vue';

  import { getPoolOptionsByOrgOrSystem } from '@/api/modules/setting/organizationAndProject';

  const options = ref([]);
  const fieldNames = { value: 'id', label: 'name' };
  const props = defineProps<{ organizationId?: string; modelValue: string[] }>();
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

  const loadList = async (id?: string) => {
    try {
      loading.value = true;
      options.value = await getPoolOptionsByOrgOrSystem(id);
    } catch (error) {
      options.value = [];
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  };
  watchEffect(() => {
    loadList(props.organizationId);
  });
</script>
