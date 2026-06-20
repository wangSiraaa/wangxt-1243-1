<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" @submit.prevent>
        <el-form-item label="人员姓名">
          <el-input v-model="searchForm.personnelName" placeholder="请输入人员姓名" clearable />
        </el-form-item>
        <el-form-item label="资质类型">
          <el-select v-model="searchForm.qualificationTypeId" placeholder="请选择资质类型" clearable>
            <el-option
              v-for="item in qualificationTypeList"
              :key="item.id"
              :label="item.typeName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="有效" value="VALID" />
            <el-option label="即将到期" value="EXPIRING" />
            <el-option label="已过期" value="EXPIRED" />
          </el-select>
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
          <el-button v-if="canEdit" type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
          <el-button v-if="canEdit && selectedRows.length > 0" type="warning" @click="handleBatchUpdateStatus">
            <el-icon><Operation /></el-icon>
            批量更新状态
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table
        :data="tableData"
        border
        stripe
        @selection-change="handleSelectionChange"
        :row-class-name="tableRowClassName"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="personnelName" label="人员姓名" width="120" />
        <el-table-column prop="qualificationTypeName" label="资质类型" width="150" />
        <el-table-column prop="certificateNo" label="证书编号" width="180" />
        <el-table-column prop="issueDate" label="发证日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.issueDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="expiryDate" label="过期日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.expiryDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="issuingAuthority" label="发证机关" width="150" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="canEdit" type="danger" link @click="handleDelete(row)">删除</el-button>
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
      :title="isEdit ? '编辑资质' : '新增资质'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="人员" prop="personnelId">
          <el-select v-model="form.personnelId" placeholder="请选择人员" filterable>
            <el-option
              v-for="item in personnelList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="资质类型" prop="qualificationTypeId">
          <el-select v-model="form.qualificationTypeId" placeholder="请选择资质类型" filterable>
            <el-option
              v-for="item in qualificationTypeList"
              :key="item.id"
              :label="item.typeName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="证书编号" prop="certificateNo">
          <el-input v-model="form.certificateNo" placeholder="请输入证书编号" />
        </el-form-item>
        <el-form-item label="发证日期" prop="issueDate">
          <el-date-picker
            v-model="form.issueDate"
            type="date"
            placeholder="请选择发证日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="过期日期" prop="expiryDate">
          <el-date-picker
            v-model="form.expiryDate"
            type="date"
            placeholder="请选择过期日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="发证机关">
          <el-input v-model="form.issuingAuthority" placeholder="请输入发证机关" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="batchDialogVisible"
      title="批量更新状态"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form label-width="100px">
        <el-form-item label="目标状态">
          <el-select v-model="batchStatus" placeholder="请选择状态" style="width: 100%">
            <el-option label="有效" value="VALID" />
            <el-option label="即将到期" value="EXPIRING" />
            <el-option label="已过期" value="EXPIRED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchLoading" @click="confirmBatchUpdate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Operation } from '@element-plus/icons-vue'
import { qualificationApi, qualificationTypeApi, personnelApi } from '@/api'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const userStore = useUserStore()
const canEdit = computed(() => userStore.isAdmin || userStore.isManager)

const searchForm = reactive({
  personnelName: '',
  qualificationTypeId: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])
const personnelList = ref([])
const qualificationTypeList = ref([])
const selectedRows = ref([])
const dialogVisible = ref(false)
const batchDialogVisible = ref(false)
const batchStatus = ref('')
const batchLoading = ref(false)
const isEdit = ref(false)
const saveLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  personnelId: '',
  qualificationTypeId: '',
  certificateNo: '',
  issueDate: '',
  expiryDate: '',
  issuingAuthority: ''
})

const rules = {
  personnelId: [{ required: true, message: '请选择人员', trigger: 'change' }],
  qualificationTypeId: [{ required: true, message: '请选择资质类型', trigger: 'change' }],
  certificateNo: [{ required: true, message: '请输入证书编号', trigger: 'blur' }],
  issueDate: [{ required: true, message: '请选择发证日期', trigger: 'change' }],
  expiryDate: [{ required: true, message: '请选择过期日期', trigger: 'change' }]
}

const getStatusText = (status) => {
  const map = {
    VALID: '有效',
    EXPIRING: '即将到期',
    EXPIRED: '已过期'
  }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = {
    VALID: 'success',
    EXPIRING: 'warning',
    EXPIRED: 'danger'
  }
  return map[status] || 'info'
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : ''
}

const tableRowClassName = ({ row }) => {
  if (row.status === 'EXPIRING' || row.status === 'EXPIRED') {
    return 'highlight-row'
  }
  return ''
}

const fetchPersonnelList = async () => {
  try {
    const response = await personnelApi.list({ page: 0, size: 1000 })
    const data = response.data
    personnelList.value = data.content || data.data || data
  } catch (error) {
    console.error('获取人员列表失败:', error)
  }
}

const fetchQualificationTypeList = async () => {
  try {
    const response = await qualificationTypeApi.list({ page: 0, size: 1000 })
    const data = response.data
    qualificationTypeList.value = data.content || data.data || data
  } catch (error) {
    console.error('获取资质类型列表失败:', error)
  }
}

const fetchData = async () => {
  try {
    const params = {
      ...searchForm,
      page: pagination.pageNum - 1,
      size: pagination.pageSize
    }
    const response = await qualificationApi.list(params)
    const data = response.data
    const list = data.content || data.data || data
    
    list.forEach(item => {
      const person = personnelList.value.find(p => p.id === item.personnelId)
      if (person) {
        item.personnelName = person.name
      }
      const type = qualificationTypeList.value.find(t => t.id === item.qualificationTypeId)
      if (type) {
        item.qualificationTypeName = type.typeName
      }
    })
    
    tableData.value = list
    pagination.total = data.totalElements !== undefined ? data.totalElements : (data.total || 0)
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  searchForm.personnelName = ''
  searchForm.qualificationTypeId = ''
  searchForm.status = ''
  handleSearch()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const resetForm = () => {
  form.id = null
  form.personnelId = ''
  form.qualificationTypeId = ''
  form.certificateNo = ''
  form.issueDate = ''
  form.expiryDate = ''
  form.issuingAuthority = ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    saveLoading.value = true
    if (isEdit.value) {
      await qualificationApi.update(form)
      ElMessage.success('修改成功')
    } else {
      await qualificationApi.save(form)
      ElMessage.success('新增成功')
    }
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

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该资质吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await qualificationApi.delete(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

const handleBatchUpdateStatus = () => {
  batchStatus.value = ''
  batchDialogVisible.value = true
}

const confirmBatchUpdate = async () => {
  if (!batchStatus.value) {
    ElMessage.warning('请选择目标状态')
    return
  }
  const ids = selectedRows.value.map(row => row.id)
  ElMessageBox.confirm(`确定要将选中的 ${ids.length} 条记录状态更新为「${getStatusText(batchStatus.value)}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      batchLoading.value = true
      await qualificationApi.updateStatuses({ ids, status: batchStatus.value })
      ElMessage.success('批量更新成功')
      batchDialogVisible.value = false
      fetchData()
    } catch (error) {
      console.error('批量更新失败:', error)
    } finally {
      batchLoading.value = false
    }
  }).catch(() => {})
}

onMounted(async () => {
  await fetchPersonnelList()
  await fetchQualificationTypeList()
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

:deep(.highlight-row) {
  background-color: #fff7e6 !important;
}

:deep(.highlight-row:hover > td) {
  background-color: #ffedd5 !important;
}
</style>
