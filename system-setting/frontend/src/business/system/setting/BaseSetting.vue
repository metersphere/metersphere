<template>
  <div v-loading="loading" >
    <el-form :model="formInline" :rules="rules" ref="formInline" class="demo-form-inline"
             :disabled="show" size="small">
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_config.base.url')" prop="url">
            <el-input v-model="formInline.url" :placeholder="$t('system_config.base.url_tip')"/>
            <i>({{ $t('commons.examples') }}:https://rdmetersphere.fit2cloud.com)</i>
          </el-form-item>
          <el-form-item :label="$t('system_config.prometheus.host')" prop="prometheusHost">
            <el-input v-model="formInline.prometheusHost" :placeholder="$t('system_config.prometheus.host_tip')"/>
            <i>({{ $t('commons.examples') }}:http://ms-prometheus:9090)</i>
          </el-form-item>
          <el-form-item :label="$t('system_config.selenium_docker_url')" prop="seleniumDockerUrl">
            <el-input v-model="formInline.seleniumDockerUrl" :placeholder="$t('system_config.selenium_docker.url_tip')"/>
            <i>({{ $t('commons.examples') }}:http://selenium-hub:4444)</i>
          </el-form-item>
          <el-form-item :label="$t('commons.help_documentation')" prop="docUrl">
            <el-input v-model="formInline.docUrl" placeholder="https://metersphere.io/docs/index.html"/>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <div>
      <el-button @click="edit" v-if="showEdit" size="small" v-permission="['SYSTEM_SETTING:READ+EDIT']">{{ $t('commons.edit') }}</el-button>
      <el-button type="success" @click="save('formInline')" v-if="showSave" :disabled="disabledSave" size="small">
        {{ $t('commons.save') }}
      </el-button>
      <el-button @click="cancel" type="info" v-if="showCancel" size="small">{{ $t('commons.cancel') }}</el-button>
    </div>
  </div>
</template>

<script>

import {getSystemBaseSetting, saveSystemBaseSetting} from "../../../api/system";

export default {
  name: "BaseSetting",
  data() {
    return {
      formInline: {runMode: true, docUrl: 'https://metersphere.io/docs/index.html'},
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
        url: [
          {
            required: true,
            message: this.$t('system_config.base.url_is_null'),
            trigger: ['change', 'blur']
          },
        ],
        prometheusHost: [
          {
            required: true,
            message: this.$t('system_config.prometheus.host_is_null'),
            trigger: ['change', 'blur']
          },
        ],
      }
    }
  },

  created() {
    this.query()
  },
  methods: {
    query() {
      this.loading = getSystemBaseSetting().then(res => {
        if(!res.data.runMode) {
          res.data.runMode = 'LOCAL'
        }
        if(!res.data.docUrl) {
          res.data.docUrl = 'https://metersphere.io/docs/index.html'
        }
        this.formInline = res.data;
        this.$nextTick(() => {
          if (this.$refs.formInline) {
            this.$refs.formInline.clearValidate();
          }
        })
      });
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
      this.$refs[formInline].validate(valid => {
        if (!valid) {
          return false;
        }
        let param = [
          {paramKey: "base.url", paramValue: this.formInline.url, type: "text", sort: 1},
          {paramKey: "base.concurrency", paramValue: this.formInline.concurrency, type: "text", sort: 2},
          {paramKey: "base.prometheus.host", paramValue: this.formInline.prometheusHost, type: "text", sort: 1},
          {paramKey: "base.selenium.docker.url", paramValue: this.formInline.seleniumDockerUrl, type: "text", sort: 1},
          {paramKey: "base.doc.url", paramValue: this.formInline.docUrl, type: "text", sort: 1}
        ];
        this.loading = saveSystemBaseSetting(param).then(res => {
          if (res.success) {
            this.$success(this.$t('commons.save_success'));
          } else {
            this.$error(this.$t('commons.save_failed'));
          }
        }).catch(() => {
          this.edit();
        });
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

.el-form {
  min-height: 300px;
}

</style>
