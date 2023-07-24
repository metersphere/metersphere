<template>
  <div v-loading="loading">
    <el-row>
      <el-col>
        <el-button icon="el-icon-circle-plus-outline" plain size="mini" @click="create"
                   v-permission="['SYSTEM_SETTING:READ+EDIT']">
          {{ $t('commons.add') }}
        </el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <el-table border class="adjust-table" :data="items" style="width: 100%">
          <el-table-column prop="name" :label="$t('commons.name')"/>
          <el-table-column prop="description" :label="$t('commons.description')"/>
          <el-table-column prop="type" :label="$t('test_resource_pool.type')">
            <template v-slot="scope">
              <span v-if="scope.row.type === 'CAS'">CAS</span>
              <span v-if="scope.row.type === 'OIDC'">OIDC</span>
              <span v-if="scope.row.type === 'OAuth2'">OAuth2</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="$t('test_resource_pool.enable_disable')">
            <template v-slot="scope">
              <el-switch v-model="scope.row.status"
                         inactive-color="#DCDFE6"
                         active-value="ENABLE"
                         inactive-value="DISABLED"
                         @change="changeSwitch(scope.row)"
                         :disabled="disabled"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" :label="$t('commons.create_time')" width="180">
            <template v-slot="scope">
              <span>{{ scope.row.createTime | datetimeFormat }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" :label="$t('commons.update_time')" width="180">
            <template v-slot="scope">
              <span>{{ scope.row.updateTime | datetimeFormat }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('commons.operating')">
            <template v-slot="scope">
              <ms-table-operator @editClick="edit(scope.row)" @deleteClick="del(scope.row)"
                                 :delete-permission="['SYSTEM_SETTING:READ+EDIT']"
                                 :edit-permission="['SYSTEM_SETTING:READ+EDIT']"/>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-col>
    </el-row>

    <el-dialog
      :close-on-click-modal="false"
      :title="form.id ? $t('commons.edit') : $t('commons.add')"
      :visible.sync="dialogVisible" width="70%"
      @closed="closeFunc"
      :destroy-on-close="true"
      v-loading="dialogLoading"
    >
      <el-form :model="form" label-position="right" label-width="140px" size="small" :rules="rule"
               ref="authSourceForm">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input v-model="form.description" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('test_resource_pool.type')" prop="type">
          <el-select v-model="form.type" :placeholder="$t('test_resource_pool.select_pool_type')"
                     @change="changeAuthType(form.type)">
            <el-option key="CAS" value="CAS" label="CAS"/>
            <el-option key="OIDC" value="OIDC" label="OIDC"/>
            <el-option key="OAuth2" value="OAuth2" label="OAuth2"/>
          </el-select>
        </el-form-item>

        <div class="node-line" v-if="form.type === 'CAS'">
          <el-row>
            <el-col>
              <el-form-item label="CAS URL" :rules="requiredRules" prop="configuration.casUrl">
                <el-input v-model="form.configuration.casUrl" placeholder="eg: http://<casurl>" show-word-limit
                          maxlength="200"/>
              </el-form-item>
              <el-form-item label="Login URL" :rules="requiredRules" prop="configuration.loginUrl">
                <el-input v-model="form.configuration.loginUrl" placeholder="eg: http://<casurl>/login" show-word-limit
                          maxlength="200"/>
              </el-form-item>
              <el-form-item label="Validate URL" :rules="requiredRules" prop="configuration.validateUrl">
                <el-input v-model="form.configuration.validateUrl" placeholder="eg: http://<casurl>/serviceValidate"
                          show-word-limit maxlength="200"/>
              </el-form-item>
              <el-form-item :rules="requiredRules" prop="configuration.redirectUrl">
                <template v-slot:label>
                  Redirect URL
                  <el-tooltip content="Logout redirect URL: http://<metersphere-endpoint>/sso/callback/cas/logout"
                              effect="light"
                              trigger="hover">
                    <i class="el-icon-info"></i>
                  </el-tooltip>
                </template>
                <el-input v-model="form.configuration.redirectUrl"
                          placeholder="eg: http://<metersphere-endpoint>/sso/callback/cas/${authId}" show-word-limit
                          maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <div class="node-line" v-if="form.type === 'OIDC'">
          <el-row>
            <el-col>
              <el-form-item label="Auth Endpoint"
                            :rules="requiredRules" prop="configuration.authUrl">
                <el-input v-model="form.configuration.authUrl"
                          placeholder="eg: http://<keycloak>/auth/realms/<metersphere>/protocol/openid-connect/auth"
                          show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item label="Token Endpoint"
                            :rules="requiredRules" prop="configuration.tokenUrl">
                <el-input v-model="form.configuration.tokenUrl"
                          placeholder="eg: http://<keycloak>/auth/realms/<metersphere>/protocol/openid-connect/token"
                          show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item label="Userinfo Endpoint"
                            :rules="requiredRules" prop="configuration.userInfoUrl">
                <el-input v-model="form.configuration.userInfoUrl"
                          placeholder="eg: http://<keycloak>/auth/realms/<metersphere>/protocol/openid-connect/userinfo"
                          show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item :rules="requiredRules" prop="configuration.logoutUrl">
                <template v-slot:label>
                  Logout Endpoint
                  <el-tooltip content="Logout redirect URL: http://<metersphere-endpoint>/sso/callback/logout"
                              effect="light"
                              trigger="hover">
                    <i class="el-icon-info"></i>
                  </el-tooltip>
                </template>
                <el-input v-model="form.configuration.logoutUrl"
                          placeholder="eg: http://<keycloak>/auth/realms/<metersphere>/protocol/openid-connect/logout"
                          show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item label="Client ID" prop="configuration.clientId"
                            :rules="requiredRules">
                <el-input v-model="form.configuration.clientId" placeholder="eg: metersphere"
                          show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item label="Redirect URL" prop="configuration.redirectUrl"
                            :rules="requiredRules">
                <el-input v-model="form.configuration.redirectUrl"
                          placeholder="eg: http://<metersphere-endpoint>/sso/callback or http://<metersphere-endpoint>/sso/callback/${authId}"
                          show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item prop="configuration.loginUrl">
                <template v-slot:label>
                  Login Endpoint
                  <el-tooltip content="Login URL: https://cloud2.metersphere.com, 输入后将自动转到第三方登录页面"
                              effect="light"
                              trigger="hover">
                    <i class="el-icon-info"></i>
                  </el-tooltip>
                </template>
                <el-input v-model="form.configuration.loginUrl" show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item label="Secret"
                            :rules="requiredRules" prop="configuration.secret">
                <el-input type="password" v-model="form.configuration.secret" show-password autocomplete="new-password"
                          placeholder="OIDC client secret" show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <div class="node-line" v-if="form.type === 'OAuth2'">
          <el-row>
            <el-col>
              <el-form-item label="Auth Endpoint" prop="configuration.authUrl"
                            :rules="requiredRules">
                <el-input v-model="form.configuration.authUrl"
                          placeholder="eg: http://example.com/login/oauth/authorize" show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item label="Token Endpoint"
                            :rules="requiredRules" prop="configuration.tokenUrl">
                <el-input v-model="form.configuration.tokenUrl"
                          placeholder="eg: https://example.com/login/oauth/access_token"
                          show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item label="Userinfo Endpoint"
                            :rules="requiredRules" prop="configuration.userInfoUrl">
                <el-input v-model="form.configuration.userInfoUrl"
                          placeholder="eg: https://example.com/user"
                          show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item label="Redirect URL"
                            :rules="requiredRules" prop="configuration.redirectUrl">
                <el-input v-model="form.configuration.redirectUrl"
                          placeholder="eg: http://<metersphere-endpoint>/sso/callback/oauth2"
                          show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item label="Client ID" prop="configuration.clientId"
                            :rules="requiredRules">
                <el-input v-model="form.configuration.clientId" placeholder="eg: metersphere"
                          show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item label="Secret" prop="configuration.secret"
                            :rules="requiredRules">
                <el-input type="password" v-model="form.configuration.secret" show-password autocomplete="new-password"
                          placeholder="oauth2 client secret" show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item label="Scope" prop="configuration.scope">
                <el-input v-model="form.configuration.scope" show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item label="Property Mapping" :rules="requiredRules" prop="configuration.mapping">
                <el-input v-model="form.configuration.mapping"
                          :placeholder="mappingTip" show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item label="Logout Endpoint">
                <el-input v-model="form.configuration.logoutUrl" show-word-limit maxlength="200"/>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          v-if="form.id"
          @cancel="dialogVisible = false"
          @confirm="updateAuthSource()"/>
        <ms-dialog-footer
          v-else
          @cancel="dialogVisible = false"
          @confirm="createAuthSource()"/>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import {hasPermission} from "metersphere-frontend/src/utils/permission";
import {addAuth, changeStatus, delAuth, getAllEnable, getAuth, searchAuths, updateAuth} from "../../../api/auth";

export default {
  name: "MxAuth",
  components: {MsTablePagination, MsTableHeader, MsTableOperator, MsDialogFooter},
  data() {
    return {
      result: {},
      loading: false,
      dialogLoading: false,
      dialogVisible: false,
      infoList: [],
      mappingTip: '{"userid": "uid", "username": "name", "email": "email"}',
      queryPath: "authsource/list",
      condition: {},
      items: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      form: {configuration: {}},
      requiredRules: [{required: true, message: this.$t('test_resource_pool.fill_the_data'), trigger: ['blur']}],
      rule: {
        name: [
          {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
          {min: 2, max: 20, message: this.$t('commons.input_limit', [2, 20]), trigger: 'blur'},
          {
            required: true,
            pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9.·-]+$/,
            message: this.$t('auth_source.auth_name_valid'),
            trigger: 'blur'
          }
        ],
        description: [
          {max: 60, message: this.$t('commons.input_limit', [0, 60]), trigger: 'blur'}
        ],
        type: [
          {required: true, message: this.$t('test_resource_pool.select_pool_type'), trigger: 'blur'}
        ]
      }
    };
  },
  mounted() {
    this.initTableData();
  },
  computed: {
    disabled() {
      return !hasPermission('SYSTEM_SETTING:READ+EDIT');
    }
  },
  methods: {
    initTableData() {
      this.loading = searchAuths(this.condition, this.currentPage, this.pageSize)
        .then(response => {
          let data = response.data;
          this.items = data.listObject;
          this.total = data.itemCount;
        });
    },
    search() {
      this.initTableData();
    },
    create() {
      this.dialogVisible = true;
      listenGoBack(this.closeFunc);
    },
    closeFunc() {
      this.form = {configuration: {}};
      this.dialogVisible = false;
      removeGoBackListener(this.closeFunc);
    },
    changeSwitch(row) {
      this.loading = changeStatus(row.id, row.status)
        .then(() => {
          this.$success(this.$t('test_resource_pool.status_change_success'));
        })
        .catch((response) => {
          this.$error(response.message);
        });
    },
    edit(row) {
      this.dialogVisible = true;
      this.dialogLoading = getAuth(row.id)
        .then(response => {
          this.form = response.data;
          this.form.configuration = JSON.parse(this.form.configuration);
        });
      listenGoBack(this.closeFunc);
    },
    del(row) {
      this.$confirm(this.$t('auth_source.delete_prompt'), this.$t('commons.prompt'), {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.loading = delAuth(row.id)
          .then(() => {
            this.initTableData();
            this.$success(this.$t('commons.delete_success'));
          });
      }).catch(() => {
        this.$info(this.$t('commons.delete_cancel'));
      });
    },
    changeAuthType() {
      this.form.configuration = {};
      if (this.$refs.authSourceForm) {
        this.$refs.authSourceForm.clearValidate();
      }
    },
    createAuthSource() {
      this.$refs.authSourceForm.validate(valid => {
        if (valid) {
          let model = JSON.parse(JSON.stringify(this.form));
          model.configuration = JSON.stringify(model.configuration);
          this.dialogLoading = addAuth(model)
            .then(() => {
              this.$message({
                  type: 'success',
                  message: this.$t('commons.save_success')
                },
                this.dialogVisible = false,
                this.initTableData());
            });
        } else {
          return false;
        }
      });
    },
    updateAuthSource() {
      this.$refs.authSourceForm.validate(valid => {
        if (!valid) {
          return false;
        }
        let model = JSON.parse(JSON.stringify(this.form));
        model.configuration = JSON.stringify(model.configuration);
        this.dialogLoading = updateAuth(model)
          .then(() => {
            this.$success(this.$t('commons.modify_success'));
            this.dialogVisible = false;
            this.initTableData();
          });
      });
    },
  },
  getAuthSources(v) {
    v.result = getAllEnable()
      .then(response => {
        v.authSources = response.data;
      });
  },
  redirectAuth(v, authId) {
    if (authId === 'LDAP' || authId === 'LOCAL') {
      return;
    }
    let source = v.authSources.filter(auth => auth.id === authId)[0];
    // 以前的cas登录
    if (source.type === 'CAS') {
      let config = JSON.parse(source.configuration);
      if (config.casServerUrl && !config.loginUrl) {
        return;
      }
    }
    v.$confirm(v.$t('commons.auth_redirect_tip'), '', {
      confirmButtonText: v.$t('commons.confirm'),
      cancelButtonText: v.$t('commons.cancel'),
      type: 'warning'
    }).then(() => {
      let config = JSON.parse(source.configuration);
      let redirectUrl = eval('`' + config.redirectUrl + '`');
      let url;
      if (source.type === 'CAS') {
        url = config.loginUrl + "?service=" + encodeURIComponent(redirectUrl);
      }
      if (source.type === 'OIDC') {
        url = config.authUrl + "?client_id=" + config.clientId + "&redirect_uri=" + redirectUrl +
          "&response_type=code&scope=openid+profile+email&state=" + authId;
      }
      if (url) {
        window.location.href = url;
      }
    }).catch(() => {
      this.form.authenticate = 'LOCAL';
    });
  }
};
</script>

<style scoped>

</style>
