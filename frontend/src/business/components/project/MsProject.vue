<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card" v-loading="result.loading">
        <template v-slot:header>
          <ms-table-header :is-tester-permission="true" :condition.sync="condition" @search="search" @create="create"
                           :create-tip="btnTips" :title="title"/>
        </template>
        <el-table border class="adjust-table" @row-click="link" :data="items" style="width: 100%" @sort-change="sort">
          <el-table-column prop="name" :label="$t('commons.name')" width="250" show-overflow-tooltip/>
          <el-table-column prop="description" :label="$t('commons.description')" show-overflow-tooltip>
            <template v-slot:default="scope">
              <pre>{{ scope.row.description }}</pre>
            </template>
          </el-table-column>
          <!--<el-table-column prop="workspaceName" :label="$t('project.owning_workspace')"/>-->
          <el-table-column
            sortable
            prop="createTime"
            :label="$t('commons.create_time')"
            show-overflow-tooltip>
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            sortable
            prop="updateTime"
            :label="$t('commons.update_time')"
            show-overflow-tooltip>
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <ms-table-operator :is-tester-permission="true" @editClick="edit(scope.row)"
                                 @deleteClick="handleDelete(scope.row)">
                <template v-if="baseUrl == 'api'" v-slot:behind>
                  <ms-table-operator-button :is-tester-permission="true" :tip="'环境配置'" icon="el-icon-setting"
                                            type="info" @exec="openEnvironmentConfig(scope.row)"/>
                </template>
              </ms-table-operator>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="list" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
    </ms-main-container>

    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="createVisible" destroy-on-close @close="handleClose">
      <el-form :model="form" :rules="rules" ref="form" label-position="right" label-width="100px" size="small">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input :autosize="{ minRows: 2, maxRows: 4}" type="textarea" v-model="form.description"></el-input>
        </el-form-item>
        <el-form-item :label="$t('project.tapd_id')" v-if="tapd">
          <el-input v-model="form.tapdId" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item :label="$t('project.jira_key')" v-if="jira">
          <el-input v-model="form.jiraKey" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <div class="dialog-footer">
          <ms-dialog-footer
            @cancel="createVisible = false"
            @confirm="submit('form')"/>
        </div>
      </template>
    </el-dialog>

    <ms-delete-confirm :title="$t('project.delete')" @delete="_handleDelete" ref="deleteConfirm"/>

    <api-environment-config ref="environmentConfig"/>

  </ms-container>
</template>

<script>
import MsCreateBox from "../settings/CreateBox";
import {Message} from "element-ui";
import MsTablePagination from "../common/pagination/TablePagination";
import MsTableHeader from "../common/components/MsTableHeader";
import MsTableOperator from "../common/components/MsTableOperator";
import MsDialogFooter from "../common/components/MsDialogFooter";
import {_sort, getCurrentUser, listenGoBack, removeGoBackListener} from "@/common/js/utils";
import MsContainer from "../common/components/MsContainer";
import MsMainContainer from "../common/components/MsMainContainer";
import MsDeleteConfirm from "../common/components/MsDeleteConfirm";
import MsTableOperatorButton from "../common/components/MsTableOperatorButton";
import ApiEnvironmentConfig from "../api/test/components/ApiEnvironmentConfig";
import TemplateComponent from "../track/plan/view/comonents/report/TemplateComponent/TemplateComponent";
import {ApiEvent, LIST_CHANGE, PerformanceEvent, TrackEvent} from "@/business/components/common/head/ListEvent";

export default {
  name: "MsProject",
  components: {
    TemplateComponent,
    ApiEnvironmentConfig,
    MsTableOperatorButton,
    MsDeleteConfirm,
    MsMainContainer,
    MsContainer, MsTableOperator, MsCreateBox, MsTablePagination, MsTableHeader, MsDialogFooter
  },
  data() {
    return {
      createVisible: false,
      result: {},
      btnTips: this.$t('project.create'),
      title: this.$t('project.create'),
      condition: {},
      items: [],
      tapd: false,
      jira: false,
      form: {},
      currentPage: 1,
      pageSize: 5,
      total: 0,
      rules: {
        name: [
          {required: true, message: this.$t('project.input_name'), trigger: 'blur'},
          {min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur'}
        ],
        description: [
          {max: 500, message: this.$t('commons.input_limit', [0, 500]), trigger: 'blur'}
        ],
      },
    }
  },
  props: {
    baseUrl: {
      type: String
    }
  },
  mounted() {
    if (this.$route.path.split('/')[2] === 'project' &&
      this.$route.path.split('/')[3] === 'create') {
      this.create();
      this.$router.push('/' + this.baseUrl + '/project/all');
    }
    this.list();
  },
  activated() {
    this.list();
  },
  watch: {
    '$route'(to) {
      if (this.$route.path.split('/')[2] === 'project' &&
        to.path.split('/')[3] === 'create') {
        this.create();
        this.$router.push('/' + this.baseUrl + '/project/all');
      } else if (this.$route.path.split('/')[2] === 'project' &&
        to.path.split('/')[3] === 'all') {
        this.list();
      }
    }
  },
  computed: {
    currentUser: () => {
      return getCurrentUser();
    }
  },
  destroyed() {
    this.createVisible = false;
  },
  methods: {
    create() {
      let workspaceId = this.currentUser.lastWorkspaceId;
      if (!workspaceId) {
        this.$warning(this.$t('project.please_choose_workspace'));
        return false;
      }
      this.title = this.$t('project.create');
      listenGoBack(this.handleClose);
      this.createVisible = true;
      this.form = {};
    },
    edit(row) {
      this.title = this.$t('project.edit');
      this.createVisible = true;
      listenGoBack(this.handleClose);
      this.form = Object.assign({}, row);
      if (this.baseUrl === 'track') {
        this.$get("/service/integration/all/" + getCurrentUser().lastOrganizationId, response => {
          let data = response.data;
          let platforms = data.map(d => d.platform);
          if (platforms.indexOf("Tapd") !== -1) {
            this.tapd = true;
          }
          if (platforms.indexOf("Jira") !== -1) {
            this.jira = true;
          }
        });
      }
    },
    submit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          let saveType = "add";
          if (this.form.id) {
            saveType = "update"
          }
          this.result = this.$post("/project/" + saveType, this.form, () => {
            this.createVisible = false;
            this.list();
            Message.success(this.$t('commons.save_success'));
          });
        } else {
          return false;
        }
      });
    },
    handleDelete(project) {
      this.$refs.deleteConfirm.open(project);
    },
    _handleDelete(project) {
      this.$confirm(this.$t('project.delete_tip'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.$get('/project/delete/' + project.id, () => {
          Message.success(this.$t('commons.delete_success'));
          this.list();
          // 发送广播，刷新 head 上的最新列表
          ApiEvent.$emit(LIST_CHANGE);
          TrackEvent.$emit(LIST_CHANGE);
          PerformanceEvent.$emit(LIST_CHANGE);
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: this.$t('commons.delete_cancelled')
        });
      });
    },
    handleClose() {
      removeGoBackListener(this.handleClose);
      this.createVisible = false;
    },
    search() {
      this.list();
    },
    list() {
      let url = "/project/list/" + this.currentPage + '/' + this.pageSize;
      this.result = this.$post(url, this.condition, (response) => {
        let data = response.data;
        this.items = data.listObject;
        this.total = data.itemCount;
      })
    },
    link(row) {
      // performance_test project link
      if (this.$route.name === 'perProject') {
        this.$router.push({
          path: '/performance/test/' + row.id,
        })
      } else if (this.$route.name === 'fucProject') {
        this.$router.push({
          path: '/api/test/list/' + row.id
        })
      } else if (this.$route.name === 'trackProject') {
        this.$router.push({
          path: '/track/case/' + row.id
        })
      }
    },
    sort(column) {
      _sort(column, this.condition);
      this.list();
    },
    openEnvironmentConfig(project) {
      this.$refs.environmentConfig.open(project.id);
    }
  }
}
</script>

<style scoped>

.el-table {
  cursor: pointer;
}

  pre {
    margin: 0 0;
  }

</style>
