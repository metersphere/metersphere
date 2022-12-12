<template>
  <div class="ms-table-header">
    <el-row v-if="title" class="table-title" type="flex" justify="space-between" align="middle">
      <slot name="title">
        {{ title }}
      </slot>
    </el-row>
    <el-row type="flex" justify="space-between" align="middle">
      <span class="operate-button">
        <slot name="button"></slot>
      </span>
      <span class="input-row">
        <el-row justify="space-between" v-if="showThumbnail">
          <el-col :span="16">
            <ms-table-search-bar :condition.sync="condition" @change="search"/>
          </el-col>
          <el-col :span="8">
            <font-awesome-icon class="ms-mode-aw ms-mode" :icon="['fas', 'list-ul']" @click="change('list')"/>
            <i class="el-icon-menu ms-mode-left ms-mode" @click="change('view')"/>
          </el-col>
        </el-row>
         <ms-table-search-bar :condition.sync="condition" @change="search" v-else/>
      </span>
    </el-row>
  </div>
</template>

<script>
import MsTableSearchBar from '../../../MsTableSearchBar';
import MsTableButton from '../../../MsTableButton';
import MsTableAdvSearchBar from "../../../search/MsTableAdvSearchBar";
import {getCurrentProjectID} from "../../../../utils/token";
import MsSearch from "../../../search/MsSearch";

export default {
  name: "MsFileHeader",
  components: {MsTableAdvSearchBar, MsTableSearchBar, MsTableButton, MsSearch},
  data() {
    return {
      version: this.currentVersion
    };
  },
  props: {
    title: {
      type: String,
      default() {
        return null;
      }
    },
    showCreate: {
      type: Boolean,
      default: true
    },
    showThumbnail: {
      type: Boolean,
      default: true
    },
    showImport: {
      type: Boolean,
      default: false
    },
    showRun: {
      type: Boolean,
      default: false
    },
    condition: {
      type: Object
    },
    createTip: {
      type: String,
      default() {
        return this.$t('commons.create');
      }
    },
    importTip: {
      type: String,
      default() {
        return this.$t('commons.import');
      }
    },
    createPermission: {
      type: Array,
      default() {
        return []
      }
    },
    uploadPermission: {
      type: Array,
      default() {
        return []
      }
    },
    runTip: {
      type: String,

    },
    currentVersion: {
      type: String,
    },
    isShowVersion: {
      type: Boolean,
      default: false
    },
    isTesterPermission: {
      type: Boolean,
      default: false
    },
    tip: {
      String,
      default() {
        return this.$t('commons.search_by_name');
      }
    },
    haveSearch: {
      Boolean,
      default() {
        return true;
      }
    },
    versionOptions: {
      type: Array,
      default() {
        return []
      }
    }
  },
  methods: {
    search(value) {
      this.$emit('update:condition', this.condition);
      this.$emit('search', value);
    },
    change(data) {
      this.$emit('change', data);
    }
  },
  computed: {
    isCombine() {
      return this.condition.components  && this.condition.components.length > 0;
    },
    projectId() {
      return getCurrentProjectID();
    },
  }
}
</script>

<style scoped>
.operate-button {
  margin-bottom: -5px;
}

.ms-mode {
  margin-top: 6px;
  margin-left: 5px;
}

.ms-mode-left {
  font-size: 20px;
}

.ms-mode-aw {
  font-size: 18px;
}

.ms-mode:hover {
  color: var(--color);
  cursor: pointer;
}
</style>
