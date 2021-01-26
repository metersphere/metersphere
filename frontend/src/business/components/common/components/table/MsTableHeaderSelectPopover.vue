<template>
  <el-table-column width="1" :resizable="false" align="center">
    <el-popover slot="header" placement="right" trigger="click" style="margin-right: 0px;">
      <el-link @click.native.stop="$emit('selectAll')">{{$t('api_test.batch_menus.select_all_data',[total])}}</el-link>
      <br/>
      <el-link @click.native.stop="$emit('selectPageAll')">{{$t('api_test.batch_menus.select_show_data',[pageSize])}}</el-link>
      <i class="el-icon-arrow-down" slot="reference"></i>
    </el-popover>
  </el-table-column>
</template>

<script>
    export default {
      name: "MsTableSelectAll",
      props: ['total', 'pageSize'],
      data() {
        return {
          gridData: [{
            date: '2016-05-02',
            name: '王小虎',
            address: '上海市普陀区金沙江路 1518 弄'
          }, {
            date: '2016-05-04',
            name: '王小虎',
            address: '上海市普陀区金沙江路 1518 弄'
          }, {
            date: '2016-05-01',
            name: '王小虎',
            address: '上海市普陀区金沙江路 1518 弄'
          }, {
            date: '2016-05-03',
            name: '王小虎',
            address: '上海市普陀区金沙江路 1518 弄'
          }]
        };
      }
    }
</script>

<style scoped>

</style>
