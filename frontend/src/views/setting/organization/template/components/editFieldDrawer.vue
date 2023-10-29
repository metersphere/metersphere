<template>
  <MsDrawer
    v-model:visible="showDrawer"
    :title="isEdit ? t('system.orgTemplate.update') : t('system.orgTemplate.addField')"
    :ok-text="t(isEdit ? 'system.orgTemplate.update' : 'system.orgTemplate.addField')"
    :ok-loading="drawerLoading"
    :width="800"
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
            :max-length="255"
            show-word-limit
          ></a-input>
        </a-form-item>
        <a-form-item field="remark" :label="t('system.orgTemplate.description')" asterisk-position="end">
          <a-textarea
            v-model="fieldForm.remark"
            :max-length="255"
            :placeholder="t('system.orgTemplate.resDescription')"
            :auto-size="{
              maxRows: 1,
            }"
          ></a-textarea>
        </a-form-item>
        <a-form-item field="type" :label="t('system.orgTemplate.fieldType')" asterisk-position="end" :required="true">
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
          <a-switch v-model="isMultipleSelectMember" size="small" />
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
              >选项KEY值
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
            :disabled="isEdit"
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
  import { cloneDeep } from 'lodash-es';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import type { FormItemType } from '@/components/pure/ms-form-create/types';
  import MsBatchForm from '@/components/business/ms-batch-form/index.vue';
  import type { FormItemModel, MsBatchFormInstance } from '@/components/business/ms-batch-form/types';

  import { addOrUpdateOrdField, getOrdFieldDetail } from '@/api/modules/setting/template';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { getGenerateId } from '@/utils';

  import type { AddOrUpdateField, fieldIconAndNameModal } from '@/models/setting/template';

  import { fieldIconAndName, getFieldType } from './fieldSetting';

  const { t } = useI18n();
  const route = useRoute();
  const appStore = useAppStore();
  const sceneType = route.query.type;
  const props = defineProps<{
    visible: boolean;
  }>();
  const emit = defineEmits(['success', 'update:visible']);

  const showDrawer = ref<boolean>(false);
  const drawerLoading = ref<boolean>(false);

  const fieldFormRef = ref<FormInstance>();
  const initFieldForm: AddOrUpdateField = {
    name: '',
    type: undefined,
    remark: '',
    scopeId: '',
    scene: 'FUNCTIONAL',
    options: [],
    enableOptionKey: false,
  };
  const fieldForm = ref<AddOrUpdateField>({ ...initFieldForm });
  const isEdit = ref<boolean>(false);
  const selectFormat = ref<FormItemType>(); // 选择格式
  const isMultipleSelectMember = ref<boolean | undefined>(false); // 成员多选
  const fieldType = ref<FormItemType>(); // 整体字段类型

  // 是否展示选项添加面板
  const showOptionsSelect = computed(() => {
    const showOptionsType: FormItemType[] = ['RADIO', 'CHECKBOX', 'SELECT', 'MULTIPLE_SELECT'];
    return showOptionsType.includes(fieldForm.value.type as FormItemType);
  });

  // 是否展示日期或数值
  const showDateOrNumber = computed(() => {
    if (fieldForm.value.type) return getFieldType(fieldForm.value.type);
  });

  // 批量表单-1.仅选项情况
  const onlyOptions: Ref<FormItemModel> = ref({
    filed: 'text',
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
      filed: 'text',
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
      filed: 'value',
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
  const batchFormRef = ref<MsBatchFormInstance | null>(null);

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

  // 保存
  const confirmHandler = async (isContinue: boolean) => {
    try {
      drawerLoading.value = true;

      const formCopy = cloneDeep(fieldForm.value);

      formCopy.scene = route.query.type;
      formCopy.scopeId = appStore.currentOrgId;

      // 如果选择是日期或者数值
      if (selectFormat.value) {
        formCopy.type = selectFormat.value;
      }

      // 如果选择是成员（单选||多选）
      if (isMultipleSelectMember.value) {
        formCopy.type = isMultipleSelectMember.value ? 'MULTIPLE_MEMBER' : 'MEMBER';
      }

      // 如果选择是日期或者是数值
      if (selectFormat.value) {
        formCopy.type = selectFormat.value;
      }

      // 处理参数
      const { id, name, options, scopeId, scene, type, remark, enableOptionKey } = formCopy;

      const params: AddOrUpdateField = {
        name,
        options,
        scopeId,
        scene,
        type,
        remark,
        enableOptionKey,
      };
      if (id) {
        params.id = id;
      }
      await addOrUpdateOrdField(params);
      Message.success(isEdit.value ? t('common.updateSuccess') : t('common.addSuccess'));
      if (!isContinue) {
        handleDrawerCancel();
      }
      resetForm();
      emit('success', isEdit.value);
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
              value: fieldForm.value.enableOptionKey ? item.value : getGenerateId(),
            };
          });
        }
        confirmHandler(isContinue);
      }
    });
  };

  // 字段类型列表选项
  const fieldOptions = ref<fieldIconAndNameModal[]>([]);
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
  const getSpecialHandler = (itemType: FormItemType): FormItemType => {
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
  const editHandler = (item: AddOrUpdateField) => {
    showDrawer.value = true;
    isMultipleSelectMember.value = item.type === 'MULTIPLE_MEMBER';
    if (item.id) {
      getFieldDetail(item.id);
      fieldForm.value = {
        ...item,
        type: getSpecialHandler(item.type),
      };
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
