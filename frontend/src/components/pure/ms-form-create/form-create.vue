<template>
  <FormCreate v-model:api="formApi" :rule="formRuleList" :option="props.options || option"></FormCreate>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { debounce } from 'lodash-es';

  import JiraKey from './comp/jiraKey.vue';
  import PassWord from './formcreate-password.vue';
  import SearchSelect from './searchSelect.vue';

  import useFormCreateStore from '@/store/modules/form-create/form-create';

  import { FormCreateKeyEnum } from '@/enums/formCreateEnum';

  import type { FormItem } from './types';
  import { FormRuleItem } from './types';
  import formCreate from '@form-create/arco-design';

  const formCreateStore = useFormCreateStore();

  formCreate.component('PassWord', PassWord);
  formCreate.component('SearchSelect', SearchSelect);
  formCreate.component('JiraKey', JiraKey);

  const FormCreate = formCreate.$form();
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
  // 处理配置项
  const props = defineProps<{
    options?: any; // 自定义配置
    formRule: FormItem[]; // 表单的规则
    formCreateKey: FormCreateKeyEnum[keyof FormCreateKeyEnum]; // 唯一表单Key
  }>();

  const emit = defineEmits(['update:form-rule']);

  const formApi = ref<any>({});

  // 计算被级联的项
  const cascadeItem = computed(() => {
    const currentFormCreateRules = formCreateStore.formCreateRuleMap.get(props.formCreateKey);
    // 获取当前列表里边所有包含cascade的item
    if (currentFormCreateRules) {
      const cascade = currentFormCreateRules
        .map((item: Record<string, any>) => item.link)
        .filter((item) => item)
        .flatMap((flatItem: any) => flatItem);
      // 给所有的link上边关联的某个item 进行绑定监视
      return currentFormCreateRules.filter((item: Record<string, any>) => {
        return cascade.indexOf(item.field) > -1;
      });
    }
  });
  // 计算远程检索的表单项
  const getOptionsRequest = debounce((val: FormRuleItem) => {
    // 获取当前变化的一项 监视到被级联的表单项
    // 从所有的列表项里边获取所有的link到的那一项
    const totalFormList = formCreateStore.formCreateRuleMap.get(props.formCreateKey);
    if (totalFormList) {
      const resultItem = totalFormList.find(
        (item) => item.link && (item.link as string[]).indexOf(val.field as string) > -1
      );
      if (resultItem) formCreateStore.getOptions(val, props.formCreateKey, resultItem, formApi.value);
    }
  }, 300);

  watch(
    cascadeItem,
    (val) => {
      // 监视当前改变请求获取当前方法下边的options 和获取多有的字段值
      if (val) {
        val.forEach(async (item) => {
          if (item.value) {
            await getOptionsRequest(item);
          }
        });
      }
    },
    { deep: true, immediate: false }
  );

  const formRuleList = ref<FormRuleItem[]>([]); // 存放转换格式后
  const formRules = ref<FormItem[]>([]);
  watch(
    () => props.formRule,
    (val) => {
      formRules.value = props.formRule;
      formCreateStore.setInitFormCreate(props.formCreateKey, props.formRule);
      formCreateStore.initFormCreateFormRules(props.formCreateKey);
      formRuleList.value = formCreateStore.formCreateRuleMap.get(props.formCreateKey) as FormRuleItem[];
    },
    { deep: true, immediate: true }
  );

  const formData = computed(() => {
    return formCreateStore.formRuleMap.get(props.formCreateKey);
  });

  watch(
    () => formData.value,
    (val) => {
      formRuleList.value = formCreateStore.formCreateRuleMap.get(props.formCreateKey) as FormRuleItem[];
    }
  );

  watch(
    () => formRuleList.value,
    () => {
      // 处理数据格式更新
      const result = formRuleList.value.map((item: any) => {
        const type = props.formRule.find((it) => it.name === item.field)?.type;
        const formItemRule = {
          name: item.field,
          type,
          label: item.title,
          value: item.value,
          required: item?.effect?.required,
          inputSearch: item.props.inputSearch || false,
          instructionsIcon: item.props.instructionsIcon || '',
          optionMethod: item.props.optionMethod || '',
          couplingConfig: {
            ...item.props.couplingConfig,
          },
        };
        return formItemRule;
      });
      formCreateStore.setInitFormCreate(props.formCreateKey, result as FormItem[]);
    },
    { deep: true }
  );

  defineExpose({
    formApi, // 对外暴漏用于表单校验和清除校验状态 具体参考form-create文档API
  });
</script>

<style scoped></style>
