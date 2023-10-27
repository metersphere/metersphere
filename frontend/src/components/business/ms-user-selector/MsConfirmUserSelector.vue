<template>
  <div
    v-if="!switchStatus"
    class="flex cursor-pointer flex-row gap-[8px] text-[rgb(var(--primary-5))]"
    @click="showUserSelector"
  >
    <div>
      <MsIcon class="mr-[4px]" type="icon-icon_add_outlined" />
    </div>
    <div>{{ t('common.quickAddMember') }}</div>
  </div>
  <div v-else>
    <div class="flex flex-row items-center">
      <MsUserSelector v-bind="$attrs" v-model="memberList" class="w-[262px]" />
      <a-button type="outline" :loading="props.okLoading" class="ml-[12px]" size="mini" @click="handleConfirm">
        {{ t('common.confirm') }}
      </a-button>
      <div
        class="ml-[8px] flex h-[24px] w-[48px] cursor-pointer items-center justify-center rounded-[4px] border border-[var(--color-text-input-border)] px-[11px] text-[12px] text-[var(--color-text-1)] hover:bg-[rgb(var(--primary-1))]"
        @click="handleCancel"
      >
        {{ t('common.cancel') }}
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsUserSelector from './index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      okLoading?: boolean;
    }>(),
    {
      okLoading: false,
    }
  );

  const emit = defineEmits<{
    (e: 'confirm', value: string[], callback: (v: boolean) => void): void;
  }>();
  const switchStatus = ref(false);
  const memberList = ref<string[]>([]);

  const handleConfirm = () => {
    emit('confirm', memberList.value, (v) => {
      switchStatus.value = !v;
      if (v) {
        memberList.value = [];
      }
    });
  };
  const handleCancel = () => {
    if (props.okLoading) return;
    switchStatus.value = false;
    memberList.value = [];
  };

  const showUserSelector = () => {
    switchStatus.value = true;
  };
</script>
