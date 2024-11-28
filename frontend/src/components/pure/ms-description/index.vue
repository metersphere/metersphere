<template>
  <a-skeleton v-if="props.showSkeleton" :loading="props.showSkeleton" :animation="true">
    <a-space direction="vertical" class="w-[28%]" size="large">
      <a-skeleton-line :rows="props.skeletonLine" :line-height="24" />
    </a-space>
    <a-space direction="vertical" class="ml-[4%] w-[68%]" size="large">
      <a-skeleton-line :rows="props.skeletonLine" :line-height="24" />
    </a-space>
  </a-skeleton>
  <div v-else class="ms-description">
    <slot name="title"></slot>
    <div
      v-for="(item, index) of props.descriptions"
      :key="item.label"
      class="ms-description-item"
      :class="item.class"
      :style="{ marginBottom: props.descriptions.length - index <= props.column ? '' : `${props.lineGap}px` }"
    >
      <a-tooltip :content="item.label">
        <div :class="`ms-description-item-label one-line-text max-w-[${props.labelWidth || '120px'}]`">
          <slot name="item-label">{{ item.label }}</slot>
        </div>
      </a-tooltip>
      <div :class="getValueClass(item)">
        <slot name="item-value" :item="item">
          <template v-if="item.isTag">
            <slot name="tag" :item="item">
              <div class="w-[280px] overflow-hidden">
                <a-overflow-list>
                  <MsTag
                    v-for="tag of Array.isArray(item.value) ? item.value : [item.value]"
                    :key="`${tag}`"
                    :theme="item.tagTheme || 'outline'"
                    :type="item.tagType || 'primary'"
                    :max-width="item.tagMaxWidth"
                    color="var(--color-text-n8)"
                    :class="`mr-[8px] font-normal !text-[var(--color-text-1)] ${item.tagClass || ''}`"
                    :closable="item.closable"
                    @close="emit('tagClose', tag, item)"
                  >
                    {{ tag }}
                  </MsTag>
                  <template #overflow="{ number }">
                    <a-tooltip
                      :content="(Array.isArray(item.value) ? item.value : [item.value]).join('，')"
                      :position="item.tooltipPosition ?? 'tl'"
                    >
                      <MsTag
                        :theme="item.tagTheme || 'outline'"
                        :type="item.tagType || 'primary'"
                        :max-width="item.tagMaxWidth"
                        color="var(--color-text-n8)"
                        :class="`font-normal !text-[var(--color-text-1)] ${item.tagClass || ''}`"
                        tooltip-disabled
                      >
                        +{{ number }}
                      </MsTag>
                    </a-tooltip>
                  </template>
                </a-overflow-list>
              </div>
              <span v-if="!item.showTagAdd" v-show="Array.isArray(item.value) && item.value.length === 0">-</span>
              <div v-else>
                <template v-if="showTagInput">
                  <a-input
                    ref="inputRef"
                    v-model.trim="addTagInput"
                    size="mini"
                    :max-length="64"
                    :error="!!tagInputError"
                    @keyup.enter="handleAddTag(item)"
                    @blur="handleAddTag(item)"
                  >
                    <template #suffix>
                      <icon-loading v-if="tagInputLoading" class="text-[rgb(var(--primary-5))]" />
                    </template>
                  </a-input>
                  <span v-if="tagInputError" class="text-[12px] leading-[16px] text-[rgb(var(--danger-6))]">
                    {{ t('ms.description.addTagRepeat') }}
                  </span>
                </template>
                <!-- 标签数量大于等于10时，不显示添加标签 -->
                <MsTag
                  v-else-if="Array.isArray(item.value) && item.value.length < 10"
                  type="primary"
                  theme="outline"
                  :max-width="item.tagMaxWidth"
                  class="inline-flex cursor-pointer items-center gap-[4px]"
                  @click="handleEdit"
                >
                  <template #icon>
                    <MsIcon type="icon-icon_add_outlined" class="text-[rgb(var(--primary-5))]" />
                  </template>
                  {{ t('ms.description.addTag') }}
                </MsTag>
              </div>
            </slot>
          </template>
          <MsButton v-else-if="item.isButton" type="text" @click="handleItemClick(item)">
            {{ item.value }}
          </MsButton>
          <template v-else>
            <slot name="value" :item="item">
              <a-tooltip
                :content="`${item.value}`"
                :disabled="item.value === undefined || item.value === null || item.value?.toString() === ''"
                :position="item.tooltipPosition ?? 'tl'"
              >
                <div class="w-[fit-content]">
                  {{
                    item.value === undefined || item.value === null || item.value?.toString() === '' ? '-' : item.value
                  }}
                </div>
              </a-tooltip>
            </slot>
            <template v-if="item.showCopy">
              <MsButton
                v-if="item.copyTimer !== null && item.copyTimer !== undefined"
                class="absolute bottom-0 right-0 bg-[var(--color-text-fff)] pl-[16px] !text-[rgb(var(--success-6))]"
                type="text"
              >
                <MsIcon type="icon-icon_succeed_filled" class="mr-[4px]"></MsIcon>
                {{ t('ms.description.copySuccess') }}
              </MsButton>
              <MsButton
                v-else
                class="absolute bottom-0 right-0 bg-[var(--color-text-fff)] pl-[16px]"
                type="text"
                @click="copyValue(item)"
              >
                <MsIcon type="icon-icon_copy_outlined" class="mr-[4px]"></MsIcon>
                {{ t('ms.description.copy') }}
              </MsButton>
            </template>
          </template>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { nextTick, ref } from 'vue';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag, { TagType, Theme } from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  export interface Description {
    label: string;
    value: (string | number) | (string | number)[];
    key?: string;
    class?: string;
    isTag?: boolean; // 是否标签
    tagClass?: string; // 标签自定义类名
    tagType?: TagType; // 标签类型
    tagTheme?: Theme; // 标签主题
    tooltipPosition?:
      | 'top'
      | 'tl'
      | 'tr'
      | 'bottom'
      | 'bl'
      | 'br'
      | 'left'
      | 'lt'
      | 'lb'
      | 'right'
      | 'rt'
      | 'rb'
      | undefined; // 提示位置防止窗口抖动
    tagMaxWidth?: string; // 标签最大宽度
    closable?: boolean; // 标签是否可关闭
    showTagAdd?: boolean; // 是否显示添加标签
    isButton?: boolean;
    showCopy?: boolean;
    copyTimer?: any | null;
    onClick?: () => void;
  }

  const props = withDefaults(
    defineProps<{
      showSkeleton?: boolean;
      skeletonLine?: number;
      column?: number;
      descriptions: Description[];
      labelWidth?: string;
      oneLineValue?: boolean;
      lineGap?: number;
      addTagFunc?: (val: string, item: Description) => Promise<void>;
    }>(),
    {
      column: 1,
      lineGap: 16,
    }
  );
  const emit = defineEmits<{
    (e: 'addTag', val: string): void;
    (e: 'tagClose', tag: string | number, item: Description): void;
  }>();

  const { t } = useI18n();
  const { copy } = useClipboard();

  function getValueClass(item: Description) {
    if (item.isTag) {
      return 'ms-description-item-value--tagline';
    }
    if (props.oneLineValue) {
      return 'ms-description-item-value ms-description-item-value--one-line';
    }
    return 'ms-description-item-value';
  }

  async function copyValue(item: Description) {
    try {
      await copy(Array.isArray(item.value) ? item.value.join(',') : item.value.toString());
      item.copyTimer = setTimeout(() => {
        item.copyTimer = null;
      }, 3000);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      Message.error(t('ms.description.copyFail'));
    }
  }

  function handleItemClick(item: Description) {
    if (typeof item.onClick === 'function') {
      item.onClick();
    }
  }

  const addTagInput = ref('');
  const showTagInput = ref(false);
  const inputRef = ref();
  const tagInputLoading = ref(false);
  const tagInputError = ref('');

  /**
   * 添加标签
   * @param item 当前标签项
   */
  async function handleAddTag(item: Description) {
    if (addTagInput.value.trim() === '') {
      showTagInput.value = false;
      return;
    }
    if (Array.isArray(item.value) && item.value.includes(addTagInput.value)) {
      tagInputError.value = t('ms.description.addTagRepeat');
      return;
    }
    tagInputError.value = '';
    try {
      tagInputLoading.value = true;
      if (props.addTagFunc && typeof props.addTagFunc === 'function') {
        await props.addTagFunc(addTagInput.value, item);
        if (Array.isArray(item.value)) {
          item.value.push(addTagInput.value);
        } else {
          item.value = [addTagInput.value];
        }
      } else {
        emit('addTag', addTagInput.value);
      }
      addTagInput.value = '';
      showTagInput.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      tagInputError.value = (error as Error).message;
    } finally {
      tagInputLoading.value = false;
    }
  }

  function handleEdit() {
    showTagInput.value = true;

    nextTick(() => {
      inputRef.value[0].focus();
    });
  }
</script>

<style lang="less" scoped>
  .ms-description {
    @apply flex max-h-full flex-wrap overflow-auto;
    .ms-scroll-bar();
    .ms-description-item {
      @apply flex;

      width: calc(100% / v-bind(column));
    }
    .ms-description-item-label {
      @apply font-normal;

      padding-right: 16px;
      color: var(--color-text-3);
      word-wrap: break-word;
    }
    .ms-description-item-value,
    .ms-description-item-value--tagline {
      @apply relative flex-1 overflow-hidden break-all align-top;
    }
    .ms-description-item-value {
      /* stylelint-disable-next-line value-no-vendor-prefix */
      display: -webkit-box;
      text-overflow: ellipsis;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
    }
    .ms-description-item-value--one-line {
      -webkit-line-clamp: 1;
    }
  }
</style>
