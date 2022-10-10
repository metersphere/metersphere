<template>
  <div>
    <el-tabs v-model="activeName">
      <el-tab-pane label="Interface" name="interface" v-if="request.interface && request.interface.length > 0">
        <el-table :data="request.interface">
          <el-table-column prop="columnTitle" :label="$t('operating_log.change_field')"/>
          <el-table-column prop="originalValue" :label="$t('operating_log.before_change')">
            <template v-slot:default="scope">
              <el-tooltip :content="scope.row.originalValue">
                <div class="current-value ms-tag-del">{{ scope.row.originalValue }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="newValue" :label="$t('operating_log.after_change')">
            <template v-slot:default="scope">
              <el-tooltip :content="scope.row.newValue">
                <div class="current-value ms-tag-add">{{ scope.row.newValue }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="Config Center" name="config" v-if="request.config && request.config.length > 0">
        <el-table :data="request.config">
          <el-table-column prop="columnTitle" :label="$t('operating_log.change_field')"/>
          <el-table-column prop="originalValue" :label="$t('operating_log.before_change')">
            <template v-slot:default="scope">
              <el-tooltip :content="scope.row.originalValue">
                <div class="current-value ms-tag-del">{{ scope.row.originalValue }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="newValue" :label="$t('operating_log.after_change')">
            <template v-slot:default="scope">
              <el-tooltip :content="scope.row.newValue">
                <div class="current-value ms-tag-add">{{ scope.row.newValue }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="Registry Center" name="registry" v-if="request.registry && request.registry.length > 0">
        <el-table :data="request.registry">
          <el-table-column prop="columnTitle" :label="$t('operating_log.change_field')"/>
          <el-table-column prop="originalValue" :label="$t('operating_log.before_change')">
            <template v-slot:default="scope">
              <el-tooltip :content="scope.row.originalValue">
                <div class="current-value ms-tag-del">{{ scope.row.originalValue }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="newValue" :label="$t('operating_log.after_change')">
            <template v-slot:default="scope">
              <el-tooltip :content="scope.row.newValue">
                <div class="current-value ms-tag-add">{{ scope.row.newValue }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="Consumer & service" name="service" v-if="request.service && request.service.length > 0">
        <el-table :data="request.service">
          <el-table-column prop="columnTitle" :label="$t('operating_log.change_field')">
          </el-table-column>
          <el-table-column prop="originalValue" :label="$t('operating_log.before_change')">
          </el-table-column>
          <el-table-column prop="newValue" :label="$t('operating_log.after_change')">
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="Args" name="args" v-if="request.args && request.args.length >0 ">
        <ms-api-key-value-detail :show-required="true" :items="request.args" :showDesc="true" :format="request.headerId"/>
      </el-tab-pane>
      <el-tab-pane label="Attachment Args" name="attachment" v-if="request.attachment && request.attachment.length >0 ">
        <ms-api-key-value-detail :show-required="true" :items="request.attachment" :showDesc="true" :format="request.headerId"/>
      </el-tab-pane>
    </el-tabs>

  </div>
</template>

<script>
import MsApiKeyValueDetail from "./common/ApiKeyValueDetail";

export default {
  name: "ApiDubboParameters",
  components: {MsApiKeyValueDetail},
  props: {
    request: {},
    isReadOnly: {
      type: Boolean,
      default: true
    },
  },
  data() {
    return {
      spanNum: 21,
      activeName: "config",
    }
  },
  created() {
    if (this.request.config && this.request.config.length > 0) {
      this.activeName = "config";
    } else if (this.request.registry && this.request.registry.length > 0) {
      this.activeName = "registry";
    } else if (this.request.service && this.request.service.length > 0) {
      this.activeName = "service";
    } else if (this.request.args && this.request.args.length > 0) {
      this.activeName = "args";
    } else if (this.request.attachment && this.request.attachment.length > 0) {
      this.activeName = "attachment";
    } else if (this.request.interface && this.request.interface.length > 0) {
      this.activeName = "interface";
    }
  },
  watch: {
    'request.headerId'() {
      if (this.request.config && this.request.config.length > 0) {
        this.activeName = "config";
      } else if (this.request.registry && this.request.registry.length > 0) {
        this.activeName = "registry";
      } else if (this.request.service && this.request.service.length > 0) {
        this.activeName = "service";
      } else if (this.request.args && this.request.args.length > 0) {
        this.activeName = "args";
      } else if (this.request.attachment && this.request.attachment.length > 0) {
        this.activeName = "attachment";
      }
    }
  },
  methods: {}
}
</script>

<style scoped>
.current-value {
  display: inline-block;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 120px;
}

.ms-tag-del {
  text-decoration: line-through;
  text-decoration-color: red;
  -moz-text-decoration-line: line-through;
  background: #F3E6E7;
}

.ms-tag-add {
  background: #E2ECDC;
}
</style>
