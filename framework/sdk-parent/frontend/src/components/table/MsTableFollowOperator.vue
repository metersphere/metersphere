<template>
  <span>
   <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark"
               v-if="!row.showFollow">
      <i class="el-icon-star-off"
         style="color: #783987; font-size: 25px; padding-left: 5px;top: 5px; position: relative; cursor: pointer;width: 28px;height: 28px;"
         @click="saveFollow(row)"></i>
   </el-tooltip>
    <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark"
                v-if="row.showFollow">
      <i class="el-icon-star-on"
         style="color: #783987; font-size: 30px;padding-left: 5px; top: 5px; position: relative; cursor: pointer;width: 28px;height: 28px; "
         @click="saveFollow(row)"></i>
    </el-tooltip>
  </span>
</template>

<script>
export default {
  name: "MsTableFollowOperator",
  props: {
    row: Object
  },
  methods: {
    saveFollow(row) {
      this.$emit('saveFollow', row);
    }
  }
}
</script>

<style scoped>

</style>
