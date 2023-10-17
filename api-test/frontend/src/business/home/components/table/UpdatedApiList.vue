<template>
  <div style="margin: 24px" class="update-api-table">
    <span class="table-title">
      {{ $t('api_test.home_page.new_case_list.title') }}
    </span>
    <div style="margin-top: 16px" v-loading="loading" element-loading-background="#FFFFFF">
      <div
        v-show="loadError"
        style="
          width: 100%;
          height: 300px;
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
        ">
        <img style="height: 100px; width: 100px" src="/assets/module/figma/icon_load_error.svg" />
        <span class="addition-info-title" style="color: #646a73">{{ $t('home.dashboard.public.load_error') }}</span>
      </div>
      <div v-show="!loadError">
        <el-table
          border
          :data="tableData"
          class="adjust-table table-content"
          style="min-height: 228px"
          @row-click="clickRow"
          header-cell-class-name="home-table-cell"
          element-loading-background="#FFFFFF">
          <!--ID-->
          <el-table-column prop="num" :label="$t('home.new_case.index')" width="100" show-overflow-tooltip>
            <template v-slot:default="{ row }">
              {{ row.num }}
            </template>
          </el-table-column>
          <!--名称-->
          <el-table-column prop="name" :label="$t('commons.name')" min-width="200" show-overflow-tooltip>
            <template v-slot:default="{ row }">
              <span class="redirectColumn">
                {{ row.name }}
              </span>
            </template>
          </el-table-column>

          <!--状态-->
          <el-table-column prop="status" :label="$t('home.new_case.api_status')" width="100">
            <template v-slot:default="scope">
              <span class="el-dropdown-link">
                <basic-status-label :value="scope.row.status" />
              </span>
            </template>
          </el-table-column>
          <!--路径-->
          <el-table-column prop="path" :label="$t('home.new_case.path')" min-width="200" show-overflow-tooltip />
          <!--更新时间-->
          <el-table-column :label="$t('home.new_case.update_time')" width="170">
            <template v-slot:default="scope">
              {{ scope.row.updateTime | datetimeFormat }}
            </template>
          </el-table-column>
          <el-table-column prop="caseTotal" :label="$t('home.new_case.relation_case')" align="right" width="100">
            <template v-slot:default="{ row }">
              <el-link
                style="color: #783887; width: 100%"
                type="info"
                :underline="false"
                v-permission-disable="['PROJECT_API_DEFINITION:READ']"
                @click="redirectPage('api', 'apiTestCase', 'singleList:' + row.id, row.protocol)">
                <span style="float: right">
                  {{ row.caseTotal }}
                </span>
              </el-link>
            </template>
          </el-table-column>
          <el-table-column
            prop="scenarioTotal"
            :label="$t('home.new_case.relation_scenario')"
            align="right"
            width="100">
            <template v-slot:default="{ row }">
              <el-link
                style="color: #783887; width: 100%"
                type="info"
                :underline="false"
                v-permission-disable="['PROJECT_API_SCENARIO:READ']"
                @click="redirectPage('scenario', 'scenario', 'list:' + row.scenarioIds)">
                <span style="float: right">
                  {{ row.scenarioTotal }}
                </span>
              </el-link>
            </template>
          </el-table-column>

          <template #empty>
            <div
              style="
                width: 100%;
                height: 238px;
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;
              ">
              <img style="height: 100px; width: 100px; margin-bottom: 8px" src="/assets/module/figma/icon_none.svg" />
              <span class="addition-info-title">{{ $t('home.dashboard.public.no_data') }}</span>
            </div>
          </template>
        </el-table>
        <home-table-pagination
          v-if="tableData.length > 0"
          :change="search"
          :current-page.sync="currentPage"
          :page-size.sync="pageSize"
          layout="prev, pager, next, sizes"
          :total="total" />
      </div>
    </div>
  </div>
</template>

<script>
import {definitionWeekList} from '@/api/definition';
import {getCurrentProjectID} from 'metersphere-frontend/src/utils/token';
import {API_STATUS} from '@/business/definition/model/JsonData';
import ApiStatus from '@/business/definition/components/list/ApiStatus';
import HomeTablePagination from '@/business/home/components/table/HomeTablePagination';
import BasicStatusLabel from 'metersphere-frontend/src/components/BasicStatusLabel';
import {hasPermission} from 'metersphere-frontend/src/utils/permission';

export default {
  name: 'UpdatedApiList',
  components: {
    BasicStatusLabel,
    ApiStatus,
    HomeTablePagination,
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
      versionId: '',
      status: API_STATUS,
    };
  },
  methods: {
    clickRow(row, column, event) {
      if (column.property !== 'caseTotal' && column.property !== 'scenarioTotal') {
        if (!hasPermission('PROJECT_API_DEFINITION:READ')) {
          return;
        }
        this.redirectPageWithDataType('api', 'api', 'edit:' + row.id);
      }
    },
    search(versionId) {
      if (versionId) {
        this.versionId = versionId;
      }
      let projectId = getCurrentProjectID();
      this.loading = true;
      this.loadError = false;

      this.result = definitionWeekList(projectId, this.versionId, this.currentPage, this.pageSize)
        .then((response) => {
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
          this.loading = false;
          this.loadError = false;
        })
        .catch(() => {
          this.loading = false;
          this.loadError = true;
        });
    },
    redirectPage(redirectPage, dataType, selectRange, type) {
      if (!type || type === undefined) {
        this.$emit('redirectPage', redirectPage, dataType, selectRange, null, type);
      } else {
        this.$emit('redirectPageWithDataType', redirectPage, dataType, selectRange, null, type);
      }
    },
  },
};
</script>

<style scoped>
.update-api-table :deep(.el-link--inner) {
  width: 100%;
  float: left;
}
</style>
