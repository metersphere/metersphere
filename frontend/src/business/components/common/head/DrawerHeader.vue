<template>
  <div class="ms-header">
    <el-row type="flex" class="head-bar">
      <el-col :span="12">
        <div class="name-edit">
          <el-button plain size="mini" icon="el-icon-back" @click="close">{{ $t('test_track.return') }}
          </el-button>
          <span class="title">{{ title }}</span>
        </div>
      </el-col>
      <el-col :span="10" class="head-right">
        <el-button v-for="button in buttons" :disabled="button.disabled" :key="button.name" plain size="mini" @click="button.exec">
          {{ button.name }}
        </el-button>
      </el-col>
      <div class="ms-export">
        <el-dropdown>
          <span class="el-dropdown-link">
            {{ $t('commons.export') }}
            <i class="el-icon-arrow-down el-icon--right"/>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native.stop="exportJPG">JPG</el-dropdown-item>
            <el-dropdown-item @click.native.stop="exportPNG">PNG</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "DrawerHeader",
  components: {},
  data() {
    return {}
  },
  props: {
    title: String,
    buttons: []
  },
  methods: {
    exportJPG() {
      this.$emit("export","JPG")
    },
    exportPNG() {
      this.$emit("export","PNG")
    },
    close() {
      this.$emit('close');
    },
  },
}
</script>

<style scoped>

.head-bar {
  background: white;
  height: 45px;
  line-height: 45px;
  padding: 0 10px;
  border: 1px solid #EBEEF5;
  box-shadow: 0 0 2px 0 rgba(31, 31, 31, 0.15), 0 1px 2px 0 rgba(31, 31, 31, 0.15);
}

.ms-export {
  float: right;
}

.head-right {
  text-align: right;
}

.title {
  margin-left: 10px;
}
</style>
