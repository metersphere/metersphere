<template>
  <MsDrawer
    v-model:visible="showDrawer"
    :title="isEdit ? t('system.orgTemplate.update') : t('system.orgTemplate.addField')"
    :ok-text="t(isEdit ? 'system.orgTemplate.update' : 'system.orgTemplate.addField')"
    :ok-loading="drawerLoading"
    :width="800"
    :show-continue="!isEdit && data.length < 20"
    :ok-disabled="data.length >= 20 && !isEdit"
    @confirm="handleDrawerConfirm"
    @continue="saveAndContinue"
    @cancel="handleDrawerCancel"
  >
    <div class="form">
      <a-form ref="fieldFormRef" class="rounded-[4px]" :model="fieldForm" layout="vertical">
        <a-form-item
          field="name"
          :label="t('system.orgTemplate.fieldName')"
          :rules="[{ required: true, message: t('system.orgTemplate.fieldNameRules') }]"
          required
          :disabled="fieldForm.internal"
          asterisk-position="end"
        >
          <a-input
            v-model:model-value="fieldForm.name"
            :placeholder="t('system.orgTemplate.fieldNamePlaceholder')"
            :max-length="255"
          ></a-input>
        </a-form-item>
        <a-form-item field="remark" :label="t('common.desc')" asterisk-position="end">
          <a-textarea
            v-model="fieldForm.remark"
            :max-length="1000"
            :placeholder="t('system.orgTemplate.resDescription')"
            :auto-size="{ minRows: 1 }"
            style="resize: vertical"
          ></a-textarea>
        </a-form-item>
        <a-form-item
          field="type"
          :label="t('system.orgTemplate.fieldType')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('system.orgTemplate.typeEmptyTip') }]"
        >
          <a-select
            v-model="fieldForm.type"
            class="w-[260px]"
            :placeholder="t('system.orgTemplate.fieldTypePlaceholder')"
            allow-clear
            :disabled="isEdit"
            @change="fieldChangeHandler"
          >
            <a-option v-for="item of fieldOptions" :key="item.key" :value="item.key">
              <div class="flex items-center"
                ><MsIcon :type="item.iconName" class="mx-2" /> <span>{{ item.label }}</span></div
              >
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item
          v-if="fieldForm.type === 'MEMBER'"
          field="type"
          :label="t('system.orgTemplate.allowMultiMember')"
          asterisk-position="end"
        >
          <a-switch v-model="isMultipleSelectMember" size="small" :disabled="isEdit" type="line" />
        </a-form-item>
        <!-- 选项选择器 -->
        <a-form-item
          v-if="showOptionsSelect"
          field="optionsModels"
          :label="t('system.orgTemplate.optionContent')"
          asterisk-position="end"
          :rules="[{ message: t('system.orgTemplate.optionContentRules') }]"
          class="relative"
          :class="[!fieldForm?.enableOptionKey ? 'max-w-[340px]' : 'w-full']"
        >
          <div v-if="sceneType === 'BUG'" class="optionsKey">
            <a-checkbox v-model="fieldForm.enableOptionKey"
              >{{ t('system.orgTemplate.optionKeyValue') }}
              <a-tooltip :content="t('system.orgTemplate.thirdPartyPlatforms')"
                ><icon-question-circle
                  :style="{ 'font-size': '16px' }"
                  class="text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]" /></a-tooltip></a-checkbox
          ></div>
          <MsBatchForm
            ref="batchFormRef"
            :models="optionsModels"
            form-mode="create"
            add-text="system.orgTemplate.addOptions"
            :is-show-drag="true"
            :form-width="!fieldForm?.enableOptionKey ? '340px' : ''"
            :default-vals="fieldDefaultValues"
          />
        </a-form-item>
        <!-- 日期和数值 -->

        <a-form-item
          v-if="fieldForm.type === 'NUMBER'"
          field="selectNumber"
          :label="t('system.orgTemplate.numberFormat')"
          asterisk-position="end"
        >
          <a-select
            v-model="selectNumber"
            class="w-[260px]"
            :placeholder="t('system.orgTemplate.formatPlaceholder')"
            allow-clear
            :default-value="numberTypeOptions[0].value"
            :disabled="isEdit"
          >
            <a-option v-for="item of numberTypeOptions" :key="item.value" :value="item.value">
              <div class="flex items-center">{{ item.label }}</div>
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item
          v-if="fieldForm.type === 'DATE'"
          field="selectFormat"
          :label="t('system.orgTemplate.dateFormat')"
          asterisk-position="end"
        >
          <a-select
            v-model="selectFormat"
            class="w-[260px]"
            :placeholder="t('system.orgTemplate.formatPlaceholder')"
            allow-clear
            :disabled="isEdit"
            :default-value="dateOptions[0].value"
          >
            <a-option v-for="item of dateOptions" :key="item.value" :value="item.value">
              <div class="flex items-center">{{ item.label }}</div>
            </a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  /**
   * @description 模板管理-自定义字段-添加自定义字段&编辑自定义字段
   */
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import type { FormItemType } from '@/components/pure/ms-form-create/types';
  import MsBatchForm from '@/components/business/ms-batch-form/index.vue';
  import type { FormItemModel } from '@/components/business/ms-batch-form/types';

  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { getGenerateId } from '@/utils';

  import type {
    AddOrUpdateField,
    DefinedFieldItem,
    fieldIconAndNameModal,
    FieldOptions,
  } from '@/models/setting/template';

  import { dateOptions, fieldIconAndName, getFieldRequestApi, numberTypeOptions } from './fieldSetting';

  const { t } = useI18n();
  const route = useRoute();
  const appStore = useAppStore();

  const sceneType = route.query.type;
  const props = defineProps<{
    visible: boolean;
    mode: 'organization' | 'project';
    data: DefinedFieldItem[];
  }>();
  const emit = defineEmits(['success', 'update:visible']);

  const showDrawer = ref<boolean>(false);
  const drawerLoading = ref<boolean>(false);

  const fieldFormRef = ref<FormInstance>();
  const currentOrgId = computed(() => appStore.currentOrgId);
  const currentProjectId = computed(() => appStore.currentProjectId);

  const scopeId = computed(() => {
    return props.mode === 'organization' ? currentOrgId.value : currentProjectId.value;
  });
  const initFieldForm: AddOrUpdateField = {
    name: '',
    used: false,
    type: undefined,
    remark: '',
    scopeId: scopeId.value,
    scene: 'FUNCTIONAL',
    options: [],
    enableOptionKey: false,
  };
  const fieldForm = ref<AddOrUpdateField>({ ...initFieldForm });
  const isEdit = ref<boolean>(false);
  const selectFormat = ref<FormItemType>(); // 选择格式
  const selectNumber = ref<FormItemType>('INT'); // 数字格式
  const isMultipleSelectMember = ref<boolean | undefined>(false); // 成员多选
  const fieldType = ref<FormItemType>(); // 整体字段类型

  // 是否展示选项添加面板
  const showOptionsSelect = computed(() => {
    const showOptionsType: FormItemType[] = ['RADIO', 'CHECKBOX', 'SELECT', 'MULTIPLE_SELECT'];
    return showOptionsType.includes(fieldForm.value.type as FormItemType);
  });

  // 批量表单-1.仅选项情况
  const onlyOptions: Ref<FormItemModel> = ref({
    field: 'text',
    type: 'input',
    label: '',
    rules: [
      { required: true, message: t('system.orgTemplate.optionContentRules') },
      { notRepeat: true, message: t('system.orgTemplate.optionsContentNoRepeat') },
    ],
    placeholder: t('system.orgTemplate.optionsPlaceholder'),
    hideAsterisk: true,
    hideLabel: true,
  });

  // 批量表单-2 缺陷情况
  const bugBatchFormRules = ref<FormItemModel[]>([
    {
      field: 'text',
      type: 'input',
      label: '',
      rules: [
        { required: true, message: t('system.orgTemplate.optionContentRules') },
        { notRepeat: true, message: t('system.orgTemplate.optionsContentNoRepeat') },
      ],
      placeholder: 'system.orgTemplate.optionsPlaceholder',
      hideAsterisk: true,
      hideLabel: true,
    },
    {
      field: 'value',
      type: 'input',
      label: '',
      rules: [
        { required: true, message: t('system.orgTemplate.optionsIdTip') },
        { notRepeat: true, message: t('system.orgTemplate.optionsIdNoRepeat') },
      ],
      placeholder: 'system.orgTemplate.optionsIdPlaceholder',
      hideAsterisk: true,
      hideLabel: true,
    },
  ]);
  const optionsModels: Ref<FormItemModel[]> = ref([]);
  const batchFormRef = ref<InstanceType<typeof MsBatchForm>>();

  const resetForm = () => {
    fieldForm.value = { ...initFieldForm };
    selectFormat.value = undefined;
    isMultipleSelectMember.value = false;
    fieldType.value = undefined;
    batchFormRef.value?.resetForm();
  };

  const handleDrawerCancel = () => {
    fieldFormRef.value?.resetFields();
    showDrawer.value = false;
    resetForm();
  };

  const { addOrUpdate, detail } = getFieldRequestApi(props.mode);
  // 保存
  const confirmHandler = async (isContinue = false) => {
    try {
      drawerLoading.value = true;
      const formCopy = cloneDeep(fieldForm.value);

      formCopy.scene = route.query.type;
      formCopy.scopeId = scopeId.value;

      // 如果选择是日期
      if (selectFormat.value) {
        formCopy.type = selectFormat.value;
      }

      // 如果选择是成员（单选||多选）
      if (isMultipleSelectMember.value) {
        formCopy.type = isMultipleSelectMember.value ? 'MULTIPLE_MEMBER' : 'MEMBER';
      }

      // 如果选择是数值
      if (formCopy.type === 'NUMBER') {
        formCopy.type = selectNumber.value;
      }

      // 处理参数
      const { id, name, options, scene, type, remark, enableOptionKey } = formCopy;

      const params: AddOrUpdateField = {
        name,
        used: false,
        options,
        scopeId: scopeId.value,
        scene,
        type,
        remark,
        enableOptionKey,
      };
      if (id) {
        params.id = id;
      }
      const res = await addOrUpdate(params);
      Message.success(isEdit.value ? t('common.updateSuccess') : t('common.newSuccess'));
      if (!isContinue) {
        handleDrawerCancel();
      }
      resetForm();
      emit('success', isEdit.value, res.id);
    } catch (error) {
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  };
  const fieldDefaultValues = ref<FormItemModel[]>([]);
  function userFormFiledValidate(cb: () => Promise<any>) {
    fieldFormRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      if (showOptionsSelect.value) {
        batchFormRef.value?.formValidate(async (list: any) => {
          try {
            drawerLoading.value = true;
            fieldDefaultValues.value = [...list];
            if (showOptionsSelect.value) {
              let startPos = 1;
              fieldForm.value.options = (batchFormRef.value?.getFormResult() || []).map((item: any) => {
                const currentItem: FieldOptions = {
                  text: item.text,
                  value: item.value ? item.value : getGenerateId(),
                  pos: startPos,
                };
                if (item.fieldId) {
                  currentItem.fieldId = item.fieldId;
                }
                startPos += 1;
                return currentItem;
              });
            }
            await cb();
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
          } finally {
            drawerLoading.value = false;
          }
        });
      } else {
        await cb();
      }
    });
  }

  // 新增 || 保存并继续添加
  const handleDrawerConfirm = (isContinue: boolean) => {
    userFormFiledValidate(confirmHandler);
  };
  function saveAndContinue() {
    userFormFiledValidate(async () => {
      await confirmHandler(true);
      resetForm();
    });
  }

  // 字段类型列表选项
  const fieldOptions = ref<fieldIconAndNameModal[]>([]);
  // 处理特殊情况编辑回显
  const getSpecialHandler = (itemType: FormItemType): FormItemType => {
    switch (itemType) {
      case 'INT':
        selectNumber.value = itemType;
        return 'NUMBER';
      case 'FLOAT':
        selectNumber.value = itemType;
        return 'NUMBER';
      case 'MULTIPLE_MEMBER':
        return 'MEMBER';
      case 'DATETIME':
        selectFormat.value = itemType;
        return 'DATE';
      default:
        selectFormat.value = itemType;
        return itemType;
    }
  };

  // 获取字段选项详情
  const getFieldDetail = async (id: string) => {
    try {
      const fieldDetail = await detail(id);
      fieldForm.value = {
        ...fieldDetail,
        type: getSpecialHandler(fieldDetail.type),
      };
      fieldDefaultValues.value = fieldDetail.options.map((item: any) => {
        return {
          ...item,
        };
      });
    } catch (error) {
      console.log(error);
    }
  };

  // 编辑
  const editHandler = (item: AddOrUpdateField) => {
    showDrawer.value = true;
    isMultipleSelectMember.value = item.type === 'MULTIPLE_MEMBER';
    if (item.id) {
      getFieldDetail(item.id);
    }
  };

  // 字段类型改变回调
  const fieldChangeHandler = () => {
    optionsModels.value = [{ ...onlyOptions.value }];
    fieldDefaultValues.value = [];
  };

  watch(
    () => showDrawer.value,
    (val) => {
      emit('update:visible', val);
    }
  );

  watch(
    () => props.visible,
    (val) => {
      showDrawer.value = val;
    }
  );

  watchEffect(() => {
    if (fieldForm.value.id) {
      isEdit.value = true;
    } else {
      isEdit.value = false;
    }
  });

  // 监视是否显示KEY值
  watch(
    () => fieldForm.value.enableOptionKey,
    (val) => {
      if (val && sceneType === 'BUG') {
        optionsModels.value = cloneDeep(bugBatchFormRules.value);
      } else {
        optionsModels.value = [{ ...onlyOptions.value }];
      }
    },
    { immediate: true }
  );
  onMounted(() => {
    const excludeOptions = ['MULTIPLE_MEMBER', 'DATETIME', 'SYSTEM', 'INT', 'FLOAT'];
    fieldOptions.value = fieldIconAndName.filter((item: any) => excludeOptions.indexOf(item.key) < 0);
  });

  defineExpose({
    editHandler,
  });
</script>

<style scoped lang="less">
  .optionsKey {
    position: absolute;
    top: 0;
    right: 0;
  }
</style>
