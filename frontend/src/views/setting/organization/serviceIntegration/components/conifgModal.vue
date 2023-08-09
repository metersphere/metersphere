<template>
  <a-modal
    v-model:visible="detailVisible"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :ok-text="t('organization.member.Confirm')"
    :cancel-text="t('organization.member.Cancel')"
  >
    <template #title> 标题 </template>
    <div>
      <MsFormCreate v-model:api="fApi" :rule="formRules" :option="options" />
    </div>
    <template #footer>
      <div class="flex justify-between">
        <div class="flex flex-row items-center justify-center">
          <a-switch size="small" />
          <a-tooltip>
            <template #content>
              <div class="text-sm">{{ t('organization.service.statusEnableTip') }}</div>
              <div class="text-sm">{{ t('organization.service.statusDisableTip') }}</div>
            </template>
            <icon-question-circle class="ml-2 text-[--color-text-4]" />
          </a-tooltip>
        </div>
        <a-space>
          <a-button type="secondary" @click="handleCancel">取消</a-button>
          <a-button type="outline">测试链接</a-button>
          <a-button type="primary" @click="saveHandler">确定</a-button>
        </a-space>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watchEffect, watch } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';

  const { t } = useI18n();

  const emits = defineEmits<{
    (event: 'update:visible', visible: boolean): void;
  }>();

  const props = defineProps<{
    visible: boolean;
    rule: any;
  }>();
  const detailVisible = ref<boolean>(false);

  const fApi = ref<any>({});
  const formRules = ref([]);

  watchEffect(() => {
    detailVisible.value = props.visible;
    formRules.value = props.rule;
  });
  watch(
    () => detailVisible.value,
    (val) => {
      emits('update:visible', val);
    }
  );

  const handleCancel = () => {
    detailVisible.value = false;
  };
  const submit = () => {
    fApi.value?.submit((formData: FormData) => {
      console.log(formData, 'formData');
    });
  };
  const saveHandler = () => {
    fApi.value?.validate((valid: any, fail: any) => {
      if (valid) {
        submit();
      } else {
        console.log(fail);
      }
    });
  };
  const options = ref({
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
    },
  });
</script>

<style scoped></style>
