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
      :style="{ marginBottom: props.descriptions.length - index <= props.column ? '' : '16px' }"
    >
      <div class="ms-description-item-label" :style="{ width: props.labelWidth || '120px' }">
        <slot name="item-label">{{ item.label }}</slot>
      </div>
      <div class="ms-description-item-value">
        <slot name="item-value" :item="item">
          <template v-if="item.isTag">
            <MsTag
              v-for="tag of item.value"
              :key="`${tag}`"
              theme="outline"
              color="var(--color-text-n8)"
              class="mb-[8px] mr-[8px] font-normal !text-[var(--color-text-1)]"
            >
              {{ tag }}
            </MsTag>
            <span v-if="!item.showTagAdd" v-show="Array.isArray(item.value) && item.value.length === 0">-</span>
            <div v-else>
              <template v-if="showTagInput">
                <a-input
                  ref="inputRef"
                  v-model.trim="addTagInput"
                  size="mini"
                  :error="!!tagInputError"
                  @keyup.enter="handleAdd(item)"
                  @blur="handleAdd(item)"
                >
                  <template #suffix>
                    <icon-loading v-if="tagInputLoading" class="text-[rgb(var(--primary-5))]" />
                  </template>
                </a-input>
                <span v-if="tagInputError" class="text-[12px] text-[rgb(var(--danger-6))]">
                  {{ t('ms.description.addTagRepeat') }}
                </span>
              </template>
              <MsTag v-else type="primary" theme="outline" class="cursor-pointer" @click="handleEdit">
                <template #icon>
                  <MsIcon type="icon-icon_add_outlined" class="text-[rgb(var(--primary-5))]" />
                </template>
                {{ t('ms.description.addTag') }}
              </MsTag>
            </div>
          </template>
          <a-button v-else-if="item.isButton" type="text" :size="item.buttonSize" @click="handleItemClick(item)">
            {{ item.value }}
          </a-button>
          <template v-else>
            <slot name="value" :item="item">
              {{ item.value === undefined || item.value === null || item.value?.toString() === '' ? '-' : item.value }}
            </slot>
            <template v-if="item.showCopy">
              <MsButton
                v-if="item.copyTimer !== null && item.copyTimer !== undefined"
                class="absolute bottom-0 right-0 bg-white pl-[16px] !text-[rgb(var(--success-6))]"
                type="text"
              >
                <MsIcon type="icon-icon_succeed_filled" class="mr-[4px]"></MsIcon>
                {{ t('ms.description.copySuccess') }}
              </MsButton>
              <MsButton
                v-else
                class="absolute bottom-0 right-0 bg-white pl-[16px]"
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
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  export interface Description {
    label: string;
    value: (string | number) | (string | number)[];
    key?: string;
    isTag?: boolean; // 是否标签
    showTagAdd?: boolean; // 是否显示添加标签
    isButton?: boolean;
    buttonSize?: 'small' | 'mini' | 'large' | 'medium';
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
      addTagFunc?: (val: string) => Promise<void>;
    }>(),
    {
      column: 1,
    }
  );
  const emit = defineEmits<{
    (e: 'addTag', val: string): void;
  }>();

  const { t } = useI18n();
  const { copy } = useClipboard();

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

  async function handleAdd(item: Description) {
    if (Array.isArray(item.value) && item.value.includes(addTagInput.value)) {
      tagInputError.value = t('ms.description.addTagRepeat');
      return;
    }
    tagInputError.value = '';
    try {
      tagInputLoading.value = true;
      if (props.addTagFunc && typeof props.addTagFunc === 'function') {
        await props.addTagFunc(addTagInput.value);
      } else {
        emit('addTag', addTagInput.value);
      }
      showTagInput.value = false;
    } catch (error) {
      // eslint-disable-next-line prettier/prettier
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
    @apply flex flex-wrap;
    .ms-description-item {
      @apply flex;

      width: calc(100% / v-bind(column));
    }
    .ms-description-item-label {
      @apply whitespace-pre-wrap font-normal;

      padding-right: 16px;
      color: var(--color-text-3);
      word-wrap: break-word;
    }
    .ms-description-item-value {
      @apply relative flex-1 overflow-hidden break-all align-top;

      /* stylelint-disable-next-line value-no-vendor-prefix */
      display: -webkit-box;
      text-overflow: ellipsis;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
    }
  }
</style>
