<script setup lang="ts">
import { Connection, Document, PictureFilled, User } from '@element-plus/icons-vue'
import { useQuery } from '@tanstack/vue-query'

import { useAuthStore } from '../auth/store'
import { formatDate } from '../../shared/utils/format'
import { categoryLabel, statusLabel, statusTone } from '../../shared/utils/labels'
import { loadDashboard } from './api'

const auth = useAuthStore()
const dashboard = useQuery({ queryKey: ['dashboard'], queryFn: loadDashboard })
</script>

<template>
  <div>
    <header class="page-header">
      <div>
        <h1>系统概览</h1>
        <p>查看服务健康、内容与媒体资源的当前状态。</p>
      </div>
    </header>

    <div v-if="dashboard.isPending.value" class="summary-grid" aria-label="正在加载系统概览">
      <div v-for="index in 3" :key="index" class="summary-item">
        <ElSkeleton :rows="1" animated />
      </div>
    </div>
    <div v-else-if="dashboard.isError.value" class="content-panel error-panel">
      <span>系统概览加载失败，请确认后端服务可用。</span>
      <ElButton @click="dashboard.refetch()">重新加载</ElButton>
    </div>
    <template v-else>
      <section class="summary-grid" aria-label="系统摘要">
        <div class="summary-item">
          <div class="summary-icon success">
            <ElIcon><Connection /></ElIcon>
          </div>
          <div>
            <div class="summary-label">服务状态</div>
            <div
              class="summary-value health"
              :class="{ down: dashboard.data.value?.health !== 'UP' }"
            >
              {{ dashboard.data.value?.health === 'UP' ? '运行正常' : '状态异常' }}
            </div>
          </div>
        </div>
        <div class="summary-item">
          <div class="summary-icon">
            <ElIcon><Document /></ElIcon>
          </div>
          <div>
            <div class="summary-label">内容总数</div>
            <div class="summary-value">{{ dashboard.data.value?.contentTotal ?? 0 }}</div>
          </div>
        </div>
        <div class="summary-item">
          <div class="summary-icon">
            <ElIcon><PictureFilled /></ElIcon>
          </div>
          <div>
            <div class="summary-label">媒体总数</div>
            <div class="summary-value">{{ dashboard.data.value?.mediaTotal ?? 0 }}</div>
          </div>
        </div>
      </section>

      <div class="dashboard-columns">
        <section class="content-panel recent-panel">
          <div class="panel-heading">
            <div>
              <h2 class="section-heading">最近内容</h2>
              <p class="section-description">按创建时间展示最近三条内容。</p>
            </div>
            <ElButton plain @click="$router.push({ name: 'contents' })">查看全部内容</ElButton>
          </div>
          <ElTable
            v-if="dashboard.data.value?.recentContents?.length"
            :data="dashboard.data.value.recentContents"
            table-layout="fixed"
          >
            <ElTableColumn prop="title" label="标题" min-width="210" />
            <ElTableColumn label="分类" width="110"
              ><template #default="{ row }">{{
                categoryLabel(row.category)
              }}</template></ElTableColumn
            >
            <ElTableColumn label="状态" width="120"
              ><template #default="{ row }"
                ><span
                  ><i class="status-dot" :class="statusTone(row.status)" />{{
                    statusLabel(row.status)
                  }}</span
                ></template
              ></ElTableColumn
            >
            <ElTableColumn label="更新时间" width="180"
              ><template #default="{ row }">{{
                formatDate(row.updatedAt)
              }}</template></ElTableColumn
            >
          </ElTable>
          <div v-else class="empty-state">暂无内容，管理员可以前往内容管理创建第一条内容。</div>
        </section>

        <aside class="content-panel operations-panel">
          <h2 class="section-heading">当前账号</h2>
          <dl class="account-details">
            <div>
              <dt>
                <ElIcon><User /></ElIcon>用户名
              </dt>
              <dd>{{ auth.user?.username }}</dd>
            </div>
            <div>
              <dt>角色</dt>
              <dd>{{ auth.user?.role }}</dd>
            </div>
            <div>
              <dt>权限说明</dt>
              <dd>{{ auth.canWrite ? '完整管理权限' : '只读访问' }}</dd>
            </div>
          </dl>
          <div v-if="auth.isViewer" class="read-only-note">
            当前为只读演示模式，所有写操作已禁用。
          </div>
        </aside>
      </div>
    </template>
  </div>
</template>

<style scoped>
.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  margin-top: 28px;
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
}
.summary-item {
  min-height: 142px;
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 26px 34px;
  border-right: 1px solid var(--border);
}
.summary-item:last-child {
  border-right: 0;
}
.summary-icon {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  color: var(--accent);
  background: var(--accent-soft);
  font-size: 21px;
}
.summary-icon.success {
  color: var(--success);
  background: #eaf8f1;
}
.summary-label {
  color: var(--muted);
  font-size: 13px;
}
.summary-value {
  margin-top: 7px;
  color: var(--accent);
  font-size: 33px;
  line-height: 1;
  font-weight: 750;
}
.summary-value.health {
  color: var(--success);
  font-size: 24px;
}
.summary-value.health.down {
  color: var(--danger);
}
.dashboard-columns {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 310px;
  gap: 34px;
}
.recent-panel {
  min-width: 0;
}
.operations-panel {
  padding-left: 32px;
  border-left: 1px solid var(--border);
}
.account-details {
  margin: 22px 0 0;
}
.account-details > div {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
}
.account-details dt {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: var(--muted);
}
.account-details dd {
  margin: 0;
  font-weight: 620;
  text-align: right;
}
.read-only-note {
  margin-top: 22px;
  padding: 14px;
  border: 1px solid #c9d9ff;
  border-radius: 7px;
  color: #2456bd;
  background: #f3f7ff;
  font-size: 12px;
  line-height: 1.7;
}
@media (max-width: 1100px) {
  .dashboard-columns {
    grid-template-columns: 1fr;
  }
  .operations-panel {
    padding-left: 0;
    border-left: 0;
  }
}
@media (max-width: 760px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
  .summary-item {
    min-height: 105px;
    border-right: 0;
    border-bottom: 1px solid var(--border);
  }
  .summary-item:last-child {
    border-bottom: 0;
  }
}
</style>
