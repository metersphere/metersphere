const ProjectSetting = () => import('@/business/components/project/ProjectSetting')
const ProjectHome = () => import('@/business/components/project/home/ProjectHome')

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
  ]
};
