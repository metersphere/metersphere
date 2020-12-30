<template>
  <el-tooltip placement="top" content="添加兄弟/子节点">
    <el-dropdown trigger="click">
      <i class="el-icon-plus plus"></i>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item>
          <span @click="addFieldAction({type:'add-field',isChild:false})">兄弟节点</span>
        </el-dropdown-item>
        <el-dropdown-item>
          <span @click="addFieldAction({type:'add-field',isChild:true})">子节点</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </el-tooltip>
</template>

<script>
export default {
  name: 'DropPlus',
  components: {},
  props: {
    prefix: {
      type: Array,
      default: () => []
    },
    name: {
      type: String,
      default: ''
    }
  },
  data() {
    return {}
  },
  created() {},
  mounted() {},
  methods: {
    addFieldAction(...args) {
      this.$emit('add-field', ...args)
    }
  }
}
</script>

<style lang="scss" scoped>
</style>
