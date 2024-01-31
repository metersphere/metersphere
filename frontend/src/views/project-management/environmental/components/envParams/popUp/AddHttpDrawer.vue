<template>
  <MsDrawer :width="891" :visible="visible" unmount-on-close :mask="false" @cancel="emit('close')">
    <template #title>
      <div>{{ title }}</div>
    </template>
    <a-form ref="httpRef" layout="vertical" :model="form">
      <a-form-item
        class="mb-[16px]"
        asterisk-position="end"
        field="hostname"
        :label="t('project.environmental.http.hostName')"
        :rules="[{ required: true, message: t('project.environmental.http.hostNameRequired') }]"
      >
        <a-input
          v-model="form.hostname"
          class="w-[100%]"
          :max-length="255"
          :placeholder="t('project.environmental.http.hostNamePlaceholder')"
        >
          <template #prefix>
            <div class="input-prefix"> http:// </div>
          </template>
        </a-input>
      </a-form-item>
      <a-form-item class="mb-[16px]" field="applyModule" :label="t('project.environmental.http.applyModule')">
        <a-checkbox-group v-model="form.applyModule">
          <a-checkbox value="apiTest">{{ t('menu.apiTest') }}</a-checkbox>
          <a-checkbox value="uiTest">{{ t('menu.uiTest') }}</a-checkbox>
        </a-checkbox-group>
      </a-form-item>
      <a-form-item class="mb-[16px]" field="enableCondition" :label="t('project.environmental.http.enableCondition')">
        <a-select v-model:model-value="form.enableCondition">
          <a-option value="none">{{ t('project.environmental.http.none') }}</a-option>
          <a-option value="module">{{ t('project.environmental.http.module') }}</a-option>
          <a-option value="path">{{ t('project.environmental.http.path') }}</a-option>
        </a-select>
      </a-form-item>
      <!-- 接口模块选择 -->
      <a-form-item
        v-if="showApiModule"
        class="mb-[16px]"
        field="apiModule"
        asterisk-position="end"
        :label="t('project.environmental.http.apiModuleSelect')"
        :rules="[{ required: true, message: t('project.environmental.http.hostNameRequired') }]"
      >
        <a-select v-model:model-value="form.apiModule" multiple :placeholder="t('common.pleaseSelect')">
          <a-option value="none">{{ t('project.environmental.http.none') }}</a-option>
        </a-select>
      </a-form-item>
      <!-- 选择UI测试模块 -->
      <a-form-item
        v-if="showUIModule"
        class="mb-[16px]"
        field="enableCondition"
        asterisk-position="end"
        :label="t('project.environmental.http.uiModuleSelect')"
        :rules="[{ required: true, message: t('project.environmental.http.hostNameRequired') }]"
      >
        <a-select v-model:model-value="form.uiModule" multiple :placeholder="t('common.pleaseSelect')">
          <a-option value="none">{{ t('project.environmental.http.none') }}</a-option>
        </a-select>
      </a-form-item>
      <!-- 路径 -->
      <a-form-item
        v-if="showPathInput"
        class="path-input mb-[16px]"
        asterisk-position="end"
        field="hostname"
        :label="t('project.environmental.http.path')"
        :rules="[{ required: true, message: t('project.environmental.http.pathRequired') }]"
      >
        <a-input
          v-model="form.hostname"
          class="w-[100%]"
          :max-length="255"
          :placeholder="t('project.environmental.http.pathPlaceholder')"
        >
          <template #prefix>
            <div class="input-prefix">
              <a-select>
                <a-option v-for="item in OPERATOR_MAP.string" :key="item.value" :value="item.value">{{
                  t(item.label)
                }}</a-option>
              </a-select>
            </div>
          </template>
        </a-input>
      </a-form-item>
    </a-form>
    <RequestHeader :params="headerParams" />
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { defineModel } from 'vue';

  import { OPERATOR_MAP } from '@/components/pure/ms-advance-filter/index';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import RequestHeader from '../../requestHeader/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    currentId?: string;
  }>();

  const emit = defineEmits<{
    (e: 'close'): void;
  }>();

  const form = reactive<{
    hostname: string;
    applyModule: string[];
    enableCondition: string;
    apiModule: string[];
    uiModule: string[];
    path: string;
    operator: string;
  }>({
    hostname: '',
    applyModule: [],
    enableCondition: 'none',
    apiModule: [],
    uiModule: [],
    path: '',
    operator: '',
  });

  const httpRef = ref();

  const showApiModule = computed(() => form.enableCondition === 'module' && form.applyModule.includes('apiTest'));
  const showUIModule = computed(() => form.enableCondition === 'module' && form.applyModule.includes('uiTest'));
  const showPathInput = computed(() => form.enableCondition === 'path');
  const headerParams = ref<[]>([]);

  const visible = defineModel('visible', { required: true, type: Boolean, default: false });

  const { t } = useI18n();

  const isEdit = computed(() => !!props.currentId);

  const title = computed(() => {
    return isEdit.value ? t('project.environmental.http.edit') : t('project.environmental.http.add');
  });
  watchEffect(() => {
    if (showApiModule.value) {
      form.apiModule = [];
    }
    if (showUIModule.value) {
      form.uiModule = [];
    }
  });
</script>

<style lang="less" scoped>
  .input-prefix {
    position: relative;
    right: 10px;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 5px 8px;
    border-top: 1px solid var(--color-text-7);
    border-bottom: 1px solid var(--color-text-7);
    border-left: 1px solid var(--color-text-7);
    border-radius: 4px 0 0 4px;
    background: var(--color-text-n8);
    flex-direction: column;
  }
  :deep(.arco-input-wrapper) {
    .arco-input-prefix {
      padding-right: 0;
    }
  }
  .path-input {
    display: inline;
    :deep(.arco-input-wrapper) {
      padding-right: 0;
      :focus-within {
        border-color: rgb(var(--primary-7));
        border-top: none;
        border-bottom: none;
        border-left: none;
      }
      .arco-input.arco-input-size-small {
        padding: 0;
      }
    }
    :deep(.arco-input-wrapper:not(:disabled):hover) {
      border-color: rgb(var(--primary-7));
      background-color: var(--color-text-n10);
    }
    :deep(.arco-select) {
      border-top: 1px solid var(--color-text-n7);
      border-right: 1px solid var(--color-text-n7);
      border-bottom: 1px solid var(--color-text-n7);
      border-radius: 0 4px 4px 0;
      background: var(--color-text-n8);
    }
    :deep(.arco-select-focused) {
      border-color: rgb(var(--primary-7));
    }
    :deep(.arco-select-view-single) {
      padding: 5px 8px;
      .arco-select-view-value {
        padding-top: 0;
        padding-bottom: 0;
        height: 22px;
        min-height: 22px;
        line-height: 22px;
      }
    }
  }
</style>
