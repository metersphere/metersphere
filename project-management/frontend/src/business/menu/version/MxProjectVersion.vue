<template>
  <ms-container>
    <ms-main-container>
      <div v-loading="result.loading">
        <el-card>
          <template v-slot:header>
            <el-row type="flex" justify="space-between" align="middle">
              <span>
                <el-switch @change="versionEnableChange"
                           :disabled="!hasPermission('PROJECT_VERSION:READ+ENABLE')"
                           v-model="versionEnable" :active-text="$t('project.version.enable')"/>
                &nbsp;
                <ms-table-button icon="el-icon-circle-plus-outline"
                                 v-permission="['PROJECT_VERSION:READ+CREATE']"
                                 :content="$t('project.version.create_version')" @click="create"/>&nbsp;
              </span>
              <span>
                <ms-table-search-bar :condition.sync="condition" @change="initData"
                                     v-permission="['PROJECT_VERSION:READ']"
                                     class="search-bar" :tip="$t('commons.search_by_name')"/>
              </span>
            </el-row>
          </template>

          <el-table :data="tableData" border class="adjust-table" style="width: 100%"
                    :height="screenHeight"
                    @filter-change="filter"
                    @sort-change="sort">
            <el-table-column prop="name" :label="$t('project.version.name')"/>
            <el-table-column prop="status" column-key="status"
                             sortable
                             width="100"
                             :label="$t('commons.status')" :filters="statusFilters">
              <template v-slot:default="scope">
                <el-switch v-model="scope.row.status"
                           :disabled="scope.row.latest || !hasPermission('PROJECT_VERSION:READ+EDIT')"
                           inactive-color="#DCDFE6"
                           active-value="open"
                           inactive-value="closed"
                           @change="changeSwitch(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="latest" column-key="latest"
                             width="100"
                             sortable
                             :label="$t('project.version.latest')">
              <template v-slot:default="scope">
                <el-switch v-model="scope.row.latest"
                           :disabled="scope.row.latest || !hasPermission('PROJECT_VERSION:READ+EDIT')"
                           @change="changeLatest(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column :label="$t('project.version.publish_time')" prop="publishTime" sortable>
              <template v-slot:default="scope">
                <span>{{ scope.row.publishTime | datetimeFormat }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('project.version.start_time')" prop="startTime" sortable>
              <template v-slot:default="scope">
                <span>{{ scope.row.startTime | datetimeFormat }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('project.version.end_time')" prop="endTime" sortable>
              <template v-slot:default="scope">
                <span>{{ scope.row.endTime | datetimeFormat }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('commons.create_time')" prop="createTime" sortable>
              <template v-slot:default="scope">
                <span>{{ scope.row.createTime | datetimeFormat }}</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="createUser"
              :label="$t('commons.create_user')"
              :filters="userFilters"
              column-key="create_user"
              show-overflow-tooltip>
              <template v-slot:default="scope">
                <span>{{ scope.row.createUserName }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('commons.operating')" min-width="120">
              <template v-slot:default="scope">
                <div>
                  <ms-table-operator
                    :edit-permission="['PROJECT_VERSION:READ+EDIT']"
                    :delete-permission="['PROJECT_VERSION:READ+DELETE']"
                    :show-delete="!scope.row.latest"
                    @editClick="edit(scope.row)" @deleteClick="del(scope.row)">
                  </ms-table-operator>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <ms-table-pagination :change="initData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                               :total="total"/>
        </el-card>
      </div>
      <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="createVisible" destroy-on-close
                 v-loading="createLoading"
                 @close="handleClose">
        <el-form :model="form" :rules="rules" ref="form" label-position="right" label-width="90px" size="small">
          <el-form-item :label="$t('project.version.name')" prop="name">
            <el-input v-model="form.name" autocomplete="off" maxlength="50" show-word-limit/>
            <el-checkbox v-model="form.latest" :disabled="!form.setLatestAble">
              {{ $t('project.version.set_latest') }}
            </el-checkbox>
          </el-form-item>
          <el-form-item :label="$t('commons.description')" prop="description">
            <el-input v-model="form.description" type="textarea" autocomplete="off" maxlength="200" show-word-limit/>
          </el-form-item>
          <el-form-item :label="$t('project.version.publish_time')" prop="publishTime">
            <el-date-picker style="width: 100%" v-model="form.publishTime" type="datetime"
                            value-format="timestamp"></el-date-picker>
          </el-form-item>
          <el-form-item :label="$t('project_version.version_time')" prop="startTime">
            <el-date-picker
              v-model="form.versionTime"
              style="width: 100%"
              type="datetimerange"
              range-separator="-"
              value-format="timestamp"
              :start-placeholder="$t('project.version.start_time')"
              :end-placeholder="$t('project.version.end_time')">
            </el-date-picker>
          </el-form-item>
        </el-form>
        <template v-slot:footer>
          <div class="dialog-footer">
            <el-button type="primary" @click="save(false)" size="small" @keydown.enter.native.prevent>
              {{ $t('commons.save') }}
            </el-button>
            <el-button @click="save(true)" size="small" @keydown.enter.native.prevent v-permission="['PROJECT_VERSION:READ+CREATE']">
              {{ $t('test_track.case.save_create_continue') }}
            </el-button>
            <el-button @click="handleClose" size="small">{{ $t('commons.cancel') }}</el-button>
          </div>
        </template>
      </el-dialog>
      <ms-delete-confirm :title="$t('project.version.delete_version')" :with-tip="showDeleteTip" @delete="_handleDel"
                         ref="deleteConfirm">
        <template v-slot:default>
          {{ $t('project.version.delete_tip') }}
        </template>
      </ms-delete-confirm>
    </ms-main-container>
  </ms-container>
</template>

<script>
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {USER_GROUP_SCOPE} from "metersphere-frontend/src/utils/table-constants";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsDeleteConfirm from "metersphere-frontend/src/components/MsDeleteConfirm";
import {_filter, _sort} from "metersphere-frontend/src/utils/tableUtils";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {hasPermission} from "metersphere-frontend/src/utils/permission";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import MsTableSearchBar from "metersphere-frontend/src/components/MsTableSearchBar";
import {
  changeLatest,
  changeProjectVersionEnable,
  changeStatus,
  checkForDelete,
  deleteProjectVersion,
  getProjectMembers,
  getProjectVersion,
  isProjectVersionEnable,
  listProjectVersions,
  saveProjectVersion
} from "../../../api/version";

export default {
  name: "MxProjectVersion",
  components: {
    MsTableSearchBar,
    MsTableButton,
    MsDialogFooter,
    MsMainContainer,
    MsContainer,
    MsTableHeader,
    MsTableOperator,
    MsTablePagination,
    MsTableOperatorButton,
    MsDeleteConfirm
  },
  data() {
    return {
      result: {},
      condition: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      screenHeight: 'calc(100vh - 160px)',
      tableData: [],
      currentGroup: {},
      createVisible: false,
      form: {status: 'open'},
      title: this.$t('project.version.create_version'),
      statusOptions: [
        {id: 'open', name: this.$t('project.version.version_open')},
        {id: 'closed', name: this.$t('project.version.version_closed')}
      ],
      rules: {
        name: [
          {required: true, message: this.$t('project.version.please_input_version'), trigger: 'blur'},
          {max: 100, message: this.$t('test_track.length_less_than') + '100', trigger: 'blur'}
        ],
      },
      userFilters: [],
      statusFilters: [
        {text: this.$t('project.version.version_open'), value: 'open'},
        {text: this.$t('project.version.version_closed'), value: 'closed'}
      ],
      versionEnable: null,
      showDeleteTip: false,
      createLoading: false,
    };
  },
  created() {
    isProjectVersionEnable(this.projectId)
      .then(response => {
        this.versionEnable = response.data;
      })
    this.initData();
    this.getPrincipalOptions([]);
  },
  computed: {
    userGroupType() {
      return USER_GROUP_SCOPE;
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    hasPermission,
    initData() {
      this.condition.projectId = this.projectId;
      if (this.projectId) {
        this.result = listProjectVersions(this.currentPage, this.pageSize, this.condition)
          .then(res => {
            let data = res.data;
            if (data) {
              let {itemCount, listObject} = data;
              this.total = itemCount;
              this.tableData = listObject;
            }
          });
      }
    },
    getPrincipalOptions(option) {
      getProjectMembers()
        .then(response => {
          option.push(...response.data);
          this.userFilters = response.data.map(u => {
            return {text: u.name, value: u.id};
          });
        });
    },
    create() {
      this.form = {status: 'open', setLatestAble: true, latest: false};
      this.title = this.$t('project.version.create_version');
      this.createVisible = true;
    },
    edit(row) {
      this.title = this.$t('project.version.edit_version');
      getProjectVersion(row.id)
        .then(response => {
          this.form = response.data;
          this.form.setLatestAble = !response.data.latest;
          if (this.form.startTime && this.form.endTime) {
            this.form.versionTime = [this.form.startTime, this.form.endTime];
          }
          this.createVisible = true;
        });
    },
    changeSwitch(row) {
      changeStatus(row)
        .then(response => {
          this.$success(this.$t('commons.save_success'));
        });
    },
    changeLatest(row) {
      //
      if (row.latest) {
        row.status = 'open';
        changeStatus(row);
      }
      this.tableData.forEach(d => {
        d.latest = false;
      });
      row.latest = true;
      if (row.latest) {
        this.$info(this.$t('project.version.change_latest_tip'));
      }
      changeLatest(row)
        .then(response => {
          this.$success(row.latest ? this.$t('commons.enable_success') : this.$t('commons.disable_success'));
        });
    },
    save(create) {
      this.$refs['form'].validate(valid => {
        if (valid) {
          this.form.projectId = this.projectId;
          if (!this.form.id) {
            // 创建时
            if (this.form.latest) {
              this.tableData.forEach(d => {
                d.latest = false;
              });
            }
          }
          if (this.form.versionTime) {
            this.form.startTime = this.form.versionTime[0];
            this.form.endTime = this.form.versionTime[1];
          }
          this.createLoading = saveProjectVersion(this.form)
            .then(resp => {
              this.$success(this.$t('commons.save_success'));
              this.initData();
              if (create) {
                this.create();
              } else {
                this.createVisible = false;
              }
            })
        } else {
          return false;
        }
      });
    },
    handleClose() {
      this.createVisible = false;
    },
    _handleDel(row) {
      this.result = deleteProjectVersion(row.id)
        .then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.initData();
        });
    },
    del(row) {
      this.showDeleteTip = false;
      this.$refs.deleteConfirm.open(row);
      checkForDelete(row.id)
        .then(response => {
          this.showDeleteTip = response.data;
        });
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.initData();
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initData();
    },
    versionEnableChange() {
      changeProjectVersionEnable(this.versionEnable, this.projectId);
    }
  }
};
</script>

<style scoped>

</style>
