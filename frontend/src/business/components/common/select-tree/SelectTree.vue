<template>
  <div v-loading="loading">
    <div class="mask" v-show="isShowSelect"></div>
    <el-popover placement="bottom-start" :width="popoverWidth" trigger="manual" v-model="isShowSelect" @hide="popoverHide" v-outside-click="outsideClick">
      <el-input
        size="mini"
        prefix-icon="el-icon-search"
        v-model="filterText">
      </el-input>
      <el-tree class="common-tree" :width="width" ref="tree" :data="treeData" :props="obj"
               :show-checkbox="multiple"
               :node-key="obj.id"
               :check-strictly="checkStrictly"
               :expand-on-click-node="multiple&&expandClickNode"
               :check-on-click-node="checkClickNode"
               :highlight-current="true"
               @check-change="nodeClick"
               :filter-node-method="filterNode"
               @node-click="nodeClick"/>
      <el-select slot="reference" ref="select" :size="size"
                 v-model="returnDataKeys"
                 :multiple="multiple"
                 :clearable="clearable"
                 :collapse-tags="collapseTags"
                 :disabled="disabled"
                 @click.native="selectClick"
                 @remove-tag="removeTag"
                 @clear="clean"
                 class="ms-tree-select">
        <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
      <el-row>
        <el-button v-if="multiple" class="ok" @click="isShowSelect=false" size="mini" type="text">{{$t('commons.confirm')}}</el-button>
      </el-row>
    </el-popover>
  </div>
</template>

<script>
  import OutsideClick from "@/common/js/outside-click";

  export default {
    name: 'SelectTree',
    directives: {OutsideClick},
    props: {
      // 树结构数据
      data: {
        type: Array,
        default() {
          return [];
        }
      },
      obj: {
        type: Object,
        required: false,
        default: () => {
          return {
            id: 'id',// ID
            label: 'name',// 显示名称
            children: 'children', //子级字段名
            path: 'path',//路径
            content: 'content',//描述
            pid: 'pid',//父id
          }
        }
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      // 配置是否可多选
      multiple: {
        type: Boolean,
        default() {
          return false;
        }
      },
      // 配置是否可清空选择
      clearable: {
        type: Boolean,
        default() {
          return false;
        }
      },
      // 配置多选时是否将选中值按文字的形式展示
      collapseTags: {
        type: Boolean,
        default() {
          return false;
        }
      },
      // 显示复选框情况下，是否严格遵循父子不互相关联
      checkStrictly: {
        type: Boolean,
        default() {
          return false;
        }
      },
      //多选是设置点击节点是否可以选中
      checkClickNode: {
        type: Boolean,
        default() {
          return false;
        }
      },
      //多选时：点击节点展开还是点三角标
      expandClickNode: {
        type: Boolean,
        default() {
          return false;
        }
      },
      // 默认选中的节点key
      defaultKey: {
        type: [Number, String, Array, Object],
        default() {
          return [];
        }
      },
      size: {
        type: String,
        default() {
          return 'small';
        }
      },
      width: {
        type: String,
        default() {
          return '100%';
        }
      },
      height: {
        type: String,
        default() {
          return '300px';
        }
      }
    },
    //上面是父组件可传入参数
    data() {
      return {
        popoverWidth: "0px",//下拉框大小
        isShowSelect: false, // 是否显示树状选择器
        options: [],//select option选项
        returnDatas: [],//返回给父组件数组对象
        returnDataKeys: [],//返回父组件数组主键值
        filterText: "",
        loading:false,
      };
    },
    computed: {
      treeData() { // 若非树状结构，则转化为树状结构数据
        return JSON.stringify(this.data).indexOf(this.obj.children) !== -1 ? this.data : this.switchTree();
      },
    },
    mounted() {
      this.init();
    },
    methods: {
      outsideClick(e) {
        e.stopPropagation();
        this.isShowSelect = false;
      },
      init() {
        if (this.defaultKey != undefined && this.defaultKey.length > 0) {
          if (this.multiple) {
            // 多选
            if (Object.prototype.toString.call(this.defaultKey).indexOf("Array") != -1) {
              if (Object.prototype.toString.call(this.defaultKey[0]).indexOf("Object") != -1) {//对象
                this.setDatas(this.defaultKey);
              } else if (Object.prototype.toString.call(this.defaultKey[0]).indexOf("Number") != -1
                || Object.prototype.toString.call(this.defaultKey[0]).indexOf("String") != -1) {
                this.setKeys(this.defaultKey);
              } else {
                console.log("多选：传入参数类型不匹配");
                return;
              }
            } else {
              console.log("多选：传入参数类型不匹配");
              return;
            }

          } else {
            // 单选
            if (Object.prototype.toString.call(this.defaultKey).indexOf("Number") != -1
              || Object.prototype.toString.call(this.defaultKey).indexOf("String") != -1
              || Object.prototype.toString.call(this.defaultKey).indexOf("Object") != -1) {
              this.setKey(this.defaultKey);
            } else {
              console.log("单选：传入参数类型不匹配");
              return;

            }

          }
        }
      },
      //下拉框select点击[入口]
      selectClick() {
        if (this.disabled) {
          return;
        }
        this.$nextTick(function () {//设置下拉框自适应宽度
          this.popoverWidth = this.$refs.select.$el.clientWidth - 26;
        })
        //显示下拉框
        return this.isShowSelect = !this.isShowSelect
      },
      //单选: 树点击方法
      nodeClick(data, node) {
        if (!this.multiple) {//单选
          this.isShowSelect = false;
          this.setKey(node.key);
        } else {//多选
          let checkedKeys = this.$refs.tree.getCheckedKeys(); // 所有被选中的节点的 key 所组成的数组数据
          let t = [];
          this.options = checkedKeys.map((item) => {//设置option选项
            let node = this.$refs.tree.getNode(item); // 所有被选中的节点对应的node
            t.push(node.data);
            return {label: node.label, value: node.key};
          });
          this.returnDataKeys = this.options.map((item) => {
            return item.value;
          });
          this.returnDatas = t;
        }
      },
      //单选:清空选中
      clean() {
        this.$refs.tree.setCurrentKey(null);//清除树选中key
        this.returnDatas = null;
        this.returnDataKeys = '';
        this.popoverHide();

      },
      //单选:设置、初始化值 key
      setKey(thisKey) {
        this.$refs.tree.setCurrentKey(thisKey);
        let node = this.$refs.tree.getNode(thisKey);
        if (node && node.data) {
          this.setData(node.data);
        }
      },
      //单选：设置、初始化对象
      setData(data) {
        this.options = [];
        this.options.push({label: data[this.obj.label], value: data[this.obj.id]});
        this.returnDatas = data;
        this.returnDataKeys = data[this.obj.id]

      },
      //多选:设置、初始化值 keys
      setKeys(thisKeys) {
        this.$refs.tree.setCheckedKeys(thisKeys);
        this.returnDataKeys = thisKeys;
        let t = [];
        thisKeys.map((item) => {//设置option选项
          let node = this.$refs.tree.getNode(item); // 所有被选中的节点对应的node
          t.push(node.data);
          this.options.push({label: node.label, value: node.key});
          return {label: node.label, value: node.key};
        });
        this.returnDatas = t;
        this.popoverHide()
      },
      //多选:设置、初始化对象
      setDatas(data) {
        this.$refs.tree.setCheckedNodes(data);
        this.returnDatas = data;
        let t = [];
        data.map((item) => {//设置option选项
          t.push(item[this.obj.id]);
        });
        this.returnDataKeys = t;
        this.popoverHide()
      },
      // 多选,删除任一select选项的回调
      removeTag(val) {
        this.$refs.tree.setChecked(val, false);//设置为未选中
        let node = this.$refs.tree.getNode(val);//获取节点
        if (!this.checkStrictly && node.childNodes.length > 0) {
          this.treeToList(node).map(item => {
            if (item.childNodes.length <= 0) {
              this.$refs.tree.setChecked(item, false);
            }
          });
        }
        this.nodeClick();
        this.popoverHide();

      },
      //下拉框关闭执行
      popoverHide() {
        this.$emit('getValue', this.returnDataKeys, this.returnDatas ? this.returnDatas : {});
      },
      // 多选，清空所有勾选
      clearSelectedNodes() {
        let checkedKeys = this.$refs.tree.getCheckedKeys(); // 所有被选中的节点的 key 所组成的数组数据
        for (let i = 0; i < checkedKeys.length; i++) {
          this.$refs.tree.setChecked(checkedKeys[i], false);
        }
      },

      //树形转为集合
      treeToList(tree) {
        let queen = [];
        let out = [];
        queen = queen.concat(tree);
        while (queen.length) {
          let first = queen.shift();
          if (first.childNodes) {
            queen = queen.concat(first.childNodes);
          }
          out.push(first);
        }
        return out;
      },
      switchTree() {
        return this.buildTree(this.data, this.defaultValue);
      },
      // 将一维的扁平数组转换为多层级对象
      buildTree(data, id) {
        const fa = (id) => {
          const temp = [];
          for (let i = 0; i < data.length; i++) {
            const n = data[i];
            if (n[this.obj.pid] === id) {
              n[this.obj.children] = fa(n[this.obj.id]);
              temp.push(n);
            }
          }
          return temp;
        };
        return fa(id);
      },
      filterNode(value, data) {
        if (!value) return true;
        if (data.label) {
          return data.label.indexOf(value.toLowerCase()) !== -1;
        }
        return false;
      },
      reload() {
        this.loading = true
        this.$nextTick(() => {
          this.loading = false
        });
      },
    },

    watch: {
      // eslint-disable-next-line no-unused-vars
      isShowSelect(val) {
        // 隐藏select自带的下拉框
        this.$refs.select.blur();
      },
      treeData: {//监听tree数据
        handler: function () {
          this.$nextTick(() => {
            this.init();
          })
        },
        deep: true
      },
      filterText(val) {
        this.$nextTick(() => {
          this.$refs.tree.filter(val);
        });
      },
      filter(val) {
        this.$nextTick(() => {
          this.$refs.tree.filter(val);
        });
      },
    }
  };
</script>

<style scoped>
  .mask {
    height: 100%;
    position: fixed;
    top: 0;
    left: 0;
    opacity: 0;
    z-index: 11;
  }

  .common-tree {
    overflow: auto;
    min-height: 200px;
    max-height: 400px;
  }

  .ms-tree-select {
    width: 100%;
    z-index: 111;
  }
  /deep/.el-tree-node__children{
    overflow: inherit;
  }
  .ok {
    float: right;
  }

  .el-row {
    padding-top: 0px !important;
  }

</style>
