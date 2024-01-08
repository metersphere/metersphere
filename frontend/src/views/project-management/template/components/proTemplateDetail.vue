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
        <a-form-item
          :label="t('system.orgTemplate.templateName')"
          field="name"
          asterisk-position="end"
          :rules="[{ required: true, message: t('system.orgTemplate.templateNameRules') }]"
        >
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
        <a-form-item v-if="route.query.type === 'BUG'" field="remark" label="" asterisk-position="end"
          ><a-checkbox v-model="templateForm.enableThirdPart">{{ t('system.orgTemplate.thirdParty') }}</a-checkbox>
        </a-form-item>
        <a-form-item
          v-if="route.query.type === 'BUG'"
          field="fieldType"
          :label="t('system.orgTemplate.columnFieldType')"
          asterisk-position="end"
        >
          <a-radio-group v-model="fieldType" type="button">
            <a-radio value="custom">{{ t('system.orgTemplate.custom') }}</a-radio>
            <a-radio value="detail">{{ t('system.orgTemplate.details') }}</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
      <!-- 已有字段表 -->
      <TemplateManagementTable
        v-if="fieldType === 'custom'"
        ref="templateFieldTableRef"
        v-model:select-data="selectData"
        :data="(totalTemplateField as DefinedFieldItem[])"
        :enable-third-part="templateForm.enableThirdPart"
        mode="project"
        @update="updateHandler"
      />
      <!-- 缺陷详情表 -->
      <a-form
        v-if="fieldType === 'detail'"
        ref="defectFormRef"
        class="rounded-[4px]"
        :model="defectForm"
        layout="vertical"
      >
        <a-form-item
          class="max-w-[732px]"
          field="name"
          :label="t('system.orgTemplate.defectName')"
          :rules="[{ required: true, message: t('system.orgTemplate.defectNamePlaceholder') }]"
          required
          asterisk-position="end"
        >
          <a-input
            v-model="defectForm.name"
            :max-length="255"
            :placeholder="t('system.orgTemplate.defectNamePlaceholder')"
            show-word-limit
            allow-clear
          ></a-input>
          <MsFormItemSub :text="t('system.orgTemplate.defectNameTip')" :show-fill-icon="false" />
        </a-form-item>
        <a-form-item
          field="precondition"
          :label="t('system.orgTemplate.defectContent')"
          asterisk-position="end"
          class="max-w-[732px]"
        >
          <MsRichText v-model:raw="defectForm.description" />
          <MsFormItemSub :text="t('system.orgTemplate.defectContentTip')" :show-fill-icon="false" />
        </a-form-item>
      </a-form>
    </div>
    <!-- 预览模式 -->
    <PreviewTemplate
      v-else
      :select-field="(selectData as DefinedFieldItem[])"
      :template-type="route.query.type"
      :defect-form="defectForm"
    />
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-项目-模版-模版管理-创建&编辑
   */
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsFormItemSub from '@/components/business/ms-form-item-sub/index.vue';
  import TemplateManagementTable from '@/views/setting/organization/template/components/templateManagementTable.vue';
  import PreviewTemplate from '@/views/setting/organization/template/components/viewTemplate.vue';

  import {
    createProjectTemplateInfo,
    getProjectFieldList,
    getProjectTemplateInfo,
    updateProjectTemplateInfo,
  } from '@/api/modules/setting/template';
  import { useI18n } from '@/hooks/useI18n';
  import useLeaveUnSaveTip from '@/hooks/useLeaveUnSaveTip';
  import { useAppStore } from '@/store';
  import { sleep } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';

  import type { ActionTemplateManage, CustomField, DefinedFieldItem } from '@/models/setting/template';
  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';

  import {
    getCardList,
    getCustomDetailFields,
    getTotalFieldOptionList,
  } from '@/views/setting/organization/template/components/fieldSetting';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);

  const { setState } = useLeaveUnSaveTip();

  setState(false);

  const title = ref('');
  const loading = ref(false);

  const initTemplateForm: ActionTemplateManage = {
    id: '',
    name: '',
    remark: '',
    scopeId: currentProjectId.value,
    enableThirdPart: false,
  };

  const fieldType = ref<string>('custom'); // 缺陷模板字段类型

  const templateForm = ref<ActionTemplateManage>({ ...initTemplateForm });

  const selectData = ref<DefinedFieldItem[]>([]); // 表格已选择字段
  const selectFiled = ref<DefinedFieldItem[]>([]);

  const formRef = ref<FormInstance>();
  const totalTemplateField = ref<DefinedFieldItem[]>([]);
  const isEdit = computed(() => !!route.query.id);
  const isEditField = ref<boolean>(false);
  const systemFieldData = ref<CustomField[]>([]);

  // 获取模板详情
  const getTemplateInfo = async () => {
    try {
      loading.value = true;
      const res = await getProjectTemplateInfo(route.query.id as string);
      const { name, customFields, systemFields } = res;
      templateForm.value = {
        ...res,
        name: route.params.mode === 'copy' ? `${name}_copy` : name,
      };
      if (route.params.mode === 'copy') {
        templateForm.value.id = undefined;
      }
      selectData.value = getCustomDetailFields(totalTemplateField.value as DefinedFieldItem[], customFields);
      systemFieldData.value = systemFields;
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  };

  // 处理表单数据格式
  const getFieldOptionList = () => {
    totalTemplateField.value = getTotalFieldOptionList(totalTemplateField.value as DefinedFieldItem[]);
    // 创建默认系统字段
    if (!isEdit.value && !isEditField.value) {
      selectData.value = totalTemplateField.value.filter((item) => item.internal);
    }
  };

  // 获取字段列表数据
  const getClassifyField = async () => {
    try {
      totalTemplateField.value = await getProjectFieldList({
        scopedId: currentProjectId.value,
        scene: route.query.type,
      });
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

  const initDefectForm = {
    name: '',
    description: '',
  };
  // 缺陷详情字段
  const defectForm = ref<Record<string, any>>({ ...initDefectForm });

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

    // 处理缺陷系统详情字段
    const sysDetailFields = Object.keys(defectForm.value).map((formKey: string) => {
      return {
        fieldId: formKey,
        defaultValue: defectForm.value[formKey],
      };
    });
    const { name, remark, enableThirdPart, id } = templateForm.value;
    return {
      id,
      name,
      remark,
      enableThirdPart,
      customFields: result as CustomField[],
      scopeId: currentProjectId.value,
      scene: route.query.type,
      systemFields: sysDetailFields,
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
        await updateProjectTemplateInfo(params);
        Message.success(t('system.orgTemplate.updateSuccess'));
      } else {
        await createProjectTemplateInfo(params);
        Message.success(t('system.orgTemplate.addSuccess'));
      }
      if (isContinueFlag.value) {
        resetForm();
      } else {
        await sleep(300);
        router.push({ name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT, query: route.query });
        setState(true);
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
    const firstBreadTitle = getCardList('organization').find((item: any) => item.key === route.query.type)?.name;
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

  watch(
    () => systemFieldData.value,
    (val) => {
      if (val) {
        defectForm.value = { ...initDefectForm };
        systemFieldData.value.forEach((item) => {
          defectForm.value[item.fieldId] = item.defaultValue;
        });
      }
    },
    { deep: true }
  );

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
