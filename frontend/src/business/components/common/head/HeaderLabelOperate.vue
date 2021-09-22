<template>
  <div>
    <template>
            <span>{{ $t('commons.operating') }}
             <i v-if="!disableHeaderConfig" class='el-icon-setting operator-color' @click="customHeader"> </i>
            </span>
    </template>
  </div>

</template>

<script>
export default {
  name: "HeaderLabelOperate",
  props:['disableHeaderConfig'],
  methods: {
    customHeader() {
      this.$emit('exec')
    }
  }
}
</script>

<style scoped>
.operator-color {
  color: var(--count_number);
  margin-left:10px;
}
</style>
