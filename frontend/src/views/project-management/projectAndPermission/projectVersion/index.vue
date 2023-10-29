<template>
  <div>项目版本 waiting for development </div>
  <MsFormCreate v-model:form-rule="formRules" :form-create-key="FormCreateKeyEnum.ORGANIZE_TEMPLATE" />
  <br />
  <br />
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsFormCreate from '@/components/pure/ms-form-create/form-create.vue';
  import type { FormItem } from '@/components/pure/ms-form-create/types';

  import { FormCreateKeyEnum } from '@/enums/formCreateEnum';

  const formRules = ref<FormItem[]>([
    {
      type: 'SELECT',
      name: 'name',
      label: '姓名',
      value: '',
      subDesc: '请输入姓名',
      required: true,
      options: [
        {
          value: '1001',
          text: '单选',
        },
        {
          value: '1002',
          text: '多选',
        },
      ],
    },
    {
      type: 'MULTIPLE_SELECT',
      name: 'gender',
      label: '性别',
      value: [],
      subDesc: '请选择性别',
      optionMethod: 'getGenderOptions',
      inputSearch: true,
      required: true,
      couplingConfig: {
        type: 'initOptions',
        cascade: 'name',
        matchRule: 'includes',
      },

      options: [
        {
          value: '1',
          text: '单选',
        },
        {
          value: '2',
          text: '多选',
        },
      ],
    },
    {
      type: 'TEXTAREA',
      name: 'member',
      label: '成员',
      value: '',
      required: true,
    },
    {
      type: 'MULTIPLE_MEMBER',
      name: 'multiple_member',
      label: '多选成员',
      value: [],
      required: true,
      options: [
        {
          value: '1',
          text: '单选',
        },
        {
          value: '2',
          text: '多选',
        },
      ],
    },
    {
      type: 'INT',
      name: 'birthday1',
      label: '出生日期',
      value: 0,
      subDesc: '请选择出生日期',
      required: true,
    },
    {
      type: 'DATE',
      name: 'birthday',
      label: '出生日期',
      value: '',
      subDesc: '请选择出生日期',
      required: true,
    },
    {
      type: 'MULTIPLE_MEMBER',
      name: 'radio',
      label: '单选',
      value: '',
      subDesc: '请选择出生日期',
      inputSearch: true,
      required: true,
      options: [
        {
          value: '1',
          text: '单选',
        },
        {
          value: '2',
          text: '多选',
        },
      ],
      couplingConfig: {
        type: 'initOptions',
        cascade: 'member',
        matchRule: 'includes',
      },
    },
    {
      type: 'SELECT',
      name: 'selectName',
      label: '单选',
      value: '',
      subDesc: '请选择出生日期',
      inputSearch: true,
      required: true,
      options: [
        {
          value: '1',
          text: '单选',
        },
        {
          value: '2',
          text: '多选',
        },
      ],
      couplingConfig: {
        type: 'initOptions',
        cascade: 'member',
        matchRule: 'includes',
      },
    },
  ]);

  const options = ref({
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
    },
  });
</script>

<style scoped></style>
