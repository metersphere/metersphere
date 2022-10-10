const get = () => {
  return localStorage.getItem('sidebarStatus')
}
const set = value => {
  localStorage.setItem('sidebarStatus', value)
}

export default {
  id: 'app',
  state: () => ({
    device: 'desktop',
    sidebar: {
      opened: get() ? !!+get() : true,
    },
  }),
  actions: {
    toggleSidebar() {
      this.sidebar.opened = !this.sidebar.opened;
      if (this.sidebar.opened) {
        set(1)
      } else {
        set(0)
      }
    },
    openSideBar() {
      set(1)
      this.sidebar.opened = true;
    },
    closeSideBar() {
      set(0)
      this.sidebar.opened = false;
    },
  }
}

