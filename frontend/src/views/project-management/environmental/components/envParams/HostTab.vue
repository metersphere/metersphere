<template>
  <div>
    <div class="flex flex-row items-center gap-[8px]">
      <a-switch v-model:model-value="currentList.enable" type="line" size="small" />
      <div class="text-[var(--color-text-1)]">{{ t('project.environmental.host.config') }}</div>
    </div>
    <!-- <a-radio-group v-model:model-value="addType" class="mt-[16px]" size="small" type="button">
      <a-radio value="single">单个添加</a-radio>
      <a-radio value="multiple">批量添加</a-radio>
    </a-radio-group> -->
    <div v-show="addType === 'single'" class="mt-[8px]">
      <MsBatchForm
        ref="batchFormRef"
        :models="batchFormModels"
        :form-mode="ruleFormMode"
        add-text="common.add"
        :default-vals="currentList.hosts"
        :show-enable="false"
        :is-show-drag="false"
      ></MsBatchForm>
    </div>
    <!-- <div v-show="addType === 'multiple'">
      <MsCodeEditor
        v-model:model-value="editorContent"
        width="100%"
        height="250px"
        theme="MS-text"
        :show-theme-change="false"
      >
        <template #leftTitle>
          <a-form-item
            :label="t('system.resourcePool.batchAddResource')"
            asterisk-position="end"
            class="hide-wrapper mb-0 w-auto"
            required
          >
          </a-form-item>
        </template>
      </MsCodeEditor>
      <div class="mb-[24px] text-[12px] leading-[16px] text-[var(--color-text-4)]">
        {{ t('system.resourcePool.nodeConfigEditorTip') }}
      </div>
    </div> -->
  </div>
</template>

<script lang="ts" setup>
  import { onClickOutside } from '@vueuse/core';
  import { debounce } from 'lodash-es';

  import MsBatchForm from '@/components/business/ms-batch-form/index.vue';
  import { FormItemModel } from '@/components/business/ms-batch-form/types';

  import { useI18n } from '@/hooks/useI18n';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  import { EnvConfigItem } from '@/models/projectManagement/environmental';

  const { t } = useI18n();

  const store = useProjectEnvStore();
  const addType = ref<string>('single');

  const currentList = computed({
    get: () => store.currentEnvDetailInfo.config.hostConfig || {},
    set: (value: EnvConfigItem) => {
      store.currentEnvDetailInfo.config.hostConfig = value || {};
    },
  });
  const batchFormRef = ref();
  onClickOutside(batchFormRef, () => {
    currentList.value.hosts = batchFormRef.value?.getFormResult();
  });
  type UserModalMode = 'create' | 'edit';
  const batchFormModels: Ref<FormItemModel[]> = ref([
    {
      field: 'ip',
      type: 'input',
      label: 'project.environmental.host.ip',
      placeholder: 'project.environmental.host.ipPlaceholder',
      rules: [{ required: true, message: t('project.environmental.host.ipIsRequire') }],
    },
    {
      field: 'domain',
      type: 'input',
      label: 'project.environmental.host.hostName',
      placeholder: 'project.environmental.host.hostNamePlaceholder',
      rules: [{ required: true, message: t('project.environmental.host.hostNameIsRequire') }],
    },
    {
      field: 'description',
      type: 'input',
      label: 'common.desc',
      placeholder: 'project.environmental.host.descPlaceholder',
    },
  ]);
  const ruleFormMode = ref<UserModalMode>('create');

  // 代码编辑器内容
  const editorContent = ref('');

  function validateForm(cb?: () => void) {
    return batchFormRef.value?.formValidate(cb);
  }

  /**
   * 解析代码编辑器内容
   */
  function analyzeCode() {
    const arr = editorContent.value.replaceAll('\r', '\n').split('\n'); // 先将回车符替换成换行符，避免粘贴的代码是以回车符分割的，然后以换行符分割
    // 将代码编辑器内写的内容抽取出来
    arr.forEach((e, i) => {
      if (e.trim() !== '') {
        // 排除空串
        const line = e.split(',');
        if (line.every((s) => s.trim() !== '')) {
          const item = {
            ip: line[0],
            domain: line[1],
          };
          if (i === 0) {
            // 第四个是concurrentNumber，需要是数字
            currentList.value = [item];
          } else {
            currentList.value.push(item);
          }
        }
      }
    });
  }

  defineExpose({
    validateForm,
  });

  watch(
    () => editorContent.value,
    (val) => {
      if (val) {
        debounce(analyzeCode, 300);
      }
    }
  );
</script>

<style lang="less" scoped></style>
