import { HttpHeaders } from '@angular/common/http';

let baseUrl;
const apiPort = '/'

baseUrl = `https://startreker-netcracker.herokuapp.com${apiPort}`

export const HttpOptions = {
  headers: new HttpHeaders({
    'Content-Type':  'application/json',
    'Access-Control-Allow-Origin': '*'
  })
}

export const HttpOptionsAuthorized = {
  headers: new HttpHeaders({
    'Content-Type':  'application/json',
    'Access-Control-Allow-Origin': '*',
    'Authorization': `Bearer ${localStorage.getItem('at')}`,
    'Authorization-Refresh': `Bearer ${localStorage.getItem('rt')}`
  })
}

const auth = {
  loginUser() {
    return `${baseUrl}api/auth/sign-in`;
  },
  registerUser() {
    return `${baseUrl}api/auth/sign-up`;
  },
  logoutUser() {
    return `${baseUrl}api/log-out`;
  },
  recoverPassword() {
    return `${baseUrl}api/auth/password-recovery`;
  }
}

const carrier = {
  carriers(){
    return `${baseUrl}api/v1/admin/carrier`;
  },
  getCarriersPagin(){
    return `${baseUrl}api/v1/admin/pagination`;
  },
  getCarrierByUsername(){
    return `${baseUrl}api/v1/admin/carrier-by-username?username=`;
  }
}

const costDash = {
  getCosts(){
    return `${baseUrl}api/v1/admin/costs`;
  }
}

export const Api = {
  auth, carrier, costDash, baseUrl
}

export const options = {
  root: baseUrl
}
