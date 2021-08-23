<template>
  <el-table :data="assertions" :row-style="getRowStyle" :header-cell-style="getRowStyle">
    <el-table-column prop="name" :label="$t('api_report.assertions_name')" width="150" show-overflow-tooltip>
      <template v-slot:default="scope">
        <span>{{ !scope.row.name || scope.row.name === 'null' ? "" : scope.row.name }}</span>
      </template>
    </el-table-column>
    <el-table-column prop="content" label="内容" width="200" show-overflow-tooltip/>
    <el-table-column prop="message" :label="$t('api_report.assertions_error_message')"/>
    <el-table-column prop="pass" :label="$t('api_report.assertions_is_success')" width="180">
      <template v-slot:default="{row}">
        <el-tag size="mini" type="success" v-if="row.pass">
          {{ $t('api_report.success') }}
        </el-tag>
        <el-tag size="mini" type="danger" v-else>
          {{ $t('api_report.fail') }}
        </el-tag>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
export default {
  name: "MsAssertionResults",

  props: {
    assertions: Array
  },

  methods: {
    getRowStyle() {
      return {backgroundColor: "#F5F5F5"};
    }
  }
}
</script>

<style scoped>

</style>
