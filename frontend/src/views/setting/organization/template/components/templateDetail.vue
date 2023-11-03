<template>
  <MsCard
    :loading="loading"
    :title="title"
    :is-edit="isEdit && route.params.mode !== 'copy'"
    has-breadcrumb
    @save="saveHandler"
    @save-and-continue="saveHandler(true)"
  >
    <template #headerRight>
      <div class="rightBtn">
        <a-button v-show="isPreview" type="outline" class="text-[var(--color-text-1)]" @click="togglePreview">{{
          t('system.orgTemplate.templatePreview')
        }}</a-button>
        <a-button v-show="!isPreview" type="outline" class="text-[var(--color-text-1)]" @click="togglePreview">{{
          t('system.orgTemplate.exitPreview')
        }}</a-button>
      </div>
    </template>
    <!-- 非预览模式 -->
    <div v-if="isPreview" class="nonPreview">
      <a-form ref="formRef" :model="templateForm" layout="vertical">
        <a-form-item :label="t('system.orgTemplate.templateName')" field="name" asterisk-position="end" required>
          <a-input
            v-model:model-value="templateForm.name"
            :placeholder="t('system.orgTemplate.templateNamePlaceholder')"
            :max-length="255"
            show-word-limit
            class="max-w-[732px]"
          ></a-input>
        </a-form-item>
        <a-form-item field="remark" :label="t('system.orgTemplate.description')" asterisk-position="end">
          <a-textarea
            v-model="templateForm.remark"
            :max-length="255"
            :placeholder="t('system.orgTemplate.resDescription')"
            :auto-size="{
              maxRows: 1,
            }"
            class="max-w-[732px]"
          ></a-textarea>
        </a-form-item>
        <a-form-item field="remark" label="" asterisk-position="end"
          ><a-checkbox v-model="templateForm.enableThirdPart">{{ t('system.orgTemplate.thirdParty') }}</a-checkbox>
        </a-form-item>
      </a-form>
      <!-- 已有字段表 -->
      <TemplateManagementTable
        ref="templateFieldTableRef"
        v-model:select-data="selectData"
        :data="(totalTemplateField as DefinedFieldItem[])"
        :enable-third-part="templateForm.enableThirdPart"
        @update="updateHandler"
      />
    </div>
    <!-- 预览模式 -->
    <PreviewTemplate v-else :select-field="(selectData as DefinedFieldItem[])" />
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织-模版-模版管理-创建&编辑
   */
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import { FieldTypeFormRules } from '@/components/pure/ms-form-create/form-create';
  import TemplateManagementTable from './templateManagementTable.vue';
  import PreviewTemplate from './viewTemplate.vue';

  import {
    createOrganizeTemplateInfo,
    getFieldList,
    getOrganizeTemplateInfo,
    updateOrganizeTemplateInfo,
  } from '@/api/modules/setting/template';
  import { useI18n } from '@/hooks/useI18n';
  import useLeaveUnSaveTip from '@/hooks/useLeaveUnSaveTip';
  import { useAppStore } from '@/store';
  import { sleep } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';

  import type { ActionTemplateManage, CustomField, DefinedFieldItem } from '@/models/setting/template';
  import { SettingRouteEnum } from '@/enums/routeEnum';

  import { getCardList } from './fieldSetting';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const appStore = useAppStore();
  const currentOrgId = computed(() => appStore.currentOrgId);
  // useLeaveUnSaveTip();

  const title = ref('');
  const loading = ref(false);

  const initTemplateForm: ActionTemplateManage = {
    id: '',
    name: '',
    remark: '',
    scopeId: currentOrgId.value,
    enableThirdPart: false,
  };

  const templateForm = ref<ActionTemplateManage>({ ...initTemplateForm });

  const selectData = ref<DefinedFieldItem[]>([]); // 表格已选择字段
  const selectFiled = ref<DefinedFieldItem[]>([]);

  const formRef = ref<FormInstance>();
  const totalTemplateField = ref<DefinedFieldItem[]>([]);
  const isEdit = computed(() => !!route.query.id);
  const currentOrd = currentOrgId.value;
  const isEditField = ref<boolean>(false);

  // 获取模板详情
  const getTemplateInfo = async () => {
    try {
      loading.value = true;
      const res = await getOrganizeTemplateInfo(route.query.id as string);
      const { name, customFields } = res;
      templateForm.value = {
        ...res,
        name: route.params.mode === 'copy' ? `${name}_copy` : name,
      };
      if (route.params.mode === 'copy') {
        templateForm.value.id = undefined;
      }
      // 处理字段列表
      const customFieldsIds = customFields.map((index: any) => index.fieldId);
      const result = totalTemplateField.value.filter((item) => {
        const currentCustomFieldIndex = customFieldsIds.findIndex((it: any) => it === item.id);
        if (customFieldsIds.indexOf(item.id) > -1) {
          const currentForm = item.formRules?.map((it: any) => {
            it.props.modelValue = customFields[currentCustomFieldIndex].defaultValue;
            return {
              ...it,
              value: customFields[currentCustomFieldIndex].defaultValue,
            };
          });
          const formItem = item;
          formItem.formRules = cloneDeep(currentForm);
          formItem.apiFieldId = customFields[currentCustomFieldIndex].apiFieldId;
          formItem.required = customFields[currentCustomFieldIndex].required;
          return true;
        }
        return false;
      });
      selectData.value = result;
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  };

  // 处理表单数据格式
  const getFieldOptionList = () => {
    totalTemplateField.value = totalTemplateField.value.map((item: any) => {
      const currentFormRules = FieldTypeFormRules[item.type];
      let selectOptions: any = [];
      if (item.options && item.options.length) {
        selectOptions = item.options.map((optionItem: any) => {
          return {
            label: optionItem.text,
            value: optionItem.value,
          };
        });
        currentFormRules.options = selectOptions;
      }
      return {
        ...item,
        formRules: [
          { ...currentFormRules, value: item.value, props: { ...currentFormRules.props, options: selectOptions } },
        ],
        fApi: null,
        required: item.internal,
      };
    });
    // 创建默认系统字段
    if (!isEdit.value && !isEditField.value) {
      selectData.value = totalTemplateField.value.filter((item) => item.internal);
    }
  };

  // 获取字段列表数据
  const getClassifyField = async () => {
    try {
      totalTemplateField.value = await getFieldList({ organizationId: currentOrd, scene: route.query.type });
      getFieldOptionList();
      // 编辑字段就需要单独处理过滤
      if (isEditField.value) {
        selectData.value = totalTemplateField.value.filter(
          (item) => selectFiled.value.map((it) => it.id).indexOf(item.id) > -1
        );
      }
      if (isEdit.value) {
        getTemplateInfo();
      }
    } catch (error) {
      console.log(error);
    }
  };

  watchEffect(async () => {
    if (isEdit.value && route.params.mode === 'copy') {
      title.value = t('system.orgTemplate.copyTemplate');
      getClassifyField();
    } else if (isEdit.value) {
      title.value = t('menu.settings.organization.templateManagementEdit');
      getClassifyField();
    } else {
      title.value = t('menu.settings.organization.templateManagementDetail');
    }
  });

  // 获取模板参数
  function getTemplateParams(): ActionTemplateManage {
    const result = selectData.value.map((item) => {
      if (item.formRules?.length) {
        const { value } = item.formRules[0];
        return {
          fieldId: item.id,
          required: item.required,
          apiFieldId: item.apiFieldId,
          defaultValue: value,
        };
      }
      return [];
    });

    const { name, remark, enableThirdPart, id } = templateForm.value;
    return {
      id,
      name,
      remark,
      enableThirdPart,
      customFields: result as CustomField[],
      scopeId: currentOrgId.value,
      scene: route.query.type,
    };
  }

  function resetForm() {
    templateForm.value = { ...initTemplateForm };
  }

  const isContinueFlag = ref(false);

  // 保存回调
  async function save() {
    try {
      loading.value = true;
      const params = getTemplateParams();
      if (isEdit.value && route.params.mode !== 'copy') {
        await updateOrganizeTemplateInfo(params);
        Message.success(t('system.orgTemplate.updateSuccess'));
      } else {
        await createOrganizeTemplateInfo(params);
        Message.success(t('system.orgTemplate.addSuccess'));
      }
      if (isContinueFlag.value) {
        resetForm();
      } else {
        await sleep(300);
        router.push({ name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT, query: route.query });
      }
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  // 保存
  function saveHandler(isContinue = false) {
    isContinueFlag.value = isContinue;
    formRef.value?.validate().then((res) => {
      if (!res) {
        return save();
      }
      return scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
    });
  }

  // 是否预览模式
  const isPreview = ref<boolean>(true); // 默认非预览模式
  function togglePreview() {
    isPreview.value = !isPreview.value;
  }

  // 计算当前级别title
  const breadTitle = computed(() => {
    const firstBreadTitle = getCardList('organization').find((item) => item.key === route.query.type)?.name;
    const ThirdBreadTitle = title.value;
    return {
      firstBreadTitle,
      ThirdBreadTitle,
    };
  });

  // 更新面包屑标题
  const setBreadText = () => {
    const { breadcrumbList } = appStore;
    const { firstBreadTitle, ThirdBreadTitle } = breadTitle.value;
    if (firstBreadTitle) {
      breadcrumbList[0].locale = firstBreadTitle;
      if (appStore.breadcrumbList.length > 2) {
        breadcrumbList[2].locale = ThirdBreadTitle;
      }
      appStore.setBreadcrumbList(breadcrumbList);
    }
  };
  // 字段表编辑更新表
  const updateHandler = (flag: boolean) => {
    isEditField.value = flag;
    selectFiled.value = selectData.value;
    getClassifyField();
  };

  onMounted(() => {
    setBreadText();
    getClassifyField();
    if (!isEdit.value) {
      selectData.value = totalTemplateField.value.filter((item) => item.internal);
    }
  });
</script>

<style scoped lang="less">
  .rightBtn {
    :deep(.arco-btn-outline) {
      border-color: var(--color-text-input-border) !important;
      color: var(--color-text-1) !important;
    }
  }
</style>
