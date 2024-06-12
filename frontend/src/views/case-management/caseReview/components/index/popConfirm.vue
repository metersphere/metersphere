<template>
  <a-popconfirm
    v-model:popup-visible="innerVisible"
    class="ms-pop-confirm--hidden-icon"
    position="bottom"
    :ok-loading="loading"
    :cancel-button-props="{ disabled: loading }"
    :on-before-ok="beforeConfirm"
    :popup-container="props.popupContainer || 'body'"
    @popup-visible-change="reset"
  >
    <template #content>
      <div class="mb-[8px] font-medium">
        {{
          props.title ||
          (props.mode === 'add' ? t('project.fileManagement.addSubModule') : t('project.fileManagement.rename'))
        }}
      </div>
      <a-form ref="formRef" :model="form" layout="vertical">
        <a-form-item
          class="hidden-item"
          field="field"
          :rules="[{ required: true, message: t('project.fileManagement.nameNotNull') }, { validator: validateName }]"
        >
          <a-textarea
            v-if="props.fieldConfig?.isTextArea"
            v-model:model-value="form.field"
            :max-length="props.fieldConfig?.maxLength || 1000"
            :auto-size="{ maxRows: 4 }"
            :placeholder="props.fieldConfig?.placeholder || t('project.fileManagement.namePlaceholder')"
            class="w-[245px]"
            @press-enter="beforeConfirm(undefined)"
          >
          </a-textarea>
          <a-input
            v-else
            v-model:model-value="form.field"
            :max-length="props.fieldConfig?.maxLength || 255"
            :placeholder="props.fieldConfig?.placeholder || t('project.fileManagement.namePlaceholder')"
            class="w-[245px]"
            @press-enter="beforeConfirm(undefined)"
          />
        </a-form-item>
      </a-form>
    </template>
    <slot></slot>
  </a-popconfirm>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import { addReviewModule, updateReviewModule } from '@/api/modules/case-management/caseReview';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { FieldRule, FormInstance } from '@arco-design/web-vue';

  interface FieldConfig {
    field?: string;
    rules?: FieldRule[];
    placeholder?: string;
    maxLength?: number;
    isTextArea?: boolean;
  }

  const props = defineProps<{
    mode: 'add' | 'rename' | 'fileRename' | 'fileUpdateDesc' | 'repositoryRename';
    visible?: boolean;
    title?: string;
    allNames: string[];
    popupContainer?: string;
    fieldConfig?: FieldConfig;
    parentId?: string; // 父节点 id
    nodeId?: string; // 节点 id
  }>();

  const emit = defineEmits(['update:visible', 'close', 'addFinish', 'renameFinish', 'updateDescFinish']);

  const appStore = useAppStore();
  const { t } = useI18n();

  const innerVisible = ref(props.visible || false);
  const form = ref({
    field: props.fieldConfig?.field || '',
  });
  const formRef = ref<FormInstance>();
  const loading = ref(false);

  watch(
    () => props.fieldConfig?.field,
    (val) => {
      form.value.field = val || '';
    },
    {
      deep: true,
    }
  );

  watch(
    () => props.visible,
    (val) => {
      innerVisible.value = val;
    }
  );

  watch(
    () => innerVisible.value,
    (val) => {
      if (!val) {
        emit('close');
      }
      emit('update:visible', val);
    }
  );

  function reset(val: boolean) {
    if (!val) {
      form.value.field = '';
      formRef.value?.resetFields();
    }
  }

  function beforeConfirm(done?: (closed: boolean) => void) {
    if (loading.value) return;
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          loading.value = true;
          if (props.mode === 'add') {
            // 添加根级模块
            await addReviewModule({
              projectId: appStore.currentProjectId,
              parentId: props.parentId || '',
              name: form.value.field,
            });
            Message.success(t('project.fileManagement.addSubModuleSuccess'));
            reset(false);
            emit('addFinish', form.value.field);
          } else if (props.mode === 'rename') {
            // 模块重命名
            await updateReviewModule({
              id: props.nodeId || '',
              name: form.value.field,
            });
            Message.success(t('project.fileManagement.renameSuccess'));
            reset(false);
            emit('renameFinish', form.value.field);
          }
          if (done) {
            done(true);
          } else {
            innerVisible.value = false;
          }
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
          if (done) {
            done(false);
          }
        } finally {
          loading.value = false;
        }
      } else if (done) {
        done(false);
      }
    });
  }

  function validateName(value: any, callback: (error?: string | undefined) => void) {
    if (props.allNames.includes(value)) {
      callback(t('project.fileManagement.nameExist'));
    }
  }
</script>

<style lang="less" scoped></style>
