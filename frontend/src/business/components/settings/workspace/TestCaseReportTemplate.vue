<template>

  <div v-loading="result.loading">
    <el-card>
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="initTableData"
                         :title="'测试报告模版'"
                         :create-tip="'新建模版'" @create="templateEdit">

        </ms-table-header>
      </template>

      <el-main>
        <testcase-template-item v-for="fit in fits" :key="fit" :name="fit"/>
      </el-main>

      <test-case-report-template-edit ref="templateEdit"/>

    </el-card>

  </div>

</template>

<script>

    import MsTableHeader from "../../common/components/MsTableHeader";
    import TestCaseReportTemplateEdit from "./components/TestCaseReportTemplateEdit";
    import TestcaseTemplateItem from "./components/TestcaseTemplateItem";
    export default {
      name: "TestCaseReportTemplate",
      components: {TestcaseTemplateItem, TestCaseReportTemplateEdit, MsTableHeader},
      data() {
        return {
          result: {},
          fits: ['默认模版', 'congewtain', 'cogewver', 'nongwee', 'scale-downddddddddddd',
            'faill', 'cdontain', 'codver', 'nodne', 'scalde-downddddddddddd',
            'fill', 'cowntain', 'corwver',
            'nonewbe',
            'scalev-downddddddddddd',
            'filelv', 'sontwain', 'cosgewver', 'nodegne', 'scale-dfownddddddddddd',
          ],
          condition: {},
        }
      },
      methods: {
        initData() {

          // this.$get()

        },
        templateCreate() {

        },
        templateEdit() {
          this.$refs.templateEdit.open();
        }
      }
    }
</script>

<style scoped>



</style>
