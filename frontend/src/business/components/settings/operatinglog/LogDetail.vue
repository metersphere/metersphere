<template>

  <el-dialog :close-on-click-modal="false" :title="getType(detail.operType)+title" :visible.sync="infoVisible" width="65%" :destroy-on-close="true"
             @close="handleClose">
    <div v-if="detail.createUser">
      <p class="tip">{{ this.$t('report.user_name') }} ：{{detail.createUser}}</p>
    </div>
    <div>
      <p class="tip">{{ this.$t('operating_log.user') }} ：{{detail.operUser}}</p>
    </div>
    <div>
      <p class="tip">{{ this.$t('operating_log.time') }} ：{{ detail.operTime | timestampFormatDate }}</p>
    </div>
    <div>
      <p class="tip">{{ this.$t('report.test_log_details') }} </p>
      <div v-if="detail && detail.operType !== 'CREATE' && detail.operType !=='DELETE' && detail.operType !=='COPY' && detail && detail.details && detail.details.columns && detail.details.columns.length >0 ">
        <div v-if="detail && detail.details && detail.details.columns" style="margin-left: 20px">
          <el-table :data="detail.details.columns">
            <el-table-column prop="columnTitle" :label="$t('operating_log.change_field')" width="150px" show-overflow-tooltip/>
            <el-table-column prop="originalValue" :label="$t('operating_log.before_change')" width="400px" show-overflow-tooltip>
              <template v-slot:default="scope">
                <span v-if="timeDates.indexOf(scope.row.columnName)!==-1">{{ scope.row.originalValue | timestampFormatDate }}</span>
                <pre v-else>{{ scope.row.originalValue }}</pre>

              </template>
            </el-table-column>
            <el-table-column prop="newValue" :label="$t('operating_log.after_change')" width="400px" show-overflow-tooltip>
              <template v-slot:default="scope">
                <span v-if="timeDates.indexOf(scope.row.columnName)!==-1">{{ scope.row.newValue | timestampFormatDate }}</span>
                <pre v-else>{{ scope.row.newValue }}</pre>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <div v-else-if="detail && (detail.operType ==='DELETE' || detail.details === null || (detail.details && detail.details.columns && detail.details.columns.length === 0))">
        <pre style="overflow: auto">{{detail.operTitle}} </pre>
        <span style="color: #409EFF">{{getType(detail.operType)}} </span>
        <span style="color: #409EFF"> {{$t('api_test.home_page.detail_card.success')}}</span>
      </div>
      <div v-else>
        <div v-if="detail && detail.details && detail.details.columns" style="overflow: auto">
          <span v-for="n in detail.details.columns" :key="n.id">
            <pre v-if="timeDates.indexOf(n.columnName)!==-1">
              {{n.columnTitle}}：{{ n.originalValue | timestampFormatDate }}
            </pre>
            <pre style="overflow: auto" v-else>
              {{n.columnTitle}}：{{n.originalValue}}
            </pre>
          </span>

        </div>
      </div>
    </div>
  </el-dialog>

</template>

<script>
  export default {
    name: "MsLogDetail",
    components: {},
    props: {
      title: String,
    },
    data() {
      return {
        infoVisible: false,
        detail: {},
        LOG_TYPE: [
          {id: 'CREATE', label: this.$t('api_test.definition.request.create_info')},
          {id: 'DELETE', label: this.$t('commons.delete')},
          {id: 'UPDATE', label: this.$t('commons.update')},
          {id: 'IMPORT', label: this.$t('api_test.api_import.label')},
          {id: 'EXPORT', label: this.$t('commons.export')},
          {id: 'ASSOCIATE_CASE', label: this.$t('test_track.review_view.relevance_case')},
          {id: 'ASSOCIATE_ISSUE', label: this.$t('test_track.case.relate_issue')},
          {id: 'REVIEW', label: this.$t('test_track.review_view.start_review')},
          {id: 'COPY', label: this.$t('commons.copy')},
          {id: 'EXECUTE', label: this.$t('api_test.automation.execute')},
          {id: 'CREATE_PRE_TEST', label: this.$t('api_test.create_performance_test')},
          {id: 'SHARE', label: this.$t('operating_log.share')},
          {id: 'LOGIN', label: this.$t('commons.login')},
          {id: 'RESTORE', label: this.$t('commons.reduction')},
          {id: 'DEBUG', label: this.$t('api_test.request.debug')},
          {id: 'GC', label: this.$t('api_test.automation.trash')},
          {id: 'BATCH_DEL', label: this.$t('api_test.definition.request.batch_delete')},
          {id: 'BATCH_UPDATE', label: this.$t('api_test.definition.request.batch_edit')},
          {id: 'BATCH_ADD', label: this.$t('commons.batch_add')},
          {id: 'UN_ASSOCIATE_CASE', label: this.$t('test_track.case.unlink')},
          {id: 'BATCH_RESTORE', label: "批量恢复"},
          {id: 'BATCH_GC', label: "批量回收"}
        ],
        LOG_TYPE_MAP: new Map([
          ['CREATE', this.$t('api_test.definition.request.create_info')],
          ['DELETE', this.$t('commons.delete')],
          ['UPDATE', this.$t('commons.update')],
          ['IMPORT', this.$t('api_test.api_import.label')],
          ['EXPORT', this.$t('commons.export')],
          ['ASSOCIATE_CASE', this.$t('test_track.review_view.relevance_case')],
          ['ASSOCIATE_ISSUE', this.$t('test_track.case.relate_issue')],
          ['REVIEW', this.$t('test_track.review_view.start_review')],
          ['COPY', this.$t('commons.copy')],
          ['EXECUTE', this.$t('api_test.automation.execute')],
          ['CREATE_PRE_TEST', this.$t('api_test.create_performance_test')],
          ['SHARE', this.$t('operating_log.share')],
          ['LOGIN', this.$t('commons.login')],
          ['RESTORE', this.$t('commons.reduction')],
          ['DEBUG', this.$t('api_test.request.debug')],
          ['GC', this.$t('api_test.automation.trash')],
          ['BATCH_DEL', this.$t('api_test.definition.request.batch_delete')],
          ['BATCH_UPDATE', this.$t('api_test.definition.request.batch_edit')],
          ['BATCH_ADD', this.$t('commons.batch_add')],
          ['BATCH_RESTORE', "批量恢复"],
          ['BATCH_GC', "批量回收"],
          ['UN_ASSOCIATE_CASE', this.$t('test_track.case.unlink')],
        ]),
        timeDates: ["plannedStartTime", "plannedEndTime", "startTime", "endTime"],
      }
    },
    methods: {
      handleClose() {
        this.infoVisible = false;
      },
      getDetails(id) {
        this.result = this.$get("/operating/log/get/" + id, response => {
          let data = response.data;
          this.detail = data;
        })
      },
      open(id) {
        this.infoVisible = true;
        this.getDetails(id);
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
