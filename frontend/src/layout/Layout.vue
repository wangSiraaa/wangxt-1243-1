<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarWidth" class="layout-aside">
      <div class="logo">
        <el-icon><Shield /></el-icon>
        <span class="logo-text">保安管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.path"
          :index="item.path"
        >
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleSidebar">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ userStore.userInfo?.name?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.name || '用户' }}</span>
              <el-tag :type="roleTagType" size="small" class="role-tag">
                {{ userStore.roleName }}
              </el-tag>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Shield,
  Fold,
  Expand,
  CaretBottom,
  User,
  SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const sidebarWidth = computed(() => isCollapse.value ? '64px' : '220px')

const activeMenu = computed(() => route.path)

const allMenuItems = [
  { path: '/dashboard', title: '仪表盘', icon: 'DataBoard', roles: ['PROJECT_MANAGER', 'TEAM_LEADER', 'CUSTOMER'] },
  { path: '/personnel', title: '人员管理', icon: 'User', roles: ['PROJECT_MANAGER', 'TEAM_LEADER'] },
  { path: '/qualification', title: '资质管理', icon: 'Certificate', roles: ['PROJECT_MANAGER', 'TEAM_LEADER'] },
  { path: '/customer', title: '客户管理', icon: 'OfficeBuilding', roles: ['PROJECT_MANAGER'] },
  { path: '/customer-point', title: '点位管理', icon: 'Location', roles: ['PROJECT_MANAGER', 'TEAM_LEADER'] },
  { path: '/shift-template', title: '班次模板', icon: 'Calendar', roles: ['PROJECT_MANAGER'] },
  { path: '/shift-schedule', title: '排班管理', icon: 'Clock', roles: ['PROJECT_MANAGER', 'TEAM_LEADER'] },
  { path: '/shift-exchange', title: '换班管理', icon: 'SwitchButton', roles: ['PROJECT_MANAGER', 'TEAM_LEADER'] },
  { path: '/patrol-event', title: '巡更事件', icon: 'Warning', roles: ['PROJECT_MANAGER', 'TEAM_LEADER', 'CUSTOMER'] },
  { path: '/penalty', title: '扣罚管理', icon: 'Finished', roles: ['PROJECT_MANAGER', 'TEAM_LEADER'] },
  { path: '/settlement', title: '结算管理', icon: 'Money', roles: ['PROJECT_MANAGER', 'CUSTOMER'] }
]

const menuItems = computed(() => {
  const userRole = userStore.role
  return allMenuItems.filter(item => item.roles.includes(userRole))
})

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(r => r.meta?.title)
  return matched.map(r => ({
    path: r.path,
    title: r.meta.title
  }))
})

const roleTagType = computed(() => {
  const typeMap = {
    PROJECT_MANAGER: 'primary',
    TEAM_LEADER: 'success',
    CUSTOMER: 'warning',
    ADMIN: 'danger',
    MANAGER: 'info'
  }
  return typeMap[userStore.role] || 'info'
})

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch {
    }
  } else if (command === 'profile') {
    ElMessage.info('个人中心功能开发中')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100%;
}

.layout-aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid #1f2d3d;
  padding: 0 16px;
  white-space: nowrap;
  overflow: hidden;
}

.logo .el-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.logo-text {
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-menu {
  border-right: none;
  height: calc(100vh - 60px);
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

.layout-header {
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  position: relative;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 0 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.user-avatar {
  background-color: #409eff;
  color: #fff;
  font-weight: 600;
}

.user-name {
  color: #606266;
  font-size: 14px;
}

.role-tag {
  margin: 0 4px;
}

.layout-main {
  background-color: #f0f2f5;
  padding: 20px;
  min-height: calc(100vh - 60px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
