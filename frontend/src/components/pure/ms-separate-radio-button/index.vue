<template>
  <a-radio-group v-model="value">
    <template v-for="item in props.options" :key="item">
      <a-radio :value="item.value">
        <template #radio="{ checked }">
          <div :class="` radio-item ${checked ? 'radio-item--active' : ''}`">
            {{ item.label }}
          </div>
        </template>
      </a-radio>
    </template>
  </a-radio-group>
</template>

<script setup lang="ts">
  const props = defineProps<{
    options: { value: string; label: string }[];
  }>();

  const value = defineModel<string>('value', { required: true });
</script>

<style lang="less" scoped>
  .arco-radio-group .arco-radio {
    margin-right: 8px;
    padding-left: 0;
  }
  .radio-item {
    @apply flex cursor-pointer items-center;

    padding-left: 8px;
    width: 186px;
    height: 38px;
    border: 1px solid;
    border-color: var(--color-text-n8);
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-fff);
    &:hover {
      border-color: rgb(var(--primary-5));
    }
  }
  .radio-item--active {
    border-color: rgb(var(--primary-5));
    color: rgb(var(--primary-5));
    background-color: rgb(var(--primary-1));
  }
</style>
