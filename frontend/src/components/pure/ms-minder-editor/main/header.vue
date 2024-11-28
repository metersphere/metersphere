<template>
  <div class="ms-minder-editor-header">
    <a-tooltip v-for="item of props.iconButtons" :key="item.eventTag" :content="t(item.tooltip)">
      <MsButton type="icon" class="ms-minder-editor-header-icon-button" @click="emit('click', item.eventTag)">
        <MsIcon :type="item.icon" class="text-[var(--color-text-4)]" />
      </MsButton>
    </a-tooltip>
    <a-dropdown v-model:popup-visible="dropdownVisible" class="structure-dropdown" @select="handleChangeMode">
      <a-tooltip :content="t('minder.main.header.style')">
        <MsButton
          type="icon"
          :class="['ms-minder-editor-header-icon-button', `${dropdownVisible ? 'dropdown-visible' : ''}`]"
        >
          <MsIcon
            :type="ModeIcon[minderStore.getMinderActiveMode as keyof typeof ModeIcon]"
            class="text-[var(--color-text-4)]"
          />
        </MsButton>
      </a-tooltip>
      <template #content>
        <a-doption
          v-for="item in Object.entries(ModeIcon)"
          :key="item[0]"
          :value="item[0]"
          :class="[
            `${
              item[0] === minderStore.getMinderActiveMode
                ? '!bg-[rgb(var(--primary-9))] !text-[rgb(var(--primary-7))]'
                : 'text-[var(--color-text-4)]'
            }`,
          ]"
        >
          <MsIcon :type="item[1]" />
        </a-doption>
      </template>
    </a-dropdown>
    <a-divider direction="vertical" :margin="0"></a-divider>
    <a-tooltip :content="isFullScreen ? t('common.offFullScreen') : t('common.fullScreen')">
      <MsButton v-if="isFullScreen" type="icon" class="ms-minder-editor-header-icon-button" @click="toggleFullScreen">
        <MsIcon type="icon-icon_off_screen" class="text-[var(--color-text-4)]" />
      </MsButton>
      <MsButton v-else type="icon" class="ms-minder-editor-header-icon-button" @click="toggleFullScreen">
        <MsIcon type="icon-icon_full_screen_one" class="text-[var(--color-text-4)]" />
      </MsButton>
    </a-tooltip>
    <a-button
      v-if="!props.disabled"
      type="outline"
      class="flex items-center gap-[2px] px-[8px] py-[2px] text-[12px]"
      size="small"
      @click="save"
    >
      {{ t('minder.main.main.save') }}
      <div>(<MsCtrlOrCommand :size="12" /> + S)</div>
    </a-button>
  </div>
</template>

<script lang="ts" setup>
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCtrlOrCommand from '@/components/pure/ms-ctrl-or-command';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import useFullScreen from '@/hooks/useFullScreen';
  import { useI18n } from '@/hooks/useI18n';
  import { useMinderStore } from '@/store';
  import { ModeType } from '@/store/modules/components/minder-editor/types';

  import { MinderKeyEnum, ModeIcon } from '@/enums/minderEnum';

  import { MinderIconButtonItem } from '../props';

  const props = defineProps<{
    iconButtons?: MinderIconButtonItem[];
    disabled?: boolean;
    minderKey?: MinderKeyEnum;
  }>();
  const emit = defineEmits<{
    (e: 'click', eventTag: string): void;
    (e: 'save'): void;
    (e: 'toggleFullScreen', isFullScreen: boolean): void;
  }>();

  const { t } = useI18n();
  const minderStore = useMinderStore();

  const dropdownVisible = ref(false);
  async function handleChangeMode(value: string | number | Record<string, any> | undefined) {
    if (props.minderKey) {
      await minderStore.setMode(props.minderKey, value as ModeType);
    }
  }

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
    @apply absolute z-10 flex items-center;

    top: 16px;
    right: 4px;
    padding: 4px 8px;
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-fff);
    box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
    gap: 8px;
    .ms-minder-editor-header-icon-button {
      @apply !mr-0;
      &:hover {
        background-color: rgb(var(--primary-1)) !important;
        .arco-icon {
          color: rgb(var(--primary-4)) !important;
        }
      }
      &.dropdown-visible {
        background-color: rgb(var(--primary-9)) !important;
        .arco-icon {
          color: rgb(var(--primary-7)) !important;
        }
      }
    }
  }
  .structure-dropdown .arco-dropdown-list {
    gap: 8px;
    .arco-dropdown-option {
      padding: 4px;
      height: 24px;
      &:hover {
        .arco-icon {
          color: rgb(var(--primary-4));
        }
      }
    }
  }
</style>
