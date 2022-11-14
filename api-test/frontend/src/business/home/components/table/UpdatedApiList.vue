<template>
  <div style="margin: 24px">
    <span class="table-title">
      {{ $t('api_test.home_page.new_case_list.title') }}
    </span>
    <div style="margin-top: 16px;" v-loading="loading" element-loading-background="#FFFFFF">
      <div v-show="loadError"
           style="width: 100%; height: 300px; display: flex; flex-direction: column;     justify-content: center;align-items: center">
        <img style="height: 100px;width: 100px;"
             src="/assets/figma/icon_load.svg"/>
        <span class="addition-info-title" style="color: #646A73">{{ $t("home.dashboard.public.load_error") }}</span>
      </div>
      <div v-show="!loadError">
        <el-table border :data="tableData" class="adjust-table table-content" height="233px"
                  header-cell-class-name="home-table-cell"
                  v-loading="loading" element-loading-background="#FFFFFF">
          <!--ID-->
          <el-table-column prop="num" :label="$t('home.new_case.index')" width="100"
                           show-overflow-tooltip>
            <template v-slot:default="{row}">
              <el-link type="info" @click="redirectPage('api','api','edit:' + row.id)">
                {{ row.num }}
              </el-link>
            </template>
          </el-table-column>
          <!--名称-->
          <el-table-column prop="name" :label="$t('commons.name')"
                           show-overflow-tooltip min-width="170">
            <template v-slot:default="{row}">
              <el-link type="info" @click="redirectPage('api','api','edit:' + row.id)">
                {{ row.name }}
              </el-link>
            </template>
          </el-table-column>
          <!--路径-->
          <el-table-column prop="path" :label="$t('home.new_case.path')" min-width="260"
                           show-overflow-tooltip>
          </el-table-column>
          <!--状态-->
          <el-table-column prop="status" :label="$t('home.new_case.api_status')" width="100">
            <template v-slot:default="scope">
              <span class="el-dropdown-link">
                <basic-status-label :value="scope.row.status"/>
              </span>
            </template>
          </el-table-column>
          <!--更新时间-->
          <el-table-column :label="$t('home.new_case.update_time')" min-width="170">
            <template v-slot:default="scope">
              {{ scope.row.updateTime | datetimeFormat }}
            </template>
          </el-table-column>
          <el-table-column prop="caseTotal" :label="$t('home.new_case.relation_case')"
                           width="100">
            <template v-slot:default="{row}">
              <el-link style="color: #783887;width: 100%;" type="info" :underline="false"
                       @click="redirectPage( 'api', 'apiTestCase', 'singleList:' + row.id)">
                {{ row.caseTotal }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="scenarioTotal"
                           :label="$t('home.new_case.relation_scenario')"
                           width="100">
            <template v-slot:default="{row}">
              <el-link style="color: #783887;width: 100%;" type="info"
                       @click="redirectPage('scenario', 'scenario','list:' +row.scenarioIds)">
                {{ row.scenarioTotal }}
              </el-link>
            </template>
          </el-table-column>

          <template #empty>
            <div
              style="width: 100%; height: 144px;display: flex; flex-direction: column;     justify-content: center;align-items: center">
              <img style="height: 100px;width: 100px;margin-bottom: 8px"
                   src="/assets/figma/icon_none.svg"/>
              <span class="addition-info-title">{{ $t("home.dashboard.public.no_data") }}</span>
            </div>
          </template>
        </el-table>
        <home-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                               layout="prev, pager, next"
                               :total="total"/>
      </div>
    </div>
  </div>
</template>

<script>
import {definitionWeekList} from "@/api/definition";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {API_STATUS} from "@/business/definition/model/JsonData";
import ApiStatus from "@/business/definition/components/list/ApiStatus";
import HomeTablePagination from "@/business/home/components/table/HomeTablePagination";
import BasicStatusLabel from "@/business/home/components/BasicStatusLabel";
import BasicStatus from "@/business/home/components/BasicStatusLabel";

export default {
  name: "UpdatedApiList",
  components: {
    BasicStatus,
    BasicStatusLabel, ApiStatus, HomeTablePagination
  },
  data() {
    return {
      result: false,
      loadError: false,
      loading: false,
      tableData: [],
      currentPage: 1,
      pageSize: 5,
      total: 0,
      status: API_STATUS,
    }
  },
  activated() {
    this.search();
  },
  methods: {
    search() {
      let projectId = getCurrentProjectID();
      this.loading = true;
      this.loadError = false;

      this.result = definitionWeekList(projectId, this.currentPage, this.pageSize).then(response => {
        this.total = response.data.itemCount;
        this.tableData = response.data.listObject;
        this.loading = false;
        this.loadError = false;
      }).catch(() => {
        this.loading = false;
        this.loadError = true;
      });
    },
    redirectPage(redirectPage, dataType, selectRange) {
      this.$emit('redirectPage', redirectPage, dataType, selectRange, null);
    }
  }
}
</script>

<style scoped>
.table-title {
  color: #1F2329;
  font-weight: 500;
  font-size: 18px;
  line-height: 26px;
}

.adjust-table :deep(.el-table__row), .adjust-table :deep(.el-table__row .el-link) {
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  color: #1F2329;
}

.adjust-table :deep(.el-link--inner) {
  width: 100%;
  float: left;
}

</style>
