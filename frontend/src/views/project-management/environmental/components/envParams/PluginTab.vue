<template>
  <MsFormCreate
    v-model:api="fApi"
    :rule="currentPluginScript"
    :option="currentPluginOptions"
    @change="handlePluginFormChange"
  />
</template>

<script lang="ts" setup>
  import { debounce } from 'lodash-es';

  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';

  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  import { EnvPluginScript } from '@/models/projectManagement/environmental';

  import type { Api } from '@form-create/arco-design';

  const props = defineProps<{
    script: EnvPluginScript;
    pluginId: string;
    fields: string[];
  }>();
  const fApi = ref<Api>();

  const store = useProjectEnvStore();

  const currentPluginScript = computed(() => props.script?.script || []);
  const currentPluginOptions = computed(() => props.script?.options || {});

  const innerParams = defineModel<Record<string, any>>({ default: () => ({}) });

  // 处理插件表单输入框变化
  const handlePluginFormChange = debounce(() => {
    innerParams.value = fApi.value?.formData() || {};
  }, 300);

  /**
   * 设置插件表单数据
   */
  function setPluginFormData() {
    const tempForm = { ...store.currentEnvDetailInfo.config.pluginConfigMap[props.pluginId] };
    if (store.currentEnvDetailInfo.config.pluginConfigMap[props.pluginId]) {
      fApi.value?.reload(currentPluginScript.value);
      if (fApi.value) {
        const form: Record<string, any> = {};
        props.fields.forEach((key) => {
          form[key] = tempForm[key];
        });
        fApi.value?.setValue(tempForm);
      }
      fApi.value?.refresh();
    } else {
      nextTick(() => {
        fApi.value?.resetFields();
      });
    }
  }

  watch(
    () => store.currentId,
    (val) => {
      if (val) {
        setPluginFormData();
      }
    }
  );

  watch(
    () => innerParams.value,
    (val) => {
      setPluginFormData();
    },
    {
      deep: true,
    }
  );

  onMounted(() => {
    setPluginFormData();
  });
</script>

<style lang="less" scoped></style>
