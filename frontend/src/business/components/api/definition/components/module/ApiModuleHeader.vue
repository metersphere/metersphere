<template>
    <div>
      <el-select class="protocol-select" size="small" v-model="condition.protocol">
        <el-option
          v-for="item in options"
          :key="item.value"
          :name="item.name"
          :value="item.value"
          :disabled="item.disabled">
        </el-option>
      </el-select>
      <el-input class="filter-input" :placeholder="$t('test_track.module.search')" v-model="condition.filterText" size="small">
        <template v-slot:append>
          <el-dropdown size="small" split-button type="primary" class="ms-api-button" @click="handleCommand('add-api')"
                       @command="handleCommand">
            <el-button icon="el-icon-folder-add" @click="addApi"></el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="add-api">{{$t('api_test.definition.request.title')}}</el-dropdown-item>
              <el-dropdown-item command="debug">{{$t('api_test.definition.request.fast_debug')}}</el-dropdown-item>
              <el-dropdown-item command="import">{{$t('api_test.api_import.label')}}</el-dropdown-item>
              <el-dropdown-item command="export">{{$t('report.export')}}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-input>

      <div @click="enableTrash" class="recycle" :class="{'is-active': condition.trashEnable}">
        <i class="el-icon-delete">  回收站</i>
      </div>

      <ms-add-basis-api
        :current-protocol="condition.protocol"
        @saveAsEdit="saveAsEdit"
        @refresh="refresh"
        ref="basisApi"/>
      <api-import ref="apiImport" @refresh="refresh"/>
    </div>
</template>

<script>
    import {OPTIONS} from "../../model/JsonData";
    import MsAddBasisApi from "../basis/AddBasisApi";
    import ApiImport from "../import/ApiImport";

    export default {
      name: "ApiModuleHeader",
      components: {ApiImport, MsAddBasisApi},
      data() {
        return {
          options: OPTIONS,
        }
      },
      props: {
        condition: {
          type: Object,
          default() {
            return {}
          }
        },
        currentModule: {
          type: Object,
          default() {
            return {}
          }
        }
      },
      methods: {
        handleCommand(e) {
          switch (e) {
            case "debug":
              this.$emit('debug');
              break;
            case "add-api":
              this.addApi();
              break;
            case "add-module":
              break;
            case "import":
              this.$refs.apiImport.open(this.currentModule);
              break;
            default:
              this.$emit('exportAPI');
              break;
          }
        },
        addApi() {
          this.$refs.basisApi.open(this.currentModule);
        },
        saveAsEdit(data) {
          this.$emit('saveAsEdit', data);
        },
        refresh() {
          this.$emit('refresh');
        },
        enableTrash() {
          this.condition.trashEnable = true;
        }
      }
    }
</script>

<style scoped>

  .protocol-select {
    width: 95px;
    height: 30px;
  }

  .filter-input {
    width: 175px;
    padding-left: 3px;
  }

  .recycle {
    padding-left: 25px;
    margin-top: 15px;
    height: 26px;
    line-height: 26px;
    margin-bottom: -10px;
  }

  .recycle:hover {
    color: #6d317c;
    cursor: pointer;
  }

  .is-active {
    background-color: #f3f6f9;
  }

</style>
