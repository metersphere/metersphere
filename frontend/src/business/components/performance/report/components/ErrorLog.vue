<template>
  <div>
    <el-table
      :data="tableData"
      border
      style="width: 100%"
      :default-sort = "{prop: 'elementLabel'}"
    >
      <el-table-column
        prop="typeOfError"
        label="Type of Error"
        sortable>
      </el-table-column>
      <el-table-column
        prop="numberOfErrors"
        label="Number of errors"
        sortable>
      </el-table-column>
      <el-table-column
        prop="error"
        label="% in errors"
        sortable>
      </el-table-column>
      <el-table-column
        prop="allSamples"
        label="% in all samples"
        sortable>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
  export default {
    name: "ErrorLog",
    data() {
      return {
        tableData: [{},{},{},{},{}]
      }
    },
    methods: {

    }
  }
</script>

<style scoped>

</style>
