<template>
  <div>
    <el-tag size="mini" type="info" v-if="row.status === 'Starting'">
      {{ getStatus(row.status) }}
    </el-tag>
    <el-tag size="mini" type="primary" effect="plain" v-else-if="row.status === 'Running'">
      {{ getStatus(row.status) }}
    </el-tag>
    <el-tag size="mini" type="success" v-else-if="getStatus(row.status) === 'success'">
      {{ getStatus(row.status) }}
    </el-tag>
    <el-tag size="mini" type="warning" v-else-if="row.status === 'Reporting'">
      {{ getStatus(row.status) }}
    </el-tag>
    <el-tag size="mini" type="danger" v-else-if="getStatus(row.status) === 'error'">
      {{ getStatus(row.status) }}
    </el-tag>
    <el-tag size="mini" type="danger" style="background-color: #F6972A; color: #FFFFFF" v-else-if="row.status === 'errorReportResult'">
      {{ $t('error_report_library.option.name') }}
    </el-tag>
    <el-tag v-else size="mini" type="info">
      {{ getStatus(row.status) }}
    </el-tag>
  </div>
</template>

<script>
  export default {
    name: "MsApiReportStatus",

    props: {
      row: Object
    },
    methods:{
      getStatus(status){
        return status.toLowerCase();
      }
    }
  }
</script>

<style scoped>

</style>
