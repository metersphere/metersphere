<template>
  <div v-if="isShowForm">
    <MsFormCreate v-model:api="fApi" :rule="props.dataRules" :option="configOptions" @change="changeHandler" />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';

  import { FormRule } from '@form-create/arco-design';

  const props = defineProps<{
    dataRules: FormRule[];
    visible: boolean;
  }>();

  const emit = defineEmits(['update:fApi', 'update:visible', 'update:defaultValue', 'updateCustoms']);

  const isShowForm = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val),
  });

  const fApi = ref();

  const configOptions = ref({
    resetBtn: false,
    submitBtn: false,
    on: false,
    form: {
      layout: 'vertical',
      labelAlign: 'left',
    },
    row: {
      gutter: 0,
    },
    wrap: {
      'asterisk-position': 'end',
      'validate-trigger': ['change'],
      'hide-label': true,
      'hide-asterisk': true,
    },
  });

  function changeHandler() {
    emit('updateCustoms');
    isShowForm.value = false;
  }
</script>

<style scoped></style>
