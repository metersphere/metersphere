<template>
  <pre ref="jr" :class="props.class" @click="pickPath"></pre>
  <input ref="ip" :value="jsonPath" class="path" type="hidden" />
</template>

<script setup lang="ts">
  import { onMounted, onUnmounted, Ref, ref } from 'vue';

  import JPPicker from '@/assets/js/jsonpath-picker-vanilla/jsonpath-picker-vanilla';

  import { Recordable } from '#/global';

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
        users: [
          {
            id: 1,
            name: 'John',
            age: 'Number(25.0000000000000000000)',
          },
          {
            id: 2,
            name: 'Jane',
            age: 30,
          },
          {
            id: 3,
            name: 'Mike',
            age: 28,
          },
        ],
        products: [
          {
            id: 101,
            name: 'iPhone',
            price: 999,
          },
          {
            id: 102,
            name: 'MacBook',
            price: 1599,
          },
          {
            id: 103,
            name: 'iPad',
            price: 799,
          },
        ],
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
  @import url('@/assets/js/jsonpath-picker-vanilla/jsonpath-picker.css');
</style>
