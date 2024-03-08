<template>
  <div v-if="props.actionConfig" ref="refWrapper" class="flex flex-row flex-nowrap">
    <div class="title">{{ t('msTable.batch.selected', { count: props.selectRowCount }) }}</div>
    <template v-for="(element, idx) in baseAction" :key="element.label">
      <a-divider v-if="element.isDivider" class="divider mx-0 my-[6px]" />
      <a-button
        v-if="!element.isDivider && !element.children && hasAllPermission(element.permission as string[])"
        class="ml-[12px]"
        :class="{
          'arco-btn-outline--danger': element.danger,
          'ml-[16px]': idx === 0,
        }"
        type="outline"
        @click="handleSelect(element)"
        >{{ t(element.label as string) }}</a-button
      >
      <!-- baseAction多菜单选择 -->
      <a-dropdown
        v-if="!element.isDivider && element.children && hasAllPermission(element.permission as string[])"
        position="tr"
        @select="handleSelect"
      >
        <a-button
          class="ml-[12px]"
          :class="{
            'arco-btn-outline--danger': element.danger,
            'ml-[16px]': idx === 0,
          }"
          type="outline"
          @click="handleSelect"
          >{{ t(element.label as string) }}</a-button
        >
        <template #content>
          <template v-for="item in element.children" :key="item.label">
            <a-divider v-if="element.isDivider" margin="4px" />
            <a-doption v-else :value="item" :class="{ delete: item.danger }">
              {{ t(item.label as string) }}
            </a-doption>
          </template>
        </template>
      </a-dropdown>
      <!-- baseAction多菜单选择 -->
    </template>
    <div v-if="moreActionLength > 0" class="drop-down relative ml-[16px] inline-block">
      <a-dropdown position="tr" @select="handleSelect">
        <a-button type="outline"><MsIcon type="icon-icon_more_outlined" /></a-button>
        <template #content>
          <template v-for="element in moreAction" :key="element.label">
            <a-divider v-if="element.isDivider" margin="4px" />
            <a-doption
              v-else-if="hasAllPermission(element.permission as string[])"
              :value="element"
              :class="{ delete: element.danger }"
            >
              {{ t(element.label as string) }}
            </a-doption>
          </template>
        </template>
      </a-dropdown>
    </div>
    <a-button class="clear-btn ml-[16px]" type="text" @click="emit('clear')">{{ t('msTable.batch.clear') }}</a-button>
  </div>
</template>

<script lang="ts" setup>
  import MsIcon from '../ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { getNodeWidth } from '@/utils/dom';
  import { hasAllPermission } from '@/utils/permission';

  import { BatchActionConfig, BatchActionParams } from './type';
  import ResizeObserver from 'resize-observer-polyfill';

  const { t } = useI18n();

  const refWrapper = ref<HTMLElement>();

  const props = defineProps<{
    selectRowCount?: number;
    actionConfig?: BatchActionConfig;
  }>();
  const emit = defineEmits<{
    (e: 'batchAction', value: BatchActionParams): void;
    (e: 'clear'): void;
  }>();

  const baseAction = ref<BatchActionConfig['baseAction']>([]);
  const moreAction = ref<BatchActionConfig['moreAction']>([]);
  // 存储所有的action
  const allAction = ref<BatchActionConfig['baseAction']>([]);
  const refResizeObserver = ref<ResizeObserver>();
  // 控制是否重新计算
  const computedStatus = ref(true);

  const moreActionLength = ref<number>(0);

  const titleClass = 'title';
  const dividerClass = 'divider';
  const dropDownClass = 'drop-down';
  const clearBtnClass = 'clear-btn';

  const handleSelect = (item: string | number | Record<string, any> | undefined | BatchActionParams) => {
    emit('batchAction', item as BatchActionParams);
  };

  const computedLastVisibleIndex = () => {
    if (!refWrapper.value) {
      return;
    }
    if (!computedStatus.value) {
      computedStatus.value = true;
      return;
    }
    const wrapperWidth = getNodeWidth(refWrapper.value);

    const childNodeList = [].slice.call(refWrapper.value.children) as HTMLElement[];

    let totalWidth = 0;
    let menuItemIndex = 0;

    for (let i = 0; i < childNodeList.length; i++) {
      const node = childNodeList[i];
      const classNames = node.className.split(' ');
      const isTitle = classNames.includes(titleClass);
      const isDivider = classNames.includes(dividerClass);
      const isDropDown = classNames.includes(dropDownClass);
      const isClearBtn = classNames.includes(clearBtnClass);
      if (isDivider) {
        totalWidth += 16;
      } else if (isTitle) {
        // title宽度为固定值100px
        totalWidth += 100;
      } else if (isDropDown) {
        // dropDown宽度为固定值48px + MarginLeft 16px
        totalWidth += 64;
      } else if (isClearBtn) {
        // 清空选择按钮 60px + MarginLeft 16px
        totalWidth += 76;
      } else {
        // 普通按钮宽度为内容宽度 + marginLeft 16px
        totalWidth += getNodeWidth(node) + 16;
        menuItemIndex++;
      }
      if (totalWidth > wrapperWidth) {
        const value = menuItemIndex - 1;
        baseAction.value = allAction.value.slice(0, value);
        moreAction.value = allAction.value.slice(value);
        computedStatus.value = false;
        return;
      }
    }
    moreAction.value = props.actionConfig?.moreAction || [];
    baseAction.value = props.actionConfig?.baseAction || [];
    moreAction.value.forEach((key) => {
      if (key.permission && hasAllPermission(key.permission as string[])) {
        moreActionLength.value += 1;
      }
    });
  };

  watch(
    () => props.actionConfig,
    (value) => {
      if (value) {
        allAction.value = [...value.baseAction];
        baseAction.value = [...value.baseAction];
        if (value.moreAction) {
          allAction.value = [...allAction.value, ...value.moreAction];
          moreAction.value = [...value.moreAction];
        }
      }
    },
    { immediate: true }
  );

  onMounted(() => {
    refResizeObserver.value = new ResizeObserver((entries: ResizeObserverEntry[]) => {
      entries.forEach(computedLastVisibleIndex);
    });

    if (refWrapper.value) {
      refResizeObserver.value.observe(refWrapper.value);
    }
  });
  onUnmounted(() => {
    refResizeObserver.value?.disconnect();
  });
</script>

<style lang="less" scoped>
  .title {
    display: flex;
    align-items: center;
    width: 100px;
    color: var(--color-text-2);
  }
  .delete {
    border-color: rgb(var(--danger-6)) !important;
    color: rgb(var(--danger-6));
  }
</style>
