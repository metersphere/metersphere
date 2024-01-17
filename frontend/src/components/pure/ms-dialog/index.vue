<template>
  <a-modal
    v-model:visible="dialogVisible"
    title-align="start"
    :class="['ms-modal-form', `ms-modal-${props.dialogSize}`]"
    v-bind="attrs"
    :footer="props.footer"
    :mask-closable="false"
    unmount-on-close
    @close="handleCancel"
  >
    <template #title>
      {{ t(props.title) }}
    </template>
    <slot></slot>
    <!-- 默认footer -->
    <template #footer>
      <slot name="footer">
        <div class="flex" :class="[props.switchProps?.showSwitch ? 'justify-between' : 'justify-end']">
          <div v-if="props.switchProps?.showSwitch" class="flex flex-row items-center justify-center">
            <a-switch v-model="switchEnable" class="mr-1" size="small" type="line" />
            <a-tooltip v-if="props.switchProps?.switchTooltip" :content="t(props.switchProps?.switchTooltip)">
              <span class="flex items-center">
                <span class="mr-1">{{ props.switchProps?.switchName }}</span>
                <span class="mt-[2px]">
                  <IconQuestionCircle class="h-[16px] w-[16px] text-[rgb(var(--primary-5))]" />
                </span>
              </span>
            </a-tooltip>
          </div>
          <div class="flex justify-end">
            <a-button v-if="showCancel" type="secondary" @click="handleCancel">
              {{ props.cancelText ? t(props.cancelText) : t('ms.dialog.cancel') }}
            </a-button>
            <!-- 自定义确认与取消之间其他按钮可以直接使用loading按钮插槽 -->
            <slot name="self-button"></slot>
            <a-button
              class="ml-3"
              type="primary"
              :loading="confirmLoading"
              :disabled="props.disabledOk"
              @click="confirmHandler"
            >
              {{ props.okText ? t(props.okText) : t('ms.dialog.ok') }}
            </a-button>
          </div>
        </div>
      </slot>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, useAttrs, watch, watchEffect } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  export type SizeType = 'medium' | 'large' | 'small';
  export interface SwitchProps {
    switchTooltip?: string; // 展示开关提示信息描述
    switchName?: string; // 开关后边的名称
    enable?: boolean | undefined; // 开关绑定值
    showSwitch: boolean; // 是否展示开关
  }

  export type DialogType = Partial<{
    footer: boolean; // 是否展示footer
    showCancel: boolean; // 是否显示取消按钮
    okText: string; // 确定按钮的文本
    cancelText: string; // 取消按钮的文本
    disabledOk: boolean; // 确认按钮禁用
    switchProps: SwitchProps; // 开关的相关属性 打开 showSwitch才有效
  }> & {
    dialogSize: SizeType; // 弹窗的宽度尺寸 medium large small
    title: string;
    confirm?: (enable: boolean | undefined) => Promise<any>; // 确定
    visible: boolean;
    close: () => void;
  };

  const props = withDefaults(defineProps<DialogType>(), {
    footer: true,
    showCancel: true,
    title: '',
    disabledOk: false,
    close: Function,
    confirm: undefined,
  });
  const emits = defineEmits<{
    (event: 'close'): void;
    (event: 'update:visible', visible: boolean): void;
  }>();

  const attrs = useAttrs();

  // 单独的开关
  const switchEnable = ref<boolean>(false);
  const dialogVisible = ref<boolean>(false);

  watchEffect(() => {
    dialogVisible.value = props.visible;
  });

  watch(
    () => props.visible,
    (val) => {
      dialogVisible.value = val;
    }
  );
  watch(
    () => dialogVisible.value,
    (val) => {
      emits('update:visible', val);
    }
  );

  watch(
    () => props.switchProps?.enable,
    (val) => {
      if (val) switchEnable.value = val;
    },
    { deep: true }
  );

  const handleCancel = () => {
    dialogVisible.value = false;
    emits('update:visible', false);
    props?.close();
  };

  const confirmLoading = ref<boolean>(false);

  const confirmHandler = async () => {
    if (props.confirm) {
      confirmLoading.value = true;
      try {
        await props.confirm(switchEnable.value);
        confirmLoading.value = false;
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        confirmLoading.value = false;
      }
    }
  };
</script>

<style scoped></style>
