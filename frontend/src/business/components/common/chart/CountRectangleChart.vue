<template>
  <div class="main-number-show">
    <span class="count-number" :class="{'big_count-number': content < 10000}">
      {{ content }}
    </span>
    <span style="color: #6C317C;">
      {{ $t('api_test.home_page.unit_of_measurement') }}
    </span>
  </div>
</template>

<script>
export default {
  name: "CountRectangleChart",
  props: {
    content: {
      type: [Number, String]
    }
  }
}
</script>

<style scoped>
.main-number-show {
  line-height: 100px;
  text-align: center;
  height: 62px;
  line-height: 62px;
  border-style: solid;
  border-width: 2px;
  border-color: var(--count_number);
  background-color: #F1EAF1;
  overflow: hidden;
  text-overflow: ellipsis;
  border-radius: 5%;
  white-space: nowrap
}

.count-number {
  font-family: 'ArialMT', 'Arial', sans-serif;
  font-size: 23px;
  color: var(--count_number);
  position: relative;
}

.big_count-number {
  font-size: 33px;
}

</style>
