<template>
  <a-form ref="formRef" :model="form" layout="vertical">
    <div class="mb-[16px] overflow-y-auto rounded-[4px] bg-[var(--color-fill-1)] p-[12px]">
      <a-scrollbar class="overflow-y-auto" :style="{ 'max-height': props.maxHeight }">
        <div class="flex flex-wrap items-start justify-between gap-[8px]">
          <template v-for="(order, i) of form.list" :key="`form-item-${order}`">
            <div class="flex w-full items-start justify-between gap-[8px]">
              <a-form-item
                v-for="item of props.models"
                :key="`${item.filed}${order}`"
                :field="`${item.filed}${order}`"
                :class="i > 0 ? 'hidden-item' : 'mb-0 flex-1'"
                :label="i === 0 && item.label ? t(item.label) : ''"
                :rules="item.rules"
                asterisk-position="end"
              >
                <a-input
                  v-if="item.type === 'input'"
                  v-model="form[`${item.filed}${order}`]"
                  class="mb-[4px] flex-1"
                  :placeholder="t(item.placeholder || '')"
                  :max-length="item.maxLength || 250"
                  allow-clear
                />
                <a-input-number
                  v-if="item.type === 'inputNumber'"
                  v-model="form[`${item.filed}${order}`]"
                  class="mb-[4px] flex-1"
                  :placeholder="t(item.placeholder || '')"
                  :min="item.min"
                  :max="item.max || 9999999"
                  allow-clear
                />
              </a-form-item>
              <div
                v-show="form.list.length > 1"
                :class="[
                  'flex',
                  'h-full',
                  'w-[32px]',
                  'cursor-pointer',
                  'items-center',
                  'justify-center',
                  'text-[var(--color-text-brand)]',
                  i === 0 ? 'mt-[36px]' : 'mt-[5px]',
                ]"
                @click="removeField(order, i)"
              >
                <icon-minus-circle />
              </div>
            </div>
          </template>
        </div>
      </a-scrollbar>
      <div v-if="props.formMode === 'create'" class="w-full">
        <a-button class="px-0" type="text" @click="addField">
          <template #icon>
            <icon-plus class="text-[14px]" />
          </template>
          {{ t(props.addText) }}
        </a-button>
      </div>
    </div>
  </a-form>
</template>

<script setup lang="ts">
  import { ref, watchEffect } from 'vue';
  import { useI18n } from '@/hooks/useI18n';

  import type { ValidatedError, FormInstance } from '@arco-design/web-vue';
  import type { FormItemModel, FormMode, ValueType } from './types';

  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      models: FormItemModel[];
      formMode: FormMode;
      addText: string;
      maxHeight?: string;
      valueType?: ValueType;
      delimiter?: string; // 当valueType为 string 类型时的分隔符，默认为英文逗号,
      defaultVals?: Record<string, string | string[] | number[]>; // 当外层是编辑状态时，可传入已填充的数据
    }>(),
    {
      valueType: 'Array',
      delimiter: ',',
      maxHeight: '30vh',
    }
  );

  const defaultForm = {
    list: [0],
  };
  const form = ref<Record<string, any>>({ ...defaultForm });
  const formRef = ref<FormInstance | null>(null);

  /**
   * 监测defaultVals和models的变化
   * 初始化时通过models创建初始化表单
   * 若defaultVals变化，则说明当前是填充模式，将清空之前的表单项，填充传入的数据（一般是表单编辑的时候）
   */
  watchEffect(() => {
    props.models.forEach((e) => {
      form.value[`${e.filed}0`] = e.type === 'inputNumber' ? null : '';
    });
    if (props.defaultVals) {
      // 重置表单，因为组件初始化后可能输入过值或创建过表单项
      form.value = { list: [0] };
      // 取出defaultVals的表单 filed
      const arr = Object.keys(props.defaultVals);
      for (let i = 0; i < arr.length; i++) {
        const filed = arr[i];
        // 取出当前 filed 的默认值
        const dVals = props.defaultVals[filed];
        // 判断默认值为数组还是字符串，字符串需要根据传入的分隔符delimiter分割
        const vals = Array.isArray(dVals) ? dVals : dVals.split(`${props.delimiter}`);
        // 遍历当前 filed 的默认值数组，填充至表单对象
        vals.forEach((val, order) => {
          form.value[`${filed}${order}`] = val;
          if (i === 0 && order > 0) {
            // 行数只需要遍历一次字段的值数组长度即可
            form.value.list.push(order);
          }
        });
      }
    }
  });

  function getFormResult() {
    const res: Record<string, any> = {};
    props.models.forEach((e) => {
      res[e.filed] = [];
    });
    form.value.list.forEach((e: number) => {
      props.models.forEach((m) => {
        res[m.filed].push(form.value[`${m.filed}${e}`]);
      });
    });
    return res;
  }

  /**
   * 触发表单校验
   * @param cb 校验通过后执行回调
   * @param isSubmit 是否需要将表单值拼接后传入回调函数
   */
  function formValidate(cb: (res?: Record<string, string[] | string>) => void, isSubmit = true) {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      if (typeof cb === 'function') {
        if (isSubmit) {
          const res = getFormResult();
          cb(props.valueType === 'Array' ? res : res.join(','));
          return;
        }
        cb();
      }
    });
  }

  /**
   * 添加表单项
   */
  function addField() {
    formValidate(() => {
      const lastIndex = form.value.list.length - 1;
      const lastOrder = form.value.list[lastIndex] + 1;
      form.value.list.push(lastOrder); // 序号自增，不会因为删除而重复
      props.models.forEach((e) => {
        form.value[`${e.filed}${lastOrder}`] = e.type === 'inputNumber' ? null : '';
      });
    }, false);
  }

  /**
   * 移除表单项
   * @param index 表单项的序号
   * @param i 表单项对应 list 的下标
   */
  function removeField(index: number, i: number) {
    props.models.forEach((e) => {
      delete form.value[`${e.filed}${index}`];
    });
    form.value.list.splice(i, 1);
  }

  defineExpose({
    formValidate,
    getFormResult,
  });
</script>

<style lang="less" scoped></style>
