<template>
  <MsDrawer
    v-model:visible="showDrawer"
    :title="isEdit ? t('system.orgTemplate.update') : t('system.orgTemplate.addField')"
    :ok-text="t(isEdit ? 'system.orgTemplate.update' : 'system.orgTemplate.addField')"
    :ok-loading="drawerLoading"
    :width="680"
    :show-continue="!isEdit"
    @confirm="handleDrawerConfirm"
    @continue="handleDrawerConfirm(true)"
    @cancel="handleDrawerCancel"
  >
    <div class="form">
      <a-form ref="fieldFormRef" class="rounded-[4px]" :model="fieldForm" layout="vertical">
        <a-form-item
          field="name"
          :label="t('system.orgTemplate.fieldName')"
          :rules="[{ required: true, message: t('system.orgTemplate.fieldNameRules') }]"
          required
          asterisk-position="end"
        >
          <a-input
            v-model:model-value="fieldForm.name"
            :placeholder="t('system.orgTemplate.fieldNamePlaceholder')"
            :max-length="250"
            show-word-limit
          ></a-input>
        </a-form-item>
        <a-form-item field="remark" :label="t('system.orgTemplate.description')" asterisk-position="end">
          <a-textarea
            v-model="fieldForm.remark"
            :max-length="250"
            :placeholder="t('system.orgTemplate.resDescription')"
            :auto-size="{
              maxRows: 1,
            }"
          ></a-textarea>
        </a-form-item>
        <a-form-item field="type" :label="t('system.orgTemplate.fieldType')" asterisk-position="end">
          <a-select
            v-model="fieldType"
            class="w-[260px]"
            :placeholder="t('system.orgTemplate.fieldTypePlaceholder')"
            allow-clear
            @change="fieldChangeHandler"
          >
            <a-option v-for="item of fieldOptions" :key="item.value" :value="item.id">
              <div class="flex items-center"
                ><MsIcon :type="item.value" class="mx-2" /> <span>{{ item.label }}</span></div
              >
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item
          v-if="fieldType === 'MEMBER'"
          field="type"
          :label="t('system.orgTemplate.allowMultiMember')"
          asterisk-position="end"
        >
          <a-switch v-model="isMultipleSelectMember" size="small" />
        </a-form-item>
        <!-- 选项选择器 -->
        <a-form-item
          v-if="showOptionsSelect"
          field="options"
          :label="t('system.orgTemplate.optionContent')"
          asterisk-position="end"
          :rules="[{ message: t('system.orgTemplate.optionContentRules') }]"
        >
          <MsBatchForm
            ref="batchFormRef"
            :models="optionsModels"
            form-mode="create"
            add-text="system.orgTemplate.addOptions"
            :is-show-drag="true"
            form-width="340px"
            :default-vals="fieldDefaultValues"
          />
        </a-form-item>
        <!-- 日期和数值 -->
        <a-form-item
          v-if="showDateOrNumber"
          field="selectFormat"
          :label="
            fieldForm.type === 'NUMBER' ? t('system.orgTemplate.numberFormat') : t('system.orgTemplate.dateFormat')
          "
          asterisk-position="end"
        >
          <a-select
            v-model="selectFormat"
            class="w-[260px]"
            :placeholder="t('system.orgTemplate.formatPlaceholder')"
            allow-clear
          >
            <a-option v-for="item of showDateOrNumber" :key="item.value" :value="item.value">
              <div class="flex items-center">{{ item.label }}</div>
            </a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBatchForm from '@/components/business/ms-batch-form/index.vue';
  import type { FormItemModel, MsBatchFormInstance } from '@/components/business/ms-batch-form/types';

  import { addOrUpdateOrdField, getOrdFieldDetail } from '@/api/modules/setting/template';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { getGenerateId } from '@/utils';

  import type { AddOrUpdateField } from '@/models/setting/template';
  import { TemplateIconEnum } from '@/enums/templateEnum';

  import { getFieldType } from './fieldSetting';

  const { t } = useI18n();
  const route = useRoute();
  const appStore = useAppStore();

  const props = defineProps<{
    visible: boolean;
  }>();
  const emit = defineEmits(['success', 'update:visible']);

  const showDrawer = ref<boolean>(false);
  const drawerLoading = ref<boolean>(false);

  const fieldFormRef = ref<FormInstance>();
  const initFieldForm: AddOrUpdateField = {
    name: '',
    type: '',
    remark: '',
    scopeId: '',
    scene: 'FUNCTIONAL',
    options: [],
  };
  const fieldForm = ref<AddOrUpdateField>({ ...initFieldForm });
  const isEdit = computed(() => !!fieldForm.value.id);
  const selectFormat = ref(''); // 选择格式
  const isMultipleSelectMember = ref<boolean | undefined>(false); // 成员多选
  const fieldType = ref(''); // 整体字段类型

  // 是否展示选项添加面板
  const showOptionsSelect = computed(() => {
    const showOptionsType = ['RADIO', 'CHECKBOX', 'SELECT', 'MULTIPLE_SELECT'];
    return showOptionsType.includes(fieldType.value);
  });

  // 是否展示日期或数值
  const showDateOrNumber = computed(() => {
    return getFieldType(fieldType.value);
  });

  // 批量表单-1.仅选项情况
  const onlyOptions: Ref<FormItemModel> = ref({
    filed: 'text',
    type: 'input',
    label: '',
    rules: [{ required: true, message: t('system.orgTemplate.optionContentRules') }],
    placeholder: t('system.orgTemplate.optionsPlaceholder'),
    hideAsterisk: true,
    hideLabel: true,
  });
  const optionsModels: Ref<FormItemModel[]> = ref([{ ...onlyOptions.value }]);
  const batchFormRef = ref<MsBatchFormInstance | null>(null);

  const resetForm = () => {
    fieldForm.value = { ...initFieldForm };
    selectFormat.value = '';
    isMultipleSelectMember.value = false;
    fieldType.value = '';
    batchFormRef.value?.resetForm();
  };

  const handleDrawerCancel = () => {
    fieldFormRef.value?.resetFields();
    showDrawer.value = false;
    resetForm();
  };

  // 保存
  const confirmHandler = async (isContinue: boolean) => {
    try {
      drawerLoading.value = true;
      fieldForm.value.scene = route.query.type;
      fieldForm.value = {
        ...fieldForm.value,
        scopeId: appStore.currentOrgId,
        type: fieldType.value,
      };
      // 如果选择是日期或者数值
      if (selectFormat.value) {
        fieldForm.value.type = selectFormat.value;
      }
      // 如果选择是成员（单选||多选）
      if (isMultipleSelectMember.value) {
        fieldForm.value.type = isMultipleSelectMember.value ? 'MULTIPLE_MEMBER' : 'MEMBER';
      }
      // 如果选择是日期或者是数值
      if (selectFormat.value) {
        fieldForm.value.type = selectFormat.value;
      }

      // 处理参数
      const { id, name, options, scopeId, scene, type, remark } = fieldForm.value;
      const params: AddOrUpdateField = { name, options, scopeId, scene, type, remark };
      if (isEdit) {
        params.id = id;
      }
      await addOrUpdateOrdField(params);
      Message.success(isEdit ? t('common.addSuccess') : t('common.updateSuccess'));
      if (!isContinue) {
        handleDrawerCancel();
      }
      resetForm();
      emit('success');
    } catch (error) {
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  };

  // 新增 || 保存并继续添加
  const handleDrawerConfirm = (isContinue: boolean) => {
    fieldFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        if (showOptionsSelect) {
          fieldForm.value.options = (batchFormRef.value?.getFormResult() || []).map((item: any) => {
            return {
              ...item,
              value: getGenerateId(),
            };
          });
        }
        confirmHandler(isContinue);
      }
    });
  };

  // 字段类型列表选项
  const fieldOptions = ref([
    {
      id: 'TEXTAREA',
      label: t('system.orgTemplate.textarea'),
      value: TemplateIconEnum.TEXTAREA,
    },
    {
      id: 'INPUT',
      label: t('system.orgTemplate.input'),
      value: TemplateIconEnum.INPUT,
    },
    {
      id: 'RADIO',
      label: t('system.orgTemplate.radio'),
      value: TemplateIconEnum.RADIO,
    },
    {
      id: 'CHECKBOX',
      label: t('system.orgTemplate.checkbox'),
      value: TemplateIconEnum.CHECKBOX,
    },
    {
      id: 'SELECT',
      label: t('system.orgTemplate.select'),
      value: TemplateIconEnum.SELECT,
    },
    {
      id: 'MULTIPLE_SELECT',
      label: t('system.orgTemplate.multipleSelect'),
      value: TemplateIconEnum.MULTIPLE_SELECT,
    },
    {
      id: 'MEMBER',
      label: t('system.orgTemplate.member'),
      value: TemplateIconEnum.MEMBER,
    },
    {
      id: 'DATE',
      label: t('system.orgTemplate.date'),
      value: TemplateIconEnum.DATE,
    },
    {
      id: 'NUMBER',
      label: t('system.orgTemplate.number'),
      value: TemplateIconEnum.NUMBER,
    },
    {
      id: 'MULTIPLE_INPUT',
      label: t('system.orgTemplate.multipleInput'),
      value: TemplateIconEnum.MULTIPLE_INPUT,
    },
  ]);

  const fieldDefaultValues = ref([]);

  // 获取字段选项详情
  const getFieldDetail = async (id: string) => {
    try {
      const fieldDetail = await getOrdFieldDetail(id);
      fieldDefaultValues.value = fieldDetail.options.map((item: any) => {
        return {
          ...item,
        };
      });
    } catch (error) {
      console.log(error);
    }
  };

  // 处理特殊情况编辑回显
  const getSpecialHandler = (itemType: string): string => {
    switch (itemType) {
      case 'INT':
        selectFormat.value = itemType;
        return 'NUMBER';
      case 'FLOAT':
        selectFormat.value = itemType;
        return 'NUMBER';
      case 'MULTIPLE_MEMBER':
        return 'MEMBER';
      case 'DATETIME':
        selectFormat.value = itemType;
        return 'DATE';
      default:
        return itemType;
    }
  };

  // 编辑
  const isEditHandler = (item: AddOrUpdateField) => {
    showDrawer.value = true;
    isMultipleSelectMember.value = item.type === 'MULTIPLE_MEMBER';
    if (isEdit && item.id) {
      getFieldDetail(item.id);
      fieldForm.value = {
        ...item,
        type: getSpecialHandler(item.type),
      };
      fieldType.value = getSpecialHandler(item.type);
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

  defineExpose({
    isEditHandler,
  });
</script>

<style scoped></style>
