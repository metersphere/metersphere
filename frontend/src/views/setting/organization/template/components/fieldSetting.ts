import { ref } from 'vue';
import dayjs from 'dayjs';

import { useI18n } from '@/hooks/useI18n';
import useTemplateStore from '@/store/modules/setting/template';

import type { DefinedFieldItem, fieldIconAndNameModal, fieldTypes } from '@/models/setting/template';
import { TemplateCardEnum, TemplateIconEnum } from '@/enums/templateEnum';

import { FormRule } from '@form-create/arco-design';

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
export const getFieldType = (selectFieldType: fieldTypes) => {
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

// 表格名称展示图标类型表格展示类型
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
export const getIconType = (iconType: fieldTypes) => {
  return fieldIconAndName.find((item) => item.key === iconType);
};

// test 数据
export const dataTest: DefinedFieldItem[] = [
  {
    id: '100469397621047549',
    name: '优先级',
    scene: 'FUNCTIONAL',
    type: 'SELECT',
    remark: '测试优先级',
    internal: false,
    scopeType: 'ORGANIZATION',
    createTime: 1696992836000,
    updateTime: 1697507281020,
    createUser: 'Administrator',
    refId: null,
    enableOptionKey: false,
    scopeId: '100001',
    options: [
      {
        fieldId: '100469397621047549',
        value: '16975072378450000030',
        text: 'P1',
        internal: false,
      },
      {
        fieldId: '100469397621047549',
        value: '16975072378450000201',
        text: 'P2',
        internal: false,
      },
      {
        fieldId: '100469397621047549',
        value: '16975072378450000868',
        text: 'P0',
        internal: false,
      },
      {
        fieldId: '100469397621047549',
        value: '16975072378450000919',
        text: 'P3',
        internal: false,
      },
    ],
  },
  {
    id: '1065478306931084',
    name: '单选下拉编辑',
    scene: 'FUNCTIONAL',
    type: 'SELECT',
    remark: '单选下拉编辑',
    internal: false,
    scopeType: 'ORGANIZATION',
    createTime: 1697450456453,
    updateTime: 1697508687465,
    createUser: 'Administrator',
    refId: null,
    enableOptionKey: null,
    scopeId: '100001',
    options: [
      {
        fieldId: '1065478306931084',
        value: '1697508644396010',
        text: '单选下拉B',
        internal: false,
      },
      {
        fieldId: '1065478306931084',
        value: '1697508644396030',
        text: '单选下拉A',
        internal: false,
      },
    ],
  },
  {
    id: '1065478306931120',
    name: '单选A',
    scene: 'FUNCTIONAL',
    type: 'RADIO',
    remark: '单选A',
    internal: false,
    scopeType: 'ORGANIZATION',
    createTime: 1697450528311,
    updateTime: 1697506498454,
    createUser: 'Administrator',
    refId: null,
    enableOptionKey: null,
    scopeId: '100001',
    options: [
      {
        fieldId: '1065478306931120',
        value: '16975064552060000571',
        text: '单选A',
        internal: false,
      },
      {
        fieldId: '1065478306931120',
        value: '16975064552060000641',
        text: '单选B',
        internal: false,
      },
    ],
  },
  {
    id: '1065478306931156',
    name: '复选1',
    scene: 'FUNCTIONAL',
    type: 'CHECKBOX',
    remark: '复选',
    internal: false,
    scopeType: 'ORGANIZATION',
    createTime: 1697450566200,
    updateTime: 1697506471742,
    createUser: 'Administrator',
    refId: null,
    enableOptionKey: null,
    scopeId: '100001',
    options: [
      {
        fieldId: '1065478306931156',
        value: '16975064283810000060',
        text: '复选C',
        internal: false,
      },
      {
        fieldId: '1065478306931156',
        value: '16975064283810000346',
        text: '复选A',
        internal: false,
      },
      {
        fieldId: '1065478306931156',
        value: '16975064283810000453',
        text: '复选B',
        internal: false,
      },
    ],
  },
  {
    id: '1065478306931192',
    name: '多选下拉',
    scene: 'FUNCTIONAL',
    type: 'MULTIPLE_SELECT',
    remark: '多选下拉',
    internal: false,
    scopeType: 'ORGANIZATION',
    createTime: 1697450596809,
    updateTime: 1697450596809,
    createUser: 'Administrator',
    refId: null,
    enableOptionKey: null,
    scopeId: '100001',
    options: [
      {
        fieldId: '1065478306931192',
        value: '16974505540590000677',
        text: '多选下拉A',
        internal: false,
      },
      {
        fieldId: '1065478306931192',
        value: '16974505540600000368',
        text: '多选下拉C',
        internal: false,
      },
      {
        fieldId: '1065478306931192',
        value: '16974505540600000973',
        text: '多选下拉B',
        internal: false,
      },
    ],
  },
  {
    id: '1065478306931228',
    name: '多选成员',
    scene: 'FUNCTIONAL',
    type: 'MULTIPLE_MEMBER',
    remark: '多选成员',
    internal: false,
    scopeType: 'ORGANIZATION',
    createTime: 1697450624027,
    updateTime: 1697450624027,
    createUser: 'Administrator',
    refId: null,
    enableOptionKey: null,
    scopeId: '100001',
    options: [],
  },
  {
    id: '1127119677562880',
    name: '单选',
    scene: 'FUNCTIONAL',
    type: 'RADIO',
    remark: '单选',
    internal: false,
    scopeType: 'ORGANIZATION',
    createTime: 1697452682379,
    updateTime: 1697452682379,
    createUser: 'Administrator',
    refId: null,
    enableOptionKey: null,
    scopeId: '100001',
    options: [
      {
        fieldId: '1127119677562880',
        value: '16974526396070000127',
        text: 'A',
        internal: false,
      },
      {
        fieldId: '1127119677562880',
        value: '16974526396070000183',
        text: 'B',
        internal: false,
      },
    ],
  },
  {
    id: '598271764471819',
    name: '测试文本',
    scene: 'FUNCTIONAL',
    type: 'TEXTAREA',
    remark: '测试文本',
    internal: false,
    scopeType: 'ORGANIZATION',
    createTime: 1697509471301,
    updateTime: 1697509471301,
    createUser: 'Administrator',
    refId: null,
    enableOptionKey: null,
    scopeId: '100001',
    options: null,
  },
  {
    id: '755931423973702',
    name: '测试文本001',
    scene: 'FUNCTIONAL',
    type: 'TEXTAREA',
    remark: '测试文本001',
    internal: false,
    scopeType: 'ORGANIZATION',
    createTime: 1697440397074,
    updateTime: 1697440397074,
    createUser: 'Administrator',
    refId: null,
    enableOptionKey: null,
    scopeId: '100001',
    options: null,
  },
  {
    id: '755931423973776',
    name: '测试输入框',
    scene: 'FUNCTIONAL',
    type: 'INPUT',
    remark: '测试输入框',
    internal: false,
    scopeType: 'ORGANIZATION',
    createTime: 1697440515103,
    updateTime: 1697440515103,
    createUser: 'Administrator',
    refId: null,
    enableOptionKey: null,
    scopeId: '100001',
    options: null,
  },
  {
    id: '755931423973812',
    name: '测试成员',
    scene: 'FUNCTIONAL',
    type: 'MEMBER',
    remark: '测试成员',
    internal: false,
    scopeType: 'ORGANIZATION',
    createTime: 1697440538699,
    updateTime: 1697440538699,
    createUser: 'Administrator',
    refId: null,
    enableOptionKey: null,
    scopeId: '100001',
    options: [],
  },
  {
    id: '755931423973848',
    name: '测试数值整数',
    scene: 'FUNCTIONAL',
    type: 'INT',
    remark: '测试数值整数',
    internal: false,
    scopeType: 'ORGANIZATION',
    createTime: 1697440790663,
    updateTime: 1697440790663,
    createUser: 'Administrator',
    refId: null,
    enableOptionKey: null,
    scopeId: '100001',
    options: null,
  },
  {
    id: '755931423973884',
    name: '测试数值浮点数',
    scene: 'FUNCTIONAL',
    type: 'FLOAT',
    remark: '测试数值浮点数',
    internal: false,
    scopeType: 'ORGANIZATION',
    createTime: 1697440828437,
    updateTime: 1697440828437,
    createUser: 'Administrator',
    refId: null,
    enableOptionKey: null,
    scopeId: '100001',
    options: null,
  },
  {
    id: '755931423974028',
    name: '测试多值输入框001',
    scene: 'FUNCTIONAL',
    type: 'MULTIPLE_INPUT',
    remark: '测试多值输入框',
    internal: false,
    scopeType: 'ORGANIZATION',
    createTime: 1697440995732,
    updateTime: 1697440995732,
    createUser: 'Administrator',
    refId: null,
    enableOptionKey: null,
    scopeId: '100001',
    options: null,
  },
];

// 表单字段使用

export const INPUT = {
  type: 'input',
  title: '',
  field: 'fieldName',
  value: '',
  props: {
    placeholder: '请输入',
  },
};
export const SELECT = {
  type: 'select',
  field: 'fieldName',
  title: '',
  value: '',
  options: [],
  props: {
    multiple: false,
    placeholder: '请选择',
  },
};

export const MULTIPLE_SELECT = {
  type: 'select',
  field: 'fieldName',
  title: '',
  value: [],
  options: [],
  props: {
    multiple: true,
    placeholder: '请选择',
  },
};

export const RADIO = {
  type: 'radio',
  field: 'fieldName',
  title: '',
  value: '',
  options: [],
};

export const CHECKBOX = {
  type: 'checkbox',
  field: 'fieldName',
  title: '',
  value: [],
  options: [],
};

export const MEMBER = {
  type: 'select',
  field: 'fieldName',
  title: '',
  value: '',
  options: [],
  props: {
    multiple: false,
    placeholder: '请选择',
  },
};

export const MULTIPLE_MEMBER = {
  type: 'select',
  field: 'fieldName',
  title: '',
  value: '',
  options: [],
  props: {
    multiple: true,
    placeholder: '请选择',
  },
};

export const DATE = {
  type: 'DatePicker',
  field: 'fieldName',
  title: '',
  value: '',
  props: {
    'placeholder': '请选择',
    'format': 'YYYY/MM/DD',
    'show-time': false,
  },
};

export const DATETIME = {
  type: 'DatePicker',
  field: 'fieldName',
  title: '',
  value: '',
  props: {
    'placeholder': '请选择',
    'format': 'YYYY/MM/DD HH:mm:ss',
    'show-time': true,
  },
};

export const FLOAT = {
  type: 'InputNumber',
  field: 'fieldName',
  title: '',
  value: 0,
  props: {
    placeholder: '请输入',
  },
};

export const INT = {
  type: 'InputNumber',
  field: 'fieldName',
  title: '',
  value: 0,
  props: {
    precision: 0,
    placeholder: '请输入',
  },
};

export const MULTIPLE_INPUT = {
  type: 'a-input-tag',
  field: 'fieldName',
  title: '',
  value: [],
  props: {
    placeholder: '请选择',
  },
};

export const TEXTAREA = {
  type: 'a-textarea',
  field: 'fieldName',
  title: '',
  value: '',
  props: {
    'placeholder': '请输入',
    'auto-size': {
      minRows: 1,
      maxRows: 3,
    },
  },
};

export const allFieldTypeFormRules: Record<string, FormRule> = {
  INPUT,
  SELECT,
  MULTIPLE_SELECT,
  RADIO,
  CHECKBOX,
  MEMBER,
  MULTIPLE_MEMBER,
  DATE,
  DATETIME,
  INT,
  FLOAT,
  MULTIPLE_INPUT,
  TEXTAREA,
};

export default {};
