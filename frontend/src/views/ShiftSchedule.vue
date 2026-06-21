<template>
  <div class="page-container">
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-date-picker
            v-model="currentMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY年MM月"
            value-format="YYYY-MM"
            @change="fetchCalendarData"
          />
          <el-select
            v-model="filterPointId"
            placeholder="客户点位"
            clearable
            style="width: 180px"
            @change="fetchCalendarData"
          >
            <el-option
              v-for="item in pointList"
              :key="item.id"
              :label="item.pointName"
              :value="item.id"
            />
          </el-select>
          <el-select
            v-model="filterPersonnelId"
            placeholder="人员"
            clearable
            style="width: 150px"
            filterable
            @change="fetchCalendarData"
          >
            <el-option
              v-for="item in personnelList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </div>
        <div class="toolbar-right">
          <el-button-group>
            <el-button
              :type="viewMode === 'calendar' ? 'primary' : 'default'"
              @click="viewMode = 'calendar'"
            >
              <el-icon><Calendar /></el-icon>
              日历视图
            </el-button>
            <el-button
              :type="viewMode === 'list' ? 'primary' : 'default'"
              @click="viewMode = 'list'"
            >
              <el-icon><List /></el-icon>
              列表视图
            </el-button>
          </el-button-group>
          <el-button
            v-if="isProjectManager"
            type="success"
            @click="batchScheduleDialogVisible = true"
          >
            <el-icon><Plus /></el-icon>
            批量排班
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="calendar-card" v-if="viewMode === 'calendar'">
      <div class="legend">
        <div class="legend-item">
          <span class="legend-badge day"></span>
          <span>白班</span>
        </div>
        <div class="legend-item">
          <span class="legend-badge night"></span>
          <span>夜班</span>
        </div>
        <div class="legend-item">
          <span class="legend-badge morning"></span>
          <span>早班</span>
        </div>
        <div class="legend-item">
          <span class="legend-badge afternoon"></span>
          <span>中班</span>
        </div>
        <div class="legend-divider"></div>
        <div class="legend-item">
          <el-badge value="" class="status-badge scheduled" />
          <span>已排班</span>
        </div>
        <div class="legend-item">
          <el-badge value="" class="status-badge checked-in" />
          <span>已签到</span>
        </div>
        <div class="legend-item">
          <el-badge value="" class="status-badge checked-out" />
          <span>已签退</span>
        </div>
        <div class="legend-item">
          <el-badge value="" class="status-badge absent" />
          <span>缺勤</span>
        </div>
      </div>

      <el-calendar v-model="selectedDate" ref="calendarRef">
        <template #date-cell="{ data }">
          <div
            class="calendar-cell"
            :class="{
              'other-month': data.type !== 'current-month',
              'is-today': data.day === todayDate
            }"
            @click="handleDateClick(data)"
          >
            <div class="cell-header">
              <span class="date-number">{{ data.day.split('-').slice(2).join('') }}</span>
            </div>
            <div class="cell-content">
              <div
                v-for="schedule in getSchedulesByDate(data.day)"
                :key="schedule.id"
                class="shift-item"
                :class="getShiftClass(schedule.shiftType)"
              >
                <div class="shift-header">
                  <el-tag
                    :type="getShiftTypeTagType(schedule.shiftType)"
                    size="small"
                    effect="dark"
                  >
                    {{ getShiftTypeText(schedule.shiftType) }}
                  </el-tag>
                  <div class="header-badges">
                    <el-tag v-if="schedule.exchangeId" type="warning" size="small" effect="plain">换</el-tag>
                    <el-badge
                      :class="getStatusBadgeClass(schedule.status)"
                      :value="getStatusBadgeValue(schedule.status)"
                    />
                  </div>
                </div>
                <div class="shift-info">
                  <span class="personnel-name">{{ schedule.personnelName }}</span>
                  <span class="point-name">{{ schedule.pointName }}</span>
                </div>
                <div class="shift-time">
                  {{ formatTime(schedule.startTime) }} - {{ formatTime(schedule.endTime) }}
                </div>
                <div class="shift-actions" v-if="isTeamLeader">
                  <el-button
                    v-if="schedule.status === 'SCHEDULED'"
                    type="primary"
                    size="small"
                    @click.stop="handleCheckIn(schedule)"
                  >
                    签到
                  </el-button>
                  <el-button
                    v-if="schedule.status === 'CHECKED_IN'"
                    type="success"
                    size="small"
                    @click.stop="handleCheckOut(schedule)"
                  >
                    签退
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </template>
      </el-calendar>
    </el-card>

    <el-card class="list-card" v-else>
      <el-table :data="listData" border stripe>
        <el-table-column prop="scheduleDate" label="日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.scheduleDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="shiftType" label="班次" width="100">
          <template #default="{ row }">
            <el-tag :type="getShiftTypeTagType(row.shiftType)" effect="dark" size="small">
              {{ getShiftTypeText(row.shiftType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="personnelName" label="人员" width="100" />
        <el-table-column prop="pointName" label="客户点位" width="150" />
        <el-table-column label="时间" width="200">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="换班" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.exchangeId" type="warning" size="small">已换班</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="签到时间" width="160">
          <template #default="{ row }">
            {{ row.checkInTime ? formatDateTime(row.checkInTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="checkOutTime" label="签退时间" width="160">
          <template #default="{ row }">
            {{ row.checkOutTime ? formatDateTime(row.checkOutTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEditSchedule(row)">编辑</el-button>
            <el-button
              v-if="isTeamLeader && row.status === 'SCHEDULED'"
              type="success"
              link
              @click="handleCheckIn(row)"
            >
              签到
            </el-button>
            <el-button
              v-if="isTeamLeader && row.status === 'CHECKED_IN'"
              type="warning"
              link
              @click="handleCheckOut(row)"
            >
              签退
            </el-button>
            <el-button
              v-if="isProjectManager"
              type="danger"
              link
              @click="handleDeleteSchedule(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="listPagination.pageNum"
        v-model:page-size="listPagination.pageSize"
        :page-sizes="[20, 50, 100]"
        :total="listPagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="fetchListData"
        @current-change="fetchListData"
      />
    </el-card>

    <el-dialog
      v-model="scheduleDialogVisible"
      :title="isEditSchedule ? '编辑排班' : '新增排班'"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form :model="scheduleForm" :rules="scheduleRules" ref="scheduleFormRef" label-width="100px">
        <el-form-item label="排班日期" prop="scheduleDate">
          <el-date-picker
            v-model="scheduleForm.scheduleDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled="isEditSchedule"
          />
        </el-form-item>
        <el-form-item label="班次模板" prop="shiftTemplateId">
          <el-select
            v-model="scheduleForm.shiftTemplateId"
            placeholder="请选择班次模板"
            style="width: 100%"
            @change="handleTemplateChange"
          >
            <el-option
              v-for="item in templateList"
              :key="item.id"
              :label="`${item.templateName} (${formatTime(item.startTime)}-${formatTime(item.endTime)})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="人员" prop="personnelId">
          <el-select
            v-model="scheduleForm.personnelId"
            placeholder="请选择人员"
            filterable
            style="width: 100%"
            @change="validatePersonnelQualification"
          >
            <el-option
              v-for="item in personnelList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="客户点位" prop="customerPointId">
          <el-select
            v-model="scheduleForm.customerPointId"
            placeholder="请选择客户点位"
            filterable
            style="width: 100%"
            @change="validatePersonnelQualification"
          >
            <el-option
              v-for="item in pointList"
              :key="item.id"
              :label="item.pointName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-alert
          v-if="qualificationWarning"
          :title="qualificationWarning"
          type="error"
          show-icon
          class="warning-alert"
        />
        <el-alert
          v-if="consecutiveNightWarning"
          :title="consecutiveNightWarning"
          type="warning"
          show-icon
          class="warning-alert"
        />

        <el-form-item label="备注">
          <el-input v-model="scheduleForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSaveSchedule">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="batchScheduleDialogVisible"
      title="批量排班"
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form :model="batchForm" :rules="batchRules" ref="batchFormRef" label-width="100px">
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="batchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="星期" prop="weekDays">
          <el-checkbox-group v-model="batchForm.weekDays">
            <el-checkbox :label="1">周一</el-checkbox>
            <el-checkbox :label="2">周二</el-checkbox>
            <el-checkbox :label="3">周三</el-checkbox>
            <el-checkbox :label="4">周四</el-checkbox>
            <el-checkbox :label="5">周五</el-checkbox>
            <el-checkbox :label="6">周六</el-checkbox>
            <el-checkbox :label="7">周日</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="班次模板" prop="shiftTemplateId">
          <el-select
            v-model="batchForm.shiftTemplateId"
            placeholder="请选择班次模板"
            style="width: 100%"
          >
            <el-option
              v-for="item in templateList"
              :key="item.id"
              :label="`${item.templateName} (${formatTime(item.startTime)}-${formatTime(item.endTime)})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="人员" prop="personnelIds">
          <el-select
            v-model="batchForm.personnelIds"
            multiple
            placeholder="请选择人员"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in personnelList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="客户点位" prop="customerPointId">
          <el-select
            v-model="batchForm.customerPointId"
            placeholder="请选择客户点位"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in pointList"
              :key="item.id"
              :label="item.pointName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="batchForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchScheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchSaveLoading" @click="handleBatchSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, List, Plus } from '@element-plus/icons-vue'
import { shiftScheduleApi, shiftTemplateApi, personnelApi, customerPointApi, qualificationApi } from '@/api'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const userStore = useUserStore()
const isProjectManager = computed(() => userStore.isProjectManager || userStore.isAdmin)
const isTeamLeader = computed(() => userStore.isTeamLeader || userStore.isAdmin || userStore.isProjectManager)

const currentMonth = ref(dayjs().format('YYYY-MM'))
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const todayDate = dayjs().format('YYYY-MM-DD')
const viewMode = ref('calendar')
const filterPointId = ref('')
const filterPersonnelId = ref('')

const templateList = ref([])
const personnelList = ref([])
const pointList = ref([])
const calendarData = ref([])
const listData = ref([])

const listPagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

const scheduleDialogVisible = ref(false)
const batchScheduleDialogVisible = ref(false)
const isEditSchedule = ref(false)
const saveLoading = ref(false)
const batchSaveLoading = ref(false)
const scheduleFormRef = ref(null)
const batchFormRef = ref(null)
const qualificationWarning = ref('')
const consecutiveNightWarning = ref('')

const scheduleForm = reactive({
  id: null,
  scheduleDate: '',
  shiftTemplateId: '',
  personnelId: '',
  customerPointId: '',
  remark: ''
})

const scheduleRules = {
  scheduleDate: [{ required: true, message: '请选择排班日期', trigger: 'change' }],
  shiftTemplateId: [{ required: true, message: '请选择班次模板', trigger: 'change' }],
  personnelId: [{ required: true, message: '请选择人员', trigger: 'change' }],
  customerPointId: [{ required: true, message: '请选择客户点位', trigger: 'change' }]
}

const batchForm = reactive({
  dateRange: [],
  weekDays: [1, 2, 3, 4, 5],
  shiftTemplateId: '',
  personnelIds: [],
  customerPointId: '',
  remark: ''
})

const batchRules = {
  dateRange: [{ required: true, message: '请选择日期范围', trigger: 'change' }],
  weekDays: [{ required: true, message: '请选择星期', trigger: 'change' }],
  shiftTemplateId: [{ required: true, message: '请选择班次模板', trigger: 'change' }],
  personnelIds: [{ required: true, message: '请选择人员', trigger: 'change' }],
  customerPointId: [{ required: true, message: '请选择客户点位', trigger: 'change' }]
}

const getShiftTypeText = (type) => {
  const map = {
    DAY: '白班',
    NIGHT: '夜班',
    MORNING: '早班',
    AFTERNOON: '中班'
  }
  return map[type] || type
}

const getShiftTypeTagType = (type) => {
  const map = {
    DAY: 'info',
    NIGHT: 'danger',
    MORNING: 'success',
    AFTERNOON: 'warning'
  }
  return map[type] || 'info'
}

const getShiftClass = (type) => {
  const map = {
    DAY: 'shift-day',
    NIGHT: 'shift-night',
    MORNING: 'shift-morning',
    AFTERNOON: 'shift-afternoon'
  }
  return map[type] || 'shift-day'
}

const getStatusText = (status) => {
  const map = {
    SCHEDULED: '已排班',
    CHECKED_IN: '已签到',
    CHECKED_OUT: '已签退',
    ABSENT: '缺勤'
  }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = {
    SCHEDULED: 'info',
    CHECKED_IN: 'primary',
    CHECKED_OUT: 'success',
    ABSENT: 'danger'
  }
  return map[status] || 'info'
}

const getStatusBadgeClass = (status) => {
  const map = {
    SCHEDULED: 'status-badge scheduled',
    CHECKED_IN: 'status-badge checked-in',
    CHECKED_OUT: 'status-badge checked-out',
    ABSENT: 'status-badge absent'
  }
  return map[status] || 'status-badge scheduled'
}

const getStatusBadgeValue = (status) => {
  return ''
}

const formatTime = (time) => {
  if (!time) return ''
  if (time.includes('T') || time.includes(' ')) {
    return time.split('T')[1]?.split(' ')[0]?.substring(0, 5) || time
  }
  return time.substring(0, 5)
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : ''
}

const formatDateTime = (datetime) => {
  return datetime ? dayjs(datetime).format('YYYY-MM-DD HH:mm') : '-'
}

const getSchedulesByDate = (date) => {
  return calendarData.value.filter(s => s.scheduleDate === date)
}

const fetchTemplateList = async () => {
  try {
    const response = await shiftTemplateApi.list({ page: 0, size: 1000 })
    const data = response.data
    templateList.value = data.content || data.data || data
  } catch (error) {
    console.error('获取班次模板失败:', error)
  }
}

const fetchPersonnelList = async () => {
  try {
    const response = await personnelApi.listByStatus('ACTIVE')
    const data = response.data
    personnelList.value = data.content || data.data || data
  } catch (error) {
    console.error('获取人员列表失败:', error)
  }
}

const fetchPointList = async () => {
  try {
    const response = await customerPointApi.list({ page: 0, size: 1000 })
    const data = response.data
    pointList.value = data.content || data.data || data
  } catch (error) {
    console.error('获取客户点位失败:', error)
  }
}

const fetchCalendarData = async () => {
  if (!currentMonth.value) return
  try {
    const params = {
      month: currentMonth.value,
      customerPointId: filterPointId.value || undefined,
      personnelId: filterPersonnelId.value || undefined
    }
    const response = await shiftScheduleApi.getCalendar(params)
    const data = response.data
    const list = data.content || data.data || data

    list.forEach(item => {
      const template = templateList.value.find(t => t.id === item.shiftTemplateId)
      if (template) {
        item.shiftType = template.shiftType
        item.startTime = template.startTime
        item.endTime = template.endTime
      }
      const personnel = personnelList.value.find(p => p.id === item.personnelId)
      if (personnel) {
        item.personnelName = personnel.name
      }
      const point = pointList.value.find(p => p.id === item.customerPointId)
      if (point) {
        item.pointName = point.pointName
      }
    })

    calendarData.value = list
  } catch (error) {
    console.error('获取日历数据失败:', error)
  }
}

const fetchListData = async () => {
  try {
    const params = {
      page: listPagination.pageNum - 1,
      size: listPagination.pageSize,
      customerPointId: filterPointId.value || undefined,
      personnelId: filterPersonnelId.value || undefined
    }
    const response = await shiftScheduleApi.list(params)
    const data = response.data
    const list = data.content || data.data || data

    list.forEach(item => {
      const template = templateList.value.find(t => t.id === item.shiftTemplateId)
      if (template) {
        item.shiftType = template.shiftType
        item.startTime = template.startTime
        item.endTime = template.endTime
      }
      const personnel = personnelList.value.find(p => p.id === item.personnelId)
      if (personnel) {
        item.personnelName = personnel.name
      }
      const point = pointList.value.find(p => p.id === item.customerPointId)
      if (point) {
        item.pointName = point.pointName
      }
    })

    listData.value = list
    listPagination.total = data.totalElements !== undefined ? data.totalElements : (data.total || 0)
  } catch (error) {
    console.error('获取列表数据失败:', error)
  }
}

const handleDateClick = (data) => {
  if (data.type === 'current-month') {
    isEditSchedule.value = false
    resetScheduleForm()
    scheduleForm.scheduleDate = data.day
    validatePersonnelQualification()
    scheduleDialogVisible.value = true
  }
}

const handleTemplateChange = () => {
  validatePersonnelQualification()
}

const validatePersonnelQualification = async () => {
  qualificationWarning.value = ''
  consecutiveNightWarning.value = ''

  if (!scheduleForm.personnelId || !scheduleForm.customerPointId) {
    return
  }

  const point = pointList.value.find(p => p.id === scheduleForm.customerPointId)
  if (point && point.keyPosition) {
    try {
      const response = await qualificationApi.listByPersonnel(scheduleForm.personnelId)
      const data = response.data
      const qualifications = data.content || data.data || data

      const now = dayjs()
      const validQualification = qualifications.find(q => {
        return q.status === 'ACTIVE' && (!q.expiryDate || dayjs(q.expiryDate).isAfter(now))
      })

      if (!validQualification) {
        qualificationWarning.value = '该人员证件已过期，不能安排到重点岗位'
      }
    } catch (error) {
      console.error('验证资质失败:', error)
    }
  }

  if (scheduleForm.shiftTemplateId) {
    const template = templateList.value.find(t => t.id === scheduleForm.shiftTemplateId)
    if (template && template.shiftType === 'NIGHT' && scheduleForm.scheduleDate) {
      const personnel = personnelList.value.find(p => p.id === scheduleForm.personnelId)
      const maxNightShifts = personnel?.maxConsecutiveNightShifts || 3

      try {
        const params = {
          personnelId: scheduleForm.personnelId,
          startDate: dayjs(scheduleForm.scheduleDate).subtract(maxNightShifts, 'day').format('YYYY-MM-DD'),
          endDate: dayjs(scheduleForm.scheduleDate).format('YYYY-MM-DD')
        }
        const response = await shiftScheduleApi.listByPersonnel(scheduleForm.personnelId, params)
        const data = response.data
        const schedules = data.content || data.data || data

        let consecutiveCount = 0
        const checkDate = dayjs(scheduleForm.scheduleDate)

        for (let i = maxNightShifts; i >= 0; i--) {
          const currentDate = checkDate.subtract(i, 'day').format('YYYY-MM-DD')
          const nightSchedule = schedules.find(s => {
            const scheduleTemplate = templateList.value.find(t => t.id === s.shiftTemplateId)
            return s.scheduleDate === currentDate && scheduleTemplate?.shiftType === 'NIGHT'
          })

          if (nightSchedule) {
            consecutiveCount++
          } else if (i > 0) {
            consecutiveCount = 0
          }
        }

        if (consecutiveCount >= maxNightShifts) {
          consecutiveNightWarning.value = `该人员连续夜班已超过${maxNightShifts}天，请调整排班`
        }
      } catch (error) {
        console.error('验证连续夜班失败:', error)
      }
    }
  }
}

const resetScheduleForm = () => {
  scheduleForm.id = null
  scheduleForm.scheduleDate = ''
  scheduleForm.shiftTemplateId = ''
  scheduleForm.personnelId = ''
  scheduleForm.customerPointId = ''
  scheduleForm.remark = ''
  qualificationWarning.value = ''
  consecutiveNightWarning.value = ''
  if (scheduleFormRef.value) {
    scheduleFormRef.value.clearValidate()
  }
}

const resetBatchForm = () => {
  batchForm.dateRange = []
  batchForm.weekDays = [1, 2, 3, 4, 5]
  batchForm.shiftTemplateId = ''
  batchForm.personnelIds = []
  batchForm.customerPointId = ''
  batchForm.remark = ''
  if (batchFormRef.value) {
    batchFormRef.value.clearValidate()
  }
}

const handleEditSchedule = (row) => {
  isEditSchedule.value = true
  Object.assign(scheduleForm, {
    id: row.id,
    scheduleDate: row.scheduleDate,
    shiftTemplateId: row.shiftTemplateId,
    personnelId: row.personnelId,
    customerPointId: row.customerPointId,
    remark: row.remark
  })
  validatePersonnelQualification()
  scheduleDialogVisible.value = true
}

const handleSaveSchedule = async () => {
  if (!scheduleFormRef.value) return
  try {
    await scheduleFormRef.value.validate()
    saveLoading.value = true

    if (qualificationWarning.value) {
      await ElMessageBox.confirm(
        qualificationWarning.value + '，是否继续保存？',
        '警告',
        { type: 'warning' }
      )
    }

    if (isEditSchedule.value) {
      await shiftScheduleApi.update(scheduleForm)
      ElMessage.success('修改成功')
    } else {
      await shiftScheduleApi.save(scheduleForm)
      ElMessage.success('新增成功')
    }

    scheduleDialogVisible.value = false
    fetchCalendarData()
    if (viewMode.value === 'list') {
      fetchListData()
    }
  } catch (error) {
    if (error !== false && error !== 'cancel') {
      console.error('保存失败:', error)
    }
  } finally {
    saveLoading.value = false
  }
}

const handleDeleteSchedule = (row) => {
  ElMessageBox.confirm('确定要删除该排班吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await shiftScheduleApi.delete(row.id)
      ElMessage.success('删除成功')
      fetchCalendarData()
      fetchListData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

const handleBatchSave = async () => {
  if (!batchFormRef.value) return
  try {
    await batchFormRef.value.validate()
    batchSaveLoading.value = true

    const [startDate, endDate] = batchForm.dateRange
    const dates = []
    let current = dayjs(startDate)
    const end = dayjs(endDate)

    while (current.isBefore(end) || current.isSame(end, 'day')) {
      const dayOfWeek = current.day() === 0 ? 7 : current.day()
      if (batchForm.weekDays.includes(dayOfWeek)) {
        dates.push(current.format('YYYY-MM-DD'))
      }
      current = current.add(1, 'day')
    }

    const batchData = {
      dates,
      shiftTemplateId: batchForm.shiftTemplateId,
      personnelIds: batchForm.personnelIds,
      customerPointId: batchForm.customerPointId,
      remark: batchForm.remark
    }

    await shiftScheduleApi.batchSave(batchData)
    ElMessage.success(`批量排班成功，共生成 ${dates.length * batchForm.personnelIds.length} 条排班记录`)

    batchScheduleDialogVisible.value = false
    resetBatchForm()
    fetchCalendarData()
    if (viewMode.value === 'list') {
      fetchListData()
    }
  } catch (error) {
    if (error !== false) {
      console.error('批量保存失败:', error)
    }
  } finally {
    batchSaveLoading.value = false
  }
}

const handleCheckIn = async (row) => {
  ElMessageBox.confirm(`确定要为 ${row.personnelName} 签到吗？`, '确认签到', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'primary'
  }).then(async () => {
    try {
      await shiftScheduleApi.checkIn(row.id)
      ElMessage.success('签到成功')
      fetchCalendarData()
      if (viewMode.value === 'list') {
        fetchListData()
      }
    } catch (error) {
      console.error('签到失败:', error)
    }
  }).catch(() => {})
}

const handleCheckOut = async (row) => {
  ElMessageBox.confirm(`确定要为 ${row.personnelName} 签退吗？`, '确认签退', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'primary'
  }).then(async () => {
    try {
      await shiftScheduleApi.checkOut(row.id)
      ElMessage.success('签退成功')
      fetchCalendarData()
      if (viewMode.value === 'list') {
        fetchListData()
      }
    } catch (error) {
      console.error('签退失败:', error)
    }
  }).catch(() => {})
}

watch(viewMode, (newVal) => {
  if (newVal === 'list') {
    fetchListData()
  }
})

onMounted(async () => {
  await fetchTemplateList()
  await fetchPersonnelList()
  await fetchPointList()
  fetchCalendarData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.toolbar-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.toolbar-left {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  gap: 15px;
  align-items: center;
}

.legend {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 20px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
}

.legend-badge {
  width: 16px;
  height: 16px;
  border-radius: 3px;
}

.legend-badge.day {
  background: #79bbff;
}

.legend-badge.night {
  background: #303133;
}

.legend-badge.morning {
  background: #67c23a;
}

.legend-badge.afternoon {
  background: #e6a23c;
}

.legend-divider {
  width: 1px;
  height: 20px;
  background: #dcdfe6;
  margin: 0 10px;
}

.status-badge {
  margin: 0;
}

.status-badge.scheduled :deep(.el-badge__content) {
  background: #909399;
}

.status-badge.checked-in :deep(.el-badge__content) {
  background: #409eff;
}

.status-badge.checked-out :deep(.el-badge__content) {
  background: #67c23a;
}

.status-badge.absent :deep(.el-badge__content) {
  background: #f56c6c;
}

.calendar-card {
  margin-bottom: 20px;
}

.calendar-cell {
  min-height: 120px;
  padding: 5px;
  cursor: pointer;
  transition: background 0.2s;
}

.calendar-cell:hover {
  background: #f0f9eb;
}

.calendar-cell.other-month {
  opacity: 0.4;
  cursor: not-allowed;
}

.calendar-cell.is-today .date-number {
  background: #409eff;
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.cell-header {
  margin-bottom: 5px;
}

.date-number {
  font-weight: bold;
  font-size: 14px;
}

.cell-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.shift-item {
  padding: 4px 6px;
  border-radius: 4px;
  font-size: 11px;
  border-left: 3px solid;
}

.shift-item.shift-day {
  background: #ecf5ff;
  border-left-color: #409eff;
}

.shift-item.shift-night {
  background: #f0f0f0;
  border-left-color: #303133;
}

.shift-item.shift-morning {
  background: #f0f9eb;
  border-left-color: #67c23a;
}

.shift-item.shift-afternoon {
  background: #fdf6ec;
  border-left-color: #e6a23c;
}

.shift-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2px;
}

.header-badges {
  display: flex;
  align-items: center;
  gap: 4px;
}

.shift-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
  margin-bottom: 2px;
}

.personnel-name {
  font-weight: 500;
  color: #303133;
}

.point-name {
  color: #909399;
  font-size: 10px;
}

.shift-time {
  color: #606266;
  font-size: 10px;
}

.shift-actions {
  margin-top: 4px;
  display: flex;
  gap: 4px;
}

.list-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.warning-alert {
  margin-bottom: 15px;
}

:deep(.el-calendar__body) {
  padding: 12px 20px;
}

:deep(.el-calendar-table .el-calendar-day) {
  padding: 0;
  height: auto;
  min-height: 130px;
}

:deep(.el-calendar-table td.is-selected .el-calendar-day) {
  background: #ecf5ff;
}
</style>
