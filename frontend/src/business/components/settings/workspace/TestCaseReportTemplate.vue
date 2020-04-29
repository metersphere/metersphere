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
        <div class="testcase-template" v-for="fit in fits" :key="fit">
          <div class="template-img">
            <i class="el-icon-error"/>
          </div>
          <span class="demonstration">{{ fit }}</span>
        </div>
      </el-main>

      <test-case-report-template-edit ref="templateEdit"/>

    </el-card>

  </div>

</template>

<script>

    import MsTableHeader from "../../common/components/MsTableHeader";
    import TestCaseReportTemplateEdit from "./components/TestCaseReportTemplateEdit";
    export default {
      name: "TestCaseReportTemplate",
      components: {TestCaseReportTemplateEdit, MsTableHeader},
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
          url: '../../../../assets/template.png',
          condition: {},
        }
      },
      methods: {
        initTableData() {

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

  .testcase-template {
    display: inline-block;
    margin: 10px 30px;
    width: 150px;
  }

  .demonstration {
    display: block;
    text-align: center;
    margin: 10px auto;
    width: 150px;
    white-space:nowrap;
    overflow:hidden;
    text-overflow:ellipsis;
  }

  .template-img {
    height: 100px;
    width: 80px;
    margin: 0 auto;
    box-shadow: 0 0 2px 0 rgba(31,31,31,0.15), 0 1px 2px 0 rgba(31,31,31,0.15);
    border: solid 2px #fff;
    box-sizing: border-box;
    border-radius: 3px;
    background: url(../../../../assets/template.png) no-repeat center;
  }

  .template-img:hover {
    border: solid 1px #4b8fdf;
    border-radius: 3px;
    color: deepskyblue;
    cursor: pointer;
  }

  .template-img > i {
    display:none;
    float: right;
    color: gray;
    margin: 2px;
  }

  .template-img > i:hover {
    color: red;
  }

  .template-img:hover > .el-icon-error {
    display: inline;
  }

</style>
