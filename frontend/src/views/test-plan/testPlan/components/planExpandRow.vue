<template>
  <div
    v-if="record.type === testPlanTypeEnum.GROUP || record.integrated"
    class="mr-2 flex items-center"
    @click="expandHandler"
  >
    <MsIcon
      type="icon-icon_split_turn-down_arrow"
      class="arrowIcon mr-1 cursor-pointer text-[16px]"
      :class="getIconClass"
    />
    <span :class="getIconClass">{{ record.childrenCount || (record.children || []).length || 0 }}</span>
  </div>
  <div v-if="showButton" :class="`one-line-text ${hasIndent}`">
    <MsButton type="text" @click="handleAction">
      <a-tooltip :content="content">
        <span>{{ record[props.numKey || 'num'] }}</span>
      </a-tooltip>
    </MsButton>
  </div>
  <a-tooltip v-else :content="content">
    <div :class="`one-line-text ${hasIndent}`">{{ record[props.numKey || 'num'] }}</div>
  </a-tooltip>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';

  import { hasAnyPermission } from '@/utils/permission';

  import { testPlanTypeEnum } from '@/enums/testPlanEnum';

  const props = defineProps<{
    record: Record<string, any>;
    numKey?: string;
    idKey?: string;
    permission?: string[];
  }>();

  const emit = defineEmits<{
    (e: 'expand'): void;
    (e: 'action'): void;
  }>();

  const innerExpandedKeys = defineModel<string[]>('expandedKeys', { default: [] });

  const getIconClass = computed(() => {
    if (hasAnyPermission(props.permission || [])) {
      return innerExpandedKeys.value.includes(props.record[props.idKey || 'id'])
        ? 'text-[rgb(var(--primary-5))]'
        : 'text-[var(--color-text-4)]';
    }
    return '';
  });

  const content = computed(() => {
    const key = props.numKey || 'num';
    return typeof props.record[key] === 'string' ? props.record[key] : props.record[key].toString();
  });

  const hasIndent = computed(() =>
    (props.record.type === testPlanTypeEnum.TEST_PLAN && props.record.groupId && props.record.groupId !== 'NONE') ||
    (!props.record.integrated && props.record.parent)
      ? 'pl-[36px]'
      : ''
  );

  function expandHandler() {
    emit('expand');
  }

  function handleAction() {
    if (hasAnyPermission(props.permission || [])) {
      emit('action');
    }
  }

  const showButton = computed(() => {
    if (props.record.type) {
      return props.record.type === testPlanTypeEnum.TEST_PLAN;
    }
    return !props.record.integrated;
  });
</script>

<style scoped lang="less"></style>
