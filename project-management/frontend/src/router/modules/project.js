import Layout from "metersphere-frontend/src/business/app-layout";

const ProjectHome = () => import('../../business/home/ProjectHome');
const ProjectMember = () => import('../../business/menu/member/Member');
const ProjectEnv = () => import('../../business/menu/environment/EnvironmentList');
const ProjectLog = () => import('../../business/menu/log/Log');
const ProjectCodeSegment = () => import('../../business/menu/function/CustomFunction');
const ProjectFileManage = () => import('../../business/menu/file/FileManage');
const ProjectUserGroup = () => import('../../business/menu/user.group/UserGroup');
const ProjectAppManage = () => import('../../business/menu/appmanage/AppManage');
const MessageSetting = () => import('../../business/menu/notification/MessageSettings');
const TemplateSetting = () => import('../../business/menu/template/TemplateSetting');
const ProjectVersion = () => import('../../business/menu/version/MxProjectVersion');
const ErrorReportLibrary = () => import('../../business/menu/errorreportlibrary/ErrorReportLibrary');

export default {
  path: "/project",
  name: "Project",
  redirect: '/project/home',
  component: Layout,
  children: [
    {
      path: 'home',
      component: ProjectHome,
    },
    {
      path: 'member',
      component: ProjectMember
    },
    {
      path: 'usergroup',
      component: ProjectUserGroup
    },
    {
      path: 'env',
      component: ProjectEnv
    },
    {
      path: 'file/manager',
      component: ProjectFileManage,
    },
    {
      path: 'log',
      component: ProjectLog
    },
    {
      path: 'code/segment',
      component: ProjectCodeSegment
    },
    {
      path: 'file/manage',
      component: ProjectFileManage
    },
    {
      path: 'app',
      component: ProjectAppManage
    },
    {
      path: 'template',
      component: TemplateSetting,
    },
    {
      path: 'messagesettings',
      component: MessageSetting,
    },
    {
      path: "/project/version",
      component: ProjectVersion,
    },
    {
      path: "/project/errorreportlibrary",
      component: ErrorReportLibrary,
    }
  ]
};

