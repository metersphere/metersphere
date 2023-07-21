<template>
  <a-form ref="formRef" :model="form" layout="vertical">
    <div class="mb-[16px] overflow-y-auto rounded-[4px] bg-[var(--color-fill-1)] p-[12px]">
      <a-scrollbar class="overflow-y-auto" :style="{ 'max-height': props.maxHeight }">
        <div class="flex flex-wrap items-start justify-between gap-[8px]">
          <template v-for="(item, i) of form.list" :key="`form-item-${i}`">
            <div class="flex w-full items-start justify-between gap-[8px]">
              <a-form-item
                v-for="model of props.models"
                :key="`${model.filed}${i}`"
                :field="`list[${i}].${model.filed}`"
                :class="i > 0 ? 'hidden-item' : 'mb-0 flex-1'"
                :label="i === 0 && model.label ? t(model.label) : ''"
                :rules="
                  model.rules?.map((e) => {
                    if (e.notRepeat === true) {
                      return {
                        validator: (val, callback) => fieldNotRepeat(val, callback, i, model.filed, e.message),
                      };
                    }
                    return e;
                  })
                "
                asterisk-position="end"
              >
                <a-input
                  v-if="model.type === 'input'"
                  v-model="item[model.filed]"
                  class="mb-[4px] flex-1"
                  :placeholder="t(model.placeholder || '')"
                  :max-length="model.maxLength || 250"
                  allow-clear
                />
                <a-input-number
                  v-if="model.type === 'inputNumber'"
                  v-model="item[model.filed]"
                  class="mb-[4px] flex-1"
                  :placeholder="t(model.placeholder || '')"
                  :min="model.min"
                  :max="model.max || 9999999"
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
                @click="removeField(i)"
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
  import { ref, watchEffect, unref } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import { scrollIntoView } from '@/utils/dom';

  import type { ValidatedError, FormInstance } from '@arco-design/web-vue';
  import type { FormItemModel, FormMode } from './types';

  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      models: FormItemModel[];
      formMode: FormMode;
      addText: string;
      maxHeight?: string;
      defaultVals?: any[]; // 当外层是编辑状态时，可传入已填充的数据
    }>(),
    {
      maxHeight: '30vh',
    }
  );

  const defaultForm = {
    list: [] as Record<string, any>[],
  };
  const form = ref<Record<string, any>>({ list: [...defaultForm.list] });
  const formRef = ref<FormInstance | null>(null);
  const formItem: Record<string, any> = {};

  /**
   * 监测defaultVals和models的变化
   * 初始化时通过models创建初始化表单
   * 若defaultVals变化，则说明当前是填充模式，将清空之前的表单项，填充传入的数据（一般是表单编辑的时候）
   */
  watchEffect(() => {
    props.models.forEach((e) => {
      formItem[e.filed] = e.type === 'inputNumber' ? null : '';
    });
    form.value.list = [{ ...formItem }];
    if (props.defaultVals?.length) {
      // 取出defaultVals的表单 filed
      form.value.list = props.defaultVals.map((e) => e);
    }
  });

  function getFormResult() {
    return unref<Record<string, any>[]>(form.value.list);
  }

  function fieldNotRepeat(
    value: string | undefined,
    callback: (error?: string) => void,
    index: number,
    field: string,
    msg?: string
  ) {
    if (value === '' || value === undefined) return;
    // 遍历其他同 feild 名的输入框的值，检查是否与当前输入框的值重复
    for (let i = 0; i < form.value.list.length; i++) {
      if (i !== index && form.value.list[i][field].trim() === value) {
        callback(t(msg || ''));
        return;
      }
    }
  }

  /**
   * 触发表单校验
   * @param cb 校验通过后执行回调
   * @param isSubmit 是否需要将表单值拼接后传入回调函数
   */
  function formValidate(cb: (res?: Record<string, any>[]) => void, isSubmit = true) {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
        return;
      }
      if (typeof cb === 'function') {
        if (isSubmit) {
          cb(getFormResult());
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
      form.value.list.push({ ...formItem }); // 序号自增，不会因为删除而重复
    }, false);
  }

  /**
   * 移除表单项
   * @param i 表单项对应 list 的下标
   */
  function removeField(i: number) {
    form.value.list.splice(i, 1);
  }

  defineExpose({
    formValidate,
    getFormResult,
  });
</script>

<style lang="less" scoped></style>
