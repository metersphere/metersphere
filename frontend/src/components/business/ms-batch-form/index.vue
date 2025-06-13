<template>
  <a-form ref="formRef" :model="form" layout="vertical">
    <div
      class="mb-[16px] overflow-y-auto rounded-[4px] border border-[var(--color-text-n8)] p-[12px]"
      :style="{ width: props.formWidth || '100%' }"
    >
      <a-scrollbar class="overflow-y-auto" :style="{ 'max-height': props.maxHeight }">
        <VueDraggable
          v-model="form.list"
          ghost-class="ghost"
          drag-class="dragChosenClass"
          :disabled="!props.isShowDrag"
          :force-fallback="true"
          :animation="150"
          handle=".dragIcon"
        >
          <div
            v-for="(element, index) in form.list"
            :key="`${element.field}${index}`"
            class="draggableElement gap-[8px] py-[6px] pr-[8px]"
            :class="[props.isShowDrag ? 'cursor-move' : '']"
          >
            <div v-if="props.isShowDrag" class="dragIcon ml-[8px] mr-[8px] pt-[8px]">
              <MsIcon type="icon-icon_drag" class="block text-[16px] text-[var(--color-text-4)]" />
            </div>
            <a-form-item
              v-for="model of props.models"
              :key="`${model.field}${index}`"
              :field="`list[${index}].${model.field}`"
              :class="index > 0 ? 'hidden-item' : 'mb-0 flex-1'"
              :rules="
                model.rules?.map((e) => {
                  if (e.notRepeat === true) {
                    return {
                      validator: (val, callback) => fieldNotRepeat(val, callback, index, model.field, e.message),
                    };
                  }
                  return e;
                })
              "
              asterisk-position="end"
              :hide-asterisk="model.hideAsterisk"
              :hide-label="model.hideLabel"
              :content-flex="model.type !== 'multiple'"
              :merge-props="model.type !== 'multiple'"
            >
              <template #label>
                <div class="inline-flex flex-row">
                  <div>{{ index === 0 && model.label ? t(model.label) : '' }}</div>
                  <div v-if="model.hasRedStar" class="ml-[2px] flex items-center">
                    <svg-icon
                      width="6px"
                      height="22px"
                      name="form-star"
                      class="text-[12px] font-medium text-[rgb(var(--danger-6))]"
                    />
                  </div>
                </div>
              </template>
              <a-input
                v-if="model.type === 'input'"
                v-model:model-value="element[model.field]"
                class="flex-1"
                :placeholder="t(model.placeholder || '')"
                :max-length="model.maxLength || 255"
                :disabled="model.disabled"
                allow-clear
                @change="emit('change')"
              />
              <MsQuickInput
                v-else-if="model.type === 'textarea'"
                v-model:model-value="element[model.field]"
                class="flex-1"
                type="textarea"
                :max-length="model.maxLength"
                :disabled="model.disabled"
                @change="
                  () => {
                    formRef?.validateField(`list[${index}].${model.field}`);
                    emit('change');
                  }
                "
              >
              </MsQuickInput>
              <a-tooltip v-else-if="model.type === 'inputNumber'" position="tl" mini :disabled="!model.tooltip">
                <a-input-number
                  v-if="model.type === 'inputNumber'"
                  v-model:model-value="element[model.field]"
                  class="flex-1"
                  :placeholder="t(model.placeholder || '')"
                  :min="model.min || element[model?.minKey || 'min']"
                  :max="model.max || element[model?.maxKey || 'max'] || 9999999"
                  model-event="input"
                  :precision="getNumberPrecision(model, element)"
                  allow-clear
                  :disabled="model.disabled"
                  @change="emit('change')"
                />
                <template #content>
                  <div>
                    {{ model?.tooltip }}
                    <span class="ml-2 inline-block cursor-pointer text-[rgb(var(--primary-4))]" @click="goTry">
                      {{ t('system.authorized.applyTrial') }}
                    </span>
                  </div>
                </template>
              </a-tooltip>
              <MsTagsInput
                v-else-if="model.type === 'tagInput'"
                v-model:model-value="element[model.field]"
                class="flex-1"
                :placeholder="t(model.placeholder || 'common.tagPlaceholder')"
                allow-clear
                unique-value
                retain-input-value
                :max-tag-count="2"
                :disabled="model.disabled"
                @change="emit('change')"
              />
              <a-select
                v-else-if="model.type === 'select'"
                v-model="element[model.field]"
                class="flex-1"
                :placeholder="t(model.placeholder || '')"
                :options="model.options"
                :disabled="model.disabled"
                :field-names="model.filedNames"
                @change="emit('change')"
              />
              <div v-else-if="model.type === 'multiple'" class="flex flex-row gap-[4px]">
                <a-form-item
                  v-for="(child, childIndex) in model.children"
                  :key="`${child.field}${childIndex}${index}`"
                  :field="`list[${index}].${child.field}`"
                  :label="child.label ? t(child.label) : ''"
                  asterisk-position="end"
                  :hide-asterisk="child.hideAsterisk"
                  :hide-label="child.hideLabel"
                  class="hidden-item"
                  :rules="child.rules"
                >
                  <a-input
                    v-if="child.type === 'input'"
                    v-model="element[child.field]"
                    :class="child.className"
                    :placeholder="t(child.placeholder || '')"
                    :max-length="child.maxLength || 255"
                    allow-clear
                    :disabled="child.disabled"
                    @change="emit('change')"
                  />
                  <a-select
                    v-else-if="child.type === 'select'"
                    v-model="element[child.field]"
                    :class="child.className"
                    :placeholder="t(child.placeholder || '')"
                    :options="child.options"
                    :disabled="child.disabled"
                    :field-names="child.filedNames"
                    @change="emit('change')"
                  />
                  <MsQuickInput
                    v-else-if="child.type === 'textarea'"
                    v-model="element[child.field]"
                    type="textarea"
                    :class="child.className"
                    :disabled="child.disabled"
                    :placeholder="t(child.placeholder || '')"
                    :max-length="child.maxLength || 255"
                    :title="child.title"
                    allow-clear
                    @change="
                      () => {
                        formRef?.validateField(`list[${index}].${child.field}`);
                        emit('change');
                      }
                    "
                  />
                </a-form-item>
              </div>
            </a-form-item>
            <div v-if="showEnable">
              <a-switch
                v-model="element.enable"
                class="mt-[8px]"
                :style="{ 'margin-top': index === 0 && !props.isShowDrag ? '36px' : '' }"
                size="small"
                :type="props.enableType"
                @change="emit('change')"
              />
            </div>
            <div
              v-if="!props.hideAdd"
              v-show="form.list.length > 1"
              class="minus"
              :class="[
                'flex',
                'h-[32px]',
                'w-[32px]',
                'cursor-pointer',
                'items-center',
                'justify-center',
                'text-[var(--color-text-4)]',
                'hover:bg-[var(--color-text-n9)]',
                'rounded',
              ]"
              :style="{ 'margin-top': index === 0 && !props.isShowDrag ? '30px' : '' }"
              @click="removeField(index)"
            >
              <icon-minus-circle />
            </div>
          </div>
        </VueDraggable>
      </a-scrollbar>
      <div v-if="props.formMode === 'create' && !props.hideAdd" class="w-full">
        <a-tooltip position="tl" mini :disabled="!props.addToolTip">
          <a-button class="px-0" type="text" @click="addField">
            <template #icon>
              <icon-plus class="text-[14px]" />
            </template>
            {{ t(props.addText || 'common.add') }}
          </a-button>
          <template #content>
            <div>
              {{ props.addToolTip }}
              <span class="ml-2 inline-block cursor-pointer text-[rgb(var(--primary-4))]" @click="goTry">
                {{ t('system.authorized.applyTrial') }}
              </span>
            </div>
          </template>
        </a-tooltip>
      </div>
    </div>
  </a-form>
</template>

<script setup lang="ts">
  import { VueDraggable } from 'vue-draggable-plus';

  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsQuickInput from '@/components/business/ms-quick-input/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { scrollIntoView } from '@/utils/dom';

  import type { FormItemModel, FormMode } from './types';
  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';
  import { FieldData } from '@arco-design/web-vue/es/form/interface';

  export interface BatchFormProps {
    models: FormItemModel[];
    formMode: FormMode;
    addText?: string;
    maxHeight?: string;
    defaultVals?: any[]; // 当外层是编辑状态时，可传入已填充的数据
    isShowDrag?: boolean; // 是否可以拖拽
    formWidth?: string; // 自定义表单区域宽度
    showEnable?: boolean; // 是否显示启用禁用switch状态
    hideAdd?: boolean; // 是否隐藏添加按钮
    addToolTip?: string;
    enableType?: 'circle' | 'round' | 'line';
  }

  const props = withDefaults(defineProps<BatchFormProps>(), {
    maxHeight: '30vh',
    isShowDrag: false,
    hideAdd: false,
    enableType: 'line',
  });

  const emit = defineEmits(['change']);

  const { t } = useI18n();

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
      // 默认填充表单项
      let value: string | number | boolean | string[] | number[] | undefined;
      if (e.type === 'inputNumber') {
        value = undefined;
      } else if (e.type === 'tagInput') {
        value = [];
      } else {
        value = e.defaultValue;
      }
      formItem[e.field] = value;
      if (props.showEnable) {
        // 如果有开启关闭状态，将默认禁用
        formItem.enable = false;
      }
      // 默认填充表单项的子项
      e.children?.forEach((child) => {
        formItem[child.field] = child.type === 'inputNumber' ? null : child.defaultValue;
      });
    });
    form.value.list = [{ ...formItem }];
    if (props.defaultVals?.length) {
      // 取出defaultVals的表单 field
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
    // 遍历其他同 field 名的输入框的值，检查是否与当前输入框的值重复
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
    const item = [{ ...formItem }];
    item[0].type = [];
    formValidate(() => {
      form.value.list.push(item[0]); // 序号自增，不会因为删除而重复
    }, false);
  }

  /**
   * 移除表单项
   * @param i 表单项对应 list 的下标
   */
  function removeField(i: number) {
    form.value.list.splice(i, 1);
  }

  function resetForm() {
    formRef.value?.resetFields();
  }

  function setFields(data: Record<string, FieldData>) {
    formRef.value?.setFields(data);
  }

  function goTry() {
    window.open('https://jinshuju.net/f/CzzAOe', '_blank');
  }

  function getNumberPrecision(model: FormItemModel, element: FormItemModel) {
    if (model?.precision) return model.precision;
    if (model?.getPrecisionFun && typeof model.getPrecisionFun === 'function') {
      return model.getPrecisionFun(model, element);
    }
    return undefined;
  }

  defineExpose({
    formValidate,
    getFormResult,
    resetForm,
    setFields,
  });
</script>

<style lang="less" scoped>
  .draggableElement {
    @apply flex w-full items-start justify-between rounded;
  }
  .ghost {
    border: 1px dashed rgba(var(--primary-5));
    background-color: rgba(var(--primary-1));
    @apply rounded;
  }
  .dragChosenClass {
    background: var(--color-text-fff);
    opacity: 1 !important;
    @apply rounded;
    .minus {
      margin: 0 !important;
    }
  }
</style>
