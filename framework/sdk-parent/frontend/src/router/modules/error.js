import Layout from "../../business/app-layout";

const Error = {
  path: '/error',
  component: Layout,
  redirect: 'noRedirect',
  name: 'ErrorPages',
  meta: {
    title: 'Error Pages',
    icon: 'el-icon-s-opportunity'
  },
  children: [{
    path: 'error1',
    component: () => import('../../business/error-page/error1'),
    name: 'error1',
    meta: {
      title: 'error1',
      noCache: true
    }
  },
    {
      path: 'error2',
      component: () => import('../../business/error-page/error2'),
      name: 'error2',
      meta: {
        title: 'error2',
        noCache: true
      }
    },
    {
      path: 'error3',
      component: () => import('../../business/error-page/error3'),
      name: 'error3',
      meta: {
        title: 'error3',
        noCache: true
      }
    },
    {
      path: 'error4',
      component: () => import('../../business/error-page/error4'),
      name: 'error4',
      meta: {
        title: 'error4',
        noCache: true
      }
    }
  ]
}
export default Error
