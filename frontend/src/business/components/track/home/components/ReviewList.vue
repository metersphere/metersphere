<template>
  <home-base-component title="我的评审" v-loading>
    <template slot="header-area">
      <div style="float: right">
        <ms-table-button :is-tester-permission="true" v-if="!showMyCreator" icon="el-icon-view"
                         content="我创建的评审" @click="searchMyCreator"/>
        <ms-table-button :is-tester-permission="true" v-if="showMyCreator" icon="el-icon-view"
                         content="待我评审" @click="searchMyCreator"/>
      </div>

    </template>
    <el-table
      class="adjust-table"
      border
      :data="tableData"
      @row-click="intoPlan"
      v-loading="result.loading">
      <el-table-column
        prop="name"
        fixed
        :label="$t('commons.name')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="creator"
        fixed
        label="创建人"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="reviewerName"
        fixed
        label="评审人"
        show-overflow-tooltip>
      </el-table-column>

      <el-table-column
        prop="status"
        :label="$t('test_track.plan.plan_status')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <plan-status-table-item :value="scope.row.status"/>
        </template>
      </el-table-column>

      <el-table-column
        prop="projectName"
        label="已评用例"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          {{scope.row.reviewed}}/{{scope.row.total}}
        </template>
      </el-table-column>

      <el-table-column
        prop="projectName"
        :label="$t('test_track.home.review_progress')"
        min-width="100"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <el-progress :percentage="scope.row.testRate"></el-progress>
        </template>
      </el-table-column>

      <el-table-column
        prop="projectName"
        :label="$t('test_track.plan.plan_project')"
        show-overflow-tooltip>
      </el-table-column>

    </el-table>

  </home-base-component>
</template>

<script>
import MsTableOperator from "../../../common/components/MsTableOperator";
import PlanStageTableItem from "../../common/tableItems/plan/PlanStageTableItem";
import PlanStatusTableItem from "../../common/tableItems/plan/PlanStatusTableItem";
import HomeBaseComponent from "./HomeBaseComponent";
import MsTableButton from "../../../common/components/MsTableButton";

export default {
  name: "ReviewList",
  components: {MsTableOperator, PlanStageTableItem, PlanStatusTableItem, HomeBaseComponent, MsTableButton},
  data() {
    return {
      result: {},
      tableData: [],
      showMyCreator: false
    }
  },
  mounted() {
    this.initTableData('');
  },
  methods: {
    initTableData(type) {
      if (!type) {
        type = 'reviewer'
      }
      this.result = this.$get('/test/case/review/list/all/relate/' + type, response => {
        this.tableData = response.data;
        for (let i = 0; i < this.tableData.length; i++) {
          let path = "/test/case/review/project";
          this.$post(path, {id: this.tableData[i].id}, res => {
            let arr = res.data;
            let projectName = arr.map(data => data.name).join("、");
            this.$set(this.tableData[i], "projectName", projectName);
          });
        }
      });
    },
    intoPlan(row) {
      this.$router.push('/track/review/view/' + row.id);
    },
    searchMyCreator() {
      this.showMyCreator = !this.showMyCreator;
      if (this.showMyCreator){
        this.initTableData("creator");
      } else {
        this.initTableData("reviewer");
      }
    }
  }
}
</script>

<style scoped>

</style>
