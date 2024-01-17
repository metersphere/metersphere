<template>
  <div class="ms-assertion">
    <a-dropdown trigger="hover" @select="handleSelect">
      <a-button class="w-[84px]" type="outline">
        <div class="flex flex-row items-center gap-[8px]">
          <icon-plus />
          <span>{{ t('ms.assertion.button') }}</span>
        </div>
      </a-button>
      <template #content>
        <a-doption v-for="item in assertOption" :key="item.value" :value="item.value">{{ item.label }}</a-doption>
      </template>
    </a-dropdown>
    <div v-if="showBody" class="ms-assertion-body">
      <article class="ms-assertion-body-left">
        <div
          v-for="(item, index) in activeOption"
          :key="item.value"
          class="ms-assertion-body-left-item"
          @click="handleItemClick(item)"
        >
          <div class="ms-assertion-body-left-item-row">
            <span class="ms-assertion-body-left-item-row-num">{{ index + 1 }}</span>
            <span class="ms-assertion-body-left-item-row-title">{{ item.label }}</span>
          </div>
          <div class="ms-assertion-body-left-item-switch">
            <a-switch type="line" size="small" />
          </div>
        </div>
      </article>
      <section class="ms-assertion-body-right">
        <MsAssertionStatusCodeTab
          v-if="activeKey === 'statusCode'"
          v-model:selectValue="codeTabState.selectValue"
          v-model:statusCode="codeTabState.statusCode"
        />
        <ResponseHeaderTab v-if="activeKey === 'responseHeader'" />
      </section>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import ResponseHeaderTab from './comp/ResponseHeaderTab.vue';
  import MsAssertionStatusCodeTab from './comp/StatusCodeTab.vue';

  import { useI18n } from '@/hooks/useI18n';

  defineOptions({
    name: 'MsAssertion',
  });

  const { t } = useI18n();

  const codeTabState = reactive({
    selectValue: '',
    statusCode: 200,
  });
  // 源选项
  const assertOptionSource = [
    {
      label: t('ms.assertion.statusCode'),
      value: 'statusCode',
    },
    {
      label: t('ms.assertion.responseHeader'),
      value: 'responseHeader',
    },
    {
      label: t('ms.assertion.responseBody'),
      value: 'responseBody',
    },
    {
      label: t('ms.assertion.responseTime'),
      value: 'responseTime',
    },
    {
      label: t('ms.assertion.param'),
      value: 'param',
    },
    {
      label: t('ms.assertion.script'),
      value: 'script',
    },
  ];
  // 选中的选项
  const selectIds = ref<string[]>([]);
  // Item点击的key
  const activeKey = ref<string>('');

  // 未选中的选项
  const assertOption = computed(() => {
    return assertOptionSource.filter((item) => !selectIds.value.includes(item.value));
  });
  // 选中的选项
  const activeOption = computed(() => {
    return assertOptionSource.filter((item) => selectIds.value.includes(item.value));
  });
  // 是否显示主体
  const showBody = computed(() => {
    return selectIds.value.length > 0;
  });
  // dropdown选择
  const handleSelect = (value: string | number | Record<string, any> | undefined) => {
    selectIds.value.push(value as string);
  };
  // item点击
  const handleItemClick = (item: { label: string; value: string }) => {
    activeKey.value = item.value;
  };
</script>

<style lang="less" scoped>
  .ms-assertion {
    width: 100%;
    &-body {
      display: flex;
      margin-top: 8px;
      flex-flow: row nowrap;
      gap: 8px;
      &-left {
        display: flex;
        padding: 12px;
        width: 216px;
        min-width: 216px;
        height: calc(100vh - 394px);
        background-color: var(--color-text-n9);
        flex-direction: column;
        gap: 4px;
        &-item {
          display: flex;
          flex-flow: row nowrap;
          justify-content: space-between;
          align-items: center;
          padding: 4px 8px;
          border-radius: 4px;
          background-color: var(--color-text-fff);
          cursor: pointer;
          &-row {
            display: flex;
            flex-direction: row;
            align-items: center;
            gap: 4px;
            &-num {
              width: 16px;
              height: 16px;
              font-size: 12px;
              font-weight: 500;
              border-radius: 50%;
              text-align: center;
              color: var(--color-text-4);
              background-color: var(--color-text-n8);
              line-height: 16px;
            }
            &-title {
              font-size: 12px;
              font-weight: 400;
              color: var(--color-text-1);
              line-height: 22px;
            }
          }
        }
      }
      &-right {
        display: flex;
        flex-grow: 1;
        padding: 16px;
        border: 1px solid var(--color-text-n8);
        border-radius: 4px;
        background: var(--color-text-fff);
      }
    }
  }
</style>
