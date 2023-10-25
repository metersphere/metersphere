import dayjs from 'dayjs';

import type { FormItemType } from '@/components/pure/ms-form-create/types';

import { useI18n } from '@/hooks/useI18n';
import useTemplateStore from '@/store/modules/setting/template';

import type { fieldIconAndNameModal } from '@/models/setting/template';
import { TemplateCardEnum, TemplateIconEnum } from '@/enums/templateEnum';

const { t } = useI18n();
const templateStore = useTemplateStore();

// 字段类型-日期
const dateOptions = [
  {
    label: dayjs().format('YYYY/MM/DD'),
    value: 'DATE',
  },
  {
    label: dayjs().format('YYYY/MM/DD HH:mm:ss'),
    value: 'DATETIME',
  },
];

// 字段类型- 数字
const numberTypeOptions = [
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
export const getFieldType = (selectFieldType: FormItemType) => {
  switch (selectFieldType) {
    case 'DATE':
      return dateOptions;
    case 'NUMBER':
      return numberTypeOptions;
    default:
      break;
  }
};

// 模板列表Icon
export const cardList = [
  {
    id: 1001,
    key: 'FUNCTIONAL',
    value: TemplateCardEnum.FUNCTIONAL,
    name: t('system.orgTemplate.caseTemplates'),
    enable: templateStore.templateStatus.FUNCTIONAL,
  },
  {
    id: 1002,
    key: 'API',
    value: TemplateCardEnum.API,
    name: t('system.orgTemplate.APITemplates'),
    enable: templateStore.templateStatus.API,
  },
  {
    id: 1003,
    key: 'UI',
    value: TemplateCardEnum.UI,
    name: t('system.orgTemplate.UITemplates'),
    enable: templateStore.templateStatus.UI,
  },
  {
    id: 1004,
    key: 'TEST_PLAN',
    value: TemplateCardEnum.TEST_PLAN,
    name: t('system.orgTemplate.testPlanTemplates'),
    enable: templateStore.templateStatus.TEST_PLAN,
  },
  {
    id: 1005,
    key: 'BUG',
    value: TemplateCardEnum.BUG,
    name: t('system.orgTemplate.defectTemplates'),
    enable: templateStore.templateStatus.BUG,
  },
];

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

export default {};
