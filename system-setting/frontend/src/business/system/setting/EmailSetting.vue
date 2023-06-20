<template>
  <div v-loading="loading">
    <!--邮件表单-->
    <el-form :model="formInline" :rules="rules" ref="formInline" class="demo-form-inline"
             :disabled="show" v-loading="loading" size="small">
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_parameter_setting.SMTP_host')" prop="host">
            <el-input v-model="formInline.host" :placeholder="$t('system_parameter_setting.SMTP_host')"
                      v-on:input="change()"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_parameter_setting.SMTP_port')" prop="port">
            <el-input v-model="formInline.port" :placeholder="$t('system_parameter_setting.SMTP_port')"
                      v-on:input="change()"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_parameter_setting.SMTP_account')" prop="account">
            <el-input v-model="formInline.account" :placeholder="$t('system_parameter_setting.SMTP_account')"
                      v-on:input="change()"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_parameter_setting.SMTP_password')" prop="password">
            <el-input v-model="formInline.password" :placeholder="$t('system_parameter_setting.SMTP_password')"
                      autocomplete="new-password" show-password type="text" @focus="changeType" ref="input">
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item prop="from">
            <template v-slot:label>
              {{ $t('system_parameter_setting.SMTP_from') }} <i style="font-size: 10px;">{{$t('system_parameter_setting.from_tip')}}</i>
            </template>
            <el-input v-model="formInline.from" :placeholder="$t('system_parameter_setting.SMTP_from')"
                      type="text" v-on:input="change()">
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_parameter_setting.test_recipients')">
            <el-input v-model="formInline.recipient" :placeholder="$t('system_parameter_setting.test_recipients')"
                      autocomplete="new-password" type="text" ref="input">
            </el-input>
            <p style="color: #8a8b8d">({{ $t('system_parameter_setting.tip') }})</p>
          </el-form-item>
        </el-col>
      </el-row>

      <!---->
      <div style="border: 0px;margin-bottom: 20px">
        <el-checkbox v-model="formInline.ssl" :label="$t('system_parameter_setting.SSL')"></el-checkbox>
      </div>
      <div style="border: 0px;margin-bottom: 20px">
        <el-checkbox v-model="formInline.tls" :label="$t('system_parameter_setting.TLS')"></el-checkbox>
      </div>
      <template v-slot:footer>
      </template>
    </el-form>
    <div>
      <el-button type="primary" @click="testConnection('formInline')" :disabled="disabledConnection" size="small">
        {{ $t('system_parameter_setting.test_connection') }}
      </el-button>
      <el-button @click="edit" v-if="showEdit" size="small" v-permission="['SYSTEM_SETTING:READ+EDIT']">{{ $t('commons.edit') }}</el-button>
      <el-button type="success" @click="save('formInline')" v-if="showSave" :disabled="disabledSave" size="small">
        {{ $t('commons.save') }}
      </el-button>
      <el-button @click="cancel" type="info" v-if="showCancel" size="small">{{ $t('commons.cancel') }}</el-button>
    </div>
  </div>
</template>

<script>

import {getSystemMailServerInfo, modifySystemMailServerInfo, testMailServerConnect} from "../../../api/system";

export default {
  name: "EmailSetting",
  data() {
    const validatorPortNum = (rule, value, callback) => {
      const numExp = /^[0-9]*$/
      if (!numExp.test(value)) {
        callback(new Error("Port" + this.$t("commons.type_of_num")))
      }else{
        callback()
      }
    }
    return {
      formInline: {},
      input: '',
      visible: true,
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
            message: this.$t('system_parameter_setting.host'),
            trigger: ['change', 'blur']
          },
        ],
        port: [
          {
            required: true,
            message: this.$t('system_parameter_setting.port'),
            trigger: ['change', 'blur'],
          },
          {
            validator: validatorPortNum, trigger: ['change', 'blur'],
          }
        ],
        account: [
          {
            required: true,
            message: this.$t('system_parameter_setting.account'),
            trigger: ['change', 'blur']
          }]
      }
    }
  },

  created() {
    this.query()
  },
  methods: {
    changeType() {
      this.$refs.input = 'password'
    },
    query() {
      this.loading = getSystemMailServerInfo().then(res => {
        this.formInline = res.data;
        this.formInline.ssl = this.formInline.ssl === 'true';
        this.formInline.tls = this.formInline.tls === 'true';
        this.$nextTick(() => {
          this.$refs.formInline.clearValidate();
        })
      })
    },
    change() {
      if (!this.formInline.host || !this.formInline.port || !this.formInline.account) {
        this.disabledConnection = true;
        this.disabledSave = true;
      } else {
        this.disabledConnection = false;
        this.disabledSave = false;
      }
    },
    testConnection(formInline) {
      let param = {
        "smtp.host": this.formInline.host,
        "smtp.port": this.formInline.port,
        "smtp.account": this.formInline.account,
        "smtp.password": this.formInline.password,
        "smtp.from": this.formInline.from,
        "smtp.ssl": this.formInline.ssl,
        "smtp.tls": this.formInline.tls,
        "smtp.recipient": this.formInline.recipient,
      };
      this.$refs[formInline].validate((valid) => {
        if (valid) {
          this.loading = testMailServerConnect(param).then(() => {
            this.$success(this.$t('commons.connection_successful'));
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
        {paramKey: "smtp.host", paramValue: this.formInline.host, type: "text", sort: 1},
        {paramKey: "smtp.port", paramValue: this.formInline.port, type: "text", sort: 2},
        {paramKey: "smtp.account", paramValue: this.formInline.account, type: "text", sort: 3},
        {paramKey: "smtp.from", paramValue: this.formInline.from, type: "text", sort: 4},
        {paramKey: "smtp.password", paramValue: this.formInline.password, type: "password", sort: 4},
        {paramKey: "smtp.ssl", paramValue: this.formInline.ssl, type: "text", sort: 5},
        {paramKey: "smtp.tls", paramValue: this.formInline.tls, type: "text", sort: 6},
        {paramKey: "smtp.recipient", paramValue: this.formInline.recipient, type: "text", sort: 8}
      ]

      this.$refs[formInline].validate(valid => {
        if (valid) {
          this.loading = modifySystemMailServerInfo(param).then(res => {
            let flag = res.success;
            if (flag) {
              this.$success(this.$t('commons.save_success'));
            } else {
              this.$error(this.$t('commons.save_failed'));
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
      this.query();
    }

  }
}
</script>

<style scoped>

</style>
