<template>
  <a-form ref="formRef" :model="formModel" layout="vertical">
    <div
      :class="`flex ${
        matchRules.length > 1 ? 'items-stretch' : 'items-center'
      } gap-[16px] overflow-hidden bg-[var(--color-text-n9)] p-[12px]`"
    >
      <div class="flex h-auto flex-col items-center">
        <a-divider v-show="matchRules.length > 1" direction="vertical" class="h-full" />
        <a-select v-model:model-value="matchAll" size="small" class="w-[75px]">
          <a-option :value="true">AND</a-option>
          <a-option :value="false">OR</a-option>
        </a-select>
        <a-divider v-show="matchRules.length > 1" direction="vertical" class="h-full" />
      </div>
      <div class="flex max-h-[300px] flex-1 flex-col gap-[8px]">
        <div v-for="(item, idx) in matchRules" :key="`filter_item_${idx}`" class="flex items-start gap-[8px]">
          <div class="w-[220px]">
            <a-form-item
              :field="`list[${idx}].key`"
              hide-asterisk
              class="hidden-item"
              :rules="[{ required: true, message: t('mockManagement.paramNameNotNull') }]"
            >
              <a-select
                v-model="item.key"
                :placeholder="t('apiTestDebug.paramName')"
                :options="props.keyOptions"
                allow-search
                @change="() => addMatchRule(idx)"
              >
              </a-select>
            </a-form-item>
          </div>
          <div class="w-[100px]">
            <a-form-item :field="`list[${idx}].condition`" hide-asterisk class="hidden-item">
              <a-select v-model="item.condition" :options="props.keyOptions" @change="() => addMatchRule(idx)">
              </a-select>
            </a-form-item>
          </div>
          <div class="flex-1">
            <a-form-item :field="`list[${idx}].value`" class="hidden-item">
              <MsParamsInput
                v-model:value="item.value"
                set-default-class
                @change="() => addMatchRule(idx)"
                @dblclick="quickInputParams(item)"
                @apply="() => addMatchRule(idx)"
              />
            </a-form-item>
          </div>
          <!-- <div class="grow-0">
            <a-form-item :field="`list[${idx}].description`" class="hidden-item">
              <paramDescInput
                v-model:desc="item.description"
                @input="() => addMatchRule(idx)"
                @dblclick="quickInputDesc(item)"
                @change="handleDescChange"
              />
            </a-form-item>
          </div> -->
          <div
            v-if="matchRules.length > 1"
            class="mt-[8px] flex h-full cursor-pointer items-start justify-center text-[var(--color-text-4)]"
            @click="handleDeleteItem(idx)"
          >
            <icon-minus-circle />
          </div>
        </div>
      </div>
    </div>
  </a-form>
  <a-modal
    v-model:visible="showQuickInputParam"
    :title="t('ms.paramsInput.value')"
    :ok-text="t('apiTestDebug.apply')"
    :ok-button-props="{ disabled: !quickInputParamValue || quickInputParamValue.trim() === '' }"
    class="ms-modal-form"
    body-class="!p-0"
    :width="680"
    title-align="start"
    @ok="applyQuickInputParam"
    @close="clearQuickInputParam"
  >
    <MsCodeEditor
      v-if="showQuickInputParam"
      v-model:model-value="quickInputParamValue"
      theme="vs"
      height="300px"
      :show-full-screen="false"
    >
      <template #rightTitle>
        <div class="flex justify-between">
          <div class="text-[var(--color-text-4)]">
            {{ t('apiTestDebug.quickInputParamsTip') }}
          </div>
        </div>
      </template>
    </MsCodeEditor>
  </a-modal>
  <!-- <a-modal
    v-model:visible="showQuickInputDesc"
    :title="t('apiTestDebug.desc')"
    :ok-text="t('common.save')"
    :ok-button-props="{ disabled: !quickInputDescValue || quickInputDescValue.trim() === '' }"
    class="ms-modal-form"
    body-class="!p-0"
    :width="480"
    title-align="start"
    :auto-size="{ minRows: 2 }"
    @ok="applyQuickInputDesc"
    @close="clearQuickInputDesc"
  >
    <a-textarea
      v-model:model-value="quickInputDescValue"
      :placeholder="t('apiTestDebug.descPlaceholder')"
      :max-length="1000"
    ></a-textarea>
  </a-modal> -->
</template>

<script setup lang="ts">
  import { FormInstance, SelectOptionData } from '@arco-design/web-vue';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsParamsInput from '@/components/business/ms-params-input/index.vue';

  // import paramDescInput from '@/views/api-test/components/paramDescInput.vue';
  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    keyOptions: SelectOptionData[];
  }>();
  const emit = defineEmits<{
    (
      e: 'change',
      form: {
        matchAll: boolean;
        matchRules: Record<string, any>[];
      },
      isInit?: boolean
    ): void;
  }>();

  const { t } = useI18n();

  const matchAll = defineModel<boolean>('matchAll', {
    required: true,
  });
  const matchRules = defineModel<Record<string, any>[]>('matchRules', {
    required: true,
  });

  const formRef = ref<FormInstance>();
  const formModel = ref({
    matchAll: matchAll.value,
    matchRules: matchRules.value,
  });

  function handleDeleteItem(index: number) {
    matchRules.value.splice(index, 1);
  }

  function addMatchRule(rowIndex: number) {
    if (rowIndex === matchRules.value.length - 1) {
      matchRules.value.push({
        key: '',
        value: '',
        description: '',
      });
    }
  }

  function emitChange(from: string, isInit?: boolean) {
    emit('change', formModel.value, isInit);
  }

  const showQuickInputParam = ref(false);
  const activeQuickInputRecord = ref<any>({});
  const quickInputParamValue = ref('');

  function quickInputParams(record: any) {
    activeQuickInputRecord.value = record;
    showQuickInputParam.value = true;
    quickInputParamValue.value = record.value;
  }

  function clearQuickInputParam() {
    activeQuickInputRecord.value = {};
    quickInputParamValue.value = '';
  }

  function applyQuickInputParam() {
    activeQuickInputRecord.value.value = quickInputParamValue.value;
    showQuickInputParam.value = false;
    addMatchRule(matchRules.value.findIndex((e) => e.id === activeQuickInputRecord.value.id));
    clearQuickInputParam();
    emitChange('applyQuickInputParam');
  }

  // const showQuickInputDesc = ref(false);
  // const quickInputDescValue = ref('');

  // function quickInputDesc(record: any) {
  //   activeQuickInputRecord.value = record;
  //   showQuickInputDesc.value = true;
  //   quickInputDescValue.value = record.description;
  // }

  // function clearQuickInputDesc() {
  //   activeQuickInputRecord.value = {};
  //   quickInputDescValue.value = '';
  // }

  // function applyQuickInputDesc() {
  //   activeQuickInputRecord.value.description = quickInputDescValue.value;
  //   showQuickInputDesc.value = false;
  //   addMatchRule(matchRules.value.findIndex((e) => e.id === activeQuickInputRecord.value.id));
  //   clearQuickInputDesc();
  //   emitChange('applyQuickInputDesc');
  // }

  // function handleDescChange() {
  //   emitChange('handleDescChange');
  // }
</script>

<style lang="less" scoped></style>
