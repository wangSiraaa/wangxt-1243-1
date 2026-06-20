<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" @submit.prevent>
        <el-form-item label="客户">
          <el-select v-model="searchForm.customerId" placeholder="请选择客户" clearable filterable>
            <el-option
              v-for="item in customerList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="点位名称">
          <el-input v-model="searchForm.pointName" placeholder="请输入点位名称" clearable />
        </el-form-item>
        <el-form-item label="是否重点岗位">
          <el-select v-model="searchForm.keyPosition" placeholder="请选择" clearable>
            <el-option label="是" :value="true" />
            <el-option label="否" :value="false" />
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
        <el-table-column prop="customerName" label="客户名称" width="180" />
        <el-table-column prop="pointName" label="点位名称" width="150" />
        <el-table-column prop="pointCode" label="点位编码" width="150" />
        <el-table-column prop="address" label="地址" min-width="200" />
        <el-table-column prop="keyPosition" label="是否重点岗位" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.keyPosition" type="danger" effect="dark">
              重点岗位
            </el-tag>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="启用状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="canEdit" type="danger" link @click="handleDelete(row)">删除</el-button>
            <el-button
              v-if="canEdit && !row.keyPosition"
              type="danger"
              link
              @click="handleToggleKeyPosition(row, true)"
            >
              设为重点
            </el-button>
            <el-button
              v-if="canEdit && row.keyPosition"
              type="info"
              link
              @click="handleToggleKeyPosition(row, false)"
            >
              取消重点
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
      :title="isEdit ? '编辑客户点位' : '新增客户点位'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="所属客户" prop="customerId">
          <el-select v-model="form.customerId" placeholder="请选择客户" filterable style="width: 100%">
            <el-option
              v-for="item in customerList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="点位名称" prop="pointName">
          <el-input v-model="form.pointName" placeholder="请输入点位名称" />
        </el-form-item>
        <el-form-item label="点位编码" prop="pointCode">
          <el-input v-model="form.pointCode" placeholder="请输入点位编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="重点岗位" prop="keyPosition">
          <el-switch v-model="form.keyPosition" active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
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
import { customerPointApi, customerApi } from '@/api'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const userStore = useUserStore()
const canEdit = computed(() => userStore.isAdmin || userStore.isManager)

const searchForm = reactive({
  customerId: '',
  pointName: '',
  keyPosition: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])
const customerList = ref([])
const selectedRows = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saveLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  customerId: '',
  pointName: '',
  pointCode: '',
  address: '',
  keyPosition: false,
  description: ''
})

const rules = {
  customerId: [{ required: true, message: '请选择所属客户', trigger: 'change' }],
  pointName: [{ required: true, message: '请输入点位名称', trigger: 'blur' }],
  pointCode: [{ required: true, message: '请输入点位编码', trigger: 'blur' }],
  keyPosition: [{ required: true, message: '请选择是否为重点岗位', trigger: 'change' }]
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : ''
}

const fetchCustomerList = async () => {
  try {
    const response = await customerApi.list({ page: 0, size: 1000 })
    const data = response.data
    customerList.value = data.content || data.data || data
  } catch (error) {
    console.error('获取客户列表失败:', error)
  }
}

const fetchData = async () => {
  try {
    const params = {
      ...searchForm,
      page: pagination.pageNum - 1,
      size: pagination.pageSize
    }
    const response = await customerPointApi.list(params)
    const data = response.data
    const list = data.content || data.data || data
    
    list.forEach(item => {
      const customer = customerList.value.find(c => c.id === item.customerId)
      if (customer) {
        item.customerName = customer.name
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
  searchForm.customerId = ''
  searchForm.pointName = ''
  searchForm.keyPosition = ''
  handleSearch()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const resetForm = () => {
  form.id = null
  form.customerId = ''
  form.pointName = ''
  form.pointCode = ''
  form.address = ''
  form.keyPosition = false
  form.description = ''
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
      await customerPointApi.update(form)
      ElMessage.success('修改成功')
    } else {
      await customerPointApi.save(form)
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
  ElMessageBox.confirm('确定要删除该客户点位吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await customerPointApi.delete(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

const handleToggleKeyPosition = async (row, keyPosition) => {
  const action = keyPosition ? '设置为重点岗位' : '取消重点岗位'
  ElMessageBox.confirm(`确定要${action}吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await customerPointApi.updateKeyPosition(row.id, keyPosition)
      ElMessage.success(`${action}成功`)
      fetchData()
    } catch (error) {
      console.error(`${action}失败:`, error)
    }
  }).catch(() => {})
}

onMounted(async () => {
  await fetchCustomerList()
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
