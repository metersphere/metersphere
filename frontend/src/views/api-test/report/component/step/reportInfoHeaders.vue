<template>
  <div class="mb-[16px] flex items-center justify-between">
    <div class="flex items-center">
      <div class="mr-2 font-medium">{{ t('report.detail.api.reportDetail') }}</div>
      <a-radio-group v-if="!props.isExport" v-model:model-value="innerActiveTab" type="button" size="small">
        <a-radio v-for="item of methods" :key="item.value" :value="item.value">
          {{ t(item.label) }}
        </a-radio>
      </a-radio-group>
    </div>
    <div v-if="!props.isExport" class="grid grid-cols-2">
      <a-input-search
        v-model:model-value="innerKeywordName"
        :placeholder="t('report.detail.api.placeHolderName')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
        @clear="reset"
      />
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
        <template #label="{ data }">
          <a-tooltip
            :content="`${cascaderOptions.find((item: any) => innerKeyword.includes(item.value))?.label} / ${data.label}`"
          >
            <div class="one-line-text inline-flex w-full items-center justify-between pr-[8px]" title="">
              {{ cascaderOptions.find((item: any) => innerKeyword.includes(item.value))?.label }} / {{ data.label }}
            </div>
          </a-tooltip>
        </template>
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
    keywordName: string;
    showType: 'API' | 'CASE';
    isExport?: boolean; // 是否是导出pdf预览
  }>();

  const emit = defineEmits(['update:activeTab', 'update:keyword', 'update:keywordName', 'search', 'reset']);

  const innerActiveTab = useVModel(props, 'activeTab', emit);
  const innerKeyword = useVModel(props, 'keyword', emit);
  const innerKeywordName = useVModel(props, 'keywordName', emit);

  function searchList() {
    emit('search');
  }

  function reset() {
    emit('reset');
  }

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
      label: t(`common.pass`),
    },
    {
      value: `${key}-FAKE_ERROR`,
      label: t(`common.fakeError`),
    },
    {
      value: `${key}-ERROR`,
      label: t(`common.fail`),
    },
    {
      value: `${key}-PENDING`,
      label: t(`common.unExecute`),
    },
    {
      value: `${key}-scriptIdentifier`,
      label: t(`report.detail.api.scriptError`),
    },
  ];
  const cascaderOptions = ref<CascaderOption[]>([
    {
      value: ScenarioStepType.API_SCENARIO,
      label: t('report.detail.api.step'),
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
