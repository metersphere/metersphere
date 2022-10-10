import {get, post} from 'metersphere-frontend/src/plugins/request';


export function getSystemStatisticsData() {
  return get('/system/statistics/data');
}

export function getSystemVersion() {
  return get('/system/version');
}

export function getSystemBaseSetting() {
  return get('/system/base/info');
}

export function saveSystemBaseSetting(obj) {
  return post('/system/save/base', obj);
}

export function getSystemMailServerInfo() {
  return get('/system/mail/info');
}

export function modifySystemMailServerInfo(mail) {
  return post('/system/edit/email', mail);
}

export function testMailServerConnect(mail) {
  return post('/system/testConnection', mail);
}

export function getLdapInfo() {
  return get('/system/ldap/info');
}

export function testLdapConnect(ldap) {
  return post('/ldap/test/connect', ldap);
}

export function saveLdapInfo(ldap) {
  return post('/system/save/ldap', ldap);
}

export function testLdapLogin(ldap) {
  return post('/ldap/test/login',ldap);
}
