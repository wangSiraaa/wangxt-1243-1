<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" @submit.prevent>
        <el-form-item label="点位">
          <el-select v-model="searchForm.customerPointId" placeholder="请选择点位" clearable filterable>
            <el-option
              v-for="point in pointList"
              :key="point.id"
              :label="point.pointName"
              :value="point.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="事件级别">
          <el-select v-model="searchForm.eventLevel" placeholder="请选择级别" clearable>
            <el-option label="正常" value="NORMAL" />
            <el-option label="警告" value="WARNING" />
            <el-option label="严重" value="SEVERE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待处理" value="OPEN" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户确认">
          <el-select v-model="searchForm.customerConfirmed" placeholder="请选择" clearable>
            <el-option label="已确认" :value="true" />
            <el-option label="未确认" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button v-if="canCreate" type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增事件
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" border stripe>
        <el-table-column label="事件编号" prop="eventNo" width="140" />
        <el-table-column label="发生时间" prop="eventTime" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.eventTime) }}
          </template>
        </el-table-column>
        <el-table-column label="点位" prop="pointName" width="120" />
        <el-table-column label="上报人" prop="reporterName" width="100" />
        <el-table-column label="事件类型" prop="eventType" width="120" />
        <el-table-column label="事件级别" prop="eventLevel" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.eventLevel)">
              {{ getLevelText(row.eventLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" prop="description" show-overflow-tooltip />
        <el-table-column label="客户确认" width="100" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.customerConfirmed" :size="20" color="#67c23a">
              <Check />
            </el-icon>
            <el-icon v-else :size="20" color="#f56c6c">
              <Close />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="canConfirm && !row.customerConfirmed"
              type="success"
              link
              @click="handleConfirm(row)"
            >
              确认
            </el-button>
            <el-button
              v-if="canUpdateStatus && row.status !== 'CLOSED'"
              type="primary"
              link
              @click="handleChangeStatus(row)"
            >
              更新状态
            </el-button>
            <el-button type="primary" link @click="handleView(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isView ? '查看详情' : '新增事件'"
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="点位" prop="customerPointId">
          <el-select
            v-model="form.customerPointId"
            placeholder="请选择点位"
            filterable
            :disabled="isView"
          >
            <el-option
              v-for="point in pointList"
              :key="point.id"
              :label="point.pointName"
              :value="point.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班次">
          <el-select
            v-model="form.scheduleId"
            placeholder="请选择班次（可选）"
            filterable
            clearable
            :disabled="isView"
          >
            <el-option
              v-for="schedule in scheduleList"
              :key="schedule.id"
              :label="`${formatDate(schedule.scheduleDate)} - ${schedule.shiftName} - ${schedule.personnelName}`"
              :value="schedule.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="上报人" prop="reporterId">
          <el-select
            v-model="form.reporterId"
            placeholder="请选择上报人"
            filterable
            :disabled="isView"
          >
            <el-option
              v-for="person in personnelList"
              :key="person.id"
              :label="person.name"
              :value="person.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="事件类型" prop="eventType">
          <el-input
            v-model="form.eventType"
            placeholder="请输入事件类型"
            :disabled="isView"
          />
        </el-form-item>
        <el-form-item label="事件级别" prop="eventLevel">
          <el-select
            v-model="form.eventLevel"
            placeholder="请选择事件级别"
            :disabled="isView"
          >
            <el-option label="正常" value="NORMAL" />
            <el-option label="警告" value="WARNING" />
            <el-option label="严重" value="SEVERE" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入事件描述"
            :disabled="isView"
          />
        </el-form-item>
        <el-form-item label="事件时间" prop="eventTime">
          <el-date-picker
            v-model="form.eventTime"
            type="datetime"
            placeholder="选择事件时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled="isView"
          />
        </el-form-item>
        <el-form-item v-if="isView && form.customerRemarks" label="客户备注">
          <el-input
            v-model="form.customerRemarks"
            type="textarea"
            :rows="2"
            disabled
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="!isView" type="primary" :loading="saveLoading" @click="handleSave">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="confirmDialogVisible"
      title="客户确认"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="confirmForm" :rules="confirmRules" ref="confirmFormRef" label-width="100px">
        <el-form-item label="备注" prop="customerRemarks">
          <el-input
            v-model="confirmForm.customerRemarks"
            type="textarea"
            :rows="3"
            placeholder="请输入确认备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="confirmLoading" @click="submitConfirm">
          确认
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="statusDialogVisible"
      title="更新状态"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="statusForm" :rules="statusRules" ref="statusFormRef" label-width="100px">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusTagType(currentEvent?.status)">
            {{ getStatusText(currentEvent?.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="目标状态" prop="status">
          <el-select v-model="statusForm.status" placeholder="请选择目标状态">
            <el-option
              v-for="status in availableStatuses"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="statusLoading" @click="submitStatus">
          确认更新
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Check, Close } from '@element-plus/icons-vue'
import { patrolEventApi, customerPointApi, shiftScheduleApi, personnelApi } from '@/api'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const userStore = useUserStore()
const canCreate = computed(() => userStore.isTeamLeader)
const canConfirm = computed(() => userStore.isCustomer)
const canUpdateStatus = computed(() => userStore.isTeamLeader || userStore.isProjectManager || userStore.isAdmin)

const searchForm = reactive({
  customerPointId: '',
  eventLevel: '',
  status: '',
  customerConfirmed: '',
  dateRange: []
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])
const dialogVisible = ref(false)
const isView = ref(false)
const saveLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  customerPointId: '',
  scheduleId: '',
  reporterId: '',
  eventType: '',
  eventLevel: '',
  description: '',
  eventTime: ''
})

const rules = {
  customerPointId: [{ required: true, message: '请选择点位', trigger: 'change' }],
  reporterId: [{ required: true, message: '请选择上报人', trigger: 'change' }],
  eventType: [{ required: true, message: '请输入事件类型', trigger: 'blur' }],
  eventLevel: [{ required: true, message: '请选择事件级别', trigger: 'change' }],
  description: [{ required: true, message: '请输入事件描述', trigger: 'blur' }],
  eventTime: [{ required: true, message: '请选择事件时间', trigger: 'change' }]
}

const pointList = ref([])
const scheduleList = ref([])
const personnelList = ref([])

const confirmDialogVisible = ref(false)
const confirmLoading = ref(false)
const confirmFormRef = ref(null)
const confirmForm = reactive({
  id: '',
  customerRemarks: ''
})
const confirmRules = {
  customerRemarks: [{ required: true, message: '请输入确认备注', trigger: 'blur' }]
}

const statusDialogVisible = ref(false)
const statusLoading = ref(false)
const statusFormRef = ref(null)
const currentEvent = ref(null)
const statusForm = reactive({
  status: ''
})
const statusRules = {
  status: [{ required: true, message: '请选择目标状态', trigger: 'change' }]
}

const statusFlow = {
  OPEN: ['PROCESSING'],
  PROCESSING: ['RESOLVED', 'OPEN'],
  RESOLVED: ['CLOSED', 'PROCESSING'],
  CLOSED: []
}

const availableStatuses = computed(() => {
  if (!currentEvent.value) return []
  const nextStatuses = statusFlow[currentEvent.value.status] || []
  const statusLabels = {
    OPEN: { label: '待处理', value: 'OPEN' },
    PROCESSING: { label: '处理中', value: 'PROCESSING' },
    RESOLVED: { label: '已解决', value: 'RESOLVED' },
    CLOSED: { label: '已关闭', value: 'CLOSED' }
  }
  return nextStatuses.map(s => statusLabels[s])
})

const getLevelText = (level) => {
  const map = {
    NORMAL: '正常',
    WARNING: '警告',
    SEVERE: '严重'
  }
  return map[level] || level
}

const getLevelTagType = (level) => {
  const map = {
    NORMAL: 'primary',
    WARNING: 'warning',
    SEVERE: 'danger'
  }
  return map[level] || 'info'
}

const getStatusText = (status) => {
  const map = {
    OPEN: '待处理',
    PROCESSING: '处理中',
    RESOLVED: '已解决',
    CLOSED: '已关闭'
  }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = {
    OPEN: 'warning',
    PROCESSING: 'primary',
    RESOLVED: 'success',
    CLOSED: 'info'
  }
  return map[status] || 'info'
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : ''
}

const formatDateTime = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : ''
}

const fetchData = async () => {
  try {
    const params = {
      ...searchForm,
      startDate: searchForm.dateRange?.[0] || '',
      endDate: searchForm.dateRange?.[1] || '',
      page: pagination.pageNum - 1,
      size: pagination.pageSize
    }
    delete params.dateRange
    const response = await patrolEventApi.list(params)
    const data = response.data
    tableData.value = data.content || data.data || data
    pagination.total = data.totalElements !== undefined ? data.totalElements : (data.total || 0)
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

const fetchPointList = async () => {
  try {
    const response = await customerPointApi.list({ size: 1000 })
    const data = response.data
    pointList.value = data.content || data.data || data
  } catch (error) {
    console.error('获取点位列表失败:', error)
  }
}

const fetchScheduleList = async () => {
  try {
    const response = await shiftScheduleApi.list({ size: 1000 })
    const data = response.data
    scheduleList.value = data.content || data.data || data
  } catch (error) {
    console.error('获取排班列表失败:', error)
  }
}

const fetchPersonnelList = async () => {
  try {
    const response = await personnelApi.list({ size: 1000, status: 'ACTIVE' })
    const data = response.data
    personnelList.value = data.content || data.data || data
  } catch (error) {
    console.error('获取人员列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  searchForm.customerPointId = ''
  searchForm.eventLevel = ''
  searchForm.status = ''
  searchForm.customerConfirmed = ''
  searchForm.dateRange = []
  handleSearch()
}

const resetForm = () => {
  form.id = null
  form.customerPointId = ''
  form.scheduleId = ''
  form.reporterId = ''
  form.eventType = ''
  form.eventLevel = ''
  form.description = ''
  form.eventTime = ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleAdd = () => {
  isView.value = false
  resetForm()
  fetchPointList()
  fetchScheduleList()
  fetchPersonnelList()
  dialogVisible.value = true
}

const handleView = (row) => {
  isView.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    saveLoading.value = true
    await patrolEventApi.save(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    if (error !== false) {
      console.error('保存失败:', error)
    }
  } finally {
    saveLoading.value = false
  }
}

const handleConfirm = (row) => {
  confirmForm.id = row.id
  confirmForm.customerRemarks = ''
  confirmDialogVisible.value = true
}

const submitConfirm = async () => {
  if (!confirmFormRef.value) return
  try {
    await confirmFormRef.value.validate()
    confirmLoading.value = true
    await patrolEventApi.confirmByCustomer(confirmForm.id, confirmForm)
    ElMessage.success('确认成功')
    confirmDialogVisible.value = false
    fetchData()
  } catch (error) {
    if (error !== false) {
      console.error('确认失败:', error)
    }
  } finally {
    confirmLoading.value = false
  }
}

const handleChangeStatus = (row) => {
  currentEvent.value = row
  statusForm.status = ''
  statusDialogVisible.value = true
}

const submitStatus = async () => {
  if (!statusFormRef.value || !currentEvent.value) return
  try {
    await statusFormRef.value.validate()
    statusLoading.value = true
    await patrolEventApi.updateStatus(currentEvent.value.id, statusForm.status)
    ElMessage.success('状态更新成功')
    statusDialogVisible.value = false
    fetchData()
  } catch (error) {
    if (error !== false) {
      console.error('状态更新失败:', error)
    }
  } finally {
    statusLoading.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchPointList()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
