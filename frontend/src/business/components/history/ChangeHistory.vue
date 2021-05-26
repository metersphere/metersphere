<template>

  <el-dialog :close-on-click-modal="false" :title="$t('operating_log.change_history')" :visible.sync="infoVisible" width="900px" :destroy-on-close="true" append-to-body
             @close="handleClose" v-loading="loading">
    <el-table :data="details">
      <el-table-column prop="operTime" :label="$t('operating_log.time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.operTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="userName" :label="$t('operating_log.user')"/>
      <el-table-column prop="columnTitle" :label="$t('operating_log.change_field')">
        <template v-slot:default="scope">
          <div v-if="scope.row.details && scope.row.details.columns">
            <div v-for="detail in scope.row.details.columns" :key="detail.id">{{ detail.columnTitle }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="originalValue" :label="$t('operating_log.before_change')">
        <template v-slot:default="scope">
          <div v-if="scope.row.details && scope.row.details.columns">
            <div v-for="detail in scope.row.details.columns" :key="detail.id">
              <div v-if="linkDatas.indexOf(detail.columnName)!== -1">
                <el-link style="color: #409EFF" @click="openDetail(scope.row,detail)">{{$t('operating_log.info')}}</el-link>
              </div>
              <el-tooltip :content="detail.originalValue" v-else>
                <div class="current-value">{{ detail.originalValue ?detail.originalValue :"空值"}}</div>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="newValue" :label="$t('operating_log.after_change')">
        <template v-slot:default="scope">
          <div v-if="scope.row.details && scope.row.details.columns">
            <div v-for="detail in scope.row.details.columns" :key="detail.id">
              <div v-if="linkDatas.indexOf(detail.columnName)!== -1">
                <el-link style="color: #409EFF" @click="openDetail(scope.row,detail)">{{$t('operating_log.info')}}</el-link>
              </div>
              <el-tooltip :content="detail.newValue" v-else>
                <div class="current-value">{{ detail.newValue ? detail.newValue : "空值"}}</div>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <ms-history-detail ref="historyDetail"></ms-history-detail>
  </el-dialog>
</template>
<script>
  import MsHistoryDetail from "./HistoryDetail";

  export default {
    name: "MsChangeHistory",
    components: {MsHistoryDetail},
    props: {
      title: String,
    },
    data() {
      return {
        infoVisible: false,
        loading: false,
        details: [],
        linkDatas: ["prerequisite", "steps", "remark", "request", "response", "scenarioDefinition","tags", "loadConfiguration", "advancedConfiguration"],
      }
    },
    methods: {
      handleClose() {
        this.infoVisible = false;
      },
      getDetails(id) {
        this.result = this.$get("/operating/log/get/source/" + id, response => {
          let data = response.data;
          this.loading =false;
          if (data) {
            this.details = data;
          }
        })
      },
      open(id) {
        this.infoVisible = true;
        this.loading = true;
        this.getDetails(id);
      },
      openDetail(row, value) {
        value.createUser = row.details.createUser;
        value.operTime = row.operTime;
        this.$refs.historyDetail.open(value);
      },
    }
  }
</script>

<style scoped>
  .current-value {
    display: inline-block;
    overflow-x: hidden;
    padding-bottom: 0;
    text-overflow: ellipsis;
    vertical-align: middle;
    white-space: nowrap;
    width: 120px;
  }

</style>
