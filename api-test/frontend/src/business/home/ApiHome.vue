<template>
  <div style="background-color: #f5f6f7; overflow: auto">
    <ms-container>
      <ms-main-container style="padding: 0px">
        <div class="api-home-layout">
          <el-row v-if="showVersionSelector" class="api-home-toolbar">
            <el-select
              clearable
              v-model="versionId"
              :placeholder="$t('home.dashboard.public.default_version')"
              size="small"
              style="height: 100%">
              <el-option v-for="item in versions" :key="item.id" :label="item.name" :value="item.id"></el-option>
            </el-select>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <api-dashboard
                @redirectPage="redirectPage"
                @redirectPageWithDataType="redirectPageWithDataType"
                ref="apiDashboard" />
            </el-col>
            <el-col :span="12">
              <api-case-dashboard @redirectPage="redirectPage" ref="apiCaseDashboard" />
            </el-col>
          </el-row>
          <el-row :gutter="16" style="margin-top: 16px">
            <el-col :span="12">
              <scenario-dashboard @redirectPage="redirectPage" ref="scenarioDashboard" />
            </el-col>
            <el-col :span="12">
              <scenario-schedule-dashboard @redirectPage="redirectPage" ref="scenarioScheduleDashboard" />
            </el-col>
          </el-row>
          <el-row style="margin-top: 16px">
            <el-col style="background-color: #ffffff">
              <updated-api-list ref="updatedApiList" @redirectPage="redirectPage"
                                @redirectPageWithDataType="redirectPageWithDataType"/>
            </el-col>
          </el-row>
          <el-row style="margin-top: 16px">
            <el-col style="background-color: #ffffff">
              <schedule-task-list @redirectPage="redirectPage" ref="scheduleTaskList" />
            </el-col>
          </el-row>
        </div>
      </ms-main-container>
    </ms-container>
  </div>
</template>

<script>
import {getUUID} from 'metersphere-frontend/src/utils';
import MsContainer from 'metersphere-frontend/src/components/MsContainer';
import MsMainContainer from 'metersphere-frontend/src/components/MsMainContainer';
import ApiDashboard from '@/business/home/components/dashboard/ApiDashboard';
import ApiCaseDashboard from '@/business/home/components/dashboard/ApiCaseDashboard';
import ScenarioDashboard from '@/business/home/components/dashboard/ScenarioDashboard';
import ScenarioScheduleDashboard from '@/business/home/components/dashboard/ScenarioScheduleDashboard';
import UpdatedApiList from '@/business/home/components/table/UpdatedApiList';
import ScheduleTaskList from '@/business/home/components/table/ScheduleTaskList';
import {getProjectVersions} from '@/api/xpack';
import {getCurrentProjectID} from 'metersphere-frontend/src/utils/token';
import {hasLicense} from 'metersphere-frontend/src/utils/permission';

export default {
  name: 'ApiHome',

  components: {
    ApiDashboard,
    ApiCaseDashboard,
    MsContainer,
    MsMainContainer,
    ScenarioDashboard,
    ScenarioScheduleDashboard,
    UpdatedApiList,
    ScheduleTaskList,
  },

  data() {
    return {
      versionId: '',
      projectId: getCurrentProjectID(),
      versions: [],
      showVersionSelector: false,
    };
  },
  activated() {
    this.initVersions();
    this.$nextTick(() => {
      this.refreshAllCard();
    });
    this.showVersionSelector = hasLicense();
  },
  watch: {
    versionId() {
      this.refreshAllCard();
    },
  },
  methods: {
    refreshAllCard() {
      let selectVersionId = this.versionId;
      if (!selectVersionId || selectVersionId === '') {
        selectVersionId = 'default';
      }

      if (this.$refs.apiDashboard) {
        this.$refs.apiDashboard.search(selectVersionId);
      }
      if (this.$refs.apiCaseDashboard) {
        this.$refs.apiCaseDashboard.search(selectVersionId);
      }
      if (this.$refs.scenarioDashboard) {
        this.$refs.scenarioDashboard.search(selectVersionId);
      }
      if (this.$refs.scenarioScheduleDashboard) {
        this.$refs.scenarioScheduleDashboard.search(selectVersionId);
      }
      if (this.$refs.updatedApiList) {
        this.$refs.updatedApiList.search(selectVersionId);
      }
      if (this.$refs.scheduleTaskList) {
        this.$refs.scheduleTaskList.search(selectVersionId);
      }
    },
    initVersions() {
      if (hasLicense()) {
        getProjectVersions(getCurrentProjectID()).then((response) => {
          if (response.data) {
            this.versions = response.data;
          } else {
            this.versions = [];
          }
        });
      }
    },

    /**
     *
     * @param redirectPage 要跳转的页面
     * @param dataType     要查询的数据类型
     * @param selectRange  查询范围 （比如成功的、失败的、未执行的、已执行的等等）
     * @param selectParam 查询参数 (比如ID)
     */
    redirectPage(redirectPage, dataType, selectRange, selectParam) {
      //传入UUID是为了进行页面重新加载判断
      let uuid = getUUID();
      let home;

      let selectVersionId = this.versionId;
      if (!selectVersionId || selectVersionId === '') {
        selectVersionId = 'default';
      }

      switch (redirectPage) {
        case 'api':
          home = this.$router.resolve({
            name: 'ApiDefinitionWithQuery',
            params: {
              versionId: selectVersionId,
              redirectID: uuid,
              dataType: dataType,
              dataSelectRange: selectRange,
            },
          });
          break;
        case 'scenario':
          home = this.$router.resolve({
            name: 'ApiAutomationWithQuery',
            params: {
              versionId: selectVersionId,
              redirectID: uuid,
              dataType: dataType,
              dataSelectRange: selectRange,
            },
          });
          break;
        case 'scenarioReport':
          home = this.$router.resolve({
            name: 'ApiReportListWithQuery',
            params: {
              versionId: selectVersionId,
              redirectID: uuid,
              dataType: dataType,
              dataSelectRange: selectRange,
            },
          });
          break;
        case 'testPlanEdit':
          home = this.$router.resolve('/track/plan/view/' + selectParam);
          break;
      }
      if (home) {
        window.open(home.href, '_blank');
      }
    },

    redirectPageWithDataType(redirectPage, dataType, selectRange, selectParam, type) {
      let uuid = getUUID();
      let home;
      let selectVersionId = this.versionId;
      if (!selectVersionId || selectVersionId === '') {
        selectVersionId = 'default';
      }

      switch (redirectPage) {
        case 'api':
          home = this.$router.resolve({
            name: 'ApiDefinitionWithQuery',
            params: {
              versionId: selectVersionId,
              redirectID: uuid,
              dataType: dataType,
              dataSelectRange: selectRange,
              projectId: this.projectId,
              type: type,
            },
          });
          break;
      }
      if (home) {
        window.open(home.href, '_blank');
      }
    },
  },
};
</script>

<style scoped>
.api-home-toolbar {
  margin-bottom: 16px;
}

.api-home-toolbar :deep(.el-input__inner) {
  border: 1px solid #bbbfc4;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
}

.api-home-toolbar :deep(.el-select__caret el-input__icon el-icon-arrow-up) {
  color: red;
}

.api-home-toolbar :deep(::-webkit-input-placeholder) {
  font-size: 14px;
  font-weight: 400;
  color: #8f959e;
  line-height: 22px;
}

:deep(.el-card__header) {
  border: 0px;
  padding: 24px;
}

:deep(.el-card__body) {
  border: 0px;
  padding: 0px;
  margin: 0px 24px 24px 24px;
}

.api-home-layout {
  margin: 16px 24px;
  min-width: 1100px;
}

.api-home-layout :deep(.dashboard-title) {
  font-size: 18px;
  font-weight: 500;
  color: #1f2329;
}

.api-home-layout :deep(.common-amount) {
  margin-top: 4px;
}

.api-home-layout :deep(.dashboard-card) {
  height: 408px;
}

.api-home-layout :deep(.main-info) {
  height: 208px;
}

.api-home-layout :deep(.main-info-card) {
  height: 208px;
  width: 100%;
  color: #646a73;
  background-color: #ffffff;
  box-sizing: border-box;
  border: 1px solid #dee0e3;
  border-radius: 4px;
}

.api-home-layout :deep(.addition-info) {
  height: 86px;
  margin: 16px 0px 0px 0px;
}

.api-home-layout :deep(.addition-info-title) {
  line-height: 22px;
  font-size: 14px;
  font-weight: 400;
  color: #646a73;
}

.api-home-layout :deep(.addition-info-text) {
  line-height: 28px;
  color: #783887;
  font-size: 20px;
  font-weight: 500;
}

.api-home-layout :deep(.addition-info-num) {
  line-height: 22px;
  color: #783887;
  font-size: 14px;
  font-weight: 500;
}

.api-home-layout :deep(.home-table-cell) {
  height: 40px;
  background-color: #f5f6f7;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid rgba(31, 35, 41, 0.15);
  border-right-width: 0;
  border-left-width: 0;
  color: #646a73;
  line-height: 22px;
}

.api-home-layout :deep(.el-card) {
  border: 0;
}

.api-home-layout :deep(.table-title) {
  color: #1f2329;
  font-weight: 500;
  font-size: 18px;
  line-height: 26px;
}

.api-home-layout :deep(.el-table__row),
.adjust-table :deep(.el-table__row .el-link) {
  height: 40px;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  color: #1f2329;
}

.api-home-layout :deep(.el-table__body tr:hover) {
  cursor: pointer;
}

.api-home-layout :deep(.el-table .cell) {
  padding-left: 12px;
  padding-right: 12px;
}
</style>
