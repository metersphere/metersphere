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
              <el-button plain size="mini" icon="el-icon-back" @click="handleClose">{{$t('test_track.return')}}</el-button>
              &nbsp;
              <span>{{report.name}}</span>
            </div>
          </el-col>
          <el-col :span="12" class="head-right">
            <el-button plain size="mini" @click="handleSave">保存</el-button>
            <el-button plain size="mini" @click="handleEdit">编辑组件</el-button>
          </el-col>
        </el-row>

        <div class="container">
          <el-main>
            <div class="preview" v-for="item in previews" :key="item.id">
              <template-component :isReportView="true" :metric="metric" :preview="item"/>
            </div>
          </el-main>
        </div>
      </template>
    </el-drawer>

    <test-case-report-template-edit :metric="metric" ref="templateEdit" @refresh="getReport"/>

  </div>
</template>

<script>
  import {jsonToMap, mapToJson} from "../../../../../../common/js/utils";
    import BaseInfoComponent from "../../../../settings/workspace/components/TemplateComponent/BaseInfoComponent";
    import TestResultChartComponent from "../../../../settings/workspace/components/TemplateComponent/TestResultChartComponent";
    import TestResultComponent from "../../../../settings/workspace/components/TemplateComponent/TestResultComponent";
    import RichTextComponent from "../../../../settings/workspace/components/TemplateComponent/RichTextComponent";
    import TestCaseReportTemplateEdit from "../../../../settings/workspace/components/TestCaseReportTemplateEdit";
    import TemplateComponent from "../../../../settings/workspace/components/TemplateComponent/TemplateComponent";

    export default {
      name: "TestCaseReportView",
      components: {
        TemplateComponent,
        TestCaseReportTemplateEdit,
        RichTextComponent, TestResultComponent, TestResultChartComponent, BaseInfoComponent},
      data() {
        return {
          result: {},
          showDialog: false,
          previews: [],
          report: {},
          reportId: '',
          metric: {},
          componentMap: new Map(
            [
              [1, { name: "基础信息", id: 1 , type: 'system'}],
              [2, { name: "测试结果", id: 2 , type: 'system'}],
              [3, { name: "测试结果分布", id: 3 ,type: 'system'}],
              [4, { name: "自定义模块", id: 4 ,type: 'custom'}]
            ]
          )
        }
      },
      props: {
        planId: {
          type: String
        }
      },
      methods: {
        open(id) {
          if (id) {
            this.reportId = id;
          }
          this.getReport();
          this.showDialog = true;
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
          this.showDialog = false;
        },
        handleEdit() {
          this.$refs.templateEdit.open(this.reportId, true);
        },
        handleSave() {
          let param = {};
          this.buildParam(param);
          this.result = this.$post('/case/report/edit', param, () =>{
            this.$success('保存成功');
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
            this.metric = response.data;
            if (this.report.startTime) {
              this.metric.startTime = new Date(this.report.startTime);
            }
            if (this.report.endTime) {
              this.metric.endTime = new Date(this.report.endTime);
            }
          });
        }
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
    box-shadow: 0 0 2px 0 rgba(31,31,31,0.15), 0 1px 2px 0 rgba(31,31,31,0.15);
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
