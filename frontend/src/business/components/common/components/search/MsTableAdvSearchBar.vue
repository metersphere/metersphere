<template>
  <span class="adv-search-bar">
    <el-link type="primary" @click="open">{{$t('commons.adv_search.title')}}</el-link>
    <el-dialog :title="$t('commons.adv_search.combine')" :visible.sync="visible" width="70%">
      <div>
        <div class="search-label">{{$t('commons.adv_search.combine')}}: </div>
        <el-select v-model="logic" :placeholder="$t('commons.please_select')" size="small" class="search-combine">
          <el-option v-for="op in options" :key="op.value" :label="$t(op.label)" :value="op.value"/>
        </el-select>
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
  import {default as components, LOGIC} from "./search-components";

  export default {
    components: {...components},
    name: "MsTableAdvSearchBar",
    props: {
      condition: Object,
    },
    data() {
      return {
        visible: false,
        options: [LOGIC.AND, LOGIC.OR],
        logic: this.condition.logic || LOGIC.AND.value
      }
    },
    methods: {
      search() {
        let condition = {
          logic: this.logic
        }
        this.condition.components.forEach(component => {
          if (Array.isArray(component.value)) {
            if (component.value.length > 0) {
              condition[component.key] = {
                operator: component.operator,
                value: component.value
              }
            }
          } else {
            if (component.value !== undefined && component.value !== null) {
              condition[component.key] = {
                operator: component.operator,
                value: component.value
              }
            }
          }
        });

        this.$emit('search', condition);
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
