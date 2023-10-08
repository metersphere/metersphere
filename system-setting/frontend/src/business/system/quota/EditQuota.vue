<template>
  <el-dialog :title="title" :visible.sync="visible" width="60%" @open="open">

    <el-form :model="form" size="small" label-width="200px" v-if="!isDefaultQuota">
      <el-form-item :label="$t('quota.use_default')">
        <el-switch v-model="form.useDefault" @change="change"></el-switch>
      </el-form-item>
    </el-form>

    <el-form :model="form" size="small" label-width="200px" :disabled="isDisabled">
      <el-row type="flex" justify="space-around">
        <el-col :span="12">
          <el-form-item :label="$t('quota.api')" prop="api">
            <el-input-number v-model="form.api" :step="10" :min="0" :max="2147483647"></el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('quota.performance')" prop="performance">
            <el-input-number v-model="form.performance" :step="10" :min="0" :max="2147483647"></el-input-number>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row type="flex" justify="space-around">
        <el-col :span="12">
          <el-form-item :label="$t('quota.max_threads')" prop="maxThreads">
            <el-input-number v-model="form.maxThreads" :step="10" :min="0" :max="2147483647"></el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('quota.duration')" prop="duration">
            <el-input-number v-model="form.duration" :step="1000" :min="0" :max="2147483647"></el-input-number>
          </el-form-item>
        </el-col>
      </el-row>


      <el-row type="flex">
        <el-col :span="12">
          <el-form-item :label="$t('quota.member')" prop="api">
            <el-input-number v-model="form.member" :step="5" :min="0" :max="2147483647"></el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('quota.vum_total')" prop="api">
            <el-input-number v-model="form.vumTotal" :step="100.00" :min="0" :precision="2"
                             :max="2147483647"></el-input-number>
            <ms-instructions-icon effect="light">
              <template>
                {{ $t('quota.vum_tip') }}
              </template>
            </ms-instructions-icon>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row type="flex">
        <el-col :span="12" v-if="quotaType === QUOTA_TYPE.WORKSPACE">
          <el-form-item :label="$t('quota.project')" prop="project">
            <el-input-number v-model="form.project" :step="5" :min="0" :max="2147483647"></el-input-number>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-form-item :label="$t('quota.enable_module')" class="quota-resource-pool">
          <el-select  v-model="form.moduleSetting" multiple filterable class="width-100">
            <el-option v-for="item in modules" :key="item.id" :label="item.name" :value="item.id">
              <span>{{ item.name }}</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-row>

      <el-row>
        <el-form-item :label="$t('quota.resource_pool')" class="quota-resource-pool">
          <el-select v-model="form.resourcePool" multiple filterable class="width-100">
            <el-option v-for="item in resources" :key="item.id" :label="item.name" :value="item.id">
              <span>{{ item.name }} ({{ item.type }})</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-row>
    </el-form>

    <div slot="footer" class="quota-dialog-footer">
      <el-button @click="cancel">{{ $t('commons.cancel') }}</el-button>
      <el-button type="primary" @click="confirm">{{ $t('commons.confirm') }}</el-button>
    </div>

  </el-dialog>
</template>

<script>

import {QUOTA_TYPE} from "../../../common/constants";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";

export default {
  name: "EditQuota",
  components: {MsInstructionsIcon},
  props: {
    isDefaultQuota: {
      type: Boolean,
      default: false
    },
    defaultQuota: Object,
    title: String,
    quota: Object,
    resources: Array,
    quotaType: {
      type: String,
      default() {
        return QUOTA_TYPE.WORKSPACE;
      }
    },
    modules: Array,
  },
  data() {
    return {
      form: {
        api: this.quota.api,
        performance: this.quota.performance,
        maxThreads: this.quota.maxThreads,
        duration: this.quota.duration,
        resourcePool: this.quota.resourcePool ? this.quota.resourcePool.split(",") : [],
        useDefault: this.quota.useDefault,
        member: this.quota.member,
        project: this.quota.project,
        vumTotal: this.quota.vumTotal,
        moduleSetting: this.quota.moduleSetting ? this.quota.moduleSetting.split(",") : [],
      },
      visible: false,
      QUOTA_TYPE,
    }
  },
  methods: {
    change(value) {
      if (value === false) {
        this.form.api = 0;
        this.form.performance = 0;
        this.form.maxThreads = 0;
        this.form.duration = 0;
        this.form.resourcePool = [];
        this.form.member = 0;
        this.form.project = 0;
        this.form.vumTotal = 0;
        this.form.moduleSetting = [];
      } else {
        this.form.api = this.defaultQuota.api;
        this.form.performance = this.defaultQuota.performance;
        this.form.maxThreads = this.defaultQuota.maxThreads;
        this.form.duration = this.defaultQuota.duration;
        this.form.resourcePool = this.defaultQuota.resourcePool ? this.defaultQuota.resourcePool.split(",") : [];
        this.form.member = this.defaultQuota.member;
        this.form.project = this.defaultQuota.project;
        this.form.vumTotal = this.defaultQuota.vumTotal;
        this.form.moduleSetting = this.defaultQuota.moduleSetting ? this.defaultQuota.moduleSetting.split(",") : [];
      }
    },
    open() {
      this.form.api = this.quota.api;
      this.form.performance = this.quota.performance;
      this.form.maxThreads = this.quota.maxThreads;
      this.form.duration = this.quota.duration;
      this.form.resourcePool = this.quota.resourcePool ? this.quota.resourcePool.split(",") : [];
      this.form.moduleSetting = this.quota.moduleSetting ? this.quota.moduleSetting.split(",") : [];
      this.form.useDefault = this.quota.useDefault;
      this.form.member = this.quota.member;
      this.form.project = this.quota.project;
      this.form.vumTotal = this.quota.vumTotal;
    },
    openDialog() {
      this.visible = true;
    },
    closeDialog() {
      this.visible = false;
    },
    cancel() {
      this.closeDialog();
    },
    confirm() {
      this.closeDialog();
      /*eslint-disable */
      this.quota.useDefault = this.form.useDefault;
      this.quota.api = this.form.api;
      this.quota.performance = this.form.performance;
      this.quota.maxThreads = this.form.maxThreads;
      this.quota.duration = this.form.duration;
      this.quota.member = this.form.member;
      this.quota.project = this.form.project;
      this.quota.vumTotal = this.form.vumTotal;
      this.quota.resourcePool = this.form.resourcePool.join(",");
      this.quota.moduleSetting = this.form.moduleSetting.join(",");
      this.$emit('confirm', this.quota);
    }
  },
  computed: {
    isDisabled() {
      return this.form.useDefault === true && this.isDefaultQuota === false;
    }
  }
}

</script>

<style scoped>
.quota-item {
  display: inline-block;
  width: 300px;
}

.quota-resource-pool {
  display: inline-block;
  width: 600px;
}

.width-100 {
  width: 100%;
}

.quota-dialog-footer {
  text-align: center;
}
</style>
