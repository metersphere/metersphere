<template>
  <span>
    <a-dropdown
      v-model:popup-visible="visible"
      :trigger="props.trigger || 'hover'"
      @select="selectHandler"
      @popup-visible-change="visibleChange"
    >
      <slot>
        <div :class="['ms-more-action-trigger-content', visible ? 'ms-more-action-trigger-content--focus' : '']">
          <MsButton v-if="isHasAllPermission" type="text" size="mini" class="more-icon-btn" @click="visible = !visible">
            <MsIcon type="icon-icon_more_outlined" size="16" class="text-[var(--color-text-4)]" />
          </MsButton>
        </div>
      </slot>
      <template #content>
        <template v-for="item of props.list">
          <a-divider
            v-if="item.isDivider"
            :key="`${item.label}-divider`"
            :class="beforeDividerHasAction && afterDividerHasAction ? '' : 'hidden'"
            margin="4px"
          />

          <a-doption
            v-else
            :key="item.label"
            v-permission="item.permission || []"
            :class="item.danger ? 'error-6' : ''"
            :disabled="item.disabled"
            :value="item.eventTag"
          >
            <MsIcon v-if="item.icon" :type="item.icon" />
            {{ t(item.label || '') }}
          </a-doption>
        </template>
      </template>
    </a-dropdown>
  </span>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { hasAnyPermission } from '@/utils/permission';

  import type { ActionsItem, SelectedValue } from './types';

  const { t } = useI18n();
  const props = defineProps<{
    list: ActionsItem[];
    trigger?: 'click' | 'hover' | 'focus' | 'contextMenu' | ('click' | 'hover' | 'focus' | 'contextMenu')[] | undefined;
  }>();

  const emit = defineEmits(['select', 'close', 'open']);

  const visible = ref(false);

  // 检测在横线之前是否有action
  const beforeDividerHasAction = computed(() => {
    let result = false;
    for (let i = 0; i < props.list.length; i++) {
      const item = props.list[i];
      if (!item.isDivider) {
        result = hasAnyPermission(item.permission || []);
        if (result) {
          return true;
        }
      } else {
        return false;
      }
    }
    return result;
  });

  // 检测在横线之后是否有action
  const afterDividerHasAction = computed(() => {
    let result = false;
    for (let i = props.list.length - 1; i > 0; i--) {
      const item = props.list[i];
      if (!item.isDivider) {
        result = hasAnyPermission(item.permission || []);
        if (result) {
          return true;
        }
      } else {
        return false;
      }
    }
    return result;
  });

  // 判断是否有任一权限
  const isHasAllPermission = computed(() => {
    const permissionList = props.list.map((item) => {
      return item.permission || [];
    });
    const permissionResult = permissionList.flat();
    return hasAnyPermission(permissionResult);
  });

  function selectHandler(value: SelectedValue) {
    const item = props.list.find((e: ActionsItem) => e.eventTag === value);
    emit('select', item);
  }

  watch(
    () => visible.value,
    (val) => {
      if (val) {
        emit('open');
      }
    }
  );

  function visibleChange(val: boolean) {
    if (!val) {
      emit('close');
    }
  }
</script>

<style lang="less" scoped>
  .error-6 {
    color: rgb(var(--danger-6));
    &:hover {
      color: rgb(var(--danger-6));
    }
  }
</style>
