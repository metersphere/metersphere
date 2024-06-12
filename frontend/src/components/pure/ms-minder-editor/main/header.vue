<template>
  <div class="ms-minder-editor-header">
    <a-tooltip v-for="item of props.iconButtons" :key="item.eventTag" :content="t(item.tooltip)">
      <MsButton type="icon" class="ms-minder-editor-header-icon-button" @click="emit('click', item.eventTag)">
        <MsIcon :type="item.icon" class="text-[var(--color-text-4)]" />
      </MsButton>
    </a-tooltip>
    <a-divider v-if="props.iconButtons?.length" direction="vertical" :margin="0"></a-divider>
    <a-tooltip :content="isFullScreen ? t('common.offFullScreen') : t('common.fullScreen')">
      <MsButton v-if="isFullScreen" type="icon" class="ms-minder-editor-header-icon-button" @click="toggleFullScreen">
        <MsIcon type="icon-icon_off_screen" class="text-[var(--color-text-4)]" />
      </MsButton>
      <MsButton v-else type="icon" class="ms-minder-editor-header-icon-button" @click="toggleFullScreen">
        <MsIcon type="icon-icon_full_screen_one" class="text-[var(--color-text-4)]" />
      </MsButton>
    </a-tooltip>
    <a-button
      type="outline"
      :disabled="props.disabled"
      class="px-[8px] py-[2px] text-[12px]"
      size="small"
      @click="save"
    >
      {{ t('minder.main.main.save') }}
    </a-button>
  </div>
</template>

<script lang="ts" setup>
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import useFullScreen from '@/hooks/useFullScreen';
  import { useI18n } from '@/hooks/useI18n';

  import { MinderIconButtonItem } from '../props';

  const props = defineProps<{
    iconButtons?: MinderIconButtonItem[];
    disabled?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'click', eventTag: string): void;
    (e: 'save'): void;
    (e: 'toggleFullScreen', isFullScreen: boolean): void;
  }>();

  const { t } = useI18n();

  const containerRef = ref<Element | null>(null);
  const { toggleFullScreen, isFullScreen } = useFullScreen(containerRef);

  watch(
    () => isFullScreen.value,
    (value) => {
      emit('toggleFullScreen', value);
    }
  );

  onMounted(() => {
    containerRef.value = document.querySelector('.ms-minder-editor-container');
  });

  function save() {
    emit('save');
  }
</script>

<style lang="less">
  .ms-minder-editor-header {
    @apply absolute z-10 flex items-center bg-white;

    top: 16px;
    right: 4px;
    gap: 8px;
    padding: 4px 8px;
    border-radius: var(--border-radius-small);
    box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
    .ms-minder-editor-header-icon-button {
      @apply !mr-0;
      &:hover {
        background-color: rgb(var(--primary-1)) !important;
        .arco-icon {
          color: rgb(var(--primary-4)) !important;
        }
      }
    }
  }
</style>
