<template>
  <!-- TODO:第一版没有模板 -->
  <!-- <MsFormCreate v-model:api="fApi" :rule="currentApiTemplateRules" :option="options" /> -->
  <a-form ref="formRef" :model="requestVModel" layout="vertical">
    <!-- <a-form-item
      field="name"
      :label="t('apiTestManagement.apiName')"
      class="mb-[16px] w-[60%]"
      asterisk-position="end"
      :rules="[{ required: true, message: t('apiTestManagement.apiNameRequired') }]"
    >
      <a-input
        v-model:model-value="requestVModel.name"
        :max-length="255"
        :placeholder="t('apiTestManagement.apiNamePlaceholder')"
        allow-clear
        @change="handleActiveApiChange"
      />
    </a-form-item> -->
    <a-form-item asterisk-position="end" :label="t('common.desc')" class="mb-[16px] w-[60%]">
      <a-textarea v-model:model-value="requestVModel.description" :max-length="1000" @change="handleActiveApiChange" />
    </a-form-item>
    <a-form-item asterisk-position="end" :label="t('apiTestManagement.belongModule')" class="mb-[16px] w-[436px]">
      <a-tree-select
        v-model:modelValue="requestVModel.moduleId"
        :data="props.selectTree as ModuleTreeNode[]"
        :field-names="{ title: 'name', key: 'id', children: 'children' }"
        :tree-props="{
          virtualListProps: {
            height: 200,
            threshold: 200,
          },
        }"
        allow-search
        :filter-tree-node="filterTreeNode"
        @change="handleActiveApiChange"
      >
        <template #tree-slot-title="node">
          <a-tooltip :content="`${node.name}`" position="tl">
            <div class="inline-flex w-full">
              <div class="one-line-text w-[240px]">
                {{ node.name }}
              </div>
            </div>
          </a-tooltip>
        </template>
      </a-tree-select>
    </a-form-item>
    <a-form-item asterisk-position="end" :label="t('common.tag')" class="mb-[16px] w-[436px]">
      <MsTagsInput v-model:model-value="requestVModel.tags" @change="handleActiveApiChange" />
    </a-form-item>
    <a-form-item asterisk-position="end" :label="t('apiTestManagement.apiStatus')" class="mb-[16px] w-[240px]">
      <a-select
        v-model:model-value="requestVModel.status"
        :placeholder="t('common.pleaseSelect')"
        @change="handleActiveApiChange"
      >
        <template #label>
          <apiStatus :status="requestVModel.status" />
        </template>
        <a-option v-for="item of Object.values(RequestDefinitionStatus)" :key="item" :value="item">
          <apiStatus :status="item" />
        </a-option>
      </a-select>
    </a-form-item>
  </a-form>
  <!-- TODO:第一版先不做依赖 -->
  <!-- <div class="mb-[8px] flex items-center">
          <div class="text-[var(--color-text-2)]">
            {{ t('apiTestManagement.addDependency') }}
          </div>
          <a-divider margin="4px" direction="vertical" />
          <MsButton
            type="text"
            class="font-medium"
            :disabled="requestVModel.preDependency.length === 0 && requestVModel.postDependency.length === 0"
            @click="clearAllDependency"
          >
            {{ t('apiTestManagement.clearSelected') }}
          </MsButton>
        </div>
        <div class="rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]">
          <div class="flex items-center">
            <div class="flex items-center gap-[4px] text-[var(--color-text-2)]">
              {{ t('apiTestManagement.preDependency') }}
              <div class="text-[rgb(var(--primary-5))]">
                {{ requestVModel.preDependency.length }}
              </div>
              {{ t('apiTestManagement.dependencyUnit') }}
            </div>
            <a-divider margin="8px" direction="vertical" />
            <MsButton type="text" class="font-medium" @click="handleDddDependency('pre')">
              {{ t('apiTestManagement.addPreDependency') }}
            </MsButton>
          </div>
          <div class="mt-[8px] flex items-center">
            <div class="flex items-center gap-[4px] text-[var(--color-text-2)]">
              {{ t('apiTestManagement.postDependency') }}
              <div class="text-[rgb(var(--primary-5))]">
                {{ requestVModel.postDependency.length }}
              </div>
              {{ t('apiTestManagement.dependencyUnit') }}
            </div>
            <a-divider margin="8px" direction="vertical" />
            <MsButton type="text" class="font-medium" @click="handleDddDependency('post')">
              {{ t('apiTestManagement.addPostDependency') }}
            </MsButton>
          </div>
        </div> -->
</template>

<script setup lang="ts">
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { filterTreeNode } from '@/utils';

  import { ModuleTreeNode } from '@/models/common';
  import { RequestDefinitionStatus } from '@/enums/apiEnum';

  import type { FormInstance } from '@arco-design/web-vue';

  const props = defineProps<{
    selectTree?: ModuleTreeNode[];
  }>();
  const requestVModel = defineModel<RequestParam>('requestVModel', { required: true });

  const { t } = useI18n();

  const formRef = ref<FormInstance>();

  function handleActiveApiChange() {
    if (requestVModel.value) {
      requestVModel.value.unSaved = true;
    }
  }

  defineExpose({
    formRef,
  });
</script>
