<template>
  <el-table :data="assertions" :row-style="getRowStyle" :header-cell-style="getRowStyle">
    <el-table-column prop="name" :label="$t('api_report.assertions_name')" width="200" show-overflow-tooltip/>
    <el-table-column prop="content" :label="$t('api_report.assertions_content')" width="300" show-overflow-tooltip/>
    <el-table-column prop="message" :label="$t('api_report.assertions_error_message')"/>
    <el-table-column prop="pass" :label="$t('api_report.assertions_is_success')" width="100">
      <template v-slot:default="{row}">
        <el-tag size="mini" type="success" v-if="row.pass">
          {{ $t('api_report.success') }}
        </el-tag>
        <el-tag size="mini" type="danger" v-else>
          {{ $t('api_report.fail') }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="script">
      <template v-slot:default="{row}">
        <div class="assertion-item btn circle" v-if="row.script">
          <i class="el-icon-view el-button el-button--primary el-button--mini is-circle" circle
             @click="showPage(row.script)"/>
        </div>
      </template>
    </el-table-column>
    <el-dialog :title="$t('api_test.request.assertions.script')" :visible.sync="visible" width="900px" append-to-body>
      <el-row type="flex" justify="space-between" align="middle" class="quick-script-block">
        <el-col :span="codeSpan" class="script-content">
          <ms-code-edit v-if="isCodeEditAlive"
                        :read-only="disabled"
                        :data.sync="scriptContent" theme="eclipse" :modes="['java','python']"
                        ref="codeEdit"/>
        </el-col>
      </el-row>
    </el-dialog>
  </el-table>
</template>

<script>

import MsCodeEdit from "@/business/components/common/components/MsCodeEdit";

export default {
  name: "MsAssertionResults",
  components: {MsCodeEdit},
  props: {
    assertions: Array
  },

  data() {
    return {
      visible: false,
      disabled: false,
      codeSpan: 20,
      isCodeEditAlive: true,
      scriptContent: '',
    }
  },
  methods: {
    getRowStyle() {
      return {backgroundColor: "#F5F5F5"};
    },
    showPage(script) {
      this.disabled = true;
      this.visible = true;
      this.scriptContent = script;
      this.reload();
    },
    reload() {
      this.isCodeEditAlive = false;
      this.$nextTick(() => (this.isCodeEditAlive = true));
    },
  },
}
</script>

<style scoped>

.assertion-item.btn.circle {
  text-align: right;
  min-width: 80px;
}

.script-content {
  height: calc(100vh - 570px);
  min-height: 440px;
}

.quick-script-block {
  margin-bottom: 10px;
}
</style>
