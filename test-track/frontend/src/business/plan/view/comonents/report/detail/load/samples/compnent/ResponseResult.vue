<template>
  <div class="text-container" v-if="responseResult">
    <el-tabs v-model="activeName" v-show="isActive" class="response-result">
      <el-tab-pane
        :label="$t('api_test.definition.request.response_body')"
        name="body"
        class="pane"
      >
        <ms-sql-result-table
          v-if="isSqlType && activeName === 'body'"
          :body="responseResult.body"
        />
        <ms-code-edit
          v-if="!isSqlType && isMsCodeEditShow && activeName === 'body'"
          :mode="mode"
          :read-only="true"
          :modes="modes"
          :data.sync="responseResult.body"
          height="250px"
          ref="codeEdit"
        />
      </el-tab-pane>

      <el-tab-pane
        :label="$t('api_test.definition.request.response_header')"
        name="headers"
        class="pane"
      >
        <ms-code-edit
          :mode="'text'"
          :read-only="true"
          :data.sync="responseResult.headers"
          ref="codeEdit"
          v-if="activeName === 'headers'"
        />
      </el-tab-pane>

      <el-tab-pane
        v-if="responseResult.console"
        :label="$t('api_test.definition.request.console')"
        name="console"
        class="pane"
      >
        <ms-code-edit
          :mode="'text'"
          :read-only="true"
          :data.sync="responseResult.console"
          ref="codeEdit"
          v-if="activeName === 'console'"
        />
      </el-tab-pane>

      <el-tab-pane
        :label="$t('api_report.assertions')"
        name="assertions"
        class="pane assertions"
      >
        <ms-assertion-results
          :assertions="responseResult.assertions"
          v-if="activeName === 'assertions'"
        />
      </el-tab-pane>

      <el-tab-pane
        :label="$t('api_test.request.extract.label')"
        name="label"
        class="pane"
      >
        <ms-code-edit
          :mode="'text'"
          :read-only="true"
          :data.sync="responseResult.vars"
          v-if="activeName === 'label'"
          ref="codeEdit"
        />
      </el-tab-pane>

      <el-tab-pane
        :label="$t('api_report.request_body')"
        name="request_body"
        class="pane"
      >
        <ms-code-edit
          :mode="'text'"
          :read-only="true"
          :data.sync="reqMessages"
          v-if="activeName === 'request_body'"
          ref="codeEdit"
        />
      </el-tab-pane>

      <el-tab-pane
        v-if="activeName == 'body'"
        :disabled="true"
        name="mode"
        class="pane cookie"
      >
        <template v-slot:label>
          <ms-dropdown
            v-if="currentProtocol === 'SQL'"
            :commands="sqlModes"
            :default-command="mode"
            @command="sqlModeChange"
          />
          <ms-dropdown
            v-else
            :commands="modes"
            :default-command="mode"
            @command="modeChange"
            ref="modeDropdown"
          />
        </template>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import MsAssertionResults from "./AssertionResults";
import MsCodeEdit from "./MsCodeEdit";
import MsDropdown from "./MsDropdown";
import MsSqlResultTable from "./SqlResultTable";

export default {
  name: "MsResponseResult",

  components: {
    MsDropdown,
    MsCodeEdit,
    MsAssertionResults,
    MsSqlResultTable,
  },

  props: {
    response: Object,
    currentProtocol: String,
  },

  data() {
    return {
      isActive: true,
      activeName: "body",
      modes: ["text", "json", "xml", "html"],
      sqlModes: ["text", "table"],
      bodyFormat: {
        TEXT: "text",
        JSON: "json",
        XML: "xml",
        HTML: "html",
      },
      mode: "text",
      isMsCodeEditShow: true,
      reqMessages: "",
    };
  },
  watch: {
    response() {
      this.setBodyType();
      this.setReqMessage();
    },
    activeName: {
      handler() {
        setTimeout(() => {
          // 展开动画大概是 300ms 左右，使视觉效果更流畅
          this.$refs.codeEdit?.$el.querySelector(".ace_text-input")?.focus();
        }, 300);
      },
      immediate: true,
    },
  },
  methods: {
    modeChange(mode) {
      this.mode = mode;
    },
    sqlModeChange(mode) {
      this.mode = mode;
    },
    setBodyType() {
      if (
        this.response &&
        this.response.responseResult &&
        this.response.responseResult.headers &&
        this.response.responseResult.headers.indexOf(
          "Content-Type: application/json"
        ) > 0
      ) {
        this.mode = this.bodyFormat.JSON;
        this.$nextTick(() => {
          if (this.$refs.modeDropdown) {
            this.$refs.modeDropdown.handleCommand(this.bodyFormat.JSON);
            this.msCodeReload();
          }
        });
      }
    },
    msCodeReload() {
      this.isMsCodeEditShow = false;
      this.$nextTick(() => {
        this.isMsCodeEditShow = true;
      });
    },
    setReqMessage() {
      if (this.response) {
        if (!this.response.url) {
          this.response.url = "";
        }
        if (!this.response.headers) {
          this.response.headers = "";
        }
        if (!this.response.cookies) {
          this.response.cookies = "";
        }
        if (!this.response.body) {
          this.response.body = "";
        }
        if (!this.response.responseResult) {
          this.response.responseResult = {};
        }
        if (!this.response.responseResult.vars) {
          this.response.responseResult.vars = "";
        }
        this.reqMessages = "";
        if (this.response.url) {
          this.reqMessages +=
            this.$t("api_test.request.address") +
            ":\n" +
            this.response.url +
            "\n";
        }
        if (this.response.headers) {
          this.reqMessages +=
            this.$t("api_test.scenario.headers") +
            ":\n" +
            this.response.headers +
            "\n";
        }

        if (this.response.cookies) {
          this.reqMessages += "Cookie:" + this.response.cookies + "\n";
        }
        this.reqMessages += "Body:" + "\n" + this.response.body;
        if (this.mode === this.bodyFormat.JSON) {
          this.msCodeReload();
        }
      }
    },
  },
  mounted() {
    this.setBodyType();
    this.setReqMessage();
  },
  computed: {
    isSqlType() {
      return (
        this.currentProtocol === "SQL" &&
        this.response &&
        this.response.responseResult &&
        this.response.responseResult.responseCode === "200" &&
        this.mode === "table"
      );
    },
    responseResult() {
      return this.response && this.response.responseResult
        ? this.response.responseResult
        : {};
    },
  },
};
</script>

<style scoped>
.text-container .icon {
  padding: 5px;
}

.text-container .collapse {
  cursor: pointer;
}

.text-container .collapse:hover {
  opacity: 0.8;
}

.text-container .icon.is-active {
  transform: rotate(90deg);
}

.text-container .pane {
  background-color: #f5f5f5;
  padding: 1px 0;
  height: 250px;
  overflow-y: auto;
}

.text-container .pane.cookie {
  padding: 0;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 0px;
}

.ms-div {
  margin-top: 20px;
}

pre {
  margin: 0;
}

.response-result :deep(.el-tabs__nav) {
  float: left !important;
}
</style>
