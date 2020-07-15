<template>
  <span class="adv-search-bar">
    <el-link type="primary" @click="open">{{$t('commons.adv_search.title')}}</el-link>
    <el-dialog :title="$t('commons.adv_search.combine')" :visible.sync="visible" custom-class="adv-dialog"
               :append-to-body="true">
      <div>
<!--        如果有需求再加上-->
        <!--        <div class="search-label">{{$t('commons.adv_search.combine')}}: </div>-->
        <!--        <el-select v-model="logic" :placeholder="$t('commons.please_select')" size="small" class="search-combine">-->
        <!--          <el-option v-for="o in options" :key="o.value" :label="o.label" :value="o.value"/>-->
        <!--        </el-select>-->
        <div class="search-items">
          <component class="search-item" v-for="(component, index) in config.components" :key="index"
                     :is="component.name" :component="component"/>
        </div>
      </div>
      <template v-slot:footer>
        <div class="dialog-footer">
          <el-button @click="reset">{{$t('commons.adv_search.reset')}}</el-button>
          <el-button type="primary" @click="search">{{$t('commons.adv_search.search')}}</el-button>
        </div>
      </template>
    </el-dialog>
  </span>
</template>

<script>
  import components from "./search-components";
  import _ from "lodash";

  export default {
    components: {...components},
    name: "MsTableAdvSearchBar",
    props: {
      condition: Object,
    },
    data() {
      return {
        visible: false,
        config: this.init(),
        options: [{
          label: this.$t("commons.adv_search.and"),
          value: "and"
        }, {
          label: this.$t("commons.adv_search.or"),
          value: "or"
        }],
        logic: this.condition.logic || "and"
      }
    },
    methods: {
      init() { // 设置默认值
        let config = _.cloneDeep(this.condition);
        config.components.forEach(component => {
          let operator = component.operator.value;
          component.operator.value = operator === undefined ? component.operator.options[0].value : operator;
        })
        return config;
      },
      search() {
        let condition = {
          // logic: this.logic // 如果有需求再加上
        }
        this.config.components.forEach(component => {
          let operator = component.operator.value;
          let value = component.value;
          if (Array.isArray(component.value)) {
            if (component.value.length > 0) {
              condition[component.key] = {
                operator: operator,
                value: value
              }
            }
          } else {
            if (component.value !== undefined && component.value !== null && component.value !== "") {
              condition[component.key] = {
                operator: operator,
                value: value
              }
            }
          }
        });

        this.$emit('search', condition);
        this.visible = false;
      },
      reset() {
        let source = this.condition.components;
        this.config.components.forEach((component, index) => {
          let operator = source[index].operator.value;
          component.operator.value = operator === undefined ? component.operator.options[0].value : operator;
          component.value = source[index].value;
        })
      },
      open() {
        this.visible = true;
      }
    }
  }
</script>

<style>
  @media only screen and (min-width: 1870px) {
    .el-dialog.adv-dialog {
      width: 70%;
    }
  }

  @media only screen and (min-width: 1650px) and (max-width: 1869px) {
    .el-dialog.adv-dialog {
      width: 80%;
    }
  }

  @media only screen and (min-width: 1470px) and (max-width: 1649px) {
    .el-dialog.adv-dialog {
      width: 90%;
    }
  }

  @media only screen and (max-width: 1469px) {
    .el-dialog.adv-dialog {
      width: 70%;
      min-width: 695px;
    }
  }
</style>

<style scoped>
  .adv-search-bar {
    margin-left: 5px;
  }

  .dialog-footer {
    text-align: center;
  }

  .search-label {
    display: inline-block;
    width: 80px;
    box-sizing: border-box;
    padding-left: 5px;
  }

  .search-combine {
    width: 160px;
  }

  .search-items {
    width: 100%;
  }

  @media only screen and (max-width: 1469px) {
    .search-item {
      width: 100%;
    }
  }

  @media only screen and (min-width: 1470px) {
    .search-item {
      width: 50%;
    }
  }

  .search-item {
    display: inline-block;
    margin-top: 10px;
  }
</style>
