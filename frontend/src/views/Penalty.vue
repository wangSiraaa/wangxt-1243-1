<template>
  <div class="page-container">
    <el-card class="stats-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-label">总扣罚金额</div>
            <div class="stat-value total">¥{{ stats.totalAmount }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-label">未缴纳金额</div>
            <div class="stat-value unpaid">¥{{ stats.unpaidAmount }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-label">已豁免金额</div>
            <div class="stat-value waived">¥{{ stats.waivedAmount }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" @submit.prevent>
        <el-form-item label="人员">
          <el-select v-model="searchForm.personnelId" placeholder="请选择人员" clearable filterable>
            <el-option
              v-for="person in personnelList"
              :key="person.id"
              :label="person.name"
              :value="person.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="扣罚类型">
          <el-select v-model="searchForm.penaltyTypeId" placeholder="请选择扣罚类型" clearable>
            <el-option
              v-for="type in penaltyTypeList"
              :key="type.id"
              :label="type.typeName"
              :value="type.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="未缴纳" value="UNPAID" />
            <el-option label="已缴纳" value="PAID" />
            <el-option label="已豁免" value="WAIVED" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
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
            新增扣罚
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" border stripe>
        <el-table-column label="扣罚编号" prop="penaltyNo" width="140" />
        <el-table-column label="扣罚日期" prop="penaltyDate" width="120">
          <template #default="{ row }">
            {{ formatDate(row.penaltyDate) }}
          </template>
        </el-table-column>
        <el-table-column label="人员" prop="personnelName" width="100" />
        <el-table-column label="扣罚类型" prop="penaltyTypeName" width="120" />
        <el-table-column label="金额" prop="amount" width="100">
          <template #default="{ row }">
            ¥{{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column label="原因" prop="reason" show-overflow-tooltip />
        <el-table-column label="关联事件" prop="eventNo" width="140" />
        <el-table-column label="状态" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="canUpdateStatus && row.status === 'UNPAID'"
              type="success"
              link
              @click="handleMarkPaid(row)"
            >
              标记已缴纳
            </el-button>
            <el-button
              v-if="canUpdateStatus && row.status === 'UNPAID'"
              type="warning"
              link
              @click="handleMarkWaived(row)"
            >
              标记已豁免
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
      :title="isView ? '查看详情' : '新增扣罚'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="人员" prop="personnelId">
          <el-select
            v-model="form.personnelId"
            placeholder="请选择人员"
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
        <el-form-item label="扣罚类型" prop="penaltyTypeId">
          <el-select
            v-model="form.penaltyTypeId"
            placeholder="请选择扣罚类型"
            :disabled="isView"
            @change="handlePenaltyTypeChange"
          >
            <el-option
              v-for="type in penaltyTypeList"
              :key="type.id"
              :label="`${type.typeName} (默认¥${type.defaultAmount})`"
              :value="type.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关联巡更事件">
          <el-select
            v-model="form.patrolEventId"
            placeholder="请选择关联事件（可选）"
            filterable
            clearable
            :disabled="isView"
          >
            <el-option
              v-for="event in eventList"
              :key="event.id"
              :label="`${event.eventNo} - ${event.eventType}`"
              :value="event.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number
            v-model="form.amount"
            :min="0"
            :precision="2"
            :step="10"
            :disabled="isView"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="扣罚日期" prop="penaltyDate">
          <el-date-picker
            v-model="form.penaltyDate"
            type="date"
            placeholder="选择扣罚日期"
            value-format="YYYY-MM-DD"
            :disabled="isView"
          />
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入扣罚原因"
            :disabled="isView"
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { penaltyApi, penaltyTypeApi, personnelApi, patrolEventApi } from '@/api'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const userStore = useUserStore()
const canCreate = computed(() => userStore.isProjectManager)
const canUpdateStatus = computed(() => userStore.isProjectManager || userStore.isAdmin)

const stats = reactive({
  totalAmount: '0.00',
  unpaidAmount: '0.00',
  waivedAmount: '0.00'
})

const searchForm = reactive({
  personnelId: '',
  penaltyTypeId: '',
  status: '',
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
  personnelId: '',
  penaltyTypeId: '',
  patrolEventId: '',
  amount: 0,
  penaltyDate: '',
  reason: ''
})

const rules = {
  personnelId: [{ required: true, message: '请选择人员', trigger: 'change' }],
  penaltyTypeId: [{ required: true, message: '请选择扣罚类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  penaltyDate: [{ required: true, message: '请选择扣罚日期', trigger: 'change' }],
  reason: [{ required: true, message: '请输入扣罚原因', trigger: 'blur' }]
}

const personnelList = ref([])
const penaltyTypeList = ref([])
const eventList = ref([])

const getStatusText = (status) => {
  const map = {
    UNPAID: '未缴纳',
    PAID: '已缴纳',
    WAIVED: '已豁免'
  }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = {
    UNPAID: 'warning',
    PAID: 'success',
    WAIVED: 'info'
  }
  return map[status] || 'info'
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : ''
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
    const response = await penaltyApi.list(params)
    const data = response.data
    tableData.value = data.content || data.data || data
    pagination.total = data.totalElements !== undefined ? data.totalElements : (data.total || 0)
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

const fetchStats = async () => {
  try {
    const response = await penaltyApi.getTotal()
    const data = response.data
    stats.totalAmount = data.totalAmount || '0.00'
    stats.unpaidAmount = data.unpaidAmount || '0.00'
    stats.waivedAmount = data.waivedAmount || '0.00'
  } catch (error) {
    console.error('获取统计数据失败:', error)
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

const fetchPenaltyTypeList = async () => {
  try {
    const response = await penaltyTypeApi.list({ size: 1000 })
    const data = response.data
    penaltyTypeList.value = data.content || data.data || data
  } catch (error) {
    console.error('获取扣罚类型列表失败:', error)
  }
}

const fetchEventList = async () => {
  try {
    const response = await patrolEventApi.list({ size: 1000 })
    const data = response.data
    eventList.value = data.content || data.data || data
  } catch (error) {
    console.error('获取事件列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  searchForm.personnelId = ''
  searchForm.penaltyTypeId = ''
  searchForm.status = ''
  searchForm.dateRange = []
  handleSearch()
}

const resetForm = () => {
  form.id = null
  form.personnelId = ''
  form.penaltyTypeId = ''
  form.patrolEventId = ''
  form.amount = 0
  form.penaltyDate = ''
  form.reason = ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handlePenaltyTypeChange = (typeId) => {
  const selectedType = penaltyTypeList.value.find(t => t.id === typeId)
  if (selectedType) {
    form.amount = selectedType.defaultAmount
  }
}

const handleAdd = () => {
  isView.value = false
  resetForm()
  fetchPersonnelList()
  fetchPenaltyTypeList()
  fetchEventList()
  form.penaltyDate = dayjs().format('YYYY-MM-DD')
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
    await penaltyApi.save(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
    fetchStats()
  } catch (error) {
    if (error !== false) {
      console.error('保存失败:', error)
    }
  } finally {
    saveLoading.value = false
  }
}

const handleMarkPaid = (row) => {
  ElMessageBox.confirm('确定要标记该扣罚为已缴纳吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await penaltyApi.updateStatus(row.id, 'PAID')
      ElMessage.success('标记成功')
      fetchData()
      fetchStats()
    } catch (error) {
      console.error('标记失败:', error)
    }
  }).catch(() => {})
}

const handleMarkWaived = (row) => {
  ElMessageBox.confirm('确定要标记该扣罚为已豁免吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await penaltyApi.updateStatus(row.id, 'WAIVED')
      ElMessage.success('标记成功')
      fetchData()
      fetchStats()
    } catch (error) {
      console.error('标记失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
  fetchStats()
  fetchPersonnelList()
  fetchPenaltyTypeList()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.stats-card {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
}

.stat-value.total {
  color: #409eff;
}

.stat-value.unpaid {
  color: #e6a23c;
}

.stat-value.waived {
  color: #909399;
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
