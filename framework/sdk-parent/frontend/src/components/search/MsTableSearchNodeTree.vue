<template>
  <ms-table-search-component
    v-model="component.operator.value"
    :component="component"
    v-on="$listeners"
    v-bind="$attrs">
    <template v-slot="scope">
      <el-select
        v-loading="loading"
        v-model="scope.component.value"
        :placeholder="$t('commons.please_select')"
        :popper-append-to-body="false"
        @remove-tag="removeTreeTag"
        @change="changeTreeTag"
        class="search-select"
        size="small"
        collapse-tags
        multiple>
        <div class="search-div">
          <el-option
            v-for="item in treeOptions"
            class="search-select-option"
            :label="item.name"
            :value="item.id"
            :key="item.id">
          </el-option>
          <el-input
            size="small"
            class="search-input"
            :placeholder="$t('api_test.request.parameters_mock_filter_tips')"
            v-model="filterText">
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
          <el-tree
            :data="treeNodes"
            :filter-node-method="filterNode"
            @check="handleCheckChange"
            default-expand-all
            show-checkbox
            node-key="id"
            class="search-tree"
            ref="tree">
          </el-tree>
        </div>
      </el-select>
    </template>
  </ms-table-search-component>
</template>

<script>
import MsTableSearchComponent from "./MsTableSearchComponet";
import MsNodeTree from "../module/MsNodeTree";
import {cloneDeep} from "lodash";
import {getCurrentProjectID} from "../../utils/token";
import {get, post} from "../../plugins/request";


export default {
  name: "MsTableSearchNodeTree",
  components: {
    MsTableSearchComponent,
    MsNodeTree
  },
  props: ['component'],
  data() {
    return {
      treeOptions: [],
      loading: false,
      treeNodes: [],
      filterText: '',
    }
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    }
  },
  created() {
    this.init();
    // 高级搜索框再次打开时调用 component.init() 函数
    this.component.init = this.reload;
  },
  methods: {
    init() {
      this.treeOptions = [];
      let options = cloneDeep(this.component.options);
      let {url, params, type} = options;
      if (!url) return;
      if (params.projectId) {
        url += '/' + params.projectId;
      } else {
        url += '/' + getCurrentProjectID();
      }
      if (type === "POST") {
        this.loading = post(url, params || {}).then(response => {
          this.handleTreeNodes(response.data);
        })
        return;
      }
      if (type === "GET") {
        this.loading = get(this.handleGETUrl(url, params)).then(response => {
          this.handleTreeNodes(response.data);
        })
      }
    },
    reload() {
      // 数据可能变更，刷新数据
      this.init();
      // 刷新选中状态
      this.resetChecked();
    },
    resetChecked() {
      if (this.component.value && this.component.value instanceof Array) {
        for (let i = this.component.value.length - 1; i >= 0; i--) {
          let node = this.$refs.tree.getNode(this.component.value[i]);
          if (!node) {
            this.component.value.splice(i, 1);
          }
        }
      }
      this.changeTreeTag();
    },
    handleGETUrl(url, params) {
      if (!params) {
        return url;
      }
      for (let p in params) {
        if (params.hasOwnProperty(p) && params[p]) {
          url = url + "/" + params[p];
        }
      }
      return url;
    },
    handleTreeNodes(data) {
      if (!data) {
        return;
      }
      this.treeNodes = data;
      this.treeNodes.forEach(node => {
        node.name = node.name === '未规划用例' ? this.$t('api_test.unplanned_case') : node.name;
        this.buildTree(node);
      });
    },
    buildTree(node) {
      this.treeOptions.push(node);
      if (node.children) {
        for (let i = 0; i < node.children.length; i++) {
          this.buildTree(node.children[i]);
        }
      }
    },
    handleCheckChange(data, curData) {
      const {checkedKeys} = curData;
      this.component.value = checkedKeys;
    },
    changeTreeTag() {
      this.$refs.tree.setCheckedKeys(this.component.value);
    },
    removeTreeTag(data) {
      this.$refs.tree.setChecked(data, false, false);
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
  }
}
</script>

<style scoped>
.search-select {
  display: inline-block;
  width: 100%;
}

.search-div {
  max-height: 700px;
}

.search-input {
  padding: 0;
  margin-top: -5px;
}

.search-tree {
  margin-top: 6px;
}

.search-select-option {
  display: none;
}

.search-input :deep( .el-input__inner ) {
  border-radius: 2px;
  border-color: #e1dee5;
}

</style>
