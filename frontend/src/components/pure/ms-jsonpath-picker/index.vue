<template>
  <pre ref="jr" :class="['container', props.class]" @click="pickPath"></pre>
  <input ref="ip" :value="jsonPath" class="path" type="hidden" />
</template>

<script setup lang="ts">
  import { onMounted, onUnmounted, Ref, ref, watch } from 'vue';
  import { JSONPath } from 'jsonpath-plus';

  import JPPicker from '@/assets/js/jsonpath-picker-vanilla/jsonpath-picker-vanilla';

  import { Recordable } from '#/global';

  const jr: Ref<HTMLElement | null> = ref(null);
  const ip: Ref<HTMLInputElement | null> = ref(null);
  const json = ref<string | Recordable<any>>('');
  const jsonPath = ref('');

  const props = withDefaults(
    defineProps<{
      data: string | Recordable<any>;
      opt?: Recordable<any>;
      class?: string;
    }>(),
    {
      opt: () => ({}),
    }
  );

  const emit = defineEmits<{
    (e: 'pick', path: string, result: any[]): void;
  }>();

  function initJsonPathPicker() {
    try {
      json.value = props.data;
      if (typeof props.data === 'string') {
        json.value = JSON.parse(props.data);
      }
      JPPicker.jsonPathPicker(jr.value, json.value, [ip.value], props.opt);
    } catch (error) {
      JPPicker.jsonPathPicker(jr.value, props.data, [ip.value], props.opt);
    }
  }

  onMounted(() => {
    initJsonPathPicker();
  });

  watch(
    () => props.data,
    () => {
      initJsonPathPicker();
    },
    {
      deep: true,
    }
  );

  function pickPath(ev: any) {
    if (ev.target && ev.target.classList.contains('pick-path')) {
      setTimeout(() => {
        if (ip.value) {
          jsonPath.value = ip.value.value;
          emit('pick', jsonPath.value, JSONPath({ json: json.value, path: jsonPath.value }));
        }
      }, 0);
    }
  }

  onUnmounted(() => {
    JPPicker.clearJsonPathPicker(jr.value);
  });
</script>

<style>
  @import url('@/assets/js/jsonpath-picker-vanilla/jsonpath-picker.css');
</style>

<style lang="less" scoped>
  .container {
    @apply h-full overflow-y-auto;
    .ms-scroll-bar();

    padding: 12px 1.85em;
    border-radius: var(--border-radius-small);
    :deep(.json-string) {
      color: rgb(var(--link-7));
    }
    :deep(.json-literal) {
      color: rgb(var(--primary-4));
    }
    :deep(.json-toggle),
    :deep(.json-dict > li) {
      color: var(--color-text-2);
    }
  }
</style>
