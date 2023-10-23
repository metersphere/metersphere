<template>
  <div>项目版本 waiting for development </div>
  <MsFormCreate :form-rule="formRules" :form-create-key="FormCreateKeyEnum.ORGANIZE_TEMPLATE" :option="options" />
  <br />
  <br />
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsFormCreate from '@/components/pure/ms-form-create/form-create.vue';
  import type { FomItem } from '@/components/pure/ms-form-create/types';

  import { FormCreateKeyEnum } from '@/enums/formCreateEnum';

  const formRules = ref<FomItem[]>([
    {
      type: 'input',
      name: 'name',
      label: '姓名',
      value: '',
      subDesc: '请输入姓名',
      required: true,
    },

    {
      type: 'multiple_select',
      name: 'gender',
      label: '性别',
      value: [],
      subDesc: '请选择性别',
      optionMethod: 'getGenderOptions',
      inputSearch: true,
      required: true,
      // couplingConfig: [
      //   {
      //     type: 'initOptions',
      //     cascade: 'name',
      //     matchRule: 'includes',
      //   },
      // ],
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
      type: 'input',
      name: 'member',
      label: '成员',
      value: '',
      required: true,
    },
    {
      type: 'multiple_member',
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
      type: 'int',
      name: 'birthday',
      label: '出生日期',
      value: 0,
      subDesc: '请选择出生日期',
      required: true,
    },
    {
      type: 'date',
      name: 'birthday',
      label: '出生日期',
      value: '',
      subDesc: '请选择出生日期',
      required: true,
    },
    {
      type: 'multiple_member',
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
      couplingConfig: [
        {
          type: 'initOptions',
          cascade: 'member',
          matchRule: 'includes',
        },
      ],
    },
    {
      type: 'select',
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
      couplingConfig: [
        {
          type: 'initOptions',
          cascade: 'member',
          matchRule: 'includes',
        },
      ],
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
