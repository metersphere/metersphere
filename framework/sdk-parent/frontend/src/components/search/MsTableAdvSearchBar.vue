<template>
  <span class="adv-search-bar">
    <el-link type="primary" @click="open" v-if="showLink">{{ $t('commons.adv_search.title') }}</el-link>
    <el-dialog :title="$t('commons.adv_search.title')" :visible.sync="visible"
               custom-class="adv-dialog" :append-to-body="true">
      <div class="search-items">
        <div class="search-item" v-for="(component) in optional.components" :key="component.key">
          <el-row>
            <el-col :span="22">
              <component :is="component.name" :component="component" :components.sync="config.components"
                         @updateKey="changeSearchItemKey" :custom="condition.custom"/>
            </el-col>
            <el-col :span="2">
              <i class="el-icon-close delete-icon" @click="remove(component)"
                 v-if="optional.components.length !==1"></i>
            </el-col>
          </el-row>
        </div>
        <el-link type="primary" icon="el-icon-plus" v-if="showAddFilterLink"
                 class="add-filter-link" @click="addFilter">{{ $t('commons.adv_search.add_filter_link') }}</el-link>
      </div>
      <template v-slot:footer>
        <div class="dialog-footer">
          <el-button @click="reset">{{ $t('commons.adv_search.reset') }}</el-button>
          <el-button type="primary" @click="search">{{ $t('commons.adv_search.search') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </span>
</template>

<script>
import components from "./search-components";
import {cloneDeep, concat, slice} from "lodash";
import {_findByKey, _findIndexByKey} from "./custom-component";

export default {
  components: {...components},
  name: "MsTableAdvSearchBar",
  props: {
    condition: Object,
    showLink: {
      type: Boolean,
      default: true,
    },
    showItemSize: {
      type: Number,
      default() {
        return 4; // 默认展示的搜索条件数量
      }
    }
  },
  data() {
    return {
      visible: false,
      config: {
        components: []
      },
      optional: {
        components: []
      },
      showAddFilterLink: true,
      nullFilterKey: '',
      isInit: false,
    }
  },
  methods: {
    doInit(handleCustom) {
      let config = cloneDeep(this.condition);
      config.components.forEach(component => {
        let operator = component.operator.value;
        component.operator.value = operator === undefined ? component.operator.options[0].value : operator;
      })
      if (!handleCustom) {
        return config;
      }
      if (this.condition.custom) {
        let components = [];
        this.systemFiled = config.components.filter(co => co.custom === undefined || co.custom === false);
        this.customFiled = config.components.filter(co => co.custom === true);
        // 选项分组
        this.$set(components, 0, {label: this.$t('custom_field.system_field'), child: this.systemFiled});
        this.$set(components, 1, {label: this.$t('custom_field.name'), child: this.customFiled});
        this.$set(config, "components", components);
      }
      return config;
    },
    search() {
      let condition = {}
      this.optional.components.forEach(component => {
        let value = component.value;
        if (Array.isArray(value)) {
          if (value.length > 0) {
            this.setCondition(condition, component);
          }
        } else {
          if (value !== undefined && value !== null && value !== "") {
            this.setCondition(condition, component);
          }
        }
      });

      // 清除name
      if (this.condition.name) this.condition.name = undefined;
      // 添加组合条件
      this.condition.combine = condition;
      this.$emit('update:condition', this.condition);
      this.$emit('search', condition);
      this.visible = false;
    },
    setCondition(condition, component) {
      // 某些字段储存在自定义表但是其 custom 的值是 false
      // 因为需求要把这些字段在有选项分类时归为 系统字段 ？
      if (component.custom || ['严重程度', '处理人', '状态', '用例状态', '责任人', '用例等级'].indexOf(component.label) > -1) {
        this.handleCustomField(condition, component);
        return;
      }
      condition[component.key] = {
        operator: component.operator.value,
        value: component.value
      };
    },
    handleCustomField(condition, component) {
      if (!condition.customs) {
        condition['customs'] = [];
      }
      let value = component.value;
      if (component.label === '用例状态' && value.length === 1 && value.indexOf('Trash') > -1) {
        return;
      }
      if (component.type === "multipleMember" || component.type === "checkbox" || component.type === "multipleSelect") {
        try {
          value = JSON.stringify(component.value);
        } catch (e) {
          // nothing
        }
      }
      condition['customs'].push({
        id: component.key,
        operator: component.operator.value,
        value: value,
        type: component.type
      });
    },
    reset() {
      let source = this.condition.components;
      this.optional.components.forEach((component, index) => {
        if (component.operator.value !== undefined) {
          let operator = _findByKey(source, component.key).operator.value;
          component.operator.value = operator === undefined ? component.operator.options[0].value : operator;
        }
        if (component.value !== undefined) {
          component.value = source[index].value;
        }
        if (component.reset && component.reset instanceof Function) {
          component.reset();
        }
      })
      this.condition.combine = undefined;
      this.$emit('update:condition', this.condition);
      this.$emit('search');
    },
    init() {
      this.config = this.doInit(true);
      this.optional = this.doInit();
      if (this.optional.components.length && this.optional.components.length <= this.showItemSize) {
        this.showAddFilterLink = false;
      }
      // 默认显示几个搜索条件
      this.optional.components = slice(this.optional.components, 0, this.showItemSize);
      let allComponent = this.condition.custom ?
        concat(this.config.components[0].child, this.config.components[1].child) : this.config.components;
      for (let component of allComponent) {
        let co = _findByKey(this.optional.components, component.key);
        co ? this.$set(co, 'disable', true) : this.$set(component, 'disable', false);
      }
    },
    open() {
      this.visible = true;
      if (!this.isInit) {
        this.isInit = true;
        this.init();
      } else {
        // 切换项目时模块组件参数替换
        this.setModulesParam();
        this.refreshComponentOption();
      }
    },
    setModulesParam() {
      let comps = this.optional.components.filter(c => c.key === 'moduleIds');
      comps.forEach(comp => comp.options.params = {"projectId": this.condition.projectId});
    },
    refreshComponentOption() {
      // 当前已存在的搜索子组件中是否有需要进行刷新数据选项的
      let comps = this.optional.components.filter(cp => cp.init && cp.init instanceof Function);
      comps.forEach(comp => comp.init());
    },
    addFilter() {
      const index = _findIndexByKey(this.optional.components, this.nullFilterKey);
      if (index > -1) {
        this.$warning(this.$t('commons.adv_search.add_filter_link_tip'));
        return;
      }
      let data = {
        key: this.nullFilterKey,
        name: 'MsTableSearchInput',
        label: '',
        operator: {
          options: []
        },
        disable: false
      };
      this.optional.components.push(data);
    },
    remove(component) {
      this.showAddFilterLink = true;
      if (!this.condition.custom) {
        this.enableOptional(component, this.config.components);
      } else {
        // 系统字段和自定义字段选项合并
        const components = concat(this.config.components[0].child, this.config.components[1].child);
        this.enableOptional(component, components);
      }
      let index = _findIndexByKey(this.optional.components, component.key);
      if (index !== -1) {
        this.optional.components.splice(index, 1);
      }
    },
    enableOptional(component, components) {
      let data = _findByKey(components, component.key);
      if (data) {
        this.$set(data, 'disable', false);
      }
    },
    // 搜索组件的字段变换时触发
    changeSearchItemKey(newData, oldData) {
      let key = oldData ? oldData.key : this.nullFilterKey;
      const index = _findIndexByKey(this.optional.components, key);
      this.optional.components.splice(index, 1, newData);
      this.showAddFilterLink = false;
      let components = [];
      if (!this.condition.custom) {
        components = this.config.components;
      } else {
        components = concat(this.config.components[0].child, this.config.components[1].child);
      }
      for (let op of components) {
        if (op.disable !== undefined && op.disable === false) {
          this.showAddFilterLink = true;
          break;
        }
      }
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

.dialog-footer {
  text-align: center;
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

.delete-icon {
  font-size: 17px;
  margin-top: 8px;
}

.delete-icon:hover {
  cursor: pointer;
}

.add-filter-link {
  position: absolute;
  left: 25px;
  bottom: 50px;
}
</style>
