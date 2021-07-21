<template>
  <api-base-component
    @copy="copyRow"
    @remove="remove"
    :data="controller"
    :show-collapse="false"
    :draggable="draggable"
    :is-max="isMax"
    :show-btn="showBtn"
    color="#E6A23C"
    background-color="#FCF6EE"
    :title="$t('api_test.automation.if_controller')">

    <template v-slot:headerLeft>

      <el-input draggable size="mini" v-model="controller.variable" style="width: 15%" :placeholder="$t('api_test.request.condition_variable')"/>

      <el-select v-model="controller.operator" :placeholder="$t('commons.please_select')" size="mini"
                 @change="change" class="ms-select">
        <el-option v-for="o in operators" :key="o.value" :label="$t(o.label)" :value="o.value"/>
      </el-select>

      <el-input draggable size="mini" v-model="controller.value" :placeholder="$t('api_test.value')" v-if="!hasEmptyOperator" class="ms-btn"/>

      <el-input draggable size="mini" v-model="controller.remark" :placeholder="$t('commons.remark')" v-if="!hasEmptyOperator && !isMax" class="ms-btn"/>

    </template>

    <template v-slot:debugStepCode>
      <span class="ms-step-debug-code" :class="node.data.code ==='error'?'ms-req-error':'ms-req-success'" v-if="!loading && node.data.debug">
        {{ getCode() }}
      </span>
    </template>
  </api-base-component>
</template>

<script>
  import ApiBaseComponent from "../common/ApiBaseComponent";

  export default {
    name: "MsIfController",
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
    watch: {
      message() {
        this.reload();
      },
    },
    methods: {
      reload() {
        this.loading = true
        this.$nextTick(() => {
          this.loading = false
        })
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
      }
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
