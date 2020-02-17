<template>
    <el-menu menu-trigger="click" :default-active="$route.path" router>
        <el-submenu index="1">
            <template slot="title">
                <font-awesome-icon class="icon account" :icon="['far', 'address-card']" size="lg"/>
                <span>账号</span>
            </template>
            <el-menu-item index="/setting/user" v-permission="['admin','test_user']">用户</el-menu-item>
            <el-menu-item index="/setting/testresourcepool" v-permission="['test_user']">测试资源池</el-menu-item>
            <el-menu-item index="/setting/organization" v-permission="['org_admin']">组织</el-menu-item>
            <el-menu-item index="/setting/workspace">工作空间</el-menu-item>
            <el-menu-item>API Keys</el-menu-item>
        </el-submenu>
        <el-submenu index=2>
            <template slot="title">
                <font-awesome-icon class="icon workspace" :icon="['far', 'clone']" size="lg"/>
                <span>工作空间</span>
            </template>
            <el-menu-item index="/setting/workspace/user">成员</el-menu-item>
            <el-menu-item>证书</el-menu-item>
            <el-menu-item>测试计划</el-menu-item>
            <el-menu-item>警告</el-menu-item>
        </el-submenu>
        <el-submenu index="3">
            <template slot="title">
                <font-awesome-icon class="icon" :icon="['far', 'user']" size="lg"/>
                <span>个人</span>
            </template>
            <el-menu-item>个人设置</el-menu-item>
            <el-menu-item>API Keys</el-menu-item>
        </el-submenu>
    </el-menu>
</template>

<script>
    export default {
        name: "MsSettingMenu"
    }
</script>

<style scoped>
    .el-menu {
        border-right: 0;
    }

    .el-menu-item {
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

    .workspace {
        color: #44b349;
    }
</style>
