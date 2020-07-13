<template>
  <span class="adv-search-bar">
    <el-link type="primary" @click="open">{{$t('commons.adv_search.title')}}</el-link>
    <el-dialog :title="$t('commons.adv_search.combine')" :visible.sync="visible" width="70%">
      <div>
<!--        如果有需求再加上-->
        <!--        <div class="search-label">{{$t('commons.adv_search.combine')}}: </div>-->
        <!--        <el-select v-model="logic" :placeholder="$t('commons.please_select')" size="small" class="search-combine">-->
        <!--          <el-option v-for="o in options" :key="o.value" :label="o.label" :value="o.value"/>-->
        <!--        </el-select>-->
        <div class="search-items">
          <component class="search-item" v-for="(component, index) in condition.components" :key="index"
                     :is="component.name" :component="component"/>
        </div>
      </div>
      <template v-slot:footer>
        <div class="dialog-footer">
          <el-button @click="visible = false">{{$t('commons.cancel')}}</el-button>
          <el-button type="primary" @click="search">{{$t('commons.adv_search.search')}}</el-button>
        </div>
      </template>
    </el-dialog>
  </span>
</template>

<script>
  import components from "./search-components";

  export default {
    components: {...components},
    name: "MsTableAdvSearchBar",
    props: {
      condition: Object,
    },
    data() {
      return {
        visible: false,
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
      search() {
        let condition = {
          // logic: this.logic // 如果有需求再加上
        }
        this.condition.components.forEach(component => {
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
      open() {
        this.visible = true;
      }
    }
  }
</script>

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

  .search-item {
    display: inline-block;
    width: 50%;
    max-width: 50%;
    margin-top: 10px;
  }
</style>
