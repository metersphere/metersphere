<template>
  <a-dropdown
    v-model:popup-visible="triggerVisible"
    class="ms-minder-node-dropdown"
    :popup-translate="triggerOffset"
    position="bl"
    trigger="click"
    @select="(val) => handleSelect(val)"
  >
    <span></span>
    <template #content>
      <a-doption
        v-for="item in props.dropdownList"
        :key="item.value"
        v-permission="item.permission || []"
        :value="item.value"
        :class="props.checkedVal === item.value ? 'ms-minder-node-dropdown-item--active' : ''"
        @click="item.onClick && item.onClick()"
      >
        <a-tooltip :content="item.label" :mouse-enter-delay="500">
          <div class="one-line-text">{{ item.label }}</div>
        </a-tooltip>
      </a-doption>
    </template>
  </a-dropdown>
</template>

<script setup lang="ts">
  import useMinderStore from '@/store/modules/components/minder-editor';

  import { MinderEventName } from '@/enums/minderEnum';

  import useMinderTrigger from '../hooks/useMinderTrigger';
  import { dropdownMenuProps } from '../props';

  const props = defineProps(dropdownMenuProps);

  const minderStore = useMinderStore();
  const { triggerVisible, triggerOffset } = useMinderTrigger();

  function handleSelect(val?: string | number | Record<string, any>) {
    if (props.checkedVal !== val) {
      minderStore.dispatchEvent(MinderEventName.DROPDOWN_SELECT, val as string);
    }
  }
</script>

<style lang="less">
  .ms-minder-node-dropdown {
    max-width: 350px;
    max-height: 350px;
    .ms-minder-node-dropdown-item--active {
      color: rgb(var(--primary-5)) !important;
      background-color: rgb(var(--primary-1)) !important;
    }
  }
</style>
