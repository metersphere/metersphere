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
        <a-doption v-for="item in assertOptionSource" :key="item.value" :value="item.value">{{ item.label }}</a-doption>
      </template>
    </a-dropdown>
    <div v-if="showBody" class="ms-assertion-body">
      <VueDraggable v-model="selectItems" class="ms-assertion-body-left" ghost-class="ghost" handle=".sort-handle">
        <div
          v-for="(item, index) in selectItems"
          :key="item.id"
          class="ms-assertion-body-left-item"
          :class="{
            'ms-assertion-body-left-item-active': activeKey === item.id,
            'ms-assertion-body-left-item-active-focus': focusKey === item.id,
          }"
          @click="handleItemClick(item)"
        >
          <div class="ms-assertion-body-left-item-row">
            <span class="ms-assertion-body-left-item-row-num">{{ index + 1 }}</span>
            <span class="ms-assertion-body-left-item-row-title">{{ item.label }}</span>
          </div>
          <div class="ms-assertion-body-left-item-switch">
            <div class="ms-assertion-body-left-item-switch-action">
              <MsIcon
                type="icon-icon_drag"
                class="action-btn-move sort-handle cursor-move text-[12px] text-[var(--color-text-4)]"
              />
              <MsTableMoreAction
                :list="itemMoreActions"
                trigger="click"
                @select="handleMoreActionSelect($event, item)"
                @close="focusKey = ''"
              >
                <MsButton type="icon" size="mini" class="action-btn-more">
                  <MsIcon
                    type="icon-icon_more_outlined"
                    size="14"
                    class="text-[var(--color-text-4)]"
                    @click="focusKey = item.id"
                  />
                </MsButton>
              </MsTableMoreAction>
            </div>

            <a-switch type="line" size="small" />
          </div>
        </div>
      </VueDraggable>
      <section class="ms-assertion-body-right">
        <StatusCodeTab
          v-if="valueKey === 'statusCode'"
          v-model:selectValue="codeTabState.selectValue"
          v-model:statusCode="codeTabState.statusCode"
        />
        <ResponseHeaderTab v-if="valueKey === 'responseHeader'" />
        <ResponseTimeTab v-if="valueKey === 'responseTime'" />
        <VariableTab v-if="valueKey === 'variable'" />
      </section>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { VueDraggable } from 'vue-draggable-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import ResponseHeaderTab from './comp/ResponseHeaderTab.vue';
  import ResponseTimeTab from './comp/ResponseTimeTab.vue';
  import StatusCodeTab from './comp/StatusCodeTab.vue';
  import VariableTab from './comp/VariableTab.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { MsAssertionItem } from './type';

  defineOptions({
    name: 'MsAssertion',
  });

  const { t } = useI18n();
  // 当前鼠标所在的key
  const focusKey = ref<string>('');
  // 选中的选项
  const selectItems = ref<MsAssertionItem[]>([]);
  // Item点击的key
  const activeKey = ref<string>('');
  // valueKey
  const valueKey = computed(() => {
    return activeKey.value && selectItems.value.find((item) => item.id === activeKey.value)?.value;
  });

  const codeTabState = reactive({
    selectValue: '',
    statusCode: 200,
  });
  const itemMoreActions: ActionsItem[] = [
    {
      label: 'common.copy',
      eventTag: 'copy',
    },
    {
      label: 'project.fileManagement.delete',
      eventTag: 'delete',
    },
  ];
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
      value: 'variable',
    },
    {
      label: t('ms.assertion.script'),
      value: 'script',
    },
  ];

  // 是否显示主体
  const showBody = computed(() => {
    return selectItems.value.length > 0;
  });
  // dropdown选择
  const handleSelect = (value: string | number | Record<string, any> | undefined) => {
    const tmpObj = {
      label: assertOptionSource.find((item) => item.value === value)?.label || '',
      value: value as string,
      id: new Date().getTime().toString(),
    };
    if (activeKey.value) {
      const currentIndex = selectItems.value.findIndex((item) => item.id === activeKey.value);
      const tmpArr = selectItems.value;
      tmpArr.splice(currentIndex, 0, tmpObj);
      selectItems.value = tmpArr;
    } else {
      selectItems.value.push(tmpObj);
    }
    activeKey.value = tmpObj.id;
  };
  const handleMoreActionSelect = (event: ActionsItem, item: MsAssertionItem) => {
    const currentIndex = selectItems.value.findIndex((tmpItem) => tmpItem.id === item.id);
    if (event.eventTag === 'delete') {
      selectItems.value.splice(currentIndex, 1);
      activeKey.value = currentIndex > 0 ? selectItems.value[currentIndex - 1].id : '';
    } else {
      // copy 当前item
      const tmpObj = { ...selectItems.value[currentIndex], id: new Date().getTime().valueOf().toString() };
      const tmpArr = selectItems.value;
      tmpArr.splice(currentIndex, 0, tmpObj);
      selectItems.value = tmpArr;
      activeKey.value = tmpObj.id;
    }
  };

  // item点击
  const handleItemClick = (item: MsAssertionItem) => {
    activeKey.value = item.id;
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
              font-size: 14px;
              font-weight: 400;
              color: var(--color-text-1);
              line-height: 22px;
            }
          }
          &-switch {
            display: flex;
            flex-direction: row;
            align-items: center;
            gap: 4px;
            &-action {
              display: flex;
              align-items: center;
              gap: 4px;
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
  .action-btn-move,
  .action-btn-more {
    opacity: 0;
    transition: opacity 0.2s;
  }
  .ms-assertion-body-left-item-active {
    background-color: rgb(var(--primary-1)) !important;
    .ms-assertion-body-left-item-row {
      &-num {
        color: rgb(var(--primary-5)) !important;
        background-color: var(--color-text-fff) !important;
      }
      &-title {
        color: rgb(var(--primary-5));
      }
    }
  }
  .ms-assertion-body-left-item:hover {
    background-color: rgb(var(--primary-1));
    .action-btn-move,
    .action-btn-more {
      opacity: 1;
    }
  }
  .ms-assertion-body-left-item-active-focus {
    background-color: rgb(var(--primary-1));
    .action-btn-move,
    .action-btn-more {
      opacity: 1;
    }
  }
</style>
