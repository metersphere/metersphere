<template>
  <el-menu menu-trigger="click" :default-active="$route.path" router class="setting">
    <el-submenu index="1" v-permission="systemPermission">
      <template v-slot:title>
        <font-awesome-icon class="icon account" :icon="['far', 'address-card']" size="lg"/>
        <span>{{ $t('commons.system') }}</span>
      </template>
      <el-menu-item v-for="menu in systems" :key="menu.index" v-permission="menu.permissions" :index="menu.index"
                    class="setting-item">
        {{ $t(menu.title) }}
      </el-menu-item>
    </el-submenu>

    <el-submenu index="2"
                v-permission="['ORGANIZATION_USER:READ', 'ORGANIZATION_WORKSPACE:READ','ORGANIZATION_SERVICE:READ','ORGANIZATION_MESSAGE:READ', 'ORGANIZATION_OPERATING_LOG:READ']">
      <template v-slot:title>
        <font-awesome-icon class="icon organization" :icon="['far', 'building']" size="lg"/>
        <span>{{ $t('commons.organization') }}</span>
      </template>
      <el-menu-item v-for="menu in organizations" v-permission="menu.permissions" :key="menu.index" :index="menu.index"
                    class="setting-item">
        {{ $t(menu.title) }}
      </el-menu-item>
    </el-submenu>

    <el-submenu index="3"
                v-permission="['WORKSPACE_USER:READ']">
      <template v-slot:title>
        <font-awesome-icon class="icon workspace" :icon="['far', 'list-alt']" size="lg"/>
        <span>{{ $t('commons.workspace') }}</span>
      </template>
      <el-menu-item v-for="menu in workspaces" v-permission="menu.permissions" :key="menu.index" :index="menu.index"
                    class="setting-item">
        {{ $t(menu.title) }}
      </el-menu-item>
      <el-submenu index="3-1"
                  v-permission="['WORKSPACE_TEMPLATE:READ']">
        <template slot="title">{{ $t('workspace.template_manage') }}</template>
        <el-menu-item v-for="menu in workspaceTemplate" v-permission="menu.permissions" :key="menu.index" :index="menu.index" class="setting-item">
          {{ $t(menu.title) }}
        </el-menu-item>
      </el-submenu>
    </el-submenu>

    <el-submenu index="4" v-permission="['PROJECT_USER:READ']">
      <template v-slot:title>
        <font-awesome-icon class="icon" :icon="['fa', 'bars']" size="lg"/>
        <span>{{ $t('commons.project') }}</span>
      </template>
      <el-menu-item v-for="menu in project" v-permission="menu.permissions" :key="menu.index" :index="menu.index"
                    class="setting-item">
        {{ $t(menu.title) }}
      </el-menu-item>
    </el-submenu>

    <!--    <el-menu-item v-for="menu in project" :key="menu.index" :index="'/setting/project/all'" class="setting-item"-->
    <!--                  v-roles="['test_user','test_manager', 'org_admin', 'admin']">-->
    <!--      <template v-slot:title>-->
    <!--        <font-awesome-icon class="icon" :icon="['fa', 'bars']" size="lg"/>-->
    <!--        <span>{{ $t(menu.title) }}</span>-->
    <!--      </template>-->
    <!--    </el-menu-item>-->

    <el-submenu index="5">
      <template v-slot:title>
        <font-awesome-icon class="icon" :icon="['far', 'user']" size="lg"/>
        <span>{{ $t('commons.personal_info') }}</span>
      </template>
      <el-menu-item v-for="menu in persons" :key="menu.index" :index="menu.index" class="setting-item">
        {{ $t(menu.title) }}
      </el-menu-item>
    </el-submenu>


  </el-menu>
</template>

<script>
import Setting from "@/business/components/settings/router";
import {LicenseKey} from '@/common/js/constants';

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const component = requireComponent.keys().length > 0 ? requireComponent("./license/LicenseMessage.vue") : null;

export default {
  name: "MsSettingMenu",
  data() {
    let getMenus = function (group) {
      let menus = [];
      Setting.children.forEach(child => {
        if (child.meta[group] === true) {
          let menu = {index: Setting.path + "/" + child.path};
          menu.title = child.meta.title;
          menu.roles = child.meta.roles;
          menu.permissions = child.meta.permissions;
          menu.valid = child.meta.valid;
          menus.push(menu);
        }
      });
      return menus;
    };
    return {
      systems: getMenus('system'),
      organizations: getMenus('organization'),
      workspaces: getMenus('workspace'),
      persons: getMenus('person'),
      project: getMenus('project'),
      workspaceTemplate: getMenus('workspaceTemplate'),
      systemPermission: [
        'SYSTEM_USER:READ','SYSTEM_ORGANIZATION:READ', 'SYSTEM_GROUP:READ',
        'ORGANIZATION_GROUP:READ', 'SYSTEM_WORKSPACE:READ','SYSTEM_TEST_POOL:READ',
        'SYSTEM_SETTING:READ','SYSTEM_QUOTA:READ','SYSTEM_AUTH:READ'
      ]
    };
  },
  methods: {
    valid() {
      Promise.all([component.default.valid(this)]).then(() => {
        let license = localStorage.getItem(LicenseKey);
        if (license != "valid") {
          this.systems.forEach(item => {
            if (item.valid === true) {
              this.systems.splice(this.systems.indexOf(item), 1);
            }
          });
        }
      });
    }
  },
  mounted() {
    if (component != null) {
      this.valid();
    }
  },
};
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
