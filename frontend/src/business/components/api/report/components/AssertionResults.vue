<template>
  <el-table :data="assertions" style="width: 100%">
    <el-table-column prop="name" :label="$t('api_report.assertions_name')" width="300">
    </el-table-column>
    <el-table-column prop="message" :label="$t('api_report.assertions_message')">
    </el-table-column>
    <el-table-column prop="pass" :label="$t('api_report.assertions_is_success')" width="180">
      <template v-slot:default="{row}">
        <el-tag size="mini" type="success" v-if="row.pass">
          {{$t('api_report.success')}}
        </el-tag>
        <el-tag size="mini" type="danger" v-else>
          {{$t('api_report.fail')}}
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
    }
  }
</script>

<style scoped>

</style>
