<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待审批" name="PENDING" />
        <el-tab-pane label="已通过" name="APPROVED" />
        <el-tab-pane label="已驳回" name="REJECTED" />
      </el-tabs>
      <el-form :inline="true" :model="searchForm" @submit.prevent>
        <el-form-item label="申请人">
          <el-input v-model="searchForm.requesterName" placeholder="请输入申请人姓名" clearable />
        </el-form-item>
        <el-form-item label="原排班日期">
          <el-date-picker
            v-model="searchForm.scheduleDate"
            type="date"
            placeholder="选择日期"
            clearable
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
            申请换班
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" border stripe>
        <el-table-column label="申请人" prop="requesterName" width="100" />
        <el-table-column label="原排班日期" prop="scheduleDate" width="120">
          <template #default="{ row }">
            {{ formatDate(row.scheduleDate) }}
          </template>
        </el-table-column>
        <el-table-column label="原班次" prop="shiftName" width="100" />
        <el-table-column label="原岗点" prop="pointName" width="120" />
        <el-table-column label="替班人" prop="replacementName" width="100" />
        <el-table-column label="申请原因" prop="exchangeReason" show-overflow-tooltip />
        <el-table-column label="申请时间" prop="createdAt" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="canApprove && row.status === 'PENDING'"
              type="success"
              link
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="canApprove && row.status === 'PENDING'"
              type="danger"
              link
              @click="handleReject(row)"
            >
              驳回
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
      :title="isView ? '查看详情' : '申请换班'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="原排班" prop="originalScheduleId">
          <el-select
            v-model="form.originalScheduleId"
            placeholder="请选择原排班"
            filterable
            :disabled="isView"
            @change="handleScheduleChange"
          >
            <el-option
              v-for="schedule in scheduleList"
              :key="schedule.id"
              :label="`${formatDate(schedule.scheduleDate)} - ${schedule.shiftName} - ${schedule.personnelName} - ${schedule.pointName}`"
              :value="schedule.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.originalScheduleId" label="原排班信息">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="日期">
              {{ formatDate(selectedSchedule?.scheduleDate) }}
            </el-descriptions-item>
            <el-descriptions-item label="班次">
              {{ selectedSchedule?.shiftName }}
            </el-descriptions-item>
            <el-descriptions-item label="人员">
              {{ selectedSchedule?.personnelName }}
            </el-descriptions-item>
            <el-descriptions-item label="岗点">
              {{ selectedSchedule?.pointName }}
            </el-descriptions-item>
          </el-descriptions>
        </el-form-item>
        <el-form-item label="替班人" prop="replacementId">
          <el-select
            v-model="form.replacementId"
            placeholder="请选择替班人"
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
        <el-form-item label="换班原因" prop="exchangeReason">
          <el-input
            v-model="form.exchangeReason"
            type="textarea"
            :rows="3"
            placeholder="请输入换班原因"
            :disabled="isView"
          />
        </el-form-item>
        <el-form-item v-if="isView && form.approvalRemarks" label="审批意见">
          <el-input
            v-model="form.approvalRemarks"
            type="textarea"
            :rows="2"
            disabled
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="!isView" type="primary" :loading="saveLoading" @click="handleSave">
          提交申请
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="approveDialogVisible"
      title="审批通过"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="approveForm" :rules="approveRules" ref="approveFormRef" label-width="100px">
        <el-form-item label="审批意见" prop="approvalRemarks">
          <el-input
            v-model="approveForm.approvalRemarks"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="approveLoading" @click="submitApprove">
          确认通过
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="rejectDialogVisible"
      title="审批驳回"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-width="100px">
        <el-form-item label="驳回原因" prop="approvalRemarks">
          <el-input
            v-model="rejectForm.approvalRemarks"
            type="textarea"
            :rows="3"
            placeholder="请输入驳回原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejectLoading" @click="submitReject">
          确认驳回
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { shiftExchangeApi, shiftScheduleApi, personnelApi } from '@/api'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const userStore = useUserStore()
const canCreate = computed(() => userStore.isTeamLeader)
const canApprove = computed(() => userStore.isProjectManager)

const activeTab = ref('PENDING')
const searchForm = reactive({
  requesterName: '',
  scheduleDate: ''
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
  originalScheduleId: '',
  replacementId: '',
  exchangeReason: ''
})

const rules = {
  originalScheduleId: [{ required: true, message: '请选择原排班', trigger: 'change' }],
  replacementId: [{ required: true, message: '请选择替班人', trigger: 'change' }],
  exchangeReason: [{ required: true, message: '请输入换班原因', trigger: 'blur' }]
}

const scheduleList = ref([])
const personnelList = ref([])
const selectedSchedule = ref(null)

const approveDialogVisible = ref(false)
const approveLoading = ref(false)
const approveFormRef = ref(null)
const approveForm = reactive({
  id: '',
  approvalRemarks: ''
})
const approveRules = {
  approvalRemarks: [{ required: true, message: '请输入审批意见', trigger: 'blur' }]
}

const rejectDialogVisible = ref(false)
const rejectLoading = ref(false)
const rejectFormRef = ref(null)
const rejectForm = reactive({
  id: '',
  approvalRemarks: ''
})
const rejectRules = {
  approvalRemarks: [{ required: true, message: '请输入驳回原因', trigger: 'blur' }]
}

const getStatusText = (status) => {
  const map = {
    PENDING: '待审批',
    APPROVED: '已通过',
    REJECTED: '已驳回'
  }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
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
      status: activeTab.value,
      page: pagination.pageNum - 1,
      size: pagination.pageSize
    }
    const response = await shiftExchangeApi.list(params)
    const data = response.data
    tableData.value = data.content || data.data || data
    pagination.total = data.totalElements !== undefined ? data.totalElements : (data.total || 0)
  } catch (error) {
    console.error('获取数据失败:', error)
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

const handleTabChange = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  searchForm.requesterName = ''
  searchForm.scheduleDate = ''
  handleSearch()
}

const resetForm = () => {
  form.id = null
  form.originalScheduleId = ''
  form.replacementId = ''
  form.exchangeReason = ''
  selectedSchedule.value = null
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleScheduleChange = (scheduleId) => {
  selectedSchedule.value = scheduleList.value.find(s => s.id === scheduleId)
}

const handleAdd = () => {
  isView.value = false
  resetForm()
  fetchScheduleList()
  fetchPersonnelList()
  dialogVisible.value = true
}

const handleView = (row) => {
  isView.value = true
  Object.assign(form, row)
  selectedSchedule.value = {
    scheduleDate: row.scheduleDate,
    shiftName: row.shiftName,
    personnelName: row.requesterName,
    pointName: row.pointName
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    saveLoading.value = true
    await shiftExchangeApi.create(form)
    ElMessage.success('申请提交成功')
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

const handleApprove = (row) => {
  approveForm.id = row.id
  approveForm.approvalRemarks = ''
  approveDialogVisible.value = true
}

const submitApprove = async () => {
  if (!approveFormRef.value) return
  try {
    await approveFormRef.value.validate()
    approveLoading.value = true
    await shiftExchangeApi.approve(approveForm.id, approveForm)
    ElMessage.success('审批通过')
    approveDialogVisible.value = false
    fetchData()
  } catch (error) {
    if (error !== false) {
      console.error('审批失败:', error)
    }
  } finally {
    approveLoading.value = false
  }
}

const handleReject = (row) => {
  rejectForm.id = row.id
  rejectForm.approvalRemarks = ''
  rejectDialogVisible.value = true
}

const submitReject = async () => {
  if (!rejectFormRef.value) return
  try {
    await rejectFormRef.value.validate()
    rejectLoading.value = true
    await shiftExchangeApi.reject(rejectForm.id, rejectForm)
    ElMessage.success('审批驳回')
    rejectDialogVisible.value = false
    fetchData()
  } catch (error) {
    if (error !== false) {
      console.error('驳回失败:', error)
    }
  } finally {
    rejectLoading.value = false
  }
}

onMounted(() => {
  fetchData()
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
