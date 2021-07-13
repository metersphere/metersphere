<template>
    <el-input class="ms-search-bar" :placeholder="$t('test_track.module.search')" v-model="condition.filterText" size="small">
      <template v-if="showOperator" v-slot:append>
        <el-dropdown>
          <el-button type="primary">
            <span class="tip-font">{{ $t('commons.more_operator') }}</span>
            <i class="el-icon-arrow-down el-icon--right"/>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item v-for="(item, index) in commands" :key="index" @click.native.stop="click(item)"
                              :disabled="disabled(item.permissions)"
            >
             <span class="tip-font" v-if="!item.children">
               {{ item.label }}
             </span>
              <el-dropdown placement="right-start" v-else>
                <span class="tip-font">
                  {{ item.label }}
                  <i class="el-icon-arrow-down el-icon--right"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <template>
                    <el-dropdown-item v-for="(child, index) in item.children" :key="index" @click.native.stop="click(child)"
                                      :disabled="disabled(child.permissions)">
                      <span class="tip-font">
                        {{child.label}}
                      </span>
                    </el-dropdown-item>
                  </template>
                </el-dropdown-menu>
              </el-dropdown>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>

      </template>
    </el-input>
</template>

<script>

import moduleTrashButton from "@/business/components/api/definition/components/module/ModuleTrashButton";
import {hasPermissions} from "@/common/js/utils";

export default {
  components:{moduleTrashButton},
  name: "MsSearchBar",
  props: {
    condition: {
      type: Object,
      default() {
        return {}
      }
    },
    showOperator: Boolean,
    commands: {
      type: Array,
      default() {
        return [
          {
            label: this.$t('api_test.api_import.label'),
            callback: () => {}
          }
        ]
      }
    }
  },
  methods: {
    click(item) {
      if (item.callback) {
        item.callback();
      }
    },
    disabled(permissions) {
      if (!permissions) {
        return false;
      }
      return !hasPermissions(...permissions);
    }
  }
}
</script>

<style scoped>
.tip-font {
  font-size: 13px;
}

.el-dropdown .el-button {
  padding: 8px;
}

</style>
