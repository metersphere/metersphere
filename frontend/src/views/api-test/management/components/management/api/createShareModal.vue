<template>
  <a-modal
    v-model:visible="innerVisible"
    :title="props?.record?.id ? t('apiTestManagement.updateCreateShare') : t('apiTestManagement.newCreateShare')"
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
          <a-select v-model="form.apiRange" class="w-[120px]">
            <a-option v-for="item in shareTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-option>
          </a-select>
          <MsTreeSelect
            v-if="form.apiRange === 'MODULE'"
            v-model:model-value="moduleIds"
            :data="moduleTree"
            allow-clear
            :multiple="true"
            tree-check-strictly
            :tree-checkable="true"
            :placeholder="t('common.pleaseSelect')"
            :field-names="{ title: 'name', key: 'id', children: 'children' }"
          />
          <a-select v-if="['TAG', 'PATH'].includes(form.apiRange)" v-model="form.rangeMatchSymbol" class="w-[120px]">
            <a-option v-for="item in tagOperators" :key="item.value" :value="item.value">
              {{ t(item.label) }}
            </a-option>
          </a-select>
          <a-input
            v-if="form.apiRange === 'PATH'"
            v-model="form.rangeMatchVal"
            :max-length="255"
            class="flex-1"
            :placeholder="t('project.environmental.http.pathPlaceholder')"
          />
          <MsTagsInput
            v-if="form.apiRange === 'TAG'"
            v-model:model-value="tags"
            class="flex-1"
            placeholder="apiTestManagement.enterTheInputTag"
            allow-clear
            unique-value
            empty-priority-highest
          />
        </div>
      </a-form-item>
      <a-form-item field="invalidTime" :label="t('apiTestManagement.invalidTime')" asterisk-position="end">
        <a-date-picker
          v-model:model-value="form.invalidTime"
          show-time
          value-format="timestamp"
          class="w-[400px]"
          @change="(v) => setRangeValue(v)"
        />
      </a-form-item>
      <div class="mb-[16px] flex items-center">
        <a-switch v-model:model-value="form.isPrivate" class="mr-[8px]" size="small" @change="changePrivate" />
        {{ t('apiTestManagement.passwordAccess') }}
      </div>
      <a-form-item
        v-if="form.isPrivate"
        field="password"
        :label="t('apiTestManagement.effectiveTime')"
        asterisk-position="end"
        hide-asterisk
        hide-label
        :validate-trigger="['blur']"
        :rules="form.isPrivate ? [{ validator: validatePassword }] : []"
      >
        <a-input-password
          v-model:model-value="form.password"
          class="w-[240px]"
          :max-length="6"
          :placeholder="t('apiTestManagement.enterPassword')"
          autocomplete="new-password"
        />
      </a-form-item>
      <div class="mb-[16px] flex items-center">
        <a-switch v-model:model-value="form.allowExport" class="mr-[8px]" size="small" />
        {{ t('apiTestManagement.allowExport') }}
      </div>
    </a-form>
    <template #footer>
      <a-button type="secondary" :disabled="confirmLoading" @click="handleCancel">{{ t('common.cancel') }}</a-button>
      <a-button
        class="ml-[12px]"
        :disabled="isDisabledSave"
        type="primary"
        :loading="confirmLoading"
        @click="handleConfirm"
      >
        {{ okText }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, Message, SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import { CONTAINS, EQUAL } from '@/components/pure/ms-advance-filter/index';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsTreeSelect from '@/components/pure/ms-tree-select/index.vue';

  import { addShare, getEnvModules, updateShare } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore, useUserStore } from '@/store';
  import useDocShareCheckStore from '@/store/modules/api/docShareCheck';
  import { TreeNode } from '@/utils';

  import type { ShareDetail } from '@/models/apiTest/management';
  import type { ModuleTreeNode } from '@/models/common';
  import { OperatorEnum } from '@/enums/advancedFilterEnum';

  const userStore = useUserStore();

  const docCheckStore = useDocShareCheckStore();

  const { t } = useI18n();
  const appStore = useAppStore();

  const props = defineProps<{
    record?: ShareDetail;
  }>();

  const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'loadList'): void;
  }>();

  const innerVisible = defineModel<boolean>('visible', {
    required: true,
  });

  const initForm: ShareDetail = {
    name: '',
    apiRange: 'ALL',
    rangeMatchSymbol: OperatorEnum.CONTAINS,
    rangeMatchVal: '',
    invalidTime: 0,
    isPrivate: false,
    password: '',
    allowExport: false,
    projectId: '',
  };

  const tags = ref<string[]>([]);
  const moduleIds = ref<string[]>([]);

  const form = ref<ShareDetail>({ ...initForm });

  const shareTypeOptions = ref<SelectOptionData>([
    {
      label: t('apiTestManagement.allApi'),
      value: 'ALL',
    },
    {
      label: t('apiTestManagement.module'),
      value: 'MODULE',
    },
    {
      label: t('apiTestManagement.path'),
      value: 'PATH',
    },
    {
      label: t('common.tag'),
      value: 'TAG',
    },
  ]);

  const tagOperators = computed(() => {
    return form.value.apiRange === 'PATH' ? [CONTAINS, EQUAL] : [CONTAINS];
  });

  const okText = computed(() => {
    return props?.record?.id ? t('common.update') : t('common.newCreate');
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
    formRef.value?.resetFields();
    form.value = cloneDeep(initForm);
    tags.value = [];
    moduleIds.value = [];
    emit('close');
  }

  const confirmLoading = ref<boolean>(false);
  const originPassword = ref<string>('');
  function handleConfirm() {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        confirmLoading.value = true;
        try {
          const params: ShareDetail = {
            ...form.value,
            invalidTime: dayjs(form.value.invalidTime).valueOf(),
            projectId: appStore.currentProjectId,
          };
          if (form.value.apiRange === 'TAG') {
            params.rangeMatchVal = tags.value.join(',');
          }
          if (form.value.apiRange === 'MODULE') {
            params.rangeMatchVal = moduleIds.value.join(',');
          }
          if (props?.record?.id) {
            if (originPassword.value !== form.value.password) {
              // 清空校验
              if (docCheckStore.isDocVerified(props?.record?.id, userStore.id || '')) {
                docCheckStore.removeDocAsVerified(props?.record?.id, userStore.id || '');
              }
            }
            await updateShare(params);
          } else {
            await addShare(params);
          }

          emit('loadList');
          handleCancel();
          Message.success(props?.record?.id ? t('common.updateSuccess') : t('common.createSuccess'));
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          confirmLoading.value = false;
        }
      }
    });
  }

  const moduleTree = ref<TreeNode<ModuleTreeNode>[]>([]);
  async function initModuleTree() {
    try {
      const res = await getEnvModules({
        projectId: appStore.currentProjectId,
      });
      moduleTree.value = res.moduleTree;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function initDetail() {
    if (props.record?.id) {
      originPassword.value = form.value.password;
      form.value = {
        ...props.record,
      };
      const { rangeMatchVal } = form.value;
      if (form.value.apiRange === 'TAG') {
        tags.value = rangeMatchVal.split(',');
      }
      if (form.value.apiRange === 'MODULE') {
        moduleIds.value = rangeMatchVal.split(',');
      }
    }
  }

  function changePrivate() {
    form.value.password = '';
  }

  function setRangeValue(v: any) {
    if (!v) {
      form.value.invalidTime = 0;
    }
  }

  watch(
    () => innerVisible.value,
    (val) => {
      if (val) {
        initModuleTree();
        initDetail();
      }
    }
  );

  const isDisabledSave = computed(() => {
    switch (form.value.apiRange) {
      case 'MODULE':
        return !moduleIds.value.length;
      case 'PATH':
        return !form.value.rangeMatchVal.length;
      case 'TAG':
        return !tags.value.length;
      default:
        return false;
    }
  });
</script>

<style scoped></style>
