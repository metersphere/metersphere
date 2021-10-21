const ProjectSetting = () => import('@/business/components/project/ProjectSetting')
const ProjectHome = () => import('@/business/components/project/home/ProjectHome')
const ProjectMember = () => import('@/business/components/project/menu/Member')
const ProjectEnv = () => import('@/business/components/project/menu/EnvironmentList')
const ProjectLog = () => import('@/business/components/project/menu/Log')
const ProjectCodeSegment = () => import('@/business/components/project/menu/function/CustomFunction')
const ProjectFileManage = () => import('@/business/components/project/menu/file/FileManage')

export default {
  path: "/project",
  name: "Project",
  redirect: '/project/home',
  components: {
    content: ProjectSetting
  },
  children: [
    {
      path: 'home',
      name: 'projectHome',
      component: ProjectHome,
    },
    {
      path: 'member',
      component: ProjectMember
    },
    {
      path: 'env',
      component: ProjectEnv
    },
    // {
    //   path: 'file/manager',
    //   component:
    // },
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
    }

  ]
};
