<template>
  <api-base-component
    @copy="copyRow"
    @remove="remove"
    :data="controller"
    :show-collapse="false"
    :draggable="draggable"
    :is-max="isMax"
    :show-btn="showBtn"
    color="#6D317C"
    background-color="#FCF6EE"
    :title="$t('api_test.automation.transcation_controller')">
    <template v-slot:debugStepCode>
      <span class="ms-step-debug-code" :class="node.data.code ==='error'?'ms-req-error':'ms-req-success'" v-if="!loading && node.data.debug">
        {{ getCode() }}
      </span>
    </template>

    <template v-slot:headerLeft>
      <el-input draggable size="mini" v-model="controller.name" style="width: 20%" :placeholder="$t('api_test.automation.transcation_controller')"/>
      <el-checkbox v-model="controller.generateParentSample" style="margin-left: 20px" @change="changeGenerateParantSample">Generate Parent Sample</el-checkbox>
      <el-checkbox v-model="controller.includeTimers" @change="changeIncludeTimers">Include Timers</el-checkbox>
    </template>

  </api-base-component>
</template>

<script>
import ApiBaseComponent from "../common/ApiBaseComponent";

export default {
  name: "MsTransactionController",
  components: {ApiBaseComponent},
  props: {
    controller: {},
    node: {},
    message: String,
    isMax: {
      type: Boolean,
      default: false,
    },
    showBtn: {
      type: Boolean,
      default: true,
    },
    index: Object,
    draggable: {
      type: Boolean,
      default: false,
    },
  },
  watch: {
    message() {
      this.reload();
    },
  },
  created() {
    if (this.controller.generateParentSample == null) {
      this.controller.generateParentSample = true;
    }
    if (this.controller.includeTimers == null) {
      this.controller.includeTimers = true;
    }
  },
  data() {
    return {
      loading: false,
      operators: {
        EQ: {
          label: "commons.adv_search.operators.equals",
          value: "=="
        },
        NE: {
          label: "commons.adv_search.operators.not_equals",
          value: "!="
        },
        LIKE: {
          label: "commons.adv_search.operators.like",
          value: "=~"
        },
        NOT_LIKE: {
          label: "commons.adv_search.operators.not_like",
          value: "!~"
        },
        GT: {
          label: "commons.adv_search.operators.gt",
          value: ">"
        },
        LT: {
          label: "commons.adv_search.operators.lt",
          value: "<"
        },
        IS_EMPTY: {
          label: "commons.adv_search.operators.is_empty",
          value: "is empty"
        },
        IS_NOT_EMPTY: {
          label: "commons.adv_search.operators.is_not_empty",
          value: "is not empty"
        }
      }
    }
  },
  methods: {
    reload() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
    getCode() {
      if (this.node && this.node.data.debug) {
        if (this.node.data.code && this.node.data.code === 'error') {
          return 'error';
        } else {
          return 'success';
        }
      }
      return '';
    },
    remove() {
      this.$emit('remove', this.controller, this.node);
    },
    copyRow() {
      this.$emit('copyRow', this.controller, this.node);
    },
    change(value) {
      if (value.indexOf("empty") > 0 && !!this.controller.value) {
        this.controller.value = "";
      }
    },
    changeGenerateParantSample(value) {
      this.controller.generateParentSample = value;
      this.$emit('refReload', this.controller, this.controller);
    },
    changeIncludeTimers(value) {
      this.controller.includeTimers = value;
      this.$emit('refReload', this.controller, this.controller);
    },
  },
  computed: {
    hasEmptyOperator() {
      return !!this.controller.operator && this.controller.operator.indexOf("empty") > 0;
    }
  }
}
</script>

<style scoped>
.ms-btn {
  width: 20%;
  margin-left: 5px;
}

.ms-select {
  width: 15%;
  margin-left: 5px;
}
.ms-req-error {
  color: #F56C6C;
}

.ms-req-success {
  color: #67C23A;
}
.ms-step-debug-code {
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
