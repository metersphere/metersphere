<template>
  <el-header style="width: 100%; padding: 0px">
    <el-card>
      <el-row>
        <el-col :span="1">
          <el-tag
            size="mini"
            :style="{
              'background-color': getColor(true, api.method),
              border: getColor(true, api.method),
            }"
            class="api-el-tag">
            {{ api.method }}
          </el-tag>
        </el-col>
        <el-col :span="9">
          <div class="variable-combine">{{ api.name }}</div>
        </el-col>
        <el-col :span="8">
          <div class="variable-combine" style="margin-left: 10px">
            {{ api.path === null ? ' ' : api.path }}
          </div>
        </el-col>
        <el-col :span="4">
          <ms-environment-select
            :project-id="projectId"
            :is-read-only="isReadOnly"
            :useEnvironment="useEnvironment"
            @setEnvironment="setEnvironment"
            ref="environmentSelect"
            v-if="api.protocol === 'HTTP' || api.protocol === 'TCP'" />
        </el-col>
        <el-col :span="2">
          <!-- 保存操作 -->
          <el-button
            v-if="!isXpack || !showUpdateRule"
            type="primary"
            size="small"
            @click="saveTestCase()"
            v-prevent-re-click
            v-permission="['PROJECT_API_DEFINITION:READ+EDIT_CASE']">
            {{ saveButtonText }}
          </el-button>
          <el-dropdown
            v-else
            style="margin-left: -15px"
            v-permission="['PROJECT_API_DEFINITION:READ+EDIT_API']"
            split-button
            type="primary"
            size="small"
            @click="saveTestCase"
            v-prevent-re-click
            @command="handleCommand">
            {{ $t('commons.save') }}
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="openSyncRule"
                >{{ $t('commons.save') + '&' + $t('workstation.sync_setting') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </el-col>
      </el-row>
    </el-card>
  </el-header>
</template>

<script>
import ApiEnvironmentConfig from 'metersphere-frontend/src/components/environment/ApiEnvironmentConfig';
import MsTag from 'metersphere-frontend/src/components/MsTag';
import MsEnvironmentSelect from './MsEnvironmentSelect';
import { API_METHOD_COLOUR } from '../../model/JsonData';
import ApiCaseItem from '@/business/definition/components/case/ApiCaseItem';
import { hasLicense, hasPermission } from 'metersphere-frontend/src/utils/permission';

export default {
  name: 'ApiCaseHeader',
  components: { MsEnvironmentSelect, MsTag, ApiEnvironmentConfig, ApiCaseItem },
  data() {
    return {
      methodColorMap: new Map(API_METHOD_COLOUR),
      saveButtonText: this.$t('commons.save'),
      isXpack: false,
      showUpdateRule: false,
    };
  },
  props: {
    api: Object,
    projectId: String,
    priorities: Array,
    isReadOnly: Boolean,
    useEnvironment: String,
    isCaseEdit: Boolean,
    buttonText: String,
    condition: {
      type: Object,
      default() {
        return {};
      },
    },
    apiCase: {
      type: Object,
      default() {
        return {};
      },
    },
  },
  mounted() {
    window.addEventListener('keydown', this.keyDown);
  },
  beforeDestroy() {
    window.removeEventListener('keydown', this.keyDown); // 在页面销毁的时候记得解除
    this.$EventBus.$off('showXpackCaseBtn');
  },
  created() {
    this.api.method = this.api.method === 'ESB' ? 'TCP' : this.api.method;
    this.isXpack = !!hasLicense();
    if (this.isXpack) {
      this.$EventBus.$on('showXpackCaseBtn', (showUpdateRule) => {
        this.handleXpackCaseBtnChange(showUpdateRule);
      });
    }
    if (this.buttonText) {
      this.saveButtonText = this.buttonText;
    }
  },
  methods: {
    keyDown(e) {
      if (hasPermission('PROJECT_API_DEFINITION:READ+EDIT_CASE')) {
        if (!(e.keyCode === 83 && (e.ctrlKey || e.metaKey))) {
          return;
        }
        e.preventDefault();
        this.saveTestCase();
      }
    },
    refreshEnvironment() {
      this.$refs.environmentSelect.refreshEnvironment();
    },
    setEnvironment(data) {
      if (data) {
        this.$emit('setEnvironment', data.id);
      }
    },
    open() {
      this.$refs.searchBar.open();
    },
    addCase() {
      this.$emit('addCase');
      this.refreshEnvironment();
    },
    getColor(enable, method) {
      if (enable) {
        return this.methodColorMap.get(method);
      }
    },
    saveTestCase() {
      this.$emit('saveCase');
    },
    handleCommand(command) {
      if (command === 'openSyncRule') {
        this.$EventBus.$emit('showXpackCaseSet', false);
        this.saveTestCase();
      }
    },
    handleXpackCaseBtnChange(showUpdateRule) {
      this.showUpdateRule = showUpdateRule;
    },
  },
};
</script>

<style scoped>
.el-card-btn {
  float: right;
  top: 20px;
  right: 0px;
  padding: 0;
  background: 0 0;
  border: none;
  outline: 0;
  cursor: pointer;
  font-size: 18px;
  margin-left: 30px;
}

.variable-combine {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 10px;
}

.el-col {
  height: 32px;
  line-height: 32px;
}

.ms-api-header-select {
  margin-left: 20px;
  min-width: 100px;
}

.el-col {
  height: 32px;
  line-height: 32px;
}

.api-el-tag {
  color: white;
}

.select-all {
  margin-right: 10px;
}

.ms-col-name {
  display: inline-block;
  margin: 0 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 100px;
}
</style>
