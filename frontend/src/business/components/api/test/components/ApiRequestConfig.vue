<template>
  <div class="request-container">
    <div class="request-item" v-for="(request, index) in requests" :key="index" @click="select(request)"
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
            <span class="el-dropdown-link el-icon-more"></span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item :command="{type: 'copy', index: index}">复制请求</el-dropdown-item>
              <el-dropdown-item :command="{type: 'delete', index: index}">删除请求</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-row>
    </div>
    <el-button class="request-create" type="primary" size="mini" icon="el-icon-plus" plain @click="createRequest"/>
  </div>
</template>

<script>
  import {Request} from "../model/ScenarioModel";

  export default {
    name: "MsApiRequestConfig",
    props: {
      requests: Array,
      open: Function
    },

    data() {
      return {
        selected: 0,
      }
    },

    computed: {
      isSelected() {
        return function (request) {
          return this.selected.randomId === request.randomId;
        }
      }
    },

    methods: {
      createRequest: function () {
        let request = new Request({method: "GET"});
        this.requests.push(request);
      },
      copyRequest: function (index) {
        let request = this.requests[index];
        this.requests.push(JSON.parse(JSON.stringify(request)));
      },
      deleteRequest: function (index) {
        this.requests.splice(index, 1);
        if (this.requests.length === 0) {
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
        this.selected = request;
        this.open(request);
      }
    },

    created() {
      if (this.requests.length === 0) {
        this.createRequest();
        this.select(this.requests[0]);
      }
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
</style>
