<template>
  <a-form ref="createFormRef" :model="scenario" layout="vertical">
    <a-form-item
      field="name"
      :label="t('apiScenario.name')"
      class="mb-[16px]"
      :rules="[{ required: true, message: t('apiScenario.nameRequired') }]"
    >
      <a-input
        v-model:model-value="scenario.name"
        :max-length="255"
        :placeholder="t('apiScenario.namePlaceholder')"
        allow-clear
        @input="() => emit('change')"
      />
    </a-form-item>
    <a-form-item :label="t('apiScenario.belongModule')" class="mb-[16px]">
      <a-tree-select
        v-model:modelValue="scenario.moduleId"
        :data="props.moduleTree"
        :field-names="{ title: 'name', key: 'id', children: 'children' }"
        :tree-props="{
          virtualListProps: {
            height: 200,
            threshold: 200,
          },
        }"
        :filter-tree-node="filterTreeNode"
        allow-search
        @change="() => emit('change')"
      >
        <template #tree-slot-title="node">
          <a-tooltip :content="`${node.name}`" position="tl">
            <div class="one-line-text w-[300px]">{{ node.name }}</div>
          </a-tooltip>
        </template>
      </a-tree-select>
    </a-form-item>
    <a-form-item :label="t('apiScenario.scenarioLevel')">
      <a-select
        v-model:model-value="scenario.priority"
        :placeholder="t('common.pleaseSelect')"
        @change="() => emit('change')"
      >
        <template #label>
          <span class="text-[var(--color-text-2)]"> <caseLevel :case-level="scenario.priority" /></span>
        </template>
        <a-option v-for="item of casePriorityOptions" :key="item.value" :value="item.value">
          <caseLevel :case-level="item.label as CaseLevel" />
        </a-option>
      </a-select>
    </a-form-item>
    <a-form-item :label="t('apiScenario.status')" class="mb-[16px]">
      <a-select
        v-model:model-value="scenario.status"
        :placeholder="t('common.pleaseSelect')"
        class="param-input w-full"
        @change="() => emit('change')"
      >
        <template #label>
          <apiStatus :status="scenario.status" />
        </template>
        <a-option v-for="item of Object.values(ApiScenarioStatus)" :key="item" :value="item">
          <apiStatus :status="item" />
        </a-option>
      </a-select>
    </a-form-item>
    <a-form-item :label="t('common.tag')" class="mb-[16px]">
      <MsTagsInput v-model:model-value="scenario.tags" @change="() => emit('change')" />
    </a-form-item>
    <a-form-item :label="t('common.desc')" class="mb-[16px]">
      <a-textarea
        v-model:model-value="scenario.description"
        :max-length="500"
        :placeholder="t('apiScenario.descPlaceholder')"
        @input="() => emit('change')"
      />
    </a-form-item>
    <template v-if="props.isEdit">
      <a-form-item field="createUserName" :label="t('apiScenario.table.columns.createUser')" class="mb-[16px]">
        <a-input :model-value="(scenario as ScenarioDetail).createUserName" disabled />
      </a-form-item>
      <a-form-item field="createTime" :label="t('apiScenario.table.columns.createTime')" class="mb-[16px]">
        <a-input :model-value="dayjs((scenario as ScenarioDetail).createTime).format('YYYY-MM-DD HH:mm:ss')" disabled />
      </a-form-item>
      <a-form-item field="updateTime" :label="t('apiScenario.table.columns.updateTime')" class="mb-[16px]">
        <a-input :model-value="dayjs((scenario as ScenarioDetail).updateTime).format('YYYY-MM-DD HH:mm:ss')" disabled />
      </a-form-item>
    </template>
  </a-form>
</template>

<script setup lang="ts">
  import { FormInstance } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { filterTreeNode } from '@/utils';

  import { Scenario, ScenarioDetail } from '@/models/apiTest/scenario';
  import { ModuleTreeNode } from '@/models/common';
  import { ApiScenarioStatus } from '@/enums/apiEnum';

  import { casePriorityOptions } from '@/views/api-test/components/config';

  const props = defineProps<{
    moduleTree: ModuleTreeNode[]; // 模块树
    isEdit?: boolean;
  }>();
  const emit = defineEmits(['change']);

  const scenario = defineModel<ScenarioDetail | Scenario>('scenario', {
    required: true,
  });

  const { t } = useI18n();

  const createFormRef = ref<FormInstance>();

  defineExpose({
    createFormRef,
  });
</script>
