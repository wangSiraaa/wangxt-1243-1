<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" @submit.prevent>
        <el-form-item label="模板名称">
          <el-input v-model="searchForm.templateName" placeholder="请输入模板名称" clearable />
        </el-form-item>
        <el-form-item label="班次类型">
          <el-select v-model="searchForm.shiftType" placeholder="请选择班次类型" clearable>
            <el-option label="白班" value="DAY" />
            <el-option label="夜班" value="NIGHT" />
            <el-option label="早班" value="MORNING" />
            <el-option label="中班" value="AFTERNOON" />
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
      <el-table :data="tableData" border stripe>
        <el-table-column prop="templateName" label="模板名称" min-width="150" />
        <el-table-column prop="shiftType" label="班次类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getShiftTypeTagType(row.shiftType)" effect="dark">
              {{ getShiftTypeText(row.shiftType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="120">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="120">
          <template #default="{ row }">
            {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="工时(小时)" width="120">
          <template #default="{ row }">
            {{ calculateDuration(row.startTime, row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
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
      :title="isEdit ? '编辑班次模板' : '新增班次模板'"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="form.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="班次类型" prop="shiftType">
          <el-select v-model="form.shiftType" placeholder="请选择班次类型" style="width: 100%">
            <el-option label="白班" value="DAY" />
            <el-option label="夜班" value="NIGHT" />
            <el-option label="早班" value="MORNING" />
            <el-option label="中班" value="AFTERNOON" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker
            v-model="form.startTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="请选择开始时间"
            style="width: 100%"
            @change="updateDuration"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker
            v-model="form.endTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="请选择结束时间"
            style="width: 100%"
            @change="updateDuration"
          />
        </el-form-item>
        <el-form-item label="工时">
          <span class="duration-text">{{ durationText }}</span>
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
import { shiftTemplateApi } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const canEdit = computed(() => userStore.isAdmin || userStore.isManager || userStore.isProjectManager)

const searchForm = reactive({
  templateName: '',
  shiftType: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saveLoading = ref(false)
const formRef = ref(null)
const durationText = ref('')

const form = reactive({
  id: null,
  templateName: '',
  shiftType: '',
  startTime: '',
  endTime: '',
  description: ''
})

const rules = {
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  shiftType: [{ required: true, message: '请选择班次类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
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

const formatTime = (time) => {
  if (!time) return ''
  if (time.includes('T') || time.includes(' ')) {
    return time.split('T')[0]?.split(' ')[1]?.substring(0, 5) || time
  }
  return time.substring(0, 5)
}

const calculateDuration = (start, end) => {
  if (!start || !end) return ''
  
  const startHour = parseInt(start.split(':')[0])
  const startMin = parseInt(start.split(':')[1])
  const endHour = parseInt(end.split(':')[0])
  const endMin = parseInt(end.split(':')[1])
  
  let startTotal = startHour * 60 + startMin
  let endTotal = endHour * 60 + endMin
  
  if (endTotal <= startTotal) {
    endTotal += 24 * 60
  }
  
  const diffMinutes = endTotal - startTotal
  const hours = Math.floor(diffMinutes / 60)
  const minutes = diffMinutes % 60
  
  if (minutes === 0) {
    return `${hours}小时`
  }
  return `${hours}小时${minutes}分钟`
}

const updateDuration = () => {
  durationText.value = calculateDuration(form.startTime, form.endTime)
}

const fetchData = async () => {
  try {
    const params = {
      ...searchForm,
      page: pagination.pageNum - 1,
      size: pagination.pageSize
    }
    const response = await shiftTemplateApi.list(params)
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
  searchForm.templateName = ''
  searchForm.shiftType = ''
  handleSearch()
}

const resetForm = () => {
  form.id = null
  form.templateName = ''
  form.shiftType = ''
  form.startTime = ''
  form.endTime = ''
  form.description = ''
  durationText.value = ''
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
  durationText.value = calculateDuration(row.startTime, row.endTime)
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    saveLoading.value = true
    if (isEdit.value) {
      await shiftTemplateApi.update(form)
      ElMessage.success('修改成功')
    } else {
      await shiftTemplateApi.save(form)
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
  ElMessageBox.confirm('确定要删除该班次模板吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await shiftTemplateApi.delete(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('删除失败:', error)
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

.duration-text {
  color: #409eff;
  font-weight: bold;
  font-size: 16px;
}
</style>
