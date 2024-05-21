<template>
  <a-modal
    v-model:visible="updateVisible"
    :mask="true"
    :mask-closable="false"
    width="680px"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
  >
    <template #title>
      <a-tooltip :content="title" position="right">
        <span>{{ t('system.plugin.updateTitle') }}</span>
        <span class="ml-1 !text-[var(--color-text-4)]"> ({{ characterLimit(title) }})</span>
      </a-tooltip>
    </template>
    <div class="form">
      <a-form ref="UpdateFormRef" :model="form" layout="vertical">
        <a-form-item field="name" :label="t('system.plugin.name')" asterisk-position="end">
          <a-input
            v-model="form.name"
            :placeholder="t('system.plugin.defaultJarNameTip')"
            :max-length="255"
            allow-clear
          />
        </a-form-item>
        <a-form-item
          :tooltip="t('system.plugin.allOrganizeTip')"
          field="global"
          :label="t('system.plugin.appOrganize')"
          asterisk-position="end"
          :row-class="form.global ? 'allOrganizeTip' : ''"
        >
          <a-radio-group v-model="form.global" type="button">
            <a-radio :value="true"
              >{{ t('system.plugin.allOrganize') }}<span class="float-right mx-1 mt-[1px]"> </span
            ></a-radio>
            <a-radio :value="false">{{ t('system.plugin.theOrganize') }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item
          v-if="!form.global"
          field="organizationIds"
          :label="t('system.plugin.selectOrganization')"
          asterisk-position="end"
          :row-class="showIncludeOrg || showRemoveOrg ? 'allOrganizeTip' : ''"
          :rules="[{ required: true, message: t('system.plugin.selectOrganizeTip') }]"
        >
          <a-select
            v-model="form.organizationIds"
            multiple
            :placeholder="t('system.plugin.selectOrganizeTip')"
            allow-clear
          >
            <a-option v-for="item of organizeList" :key="item.id" :value="item.id">{{ item.name }}</a-option>
          </a-select>
        </a-form-item>
        <div v-if="form.global" class="mb-[16px] ml-1 text-[12px] text-[rgb(var(--danger-6))]">
          {{ title }} {{ t('system.plugin.switchAllOrganizeTip') }}
        </div>
        <div v-if="showIncludeOrg" class="mb-[16px] ml-1 text-[12px] text-[rgb(var(--danger-6))]">
          {{ title }} {{ t('system.plugin.switchSectionOrganizeTip') }}
        </div>
        <div v-if="showRemoveOrg" class="mb-[16px] ml-1 text-[12px] text-[rgb(var(--danger-6))]">
          {{ t('system.plugin.changeOrganizeTip') }}
        </div>
        <a-form-item field="description" :label="t('common.desc')" asterisk-position="end">
          <a-textarea
            v-model="form.description"
            :max-length="1000"
            :placeholder="t('system.plugin.pluginDescription')"
          />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <a-button type="secondary" @click="handleCancel">{{ t('system.plugin.pluginCancel') }}</a-button>
      <a-button class="ml-[12px]" type="primary" :loading="confirmLoading" @click="handleOk">
        {{ t('system.plugin.pluginConfirm') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watch, watchEffect } from 'vue';
  import { FormInstance, Message, SelectOptionData, ValidatedError } from '@arco-design/web-vue';
  import { isEqual } from 'lodash-es';

  import { updatePlugin } from '@/api/modules/setting/pluginManger';
  import { useI18n } from '@/hooks/useI18n';
  import { characterLimit } from '@/utils';

  import type { PluginItem, UpdatePluginModel } from '@/models/setting/plugin';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    organizeList: SelectOptionData;
  }>();
  const emits = defineEmits<{
    (e: 'success'): void;
    (e: 'update:visible', val: boolean): void;
  }>();
  const confirmLoading = ref<boolean>(false);
  const UpdateFormRef = ref<FormInstance | null>(null);
  const title = ref<string>('');
  const form = ref<UpdatePluginModel>({
    name: '',
    global: '',
    organizationIds: [],
    description: '',
  });

  const updateVisible = ref<boolean>(false);
  watchEffect(() => {
    updateVisible.value = props.visible;
  });
  watch(
    () => updateVisible.value,
    (val) => {
      emits('update:visible', val);
    }
  );
  const handleCancel = () => {
    UpdateFormRef.value?.resetFields();
    updateVisible.value = false;
  };
  const originOrgIds = ref<string[]>([]);
  const open = (record: PluginItem) => {
    title.value = record.name as string;
    originOrgIds.value = (record.organizations || []).map((item) => item.id);
    form.value = {
      ...record,
      organizationIds: (record.organizations || []).map((item) => item.id),
    };
  };

  const handleOk = () => {
    UpdateFormRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        confirmLoading.value = true;
        try {
          const { id, name, organizationIds, global, description, fileName } = form.value;
          const params = {
            id,
            name: name || fileName,
            organizationIds,
            global,
            description,
          };
          await updatePlugin(params);
          Message.success(t('system.plugin.updateSuccessTip'));
          handleCancel();
          emits('success');
        } catch (error) {
          console.log(error);
        } finally {
          confirmLoading.value = false;
        }
      } else {
        return false;
      }
    });
  };

  const showIncludeOrg = computed(() => {
    const { organizationIds } = form.value;
    if (!form.value.global && !isEqual(organizationIds, originOrgIds.value)) {
      return originOrgIds.value?.every((item) => organizationIds?.includes(item));
    }
    return false;
  });

  const showRemoveOrg = computed(() => {
    const { organizationIds } = form.value;
    if (!form.value.global && !isEqual(originOrgIds.value, organizationIds)) {
      return originOrgIds.value?.some((item) => !organizationIds?.includes(item));
    }
    return false;
  });

  defineExpose({
    open,
    UpdateFormRef,
  });
</script>

<style scoped lang="less">
  .allOrganizeTip {
    margin-bottom: 8px;
  }
</style>
