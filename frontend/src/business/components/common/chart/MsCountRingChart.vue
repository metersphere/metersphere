<template>
  <el-tooltip :content="content + ''" placement="top-start">
    <div class="main-number-show">
      <span class="count-number">
        {{content}}
      </span>
      <span style="color: #6C317C;">
        {{$t('api_test.home_page.unit_of_measurement')}}
      </span>
    </div>
  </el-tooltip>
</template>

<script>
export default {
  name: "MsCountRingChart",
  props: {
    content: {
      type: [Number, String]
    }
  }
}
</script>

<style scoped>

.main-number-show {
  width: 100px;
  height: 100px;
  border-style: solid;
  border-width: 7px;
  border-color: var(--count_number_shallow);
  border-radius:50%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap
}

.count-number {
  font-family:'ArialMT', 'Arial', sans-serif;
  font-size: 33px;
  color: var(--count_number);
  position: relative;
}

</style>
