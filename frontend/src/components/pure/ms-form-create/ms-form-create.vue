<template>
  <FormCreate
    v-model:api="fApi"
    :rule="formRuleList"
    :option="props.options || option"
    @change="changeHandler"
    @mounted="handleMounted"
  >
  </FormCreate>
</template>

<script setup lang="ts">
  /**
   * @description 用于自己扩展功能的form-create
   */
  import { ref, watch } from 'vue';
  import { useVModel } from '@vueuse/core';

  import { FieldTypeFormRules } from '@/components/pure/ms-form-create/form-create';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import JiraKey from './comp/jiraKey.vue';
  import PassWord from './formcreate-password.vue';
  import SearchSelect from './searchSelect.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { FormItem, FormRuleItem } from './types';
  import formCreate from '@form-create/arco-design';

  defineOptions({ name: 'MsFormCreate' });

  const { t } = useI18n();

  formCreate.component('PassWord', PassWord);
  formCreate.component('SearchSelect', SearchSelect);
  formCreate.component('JiraKey', JiraKey);
  formCreate.component('MsTagsInput', MsTagsInput);

  const FormCreate = formCreate.$form();
  // 处理配置项
  const props = defineProps<{
    options?: any; // 自定义配置
    formRule: FormItem[]; // 表单的规则
    formItem: FormRuleItem[]; // 处理后的表单
    api: any; // 表单对象
    disabled?: boolean; // 是否禁用
  }>();

  const emit = defineEmits(['update:api', 'update', 'update:formItem', 'change', 'mounted']);

  const fApi = computed({
    get() {
      return props.api;
    },
    set(val) {
      emit('update:api', val);
    },
  });

  const innerFormItem = useVModel(props, 'formItem', emit);

  // 规定好的字段格式
  const formItems = ref<FormItem[]>([...props.formRule]);
  // 控制器的选项
  const controlFormItemsMaps = ref<Record<string, FormItem[]>>({});
  // 级联项需要绑定change事件的选项 key对应name value 对应级联需要远程请求的那一项
  const cascadeItemsMaps = ref<Record<string, FormItem[]>>({});

  const formRuleList = ref<FormRuleItem[]>([]);

  function getUpdateFun(val: any, rule: any, api: any, key: string) {
    api.getRule(cascadeItemsMaps.value[key][0].name).value = '';
    api.getRule(cascadeItemsMaps.value[key][0].name).props.keyword = val;
    api.getRule(cascadeItemsMaps.value[key][0].name).props.modelValue = val;
    api.getRule(cascadeItemsMaps.value[key][0].name).props.formValue = api.formData();
  }

  // 处理级联项添加change事件
  function initCascadeItemsMaps() {
    const cascadeItemName = formItems.value
      .filter((item) => item.couplingConfig && item.couplingConfig.cascade)
      .map((item) => item.couplingConfig && item.couplingConfig.cascade);
    // 将级联项单独映射
    formItems.value.forEach((it: FormItem) => {
      const cascadeName = it?.couplingConfig?.cascade;
      if (cascadeName && cascadeItemName.indexOf(cascadeName) > -1) {
        if (!cascadeItemsMaps.value[cascadeName]) {
          cascadeItemsMaps.value[cascadeName] = [it];
        } else {
          cascadeItemsMaps.value[cascadeName].push(it);
        }
      }
    });
    // 级联改变项添加update事件
    formItems.value = formItems.value.map((item: FormItem) => {
      if (cascadeItemName.indexOf(item.name) > -1) {
        return {
          ...item,
          update(val: any, rule: any, api: any) {
            getUpdateFun(val, rule, api, item.name);
          },
        };
      }
      return {
        ...item,
      };
    });
  }

  // 处理控制器项
  function getControlFormItemsMaps() {
    const parentControlsItems: FormItem[] = [];
    formItems.value.forEach((item) => {
      if (!item.displayConditions) {
        parentControlsItems.push(item);
      } else {
        const displayConditions = item?.displayConditions;
        if (controlFormItemsMaps.value[displayConditions.field]) {
          controlFormItemsMaps.value[displayConditions.field].push(item);
        } else {
          controlFormItemsMaps.value[displayConditions.field] = [item];
        }
      }
    });
  }

  // 将传进来的数据进行map映射
  function getFormCreateItem() {
    formItems.value = formItems.value.map((item: FormItem) => {
      if (controlFormItemsMaps.value[item.name]) {
        // 处理控制器
        const controlArr = controlFormItemsMaps.value[item.name];
        const conTrolMap: Record<string, FormItem[]> = {};
        controlArr.forEach((controlled: FormItem) => {
          if (conTrolMap[controlled.displayConditions?.value]) {
            conTrolMap[controlled.displayConditions?.value].push(controlled);
          } else {
            conTrolMap[controlled.displayConditions?.value] = [controlled];
          }
        });
        const controlList: any = [];
        Object.keys(conTrolMap).forEach((controlKeys: string) => {
          const controlItem = {
            value: controlKeys,
            rule: conTrolMap[controlKeys],
          };
          controlList.push(controlItem);
        });
        return {
          ...item,
          control: controlList,
        };
      }
      return {
        ...item,
      };
    });
    formItems.value = formItems.value.filter((item: FormItem) => !item.displayConditions?.field);
  }

  function mapOption(options: any[]) {
    return options.map((optionsItem) => {
      const mappedItem: any = {
        label: optionsItem.text,
        value: optionsItem.value,
      };

      if (optionsItem.children) {
        mappedItem.children = mapOption(optionsItem.children || []);
      }
      return mappedItem;
    });
  }

  function convertItem(item: FormItem) {
    // 当前类型
    let fieldType;
    // 从总form类型里边配置：参考form-create.ts里边配置
    const currentTypeForm = Object.keys(FieldTypeFormRules).find(
      (formItemType: any) => item.type?.toUpperCase() === formItemType
    );
    if (currentTypeForm) {
      if (currentTypeForm === 'INPUT' && item.subDesc) {
        // 如果是input类型并且有subDesc说明是JiraKey 类型
        fieldType = 'JiraKey';
      } else {
        fieldType = FieldTypeFormRules[currentTypeForm].type;
      }
      const options = Array.isArray(item?.options) ? item?.options : [];
      const currentOptions = mapOption(options);
      const ruleItem: any = {
        type: fieldType, // 表单类型
        field: item.name, // 字段
        title: t(item.label), // label 表单标签
        value: item.value || FieldTypeFormRules[currentTypeForm].value, // 目前的值
        effect: {
          required: item.required, // 是否必填
        },
        // 级联关联到某一个form上 可能存在多个级联
        options: !item.optionMethod ? currentOptions : [],
        link: [item.couplingConfig?.cascade ? item.couplingConfig?.cascade : ''],
        rule: item.validate || [],
        // 梳理表单所需要属性
        props: {
          ...FieldTypeFormRules[currentTypeForm].props,
          // 'tooltip': item.tooltip,
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
          'disabled': item?.props?.disabled,
          'type': item.control?.length && item.type === 'RADIO' ? 'button' : 'radio',
        },
        sourceType: item.type || '',
        control: [],
        update: item.update,
        wrap: {
          tooltip: item.tooltip,
        },
      };
      if (ruleItem.type === 'input') {
        // input 需要单独emit监听事件 emit:['change', 'blur'],
        ruleItem.on = {
          blur: () => {
            // 失去焦点后value值改变
            if (item.value !== fApi.value.getValue(item.name)) {
              fApi.value.validateField(item.name);
              emit('change', fApi.value.getValue(item.name), ruleItem, fApi.value);
            }
          },
        };
      }
      // 如果存在placeholder, 替换掉默认的placeholder
      if (item.platformPlaceHolder) {
        ruleItem.props.placeholder = item.platformPlaceHolder;
      }
      // 如果不存在关联name删除link关联属性
      if (!ruleItem.link.filter((ink: string) => ink).length) {
        delete ruleItem.link;
      }
      // 如果不是等于下拉多选或者单选等
      if (ruleItem.type !== 'SearchSelect') {
        delete ruleItem.props.inputSearch;
      }
      if (ruleItem.type !== 'RADIO') {
        delete ruleItem.props.type;
      }
      let controlItem;
      if (item?.control) {
        controlItem = item.control.map((control: { value: string; rule: FormItem[] }) => {
          return {
            value: control.value,
            rule: control.rule.map((itemRule: FormItem) => {
              return convertItem(itemRule);
            }),
          };
        });
        ruleItem.control = controlItem;
      }
      return ruleItem;
    }
  }

  function changeHandler(value: any, defaultValue: any, formRuleItem: FormRuleItem, api: any) {
    if (formRuleItem.type === 'input') {
      // 输入框失去焦点后再保存
      return;
    }
    fApi.value.validateField(value);
    emit('change', defaultValue, formRuleItem, api);
  }

  function getControlFormItems() {
    const convertedData = (formItems.value || []).map((item: FormItem) => convertItem(item));
    formRuleList.value = convertedData;
  }

  // 初始化表单值
  function initFormItem() {
    initCascadeItemsMaps();
    getControlFormItemsMaps();
    getFormCreateItem();
    getControlFormItems();
  }
  function setValue() {
    nextTick(() => {
      const tempObj: Record<string, any> = {};
      props.formRule.forEach((item) => {
        tempObj[item.name] = item.value;
      });

      fApi.value?.setValue({ ...tempObj });
    });
  }

  watchEffect(() => {
    formItems.value = props.formRule;
    // 初始化的时候设置一次值 放置初始上来校验不通过

    initFormItem();
    // setValue();
  });

  const option = {
    resetBtn: false, // 不展示默认配置的重置和提交
    submitBtn: false,
    on: false, // 取消绑定on事件
    form: {
      layout: 'vertical',
      labelAlign: 'left',
    },
    // 暂时默认
    row: {
      gutter: 0,
    },
    wrap: {
      'asterisk-position': 'end',
      'validate-trigger': ['change'],
    },
  };

  function handleMounted() {
    // setValue();
    if (props.disabled) {
      fApi.value.disabled(true);
    }
    emit('mounted');
  }

  watch(
    () => formRuleList.value,
    (val) => {
      if (val) {
        innerFormItem.value = val;
      }
    },
    {
      deep: true,
    }
  );

  defineExpose({
    formRuleList,
  });

  onUnmounted(() => {
    formItems.value = [];
    controlFormItemsMaps.value = {};
    cascadeItemsMaps.value = {};
  });
</script>

<style lang="less" scoped>
  :deep(.arco-form-item-label) {
    @apply flex items-center;
    span {
      @apply block overflow-hidden overflow-ellipsis whitespace-nowrap;
    }
  }
</style>
