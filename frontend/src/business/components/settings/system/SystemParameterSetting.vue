<template>
  <div>
    <el-card class="box-card" v-loading="result.loading">
      <template v-slot:header>
        <h2>{{$t('system_parameter_setting.mailbox_service_settings')}}</h2>
      </template>
      <!--邮件表单-->
      <el-form :inline="true" :model="formInline" :rules="rules" ref="formInline" class="demo-form-inline"
               :disabled="show" v-loading="loading">
        <el-row>
          <el-col>
            <el-form-item :label="$t('system_parameter_setting.SMTP_host')" prop="host">
            </el-form-item>
            <el-input v-model="formInline.host" :placeholder="$t('system_parameter_setting.SMTP_host')"
                      v-on:input="host('host')"></el-input>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-form-item :label="$t('system_parameter_setting.SMTP_port')" prop="port">
            </el-form-item>
            <el-input v-model="formInline.port" :placeholder="$t('system_parameter_setting.SMTP_port')"
                      v-on:input="port('port')"></el-input>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-form-item :label="$t('system_parameter_setting.SMTP_account')" prop="account">
            </el-form-item>
            <el-input v-model="formInline.account" :placeholder="$t('system_parameter_setting.SMTP_account')"
                      v-on:input="account('account')"></el-input>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-form-item :label="$t('system_parameter_setting.SMTP_password')" prop="password">
            </el-form-item>
            <el-input v-model="formInline.password" :placeholder="$t('system_parameter_setting.SMTP_password')"
                      show-password></el-input>
          </el-col>
        </el-row>

        <!---->
        <div style="border: 0px;margin-bottom: 20px;margin-top: 20px">
          <el-checkbox v-model="SSL" :label="$t('system_parameter_setting.SSL')"></el-checkbox>
        </div>
        <div style="border: 0px;margin-bottom: 20px">
          <el-checkbox v-model="TLS" :label="$t('system_parameter_setting.TLS')"></el-checkbox>
        </div>
        <div style="border: 0px;margin-bottom: 20px">
          <el-checkbox v-model="SMTP" :label="$t('system_parameter_setting.SMTP')"></el-checkbox>
        </div>
        <template v-slot:footer>
        </template>
      </el-form>
      <div style="margin-left: 640px">
        <el-button type="primary" @click="testConnection('formInline')" :disabled="disabledConnection">
          {{$t('system_parameter_setting.test_connection')}}
        </el-button>
        <el-button @click="edit" v-if="showEdit">{{$t('commons.edit')}}</el-button>
        <el-button type="success" @click="save('formInline')" v-if="showSave" :disabled="disabledSave">
          {{$t('commons.save')}}
        </el-button>
        <el-button @click="cancel" type="info" v-if="showCancel">{{$t('commons.cancel')}}</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>

  export default {
    name: "SystemParameterSetting",
    data() {
      return {
        formInline: {
          host: 'smtp.163.com',
          port: '465',
          account: 'xjj0608@153.com',
          password: '2345678',
        },
        result: {},
        SSL: false,
        TLS: false,
        SMTP: true,
        showEdit: true,
        showSave: false,
        showCancel: false,
        show: true,
        disabledConnection: false,
        disabledSave: false,
        loading: false,
        rules: {
          host: [
            {
              required: true,
              message: this.$t('commons.host_cannot_be_empty')
            },
          ],
          port: [
            {
              required: true,
              message: ' '
            }
          ],
          account: [
            {
              required: true,
              message: ' '
            }]
        }
      }
    },


    methods: {
      host() {
        let host = this.formInline.host;
        if (!host) {
          this.disabledConnection = true;
          this.disabledSave = true;
        } else {
          this.disabledConnection = false;
          this.disabledSave = false;
        }
      },
      port() {

        let port = this.formInline.port;
        if (!port) {
          this.disabledConnection = true;
          this.disabledSave = true;
        } else {
          this.disabledConnection = false;
          this.disabledSave = false;
        }
      },
      account() {
        let account = this.formInline.account;
        if (!account) {
          this.disabledConnection = true;
          this.disabledSave = true;
        } else {
          this.disabledConnection = false;
          this.disabledSave = false;
        }
      },
      testConnection(formInline) {
        let param = {
          "meter.host": this.formInline.host,
          "meter.port": this.formInline.port,
          "meter.account": this.formInline.account,
          "meter.password": this.formInline.password,
          "meter.ssl": this.SSL,
          "meter.tls": this.TLS,
          "meter.smtp": this.SMTP,
        };
        this.$refs[formInline].validate((valid) => {
          if (valid) {
            this.result = this.$post("/system/testConnection", param, response => {
              let flag = response.success;
              if (flag) {
                this.$success(this.$t('commons.connection_successful'));
              } else {
                this.$message.error(this.$t('commons.connection_failed'));
              }
            });
          } else {
            return false;
          }
        })
      },
      edit() {
        this.showEdit = false;
        this.showSave = true;
        this.showCancel = true;
        this.show = false;
      },
      save(formInline) {
        this.showEdit = true;
        this.showCancel = false;
        this.showSave = false;
        this.show = true;
        let param = [
          {paramKey: "meter.host", paramValue: this.formInline.host, type: "text", sort: 1},
          {paramKey: "meter.port", paramValue: this.formInline.port, type: "text", sort: 2},
          {paramKey: "meter.account", paramValue: this.formInline.account, type: "text", sort: 3},
          {paramKey: "meter.password", paramValue: this.formInline.password, type: "password", sort: 4},
          {paramKey: "meter.ssl", paramValue: this.SSL, type: "text", sort: 5},
          {paramKey: "meter.tls", paramValue: this.TLS, type: "text", sort: 6},
          {paramKey: "meter.smtp", paramValue: this.SMTP, type: "text", sort: 7}
        ]

        this.$refs[formInline].validate(valid => {
          if (valid) {
            this.result = this.$post("/system/edit/email", param, response => {
              let flag = response.success;
              if (flag) {
                this.$success(this.$t('commons.save_success'));
              } else {
                this.$message.error(this.$t('commons.save_failed'));
              }
            });
          } else {
            return false;
          }
        })
      },
      cancel() {
        this.showEdit = true;
        this.showCancel = false;
        this.showSave = false;
        this.show = true;
      }

    }
  }
</script>

<style scoped>
  .text {
    font-size: 18px;
  }

  .item {
    margin-bottom: 30px;
  }

  .box-card {
    padding-left: 5px;
  }

  /deep/ .el-input__inner {
    border-width: 0px;
    border-bottom-width: 1px;
  }
</style>
