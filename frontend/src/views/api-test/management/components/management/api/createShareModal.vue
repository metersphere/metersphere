<template>
  <a-modal
    v-model:visible="innerVisible"
    :title="props.editId ? t('apiTestManagement.updateCreateShare') : t('apiTestManagement.newCreateShare')"
    title-align="start"
    class="ms-modal-form"
    :cancel-button-props="{ disabled: confirmLoading }"
    :ok-loading="confirmLoading"
    @before-ok="handleConfirm"
    @close="handleCancel"
  >
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item
        field="name"
        :label="t('common.name')"
        :rules="[{ required: true, message: t('apiTestManagement.pleaseEnterName') }]"
        asterisk-position="end"
      >
        <a-input v-model:model-value="form.name" :max-length="255" :placeholder="t('common.pleaseInput')" />
      </a-form-item>
      <a-form-item field="interfaceRange" :label="t('apiTestManagement.interfaceRange')" asterisk-position="end">
        <div class="flex w-full items-center gap-[8px]">
          <a-select v-model="form.type" class="w-[120px]">
            <a-option v-for="item in shareTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-option>
          </a-select>

          <a-tree-select
            v-if="form.type === 'module'"
            v-model:modelValue="form.moduleId"
            :data="moduleTree"
            :field-names="{ title: 'name', key: 'id', children: 'children' }"
            :tree-props="{
              virtualListProps: {
                height: 200,
                threshold: 200,
              },
            }"
            :filter-tree-node="filterTreeNode"
            allow-search
          >
            <template #tree-slot-title="node">
              <a-tooltip :content="`${node.name}`" position="tl">
                <div class="one-line-text w-[300px]">{{ node.name }}</div>
              </a-tooltip>
            </template>
          </a-tree-select>
          <a-select v-if="form.type === 'tag'" v-model="form.operator" class="w-[120px]">
            <a-option v-for="item in tagOperators" :key="item.value" :value="item.value">
              {{ t(item.label) }}
            </a-option>
          </a-select>
          <MsTagsInput
            v-if="form.type === 'tag'"
            v-model:model-value="form.tags"
            class="flex-1"
            placeholder="apiTestManagement.enterTheInputTag"
            allow-clear
            unique-value
            empty-priority-highest
            retain-input-value
          />
        </div>
      </a-form-item>
      <a-form-item field="effectiveTime" :label="t('apiTestManagement.effectiveTime')" asterisk-position="end">
        <MsTimeSelectorVue v-model="form.time" @change="handleTimeChange" />
      </a-form-item>
      <div class="mb-[16px] flex items-center">
        <a-switch v-model:model-value="form.passwordAccess" class="mr-[8px]" size="small" />
        {{ t('apiTestManagement.passwordAccess') }}
      </div>
      <a-form-item
        field="password"
        :label="t('apiTestManagement.effectiveTime')"
        asterisk-position="end"
        hide-asterisk
        hide-label
        :validate-trigger="['blur']"
        :rules="form.passwordAccess ? [{ validator: validatePassword }] : []"
      >
        <a-input
          v-model:model-value="form.password"
          class="w-[240px]"
          :max-length="6"
          :placeholder="t('apiTestManagement.enterPassword')"
        />
      </a-form-item>
      <div class="mb-[16px] flex items-center">
        <a-switch v-model:model-value="form.allowExport" class="mr-[8px]" size="small" />
        {{ t('apiTestManagement.allowExport') }}
      </div>
    </a-form>
    <template #footer>
      <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
      <a-button class="ml-[12px]" type="primary" :loading="confirmLoading" @click="handleConfirm">
        {{ okText }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, Message, SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import { CONTAINS, EQUAL } from '@/components/pure/ms-advance-filter/index';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsTimeSelectorVue from '@/components/pure/ms-time-selector/MsTimeSelector.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { filterTreeNode, TreeNode } from '@/utils';

  import type { ModuleTreeNode } from '@/models/common';
  import { OperatorEnum } from '@/enums/advancedFilterEnum';

  const { t } = useI18n();

  const props = defineProps<{
    editId?: string;
  }>();

  const emit = defineEmits<{
    (e: 'close'): void;
  }>();

  const innerVisible = defineModel<boolean>('visible', {
    required: true,
  });
  const initForm = {
    id: '',
    name: '',
    type: 'allApi',
    moduleId: '',
    operator: OperatorEnum.CONTAINS,
    tags: [],
    time: '',
    passwordAccess: false,
    password: '',
    allowExport: false,
  };

  const form = ref({ ...initForm });
  const moduleTree = ref<TreeNode<ModuleTreeNode>[]>([]);

  const shareTypeOptions = ref<SelectOptionData>([
    {
      label: t('apiTestManagement.allApi'),
      value: 'allApi',
    },
    {
      label: t('apiTestManagement.module'),
      value: 'module',
    },
    {
      label: t('apiTestManagement.path'),
      value: 'path',
    },
    {
      label: t('common.tag'),
      value: 'tag',
    },
  ]);

  const tagOperators = ref([CONTAINS, EQUAL]);

  function handleTimeChange(value: string) {
    form.value.time = value;
  }

  const okText = computed(() => {
    return props.editId ? t('common.update') : t('common.newCreate');
  });

  const validatePassword = (value: string | undefined, callback: (error?: string) => void) => {
    const sixDigitRegex = /^\d{6}$/;

    if (value === undefined || value === '') {
      callback(t('apiTestManagement.enterPassword'));
    } else if (!sixDigitRegex.test(value)) {
      callback(t('apiTestManagement.enterPassword'));
    } else {
      callback();
    }
  };

  const formRef = ref<FormInstance>();
  function handleCancel() {
    innerVisible.value = false;
    formRef.value?.resetFields();
    form.value = cloneDeep(initForm);
    emit('close');
  }

  const confirmLoading = ref<boolean>(false);

  function handleConfirm() {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        confirmLoading.value = true;
        try {
          // 等待联调
        } catch (error) {
          console.log(error);
        } finally {
          confirmLoading.value = false;
        }
      }
    });
  }
</script>

<style scoped></style>
