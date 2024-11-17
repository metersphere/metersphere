<template>
  <a-dropdown
    v-model:popup-visible="visible"
    class="contain-child-dropdown"
    position="br"
    :trigger="['click', 'hover']"
    :hide-on-select="false"
  >
    <div :class="['ms-more-action-trigger-content', visible ? 'ms-more-action-trigger-content--focus' : '']">
      <MsButton type="text" size="mini" class="more-icon-btn" @click="visible = !visible">
        <MsIcon type="icon-icon_more_outlined" size="16" class="text-[var(--color-text-4)]" />
      </MsButton>
    </div>
    <template #content>
      <a-doption>
        <a-checkbox v-model="containChildModule" @change="handleContainChildModule">
          {{ t('project.environmental.http.containChildModule') }}
        </a-checkbox>
        <a-tooltip :content="t('project.environmental.http.containChildModuleTip')" position="br">
          <MsIcon
            class="text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
            type="icon-icon-maybe_outlined"
          />
        </a-tooltip>
      </a-doption>
    </template>
  </a-dropdown>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'open'): void;
    (e: 'handleContainChildModule', containChildModule: boolean): void;
  }>();

  const { t } = useI18n();

  const visible = ref(false);
  const containChildModule = defineModel<boolean>('containChildModule', { required: false, default: false });

  function handleContainChildModule(val: boolean | (string | number | boolean)[]) {
    emit('handleContainChildModule', val as boolean);
  }

  watch(
    () => visible.value,
    (val) => {
      if (val) {
        emit('open');
      } else {
        emit('close');
      }
    }
  );
</script>
