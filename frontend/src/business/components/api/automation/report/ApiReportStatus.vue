<template>
  <div>
    <el-tag size="mini" type="info" v-if="row.status === 'Starting'">
      {{ row.status }}
    </el-tag>
    <el-tag size="mini" type="primary" effect="plain" v-else-if="row.status === 'Running'">
      {{ row.status }}
    </el-tag>
    <el-tag size="mini" type="success" v-else-if="row.status === 'Success'">
      {{ row.status }}
    </el-tag>
    <el-tag size="mini" type="warning" v-else-if="row.status === 'Reporting'">
      {{ row.status }}
    </el-tag>
    <el-tag size="mini" type="danger" v-else-if="row.status === 'Error'">
      {{ row.status }}
    </el-tag>
    <el-tag size="mini" type="danger" style="background-color: #F6972A; color: #FFFFFF" v-else-if="row.status === 'errorReportResult'">
      {{ $t('error_report_library.option.name') }}
    </el-tag>
    <el-tag v-else size="mini" type="info">
      {{ row.status }}
    </el-tag>
  </div>
</template>

<script>
  export default {
    name: "MsApiReportStatus",

    props: {
      row: Object
    }
  }
</script>

<style scoped>

</style>
