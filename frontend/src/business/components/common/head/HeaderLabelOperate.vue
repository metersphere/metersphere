<template>
  <div>
    <template>
            <span>{{ $t('commons.operating') }}
             <i class='el-icon-setting operator-color' @click="customHeader"> </i>
            </span>
    </template>
  </div>

</template>

<script>
export default {
  name: "HeaderLabelOperate",
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
