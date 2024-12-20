import { cloneDeep } from 'lodash-es';
import dayjs from 'dayjs';

import { FieldTypeFormRules } from '@/components/pure/ms-form-create/form-create';
import type { FormItemType } from '@/components/pure/ms-form-create/types';

import {
  addOrUpdateOrdField,
  addOrUpdateProjectField,
  createProjectWorkFlowStatus,
  createWorkFlowStatus,
  deleteOrdField,
  deleteOrdWorkState,
  deleteProjectField,
  deleteProjectWorkState,
  getFieldList,
  getOrdFieldDetail,
  getProjectFieldDetail,
  getProjectFieldList,
  getProjectWorkFlowList,
  getWorkFlowList,
  setOrdWorkState,
  setOrdWorkStateSort,
  setProjectWorkState,
  setProjectWorkStateSort,
  updateOrdWorkStateFlow,
  updateProjectWorkFlowStatus,
  updateProjectWorkStateFlow,
  updateWorkFlowStatus,
} from '@/api/modules/setting/template';
import { useI18n } from '@/hooks/useI18n';
import { useAppStore } from '@/store';
import useTemplateStore from '@/store/modules/setting/template';

import type { CustomField, DefinedFieldItem, fieldIconAndNameModal } from '@/models/setting/template';
import { TemplateCardEnum, TemplateDarkCardEnum, TemplateIconEnum } from '@/enums/templateEnum';

const { t } = useI18n();
const templateStore = useTemplateStore();
const appStore = useAppStore();

// 字段类型-日期
export const dateOptions: { label: string; value: FormItemType }[] = [
  {
    label: dayjs().format('YYYY-MM-DD'),
    value: 'DATE',
  },
  {
    label: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    value: 'DATETIME',
  },
];

// 字段类型- 数字
export const numberTypeOptions: { label: string; value: FormItemType }[] = [
  {
    label: '整数',
    value: 'INT',
  },
  {
    label: '保留小数',
    value: 'FLOAT',
  },
];

// 获取字段类型是数值 || 日期
export function getFieldType(selectFieldType: FormItemType): { label: string; value: FormItemType }[] {
  switch (selectFieldType) {
    case 'DATE':
      return dateOptions;
    case 'NUMBER':
      return numberTypeOptions;
    default:
      return [];
  }
}

const organizationState = computed(() => templateStore.ordStatus);
const projectState = computed(() => templateStore.projectStatus);

// 模板列表Icon
export function getCardList(type: string): Record<string, any>[] {
  const currentThemeEnum = appStore.isDarkTheme ? TemplateDarkCardEnum : TemplateCardEnum;
  const dataList = ref([
    {
      id: 1001,
      key: 'FUNCTIONAL',
      value: currentThemeEnum.FUNCTIONAL,
      name: t('system.orgTemplate.caseTemplates'),
    },
    // TODO 暂时不上
    // {
    //   id: 1002,
    //   key: 'API',
    //   value: currentThemeEnum.API,
    //   name: t('system.orgTemplate.APITemplates'),
    // },
    // {
    //   id: 1003,
    //   key: 'UI',
    //   value: currentThemeEnum.UI,
    //   name: t('system.orgTemplate.UITemplates'),
    // },
    // {
    //   id: 1004,
    //   key: 'TEST_PLAN',
    //   value: currentThemeEnum.TEST_PLAN,
    //   name: t('system.orgTemplate.testPlanTemplates'),
    // },
    {
      id: 1005,
      key: 'BUG',
      value: TemplateCardEnum.BUG,
      name: t('system.orgTemplate.defectTemplates'),
    },
  ]);
  if (type === 'organization') {
    return dataList.value.map((item) => {
      return {
        ...item,
        enable: organizationState.value[item.key],
      };
    });
  }

  return dataList.value.map((item) => {
    return {
      ...item,
      enable: projectState.value[item.key],
    };
  });
}

export function getTemplateName(type: string, scene: string) {
  const dataList = getCardList(type);
  return dataList.find((item) => item.key === scene)?.name;
}

// table名称展示图标类型表格展示类型
export const fieldIconAndName: fieldIconAndNameModal[] = [
  {
    key: 'INPUT',
    iconName: TemplateIconEnum.INPUT,
    label: t('system.orgTemplate.input'),
  },
  {
    key: 'TEXTAREA',
    iconName: TemplateIconEnum.TEXTAREA,
    label: t('system.orgTemplate.textarea'),
  },
  {
    key: 'SELECT',
    iconName: TemplateIconEnum.SELECT,
    label: t('system.orgTemplate.select'),
  },
  {
    key: 'MULTIPLE_SELECT',
    iconName: TemplateIconEnum.MULTIPLE_SELECT,
    label: t('system.orgTemplate.multipleSelect'),
  },
  {
    key: 'RADIO',
    iconName: TemplateIconEnum.RADIO,
    label: t('system.orgTemplate.radio'),
  },
  {
    key: 'CHECKBOX',
    iconName: TemplateIconEnum.CHECKBOX,
    label: t('system.orgTemplate.checkbox'),
  },
  {
    key: 'MEMBER',
    iconName: TemplateIconEnum.MEMBER,
    label: t('system.orgTemplate.member'),
  },
  {
    key: 'MULTIPLE_MEMBER',
    iconName: TemplateIconEnum.MULTIPLE_MEMBER,
    label: t('system.orgTemplate.multipleMember'),
  },
  {
    key: 'DATE',
    iconName: TemplateIconEnum.DATE,
    label: t('system.orgTemplate.date'),
  },
  {
    key: 'DATETIME',
    iconName: TemplateIconEnum.DATETIME,
    label: t('system.orgTemplate.dateTime'),
  },
  {
    key: 'NUMBER',
    iconName: TemplateIconEnum.NUMBER,
    label: t('system.orgTemplate.number'),
  },
  {
    key: 'INT',
    iconName: TemplateIconEnum.INT,
    label: t('system.orgTemplate.number'),
  },
  {
    key: 'FLOAT',
    iconName: TemplateIconEnum.FLOAT,
    label: t('system.orgTemplate.number'),
  },
  {
    key: 'MULTIPLE_INPUT',
    iconName: TemplateIconEnum.MULTIPLE_INPUT,
    label: t('system.orgTemplate.multipleInput'),
  },
  {
    key: 'SYSTEM',
    iconName: TemplateIconEnum.SYSTEM,
    label: '',
  },
];

// 获取图标类型
export const getIconType = (iconType: FormItemType) => {
  return fieldIconAndName.find((item) => item.key === iconType);
};

// 获取接口类型
export const getFieldRequestApi = (mode: 'organization' | 'project') => {
  if (mode === 'organization') {
    return {
      list: getFieldList,
      delete: deleteOrdField,
      addOrUpdate: addOrUpdateOrdField,
      detail: getOrdFieldDetail,
    };
  }
  return {
    list: getProjectFieldList,
    delete: deleteProjectField,
    addOrUpdate: addOrUpdateProjectField,
    detail: getProjectFieldDetail,
  };
};

// 获取工作流类型接口
export const getWorkFlowRequestApi = (mode: 'organization' | 'project') => {
  if (mode === 'organization') {
    return {
      list: getWorkFlowList,
      create: createWorkFlowStatus,
      update: updateWorkFlowStatus,
      delete: deleteOrdWorkState,
      changeState: setOrdWorkState,
      dragChange: setOrdWorkStateSort,
      updateFlow: updateOrdWorkStateFlow,
    };
  }
  return {
    list: getProjectWorkFlowList,
    create: createProjectWorkFlowStatus,
    update: updateProjectWorkFlowStatus,
    delete: deleteProjectWorkState,
    changeState: setProjectWorkState,
    dragChange: setProjectWorkStateSort,
    updateFlow: updateProjectWorkStateFlow,
  };
};

/** **
 * @description 处理totalData自定义字段列表格式
 * @param totalData: 自定义字段总列表
 */

export const getTotalFieldOptionList = (totalData: DefinedFieldItem[]) => {
  return totalData.map((item: any) => {
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
        {
          ...currentFormRules,
          title: item.name,
          field: item.id,
          effect: {
            required: false,
          },
          props: {
            ...currentFormRules.props,
            options: selectOptions,
            placeholder: t('system.orgTemplate.defaultValue'),
          },
        },
      ],
      fApi: null,
      required: item.internal && item.internalFieldKey === 'functional_priority',
    };
  });
};

/** **
 * @description 处理自定义字段详情展示格式
 * @param totalData: 自定义字段总列表
 * @param customFields: 自定义字段总列表
 */
export const getCustomDetailFields = (totalData: DefinedFieldItem[], customFields: CustomField[]) => {
  const customFieldsIds = customFields.map((index: any) => index.fieldId);
  return totalData
    .filter((item) => {
      const index = customFieldsIds.findIndex((it: any) => it === item.id);
      if (customFieldsIds.indexOf(item.id) > -1) {
        const currentForm = item.formRules?.map((it: any) => {
          it.props.modelValue = customFields[index].defaultValue;
          return {
            ...it,
            value: customFields[index].defaultValue,
            effect: {
              required: item.required,
            },
          };
        });
        const formItem = item;
        formItem.formRules = cloneDeep(currentForm);
        formItem.apiFieldId = customFields[index].apiFieldId;
        formItem.required = customFields[index].required;
        formItem.index = index;
        return true;
      }
      return false;
    })
    .sort((f1, f2) => f1.index - f2.index);
};

export default {};
