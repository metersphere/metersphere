<template>
  <div class="mb-4 flex h-[36px] items-center justify-between">
    <div class="flex items-center">
      <div class="mr-2 font-medium leading-[36px]">{{ t('report.detail.api.reportDetail') }}</div>
      <a-radio-group v-model:model-value="innerActiveTab" type="button" size="small">
        <a-radio v-for="item of methods" :key="item.value" :value="item.value">
          {{ t(item.label) }}
        </a-radio>
      </a-radio-group>
    </div>
    <div class="w-[240px]">
      <MsCascader
        v-model:model-value="innerKeyword"
        mode="native"
        option-size="small"
        class="w-full"
        :multiple="false"
        :options="filterOptions || []"
        :virtual-list-props="{ height: 200 }"
        :placeholder="t('report.detail.api.filterPlaceholder')"
      >
        <template #option="{ data }">
          <div title="">
            <a-tooltip :content="t(data.label)">
              <div class="one-line-text w-[100%]" title="">
                {{ t(data.label) }}
              </div>
            </a-tooltip>
          </div>
        </template>
      </MsCascader>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';

  import MsCascader from '@/components/business/ms-cascader/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ScenarioStepType } from '@/enums/apiEnum';

  import type { CascaderOption } from '@arco-design/web-vue';

  const { t } = useI18n();
  const props = defineProps<{
    activeTab: 'tiled' | 'tab';
    keyword: string;
    showType: 'API' | 'CASE';
  }>();

  const emit = defineEmits(['update:activeTab', 'update:keyword']);

  const innerActiveTab = useVModel(props, 'activeTab', emit);
  const innerKeyword = useVModel(props, 'keyword', emit);

  const methods = ref([
    {
      label: t('report.detail.api.tiledDisplay'),
      value: 'tiled',
    },
    {
      label: t('report.detail.api.tabDisplay'),
      value: 'tab',
    },
  ]);

  const createChildOption = (key: string) => [
    {
      value: `${key}-SUCCESS`,
      label: t(`report.detail.successCount`),
    },
    {
      value: `${key}-FAKE_ERROR`,
      label: t(`report.detail.fakeErrorCount`),
    },
    {
      value: `${key}-ERROR`,
      label: t(`report.detail.errorCount`),
    },
    {
      value: `${key}-PENDING`,
      label: t(`report.detail.pendingCount`),
    },
    {
      value: `${key}-scriptIdentifier`,
      label: t(`report.detail.api.scriptError`),
    },
  ];
  const cascaderOptions = ref<CascaderOption[]>([
    {
      value: ScenarioStepType.API_SCENARIO,
      label: t('report.detail.api.scenario'),
      children: createChildOption(ScenarioStepType.API_SCENARIO),
    },
    {
      value: 'REQUEST',
      label: t('report.detail.api.request'),
      children: createChildOption(ScenarioStepType.CUSTOM_REQUEST),
    },
  ]);
  const filterOptions = computed(() =>
    props.showType === 'API' ? cascaderOptions.value : cascaderOptions.value.slice(-1)
  );
</script>

<style scoped lang="less"></style>
