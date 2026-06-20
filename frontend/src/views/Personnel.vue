<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" @submit.prevent>
        <el-form-item label="员工编号">
          <el-input v-model="searchForm.employeeNo" placeholder="请输入员工编号" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="在职" value="ACTIVE" />
            <el-option label="离职" value="INACTIVE" />
            <el-option label="休假" value="ON_LEAVE" />
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
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="employeeNo" label="员工编号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            <span>{{ row.gender === 'MALE' ? '男' : '女' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="idCard" label="身份证" width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxConsecutiveNightShifts" label="最大连续夜班" width="120" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="canEdit" type="danger" link @click="handleDelete(row)">删除</el-button>
            <el-button v-if="canEdit && row.status !== 'ACTIVE'" type="success" link @click="handleToggleStatus(row, 'ACTIVE')">
              启用
            </el-button>
            <el-button v-if="canEdit && row.status === 'ACTIVE'" type="warning" link @click="handleToggleStatus(row, 'INACTIVE')">
              停用
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
      :title="isEdit ? '编辑人员' : '新增人员'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="员工编号" prop="employeeNo">
          <el-input v-model="form.employeeNo" placeholder="请输入员工编号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio value="MALE">男</el-radio>
            <el-radio value="FEMALE">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="身份证" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status">
            <el-option label="在职" value="ACTIVE" />
            <el-option label="离职" value="INACTIVE" />
            <el-option label="休假" value="ON_LEAVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大连续夜班" prop="maxConsecutiveNightShifts">
          <el-input-number v-model="form.maxConsecutiveNightShifts" :min="1" :max="7" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { personnelApi } from '@/api'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const userStore = useUserStore()
const canEdit = computed(() => userStore.isAdmin || userStore.isManager)

const searchForm = reactive({
  employeeNo: '',
  name: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])
const selectedRows = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saveLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  employeeNo: '',
  name: '',
  gender: 'MALE',
  phone: '',
  idCard: '',
  address: '',
  status: 'ACTIVE',
  maxConsecutiveNightShifts: 3
})

const rules = {
  employeeNo: [{ required: true, message: '请输入员工编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  maxConsecutiveNightShifts: [{ required: true, message: '请输入最大连续夜班数', trigger: 'blur' }]
}

const getStatusText = (status) => {
  const map = {
    ACTIVE: '在职',
    INACTIVE: '离职',
    ON_LEAVE: '休假'
  }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = {
    ACTIVE: 'success',
    INACTIVE: 'danger',
    ON_LEAVE: 'warning'
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
      page: pagination.pageNum - 1,
      size: pagination.pageSize
    }
    const response = await personnelApi.list(params)
    const data = response.data
    tableData.value = data.content || data.data || data
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
  searchForm.employeeNo = ''
  searchForm.name = ''
  searchForm.status = ''
  handleSearch()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const resetForm = () => {
  form.id = null
  form.employeeNo = ''
  form.name = ''
  form.gender = 'MALE'
  form.phone = ''
  form.idCard = ''
  form.address = ''
  form.status = 'ACTIVE'
  form.maxConsecutiveNightShifts = 3
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
      await personnelApi.update(form)
      ElMessage.success('修改成功')
    } else {
      await personnelApi.save(form)
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
  ElMessageBox.confirm('确定要删除该人员吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await personnelApi.delete(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

const handleToggleStatus = async (row, targetStatus) => {
  const action = targetStatus === 'ACTIVE' ? '启用' : '停用'
  ElMessageBox.confirm(`确定要${action}该人员吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await personnelApi.update({ ...row, status: targetStatus })
      ElMessage.success(`${action}成功`)
      fetchData()
    } catch (error) {
      console.error(`${action}失败:`, error)
    }
  }).catch(() => {})
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
