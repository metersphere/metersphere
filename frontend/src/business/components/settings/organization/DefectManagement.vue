<template>
  <el-card class="header-title">
    <div v-loading="result.loading">
      <div>{{$t('organization.select_defect_platform')}}</div>
      <el-radio-group v-model="platform" style="margin-top: 10px" @change="change">
        <el-radio v-for="(item, index) in platforms" :key="index" :label="item.value" size="small">
          {{item.name}}
        </el-radio>
      </el-radio-group>
    </div>

    <div style="width: 500px">
      <div style="margin-top: 20px;margin-bottom: 10px">{{$t('organization.basic_auth_info')}}</div>
      <el-form :model="form" ref="form" label-width="100px" size="small" :disabled="show" :rules="rules">
        <el-form-item :label="$t('organization.api_account')" prop="account">
          <el-input v-model="form.account" :placeholder="$t('organization.input_api_account')"/>
        </el-form-item>
        <el-form-item :label="$t('organization.api_password')" prop="password">
          <el-input v-model="form.password" auto-complete="new-password"
                    :placeholder="$t('organization.input_api_password')" show-password/>
        </el-form-item>
        <el-form-item label="JIRA 地址" prop="url" v-if="platform === 'Jira'">
          <el-input v-model="form.url" placeholder="请输入Jira地址"/>
        </el-form-item>
      </el-form>
    </div>

    <div style="margin-left: 100px">
      <el-button type="primary" size="small" :disabled="!show" @click="testConnection">{{$t('ldap.test_connect')}}
      </el-button>
      <el-button v-if="showEdit" size="small" @click="edit">{{$t('commons.edit')}}</el-button>
      <el-button type="primary" v-if="showSave" size="small" @click="save('form')">{{$t('commons.save')}}</el-button>
      <el-button v-if="showCancel" size="small" @click="cancelEdit">取消编辑</el-button>
      <el-button type="info" size="small" @click="cancelIntegration('form')" :disabled="!show">
        取消集成
      </el-button>
    </div>

    <div class="defect-tip">
      <div>{{$t('organization.use_tip')}}</div>
      <div>
        1. {{$t('organization.use_tip_one')}}
      </div>
      <div>
        2. {{$t('organization.use_tip_two')}}
        <router-link to="/track/project/all" style="margin-left: 5px">{{$t('organization.link_the_project_now')}}
        </router-link>
      </div>
    </div>
  </el-card>
</template>

<script>
  import {getCurrentUser} from "../../../../common/js/utils";

  export default {
    name: "DefectManagement",
    data() {
      return {
        form: {},
        result: {},
        platform: '',
        orgId: '',
        show: true,
        showEdit: true,
        showSave: false,
        showCancel: false,
        platforms: [
          {
            name: 'TAPD',
            value: 'Tapd',
          },
          {
            name: 'JIRA',
            value: 'Jira',
          }
        ],
        rules: {
          account: {required: true, message: this.$t('organization.input_api_account'), trigger: ['change', 'blur']},
          password: {required: true, message: this.$t('organization.input_api_password'), trigger: ['change', 'blur']},
          url: {required: true, message: '请输入url', trigger: ['change', 'blur']}
        },
      }
    },
    created() {
      this.init(this.platform);
    },
    methods: {
      init(platform) {
        let param = {};
        param.platform = platform;
        param.orgId = getCurrentUser().lastOrganizationId;
        this.result = this.$post("service/integration/type", param, response => {
          let data = response.data;
          this.platform = data.platform;
          if (data.configuration) {
            let config = JSON.parse(data.configuration);
            this.$set(this.form, 'account', config.account);
            this.$set(this.form, 'password', config.password);
            this.$set(this.form, 'url', config.url);
          } else {
            this.clear();
          }
        })
      },
      edit() {
        this.show = false;
        this.showEdit = false;
        this.showSave = true;
        this.showCancel = true;
      },
      cancelEdit() {
        this.showEdit = true;
        this.showCancel = false;
        this.showSave = false;
        this.show = true;
        this.init(this.platform);
      },
      cancelIntegration() {
        if (this.form.account && this.form.password && this.platform) {

          this.$alert("确认取消集成 " + this.platform + "？", '', {
            confirmButtonText: this.$t('commons.confirm'),
            callback: (action) => {
              if (action === 'confirm') {
                let param = {};
                param.orgId = getCurrentUser().lastOrganizationId;
                param.platform = this.platform;
                this.result = this.$post("service/integration/delete", param, () => {
                  this.$success("操作成功");
                  this.init('');
                });
              }
            }
          });
        } else  {
          this.$warning("未集成该平台！");
        }
      },
      save(form) {
        if (!this.platform) {
          this.$warning("请选择集成的平台！");
          return;
        }
        let param = {};
        let auth = {
          account: this.form.account,
          password: this.form.password,
          url: this.form.url
        };
        param.organizationId = getCurrentUser().lastOrganizationId;
        param.platform = this.platform;
        param.configuration = JSON.stringify(auth);
        this.$refs[form].validate(valid => {
          if (valid) {
            this.result = this.$post("service/integration/save", param, () => {
              this.show = true;
              this.showEdit = true;
              this.showSave = false;
              this.showCancel = false;
              this.init(this.platform);
              this.$success(this.$t('commons.save_success'));
            });
          } else {
            return false;
          }
        })
      },
      change(platform) {
        this.show = true;
        this.showEdit = true;
        this.showCancel = false;
        this.showSave = false;
        let param = {};
        param.orgId = getCurrentUser().lastOrganizationId;
        param.platform = platform;
        this.result = this.$post("service/integration/type", param, response => {
          let data = response.data;
          if (data.configuration) {
            let config = JSON.parse(data.configuration);
            this.$set(this.form, 'account', config.account);
            this.$set(this.form, 'password', config.password);
            this.$set(this.form, 'url', config.url);
          } else {
            this.clear();
          }
        })
      },
      clear() {
        this.$set(this.form, 'account', '');
        this.$set(this.form, 'password', '');
        this.$set(this.form, 'url', '');
        this.$nextTick(() => {
          this.$refs.form.clearValidate();
        });
      },
      testConnection() {
        this.$get("issues/auth/" + this.platform, () => {
          this.$success("验证通过！");
        });
      }
    }
  }
</script>

<style scoped>
  .header-title {
    padding: 10px 30px;
  }

  .defect-tip {
    background: #EDEDED;
    border: solid #E1E1E1 1px;
    margin: 10px 0;
    padding: 10px;
    border-radius: 3px;
  }
</style>
