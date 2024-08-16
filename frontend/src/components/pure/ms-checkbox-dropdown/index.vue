<template>
  <a-dropdown v-model:popup-visible="visible" :hide-on-select="false">
    <MsButton
      type="icon"
      status="secondary"
      :class="`!rounded-[var(--border-radius-medium)] px-[4px] py-[2px] hover:!bg-[rgb(var(--primary-1))] ${
        visible || selectList?.length
          ? 'bg-[rgb(var(--primary-1))] !text-[rgb(var(--primary-5))] '
          : 'hover:!text-[var(--color-text-2)]'
      }`"
      @click="visible = !visible"
    >
      <span class="mr-[8px] font-medium"> {{ props.title }} </span>
      <svg-icon
        width="16px"
        height="16px"
        :name="visible || selectList?.length ? 'filter-icon-color' : 'filter-icon'"
        class="text-[12px] font-medium"
      />
    </MsButton>
    <template #content>
      <a-checkbox-group v-model="selectList" direction="vertical" @change="handleChange">
        <a-checkbox v-for="item in props.options" :key="item.value" :value="item.value">
          <slot name="item" :filter-item="item">
            <div class="one-line-text max-w-[120px]">{{ item.label }}</div>
          </slot>
        </a-checkbox>
      </a-checkbox-group>
    </template>
  </a-dropdown>
</template>

<script lang="ts" setup>
  import MsButton from '@/components/pure/ms-button/index.vue';

  const props = defineProps<{
    title: string;
    options: { label: string; value: string }[];
  }>();
  const emit = defineEmits<{
    (e: 'handleChange', val: string[]): void;
  }>();

  const selectList = defineModel<string[]>('selectList', {
    required: false,
  });

  const visible = ref(false);

  function handleChange(val: (string | number | boolean)[]) {
    emit('handleChange', val as string[]);
  }
</script>
