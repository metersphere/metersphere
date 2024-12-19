<template>
  <a-form ref="formRef" :model="formModel" layout="vertical">
    <a-spin :loading="loading" class="block">
      <div
        v-if="matchRules.length > 0 || !disabled"
        :class="`flex ${
          matchRules.length > 1 ? 'items-stretch' : 'items-center'
        } gap-[16px] bg-[var(--color-text-n9)] p-[12px]`"
      >
        <div class="flex h-auto flex-col items-center">
          <a-divider v-show="matchRules.length > 1" direction="vertical" class="h-full" />
          <a-select v-model:model-value="matchAll" size="small" :disabled="props.disabled" class="w-[75px]">
            <a-option :value="true">AND</a-option>
            <a-option :value="false">OR</a-option>
          </a-select>
          <a-divider v-show="matchRules.length > 1" direction="vertical" class="h-full" />
        </div>
        <div class="flex flex-1 flex-col gap-[8px]">
          <div v-for="(item, idx) in matchRules" :key="`filter_item_${idx}`" class="flex items-start gap-[8px]">
            <div class="w-[220px]">
              <a-form-item
                :field="`matchRules[${idx}].key`"
                hide-asterisk
                class="hidden-item"
                :rules="[{ required: true, message: t('mockManagement.paramNameNotNull') }]"
                :disabled="props.disabled"
              >
                <a-auto-complete
                  v-model="item.key"
                  :placeholder="t('apiTestDebug.paramName')"
                  :data="props.keyOptions"
                  @change="(val) => selectedKey(item, idx)"
                />

                <!--                <a-select-->
                <!--                  v-model="item.key"-->
                <!--                  :placeholder="t('apiTestDebug.paramName')"-->
                <!--                  :options="props.keyOptions"-->
                <!--                  allow-search-->
                <!--                  allow-create-->
                <!--                  @change="(val) => selectedKey(item, idx)"-->
                <!--                >-->
                <!--                </a-select>-->
              </a-form-item>
            </div>
            <div class="w-[110px]">
              <a-form-item
                :field="`matchRules[${idx}].condition`"
                hide-asterisk
                class="hidden-item"
                :disabled="props.disabled"
              >
                <a-select
                  v-model="item.condition"
                  :options="getMatchRuleOptions(item.paramType)"
                  @change="() => addMatchRule(idx)"
                >
                </a-select>
              </a-form-item>
            </div>
            <div class="flex-1">
              <a-form-item :field="`matchRules[${idx}].value`" class="hidden-item" :disabled="props.disabled">
                <MsAddAttachment
                  v-if="item.paramType === RequestParamsType.FILE"
                  v-model:file-list="item.files"
                  mode="input"
                  :fields="{
                    id: 'fileId',
                    name: 'fileName',
                  }"
                  input-class="h-[32px]"
                  :file-save-as-source-id="props.id"
                  :file-save-as-api="transferMockFile"
                  :file-module-options-api="getMockTransferOptions"
                  :disabled="props.disabled"
                  @change="(files, file) => handleFileChange(files, item, idx, file)"
                />
                <MsParamsInput
                  v-else
                  v-model:value="item.value"
                  set-default-class
                  :disabled="props.disabled"
                  @change="() => addMatchRule(idx)"
                  @set-params="quickInputParams(item)"
                  @apply="() => addMatchRule(idx)"
                />
              </a-form-item>
            </div>
            <!-- <div class="grow-0">
            <a-form-item :field="`matchRules[${idx}].description`" class="hidden-item">
              <paramDescInput
                v-model:desc="item.description"
                @input="() => addMatchRule(idx)"
                @dblclick="quickInputDesc(item)"
                @change="handleDescChange"
              />
            </a-form-item>
          </div> -->
            <div
              v-if="matchRules.length > 1 && !props.disabled && idx !== matchRules.length - 1"
              class="mt-[8px] flex h-full cursor-pointer items-start justify-center text-[var(--color-text-4)]"
              @click="handleDeleteItem(idx)"
            >
              <icon-minus-circle />
            </div>
          </div>
        </div>
      </div>
      <div v-else>
        <a-empty :description="t('mockManagement.noMatchRules')"></a-empty>
      </div>
    </a-spin>
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
    :title="t('common.desc')"
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
  import { cloneDeep } from 'lodash-es';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import MsAddAttachment from '@/components/business/ms-add-attachment/index.vue';
  import MsParamsInput from '@/components/business/ms-params-input/index.vue';

  import { getMockTransferOptions, transferMockFile, uploadMockTempFile } from '@/api/modules/api-test/management';
  // import paramDescInput from '@/views/api-test/components/paramDescInput.vue';
  import { useI18n } from '@/hooks/useI18n';

  import { MatchRuleItem } from '@/models/apiTest/mock';
  import { RequestParamsType } from '@/enums/apiEnum';

  import { defaultMatchRuleItem, matchRuleOptions, mockFileMatchRules } from '@/views/api-test/components/config';

  const props = defineProps<{
    id?: string;
    formKey?: string;
    keyOptions: SelectOptionData[];
    disabled: boolean;
  }>();
  const emit = defineEmits<{
    (
      e: 'change',
      form: {
        matchAll: boolean;
        matchRules: MatchRuleItem[];
      },
      isInit?: boolean
    ): void;
  }>();

  const { t } = useI18n();

  const matchAll = defineModel<boolean>('matchAll', {
    required: true,
  });
  const matchRules = defineModel<MatchRuleItem[]>('matchRules', {
    required: true,
  });

  const loading = ref(false);
  const formRef = ref<FormInstance>();
  const formModel = ref({
    matchAll: matchAll.value,
    matchRules: matchRules.value,
  });

  watch(
    () => [props.formKey, props.disabled],
    () => {
      formModel.value = {
        matchAll: matchAll.value,
        matchRules: matchRules.value,
      };
      formRef.value?.clearValidate();
    }
  );

  function handleDeleteItem(index: number) {
    matchRules.value.splice(index, 1);
  }

  function addMatchRule(rowIndex: number) {
    if (rowIndex === matchRules.value.length - 1) {
      matchRules.value.push({
        id: `${Date.now() + rowIndex}`,
        ...cloneDeep(defaultMatchRuleItem),
      });
    }
  }

  /**
   * 选择参数名称
   * @param ruleItem 当前规则项
   * @param rowIndex 当前行索引
   */
  function selectedKey(ruleItem: MatchRuleItem, rowIndex: number) {
    const item = formModel.value.matchRules[rowIndex];
    if (item) {
      const newParamType =
        props.keyOptions.find((e) => e.value === ruleItem.key)?.paramType || defaultMatchRuleItem.paramType;
      item.paramType = newParamType;
      if (newParamType === RequestParamsType.FILE && !mockFileMatchRules.includes(item.condition)) {
        // 如果选择的参数类型是文件，且当前条件不在文件匹配规则中，则默认为等于
        item.condition = 'EQUALS';
      }
      addMatchRule(rowIndex);
    }
  }

  /**
   * 获取对应参数类型的匹配规则选项
   * @param paramType 参数类型
   */
  function getMatchRuleOptions(paramType: RequestParamsType) {
    if (paramType === RequestParamsType.FILE) {
      return matchRuleOptions
        .filter((e) => mockFileMatchRules.includes(e.value))
        .map((e) => ({ ...e, label: t(e.label) }));
    }
    return matchRuleOptions.map((e) => ({ ...e, label: t(e.label) }));
  }

  function emitChange(from: string, isInit?: boolean) {
    emit('change', formModel.value, isInit);
  }

  async function handleFileChange(
    files: MsFileItem[],
    record: Record<string, any>,
    rowIndex: number,
    file?: MsFileItem
  ) {
    try {
      if (file?.local && file.file) {
        // 本地上传单次只能选一个文件
        loading.value = true;
        const res = await uploadMockTempFile(file.file);
        for (let i = 0; i < record.files.length; i++) {
          const item = record.files[i];
          if ([item.fileId, item.uid].includes(file.uid)) {
            record.files[i] = {
              ...file,
              fileId: res.data,
              fileName: file.name || '',
              fileAlias: file.name || '',
            };
            break;
          }
        }
      } else {
        // 关联文件可选多个文件
        record.files = files.map((e) => ({
          ...e,
          fileId: e.uid || e.fileId || '',
          fileName: e.originalName || '',
          fileAlias: e.name || '',
        }));
      }
      addMatchRule(rowIndex);
      emitChange('handleFileChange');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
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
