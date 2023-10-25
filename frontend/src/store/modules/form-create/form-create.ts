import { defineStore } from 'pinia';

import { FieldTypeFormRules } from '@/components/pure/ms-form-create/form-create';
import type { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';

import { FormCreateKeyEnum } from '@/enums/formCreateEnum';

const useFormCreateStore = defineStore('form-create', {
  persist: false,
  state: (): {
    formRuleMap: Map<FormCreateKeyEnum[keyof FormCreateKeyEnum], FormItem[]>;
    formCreateRuleMap: Map<FormCreateKeyEnum[keyof FormCreateKeyEnum], FormRuleItem[]>;
  } => ({
    formRuleMap: new Map(),
    formCreateRuleMap: new Map(),
  }),
  actions: {
    // 存储外边传递初始化数据格式存储form-item
    setInitFormCreate(key: FormCreateKeyEnum[keyof FormCreateKeyEnum], formRule: FormItem[]) {
      this.formRuleMap = new Map();
      this.formRuleMap.set(key, formRule);
    },
    // 根据不同的类型初始化数据
    initFormCreateFormRules(key: FormCreateKeyEnum[keyof FormCreateKeyEnum]) {
      const currentFormRule = this.formRuleMap.get(key);
      // 处理数据结构
      const result = currentFormRule?.map((item: FormItem) => {
        // 当前类型
        let fieldType;
        const currentTypeForm = Object.keys(FieldTypeFormRules).find(
          (formItemType: any) => item.type.toUpperCase() === formItemType
        );
        if (currentTypeForm) {
          fieldType = FieldTypeFormRules[currentTypeForm].type;
          const options = item?.options;
          const currentOptions = options?.map((optionsItem) => {
            return {
              label: optionsItem.text,
              value: optionsItem.value,
            };
          });
          return {
            type: fieldType, // 表单类型
            field: item.name, // 字段
            title: item.label, // label 表单标签
            value: FieldTypeFormRules[currentTypeForm].value, // 目前的值
            effect: {
              required: item.required, // 是否必填
            },
            // 级联关联到某一个form上 可能存在多个级联
            options: !item.optionMethod ? currentOptions : [],
            link: item.couplingConfig?.map((cascadeItem: any) => cascadeItem.cascade),
            rule: item.validate || [],
            // 梳理表单所需要属性
            props: {
              ...FieldTypeFormRules[currentTypeForm].props,
              'tooltip': item.tooltip,
              // 表单后边展示图片
              'instructionsIcon': item.instructionsIcon,
              // 下拉选项请求 必须是开启远程搜索才有该方法
              'subDesc': item.subDesc,
              // 级联匹配规则
              'couplingConfig': { ...item.couplingConfig },
              'optionMethod': item.inputSearch && item.optionMethod ? item.optionMethod : '',
              'inputSearch': item.inputSearch,
              'allow-search': item.inputSearch,
              'keyword': '',
              'modelValue': item.value,
              'options': currentOptions,
            },
          };
        }
        return {};
      });
      if (result && result.length) {
        this.setInitdRules(key, result as FormRuleItem[]);
      }
    },
    // 初始化好了的格式给formCreate
    setInitdRules(key: FormCreateKeyEnum[keyof FormCreateKeyEnum], result: FormRuleItem[]) {
      this.formCreateRuleMap.set(key, result);
    },

    /** **
     * @description 处理监视联动获取请求
     * @param key: 对应Map的Key
     * @param item: 当前对应关联项-请求改变options
     * @param formValueApi: 当前表单值实例可以获取表单的当前已经设置的值
     */
    async getOptions(
      val: FormRuleItem,
      key: FormCreateKeyEnum[keyof FormCreateKeyEnum],
      cascadeItem: FormRuleItem,
      formValueApi: any
    ) {
      const formValue = formValueApi.formData();
      // 设置自定义属性给到searchSelect
      const formCreateRuleArr = this.formCreateRuleMap.get(key);
      const formCreateItem = formCreateRuleArr?.find((items: FormRuleItem) => cascadeItem.field === items.field);
      if (formCreateItem) {
        formCreateItem.props.keyword = val.value;
        formCreateItem.props.formValue = formValue;
      }
    },
  },
  getters: {},
});

export default useFormCreateStore;
