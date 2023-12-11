import { FieldTypeFormRules } from '@/components/pure/ms-form-create/form-create';
import type { FormItem } from '@/components/pure/ms-form-create/types';

import { useI18n } from '@/hooks/useI18n';

const { t } = useI18n();

export function getRules(formItemList: any, key: any) {
  return formItemList.map((item: FormItem) => {
    // 当前类型
    let fieldType;
    // 从总form类型里边配置：参考form-create.ts里边配置
    const currentTypeForm = Object.keys(FieldTypeFormRules).find(
      (formItemType: any) => item.type?.toUpperCase() === formItemType
    );
    if (currentTypeForm) {
      fieldType = FieldTypeFormRules[currentTypeForm].type;
      const options = item?.options;
      const currentOptions = options?.map((optionsItem: any) => {
        return {
          label: optionsItem.text,
          value: optionsItem.value,
        };
      });
      const ruleItem = {
        type: fieldType, // 表单类型
        field: item.name, // 字段
        title: t(item.label), // label 表单标签
        value: item.value || FieldTypeFormRules[currentTypeForm].value, // 目前的值
        effect: {
          required: item.required, // 是否必填
        },
        // 级联关联到某一个form上 可能存在多个级联
        options: !item.optionMethod ? currentOptions : [],
        link: item.couplingConfig?.cascade,
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
          'keyword': '', // SearchSelect组件变化值
          'modelValue': item.value,
          'options': currentOptions, // 当前已经存在的options
          'formKey': key, // 对应pinia-form-create里边初始化的KEY
          'disabled': item?.props?.disabled,
        },
      };
      // 如果不存在关联name删除link关联属性
      if (ruleItem.link === '') {
        delete ruleItem.link;
      }
      // 如果不是等于下拉多选或者单选等
      if (ruleItem.type !== 'SearchSelect') {
        delete ruleItem.props.inputSearch;
      }
      return ruleItem;
    }
    return {};
  });
}

export default {};
