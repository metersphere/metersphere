<template>

  <el-dialog :title="'选择模版'"
             :visible.sync="templateVisible"
             width="50%">

    <el-main>
      <testcase-template-item v-for="item in templates" :key="item.id"
                              :template="item" @templateEdit="createTemplate" @refresh="initData"/>
    </el-main>

  </el-dialog>
</template>

<script>
    import TestcaseTemplateItem from "../../../../settings/workspace/components/TestcaseTemplateItem";
    import {WORKSPACE_ID} from "../../../../../../common/js/constants";
    export default {
      name: "TestReportTemplateList",
      components: {TestcaseTemplateItem},
      data() {
        return {
          templateVisible: false,
          templates: []
        }
      },
      props: {
        planId: {
          type: String
        }
      },
      methods: {
        initData() {
          let condition = {};
          condition.queryDefault = true;
          condition.workspaceId = localStorage.getItem(WORKSPACE_ID);
          this.result = this.$post('/case/report/template/list', condition, response => {
            this.templates = response.data;
          });
        },
        createTemplate(templateId) {
          let param = {};
          param.planId = this.planId;
          param.templateId = templateId;
          this.$post('/case/report/add', param, response => {
            this.$emit('openReport', response.data);
            this.templateVisible = false;
          });
        },
        open() {
          this.templateVisible = true;
          this.initData();
        }

      }
    }
</script>

<style scoped>

  .el-main >>> .testcase-template:hover i{
    display: none;
  }

</style>
