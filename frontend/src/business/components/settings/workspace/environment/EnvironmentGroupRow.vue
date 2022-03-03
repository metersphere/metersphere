<template>
  <div>
    <div class="btn-div">
      <el-button size="mini" type="primary" class="save-btn" v-if="!rowReadOnly && !showSaveBtn" @click="update">
        {{ $t('commons.save') }}
      </el-button>
    </div>
    <div v-loading="result.loading" class="group-row">
      <el-form class="row-form">
        <el-form-item v-for="(item,index) in envGroupProject" :key="index">
          <el-row type="flex" justify="space-between" :gutter="10">
            <el-col :span="6">
              <el-select v-model="item.projectId" filterable clearable style="width: 100%" @change="projectChange(item)"
                         :size="itemSize" @clear="clearProjectSelect"
                         :placeholder="$t('workspace.env_group.please_select_project')" :disabled="rowReadOnly">
                <el-option v-for="(project, projectIndex) in projectList" :key="projectIndex" :label="project.name"
                           :disabled="project.disabled"
                           :value="project.id"></el-option>
              </el-select>
            </el-col>

            <el-col :span="6">
              <el-select v-model="item.environmentId" filterable clearable style="width: 100%"
                         @change="environmentChange(item)" :size="itemSize"
                         :placeholder="$t('workspace.env_group.please_select_env')" :disabled="rowReadOnly">
                <el-option v-for="(environment, envIndex) in item.environments" :key="envIndex"
                           :label="environment.name"
                           :value="environment.id"></el-option>
              </el-select>
            </el-col>

            <el-col :span="4">
              <el-button :size="itemSize" icon="el-icon-s-data" style="width: 100%;"
                         @click="showDomainInfo(item)" v-if="item.moreDomain">
                {{ $t('workspace.env_group.view_details') }}
              </el-button>
              <el-input v-else v-model="item.domainName" :disabled="true" :size="itemSize"/>
            </el-col>

            <el-col :span="5">
              <el-input prop="description" show-overflow-tooltip :placeholder="$t('commons.description')"
                        maxlength="100"
                        v-model="item.domainDescription"
                        show-word-limit :size="itemSize" :disabled="true"/>
            </el-col>

            <el-col :span="3">
              <el-button type="info" icon="el-icon-plus" circle :size="itemSize" :disabled="rowReadOnly"
                         @click="change"></el-button>
              <el-button type="danger" icon="el-icon-delete" circle :size="itemSize" :disabled="rowReadOnly"
                         @click="remove(index)"></el-button>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
      <el-dialog :title="$t('workspace.env_group.domain_list')" :visible.sync="domainVisible" append-to-body>
        <el-table :data="conditions">
          <el-table-column prop="socket" :label="$t('load_test.domain')" show-overflow-tooltip width="180">
            <template v-slot:default="{row}">
              {{ row.conditionType ? row.server : getUrl(row) }}
            </template>
          </el-table-column>
          <el-table-column :label="$t('commons.type')" show-overflow-tooltip
                           min-width="100px">
            <template v-slot:default="{row}">
              <el-tag type="info" size="mini">{{ row.conditionType ? row.conditionType : "HTTP" }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="type" :label="$t('api_test.environment.condition_enable')" show-overflow-tooltip
                           min-width="100px">
            <template v-slot:default="{row}">
              {{ row.conditionType ? "-" : getName(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="details" show-overflow-tooltip min-width="120px" :label="$t('api_test.value')">
            <template v-slot:default="{row}">
              {{ row.conditionType ? "-" : getDetails(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="description" show-overflow-tooltip min-width="120px"
                           :label="$t('commons.description')">
            <template v-slot:default="{row}">
              <span>{{ row.description ? row.description : "-" }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" show-overflow-tooltip min-width="120px" :label="$t('commons.create_time')">
            <template v-slot:default="{row}">
              <span v-if="!row.conditionType">{{ row.time | timestampFormatDate }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
        <span slot="footer" class="dialog-footer">
          <el-button @click="domainVisible = false" size="small">{{ $t('commons.cancel') }}</el-button>
          <el-button type="primary" @click="domainVisible = false" size="small">{{ $t('commons.confirm') }}</el-button>
        </span>
      </el-dialog>
    </div>
  </div>

</template>

<script>

export default {
  name: "EnvironmentGroupRow",
  data() {
    return {
      result: {},
      envGroupProject: [],
      projectList: [],
      environmentList: [],
      environments: [],
      domainVisible: false,
      conditions: [],
      rowReadOnly: this.readOnly
    }
  },
  watch: {
    readOnly: {
      handler(v) {
        this.rowReadOnly = v;
      },
    },
  },
  props: {
    envGroupId: {
      type: String,
      default() {
        return '';
      }
    },
    readOnly: {
      type: Boolean,
      default() {
        return true;
      }
    },
    itemSize: {
      type: String,
      default() {
        return "mini";
      }
    },
    showSaveBtn: {
      type: Boolean,
      default() {
        return false;
      }
    }
  },
  created() {
    this.getEnvironmentGroupProject();
    this.getProjects();
  },
  methods: {
    projectChange(item) {
      if (item && item.projectId) {
        let project = this.projectList.find(project => project.id === item.projectId);
        if (project) {
          project.disabled = true;
        }
        this.clearProjectSelect();
        this.$get('/api/environment/list/' + item.projectId, res => {
          this.$set(item, 'environments', res.data);
        });
      } else {
        this.$set(item, 'environments', []);
      }
      this.$set(item, 'environmentId', "");
      this.$set(item, 'domainName', '');
    },
    clearProjectSelect() {
      let usedProjectId = this.envGroupProject.map(egp => egp.projectId);
      if (usedProjectId) {
        this.projectList.forEach(p => {
          p.disabled = !!usedProjectId.find(id => id === p.id);
        })
      }
    },
    environmentChange(item) {
      // todo 优化
      // 环境改变时初始化判断状态
      this.$set(item, "moreDomain", false);
      this.$set(item, "domainName", '');
      this.$set(item, "domainDescription", '');
      let environments = item.environments;
      let index = environments.findIndex(e => e.id === item.environmentId);
      if (index === -1) {
        this.$set(item, "domainName", '');
        this.$set(item, "domainDescription", '');
        return;
      }
      let environment = environments[index].config;
      if (environment) {
        const config = JSON.parse(environment);
        if (config.httpConfig && !config.httpConfig.conditions) {
          if (config.httpConfig.protocol && config.httpConfig.domain) {
            let domain = config.httpConfig.protocol + "://" + config.httpConfig.domain;
            this.$set(item, "domainName", domain);
            this.$set(item, "domainDescription", config.httpConfig.description ? config.httpConfig.description : "");
            return;
          }
        } else {
          if (config.httpConfig.conditions.length === 1) {
            if (config.tcpConfig && config.tcpConfig.server) {
              this.$set(item, "moreDomain", true);
              return;
            }
            let obj = config.httpConfig.conditions[0];
            if (obj.protocol && obj.socket) {
              this.$set(item, "domainName", obj.protocol + "://" + obj.socket);
              this.$set(item, "domainDescription", obj.description ? obj.description : "");
              return;
            }
          } else if (config.httpConfig.conditions.length > 1) {
            this.$set(item, "moreDomain", true);
            return;
          } else if (config.tcpConfig && config.tcpConfig.server) {
            this.$set(item, "domainName", config.tcpConfig.server);
            this.$set(item, "domainDescription", config.tcpConfig.description);
            return;
          }
        }
      } else {
        this.$set(item, "domainName", environment.protocol + '://' + environment.domain);
        this.$set(item, "domainDescription", environment.description ? environment.description : "");
        return;
      }
      this.$set(item, "domainName", "");
      this.$set(item, "domainDescription", "");
    },
    showDomainInfo(item) {
      const index = item.environments.findIndex(e => e.id === item.environmentId);
      if (index === -1) {
        return "";
      }
      let environment = item.environments[index]
      const config = JSON.parse(environment.config);
      this.conditions = config.httpConfig.conditions;
      if (config.tcpConfig && config.tcpConfig.server) {
        let condition = {
          conditionType: 'TCP',
          server: config.tcpConfig.server,
          description: config.tcpConfig.description
        }
        this.conditions.push(condition);
      }
      this.domainVisible = true;
    },
    getProjects() {
      // 工作空间下项目
      this.$get("/project/listAll", response => {
        let data = response.data;
        if (data) {
          this.projectList = data;
          this.projectList.forEach(project => {
            this.$set(project, "disabled", false);
          })
        }
      })
    },
    getEnvironmentGroupProject() {
      if (!this.envGroupId) {
        this.envGroupProject = [{}];
        return;
      }
      let url = '/environment/group/project/list/' + this.envGroupId;
      this.result = this.$get(url, response => {
        this.envGroupProject = response.data;
        if (this.envGroupProject) {
          // 初始化环境数据
          this.envGroupProject.forEach(env => {
            this.disabledOption(env.projectId, true);
            this._parseDomainName(env);
          });
        }
        if (this.envGroupProject.length < 1) {
          this.envGroupProject.push({});
        }
      })
    },
    _parseDomainName(item) {
      let {environmentId} = item;
      const index = item.environments.findIndex(e => e.id === environmentId);
      if (index === -1) {
        this.$set(item, "domainName", "");
        return;
      }
      let environment = item.environments[index].config;
      if (environment) {
        const config = JSON.parse(environment);
        if (config.httpConfig && !config.httpConfig.conditions) {
          if (config.httpConfig.protocol && config.httpConfig.domain) {
            let domain = config.httpConfig.protocol + "://" + config.httpConfig.domain;
            this.$set(item, "domainName", domain);
            this.$set(item, "domainDescription", config.httpConfig.description ? config.httpConfig.description : "");
          }
        } else {
          if (config.httpConfig.conditions.length === 1) {
            if (config.tcpConfig && config.tcpConfig.server) {
              this.$set(item, "moreDomain", true);
              return;
            }
            let obj = config.httpConfig.conditions[0];
            if (obj.protocol && obj.socket) {
              this.$set(item, "domainName", obj.protocol + "://" + obj.socket);
              this.$set(item, "domainDescription", obj.description ? obj.description : "");
            }
          } else if (config.httpConfig.conditions.length > 1) {
            this.$set(item, "moreDomain", true);
            return;
          } else if (config.tcpConfig && config.tcpConfig.server) {
            this.$set(item, "domainName", config.tcpConfig.server);
            this.$set(item, "domainDescription", config.tcpConfig.description);
            return;
          }
        }
      } else {
        this.$set(item, "domainName", environment.protocol + '://' + environment.domain);
        this.$set(item, "domainDescription", environment.description ? environment.description : "");
      }
    },
    getName(row) {
      switch (row.type) {
        case "NONE":
          return this.$t("api_test.definition.document.data_set.none");
        case "MODULE":
          return this.$t("test_track.module.module");
        case "PATH":
          return this.$t("api_test.definition.api_path");
      }
    },
    getUrl(row) {
      return row.protocol + "://" + row.socket;
    },
    getDetails(row) {
      if (row && row.type === "MODULE") {
        if (row.details && row.details instanceof Array) {
          let value = "";
          row.details.forEach((item) => {
            value += item.name + ",";
          });
          if (value.endsWith(",")) {
            value = value.substr(0, value.length - 1);
          }
          return value;
        }
      } else if (row && row.type === "PATH" && row.details.length > 0 && row.details[0].name) {
        return row.details[0].value === "equals" ? this.$t("commons.adv_search.operators.equals") + row.details[0].name : this.$t("api_test.request.assertions.contains") + row.details[0].name;
      } else {
        return "";
      }
    },
    remove(index) {
      // 如果该行有项目ID，设置项目的disabled的为false
      let envGroupProject = this.envGroupProject.splice(index, 1);
      if (envGroupProject.length > 0) {
        this.disabledOption(envGroupProject[0].projectId, false);
      }
      if (this.envGroupProject.length === 0) {
        this.envGroupProject.push({});
      }
    },
    disabledOption(projectId, sign) {
      let project = this.projectList.find(project => project.id === projectId);
      if (project) {
        project.disabled = sign;
      }
    },
    change() {
      let isNeedCreate = true;
      let removeIndex = -1;
      this.envGroupProject.forEach((item, index) => {
        if (!item.projectId && !item.environmentId) {
          // 多余的空行
          if (index !== this.envGroupProject.length - 1) {
            removeIndex = index;
          }
          // 没有空行，需要创建空行
          isNeedCreate = false;
        }
      });
      if (isNeedCreate) {
        this.envGroupProject.push({});
      }
    },
    update() {
      let sign = this.valid();
      if (!sign) {
        this.$warning(this.$t('workspace.env_group.not_intact'));
        return false;
      }
      let param = {
        envGroupProject: this.envGroupProject,
        id: this.envGroupId
      };
      this.$post('/environment/group/update', param, () => {
        this.$success(this.$t('commons.modify_success'));
      })
    },
    valid() {
      if (this.envGroupProject) {
        for (let egp of this.envGroupProject) {
          if (egp) {
            let {projectId, environmentId, description} = egp;
            let book = projectId ? environmentId : !description;
            if (!book) {
              return false;
            }
          }
        }
        return true;
      }
    }
  }
}
</script>

<style scoped>
.row-form >>> .el-form-item {
  margin-bottom: 0;
}

.btn-div {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 999;
}
</style>
