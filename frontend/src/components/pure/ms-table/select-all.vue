<template>
  <div class="ms-table-select-all items-center text-base">
    <a-checkbox v-model="checked" class="text-base" :indeterminate="indeterminate" @change="handleCheckChange" />
    <a-dropdown position="bl" @select="handleSelect">
      <a-icon-down class="dropdown-icon ml-0.5" />
      <template #content>
        <a-doption :value="SelectAllEnum.CURRENT">{{ t('msTable.current') }}</a-doption>
        <a-doption :value="SelectAllEnum.ALL">{{ t('msTable.all') }}</a-doption>
      </template>
    </a-dropdown>
  </div>
</template>

<script lang="ts" setup>
  import { ref, watchEffect } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import { SelectAllEnum } from './type';

  const { t } = useI18n();

  const emit = defineEmits<{
    (e: 'change', value: string): void;
  }>();

  const props = defineProps({
    current: {
      type: Number,
      default: 0,
    },
    total: {
      type: Number,
      default: 0,
    },
  });

  const checked = ref(false);
  const indeterminate = ref(false);

  watchEffect(() => {
    if (props.current === 0) {
      checked.value = false;
      indeterminate.value = false;
    } else if (props.current < props.total) {
      checked.value = false;
      indeterminate.value = true;
    } else if (props.current >= props.total) {
      checked.value = true;
      indeterminate.value = false;
    }
  });

  const handleSelect = (v: string | number | Record<string, any> | undefined) => {
    emit('change', v as string);
  };

  const handleCheckChange = () => {
    if (checked.value) {
      handleSelect(SelectAllEnum.CURRENT);
    } else {
      handleSelect(SelectAllEnum.NONE);
    }
  };
</script>

<style lang="less" scoped>
  .ms-table-select-all {
    .dropdown-icon {
      color: rgb(var(--primary-6));
    }
    .dropdown-icon:hover {
      border-radius: 50%;
      background-color: rgb(var(--primary-3));
    }
  }
</style>
