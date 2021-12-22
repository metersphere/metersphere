<template>
  <div>
    <div style="margin-left: 20px;">
      <el-select v-model="envGroupId" :placeholder="$t('workspace.env_group.select')" style="margin-top: 8px;width: 200px;" size="small">
        <el-option-group
          v-for="group in groups"
          :key="group.label"
          :label="group.label">
          <el-option
            v-for="item in group.options"
            :key="item.name"
            :label="item.name"
            :disabled="item.disabled"
            :value="item.id">
          </el-option>
        </el-option-group>
      </el-select>
      <span style="margin-left: 8px;">{{$t('workspace.env_group.name')}}</span>
      <i class="el-icon-view icon-view-btn" @click="viewGroup"></i>
    </div>
    <el-button type="primary" @click="handleConfirm" size="small" class="env-confirm">{{$t('workspace.env_group.confirm')}}</el-button>
    <el-dialog :visible="visble" append-to-body :title="$t('workspace.env_group.name')" @close="visble = false" style="height: 800px;">
      <template>
        <environment-group style="overflow-y: auto;"
                           :screen-height="'350px'"
                           :read-only="true"
        ></environment-group>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import EnvironmentGroup from "@/business/components/settings/workspace/environment/EnvironmentGroupList";

export default {
  name: "EnvGroupWithOption",
  components: {EnvironmentGroup},
  data() {
    return {
      groups: [],
      envGroupId: this.groupId,
      visble: false,
      disabledGroups: [],
      notDisabledGroups: [],
      result: {}
    }
  },
  props: {
    groupId: {
      type: String,
      default() {
        return "";
      }
    },
    projectIds: Set,
  },
  watch: {
    groupId(val) {
      this.envGroupId = val;
    }
  },
  created() {
    this.init();
  },
  methods: {
    open() {
      this.envGroupId = this.groupId;
    },
    init() {
      this.result = this.$post("/environment/group/get/option", {projectIds: [...this.projectIds]}, res => {
        let groups = res.data;
        this.disabledGroups = groups.filter(group => group.disabled === true);
        this.notDisabledGroups = groups.filter(group => group.disabled === false);
        this.$set(this.groups, 0, {
          label: this.$t('workspace.env_group.available_group'),
          options: this.notDisabledGroups
        });
        this.$set(this.groups, 1, {
          label: this.$t('workspace.env_group.not_available_group'),
          options: this.disabledGroups
        });
      })
    },
    viewGroup() {
      this.visble = true;
    },
    async handleConfirm() {
      const sign = await this.checkEnv();
      if (sign) {
        this.$emit("setEnvGroup", this.envGroupId);
        this.$emit('close');
      }
    },
    checkEnv() {
      return new Promise((resolve) => {
        if (!this.envGroupId) {
          this.$warning(this.$t('workspace.env_group.select'));
          resolve(false);
          return false;
        }
        this.$get("/environment/group/project/map/" + this.envGroupId, res => {
          let data = res.data;
          if (!data) {
            this.$warning(this.$t('workspace.env_group.lack_env'));
            resolve(false);
            return;
          }
          let map = new Map(Object.entries(data));
          for (let id of this.projectIds) {
            if (!map.get(id)) {
              this.$warning(this.$t('workspace.env_group.lack_necessary_environment'));
              resolve(false);
              return;
            }
          }
          resolve(true);
        });
      })
    }
  }
}
</script>

<style scoped>
.env-confirm {
  margin-left: 20px;
  width: 360px;
  margin-top: 10px;
}
.icon-view-btn {
  margin-left: 5px;
}
.icon-view-btn:hover {
  cursor: pointer;
}
</style>
