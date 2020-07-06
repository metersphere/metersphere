<template>
  <div class="request-container">
    <draggable :list="this.scenario.requests" group="Request" class="request-draggable" ghost-class="request-ghost">
      <div class="request-item" v-for="(request, index) in this.scenario.requests" :key="index" @click="select(request)"
           :class="{'selected': isSelected(request)}">
        <el-row type="flex">
          <div class="request-method">
            {{request.method}}
          </div>
          <div class="request-name">
            {{request.name}}
          </div>
          <div class="request-btn">
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="el-dropdown-link el-icon-more"/>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :disabled="isReadOnly" :command="{type: 'copy', index: index}">
                  {{$t('api_test.request.copy')}}
                </el-dropdown-item>
                <el-dropdown-item :disabled="isReadOnly" :command="{type: 'delete', index: index}">
                  {{$t('api_test.request.delete')}}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-row>
      </div>
    </draggable>
    <el-button :disabled="isReadOnly" class="request-create" type="primary" size="mini" icon="el-icon-plus" plain @click="createRequest"/>
  </div>
</template>

<script>
  import {Request} from "../model/ScenarioModel";
  import draggable from 'vuedraggable';

  export default {
    name: "MsApiRequestConfig",

    components: {draggable},

    props: {
      scenario: Object,
      open: Function,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },

    data() {
      return {
        selected: 0,
      }
    },

    computed: {
      isSelected() {
        return function (request) {
          return this.selected.id === request.id;
        }
      }
    },

    methods: {
      createRequest: function () {
        let request = new Request();
        this.scenario.requests.push(request);
      },
      copyRequest: function (index) {
        let request = this.scenario.requests[index];
        this.scenario.requests.push(new Request(request));
      },
      deleteRequest: function (index) {
        this.scenario.requests.splice(index, 1);
        if (this.scenario.requests.length === 0) {
          this.createRequest();
        }
      },
      handleCommand: function (command) {
        switch (command.type) {
          case "copy":
            this.copyRequest(command.index);
            break;
          case "delete":
            this.deleteRequest(command.index);
            break;
        }
      },
      select: function (request) {
        request.environment = this.scenario.environment;
        if (!request.useEnvironment) {
          request.useEnvironment = false;
        }
        this.selected = request;
        this.open(request);
      }
    },

    created() {
      this.select(this.scenario.requests[0]);
    }
  }
</script>

<style scoped>
  .request-item {
    border-left: 5px solid #1E90FF;
    line-height: 40px;
    max-height: 40px;
    border-top: 1px solid #EBEEF5;
    cursor: pointer;
  }

  .request-item:first-child {
    border-top: 0;
  }

  .request-item:hover, .request-item.selected:hover {
    background-color: #ECF5FF;
  }

  .request-item.selected {
    background-color: #F5F5F5;
  }

  .request-method {
    padding: 0 5px;
    color: #1E90FF;
  }

  .request-name {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 14px;
    width: 100%;
  }

  .request-btn {
    float: right;
    text-align: center;
    height: 40px;
  }

  .request-btn .el-icon-more {
    padding: 13px;
  }

  .request-create {
    width: 100%;
  }

  .request-ghost {
    opacity: 0.5;
    background-color: #909399;
  }
</style>
