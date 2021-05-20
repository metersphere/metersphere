<template>

  <el-dialog :close-on-click-modal="false" :title="$t('operating_log.info')" :visible.sync="infoVisible" width="900px" :destroy-on-close="true"
             @close="handleClose" append-to-body>
    <div style="height: 700px;overflow: auto">
      <div v-if="detail.createUser">
        <p class="tip">{{ this.$t('report.user_name') }} ：{{detail.createUser}}</p>
      </div>
      <div>
        <p class="tip">{{ this.$t('operating_log.time') }} ：{{ detail.operTime | timestampFormatDate }}</p>
      </div>
      <div style="overflow: auto">
        <p class="tip">{{ this.$t('report.test_log_details') }} </p>
        <el-row style="background:#F8F8F8">
          <el-col :span="12">
            {{$t('operating_log.before_change')}}
          </el-col>
          <el-col :span="12">
            {{$t('operating_log.after_change')}}
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <div style="width: 400px;overflow: auto">
              <pre>{{ detail.originalValue }}</pre>
            </div>
          </el-col>
          <el-col :span="12">
            <div style="width: 400px;overflow: auto">
              <pre>{{ detail.newValue }}</pre>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </el-dialog>
</template>

<script>
  export default {
    name: "MsHistoryDetail",
    components: {},
    props: {
      title: String,
    },
    data() {
      return {
        infoVisible: false,
        detail: {},
      }
    },
    methods: {
      handleClose() {
        this.infoVisible = false;
      },
      open(value) {
        this.infoVisible = true;
        this.detail = value;
      },
      getType(type) {
        return this.LOG_TYPE_MAP.get(type);
      },
    }
  }
</script>

<style scoped>

  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
  }
</style>
