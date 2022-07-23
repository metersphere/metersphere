<template>
  <div>
    <el-row>
      <el-col :span="4">{{ $t('api_test.mock.base_info') + ":" }}</el-col>
      <el-col :span="20" style="color: #783887">
        <el-checkbox v-model="fromData.protocol" disabled>{{
            $t('api_report.request') + $t('api_test.request.protocol')
          }}
        </el-checkbox>
        <el-checkbox v-model="fromData.method" disabled>
          {{ $t('api_test.definition.document.request_method') + '\xa0\xa0\xa0\xa0\xa0' }}
        </el-checkbox>
        <el-checkbox v-model="fromData.path" disabled>{{ "URL" }}</el-checkbox>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="4">{{ $t('api_test.mock.req_param') + ":" }}</el-col>
      <el-col :span="20" style="color: #783887">
        <el-checkbox v-model="fromData.headers">{{ "Header" + '\xa0\xa0' }}</el-checkbox>
        <el-checkbox v-model="fromData.query">{{ $t('api_test.definition.request.query_param') }}</el-checkbox>
        <el-checkbox v-model="fromData.rest">{{ $t('api_test.definition.request.rest_param') }}</el-checkbox>
        <el-checkbox v-model="fromData.body">{{ $t('api_test.request.body') }}</el-checkbox>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="4">{{ $t('api_test.definition.request.other_config') + ":" }}</el-col>
      <el-col :span="20" style="color: #783887">
        <el-checkbox v-model="fromData.delNotSame">{{ $t('workstation.delNotSame') }}</el-checkbox>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "SyncSetting",
  data() {
    return {
      fromData: {
        protocol: true,
        method: true,
        path: true,
        headers: true,
        query: true,
        rest: true,
        body: true,
        delNotSame: true,
        runError: true,
        unRun: true,
      }
    }
  }
}
</script>

<style scoped>
.el-row {
  margin-bottom: 3px;
}
</style>

