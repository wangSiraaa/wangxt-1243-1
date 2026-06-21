<template>
  <div class="settlement-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="客户">
          <el-select
            v-model="searchForm.customerId"
            placeholder="请选择客户"
            clearable
            filterable
            style="width: 200px"
          >
            <el-option
              v-for="customer in customerList"
              :key="customer.id"
              :label="customer.name"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已开票" value="BILLED" />
            <el-option label="已付款" value="PAID" />
          </el-select>
        </el-form-item>
        <el-form-item label="结算月份">
          <el-date-picker
            v-model="searchForm.settlementMonth"
            type="month"
            placeholder="选择月份"
            value-format="YYYY-MM"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="action-card" v-if="userStore.isProjectManager">
      <el-space>
        <el-button type="primary" @click="openGenerateDialog">
          <el-icon><Plus /></el-icon> 生成结算单
        </el-button>
      </el-space>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="settlementNo" label="结算单号" width="180" />
        <el-table-column label="客户" width="150">
          <template #default="{ row }">
            {{ getCustomerName(row.customerId) }}
          </template>
        </el-table-column>
        <el-table-column prop="settlementMonth" label="结算月份" width="120" />
        <el-table-column prop="totalShifts" label="总班次" width="100" align="right" />
        <el-table-column label="总金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ formatNumber(row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column label="扣罚金额" width="120" align="right">
          <template #default="{ row }">
            <span class="penalty-amount">-¥{{ formatNumber(row.penaltyAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实结金额" width="130" align="right">
          <template #default="{ row }">
            <span class="actual-amount">¥{{ formatNumber(row.actualAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="未确认事件" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.unconfirmedEventCount > 0" type="danger" effect="light">
              {{ row.unconfirmedEventCount }} 条
            </el-tag>
            <el-tag v-else type="success" effect="light">
              0 条
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetails(row)">
              明细
            </el-button>
            <el-button
              v-if="userStore.isProjectManager && row.status === 'DRAFT' && row.unconfirmedEventCount === 0"
              type="success"
              link
              @click="handleConfirm(row)"
            >
              确认
            </el-button>
            <el-button
              v-if="userStore.isProjectManager && row.status === 'DRAFT' && row.unconfirmedEventCount > 0"
              type="warning"
              link
              disabled
            >
              待确认异常
            </el-button>
            <el-button
              v-if="userStore.isProjectManager && row.status === 'CONFIRMED'"
              type="primary"
              link
              @click="handleUpdateStatus(row, 'BILLED')"
            >
              标记开票
            </el-button>
            <el-button
              v-if="userStore.isProjectManager && row.status === 'BILLED'"
              type="success"
              link
              @click="handleUpdateStatus(row, 'PAID')"
            >
              标记付款
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
      />
    </el-card>

    <el-dialog
      v-model="generateDialogVisible"
      title="生成结算单"
      width="500px"
    >
      <el-form :model="generateForm" label-width="100px">
        <el-form-item label="客户" required>
          <el-select
            v-model="generateForm.customerId"
            placeholder="请选择客户"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="customer in customerList"
              :key="customer.id"
              :label="customer.name"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="结算月份" required>
          <el-date-picker
            v-model="generateForm.settlementMonth"
            type="month"
            placeholder="选择月份"
            value-format="YYYY-MM"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <el-alert
        v-if="unconfirmedEventWarning > 0"
        title="存在未确认的异常事件"
        :description="`该客户在 ${generateForm.settlementMonth} 有 ${unconfirmedEventWarning} 条未确认的巡更事件，未确认的异常不能计入结算，结算单将只能保持草稿状态。`"
        type="warning"
        show-icon
        class="warning-alert"
        :closable="false"
      />

      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="handleGenerate">
          生成结算单
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailDialogVisible"
      title="结算明细"
      width="1000px"
    >
      <el-descriptions :column="3" border class="settlement-info">
        <el-descriptions-item label="结算单号">
          {{ currentSettlement?.settlementNo }}
        </el-descriptions-item>
        <el-descriptions-item label="客户">
          {{ getCustomerName(currentSettlement?.customerId) }}
        </el-descriptions-item>
        <el-descriptions-item label="结算月份">
          {{ currentSettlement?.settlementMonth }}
        </el-descriptions-item>
        <el-descriptions-item label="总班次">
          {{ currentSettlement?.totalShifts }}
        </el-descriptions-item>
        <el-descriptions-item label="总金额">
          ¥{{ formatNumber(currentSettlement?.totalAmount) }}
        </el-descriptions-item>
        <el-descriptions-item label="扣罚金额">
          <span class="penalty-amount">-¥{{ formatNumber(currentSettlement?.penaltyAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="实结金额">
          <span class="actual-amount">¥{{ formatNumber(currentSettlement?.actualAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="未确认事件">
          <el-tag v-if="currentSettlement?.unconfirmedEventCount > 0" type="danger">
            {{ currentSettlement?.unconfirmedEventCount }} 条
          </el-tag>
          <el-tag v-else type="success">0 条</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentSettlement?.status)">
            {{ getStatusName(currentSettlement?.status) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">结算明细</el-divider>

      <el-table :data="settlementDetails" border stripe size="small">
        <el-table-column prop="shiftDate" label="日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.shiftDate) }}
          </template>
        </el-table-column>
        <el-table-column label="岗点" width="150">
          <template #default="{ row }">
            {{ getPointName(row.scheduleId) }}
          </template>
        </el-table-column>
        <el-table-column label="班次" width="120">
          <template #default="{ row }">
            {{ getShiftName(row.scheduleId) }}
          </template>
        </el-table-column>
        <el-table-column label="人员" width="120">
          <template #default="{ row }">
            {{ getPersonnelName(row.scheduleId) }}
          </template>
        </el-table-column>
        <el-table-column label="换班" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.exchangeInfo" type="warning" size="small">已换班</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.includedInSettlement" class="amount-included">
              ¥{{ formatNumber(row.shiftAmount) }}
            </span>
            <span v-else class="amount-excluded">
              ¥{{ formatNumber(row.shiftAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="是否计入" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.includedInSettlement" type="success" size="small">
              是
            </el-tag>
            <el-tag v-else type="info" size="small">
              否
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="exclusionReason" label="排除原因" min-width="150">
          <template #default="{ row }">
            <span v-if="row.exclusionReason" class="exclusion-reason">
              {{ row.exclusionReason }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores/user'
import * as settlementApi from '@/api'
import * as customerApi from '@/api'

const userStore = useUserStore()

const loading = ref(false)
const generating = ref(false)
const generateDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const unconfirmedEventWarning = ref(0)

const searchForm = reactive({
  customerId: null,
  status: null,
  settlementMonth: null
})

const generateForm = reactive({
  customerId: null,
  settlementMonth: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const customerList = ref([])
const settlementDetails = ref([])
const currentSettlement = ref(null)
const scheduleMap = ref(new Map())

const getStatusType = (status) => {
  const types = {
    DRAFT: 'info',
    CONFIRMED: 'warning',
    BILLED: 'primary',
    PAID: 'success'
  }
  return types[status] || 'info'
}

const getStatusName = (status) => {
  const names = {
    DRAFT: '草稿',
    CONFIRMED: '已确认',
    BILLED: '已开票',
    PAID: '已付款'
  }
  return names[status] || status
}

const getCustomerName = (customerId) => {
  const customer = customerList.value.find(c => c.id === customerId)
  return customer?.name || '-'
}

const getPointName = (scheduleId) => {
  const schedule = scheduleMap.value.get(scheduleId)
  return schedule?.customerPoint?.pointName || '-'
}

const getShiftName = (scheduleId) => {
  const schedule = scheduleMap.value.get(scheduleId)
  return schedule?.shiftTemplate?.templateName || '-'
}

const getPersonnelName = (scheduleId) => {
  const schedule = scheduleMap.value.get(scheduleId)
  return schedule?.personnel?.name || '-'
}

const formatNumber = (num) => {
  if (num === null || num === undefined) return '0.00'
  return Number(num).toFixed(2)
}

const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const loadCustomerList = async () => {
  try {
    const res = await customerApi.customerApi.list()
    if (res.code === 200) {
      customerList.value = res.data || []
    }
  } catch (e) {
    console.error('Load customer list failed:', e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.page - 1,
      size: pagination.size
    }
    const res = await settlementApi.settlementApi.list(params)
    if (res.code === 200) {
      tableData.value = res.data?.content || res.data || []
      pagination.total = res.data?.totalElements || tableData.value.length
    }
  } catch (e) {
    ElMessage.error('加载数据失败')
    console.error('Load settlement data failed:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.customerId = null
  searchForm.status = null
  searchForm.settlementMonth = null
  pagination.page = 1
  loadData()
}

const openGenerateDialog = () => {
  generateForm.customerId = null
  generateForm.settlementMonth = null
  unconfirmedEventWarning.value = 0
  generateDialogVisible.value = true
}

watch(
  () => [generateForm.customerId, generateForm.settlementMonth],
  async ([customerId, settlementMonth]) => {
    if (customerId && settlementMonth) {
      try {
        const res = await settlementApi.patrolEventApi.countUnconfirmedByCustomer(customerId)
        if (res.code === 200) {
          unconfirmedEventWarning.value = res.data || 0
        }
      } catch (e) {
        console.error('Check unconfirmed events failed:', e)
      }
    } else {
      unconfirmedEventWarning.value = 0
    }
  }
)

const handleGenerate = async () => {
  if (!generateForm.customerId) {
    ElMessage.warning('请选择客户')
    return
  }
  if (!generateForm.settlementMonth) {
    ElMessage.warning('请选择结算月份')
    return
  }

  generating.value = true
  try {
    const params = {
      customerId: generateForm.customerId,
      settlementMonth: generateForm.settlementMonth
    }
    const res = await settlementApi.settlementApi.generate(params)
    if (res.code === 200) {
      if (unconfirmedEventWarning.value > 0) {
        ElMessage.warning(`结算单已生成，但存在 ${unconfirmedEventWarning.value} 条未确认事件，请先确认后再确认结算单`)
      } else {
        ElMessage.success('结算单生成成功')
      }
      generateDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (e) {
    ElMessage.error('生成失败：' + (e.response?.data?.message || e.message))
  } finally {
    generating.value = false
  }
}

const handleConfirm = async (row) => {
  if (row.unconfirmedEventCount > 0) {
    ElMessage.warning('存在未确认的异常事件，无法确认结算单')
    return
  }

  try {
    await ElMessageBox.confirm(
      '确认后将无法修改结算单金额，是否继续？',
      '确认结算单',
      { type: 'warning' }
    )

    const res = await settlementApi.settlementApi.confirm(row.id)
    if (res.code === 200) {
      ElMessage.success('结算单已确认')
      loadData()
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('确认失败')
    }
  }
}

const handleUpdateStatus = async (row, status) => {
  const statusNames = {
    BILLED: '开票',
    PAID: '付款'
  }

  try {
    await ElMessageBox.confirm(
      `确定要标记为已${statusNames[status]}吗？`,
      '确认操作',
      { type: 'warning' }
    )

    const res = await settlementApi.settlementApi.updateStatus(row.id, status)
    if (res.code === 200) {
      ElMessage.success(`已标记为已${statusNames[status]}`)
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const viewDetails = async (row) => {
  currentSettlement.value = row
  settlementDetails.value = []
  scheduleMap.value.clear()

  try {
    const res = await settlementApi.settlementApi.getDetails(row.id)
    if (res.code === 200) {
      settlementDetails.value = res.data || []
      const scheduleIds = settlementDetails.value.map(d => d.scheduleId)
      for (const scheduleId of scheduleIds) {
        try {
          const schedRes = await settlementApi.shiftScheduleApi.get(scheduleId)
          if (schedRes.code === 200) {
            scheduleMap.value.set(scheduleId, schedRes.data)
          }
        } catch (e) {
          console.error('Load schedule failed:', e)
        }
      }
    }
    detailDialogVisible.value = true
  } catch (e) {
    ElMessage.error('加载明细失败')
    console.error('Load settlement details failed:', e)
  }
}

onMounted(() => {
  loadCustomerList()
  loadData()
})
</script>

<style scoped lang="scss">
.settlement-container {
  padding: 20px;

  .search-card,
  .action-card,
  .table-card {
    margin-bottom: 20px;
  }

  .search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }

  .penalty-amount {
    color: #f56c6c;
    font-weight: 600;
  }

  .actual-amount {
    color: #67c23a;
    font-weight: 600;
    font-size: 15px;
  }

  .amount-included {
    color: #67c23a;
  }

  .amount-excluded {
    color: #909399;
    text-decoration: line-through;
  }

  .exclusion-reason {
    color: #f56c6c;
    font-size: 12px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .settlement-info {
    margin-bottom: 20px;
  }

  .warning-alert {
    margin-top: 20px;
    margin-bottom: 10px;
  }
}
</style>
