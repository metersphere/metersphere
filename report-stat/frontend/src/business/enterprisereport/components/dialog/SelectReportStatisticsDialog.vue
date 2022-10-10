<template>
  <div>
    <el-dialog v-loading="result.loading"
               :close-on-click-modal="false"
               :destroy-on-close="true"
               :visible.sync="dialogFormVisible"
               top="8vh"
               width="20%">
      <el-form>
        <el-row>
          <el-input v-model="recordName" size="small" :placeholder="$t('commons.input_name')"></el-input>
        </el-row>
        <div style="height: 300px; margin-top: 20px; overflow: auto">
          <el-row v-for="(item) in reportRecords" :key="item.id" style="margin: 5px">
            <el-radio v-model="selectReportId" :label="item.id">{{ item.name }}</el-radio>
          </el-row>
        </div>
      </el-form>
      <template v-slot:footer>
        <div class="dialog-footer">
          <el-button
            type="primary"
            v-prevent-re-click
            v-loading="reportRecordSelecting"
            @click="selectReportRecord">
            {{ $t('test_track.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>

</template>

<script>

import TestCaseCountContainer from "@/business/projectstatistics/casecount/TestCaseCountContainer";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {selectHistoryReportById, selectHistoryReportByParams} from "@/api/history-report";

export default {
  name: "SelectReportStatistics",
  components: {TestCaseCountContainer},
  data() {
    return {
      reportDetailData: {},
      reportRecordSelecting: false,
      result: {loading: false},
      dialogFormVisible: false,
      recordName: "",
      reportRecords: [],
      selectReportId: "",
    };
  },
  props: {},
  created() {
    this.reportRecordSelecting = false;
    this.searchReportRecord();
  },
  computed: {},
  watch: {
    recordName() {
      this.searchReportRecord();
    }
  },
  methods: {
    open(reportData) {
      this.reportRecordSelecting = false;
      this.reportDetailData = reportData;
      this.searchReportRecord();
      this.dialogFormVisible = true;
    },
    searchReportRecord() {
      this.selectReportId = "";
      let paramsObj = {
        projectId: getCurrentProjectID(),
        reportType: "TEST_CASE_COUNT",
        name: this.recordName
      };
      selectHistoryReportByParams(paramsObj).then(response => {
        this.reportRecords = response.data;
      });
    },
    selectReportRecord() {
      if (this.selectReportId === '') {
        this.$error(this.$t('请选择要添加的报告!'));
        return false;
      }
      let paramObj = {
        id: this.selectReportId
      }
      this.reportRecordSelecting = true;
      selectHistoryReportById(paramObj).then(response => {
        let reportData = response.data;
        if (reportData) {
          this.reportDetailData.reportRecordId = this.selectReportId;
          let selectOption = JSON.parse(reportData.selectOption);
          let dataOption = JSON.parse(reportData.dataOption);
          dataOption.selectOption = selectOption;
          this.reportDetailData.reportRecordData = dataOption;
        }
        this.$emit("addReportRecord", this.reportDetailData);
        this.dialogFormVisible = false;
        this.reportRecordSelecting = false;
      }, () => {
        this.$error(this.$t('查找报告失败!'));
        this.reportRecordSelecting = false;
        return false;
      });
    }
  }
}
</script>

<style scoped>
</style>
