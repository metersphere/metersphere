<template>

  <div v-loading="result.loading">
    <el-card>
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="initData"
                         :title="'测试报告模版'"
                         :create-tip="'新建模版'" @create="templateEdit">

        </ms-table-header>
      </template>

      <el-main>
        <testcase-template-item v-for="item in templates" :key="item.id"
                                :template="item" @templateEdit="templateEdit"/>
      </el-main>

      <test-case-report-template-edit ref="templateEdit"/>

    </el-card>

  </div>

</template>

<script>

    import MsTableHeader from "../../common/components/MsTableHeader";
    import TestCaseReportTemplateEdit from "./components/TestCaseReportTemplateEdit";
    import TestcaseTemplateItem from "./components/TestcaseTemplateItem";
    import {WORKSPACE_ID} from '../../../../common/js/constants';

    export default {
      name: "TestCaseReportTemplate",
      components: {TestcaseTemplateItem, TestCaseReportTemplateEdit, MsTableHeader},
      data() {
        return {
          result: {},
          condition: {},
          templates: []
        }
      },
      mounted() {
        this.initData();
      },
      watch: {
        '$route'(to) {
          if (to.path.indexOf("setting/testcase/report/template") >= 0) {
            this.initData();
          }
        }
      },
      methods: {
        initData() {
          this.condition.workspaceId = localStorage.getItem(WORKSPACE_ID);
          this.result = this.$post('/case/report/template/list', this.condition, response => {
            this.templates = response.data;
          });
        },
        templateCreate() {

        },
        templateEdit(template) {
          this.$refs.templateEdit.open(template);
        }
      }
    }
</script>

<style scoped>



</style>
