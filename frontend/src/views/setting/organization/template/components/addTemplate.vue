<template>
  <MsCard
    :special-height="55"
    :loading="loading"
    :title="title"
    :is-edit="isEdit && route.params.mode !== 'copy'"
    has-breadcrumb
    :hide-back="true"
    @save="saveHandler"
    @save-and-continue="saveHandler(true)"
  >
    <template #headerLeft>
      <a-alert v-if="templateForm.enableThirdPart && route.query.type === 'BUG'" class="mb-[16px] w-full">
        {{ t('system.orgTemplate.enableApiAlert') }}
      </a-alert>
      <a-form ref="formRef" class="mt-1 max-w-[710px]" :model="templateForm">
        <a-form-item
          v-if="!templateForm?.internal"
          field="name"
          asterisk-position="end"
          :hide-label="true"
          hide-asterisk
          content-class="contentClass"
          class="mb-0 max-w-[710px]"
        >
          <a-input
            v-model:model-value="templateForm.name"
            :placeholder="t('system.orgTemplate.templateNamePlaceholder')"
            :max-length="255"
            class="max-w-[732px]"
            :error="isError"
            :disabled="templateForm?.internal"
            @input="inputHandler"
          ></a-input>
        </a-form-item>
        <span v-else class="font-medium text-[var(--color-text-1)] underline">{{ templateForm.name }}</span>
      </a-form>
    </template>
    <template #headerRight>
      <div class="flex items-center">
        <!-- <a-select
          v-if="templateForm.enableThirdPart && route.query.type === 'BUG'"
          v-model="templateForm.platForm"
          class="!my-0 w-[240px]"
          :placeholder="t('system.orgTemplate.selectThirdPlatType')"
        >
          <a-option v-for="item of platFormList" :key="item.value" :value="item.value">{{ item.label }}</a-option>
        </a-select> -->
        <a-checkbox v-if="route.query.type === 'BUG'" v-model="templateForm.enableThirdPart" class="mx-2"
          >{{ t('system.orgTemplate.thirdParty') }}
        </a-checkbox>
        <MsTag size="large" class="cursor-pointer" theme="outline" @click="brash">
          <MsIcon class="text-[var(color-text-4)]" :size="16" type="icon-icon_reset_outlined" />
        </MsTag>
      </div>
    </template>
    <div class="wrapper-preview">
      <div class="preview-left pr-4">
        <DefectTemplateLeftContent
          v-if="route.query.type === 'BUG'"
          v-model:uploadImgFileIds="uploadBugImgFileIds"
          v-model:defaultForm="defaultBugForm"
          :mode="props.mode"
        />
        <CaseTemplateLeftContent
          v-else
          v-model:uploadImgFileIds="uploadCaseImgFileIds"
          v-model:defaultForm="defaultCaseForm"
          :mode="props.mode"
        />
      </div>
      <div class="preview-right px-4">
        <!-- 系统内置的字段 {处理人, 状态...} -->
        <DefectTemplateRightSystemField v-if="route.query.type === 'BUG'" />
        <CaseTemplateRightSystemField v-else />
        <!-- 自定义字段开始 -->
        <VueDraggable v-model="selectData" handle=".form" ghost-class="ghost" @change="changeDrag">
          <div v-for="(formItem, index) of selectData" :key="formItem.id" class="customWrapper">
            <div class="action">
              <span class="required">
                <a-checkbox
                  v-model="formItem.required"
                  :disabled="formItem.internal && formItem.internalFieldKey === 'functional_priority'"
                  class="mr-1"
                  @change="(value) => changeState(value, formItem as DefinedFieldItem)"
                >
                  {{ t('system.orgTemplate.required') }}
                </a-checkbox>
              </span>
              <div class="actionList">
                <a-tooltip :content="t('system.orgTemplate.toTop')">
                  <MsIcon
                    type="icon-icon_up_outlined"
                    size="16"
                    :class="getColor(index, 'top')"
                    @click="moveField(formItem as DefinedFieldItem, 'top')"
                  />
                </a-tooltip>
                <a-divider direction="vertical" class="!m-0 !mx-2" />
                <a-tooltip :content="t('system.orgTemplate.toBottom')">
                  <MsIcon
                    :class="getColor(index, 'bottom')"
                    type="icon-icon_down_outlined"
                    size="16"
                    @click="moveField(formItem as DefinedFieldItem, 'bottom')"
                  />
                </a-tooltip>
                <a-divider
                  v-if="!formItem.internal || formItem.fieldName != t('case.caseLevel')"
                  direction="vertical"
                  class="!m-0 !mx-2"
                />
                <a-tooltip :content="t('common.edit')">
                  <MsIcon
                    v-if="!formItem.internal"
                    type="icon-icon_edit_outlined"
                    size="16"
                    @click="editField(formItem as DefinedFieldItem)"
                  />
                </a-tooltip>
                <a-divider v-if="!formItem.internal" direction="vertical" class="!m-0 !mx-2" />
                <a-tooltip :content="t('common.delete')">
                  <MsIcon
                    v-if="formItem.fieldName != t('case.caseLevel')"
                    type="icon-icon_delete-trash_outlined1"
                    size="16"
                    @click="deleteSelectedField(formItem as DefinedFieldItem)"
                  />
                </a-tooltip>
              </div>
            </div>
            <div
              class="form"
              :class="{
                'hover:border-[var(--color-text-n8)]': activeIndex !== index,
                'activeStyle': activeIndex === index,
              }"
              @click="activeHandler(index)"
            >
              <div class="mb-[8px] flex w-full items-center">
                <a-tooltip
                  :content="formItem.formRules && formItem.formRules[0] ? formItem?.formRules[0]?.title : ''"
                  position="left"
                >
                  <div class="one-line-text max-w-[calc(100%-200px)]">{{
                    formItem.formRules && formItem.formRules[0] ? formItem?.formRules[0]?.title : ''
                  }}</div>
                </a-tooltip>
                <div v-if="formItem.required" class="ml-[2px] flex items-center">
                  <svg-icon
                    width="6px"
                    height="18px"
                    name="form-star"
                    class="-mt-[2px] text-[12px] font-medium text-[rgb(var(--danger-6))]"
                /></div>
              </div>
              <!-- 表单 -->
              <MsFormCreate
                v-model:api="formItem.fApi"
                v-model:rule="formItem.formRules"
                :option="configOptions"
                is-in-for
                @click="activeHandler(index)"
              />
              <a-form
                v-if="templateForm.enableThirdPart && route.query.type === 'BUG'"
                :ref="(el: refItem) => setStepRefMap(el, formItem as DefinedFieldItem)"
                :model="formItem"
              >
                <a-form-item
                  row-class="apiFieldIdClass"
                  hide-asterisk
                  hide-label
                  field="apiFieldId"
                  :rules="[{ required: true, message: t('system.orgTemplate.apiFieldNotEmpty') }]"
                >
                  <a-input
                    v-model:model-value="formItem.apiFieldId"
                    :placeholder="t('system.orgTemplate.pleaseEnterAPITip')"
                    class="mt-1"
                    :max-length="255"
                  />
                </a-form-item>
              </a-form>
            </div>
          </div>
        </VueDraggable>
        <!-- 自定义字段结束 -->

        <!-- 标签字段开始 -->
        <div class="tagWrapper">
          <a-form layout="vertical" :model="defectForm">
            <a-form-item field="tags" :label="t('system.orgTemplate.tags')" asterisk-position="end">
              <a-input :disabled="true" :placeholder="t('system.orgTemplate.noDefaultPlaceholder')" />
            </a-form-item>
          </a-form>
        </div>
        <!-- 标签字段结束 -->

        <div class="flex items-center">
          <a-button class="mr-1 mt-1 px-0" type="text" @click="associatedField">
            <template #icon>
              <icon-plus class="text-[14px]" />
            </template>
            {{ t('system.orgTemplate.associatedField') }}
          </a-button>
          <a-tooltip :content="t('system.orgTemplate.associatedHasField')" placement="top" effect="dark">
            <IconQuestionCircle
              class="mr-8 mt-1 h-[16px] w-[16px] text-[--color-text-4] hover:text-[rgb(var(--primary-5))]"
            />
          </a-tooltip>

          <a-button class="mr-1 mt-1 px-0" type="text" :disabled="totalTemplateField.length >= 20" @click="createField">
            <template #icon>
              <icon-plus class="text-[14px]" />
            </template>
            {{ t('system.orgTemplate.addField') }}
          </a-button>
          <a-tooltip :content="t('system.orgTemplate.addFieldDesc')" placement="top" effect="dark">
            <IconQuestionCircle
              class="mt-1 h-[16px] w-[16px] text-[--color-text-4] hover:text-[rgb(var(--primary-5))]"
            />
          </a-tooltip>
        </div>
      </div>
      <!-- 添加字段到模板抽屉 -->
      <AddFieldToTemplateDrawer
        ref="fieldSelectRef"
        v-model:visible="showDrawer"
        :total-data="(totalTemplateField as DefinedFieldItem[])"
        :table-select-data="(selectData as DefinedFieldItem[])"
        :mode="props.mode"
        @confirm="confirmHandler"
        @update-data="updateFieldHandler"
      />
      <EditFieldDrawer
        ref="fieldDrawerRef"
        v-model:visible="showFieldDrawer"
        :mode="props.mode"
        :data="(totalTemplateField as DefinedFieldItem[])"
        @success="updateFieldHandler"
      />
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 模板-创建模板&编辑模板-预览模板
   */
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import { VueDraggable } from 'vue-draggable-plus';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import { FieldTypeFormRules } from '@/components/pure/ms-form-create/form-create';
  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import AddFieldToTemplateDrawer from './addFieldToTemplateDrawer.vue';
  import CaseTemplateLeftContent from './caseTemplateLeftContent.vue';
  import DefectTemplateLeftContent from './defectTemplateLeftContent.vue';
  import EditFieldDrawer from './editFieldDrawer.vue';
  import CaseTemplateRightSystemField from '@/views/setting/organization/template/components/caseTemplateRightSystemField.vue';
  import DefectTemplateRightSystemField from '@/views/setting/organization/template/components/defectTemplateRightSystemField.vue';

  import {
    createOrganizeTemplateInfo,
    createProjectTemplateInfo,
    getFieldList,
    getOrganizeTemplateInfo,
    getProjectFieldList,
    getProjectTemplateInfo,
    updateOrganizeTemplateInfo,
    updateProjectTemplateInfo,
  } from '@/api/modules/setting/template';
  import { defaultTemplateBugDetail, defaultTemplateCaseDetail } from '@/config/template';
  import { useI18n } from '@/hooks/useI18n';
  import useLeaveUnSaveTip from '@/hooks/useLeaveUnSaveTip';
  import { useAppStore } from '@/store';
  import { sleep } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';

  import type {
    ActionTemplateManage,
    CustomField,
    defaultBugField,
    defaultCaseField,
    DefinedFieldItem,
  } from '@/models/setting/template';
  import { ProjectManagementRouteEnum, SettingRouteEnum } from '@/enums/routeEnum';

  import { getTemplateName, getTotalFieldOptionList } from './fieldSetting';
  import { FormInstance } from '@arco-design/web-vue/es/form';

  const props = defineProps<{
    mode: 'organization' | 'project';
  }>();

  type refItem = Element | ComponentPublicInstance | null;
  const appStore = useAppStore();
  const currentOrgId = computed(() => appStore.currentOrgId);
  const currentProjectId = computed(() => appStore.currentProjectId);

  const { setIsSave } = useLeaveUnSaveTip();

  setIsSave(false);
  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();

  const title = ref('');
  const initTemplateForm: ActionTemplateManage = {
    id: '',
    name: '',
    remark: '',
    scopeId: props.mode === 'organization' ? currentOrgId.value : currentProjectId.value,
    enableThirdPart: false,
    platForm: '',
    uploadImgFileIds: [],
  };

  const initBugForm = {
    name: '',
    description: '',
    tags: '',
  };

  const defectForm = ref({ ...initBugForm });

  const isEdit = computed(() => !!route.query.id);
  const loading = ref(false);

  const formRef = ref<FormInstance>();
  const templateForm = ref<ActionTemplateManage>({ ...initTemplateForm });
  const isContinueFlag = ref(false);

  const platFormList = ref<{ value: string; label: string }[]>([
    {
      value: 'zental',
      label: '禅道',
    },
    {
      value: 'JIRA',
      label: 'JIRA',
    },
  ]);

  const templateApiMaps: Record<string, any> = {
    organization: {
      fieldList: getFieldList,
      create: createOrganizeTemplateInfo,
      update: updateOrganizeTemplateInfo,
      detail: getOrganizeTemplateInfo,
    },
    project: {
      fieldList: getProjectFieldList,
      create: createProjectTemplateInfo,
      update: updateProjectTemplateInfo,
      detail: getProjectTemplateInfo,
    },
  };

  const totalTemplateField = ref<DefinedFieldItem[]>([]);
  const selectData = ref<DefinedFieldItem[]>([]);

  // 用例默认字段
  const defaultCaseForm = ref<defaultCaseField>(cloneDeep(defaultTemplateCaseDetail));
  // 缺陷默认字段
  const defaultBugForm = ref<defaultBugField>(cloneDeep(defaultTemplateBugDetail));

  const uploadBugImgFileIds = ref<string[]>([]);
  const uploadCaseImgFileIds = ref<string[]>([]);

  // 获取系统默认字段
  function getSystemFields(form: Record<string, any>) {
    const tempFormField: Record<string, any>[] = [];
    Object.keys(form).forEach((key: any) => {
      if (form[key]) {
        tempFormField.push({
          fieldId: key,
          defaultValue: form[key],
        });
      }
    });
    return tempFormField;
  }

  // 获取模板参数
  function getTemplateParams(): ActionTemplateManage {
    const result = selectData.value.map((item) => {
      if (item.formRules?.length) {
        const { value } = item.formRules[0];
        let setValue;
        if (typeof value === 'number') {
          setValue = value;
        } else {
          setValue = value || '';
        }
        return {
          fieldId: item.id,
          required: item.required,
          apiFieldId: item.apiFieldId || '',
          defaultValue: setValue,
        };
      }
      return [];
    });

    const systemFields: Record<string, any>[] =
      route.query.type === 'BUG' ? getSystemFields(defaultBugForm.value) : getSystemFields(defaultCaseForm.value);

    const uploadImgFileIds = route.query.type === 'BUG' ? uploadBugImgFileIds.value : uploadCaseImgFileIds.value;

    const { name, remark, enableThirdPart, id } = templateForm.value;
    return {
      id,
      name,
      remark,
      enableThirdPart,
      customFields: result as CustomField[],
      scopeId: props.mode === 'organization' ? currentOrgId.value : currentProjectId.value,
      scene: route.query.type,
      systemFields,
      uploadImgFileIds,
    };
  }

  function resetForm() {
    templateForm.value = { ...initTemplateForm };
  }

  const isError = ref(false);

  function inputHandler(value: string) {
    if (value.trim().length === 0) {
      isError.value = true;
    }
    isError.value = false;
  }

  // 保存回调
  async function save() {
    try {
      loading.value = true;
      const params = getTemplateParams();
      let isSysErr = false;
      if (params.systemFields) {
        params.systemFields.forEach((item) => {
          if (item.fieldId === 'description' && item.defaultValue && item.defaultValue.length > 1500) {
            Message.error(t('system.orgTemplate.description.too.long'));
            isSysErr = true;
          }
        });
      }
      if (isSysErr) {
        // 系统字段错误, 不保存
        return;
      }
      if (!templateForm.value.name) {
        isError.value = true;
        Message.error(t('system.orgTemplate.templateNamePlaceholder'));
        return;
      }
      if (isEdit.value && route.params.mode !== 'copy') {
        await templateApiMaps[props.mode].update(params);
        Message.success(t('system.orgTemplate.updateSuccess'));
      } else {
        await templateApiMaps[props.mode].create(params);
        Message.success(t('system.orgTemplate.addSuccess'));
      }
      if (isContinueFlag.value) {
        resetForm();
      } else {
        await sleep(300);
        if (props.mode === 'organization') {
          router.push({ name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT, query: route.query });
        } else {
          router.push({ name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT, query: route.query });
        }

        setIsSave(true);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  const refStepMap: Record<string, any> = {};

  function setStepRefMap(el: refItem, formItem: DefinedFieldItem) {
    if (el) {
      refStepMap[`${formItem.id}`] = el;
    }
  }

  // 保存
  function saveHandler(isContinue = false) {
    isContinueFlag.value = isContinue;
    formRef.value?.validate().then(async (res) => {
      if (!res) {
        return save();
      }
      return scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
    });
  }

  // 处理表单数据格式
  const getFieldOptionList = () => {
    totalTemplateField.value = getTotalFieldOptionList(totalTemplateField.value as DefinedFieldItem[]);
    if (!isEdit.value) {
      selectData.value = totalTemplateField.value.filter((item) => item.internal);
    }
  };

  // 获取字段列表数据
  const getClassifyField = async () => {
    try {
      totalTemplateField.value = await templateApiMaps[props.mode].fieldList({
        scopedId: props.mode === 'organization' ? currentOrgId.value : currentProjectId.value,
        scene: route.query.type,
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  watchEffect(async () => {
    if (isEdit.value && route.params.mode === 'copy') {
      title.value = t('system.orgTemplate.copyTemplate');
    } else if (isEdit.value) {
      title.value = t('system.orgTemplate.editTemplateType', {
        type: getTemplateName('organization', route.query.type as string),
      });
    } else {
      title.value = t('system.orgTemplate.createTemplateType', {
        type: getTemplateName('organization', route.query.type as string),
      });
    }
  });

  const showFieldDrawer = ref<boolean>(false);

  function createField() {
    showFieldDrawer.value = true;
  }

  const showDrawer = ref<boolean>(false);

  function associatedField() {
    showDrawer.value = true;
  }

  const selectFiled = ref<DefinedFieldItem[]>([]);

  // 编辑更新已选择字段
  const isEditField = ref<boolean>(false);

  // 添加字段或编辑字段
  const updateFieldHandler = async (editFlag: boolean, fieldId: string) => {
    selectFiled.value = selectData.value;
    isEditField.value = editFlag;
    await getClassifyField();
    totalTemplateField.value = getTotalFieldOptionList(totalTemplateField.value as DefinedFieldItem[]);
    // 编辑字段
    if (isEditField.value) {
      const index = selectData.value.findIndex((e: any) => e.id === fieldId);
      const newUpdateData = totalTemplateField.value.find((item: any) => item.id === fieldId);
      if (index > -1 && newUpdateData) {
        selectData.value.splice(index, 1);
        selectData.value.splice(index, 0, newUpdateData);
      }
    }
    // 新增字段
    if (!isEditField.value && fieldId) {
      const newUpdateData = totalTemplateField.value.find((item: any) => item.id === fieldId);
      if (newUpdateData) {
        selectData.value.push(newUpdateData);
      }
    }
  };

  // 删除已选择字段
  const deleteSelectedField = (record: DefinedFieldItem) => {
    selectData.value = selectData.value.filter((item) => item.id !== record.id);
  };
  const fieldDrawerRef = ref();
  // 编辑字段
  const editField = (record: DefinedFieldItem) => {
    showFieldDrawer.value = true;
    fieldDrawerRef.value.editHandler(record);
  };

  // 确定处理字段表单数据
  const confirmHandler = (dataList: DefinedFieldItem[]) => {
    const selectFieldIds = selectData.value.map((e) => e.id);
    const newData = dataList.filter((item) => !selectFieldIds.includes(item.id));
    const newIds = dataList.map((item) => item.id);
    // @desc 原先已经选择过的选项value值不能再次添加的时候置空
    const selectDataValue = selectData.value.filter((item) => newIds.includes(item.id));
    selectData.value = [...selectDataValue, ...newData];
  };

  function changeState(value: boolean | (string | number | boolean)[], formItem: DefinedFieldItem) {
    formItem.required = !!value;
  }

  function getSelectData(customFields: DefinedFieldItem[]) {
    return customFields.map((item: any) => {
      const currentFormRules = FieldTypeFormRules[item.type];
      let selectOptions: any = [];
      const multipleType = ['MULTIPLE_SELECT', 'CHECKBOX', 'MULTIPLE_MEMBER'];
      if (item.options && item.options.length) {
        selectOptions = item.options.map((optionItem: any) => {
          return {
            label: optionItem.text,
            value: optionItem.value,
          };
        });

        currentFormRules.options = selectOptions;
      }
      // 如果为成员需要默认创建人
      if (item.type === 'MEMBER' || item.type === 'MULTIPLE_MEMBER') {
        selectOptions = [
          {
            label: t('system.organization.creator'),
            value: 'CREATE_USER',
          },
        ];
        currentFormRules.options = selectOptions;
      }
      let initValue;
      if (multipleType.includes(item.type)) {
        const optionsIds = selectOptions.map((e: any) => e.value);
        initValue = optionsIds.filter((e: any) => item.defaultValue.includes(e));
      } else {
        initValue = item.defaultValue;
      }
      return {
        ...item,
        id: item.fieldId,
        formRules: [
          {
            ...currentFormRules,
            title: item.fieldName,
            field: item.fieldId,
            effect: {
              required: false,
            },
            value: initValue,
            props: {
              ...currentFormRules.props,
              options: selectOptions,
              modelValue: initValue,
              placeholder: t('system.orgTemplate.defaultValue'),
            },
          },
        ],
        fApi: null,
        required: item.required,
      };
    });
  }

  // 设置系统表单默认字段
  function setCaseSystemFormField(systemFields: CustomField[]) {
    systemFields.forEach((item: CustomField) => {
      const key = item.fieldId;
      if (route.query.type === 'BUG') {
        defaultBugForm.value[key] = item.defaultValue;
      } else {
        defaultCaseForm.value[key] = item.defaultValue;
      }
    });
  }

  // 获取模板详情
  const getTemplateInfo = async () => {
    try {
      loading.value = true;
      const res = await templateApiMaps[props.mode].detail(route.query.id as string);
      const { name, customFields, systemFields } = res;
      let copyName = `copy_${name}`;
      if (copyName.length > 255) {
        copyName = copyName.slice(0, 255);
      }
      templateForm.value = {
        ...res,
        name: route.params.mode === 'copy' ? copyName : name,
      };

      if (route.params.mode === 'copy') {
        templateForm.value.id = undefined;
      }
      selectData.value = getSelectData(customFields);
      setCaseSystemFormField(systemFields || []);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  };

  function moveField(formItem: DefinedFieldItem, type: string) {
    const moveIndex = selectData.value.findIndex((item: any) => item.id === formItem.id);
    if (type === 'top') {
      if (moveIndex === 0) {
        return;
      }
      selectData.value.splice(moveIndex, 1);
      selectData.value.splice(moveIndex - 1, 0, formItem);
    } else {
      if (moveIndex === selectData.value.length - 1) {
        return;
      }
      selectData.value.splice(moveIndex, 1);
      selectData.value.splice(moveIndex + 1, 0, formItem);
    }
  }

  function getColor(index: number, type: string) {
    if (type === 'top' && index === 0) {
      return ['text-[rgb(var(--primary-3))]'];
    }
    if (type === 'bottom' && index === selectData.value.length - 1) {
      return ['text-[rgb(var(--primary-3))]'];
    }
  }

  onMounted(async () => {
    selectData.value = [];
    totalTemplateField.value = [];
    await getClassifyField();
    getFieldOptionList();
    if (isEdit.value) {
      getTemplateInfo();
    }
  });

  const activeIndex = ref(-1);

  async function brash() {
    activeIndex.value = -1;
    await getClassifyField();
    getFieldOptionList();
    if (isEdit.value) {
      getTemplateInfo();
    }
  }

  function activeHandler(index: number) {
    activeIndex.value = index;
  }

  function changeDrag() {
    activeIndex.value = -1;
  }

  const configOptions = ref({
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
      'validate-trigger': ['change'],
      'row-class': 'selfClass',
      'hide-asterisk': true,
      'hide-label': true,
    },
  });
</script>

<style scoped lang="less">
  .wrapper-preview {
    display: flex;
    height: 100%;
    .preview-left {
      width: 100%;
      border-right: 1px solid var(--color-text-n8);
    }
    .preview-right {
      padding-top: 8px;
      width: 428px;
      min-width: 428px;
      .customWrapper {
        position: relative;
        margin-bottom: 4px;
        border: 1px solid transparent;
        border-radius: 6px;
        @apply flex flex-col justify-between;
        .form {
          padding: 8px;
          border: 1px solid transparent;
          border-radius: 6px;
        }
        .form.activeStyle {
          border-color: rgb(var(--primary-5));
          background: var(--color-text-n9);
        }
        .action {
          position: absolute;
          top: -12px;
          right: 16px;
          z-index: 9 !important;
          background: var(--color-text-fff);
          opacity: 0;
          @apply flex items-center justify-end;
          .actionList {
            padding: 4px;
            border-radius: 4px;
            @apply flex items-center justify-center;
          }
          .required > .arco-checkbox {
            padding: 2px 4px;
            border-radius: 4px;
            box-shadow: 0 4px 10px -1px rgba(100 100 102/ 15%);
          }
        }
        &:hover {
          border: 1px solid var(--color-text-n8);
          background: var(--color-text-n9);
        }
        &:hover > .action {
          opacity: 1;
        }
        &:hover > .action > .actionList {
          color: rgb(var(--primary-5));
          box-shadow: 0 4px 10px -1px rgba(100 100 102/ 15%);
        }
      }
    }
  }
  .hoverStyle {
    .customWrapper:hover > .form {
      border-color: var(--color-text-n8) !important;
    }
  }
  .activeStyle {
    .customWrapper:hover .form {
      border-color: rgb(var(--primary-5));
    }
  }
  :deep(.selfClass) {
    margin-bottom: 0;
  }
  .ghost {
    border: 1px solid rgba(var(--primary-5));
    background-color: var(--color-text-n9);
  }
  :deep(.apiFieldIdClass) {
    margin-bottom: 0;
  }
  :deep(.systemFieldWrapper) {
    .arco-form-item {
      margin-bottom: 0;
    }

    padding: 8px;
    border: 1px solid transparent;
    border-radius: 6px;
    &:hover {
      border: 1px solid var(--color-text-n8);
      background: var(--color-text-n9);
    }
  }
  .tagWrapper {
    .arco-form-item {
      margin-bottom: 0;
    }

    padding: 8px;
    border: 1px solid transparent;
    border-radius: 6px;
    &:hover {
      border: 1px solid var(--color-text-n8);
      background: var(--color-text-n9);
    }
  }
  :deep(.arco-form-item-layout-vertical > .arco-form-item-label-col) {
    overflow-wrap: break-word;
  }
  :deep(.arco-form-item-content) {
    overflow-wrap: anywhere;
  }
  :deep(.label-validate-star) {
    .arco-form-item-label::after {
      display: inline-block;
      margin-top: -3px;
      content: '';
      width: 7px;
      height: 7px;
      vertical-align: middle;
      background: url('@/assets/svg/icons/validateStar.svg') center/cover;
    }
  }
</style>
