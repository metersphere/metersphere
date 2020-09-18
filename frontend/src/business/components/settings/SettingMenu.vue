<template>
  <el-menu menu-trigger="click" :default-active="$route.path" router class="setting">
    <el-submenu index="1" v-permission="['admin']">
      <template v-slot:title>
        <font-awesome-icon class="icon account" :icon="['far', 'address-card']" size="lg"/>
        <span>{{ $t('commons.system') }}</span>
      </template>
      <el-menu-item v-for="menu in systems" :key="menu.index" :index="menu.index" class="setting-item">
        {{ $t(menu.title) }}
      </el-menu-item>
    </el-submenu>

    <el-submenu index="2" v-permission="['org_admin']" v-if="isCurrentOrganizationAdmin">
      <template v-slot:title>
        <font-awesome-icon class="icon organization" :icon="['far', 'building']" size="lg"/>
        <span>{{ $t('commons.organization') }}</span>
      </template>
      <el-menu-item v-for="menu in organizations" :key="menu.index" :index="menu.index" class="setting-item">
        {{ $t(menu.title) }}
      </el-menu-item>
    </el-submenu>

    <el-submenu index="3" v-permission="['test_manager']" v-if="isCurrentWorkspaceUser">
      <template v-slot:title>
        <font-awesome-icon class="icon workspace" :icon="['far', 'list-alt']" size="lg"/>
        <span>{{ $t('commons.workspace') }}</span>
      </template>
      <el-menu-item v-for="menu in workspaces" :key="menu.index" :index="menu.index" class="setting-item">
        {{ $t(menu.title) }}
      </el-menu-item>
    </el-submenu>

    <el-submenu index="4">
      <template v-slot:title>
        <font-awesome-icon class="icon" :icon="['far', 'user']" size="lg"/>
        <span>{{ $t('commons.personal_info') }}</span>
      </template>
      <el-menu-item v-for="menu in persons" :key="menu.index" :index="menu.index" class="setting-item"
                    v-permission="menu.roles">
        {{ $t(menu.title) }}
      </el-menu-item>
    </el-submenu>

  </el-menu>
</template>

<script>
  import {checkoutCurrentOrganization, checkoutCurrentWorkspace} from "@/common/js/utils";
  import Setting from "@/business/components/settings/router";
  import {LicenseKey} from '@/common/js/constants';

  export default {
    name: "MsSettingMenu",
    data() {
      let valid = false;
      let getMenus = function (group) {
        let menus = [];
        Setting.children.forEach(child => {
          if (child.meta[group] === true) {
            let menu = {index: Setting.path + "/" + child.path}
            menu.title = child.meta.title;
            menu.roles = child.meta.roles;
            if (child.meta.valid != undefined && child.meta.valid === true) {
              menu.valid = child.meta.valid;
              valid = true;
            }
            menus.push(menu);
          }
        })
        return menus;
      }
      return {
        systems: getMenus('system'),
        organizations: getMenus('organization'),
        workspaces: getMenus('workspace'),
        persons: getMenus('person'),
        isValid: valid,
        isCurrentOrganizationAdmin: false,
        isCurrentWorkspaceUser: false,
      }
    },
    mounted() {
      if (this.isValid === true) {
        this.valid();
      }
      this.isCurrentOrganizationAdmin = checkoutCurrentOrganization();
      this.isCurrentWorkspaceUser = checkoutCurrentWorkspace();
    },
    methods: {
      valid() {
        let data = localStorage.getItem(LicenseKey);
        if (data != undefined && data != null) {
          data = JSON.parse(data);
        }
        if (data === undefined || data === null || data.status != "valid") {
          this.systems.forEach(item => {
            if (item.valid != undefined && item.valid === true) {
              this.systems.splice(this.systems.indexOf(item), 1);
            }
          })

          this.organizations.forEach(item => {
            if (item.valid != undefined && item.valid === true) {
              this.organizations.splice(this.organizations.indexOf(item), 1);
            }
          })

          this.workspaces.forEach(item => {
            if (item.valid != undefined && item.valid === true) {
              this.workspaces.splice(this.workspaces.indexOf(item), 1);
            }
          })

          this.persons.forEach(item => {
            if (item.valid != undefined && item.valid === true) {
              this.persons.splice(this.persons.indexOf(item), 1);
            }
          })
        }
      }
    }
  }
</script>

<style scoped>

  .setting {
    border-right: 0;
  }

  .setting .setting-item {
    height: 40px;
    line-height: 40px;
  }

  .icon {
    width: 24px;
    margin-right: 10px;
  }

  .account {
    color: #5a78f0;
  }

  .organization {
    color: #b33a5b;
  }

  .workspace {
    color: #44b349;
  }
</style>
