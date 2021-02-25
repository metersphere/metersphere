<template>
  <div>
    <template>
            <span>{{ $t('commons.operating') }}
             <i class='el-icon-setting' style="color:#7834c1; margin-left:10px" @click="customHeader"> </i>
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

</style>
