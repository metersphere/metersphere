<template>
  <div>

    <el-drawer
      :visible.sync="showDialog"
      :with-header="false"
      :modal-append-to-body="false"
      size="100%"
      ref="drawer"
      v-loading="result.loading">
      <template v-slot:default="scope">

        <el-row type="flex" class="head-bar">
          <el-col :span="12">
            <div class="name-edit">
              <el-button plain size="mini" icon="el-icon-back" @click="handleClose">{{$t('test_track.return')}}
              </el-button>&nbsp;
              <span class="title">{{report.name}}</span>
            </div>
          </el-col>
          <el-col :span="12" class="head-right">
            <el-button :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleSave">
              {{$t('commons.save')}}
            </el-button>
            <el-button :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleEdit">
              {{$t('test_track.plan_view.edit_component')}}
            </el-button>
            <!--<el-button :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleExport(report.name)">
              {{$t('test_track.plan_view.export_report')}}
            </el-button>-->
          </el-col>
        </el-row>

        <div class="container" ref="resume" id="app">
          <el-main>
            <div class="preview" v-for="item in previews" :key="item.id">
              <template-component :isReportView="true" :metric="metric" :preview="item"/>
            </div>
          </el-main>
        </div>
      </template>
    </el-drawer>
    <test-case-report-template-edit :metric="metric" ref="templateEdit" @refresh="getReport"/>
    <!-- <script>

     </script>-->
  </div>
</template>

<script>
  import {checkoutTestManagerOrTestUser, jsonToMap, mapToJson} from "../../../../../../../common/js/utils";
  import BaseInfoComponent from "./TemplateComponent/BaseInfoComponent";
  import TestResultChartComponent from "./TemplateComponent/TestResultChartComponent";
  import TestResultComponent from "./TemplateComponent/TestResultComponent";
  import RichTextComponent from "./TemplateComponent/RichTextComponent";
  import TestCaseReportTemplateEdit from "./TestCaseReportTemplateEdit";
  import TemplateComponent from "./TemplateComponent/TemplateComponent";
  import writer from 'file-writer'
  import ReportStyle from "../../../../../../../common/css/report.css.js";

  export default {
    name: "TestCaseReportView",
    components: {
      TemplateComponent,
      TestCaseReportTemplateEdit,
      RichTextComponent, TestResultComponent, TestResultChartComponent, BaseInfoComponent
    },
    data() {
      return {
        result: {},
        imgUrl: "",
        showDialog: false,
        previews: [],
        report: {},
        reportId: '',
        metric: {},
        planId: '',
        componentMap: new Map(
          [
            [1, {name: this.$t('test_track.plan_view.base_info'), id: 1, type: 'system'}],
            [2, {name: this.$t('test_track.plan_view.test_result'), id: 2, type: 'system'}],
            [3, {name: this.$t('test_track.plan_view.result_distribution'), id: 3, type: 'system'}],
            [4, {name: this.$t('test_track.plan_view.failure_case'), id: 4, type: 'system'}],
            [5, {name: this.$t('test_track.plan_view.defect_list'), id: 5, type: 'system'}],
            [6, {name: this.$t('test_track.plan_view.custom_component'), id: 6, type: 'custom'}]
          ]
        ),
        isTestManagerOrTestUser: false
      }
    },
    mounted() {
      this.isTestManagerOrTestUser = checkoutTestManagerOrTestUser();
    },
    methods: {
      listenGoBack() {
        //监听浏览器返回操作，关闭该对话框
        if (window.history && window.history.pushState) {
          history.pushState(null, null, document.URL);
          window.addEventListener('popstate', this.goBack, false);
        }
      },
      goBack() {
        this.handleClose();
      },
      open(planId, id) {
        this.planId = planId;
        if (id) {
          this.reportId = id;
        }
        this.getReport();
        this.showDialog = true;
        this.listenGoBack();
      },
      getReport() {
        this.result = this.$get('/case/report/get/' + this.reportId, response => {
          this.report = response.data;
          this.report.content = JSON.parse(response.data.content);
          if (this.report.content.customComponent) {
            this.report.content.customComponent = jsonToMap(this.report.content.customComponent);
          }
          this.getMetric();
          this.initPreviews();
        });
      },
      initPreviews() {
        this.previews = [];
        this.report.content.components.forEach(item => {
          let preview = this.componentMap.get(item);
          if (preview && preview.type != 'custom') {
            this.previews.push(preview);
          } else {
            if (this.report.content.customComponent) {
              let customComponent = this.report.content.customComponent.get(item.toString());
              if (customComponent) {
                this.previews.push({id: item, title: customComponent.title, content: customComponent.content});
              }
            }
          }
        });
      },
      handleClose() {
        window.removeEventListener('popstate', this.goBack, false);
        this.$emit('refresh');
        this.showDialog = false;
      },
      handleEdit() {
        this.$refs.templateEdit.open(this.reportId, true);
      },
      handleSave() {
        let param = {};
        this.buildParam(param);
        this.result = this.$post('/case/report/edit', param, () => {
          this.$success(this.$t('commons.save_success'));
        });
      },
      buildParam(param) {
        let content = {};
        content.components = [];
        this.previews.forEach(item => {
          content.components.push(item.id);
          if (!this.componentMap.get(item.id)) {
            content.customComponent = new Map();
            content.customComponent.set(item.id, {title: item.title, content: item.content})
          }
        });
        param.name = this.report.name;
        if (content.customComponent) {
          content.customComponent = mapToJson(content.customComponent);
        }
        param.content = JSON.stringify(content);
        param.id = this.report.id;
        if (this.metric.startTime) {
          param.startTime = this.metric.startTime.getTime();
        }
        if (this.metric.endTime) {
          param.endTime = this.metric.endTime.getTime();
        }
      },
      getMetric() {
        this.result = this.$get('/test/plan/get/metric/' + this.planId, response => {
          this.metric = response.data

          if (!this.metric.failureTestCases) {
            this.metric.failureTestCases = [];
          }
          if (!this.metric.executeResult) {
            this.metric.executeResult = [];
          }
          if (!this.metric.moduleExecuteResult) {
            this.metric.moduleExecuteResult = [];
          }
          /*缺陷列表*/
          if (!this.metric.issues) {
            this.metric.issues = [];
          }


          if (this.report.startTime) {
            this.metric.startTime = new Date(this.report.startTime);
          }
          if (this.report.endTime) {
            this.metric.endTime = new Date(this.report.endTime);
          }
        });
      },
      /*导出报告*/
      handleExport(name) {
        let html = this.getHtml();
        writer(`${name}.html`, html, 'utf-8');
        console.log(html)
      },
      getHtml() {
        let template = this.$refs.resume.innerHTML;
        let html = `<!DOCTYPE html>
                 <html>
                 <head>
                 <meta charset="utf-8">
                 <meta name="viewport" content="width=device-width,initial-scale=1.0">
                 <title>html</title>
                <link rel="stylesheet" href="https://unpkg.com/element-ui/lib/theme-chalk/index.css">
                <style>${ReportStyle}</style>
                </head>
                <body>
                <div style="margin:0 auto;width:1200px">
                     ${template}
                </div>
                <script src="https://cdn.bootcss.com/element-ui/2.4.11/index.js"/>
                </body>
                </html>`
        return html
      },

    }
  }
</script>

<style scoped>

  .el-main {
    height: calc(100vh - 70px);
    width: 100%;
  }

  .head-bar {
    background: white;
    height: 45px;
    line-height: 45px;
    padding: 0 10px;
    border: 1px solid #EBEEF5;
    box-shadow: 0 0 2px 0 rgba(31, 31, 31, 0.15), 0 1px 2px 0 rgba(31, 31, 31, 0.15);
  }

  .container {
    height: 100vh;
    background: #F5F5F5;
  }

  .el-card {
    width: 70%;
    margin: 5px auto;
  }

  .head-right {
    text-align: right;
  }

</style>
