<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalPersonnel }}</div>
              <div class="stat-label">总人员数</div>
            </div>
            <div class="stat-icon icon-blue">
              <el-icon><User /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalPoints }}</div>
              <div class="stat-label">总点位</div>
            </div>
            <div class="stat-icon icon-green">
              <el-icon><Location /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ stats.monthlyShifts }}</div>
              <div class="stat-label">本月排班数</div>
            </div>
            <div class="stat-icon icon-orange">
              <el-icon><Calendar /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingEvents }}</div>
              <div class="stat-label">待确认事件</div>
            </div>
            <div class="stat-icon icon-red">
              <el-icon><Warning /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="alerts-row">
      <el-col :span="24">
        <el-alert
          v-if="warnings.expiringQualifications > 0"
          :title="`有 ${warnings.expiringQualifications} 个资质将在30天内过期`"
          type="warning"
          show-icon
          :closable="false"
          class="alert-item"
        />
        <el-alert
          v-if="warnings.pendingExchanges > 0"
          :title="`有 ${warnings.pendingExchanges} 个换班申请待处理`"
          type="error"
          show-icon
          :closable="false"
          class="alert-item"
        />
        <el-alert
          v-if="userStore.isCustomer && warnings.unconfirmedEvents > 0"
          :title="`有 ${warnings.unconfirmedEvents} 个巡更事件待您确认`"
          type="error"
          show-icon
          :closable="false"
          class="alert-item"
        />
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">月度排班统计</span>
            </div>
          </template>
          <v-chart class="chart" :option="shiftChartOption" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">人员状态分布</span>
            </div>
          </template>
          <v-chart class="chart" :option="statusChartOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="table-row">
      <el-col :span="24">
        <el-card class="table-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">最近巡更事件</span>
              <el-button type="primary" link @click="goToPatrolEvents">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentEvents" stripe style="width: 100%">
            <el-table-column prop="eventTime" label="时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.eventTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="pointName" label="点位" />
            <el-table-column prop="personnelName" label="巡更人员" />
            <el-table-column prop="eventType" label="事件类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getEventTypeTag(row.eventType)" size="small">
                  {{ row.eventType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="事件描述" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusTag(row.status)" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="recentEvents.length === 0" description="暂无巡更事件" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import VChart from 'vue-echarts'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores/user'
import {
  personnelApi,
  customerPointApi,
  shiftScheduleApi,
  shiftExchangeApi,
  patrolEventApi,
  qualificationApi
} from '@/api'

const router = useRouter()
const userStore = useUserStore()

const stats = reactive({
  totalPersonnel: 0,
  totalPoints: 0,
  monthlyShifts: 0,
  pendingEvents: 0
})

const warnings = reactive({
  expiringQualifications: 0,
  pendingExchanges: 0,
  unconfirmedEvents: 0
})

const recentEvents = ref([])

const shiftChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
    axisLabel: {
      interval: 0
    }
  },
  yAxis: {
    type: 'value',
    name: '班次数量'
  },
  series: [
    {
      name: '排班数量',
      type: 'bar',
      data: [120, 132, 101, 134, 90, 230, 210, 182, 191, 234, 290, 330],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#2378f7' },
            { offset: 0.7, color: '#2378f7' },
            { offset: 1, color: '#83bff6' }
          ])
        }
      }
    }
  ]
}))

const statusChartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    right: '5%',
    top: 'center'
  },
  series: [
    {
      name: '人员状态',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 20,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        { value: 45, name: '在岗', itemStyle: { color: '#67c23a' } },
        { value: 10, name: '休假', itemStyle: { color: '#e6a23c' } },
        { value: 5, name: '培训', itemStyle: { color: '#909399' } },
        { value: 3, name: '离职', itemStyle: { color: '#f56c6c' } }
      ]
    }
  ]
}))

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const getEventTypeTag = (type) => {
  const typeMap = {
    '正常': 'success',
    '异常': 'warning',
    '紧急': 'danger',
    '巡检': 'info'
  }
  return typeMap[type] || 'info'
}

const getStatusTag = (status) => {
  const statusMap = {
    '待确认': 'warning',
    '已确认': 'success',
    '处理中': 'primary',
    '已关闭': 'info'
  }
  return statusMap[status] || 'info'
}

const goToPatrolEvents = () => {
  router.push('/patrol-event')
}

const fetchStats = async () => {
  try {
    const [personnelRes, pointsRes, shiftsRes, eventsRes] = await Promise.all([
      personnelApi.list({ pageSize: 1 }),
      customerPointApi.list({ pageSize: 1 }),
      shiftScheduleApi.list({ pageSize: 1 }),
      patrolEventApi.getUnconfirmedCount()
    ])

    stats.totalPersonnel = personnelRes.data?.total || 0
    stats.totalPoints = pointsRes.data?.total || 0
    stats.monthlyShifts = shiftsRes.data?.total || 0
    stats.pendingEvents = eventsRes.data?.count || 0
  } catch (error) {
    console.error('Failed to fetch stats:', error)
  }
}

const fetchWarnings = async () => {
  try {
    const [qualRes, exchangeRes, eventRes] = await Promise.all([
      qualificationApi.listByStatus('expiring_soon'),
      shiftExchangeApi.listPending({ pageSize: 1 }),
      patrolEventApi.listUnconfirmed({ pageSize: 1 })
    ])

    warnings.expiringQualifications = qualRes.data?.total || 0
    warnings.pendingExchanges = exchangeRes.data?.total || 0
    warnings.unconfirmedEvents = eventRes.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch warnings:', error)
  }
}

const fetchRecentEvents = async () => {
  try {
    const res = await patrolEventApi.list({ pageSize: 5, sort: 'eventTime,desc' })
    recentEvents.value = res.data?.list || res.data?.records || []
  } catch (error) {
    console.error('Failed to fetch recent events:', error)
    recentEvents.value = []
  }
}

onMounted(() => {
  fetchStats()
  fetchWarnings()
  fetchRecentEvents()
})
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}

.icon-blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.icon-green {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.icon-orange {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.icon-red {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.alerts-row {
  margin-bottom: 20px;
}

.alert-item {
  margin-bottom: 10px;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart {
  height: 350px;
  width: 100%;
}

.table-row {
  margin-bottom: 0;
}

.table-card {
  border-radius: 8px;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
