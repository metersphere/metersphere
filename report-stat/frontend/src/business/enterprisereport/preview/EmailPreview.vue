<template>
  <div style="margin: 5px" v-if="reloaded" v-loading="loading">
    <el-row>
      <el-col :span="10">
        <h3>{{ reportData.name }}</h3>
      </el-col>
      <el-col v-if="showButton" :span="14">
        <div style="float: right">
          <el-button
            v-prevent-re-click
            v-permission="['PROJECT_ENTERPRISE_REPORT:READ+EXPORT']"
            @click="savePlan('send')">
            {{ $t('commons.report_statistics.option.send') }}
          </el-button>
          <el-button
            v-prevent-re-click
            @click="closeDialog">
            {{ $t('test_track.cancel') }}
          </el-button>
        </div>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="4">
        {{ $t('mail.mail_subject') }}
      </el-col>
      <el-col :span="20">
        {{ reportData.name }}
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="4">
        {{ $t('mail.mail_addressee') }}
      </el-col>
      <el-col :span="20">
        {{ formatMailAddress(reportData.addressee) }}
      </el-col>
    </el-row>

    <el-row style="margin-bottom: 10px">
      <el-col :span="4">
        {{ $t('mail.mail_duplicate') }}
      </el-col>
      <el-col :span="20">
        {{ formatMailAddress(reportData.duplicated) }}
      </el-col>
    </el-row>
    <div style="margin-top: 10px;margin-bottom: 10px; ">
      <div v-for="item in reportData.reportContent" :key="item.id">
        <div style="background-color: #783887;font-size: 16px;color: white;margin: 5px;width: 100%; line-height:30px">
          <span style="margin-left: 5px">{{ item.name }}
          </span>
        </div>
        <div v-if="item.type ==='txt' " class="previewDiv" style="width: 100%" v-html="item.previewContext"></div>
        <div v-if="item.type === 'report' ">
          <img style="width: 100%" :src="item.recordImageContent"/>
          <table cellspacing="0" cellpadding="0" style="width: 100%;">
            <tr>
              <th style="border: 1px solid #E8EBF3;text-align: left; padding: 6px 10px"
                  v-for="tableHead in item.reportRecordData.showTable.heads" :key="tableHead.id">{{ tableHead.value }}
              </th>
            </tr>
            <tr v-for="tableRow in item.reportRecordData.showTable.data" :key="tableRow.id">
              <td style="border: 1px solid #E8EBF3;text-align: left; padding: 6px 10px"
                  v-for=" itemData in tableRow.tableDatas" :key="itemData.id"> {{ itemData.value }}
              </td>
            </tr>
          </table>
        </div>
        <div/>
      </div>
    </div>
  </div>
</template>

<script>
import {sendEnterpriseReport} from "@/api/enterprise-report";

export default {
  name: "EmailPreview",
  data() {
    return {
      reloaded: true,
      loading: false,
      reportData: {},
    }
  },
  props: {
    data: Object,
    showButton: {
      type: Boolean,
      default: false
    }
  },
  created() {
    if (this.data) {
      this.reportData = this.data;
      this.reloaded = false;

      if (this.reportData.reportContent) {
        this.reportData.reportContent.forEach(reportDetail => {
          if (reportDetail.reportRecordData && reportDetail.reportRecordData.showTable && reportDetail.reportRecordData.showTable.heads) {
            reportDetail.reportRecordData.showTable.heads.forEach(head => {
              if (head.value === 'creator') {
                head.value = this.$t('commons.report_statistics.report_filter.select_options.creator');
              } else if (head.value === 'Count') {
                head.value = this.$t('commons.report_statistics.count');
              } else if (head.value === 'maintainer') {
                head.value = this.$t('commons.report_statistics.report_filter.select_options.maintainer');
              } else if (head.value === 'casetype') {
                head.value = this.$t('commons.report_statistics.report_filter.select_options.case_type');
              } else if (head.value === 'casestatus') {
                head.value = this.$t('commons.report_statistics.report_filter.select_options.case_status');
              } else if (head.value === 'caselevel') {
                head.value = this.$t('commons.report_statistics.report_filter.select_options.case_level');
              } else if (head.value === 'testCase') {
                head.value = this.$t('api_test.home_page.failed_case_list.table_value.case_type.functional');
              } else if (head.value === 'apiCase') {
                head.value = this.$t('api_test.home_page.failed_case_list.table_value.case_type.api');
              } else if (head.value === 'scenarioCase') {
                head.value = this.$t('api_test.home_page.failed_case_list.table_value.case_type.scene');
              } else if (head.value === 'loadCase') {
                head.value = this.$t('api_test.home_page.failed_case_list.table_value.case_type.load');
              }
            });
          }
        });
      }
      this.$nextTick(() => {
        this.reloaded = true;
      })
    }
  },
  methods: {
    formatMailAddress(addressJson) {
      if (addressJson && addressJson.length > 0) {
        let returnAddress = "";
        addressJson.forEach(addr => {
          returnAddress += addr + ";";
        });
        return returnAddress;
      }
      return "";
    },
    savePlan(saveType) {
      let url = '/enterprise/test/report/send';
      let param = {};
      param.id = this.reportData.id;
      param.status = saveType;
      this.loading = true;
      sendEnterpriseReport(param).then(() => {
        this.$success(this.$t('commons.send_success'));
        this.dialogFormVisible = false;
        this.$emit("refresh");
        this.closeDialog();
        this.loading = false;
      }).catch(() => {
        this.loading = false;
        this.closeDialog();
      });
    },
    closeDialog() {
      this.$emit("closeDialog");
    }
  }
}
</script>

<style scoped>
.previewDiv {
  -webkit-text-size-adjust: 100%;
  line-height: 1.5;
  color: #24292e;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol";
  font-size: 16px;
  line-height: 1.5;
  word-wrap: break-word;
  overflow: auto;
}

.previewDiv :deep(p) {
  width: 100%;
}

.previewDiv :deep([class*=" fa-mavon-"]:before, [class^=fa-mavon-]:before) {
  font-family: fontello;
  font-style: normal;
  font-weight: 400;
  speak: none;
  display: inline-block;
  text-decoration: inherit;
  width: 1em;
  margin-right: .2em;
  text-align: center;
  font-variant: normal;
  text-transform: none;
  line-height: 1em;
  margin-left: .2em;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale
}

.previewDiv :deep(.fa-mavon-bold:before) {
  content: "\E800"
}

.previewDiv :deep(.fa-mavon-italic:before) {
  content: "\E801"
}

.previewDiv :deep(.fa-mavon-thumb-tack:before) {
  content: "\E802"
}

.previewDiv :deep(.fa-mavon-link:before) {
  content: "\E803"
}

.previewDiv :deep(.fa-mavon-picture-o:before) {
  content: "\E804"
}

.previewDiv :deep(.fa-mavon-repeat:before) {
  content: "\E805"
}

.previewDiv :deep(.fa-mavon-undo:before) {
  content: "\E806"
}

.previewDiv :deep(.fa-mavon-trash-o:before) {
  content: "\E807"
}

.previewDiv :deep(.fa-mavon-floppy-o:before) {
  content: "\E808"
}

.previewDiv :deep(.fa-mavon-compress:before) {
  content: "\E809"
}

.previewDiv :deep(.fa-mavon-eye:before) {
  content: "\E80A"
}

.previewDiv :deep(.fa-mavon-eye-slash:before) {
  content: "\E80B"
}

.previewDiv :deep(.fa-mavon-question-circle:before) {
  content: "\E80C"
}

.previewDiv :deep(.fa-mavon-times:before) {
  content: "\E80D"
}

.previewDiv :deep(.fa-mavon-align-left:before) {
  content: "\E80F"
}

.previewDiv :deep(.fa-mavon-align-center:before) {
  content: "\E810"
}

.previewDiv :deep(.fa-mavon-align-right:before) {
  content: "\E811"
}

.previewDiv :deep(.fa-mavon-arrows-alt:before) {
  content: "\F0B2"
}

.previewDiv :deep(.fa-mavon-bars:before) {
  content: "\F0C9"
}

.previewDiv :deep(.fa-mavon-list-ul:before) {
  content: "\F0CA"
}

.previewDiv :deep(.fa-mavon-list-ol:before) {
  content: "\F0CB"
}

.previewDiv :deep(.fa-mavon-strikethrough:before) {
  content: "\F0CC"
}

.previewDiv :deep(.fa-mavon-underline:before) {
  content: "\F0CD"
}

.previewDiv :deep(.fa-mavon-table:before) {
  content: "\F0CE"
}

.previewDiv :deep(.fa-mavon-columns:before) {
  content: "\F0DB"
}

.previewDiv :deep(.fa-mavon-quote-left:before) {
  content: "\F10D"
}

.previewDiv :deep(.fa-mavon-code:before) {
  content: "\F121"
}

.previewDiv :deep(.fa-mavon-superscript:before) {
  content: "\F12B"
}

.previewDiv :deep(.fa-mavon-subscript:before) {
  content: "\F12C"
}

.previewDiv :deep(.fa-mavon-header:before) {
  content: "\F1DC"
}

.previewDiv :deep(.fa-mavon-window-maximize:before) {
  content: "\F2D0"
}

.previewDiv :deep(strong) {
  font-weight: bolder
}

.previewDiv :deep(.hljs-center) {
  text-align: center
}

.previewDiv :deep(.hljs-right) {
  text-align: right
}

.previewDiv :deep(.hljs-left) {
  text-align: left
}
</style>
