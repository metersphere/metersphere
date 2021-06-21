<template>
  <el-card class="table-card" v-loading="result.loading" body-style="padding:10px;">
    <div slot="header">
      <span class="title">
        {{ $t('test_track.home.bug_count') }}
      </span>
    </div>
    <el-container>
      <el-aside width="150px">
        <div class="main-number-show">
          <span class="count-number">
            {{ bugTotalSize }}
          </span>
          <span style="color: #6C317C;">
            {{ $t('api_test.home_page.unit_of_measurement') }}
          </span>
          <div>
            {{ $t('test_track.home.percentage') }}
            <span class="rage">
              {{rage}}
            </span>
          </div>
        </div>
      </el-aside>
      <el-table border :data="tableData" class="adjust-table table-content" height="300">
        <el-table-column prop="index" :label="$t('test_track.home.serial_number')"
                         width="60" show-overflow-tooltip/>
        <el-table-column prop="planName" :label="$t('test_track.home.test_plan_name')"
                         width="130" show-overflow-tooltip/>
        <el-table-column prop="createTime" :label="$t('commons.create_time')" width="180" show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          column-key="status"
          :label="$t('test_track.plan.plan_status')"
          width="100"
          show-overflow-tooltip>
          <template v-slot:default="scope">
          <span @click.stop="clickt = 'stop'">
            <plan-status-table-item :value="scope.row.status"/>
          </span>
          </template>
        </el-table-column>
        <el-table-column prop="caseSize" :label="$t('test_track.home.case_size')"
                         width="80" show-overflow-tooltip/>
        <el-table-column prop="bugSize" :label="$t('test_track.home.bug_size')"
                         width="80" show-overflow-tooltip/>
        <el-table-column prop="passRage" :label="$t('test_track.home.passing_rate')"
                         width="80" show-overflow-tooltip/>
      </el-table>
    </el-container>
  </el-card>
</template>

<script>
import {getCurrentProjectID} from "@/common/js/utils";
import PlanStatusTableItem from "@/business/components/track/common/tableItems/plan/PlanStatusTableItem";

export default {
  name: "BugCountCard",
  components: {
    PlanStatusTableItem
  },
  data() {
    return {
      tableData: [],
      result: {},
      bugTotalSize: 0,
      rage: '0%'
    }
  },
  methods: {
    init() {
      this.result = this.$get("/track/bug/count/" + getCurrentProjectID(), res => {
        let data = res.data;
        this.tableData = data.list;
        this.bugTotalSize = data.bugTotalSize;
        this.rage = data.rage;
      })
    }
  },
  created() {
    this.init()
  },
  activated() {
    this.init()
  }
}
</script>

<style scoped>

.el-card /deep/ .el-card__header {
  border-bottom: 0px solid #EBEEF5;
}

.el-aside {
  line-height: 100px;
  text-align: center;
  overflow-y: hidden;
}

.count-number {
  font-family: 'ArialMT', 'Arial', sans-serif;
  font-size: 33px;
  color: var(--count_number);
}

.rage {
  font-family: 'ArialMT', 'Arial', sans-serif;
  font-size: 18px;
  color: var(--count_number);
}

.main-number-show {
  width: 100px;
  height: 100px;
  border-style: solid;
  border-width: 7px;
  border-color: var(--count_number_shallow);
  border-radius: 50%;

}

.count-number-show {
  margin: 20px auto;
}
</style>
