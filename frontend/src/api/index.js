import request from '@/utils/request'

export const authApi = {
  login(data) {
    return request.post('/auth/login', data)
  },
  getCurrentUser() {
    return request.get('/auth/current-user')
  }
}

export const personnelApi = {
  list(params) {
    return request.get('/personnel/', { params })
  },
  get(id) {
    return request.get(`/personnel/${id}`)
  },
  save(data) {
    return request.post('/personnel/', data)
  },
  update(data) {
    return request.put('/personnel/', data)
  },
  delete(id) {
    return request.delete(`/personnel/${id}`)
  },
  listByStatus(status) {
    return request.get(`/personnel/status/${status}`)
  }
}

export const customerApi = {
  list(params) {
    return request.get('/customer/', { params })
  },
  get(id) {
    return request.get(`/customer/${id}`)
  },
  save(data) {
    return request.post('/customer/', data)
  },
  update(data) {
    return request.put('/customer/', data)
  },
  delete(id) {
    return request.delete(`/customer/${id}`)
  }
}

export const customerPointApi = {
  list(params) {
    return request.get('/customer-point/', { params })
  },
  get(id) {
    return request.get(`/customer-point/${id}`)
  },
  save(data) {
    return request.post('/customer-point/', data)
  },
  update(data) {
    return request.put('/customer-point/', data)
  },
  delete(id) {
    return request.delete(`/customer-point/${id}`)
  },
  listByCustomer(customerId) {
    return request.get(`/customer-point/customer/${customerId}`)
  },
  listKeyPositions() {
    return request.get('/customer-point/key-positions')
  },
  updateKeyPosition(id, isKeyPosition) {
    return request.put(`/customer-point/${id}/key-position`, null, {
      params: { isKeyPosition }
    })
  }
}

export const qualificationApi = {
  list(params) {
    return request.get('/qualification/', { params })
  },
  get(id) {
    return request.get(`/qualification/${id}`)
  },
  save(data) {
    return request.post('/qualification/', data)
  },
  update(data) {
    return request.put('/qualification/', data)
  },
  delete(id) {
    return request.delete(`/qualification/${id}`)
  },
  listByPersonnel(personnelId) {
    return request.get(`/qualification/personnel/${personnelId}`)
  },
  listByStatus(status) {
    return request.get(`/qualification/status/${status}`)
  },
  updateStatuses(data) {
    return request.post('/qualification/update-statuses', data)
  }
}

export const qualificationTypeApi = {
  list(params) {
    return request.get('/qualification-type/', { params })
  },
  get(id) {
    return request.get(`/qualification-type/${id}`)
  },
  save(data) {
    return request.post('/qualification-type/', data)
  },
  update(data) {
    return request.put('/qualification-type/', data)
  },
  delete(id) {
    return request.delete(`/qualification-type/${id}`)
  }
}

export const shiftTemplateApi = {
  list(params) {
    return request.get('/shift-template/', { params })
  },
  get(id) {
    return request.get(`/shift-template/${id}`)
  },
  save(data) {
    return request.post('/shift-template/', data)
  },
  update(data) {
    return request.put('/shift-template/', data)
  },
  delete(id) {
    return request.delete(`/shift-template/${id}`)
  },
  listByType(shiftType) {
    return request.get(`/shift-template/type/${shiftType}`)
  }
}

export const shiftScheduleApi = {
  list(params) {
    return request.get('/shift-schedule', { params })
  },
  get(id) {
    return request.get(`/shift-schedule/${id}`)
  },
  save(data) {
    return request.post('/shift-schedule', data)
  },
  update(data) {
    return request.put('/shift-schedule', data)
  },
  delete(id) {
    return request.delete(`/shift-schedule/${id}`)
  },
  batchSave(data) {
    return request.post('/shift-schedule/batch', data)
  },
  getCalendar(params) {
    return request.get('/shift-schedule/calendar', { params })
  },
  listByPersonnel(personnelId, params) {
    return request.get(`/shift-schedule/personnel/${personnelId}`, { params })
  },
  listByPoint(customerPointId, params) {
    return request.get(`/shift-schedule/point/${customerPointId}`, { params })
  },
  checkIn(id) {
    return request.put(`/shift-schedule/${id}/check-in`)
  },
  checkOut(id) {
    return request.put(`/shift-schedule/${id}/check-out`)
  }
}

export const shiftExchangeApi = {
  list(params) {
    return request.get('/shift-exchange', { params })
  },
  listPending(params) {
    return request.get('/shift-exchange/pending', { params })
  },
  get(id) {
    return request.get(`/shift-exchange/${id}`)
  },
  create(data) {
    return request.post('/shift-exchange', data)
  },
  approve(id, data) {
    return request.put(`/shift-exchange/${id}/approve`, data)
  },
  reject(id, data) {
    return request.put(`/shift-exchange/${id}/reject`, data)
  }
}

export const patrolEventApi = {
  list(params) {
    return request.get('/patrol-event', { params })
  },
  get(id) {
    return request.get(`/patrol-event/${id}`)
  },
  save(data) {
    return request.post('/patrol-event', data)
  },
  update(data) {
    return request.put('/patrol-event', data)
  },
  listUnconfirmed(params) {
    return request.get('/patrol-event/unconfirmed', { params })
  },
  getUnconfirmedCount() {
    return request.get('/patrol-event/unconfirmed/count')
  },
  updateStatus(id, status) {
    return request.put(`/patrol-event/${id}/status`, null, {
      params: { status }
    })
  },
  confirmByCustomer(id, data) {
    return request.put(`/patrol-event/${id}/confirm`, data)
  }
}

export const penaltyApi = {
  list(params) {
    return request.get('/penalty', { params })
  },
  get(id) {
    return request.get(`/penalty/${id}`)
  },
  save(data) {
    return request.post('/penalty', data)
  },
  update(data) {
    return request.put('/penalty', data)
  },
  updateStatus(id, status) {
    return request.put(`/penalty/${id}/status`, null, {
      params: { status }
    })
  },
  getTotal(params) {
    return request.get('/penalty/total', { params })
  }
}

export const penaltyTypeApi = {
  list(params) {
    return request.get('/penalty-type', { params })
  },
  get(id) {
    return request.get(`/penalty-type/${id}`)
  },
  save(data) {
    return request.post('/penalty-type', data)
  },
  update(data) {
    return request.put('/penalty-type', data)
  },
  delete(id) {
    return request.delete(`/penalty-type/${id}`)
  }
}

export const settlementApi = {
  list(params) {
    return request.get('/settlement', { params })
  },
  get(id) {
    return request.get(`/settlement/${id}`)
  },
  getDetails(id) {
    return request.get(`/settlement/${id}/details`)
  },
  generate(data) {
    return request.post('/settlement/generate', data)
  },
  confirm(id) {
    return request.put(`/settlement/${id}/confirm`)
  },
  updateStatus(id, status) {
    return request.put(`/settlement/${id}/status`, null, {
      params: { status }
    })
  }
}

export default {
  authApi,
  personnelApi,
  customerApi,
  customerPointApi,
  qualificationApi,
  qualificationTypeApi,
  shiftTemplateApi,
  shiftScheduleApi,
  shiftExchangeApi,
  patrolEventApi,
  penaltyApi,
  penaltyTypeApi,
  settlementApi
}
