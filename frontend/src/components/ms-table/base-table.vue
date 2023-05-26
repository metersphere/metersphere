<template>
  <div class="ms-base-tale">
    <a-table v-bind="$attrs">
      <template v-for="(item, key, i) in slots" :key="i" #[key]="{ record, rowIndex, column }">
        <slot :name="key" v-bind="{ rowIndex, record, column }"></slot>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
  import { useSlots } from 'vue';

  const slots = useSlots();
</script>
