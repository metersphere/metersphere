import Layout from "metersphere-frontend/src/business/app-layout";
import Upcoming from '@/business/upcoming/Upcoming'
import Focus from '@/business/focus/Focus'
import Creation from '@/business/creation/Creation'
import Dashboard from '@/business/dashboard/Dashboard'

export default {
  path: "/workstation",
  name: "workstation",
  redirect: "/workstation/dashboard",
  component: Layout,
  children: [
    {
      path: 'dashboard',
      name: 'workstationDashboard',
      component: Dashboard,
    },
    {
      path: 'upcoming',
      name: 'workstationUpcoming',
      component: Upcoming,
    },
    {
      path: 'focus',
      name: 'workstationFocus',
      component: Focus,
    },
    {
      path: 'creation',
      name: 'workstationCreation',
      component: Creation,
    },
  ]
};

