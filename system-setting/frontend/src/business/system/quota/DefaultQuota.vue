<template>
  <div class="quota-card">
    <el-card shadow="always">
      <div class="title">
        {{ title }}
      </div>

      <el-row :gutter="5" class="quota-row">
        <el-col :span="6">
          {{ $t('quota.member') }}:
          <quota-value :value="quota.member"/>
        </el-col>
        <el-col :span="6">
          {{ $t('quota.api') }}:
          <quota-value :value="quota.api"/>
        </el-col>
        <el-col :span="6">
          {{ $t('quota.performance') }}:
          <quota-value :value="quota.performance"/>
        </el-col>
        <el-col :span="6">
          {{ $t('quota.vum_total') }}:
          <quota-value :value="quota.vumTotal"/>
        </el-col>
      </el-row>

      <el-row :gutter="5" class="quota-row">
        <el-col :span="6" v-if="quotaType === QUOTA_TYPE.WORKSPACE">
          {{ $t('quota.project') }}:
          <quota-value :value="quota.project"/>
        </el-col>
        <el-col :span="6">
          {{ $t('quota.max_threads') }}:
          <quota-value :value="quota.maxThreads"/>
        </el-col>
        <el-col :span="6">
          {{ $t('quota.duration') }}:
          <quota-value :value="quota.duration"/>
        </el-col>
        <el-col :span="6">

        </el-col>
      </el-row>

      <el-row :gutter="5" class="quota-row">
        <el-col :span="24">
          {{ $t('quota.enable_module') }}:
          <quota-value :value="moduleName"/>
        </el-col>
      </el-row>

      <el-row :gutter="5" class="quota-row">
        <el-col :span="24">
          {{ $t('quota.resource_pool') }}:
          <quota-value :value="resourcePoolNames"/>
        </el-col>
      </el-row>

      <div class="bottom quota-row">
        <el-button @click="open" plain size="mini" v-permission="permission">
          {{ $t('quota.edit') }}
        </el-button>
      </div>
    </el-card>

    <edit-quota :title="title"
                   :quota="quota"
                   :resources="resources"
                   :modules="modules"
                   :is-default-quota="true"
                   :quota-type="quotaType"
                   @confirm="confirm"
                   ref="editQuota"
    />
  </div>
</template>

<script>
import QuotaValue from "./QuotaValue";
import EditQuota from "./EditQuota";
import {QUOTA_TYPE} from "../../../common/constants";

export default {
  name: "DefaultQuota",
  components: {QuotaValue, EditQuota},
  props: {
    title: String,
    quota: Object,
    resources: Array,
    modules: Array,
    quotaType: {
      type: String,
      default() {
        return QUOTA_TYPE.WORKSPACE;
      }
    }
  },
  data() {
    return {
      visible: false,
      QUOTA_TYPE,
    }
  },
  computed: {
    // id => name
    resourcePoolNames() {
      if (!this.quota.resourcePool) return "";
      let ids = this.quota.resourcePool.split(",");
      let names = [];
      ids.forEach(id => {
        for (let resource of this.resources) {
          if (resource.id === id) {
            names.push(resource.name);
          }
        }
      })
      return names.join(",");
    },
    moduleName() {
      if (!this.quota.moduleSetting) return "";
      let ids = this.quota.moduleSetting.split(",");
      let names = [];
      ids.forEach(id => {
        for (let module of this.modules) {
          if (module.id === id) {
            names.push(module.name);
          }
        }
      })
      return names.join(",");
    },
    permission() {
      if (this.quotaType === QUOTA_TYPE.WORKSPACE) {
        return ['SYSTEM_QUOTA:READ+EDIT']
      } else if(this.quotaType === QUOTA_TYPE.PROJECT) {
        return ['WORKSPACE_QUOTA:READ+EDIT']
      } else {
        return [];
      }
    }
  },
  methods: {
    open() {
      this.$refs.editQuota.openDialog();
    },
    confirm(quota) {
      this.$emit('confirm', quota);
    }
  }
}

</script>

<style scoped>
.quota-card .title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}

.quota-row {
  margin-top: 15px;
}
</style>
