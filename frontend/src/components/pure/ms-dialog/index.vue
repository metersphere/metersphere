<template>
  <a-modal
    v-model:visible="dialogVisible"
    title-align="start"
    :class="['ms-modal-form', `ms-modal-${props.dialogSize}`]"
    v-bind="attrs"
    :footer="props.showfooter"
    :mask-closable="false"
    @close="handleCancel"
  >
    <template #title>
      {{ t(props.title) }}
    </template>
    <slot></slot>
    <!-- 自定义footer -->
    <slot name="self-footer"></slot>
    <!-- 默认footer -->
    <template #footer>
      <div class="flex" :class="[props.showSwitch ? 'justify-between' : 'justify-end']">
        <div v-if="props.showSwitch" class="flex flex-row items-center justify-center">
          <a-switch v-model="switchEnable" class="mr-1" size="small" />
          <a-tooltip v-if="props.showSwitchTooltip" :content="t(props.showSwitchTooltip)">
            <span class="flex items-center"
              ><span class="mr-2">{{ props.switchName }}</span>
              <span><svg-icon width="16px" height="16px" :name="'infotip'" /></span
            ></span>
          </a-tooltip>
        </div>
        <div class="flex justify-end">
          <a-space>
            <a-button v-if="showCancel" type="secondary" @click="handleCancel">{{
              props.cancelText ? t(props.cancelText) : $t('ms.dialog.cancel')
            }}</a-button>
            <!-- 自定义确认与取消之间其他按钮可以直接使用loading按钮插槽 -->
            <slot name="self-button"></slot>
            <a-button type="primary" :loading="props.loading" :disabled="props.disabledOk" @click="confirmHandler">
              {{ props.okText ? t(props.okText) : t('ms.dialog.ok') }}
            </a-button>
          </a-space>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, useAttrs, watch } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import { FormInstance } from '@arco-design/web-vue/es/form';

  const { t } = useI18n();

  export type buttontype = 'text' | 'dashed' | 'outline' | 'primary' | 'secondary' | undefined;
  export type SizeType = 'medium' | 'large' | 'small';
  export type DialogType = Partial<{
    dialogSize: SizeType; // 弹窗的宽度尺寸 medium large small
    showfooter: boolean; // 是否展示footer
    title: string; // 标题
    showCancel: boolean; // 是否显示取消按钮
    okText: string; // 确定按钮的文本
    cancelText: string; // 取消按钮的文本
    showSwitchTooltip: string; // 展示开关提示信息描述
    showSwitch: boolean; // 是否展示开关
    visible: boolean;
    confirm: (enable: boolean | undefined) => void; // 确定
    formRef: FormInstance | null; // 表单ref传入校验
    disabledOk: boolean; // 确认按钮禁用
    close: () => void;
    enable: boolean | undefined; // 开关禁用
    switchName: string; // 开关后边的名称
    loading: boolean;
  }>;

  const props = withDefaults(defineProps<DialogType>(), {
    showfooter: true,
    showSwitch: false,
    showCancel: true,
    title: '',
    disabledOk: false,
    close: Function,
    enable: undefined,
  });
  const emits = defineEmits<{
    (event: 'close'): void;
    (event: 'update:visible', visible: boolean): void;
    (event: 'update:enable', enable: boolean): void;
  }>();

  const attrs = useAttrs();

  // 单独的开关
  const switchEnable = ref<boolean>(false);
  const dialogVisible = ref<boolean>(props.visible);

  watch(
    () => props.visible,
    (val) => {
      dialogVisible.value = val;
    }
  );

  watch(
    () => props.enable,
    (val) => {
      switchEnable.value = val;
    }
  );

  watch(
    () => switchEnable.value,
    (val) => {
      emits('update:enable', val);
    }
  );

  const handleCancel = () => {
    dialogVisible.value = false;
    emits('update:visible', false);
    props?.close();
  };

  const confirmHandler = async () => {
    if (props.confirm) {
      props.confirm(switchEnable.value);
    }
  };
</script>

<style scoped></style>
