<template>
  <div>
    <el-row>
      <el-col :span="8">
        <el-select class="protocol-select" size="small" v-model="condition.protocol">
          <el-option
              v-for="item in options"
              :key="item.value"
              :name="item.name"
              :value="item.value"
              :disabled="item.disabled">
          </el-option>
        </el-select>
      </el-col>
      <el-col :span="15">
        <ms-search-bar
            :show-operator="showOperator"
            :condition="condition"/>
      </el-col>
    </el-row>

  </div>
</template>

<script>
import MsSearchBar from "metersphere-frontend/src/components/search/MsSearchBar";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

export default {
  name: "ApiModuleHeader",
  components: {MsSearchBar},
  data() {
    return {};
  },
  props: {
    condition: {
      type: Object,
      default() {
        return {};
      }
    },
    showOperator: Boolean,
    moduleOptions: Array,
    total: Number,
    currentModule: {
      type: Object,
      default() {
        return {};
      }
    },
    isReadOnly: {
      type: Boolean,
      default() {
        return false;
      }
    },
    options: {
      type: Array,
      default() {
        return [
          {value: 'HTTP', name: 'HTTP'},
          {value: 'TCP', name: 'TCP'},
          {value: 'SQL', name: 'SQL'},
          {value: 'DUBBO', name: 'DUBBO'}
        ];
      }
    },
    selectProjectId: {
      type: String,
      default() {
        return getCurrentProjectID();
      }
    }
  },
  computed: {
    projectId() {
      if (this.selectProjectId) {
        return this.selectProjectId;
      } else {
        return getCurrentProjectID();
      }
    },
    showTrashNode() {
      return (!this.isReadOnly);
    }
  },
  methods: {
    refresh() {
      this.$emit('refresh');
    },
    enableTrash() {
      this.condition.trashEnable = true;
    }
  }
};
</script>

<style scoped>
.protocol-select {
  width: 85px;
  height: 30px;
}

.protocol-col {
  min-width: 93px;
}

.read-only {
  width: 150px !important;
}

.filter-input {
  width: 174px;
  padding-left: 3px;
}
</style>
