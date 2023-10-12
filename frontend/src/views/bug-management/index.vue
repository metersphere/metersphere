<template>
  <MsCard simple>
    <h1>BugManagement is waiting for development </h1>
    <ms-pagination size="small" :total="100000" show-total show-jumper show-page-size />

    <a-range-picker
      style="margin: 0 24px 24px 0; width: 360px"
      show-time
      :time-picker-props="{ defaultValue: ['00:00:00', '09:09:06'] }"
      format="YYYY-MM-DD HH:mm"
      @change="onChange"
      @select="onSelect"
      @ok="onOk"
    />
    <!-- <MsRichText v-model="content" /> -->
    <MsTimeSelector v-model="timeValue" />
    <div>value: {{ timeValue }}</div>
  </MsCard>
</template>

<script setup>
  import MsPagination from '@/components/pure/ms-pagination/index';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsTimeSelector from '@/components/pure/ms-time-selector/MsTimeSelector.vue';

  const content = ref('');
  const timeValue = ref('3M');
</script>
