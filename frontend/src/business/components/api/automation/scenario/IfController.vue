<template>
  <el-card>
    <el-row>
      <div class="el-step__icon is-text ms-api-col">
        <div class="el-step__icon-inner">{{controller.index}}</div>
      </div>
      <el-button class="ms-title-buttion" size="small">{{$t('api_test.automation.if_controller')}}</el-button>

      <el-input size="small" v-model="controller.variable" style="width: 20%" :placeholder="$t('api_test.request.condition_variable')"/>

      <el-select v-model="controller.operator" :placeholder="$t('commons.please_select')" size="small"
                 @change="change" style="width: 10%;margin-left: 10px">
        <el-option v-for="o in operators" :key="o.value" :label="$t(o.label)" :value="o.value"/>
      </el-select>

      <el-input size="small" v-model="controller.value" :placeholder="$t('api_test.value')" v-if="!hasEmptyOperator" style="width: 20%;margin-left: 20px"/>
      <div style="margin-right: 20px; float: right">
        <el-switch v-model="controller.enable" style="margin-left: 10px"/>
        <el-button size="mini" icon="el-icon-copy-document" circle @click="copyRow" style="margin-left: 10px"/>
        <el-button size="mini" icon="el-icon-delete" type="danger" circle @click="remove" style="margin-left: 10px"/>
      </div>
    </el-row>
  </el-card>
</template>

<script>
  export default {
    name: "MsIfController",
    props: {
      controller: {},
      node: {},
      index: Object,
    },
    data() {
      return {
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
  .ms-api-col {
    background-color: #FCF6EE;
    border-color: #E6A23C;
    margin-right: 10px;
    color: #E6A23C;
  }

  .ms-title-buttion {
    background-color: #FCF6EE;
    margin-right: 20px;
    color: #E6A23C;
  }
</style>
