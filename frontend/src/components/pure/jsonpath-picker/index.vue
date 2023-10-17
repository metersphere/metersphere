<template>
  <pre ref="jr" :class="props.class" @click="pickPath"></pre>
  <input ref="ip" :value="jsonPath" class="path" type="hidden" />
</template>

<script setup lang="ts">
  import { onMounted, onUnmounted, Ref, ref } from 'vue';

  import { Recordable } from '#/global';
  import JPPicker from 'jsonpath-picker-vanilla';

  const jr: Ref<HTMLElement | null> = ref(null);
  const ip: Ref<HTMLInputElement | null> = ref(null);
  const jsonPath = ref('');

  const props = withDefaults(
    defineProps<{
      data: object;
      opt?: Recordable<any>;
      class?: string;
    }>(),
    {
      data: () => ({
        name1: 'val1',
        name2: {
          name3: 'val2',
        },
      }),
      opt: () => ({}),
    }
  );

  const emit = defineEmits<{
    (e: 'pick', path: string): void;
  }>();

  onMounted(() => {
    JPPicker.jsonPathPicker(jr.value, props.data, [ip.value], props.opt);
  });

  function pickPath(ev: any) {
    if (ev.target && ev.target.classList.contains('pick-path')) {
      setTimeout(() => {
        if (ip.value) {
          jsonPath.value = ip.value.value;
          emit('pick', jsonPath.value);
        }
      }, 0);
    }
  }

  onUnmounted(() => {
    JPPicker.clearJsonPathPicker(jr.value);
  });
</script>

<style lang="css">
  @import url('jsonpath-picker-vanilla/lib/jsonpath-picker.css');
</style>
