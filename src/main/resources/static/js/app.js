// PHANTOM-ERP Enterprise Web Application Client

const app = {
  activeTab: 'dashboard',
  financialChart: null,
  departmentChart: null,
  currentUser: null,
  authToken: null,

  async init() {
    this.setupNavigation();
    this.setupDateDefaults();

    // Check existing authentication session
    const token = localStorage.getItem('omni_token') || sessionStorage.getItem('omni_token');
    if (token) {
      const valid = await this.verifySession(token);
      if (valid) {
        this.hideLogin();
        this.loadDashboard();
      } else {
        this.showLogin();
      }
    } else {
      this.showLogin();
    }
    
    // Auto-refresh dashboard periodically
    setInterval(() => {
      if (this.currentUser && this.activeTab === 'dashboard') {
        this.loadDashboard(false);
      }
    }, 30000);
  },

  setupDateDefaults() {
    const today = new Date().toISOString().split('T')[0];
    const empHire = document.getElementById('empHireDate');
    if (empHire) empHire.value = today;
  },

  setupNavigation() {
    document.querySelectorAll('.nav-item').forEach(item => {
      item.addEventListener('click', (e) => {
        if (item.id === 'navItemHr') return; // Handled specifically by handleHrNavClick
        const tab = item.getAttribute('data-tab');
        if (tab) this.switchTab(tab);
      });
    });
  },

  handleHrNavClick(event) {
    if (event) event.stopPropagation();
    const group = document.getElementById('navGroupHr');
    if (group) {
      group.classList.toggle('open');
    }
    this.switchTab('hr');
  },

  switchHrSubTab(subTab, event) {
    if (event) event.stopPropagation();
    const group = document.getElementById('navGroupHr');
    if (group && !group.classList.contains('open')) {
      group.classList.add('open');
    }
    if (this.activeTab !== 'hr') {
      this.switchTab('hr');
    }
    document.querySelectorAll('.nav-subitem').forEach(el => {
      el.classList.toggle('active', el.getAttribute('data-subtab') === subTab);
    });
    this.filterHrView(subTab);
  },

  filterHrView(view) {
    // Update sub-tab pill buttons
    document.querySelectorAll('.sub-tab-btn').forEach(btn => {
      btn.classList.toggle('active', btn.id === `hrSubTab-${view}`);
    });
    // Sync sidebar subitem active state
    document.querySelectorAll('.nav-subitem').forEach(el => {
      el.classList.toggle('active', el.getAttribute('data-subtab') === view);
    });

    const cardReqs = document.getElementById('hrCard-requisitions');
    const cardRecruitment = document.getElementById('hrCard-recruitment');
    const cardDir = document.getElementById('hrCard-directory');
    const cardDeptLeaves = document.getElementById('hrCard-deptAndLeaves');
    const cardPayroll = document.getElementById('hrCard-payroll');

    if (view === 'overview') {
      document.querySelectorAll('.hr-module-card').forEach(card => card.style.display = '');
    } else if (view === 'requisitions') {
      document.querySelectorAll('.hr-module-card').forEach(card => card.style.display = 'none');
      if (cardReqs) {
        cardReqs.style.display = '';
        cardReqs.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }
    } else if (view === 'recruitment') {
      document.querySelectorAll('.hr-module-card').forEach(card => card.style.display = 'none');
      if (cardRecruitment) {
        cardRecruitment.style.display = '';
        cardRecruitment.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }
    } else if (view === 'directory') {
      document.querySelectorAll('.hr-module-card').forEach(card => card.style.display = 'none');
      if (cardDir) {
        cardDir.style.display = '';
        cardDir.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }
    } else if (view === 'payroll') {
      document.querySelectorAll('.hr-module-card').forEach(card => card.style.display = 'none');
      if (cardPayroll) {
        cardPayroll.style.display = '';
        cardPayroll.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }
    } else if (view === 'leaves') {
      document.querySelectorAll('.hr-module-card').forEach(card => card.style.display = 'none');
      if (cardDeptLeaves) {
        cardDeptLeaves.style.display = '';
        cardDeptLeaves.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }
    }
  },

  switchTab(tabName) {
    this.activeTab = tabName;
    
    // Update sidebar nav items
    document.querySelectorAll('.nav-item').forEach(item => {
      if (item.getAttribute('data-tab') === tabName) {
        item.classList.add('active');
      } else {
        item.classList.remove('active');
      }
    });

    // Update Tab Panels
    document.querySelectorAll('.tab-panel').forEach(panel => {
      panel.classList.remove('active');
    });
    const activePanel = document.getElementById(`tab-${tabName}`);
    if (activePanel) activePanel.classList.add('active');

    // Update Title
    const titles = {
      dashboard: 'Executive Dashboard & Real-Time Insights',
      finance: 'Corporate Finance & General Ledger',
      hr: 'Human Resources & Workforce Management',
      procurement: 'Procurement & Supply Chain Management',
      sales: 'Enterprise Sales & Customer Relationship Management',
      manufacturing: 'Manufacturing Operations & Bill of Materials',
      inventory: 'Warehouse & Inventory Ledger',
      deployment: 'Enterprise ERP Deployment Architecture',
      admin: 'Roles & System Administration',
      audit: 'Central System Audit Trail'
    };
    document.getElementById('currentViewTitle').textContent = titles[tabName] || 'Enterprise Resource Planning';

    // Trigger tab specific data load
    this.refreshCurrentTab();
  },

  refreshCurrentTab() {
    switch (this.activeTab) {
      case 'dashboard': this.loadDashboard(); break;
      case 'finance': this.loadFinance(); break;
      case 'hr': this.loadHr(); break;
      case 'procurement': this.loadProcurement(); break;
      case 'sales': this.loadSales(); break;
      case 'manufacturing': this.loadManufacturing(); break;
      case 'inventory': this.loadInventory(); break;
      case 'admin': this.loadAdmin(); break;
      case 'audit': this.loadAudit(); break;
    }
  },

  // ===============================================
  // 1. DASHBOARD
  // ===============================================
  async loadDashboard(showLoader = true) {
    try {
      const res = await fetch('/api/dashboard');
      const data = await res.json();

      const fin = data.financials || {};
      const rev = fin.totalRevenue || 0;
      const exp = fin.totalExpenses || 0;
      const net = fin.netProfit || 0;

      document.getElementById('kpiRevenue').textContent = this.formatCurrency(rev);
      document.getElementById('kpiNetMargin').textContent = `Net Profit: ${this.formatCurrency(net)}`;
      document.getElementById('kpiValuation').textContent = this.formatCurrency(data.inventoryValuation || 0);
      document.getElementById('kpiProductsCount').textContent = `${data.totalProducts || 0} SKUs (${data.lowStockCount || 0} Low Stock)`;
      document.getElementById('kpiHeadcount').textContent = data.activeEmployees || 0;
      document.getElementById('kpiDeptsCount').textContent = `Across ${data.totalDepartments || 0} Departments`;

      const totalOps = (data.openSalesOrders || 0) + (data.openPurchaseOrders || 0) + (data.activeWorkOrders || 0);
      document.getElementById('kpiOpenOrders').textContent = totalOps;
      document.getElementById('kpiOrderSplit').textContent = `${data.openSalesOrders} SO | ${data.openPurchaseOrders} PO | ${data.activeWorkOrders} WO`;

      if (data.deployment && data.deployment.mode) {
        document.getElementById('deploymentLabel').textContent = `${data.deployment.mode} CLOUD / READY`;
      }

      // Render Charts
      this.renderFinancialChart(rev, exp, net);
      this.renderDepartmentChart();

      // Render recent events
      const audits = data.recentAudits || [];
      const tbody = document.getElementById('dashboardAuditTable');
      if (tbody) {
        if (audits.length === 0) {
          tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">No recent events found.</td></tr>`;
        } else {
          tbody.innerHTML = audits.slice(0, 8).map(a => `
            <tr>
              <td><small>${this.formatDateTime(a.timestamp)}</small></td>
              <td><span class="badge ${this.getModuleBadge(a.module)}">${a.module}</span></td>
              <td><strong>${a.action}</strong></td>
              <td><code>${a.entityId || 'N/A'}</code></td>
              <td>${a.performedBy || 'System'}</td>
              <td>${a.details || ''}</td>
            </tr>
          `).join('');
        }
      }
    } catch (e) {
      console.error("Dashboard load failed:", e);
    }
  },

  renderFinancialChart(rev, exp, net) {
    const ctx = document.getElementById('financialChart');
    if (!ctx) return;
    
    if (this.financialChart) this.financialChart.destroy();
    
    this.financialChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['Total Revenue', 'Operating Expenses', 'Net Operating Profit'],
        datasets: [{
          label: 'Amount ($)',
          data: [rev, exp, net],
          backgroundColor: ['#10b981', '#ef4444', '#2563eb'],
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { display: false }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: value => '$' + value.toLocaleString()
            }
          }
        }
      }
    });
  },

  async renderDepartmentChart() {
    const ctx = document.getElementById('departmentChart');
    if (!ctx) return;

    try {
      const res = await fetch('/api/hr/departments');
      const depts = await res.json();
      
      const labels = depts.map(d => d.name);
      const budgets = depts.map(d => d.budget || 0);

      if (this.departmentChart) this.departmentChart.destroy();

      this.departmentChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: labels,
          datasets: [{
            data: budgets,
            backgroundColor: [
              '#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#06b6d4'
            ]
          }]
        },
        options: {
          responsive: true,
          plugins: {
            legend: { position: 'bottom', labels: { boxWidth: 12, font: { size: 10 } } }
          }
        }
      });
    } catch (e) {
      console.error(e);
    }
  },

  // ===============================================
  // 2. FINANCE
  // ===============================================
  async loadFinance() {
    try {
      const [accRes, jeRes, invRes, dashRes] = await Promise.all([
        fetch('/api/finance/accounts'),
        fetch('/api/finance/journal-entries'),
        fetch('/api/finance/invoices'),
        fetch('/api/dashboard')
      ]);

      const accounts = await accRes.json();
      const entries = await jeRes.json();
      const invoices = await invRes.json();
      const dash = await dashRes.json();

      const fin = dash.financials || {};
      document.getElementById('finTotalAssets').textContent = this.formatCurrency(fin.totalAssets || 0);
      document.getElementById('finTotalLiab').textContent = this.formatCurrency(fin.totalLiabilities || 0);
      document.getElementById('finTotalEquity').textContent = this.formatCurrency(fin.totalEquity || 0);
      document.getElementById('finNetProfit').textContent = this.formatCurrency(fin.netProfit || 0);

      // Accounts Table
      const accTbody = document.getElementById('chartOfAccountsTable');
      accTbody.innerHTML = accounts.map(a => `
        <tr>
          <td><strong>${a.accountCode}</strong></td>
          <td>${a.accountName}</td>
          <td><span class="badge ${this.getAccountBadge(a.type)}">${a.type}</span></td>
          <td><strong>${this.formatCurrency(a.balance)}</strong></td>
          <td>${a.description || ''}</td>
        </tr>
      `).join('');

      // Journal Entries Table
      const jeTbody = document.getElementById('journalEntriesTable');
      jeTbody.innerHTML = entries.map(e => `
        <tr>
          <td><code>${e.entryNumber}</code></td>
          <td>${e.entryDate}</td>
          <td>${e.description}</td>
          <td><span class="badge badge-info">${e.referenceType || 'GL'}</span></td>
          <td>${e.referenceNumber || 'N/A'}</td>
          <td><strong style="color:#059669;">${this.formatCurrency(e.totalDebit)}</strong></td>
          <td><strong style="color:#2563eb;">${this.formatCurrency(e.totalCredit)}</strong></td>
          <td><span class="badge badge-success">${e.status}</span></td>
        </tr>
      `).join('');

      // Invoices Table
      const invTbody = document.getElementById('invoicesTable');
      invTbody.innerHTML = invoices.map(i => {
        const isAr = i.type === 'AR_SALES';
        const remaining = (i.amount || 0) - (i.paidAmount || 0);
        const canPay = i.status !== 'PAID' && i.status !== 'CANCELLED';
        return `
          <tr>
            <td><code>${i.invoiceNumber}</code></td>
            <td><span class="badge ${isAr ? 'badge-success' : 'badge-warning'}">${isAr ? 'AR Customer' : 'AP Supplier'}</span></td>
            <td><strong>${i.partyName}</strong></td>
            <td>${i.referenceOrder || 'N/A'}</td>
            <td>${i.dueDate || 'N/A'}</td>
            <td>${this.formatCurrency(i.amount)}</td>
            <td>${this.formatCurrency(i.paidAmount)}</td>
            <td><span class="badge ${i.status === 'PAID' ? 'badge-success' : 'badge-warning'}">${i.status}</span></td>
            <td>
              ${canPay ? `<button class="btn btn-sm btn-success" onclick="app.openPayInvoiceModal(${i.id}, '${i.invoiceNumber}', '${i.partyName}', ${remaining})">${isAr ? 'Collect' : 'Pay'}</button>` : '—'}
            </td>
          </tr>
        `;
      }).join('');

    } catch (e) {
      console.error(e);
      this.showToast('Failed to load finance data', 'error');
    }
  },

  openPayInvoiceModal(id, num, party, balance) {
    document.getElementById('payInvoiceId').value = id;
    document.getElementById('payInvoiceAmount').value = balance.toFixed(2);
    document.getElementById('payInvoiceAmount').max = balance;
    document.getElementById('payInvoiceDetails').textContent = `Processing transaction for Invoice ${num} (${party}). Balance due: $${balance.toFixed(2)}`;
    this.openModal('modalPayInvoice');
  },

  async handlePayInvoice(e) {
    e.preventDefault();
    const id = document.getElementById('payInvoiceId').value;
    const amount = parseFloat(document.getElementById('payInvoiceAmount').value);

    try {
      const res = await fetch(`/api/finance/invoices/${id}/pay`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ amount })
      });
      if (!res.ok) throw new Error(await res.text());
      this.closeModal('modalPayInvoice');
      this.showToast(`Recorded payment of $${amount.toFixed(2)}! Journal Entry posted.`, 'success');
      this.loadFinance();
    } catch (err) {
      this.showToast(err.message || 'Payment failed', 'error');
    }
  },

  // ===============================================
  // 3. HUMAN RESOURCES
  // ===============================================
  async loadHr() {
    try {
      const [empRes, deptRes, leaveRes, payRes, reqRes, chRes, postRes] = await Promise.all([
        fetch('/api/hr/employees'),
        fetch('/api/hr/departments'),
        fetch('/api/hr/leaves'),
        fetch('/api/hr/payrolls'),
        fetch('/api/hr/requisitions'),
        fetch('/api/recruitment/channels'),
        fetch('/api/recruitment/posts')
      ]);

      const employees = await empRes.json();
      const departments = await deptRes.json();
      const leaves = await leaveRes.json();
      const payrolls = await payRes.json();
      const requisitions = await reqRes.json();
      const channels = await chRes.json();
      const recruitmentPosts = await postRes.json();

      this.requisitions = requisitions || [];
      this.channels = channels || [];
      this.recruitmentPosts = recruitmentPosts || [];

      // 1. Requisition KPIs & Table
      const pendingReqs = this.requisitions.filter(r => r.status === 'PENDING_APPROVAL');
      const activeReqs = this.requisitions.filter(r => r.status === 'APPROVED' || r.status === 'OPEN');
      const totalOpenings = this.requisitions
        .filter(r => r.status !== 'CANCELLED')
        .reduce((sum, r) => sum + (r.openingsCount || 1), 0);

      const kpiTotalEl = document.getElementById('kpiTotalReqs');
      if (kpiTotalEl) kpiTotalEl.textContent = this.requisitions.length;
      const kpiPendingEl = document.getElementById('kpiPendingReqs');
      if (kpiPendingEl) kpiPendingEl.textContent = pendingReqs.length;
      const kpiActiveEl = document.getElementById('kpiActiveReqs');
      if (kpiActiveEl) kpiActiveEl.textContent = activeReqs.length;
      const kpiOpeningsEl = document.getElementById('kpiTotalOpenings');
      if (kpiOpeningsEl) kpiOpeningsEl.textContent = totalOpenings;

      // Update badge counts in sidebar and subtab pill
      const badgeMini = document.getElementById('badgePendingReqs');
      if (badgeMini) badgeMini.textContent = pendingReqs.length;
      const pillReq = document.getElementById('pillReqCount');
      if (pillReq) pillReq.textContent = this.requisitions.length;

      // 2. Recruitment Multi-Channel Section KPIs & Status
      const connectedChannels = this.channels.filter(ch => ch.connected);
      const totalViews = this.recruitmentPosts.reduce((sum, p) =>
        sum + (p.distributions || []).reduce((dSum, d) => dSum + (d.viewsCount || 0), 0), 0);
      const totalApplicants = this.recruitmentPosts.reduce((sum, p) =>
        sum + (p.distributions || []).reduce((dSum, d) => dSum + (d.applicantsCount || 0), 0), 0);

      const kpiTotalPostsEl = document.getElementById('kpiTotalPosts');
      if (kpiTotalPostsEl) kpiTotalPostsEl.textContent = this.recruitmentPosts.length;
      const kpiConnEl = document.getElementById('kpiConnectedChannels');
      if (kpiConnEl) kpiConnEl.textContent = `${connectedChannels.length} / ${this.channels.length}`;
      const kpiViewsEl = document.getElementById('kpiTotalViews');
      if (kpiViewsEl) kpiViewsEl.textContent = totalViews.toLocaleString();
      const kpiAppsEl = document.getElementById('kpiTotalApplicants');
      if (kpiAppsEl) kpiAppsEl.textContent = totalApplicants.toLocaleString();

      const badgeRecruit = document.getElementById('badgeRecruitmentPosts');
      if (badgeRecruit) badgeRecruit.textContent = this.recruitmentPosts.length;
      const pillRecruit = document.getElementById('pillRecruitmentCount');
      if (pillRecruit) pillRecruit.textContent = this.recruitmentPosts.length;

      // Render Connected Channels Status Ribbon
      const stripEl = document.getElementById('channelsStatusStrip');
      if (stripEl) {
        stripEl.innerHTML = this.channels.map(ch => `
          <div class="channel-card ${ch.connected ? 'connected' : 'disconnected'}">
            <div class="channel-card-header">
              <div class="channel-info">
                <div class="channel-icon">${ch.logoIcon || '🌐'}</div>
                <div>
                  <div class="channel-name">${ch.name}</div>
                  <div class="channel-account">${ch.accountIdentifier || 'Bridge Ready'}</div>
                </div>
              </div>
              <span class="channel-status-pill ${ch.connected ? 'online' : 'offline'}">
                ${ch.connected ? '● Live Bridge' : '○ Offline'}
              </span>
            </div>
            <div class="channel-meta-stats">
              <span>Auto-Post: <strong>${ch.autoPostEnabled ? 'Active' : 'Off'}</strong></span>
              <span>Active Circulars: <strong>${ch.activePostsCount || 0}</strong></span>
              <span>Applicants: <strong style="color:#10b981;">${ch.totalApplicantsReceived || 0}</strong></span>
            </div>
          </div>
        `).join('');
      }

      // Render Recruitment Posts Table
      const postTbody = document.getElementById('recruitmentPostsTable');
      if (postTbody) {
        postTbody.innerHTML = this.recruitmentPosts.map(p => {
          const deptName = p.department ? p.department.name : 'N/A';
          const distBadges = (p.distributions || []).map(d => {
            let badgeClass = 'dist-badge-portal';
            let label = d.channelName;
            if (d.channelCode === 'LINKEDIN') {
              badgeClass = 'dist-badge-linkedin';
              label = `💼 LinkedIn: Live (${d.applicantsCount || 0} apps)`;
            } else if (d.channelCode === 'BDJOBS') {
              badgeClass = 'dist-badge-bdjobs';
              label = `🇧🇩 BDJobs: Live (${d.applicantsCount || 0} apps)`;
            } else if (d.channelCode === 'INDEED') {
              badgeClass = 'dist-badge-indeed';
              label = `🔍 Indeed: Live (${d.applicantsCount || 0} apps)`;
            } else {
              label = `🌐 Portal: Live (${d.applicantsCount || 0} apps)`;
            }
            return `<a class="dist-badge ${badgeClass}" title="View live sync for ${d.channelName}" onclick="app.viewRecruitmentPostDetails(${p.id})">${label}</a>`;
          }).join('');

          const postAppsTotal = (p.distributions || []).reduce((sum, d) => sum + (d.applicantsCount || 0), 0);

          return `
            <tr>
              <td><code>${p.postingCode}</code></td>
              <td>
                <div style="font-weight:700; color:#0f172a; font-size:0.95rem;">${p.jobTitle}</div>
                ${p.jobRequisition ? `<span style="font-size:0.75rem; color:#64748b;">Ref Requisition: <code style="color:#0284c7;">${p.jobRequisition.requisitionNumber}</code></span>` : ''}
              </td>
              <td>
                <div><span class="badge badge-info">${deptName}</span></div>
                <div style="font-size:0.75rem; color:#64748b; margin-top:2px;">📍 ${p.location}</div>
              </td>
              <td><span style="font-size:0.82rem; color:#334155; font-weight:500;">${p.employmentType} &bull; ${p.experienceLevel}</span></td>
              <td><strong style="color:#059669; font-size:0.84rem;">${p.salaryDisplay || 'Competitive'}</strong></td>
              <td><div class="dist-badges-wrap">${distBadges}</div></td>
              <td><span class="badge badge-success" style="font-weight:700; font-size:0.8rem;">📥 ${postAppsTotal} Candidates</span></td>
              <td><span class="badge ${p.status === 'PUBLISHED' ? 'badge-success' : 'badge-secondary'}">${p.status}</span></td>
              <td>
                <div style="display:flex; gap:6px;">
                  <button class="btn btn-sm btn-purple" onclick="app.viewRecruitmentPostDetails(${p.id})">👁️ Dossier</button>
                  <button class="btn btn-sm btn-secondary" title="Simulate Candidate Apply" onclick="app.simulateApplicantPrompt(${p.id})">⚡ + Candidate</button>
                  ${p.status === 'PUBLISHED' ? `<button class="btn btn-sm btn-danger" title="Close Post" onclick="app.closeRecruitmentPost(${p.id})">✕</button>` : ''}
                </div>
              </td>
            </tr>
          `;
        }).join('');
      }

      // Requisitions Table
      const reqTbody = document.getElementById('jobRequisitionsTable');
      if (reqTbody) {
        reqTbody.innerHTML = this.requisitions.map(r => {
          const deptName = r.department ? r.department.name : 'N/A';
          const salaryDisplay = (r.salaryMin && r.salaryMax)
            ? `${this.formatCurrency(r.salaryMin)} - ${this.formatCurrency(r.salaryMax)}`
            : '—';

          let quickActionBtn = '';
          if (r.status === 'PENDING_APPROVAL') {
            quickActionBtn = `<button class="btn btn-sm btn-success" title="Approve Requisition" onclick="app.updateRequisitionStatus(${r.id}, 'APPROVED')">✓ Approve</button>`;
          } else if (r.status === 'APPROVED') {
            quickActionBtn = `<button class="btn btn-sm btn-primary" title="Launch Recruitment" onclick="app.updateRequisitionStatus(${r.id}, 'OPEN')">🚀 Recruit</button>`;
          } else if (r.status === 'OPEN') {
            quickActionBtn = `<button class="btn btn-sm btn-success" title="Mark Position Filled" onclick="app.updateRequisitionStatus(${r.id}, 'FILLED')">🎯 Filled</button>`;
          }

          return `
            <tr>
              <td><code>${r.requisitionNumber}</code></td>
              <td><strong style="color:#0f172a; font-size:0.95rem;">${r.jobTitle}</strong></td>
              <td><span class="badge badge-info">${deptName}</span></td>
              <td><span class="badge badge-secondary">${r.team || 'General'}</span></td>
              <td><span style="color:#334155; font-weight:500;">${r.hiringManager}</span></td>
              <td><span class="badge badge-primary" style="font-weight:700;">${r.openingsCount}</span></td>
              <td><span style="font-size:0.82rem; color:#334155; font-weight:500;">${r.jobType} &bull; ${r.experienceLevel}</span></td>
              <td><strong style="color:#10b981; font-size:0.82rem;">${salaryDisplay}</strong></td>
              <td>${r.targetStartDate || 'ASAP'}</td>
              <td><span class="badge ${this.getRequisitionBadge(r.status)}">${r.status}</span></td>
              <td>
                <div style="display:flex; gap:6px;">
                  ${quickActionBtn}
                  <button class="btn btn-sm btn-secondary" onclick="app.viewRequisitionDetails(${r.id})">👁️ Dossier</button>
                </div>
              </td>
            </tr>
          `;
        }).join('');
      }

      // Employees Table
      const empTbody = document.getElementById('employeesTable');
      empTbody.innerHTML = employees.map(emp => `
        <tr>
          <td><code>${emp.employeeCode}</code></td>
          <td><strong>${emp.fullName}</strong></td>
          <td><span class="badge badge-info">${emp.department ? emp.department.name : 'Unassigned'}</span></td>
          <td>${emp.position || ''}</td>
          <td><span class="badge badge-secondary">${emp.team || 'General'}</span></td>
          <td>${emp.email}</td>
          <td>${this.formatCurrency(emp.salary)}</td>
          <td><span class="badge ${emp.status === 'ACTIVE' ? 'badge-success' : 'badge-warning'}">${emp.status}</span></td>
        </tr>
      `).join('');

      // Departments Table
      const deptTbody = document.getElementById('departmentsTable');
      deptTbody.innerHTML = departments.map(d => `
        <tr>
          <td><code>${d.code}</code></td>
          <td><strong>${d.name}</strong></td>
          <td>${d.managerName || 'N/A'}</td>
          <td>${this.formatCurrency(d.budget)}</td>
          <td>${d.location || 'N/A'}</td>
        </tr>
      `).join('');

      // Populate Department Selects for Hiring Modal & Requisitions Modal
      const deptSelect = document.getElementById('selectEmployeeDept');
      if (deptSelect) {
        deptSelect.innerHTML = departments.map(d => `<option value="${d.id}">${d.name} (${d.code})</option>`).join('');
      }
      const reqDeptSelect = document.getElementById('selectReqDept');
      if (reqDeptSelect) {
        reqDeptSelect.innerHTML = departments.map(d => `<option value="${d.id}">${d.name} (${d.code})</option>`).join('');
      }

      // Populate Department Select for Recruitment Post Modal
      const postDeptSelect = document.getElementById('selectPostDept');
      if (postDeptSelect) {
        postDeptSelect.innerHTML = departments.map(d => `<option value="${d.id}">${d.name} (${d.code})</option>`).join('');
      }

      // Populate Requisition Autofill Dropdown in Post Modal
      const postReqSelect = document.getElementById('selectPostFromReq');
      if (postReqSelect) {
        postReqSelect.innerHTML = `<option value="">-- Choose Approved Requisition to Autofill (Optional) --</option>` +
          this.requisitions.map(r => `<option value="${r.id}">${r.requisitionNumber} - ${r.jobTitle} (${r.department ? r.department.name : 'N/A'} - ${r.openingsCount} Openings)</option>`).join('');
      }

      this.renderManageChannelsModal();

      // Leaves Table
      const leaveTbody = document.getElementById('leaveRequestsTable');
      leaveTbody.innerHTML = leaves.map(l => `
        <tr>
          <td><strong>${l.employee ? l.employee.fullName : 'N/A'}</strong></td>
          <td>${l.leaveType}</td>
          <td>${l.startDate} to ${l.endDate}</td>
          <td>${l.days} days</td>
          <td><span class="badge ${l.status === 'APPROVED' ? 'badge-success' : l.status === 'PENDING' ? 'badge-warning' : 'badge-danger'}">${l.status}</span></td>
          <td>
            ${l.status === 'PENDING' ? `
              <button class="btn btn-sm btn-success" onclick="app.updateLeaveStatus(${l.id}, 'APPROVED')">✓ Approve</button>
              <button class="btn btn-sm btn-secondary" onclick="app.updateLeaveStatus(${l.id}, 'REJECTED')">✕ Reject</button>
            ` : '—'}
          </td>
        </tr>
      `).join('');

      // Payrolls Table
      const payTbody = document.getElementById('payrollTable');
      payTbody.innerHTML = payrolls.map(p => `
        <tr>
          <td><code>${p.payrollNumber}</code></td>
          <td><strong>${p.employee ? p.employee.fullName : 'N/A'}</strong></td>
          <td>${p.payPeriod}</td>
          <td>${this.formatCurrency(p.grossSalary)}</td>
          <td>${this.formatCurrency(p.taxAmount)}</td>
          <td><strong style="color:#059669;">${this.formatCurrency(p.netSalary)}</strong></td>
          <td>${p.paymentDate}</td>
          <td><span class="badge badge-success">${p.status}</span></td>
        </tr>
      `).join('');

    } catch (e) {
      console.error(e);
      this.showToast('Failed to load HR data', 'error');
    }
  },

  getRequisitionBadge(status) {
    switch (status) {
      case 'DRAFT': return 'badge-secondary';
      case 'PENDING_APPROVAL': return 'badge-warning';
      case 'APPROVED': return 'badge-info';
      case 'OPEN': return 'badge-success';
      case 'FILLED': return 'badge-purple';
      case 'CANCELLED': return 'badge-danger';
      default: return 'badge-secondary';
    }
  },

  openNewRequisitionModal() {
    const nextIndex = (this.requisitions ? this.requisitions.length : 0) + 1;
    const nextNum = 'REQ-2026-00' + nextIndex;
    const numInput = document.getElementById('newReqNumber');
    if (numInput) numInput.value = nextNum;

    const targetDate = new Date();
    targetDate.setDate(targetDate.getDate() + 30);
    const dateInput = document.getElementById('reqTargetStartDate');
    if (dateInput) dateInput.value = targetDate.toISOString().split('T')[0];

    this.openModal('modalNewRequisition');
  },

  async handleNewRequisition(e) {
    e.preventDefault();
    const form = e.target;
    const req = {
      requisitionNumber: form.requisitionNumber.value,
      jobTitle: form.jobTitle.value,
      department: { id: parseInt(form.departmentId.value) },
      team: form.team && form.team.value ? form.team.value : 'General',
      hiringManager: form.hiringManager.value,
      openingsCount: parseInt(form.openingsCount.value) || 1,
      jobType: form.jobType.value,
      experienceLevel: form.experienceLevel.value,
      salaryMin: parseFloat(form.salaryMin.value),
      salaryMax: parseFloat(form.salaryMax.value),
      targetStartDate: form.targetStartDate.value,
      status: form.status.value,
      reasonForHire: form.reasonForHire.value,
      description: form.description.value
    };

    try {
      const res = await fetch('/api/hr/requisitions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(req)
      });
      if (!res.ok) throw new Error(await res.text());
      this.closeModal('modalNewRequisition');
      this.showToast(`Job Requisition ${req.requisitionNumber} (${req.jobTitle}) created successfully!`, 'success');
      form.reset();
      this.loadHr();
    } catch (err) {
      this.showToast(err.message || 'Error submitting requisition', 'error');
    }
  },

  async updateRequisitionStatus(id, newStatus) {
    try {
      const res = await fetch(`/api/hr/requisitions/${id}/status`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus })
      });
      if (!res.ok) throw new Error(await res.text());
      this.showToast(`Requisition status updated to ${newStatus}`, 'success');
      const viewModal = document.getElementById('modalViewRequisition');
      if (viewModal && viewModal.classList.contains('active')) {
        this.closeModal('modalViewRequisition');
      }
      this.loadHr();
    } catch (err) {
      this.showToast(err.message || 'Error updating requisition', 'error');
    }
  },

  viewRequisitionDetails(id) {
    const req = (this.requisitions || []).find(r => r.id === id);
    if (!req) return;

    const titleEl = document.getElementById('viewReqTitle');
    if (titleEl) {
      titleEl.innerHTML = `📋 Job Requisition: <code>${req.requisitionNumber}</code>`;
    }

    const deptName = req.department ? req.department.name : 'N/A';
    const salaryRange = (req.salaryMin && req.salaryMax)
      ? `${this.formatCurrency(req.salaryMin)} - ${this.formatCurrency(req.salaryMax)}`
      : 'Negotiable / TBD';

    const bodyEl = document.getElementById('viewReqBody');
    if (bodyEl) {
      bodyEl.innerHTML = `
        <div style="background:#0f172a; padding:16px; border-radius:8px; border:1px solid #334155; margin-bottom:16px;">
          <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:10px; flex-wrap:wrap; gap:8px;">
            <div>
              <h2 style="font-size:1.15rem; color:#f8fafc; margin:0 0 4px 0;">${req.jobTitle}</h2>
              <div style="font-size:0.85rem; color:#94a3b8;">
                <span>🏢 Department: <strong>${deptName}</strong></span> &bull; 
                <span>👥 Team: <strong>${req.team || 'General'}</strong></span> &bull; 
                <span>🎯 Openings: <strong>${req.openingsCount}</strong></span>
              </div>
            </div>
            <span class="badge ${this.getRequisitionBadge(req.status)}" style="font-size:0.82rem; padding:4px 10px;">${req.status}</span>
          </div>

          <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px; font-size:0.82rem; margin-top:14px; padding-top:12px; border-top:1px solid #1e293b;">
            <div><span style="color:#64748b;">Hiring Manager:</span> <strong>${req.hiringManager}</strong></div>
            <div><span style="color:#64748b;">Target Start Date:</span> <strong>${req.targetStartDate || 'ASAP'}</strong></div>
            <div><span style="color:#64748b;">Employment Type:</span> <strong>${req.jobType} (${req.experienceLevel})</strong></div>
            <div><span style="color:#64748b;">Target Salary Range:</span> <strong style="color:#10b981;">${salaryRange}</strong></div>
            <div><span style="color:#64748b;">Requested Date:</span> <strong>${req.requestedDate || 'Recent'}</strong></div>
          </div>
        </div>

        <div style="margin-bottom:16px;">
          <h4 style="font-size:0.88rem; color:#38bdf8; margin-bottom:6px;">📌 Business Justification / Reason for Hire</h4>
          <div style="background:#1e293b; padding:12px 14px; border-radius:6px; font-size:0.85rem; color:#cbd5e1; border-left:3px solid #38bdf8;">
            ${req.reasonForHire || 'Operational staffing demand.'}
          </div>
        </div>

        <div>
          <h4 style="font-size:0.88rem; color:#38bdf8; margin-bottom:6px;">📝 Role Description & Candidate Requirements</h4>
          <div style="background:#1e293b; padding:12px 14px; border-radius:6px; font-size:0.85rem; color:#cbd5e1; white-space:pre-line;">
            ${req.description || 'No detailed description provided.'}
          </div>
        </div>
      `;
    }

    let actionButtons = '';
    if (req.status === 'PENDING_APPROVAL') {
      actionButtons = `
        <button class="btn btn-sm btn-success" onclick="app.updateRequisitionStatus(${req.id}, 'APPROVED')">✓ Approve Requisition</button>
        <button class="btn btn-sm btn-danger" onclick="app.updateRequisitionStatus(${req.id}, 'CANCELLED')">✕ Cancel</button>
      `;
    } else if (req.status === 'APPROVED') {
      actionButtons = `
        <button class="btn btn-sm btn-primary" onclick="app.updateRequisitionStatus(${req.id}, 'OPEN')">🚀 Launch Recruitment</button>
      `;
    } else if (req.status === 'OPEN') {
      actionButtons = `
        <button class="btn btn-sm btn-success" onclick="app.updateRequisitionStatus(${req.id}, 'FILLED')">🎯 Mark Position Filled</button>
        <button class="btn btn-sm btn-secondary" onclick="app.updateRequisitionStatus(${req.id}, 'CANCELLED')">Cancel</button>
      `;
    }
    const actionsEl = document.getElementById('viewReqActions');
    if (actionsEl) actionsEl.innerHTML = actionButtons;

    this.openModal('modalViewRequisition');
  },

  // ===============================================
  // RECRUITMENT MULTI-CHANNEL & SECTORS HANDLERS
  // ===============================================
  openNewRecruitmentPostModal() {
    const nextIndex = (this.recruitmentPosts ? this.recruitmentPosts.length : 0) + 1;
    const nextCode = 'POST-2026-00' + nextIndex;
    const codeInput = document.getElementById('newPostCode');
    if (codeInput) codeInput.value = nextCode;

    const deadline = new Date();
    deadline.setDate(deadline.getDate() + 30);
    const deadlineInput = document.getElementById('newPostDeadline');
    if (deadlineInput) deadlineInput.value = deadline.toISOString().split('T')[0];

    const form = document.getElementById('formNewRecruitmentPost');
    if (form) {
      const selectReq = document.getElementById('selectPostFromReq');
      if (selectReq) selectReq.value = '';
    }

    this.openModal('modalNewRecruitmentPost');
  },

  handleImportRequisition(reqId) {
    if (!reqId) return;
    const req = (this.requisitions || []).find(r => r.id === parseInt(reqId));
    if (!req) return;

    const titleEl = document.getElementById('newPostTitle');
    if (titleEl) titleEl.value = req.jobTitle || '';

    const deptEl = document.getElementById('selectPostDept');
    if (deptEl && req.department) deptEl.value = req.department.id;

    const teamEl = document.getElementById('newPostTeam');
    if (teamEl) teamEl.value = req.team || '';

    const empTypeEl = document.getElementById('newPostEmpType');
    if (empTypeEl) empTypeEl.value = req.jobType || 'Full-time';

    const expLevelEl = document.getElementById('newPostExpLevel');
    if (expLevelEl) expLevelEl.value = req.experienceLevel || 'Senior';

    const salaryEl = document.getElementById('newPostSalary');
    if (salaryEl) {
      if (req.salaryMin && req.salaryMax) {
        salaryEl.value = `$${req.salaryMin.toLocaleString()} - $${req.salaryMax.toLocaleString()} / year`;
      }
    }

    const reqIdEl = document.getElementById('newPostReqId');
    if (reqIdEl) reqIdEl.value = req.id;

    const summaryEl = document.getElementById('newPostSummary');
    if (summaryEl) summaryEl.value = `Join PHANTOM-ERP as ${req.jobTitle} in our ${req.department ? req.department.name : 'Engineering'} division.`;

    const descEl = document.getElementById('newPostDesc');
    if (descEl) descEl.value = req.description || '';

    const reqsEl = document.getElementById('newPostReqs');
    if (reqsEl) reqsEl.value = req.reasonForHire ? `Key business focus: ${req.reasonForHire}. Prior experience in high-growth environments.` : '';

    this.showToast(`Imported details from Requisition ${req.requisitionNumber}!`, 'info');
  },

  async handleNewRecruitmentPost(e) {
    e.preventDefault();
    const form = e.target;

    const checkedChannels = Array.from(form.querySelectorAll('input[name="postChannels"]:checked'))
      .map(cb => cb.value);

    if (checkedChannels.length === 0) {
      this.showToast('Please select at least one recruitment channel to broadcast.', 'warning');
      return;
    }

    const postData = {
      postingCode: form.postingCode.value,
      jobTitle: form.jobTitle.value,
      department: { id: parseInt(form.departmentId.value) },
      team: form.team && form.team.value ? form.team.value : 'General',
      location: form.location.value,
      employmentType: form.employmentType.value,
      experienceLevel: form.experienceLevel.value,
      salaryDisplay: form.salaryDisplay.value,
      applicationDeadline: form.applicationDeadline.value,
      summary: form.summary.value,
      description: form.description.value,
      requirements: form.requirements ? form.requirements.value : '',
      benefits: form.benefits ? form.benefits.value : ''
    };

    if (form.requisitionId && form.requisitionId.value) {
      postData.jobRequisition = { id: parseInt(form.requisitionId.value) };
    }

    try {
      const res = await fetch('/api/recruitment/posts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ post: postData, channels: checkedChannels })
      });
      if (!res.ok) throw new Error(await res.text());
      const saved = await res.json();
      this.closeModal('modalNewRecruitmentPost');
      this.showToast(`📢 Job Post '${saved.jobTitle}' successfully broadcasted to ${checkedChannels.join(', ')}!`, 'success');
      form.reset();
      this.loadHr();
    } catch (err) {
      this.showToast(err.message || 'Error broadcasting post', 'error');
    }
  },

  viewRecruitmentPostDetails(id) {
    const post = (this.recruitmentPosts || []).find(p => p.id === id);
    if (!post) return;

    const titleEl = document.getElementById('viewPostTitle');
    if (titleEl) {
      titleEl.innerHTML = `📢 Multi-Sector Distribution: <code>${post.postingCode}</code>`;
    }

    const deptName = post.department ? post.department.name : 'N/A';
    const totalApps = (post.distributions || []).reduce((sum, d) => sum + (d.applicantsCount || 0), 0);
    const totalViews = (post.distributions || []).reduce((sum, d) => sum + (d.viewsCount || 0), 0);
    const totalClicks = (post.distributions || []).reduce((sum, d) => sum + (d.clicksCount || 0), 0);

    const distCardsHtml = (post.distributions || []).map(d => {
      let icon = '🌐';
      let themeColor = '#10b981';
      if (d.channelCode === 'LINKEDIN') { icon = '💼'; themeColor = '#0a66c2'; }
      else if (d.channelCode === 'BDJOBS') { icon = '🇧🇩'; themeColor = '#dc2626'; }
      else if (d.channelCode === 'INDEED') { icon = '🔍'; themeColor = '#2563eb'; }

      return `
        <div style="background:#0f172a; border:1px solid #334155; border-left:4px solid ${themeColor}; border-radius:6px; padding:12px 14px; margin-bottom:10px;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px; flex-wrap:wrap; gap:6px;">
            <div style="display:flex; align-items:center; gap:8px;">
              <span style="font-size:1.2rem;">${icon}</span>
              <div>
                <strong style="color:#f8fafc; font-size:0.9rem;">${d.channelName}</strong>
                <div style="font-size:0.72rem; color:#94a3b8;">External Circular ID: <code>${d.externalListingId}</code></div>
              </div>
            </div>
            <div style="display:flex; align-items:center; gap:8px;">
              <span class="badge badge-success" style="font-size:0.72rem;">● LIVE & SYNCED</span>
              <a href="${d.externalUrl}" target="_blank" class="btn btn-sm btn-secondary" style="font-size:0.75rem; padding:3px 8px;" onclick="event.preventDefault(); window.open('${d.externalUrl}', '_blank');">🔗 Preview Listing</a>
            </div>
          </div>
          <div style="display:flex; gap:16px; font-size:0.78rem; color:#cbd5e1; margin-top:8px; padding-top:6px; border-top:1px solid #1e293b; flex-wrap:wrap;">
            <span>Impressions: <strong style="color:#38bdf8;">${d.viewsCount || 0}</strong></span>
            <span>Clicks: <strong style="color:#a855f7;">${d.clicksCount || 0}</strong></span>
            <span>Applications: <strong style="color:#10b981; font-size:0.85rem;">📥 ${d.applicantsCount || 0}</strong></span>
            <button class="btn btn-sm btn-purple" style="margin-left:auto; font-size:0.7rem; padding:2px 8px;" onclick="app.simulateApplicant(${post.id}, '${d.channelCode}')">⚡ Simulate Candidate</button>
          </div>
        </div>
      `;
    }).join('');

    const bodyEl = document.getElementById('viewPostBody');
    if (bodyEl) {
      bodyEl.innerHTML = `
        <div style="background:#1e293b; padding:16px; border-radius:8px; margin-bottom:16px; border:1px solid #334155;">
          <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:8px; flex-wrap:wrap; gap:8px;">
            <div>
              <h2 style="font-size:1.2rem; color:#f8fafc; margin:0 0 4px 0;">${post.jobTitle}</h2>
              <div style="font-size:0.82rem; color:#94a3b8;">
                <span>🏢 ${deptName}</span> &bull; 
                <span>📍 ${post.location}</span> &bull; 
                <span>🎯 ${post.employmentType} (${post.experienceLevel})</span>
              </div>
            </div>
            <span class="badge ${post.status === 'PUBLISHED' ? 'badge-success' : 'badge-secondary'}" style="font-size:0.82rem; padding:4px 10px;">${post.status}</span>
          </div>

          <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(140px, 1fr)); gap:10px; font-size:0.8rem; margin-top:12px; padding-top:10px; border-top:1px solid #334155;">
            <div><span style="color:#64748b;">Target Compensation:</span><br><strong style="color:#10b981;">${post.salaryDisplay || 'Competitive'}</strong></div>
            <div><span style="color:#64748b;">Total Impressions:</span><br><strong style="color:#38bdf8;">${totalViews} views</strong></div>
            <div><span style="color:#64748b;">Total Clicks:</span><br><strong style="color:#a855f7;">${totalClicks} clicks</strong></div>
            <div><span style="color:#64748b;">Candidates Applied:</span><br><strong style="color:#10b981; font-size:0.95rem;">📥 ${totalApps} Applicants</strong></div>
          </div>
        </div>

        <div style="margin-bottom:14px;">
          <h4 style="font-size:0.85rem; color:#38bdf8; margin-bottom:8px;">🌐 Live Connected Channels Distribution Status</h4>
          ${distCardsHtml}
        </div>

        <div style="margin-bottom:12px;">
          <h4 style="font-size:0.85rem; color:#38bdf8; margin-bottom:4px;">📝 Role Summary & Description</h4>
          <div style="background:#0f172a; padding:10px 12px; border-radius:6px; font-size:0.82rem; color:#cbd5e1; white-space:pre-line;">
            ${post.description || post.summary || 'N/A'}
          </div>
        </div>
      `;
    }

    let actions = `
      <button class="btn btn-sm btn-purple" onclick="app.simulateApplicantPrompt(${post.id})">⚡ Simulate Candidates</button>
    `;
    if (post.status === 'PUBLISHED') {
      actions += `
        <button class="btn btn-sm btn-danger" onclick="app.closeRecruitmentPost(${post.id})">✕ Close Listing Across Channels</button>
      `;
    }
    const actEl = document.getElementById('viewPostActions');
    if (actEl) actEl.innerHTML = actions;

    this.openModal('modalViewRecruitmentPost');
  },

  async closeRecruitmentPost(id) {
    try {
      const res = await fetch(`/api/recruitment/posts/${id}/close`, { method: 'POST' });
      if (!res.ok) throw new Error(await res.text());
      this.showToast('Recruitment post closed across all connected platforms.', 'success');
      this.closeModal('modalViewRecruitmentPost');
      this.loadHr();
    } catch (err) {
      this.showToast(err.message || 'Error closing post', 'error');
    }
  },

  simulateApplicantPrompt(postId) {
    this.simulateApplicant(postId, 'LINKEDIN');
    setTimeout(() => {
      this.simulateApplicant(postId, 'BDJOBS');
    }, 600);
  },

  async simulateApplicant(postId, channelCode) {
    try {
      const res = await fetch(`/api/recruitment/posts/${postId}/simulate-applicant`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ channelCode: channelCode, count: 1 })
      });
      if (!res.ok) throw new Error(await res.text());
      const updated = await res.json();
      this.showToast(`🎯 New candidate profile received via ${channelCode}!`, 'success');
      this.loadHr();
      const modal = document.getElementById('modalViewRecruitmentPost');
      if (modal && modal.classList.contains('active')) {
        this.viewRecruitmentPostDetails(postId);
      }
    } catch (err) {
      this.showToast(err.message || 'Simulation error', 'error');
    }
  },

  async toggleChannel(id, connected, autoPost) {
    try {
      const res = await fetch(`/api/recruitment/channels/${id}/toggle`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ connected: connected, autoPost: autoPost })
      });
      if (!res.ok) throw new Error(await res.text());
      this.showToast('Recruitment sector status updated!', 'success');
      this.loadHr();
    } catch (err) {
      this.showToast(err.message || 'Error toggling channel', 'error');
    }
  },

  openManageChannelsModal() {
    this.renderManageChannelsModal();
    this.openModal('modalManageChannels');
  },

  renderManageChannelsModal() {
    const listEl = document.getElementById('manageChannelsList');
    if (!listEl || !this.channels) return;

    listEl.innerHTML = this.channels.map(ch => `
      <div style="background:#0f172a; border:1px solid #334155; border-radius:8px; padding:14px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px;">
        <div style="display:flex; align-items:center; gap:12px;">
          <div style="font-size:1.6rem;">${ch.logoIcon || '🌐'}</div>
          <div>
            <div style="font-weight:700; color:#f8fafc; font-size:0.95rem;">${ch.name}</div>
            <div style="font-size:0.75rem; color:#94a3b8;">${ch.accountIdentifier}</div>
            <div style="font-size:0.7rem; color:#38bdf8; margin-top:2px;">Endpoint: <code>${ch.apiEndpoint}</code> &bull; Key: <code>${ch.apiKeyPreview}</code></div>
          </div>
        </div>
        <div style="display:flex; align-items:center; gap:12px;">
          <button class="btn btn-sm ${ch.connected ? 'btn-danger' : 'btn-success'}" onclick="app.toggleChannel(${ch.id}, ${!ch.connected}, ${ch.autoPostEnabled})">
            ${ch.connected ? 'Disconnect' : 'Connect'}
          </button>
          <button class="btn btn-sm ${ch.autoPostEnabled ? 'btn-primary' : 'btn-secondary'}" onclick="app.toggleChannel(${ch.id}, ${ch.connected}, ${!ch.autoPostEnabled})">
            Auto-Post: ${ch.autoPostEnabled ? 'ON' : 'OFF'}
          </button>
        </div>
      </div>
    `).join('');
  },

  async handleNewEmployee(e) {
    e.preventDefault();
    const form = e.target;
    const emp = {
      employeeCode: form.employeeCode.value,
      firstName: form.firstName.value,
      lastName: form.lastName.value,
      email: form.email.value,
      position: form.position.value,
      team: form.team && form.team.value ? form.team.value : 'General',
      salary: parseFloat(form.salary.value),
      hireDate: form.hireDate.value,
      department: { id: parseInt(form.departmentId.value) }
    };

    try {
      const res = await fetch('/api/hr/employees', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(emp)
      });
      if (!res.ok) throw new Error(await res.text());
      this.closeModal('modalNewEmployee');
      this.showToast(`Employee ${emp.firstName} ${emp.lastName} added successfully!`, 'success');
      form.reset();
      this.loadHr();
    } catch (err) {
      this.showToast(err.message || 'Error creating employee', 'error');
    }
  },

  async handleRunPayroll(e) {
    e.preventDefault();
    const form = e.target;
    const period = form.period.value;

    try {
      const res = await fetch('/api/hr/payrolls/run', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ period })
      });
      if (!res.ok) throw new Error(await res.text());
      const payrolls = await res.json();
      this.closeModal('modalRunPayroll');
      this.showToast(`Payroll for ${period} processed for ${payrolls.length} employees! General ledger updated.`, 'success');
      this.loadHr();
    } catch (err) {
      this.showToast(err.message || 'Failed to run payroll', 'error');
    }
  },

  async updateLeaveStatus(id, status) {
    try {
      const res = await fetch(`/api/hr/leaves/${id}/status`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status })
      });
      if (!res.ok) throw new Error(await res.text());
      this.showToast(`Leave status updated to ${status}`, 'success');
      this.loadHr();
    } catch (err) {
      this.showToast(err.message || 'Failed to update leave', 'error');
    }
  },

  // ===============================================
  // 4. PROCUREMENT
  // ===============================================
  async loadProcurement() {
    try {
      const [poRes, venRes, prodRes] = await Promise.all([
        fetch('/api/procurement/orders'),
        fetch('/api/procurement/vendors'),
        fetch('/api/inventory/products')
      ]);

      const orders = await poRes.json();
      const vendors = await venRes.json();
      const products = await prodRes.json();

      // Orders Table
      const poTbody = document.getElementById('purchaseOrdersTable');
      poTbody.innerHTML = orders.map(po => {
        const canApprove = po.status === 'DRAFT';
        const canReceive = po.status === 'APPROVED';
        const itemsSummary = (po.items || []).map(i => `${i.product ? i.product.name : 'Item'} (x${i.quantity})`).join(', ');

        return `
          <tr>
            <td><code>${po.poNumber}</code></td>
            <td><strong>${po.vendor ? po.vendor.companyName : 'N/A'}</strong></td>
            <td>${po.orderDate}</td>
            <td>${po.expectedDeliveryDate || 'TBD'}</td>
            <td><small>${itemsSummary || 'No items'}</small></td>
            <td><strong>${this.formatCurrency(po.totalAmount)}</strong></td>
            <td><span class="badge ${this.getPoBadge(po.status)}">${po.status}</span></td>
            <td>
              ${canApprove ? `<button class="btn btn-sm btn-primary" onclick="app.approvePO(${po.id})">Approve PO</button>` : ''}
              ${canReceive ? `<button class="btn btn-sm btn-success" onclick="app.receivePO(${po.id})">📦 Receive Goods</button>` : ''}
              ${po.status === 'RECEIVED' ? '<span style="color:#059669; font-size:0.8rem;">✓ Received & Stocked</span>' : ''}
            </td>
          </tr>
        `;
      }).join('');

      // Vendors Table
      const venTbody = document.getElementById('vendorsTable');
      venTbody.innerHTML = vendors.map(v => `
        <tr>
          <td><code>${v.vendorCode}</code></td>
          <td><strong>${v.companyName}</strong></td>
          <td>${v.contactPerson || 'N/A'}</td>
          <td>${v.email}</td>
          <td>${v.paymentTerms || 'Net 30'}</td>
          <td>⭐ ${v.rating || 5.0}</td>
          <td><strong style="color:#b91c1c;">${this.formatCurrency(v.balanceDue)}</strong></td>
        </tr>
      `).join('');

      // Populate PO Modal Selects
      const poNum = 'PO-' + new Date().getFullYear() + '-' + String(orders.length + 1).padStart(3, '0');
      document.getElementById('poNumberInput').value = poNum;

      const venSelect = document.getElementById('selectPoVendor');
      if (venSelect) {
        venSelect.innerHTML = vendors.map(v => `<option value="${v.id}">${v.companyName} (${v.vendorCode})</option>`).join('');
      }

      const prodSelect = document.getElementById('selectPoProduct');
      if (prodSelect) {
        // filter raw materials & components for procurement
        prodSelect.innerHTML = products.map(p => `<option value="${p.id}">${p.name} [${p.sku}] - Cost: $${p.unitCost}</option>`).join('');
      }

    } catch (e) {
      console.error(e);
      this.showToast('Failed to load procurement data', 'error');
    }
  },

  async approvePO(id) {
    try {
      const res = await fetch(`/api/procurement/orders/${id}/approve`, { method: 'POST' });
      if (!res.ok) throw new Error(await res.text());
      this.showToast('Purchase Order Approved!', 'success');
      this.loadProcurement();
    } catch (e) {
      this.showToast(e.message || 'Approval failed', 'error');
    }
  },

  async receivePO(id) {
    try {
      const res = await fetch(`/api/procurement/orders/${id}/receive`, { method: 'POST' });
      if (!res.ok) throw new Error(await res.text());
      this.showToast('Goods Received! Inventory increased, AP bill generated, and GL posted.', 'success');
      this.loadProcurement();
    } catch (e) {
      this.showToast(e.message || 'Goods receipt failed', 'error');
    }
  },

  async handleNewPO(e) {
    e.preventDefault();
    const form = e.target;
    const po = {
      poNumber: form.poNumber.value,
      vendor: { id: parseInt(form.vendorId.value) },
      orderDate: new Date().toISOString().split('T')[0],
      expectedDeliveryDate: new Date(Date.now() + 7 * 86400000).toISOString().split('T')[0],
      notes: form.notes.value,
      status: 'APPROVED', // auto approve for speed
      items: [
        {
          product: { id: parseInt(form.productId.value) },
          quantity: parseInt(form.quantity.value)
        }
      ]
    };

    // get product unit cost
    const prodRes = await fetch(`/api/inventory/products`);
    const prods = await prodRes.json();
    const selectedProd = prods.find(p => p.id === po.items[0].product.id);
    po.items[0].unitCost = selectedProd ? selectedProd.unitCost : 50;

    try {
      const res = await fetch('/api/procurement/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(po)
      });
      if (!res.ok) throw new Error(await res.text());
      this.closeModal('modalNewPO');
      this.showToast(`Purchase Order ${po.poNumber} created & ready for receipt!`, 'success');
      this.loadProcurement();
    } catch (err) {
      this.showToast(err.message || 'Error creating PO', 'error');
    }
  },

  // ===============================================
  // 5. SALES
  // ===============================================
  async loadSales() {
    try {
      const [soRes, custRes, prodRes] = await Promise.all([
        fetch('/api/sales/orders'),
        fetch('/api/sales/customers'),
        fetch('/api/inventory/products')
      ]);

      const orders = await soRes.json();
      const customers = await custRes.json();
      const products = await prodRes.json();

      // Orders Table
      const soTbody = document.getElementById('salesOrdersTable');
      soTbody.innerHTML = orders.map(so => {
        const canConfirm = so.status === 'PENDING';
        const canFulfill = so.status === 'CONFIRMED';
        const itemsSummary = (so.items || []).map(i => `${i.product ? i.product.name : 'Item'} (x${i.quantity})`).join(', ');

        return `
          <tr>
            <td><code>${so.soNumber}</code></td>
            <td><strong>${so.customer ? so.customer.name : 'N/A'}</strong></td>
            <td>${so.orderDate}</td>
            <td><small>${itemsSummary || 'No items'}</small></td>
            <td><strong>${this.formatCurrency(so.totalAmount)}</strong></td>
            <td><span class="badge ${this.getSoBadge(so.status)}">${so.status}</span></td>
            <td>
              ${canConfirm ? `<button class="btn btn-sm btn-primary" onclick="app.confirmSO(${so.id})">Confirm</button>` : ''}
              ${canFulfill ? `<button class="btn btn-sm btn-success" onclick="app.fulfillSO(${so.id})">🚀 Ship & Invoice</button>` : ''}
              ${so.status === 'SHIPPED' ? '<span style="color:#059669; font-size:0.8rem;">✓ Dispatched & Invoiced</span>' : ''}
            </td>
          </tr>
        `;
      }).join('');

      // Customers Table
      const custTbody = document.getElementById('customersTable');
      custTbody.innerHTML = customers.map(c => `
        <tr>
          <td><code>${c.customerCode}</code></td>
          <td><strong>${c.name}</strong></td>
          <td>${c.contactPerson || 'N/A'}</td>
          <td>${c.email}</td>
          <td>${this.formatCurrency(c.creditLimit)}</td>
          <td><strong style="color:#059669;">${this.formatCurrency(c.outstandingBalance)}</strong></td>
        </tr>
      `).join('');

      // Populate SO Modal
      const soNum = 'SO-' + new Date().getFullYear() + '-' + String(orders.length + 1).padStart(3, '0');
      document.getElementById('soNumberInput').value = soNum;

      const custSelect = document.getElementById('selectSoCustomer');
      if (custSelect) {
        custSelect.innerHTML = customers.map(c => `<option value="${c.id}">${c.name} (${c.customerCode})</option>`).join('');
      }

      const prodSelect = document.getElementById('selectSoProduct');
      if (prodSelect) {
        // filter finished goods
        const fg = products.filter(p => p.category === 'FINISHED_GOOD');
        prodSelect.innerHTML = fg.map(p => `<option value="${p.id}">${p.name} - Price: $${p.sellingPrice} (Stock: ${p.currentStock})</option>`).join('');
      }

    } catch (e) {
      console.error(e);
      this.showToast('Failed to load sales data', 'error');
    }
  },

  async confirmSO(id) {
    try {
      const res = await fetch(`/api/sales/orders/${id}/confirm`, { method: 'POST' });
      if (!res.ok) throw new Error(await res.text());
      this.showToast('Sales Order Confirmed!', 'success');
      this.loadSales();
    } catch (e) {
      this.showToast(e.message || 'Confirmation failed', 'error');
    }
  },

  async fulfillSO(id) {
    try {
      const res = await fetch(`/api/sales/orders/${id}/fulfill`, { method: 'POST' });
      if (!res.ok) throw new Error(await res.text());
      this.showToast('Order Shipped! Stock dispatched, AR Invoice generated, and revenue recognized!', 'success');
      this.loadSales();
    } catch (e) {
      this.showToast(e.message || 'Fulfillment failed', 'error');
    }
  },

  async handleNewSO(e) {
    e.preventDefault();
    const form = e.target;
    const so = {
      soNumber: form.soNumber.value,
      customer: { id: parseInt(form.customerId.value) },
      orderDate: new Date().toISOString().split('T')[0],
      deliveryDate: new Date(Date.now() + 5 * 86400000).toISOString().split('T')[0],
      notes: form.notes.value,
      status: 'CONFIRMED', // auto confirm for quick testing
      items: [
        {
          product: { id: parseInt(form.productId.value) },
          quantity: parseInt(form.quantity.value)
        }
      ]
    };

    const prodRes = await fetch(`/api/inventory/products`);
    const prods = await prodRes.json();
    const selectedProd = prods.find(p => p.id === so.items[0].product.id);
    so.items[0].unitPrice = selectedProd ? selectedProd.sellingPrice : 500;

    try {
      const res = await fetch('/api/sales/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(so)
      });
      if (!res.ok) throw new Error(await res.text());
      this.closeModal('modalNewSO');
      this.showToast(`Sales Order ${so.soNumber} created & confirmed! Ready to Ship.`, 'success');
      this.loadSales();
    } catch (err) {
      this.showToast(err.message || 'Error creating SO', 'error');
    }
  },

  // ===============================================
  // 6. MANUFACTURING
  // ===============================================
  async loadManufacturing() {
    try {
      const [woRes, bomRes] = await Promise.all([
        fetch('/api/manufacturing/work-orders'),
        fetch('/api/manufacturing/boms')
      ]);

      const workOrders = await woRes.json();
      const boms = await bomRes.json();

      // Work Orders Table
      const woTbody = document.getElementById('workOrdersTable');
      woTbody.innerHTML = workOrders.map(wo => {
        const canStart = wo.status === 'PLANNED';
        const canComplete = wo.status === 'IN_PROGRESS';
        const finName = wo.billOfMaterial && wo.billOfMaterial.finishedProduct ?
                wo.billOfMaterial.finishedProduct.name : 'Unknown Product';

        return `
          <tr>
            <td><code>${wo.woNumber}</code></td>
            <td><strong>${finName}</strong></td>
            <td>${wo.targetQuantity} units</td>
            <td>${wo.completedQuantity || 0} units</td>
            <td>${wo.dueDate || 'N/A'}</td>
            <td>${wo.supervisor || 'N/A'}</td>
            <td><span class="badge ${this.getWoBadge(wo.status)}">${wo.status}</span></td>
            <td>
              ${canStart ? `<button class="btn btn-sm btn-primary" onclick="app.startWO(${wo.id})">▶ Start Run</button>` : ''}
              ${canComplete ? `<button class="btn btn-sm btn-success" onclick="app.completeWO(${wo.id})">⚙️ Complete Run</button>` : ''}
              ${wo.status === 'COMPLETED' ? '<span style="color:#059669; font-size:0.8rem;">✓ Finished & Stocked</span>' : ''}
            </td>
          </tr>
        `;
      }).join('');

      // BOMs Table
      const bomTbody = document.getElementById('bomsTable');
      bomTbody.innerHTML = boms.map(b => {
        const compList = (b.components || []).map(c =>
                `• ${c.componentProduct ? c.componentProduct.name : 'Part'} (x${c.requiredQuantity})`
        ).join('<br>');

        return `
          <tr>
            <td><code>${b.bomNumber}</code></td>
            <td><strong>${b.finishedProduct ? b.finishedProduct.name : 'N/A'}</strong></td>
            <td>${b.description || ''}</td>
            <td><small>${compList || 'None'}</small></td>
            <td>${this.formatCurrency(b.laborCost)}</td>
            <td>${b.standardProductionHours || 1} hrs</td>
          </tr>
        `;
      }).join('');

      // Populate WO Modal
      const woNum = 'WO-' + new Date().getFullYear() + '-' + String(workOrders.length + 1).padStart(3, '0');
      document.getElementById('woNumberInput').value = woNum;

      const bomSelect = document.getElementById('selectWoBom');
      if (bomSelect) {
        bomSelect.innerHTML = boms.map(b => `
          <option value="${b.id}">${b.bomNumber} — ${b.finishedProduct ? b.finishedProduct.name : 'Product'}</option>
        `).join('');
      }

    } catch (e) {
      console.error(e);
      this.showToast('Failed to load manufacturing data', 'error');
    }
  },

  async startWO(id) {
    try {
      const res = await fetch(`/api/manufacturing/work-orders/${id}/start`, { method: 'POST' });
      if (!res.ok) throw new Error(await res.text());
      this.showToast('Production Run Started! Work Order is now IN_PROGRESS.', 'info');
      this.loadManufacturing();
    } catch (e) {
      this.showToast(e.message || 'Failed to start WO', 'error');
    }
  },

  async completeWO(id) {
    try {
      const res = await fetch(`/api/manufacturing/work-orders/${id}/complete`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({})
      });
      if (!res.ok) throw new Error(await res.text());
      this.showToast('Production Run Completed! Raw materials consumed, Finished Goods added to stock, and GL entries posted.', 'success');
      this.loadManufacturing();
    } catch (e) {
      this.showToast(e.message || 'Failed to complete production run', 'error');
    }
  },

  async handleNewWO(e) {
    e.preventDefault();
    const form = e.target;
    const wo = {
      woNumber: form.woNumber.value,
      billOfMaterial: { id: parseInt(form.bomId.value) },
      targetQuantity: parseInt(form.targetQuantity.value),
      supervisor: form.supervisor.value,
      notes: form.notes.value,
      startDate: new Date().toISOString().split('T')[0],
      dueDate: new Date(Date.now() + 7 * 86400000).toISOString().split('T')[0],
      status: 'IN_PROGRESS' // start immediately
    };

    try {
      const res = await fetch('/api/manufacturing/work-orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(wo)
      });
      if (!res.ok) throw new Error(await res.text());
      this.closeModal('modalNewWO');
      this.showToast(`Work Order ${wo.woNumber} created & ready for execution!`, 'success');
      this.loadManufacturing();
    } catch (err) {
      this.showToast(err.message || 'Error creating WO', 'error');
    }
  },

  // ===============================================
  // 7. INVENTORY
  // ===============================================
  async loadInventory() {
    try {
      const [prodRes, moveRes, lowRes] = await Promise.all([
        fetch('/api/inventory/products'),
        fetch('/api/inventory/movements'),
        fetch('/api/inventory/low-stock')
      ]);

      const products = await prodRes.json();
      const movements = await moveRes.json();
      const lowStock = await lowRes.json();

      const lowBadge = document.getElementById('lowStockWarningBadge');
      if (lowBadge) {
        lowBadge.style.display = lowStock.length > 0 ? 'inline-flex' : 'none';
        lowBadge.textContent = `⚠️ ${lowStock.length} Low Stock Alerts Present`;
      }

      // Products Table
      const prodTbody = document.getElementById('productsTable');
      prodTbody.innerHTML = products.map(p => {
        const isLow = p.currentStock <= p.minStockLevel;
        const totalVal = (p.currentStock || 0) * (p.unitCost || 0);
        return `
          <tr>
            <td><code>${p.sku}</code></td>
            <td><strong>${p.name}</strong></td>
            <td><span class="badge ${this.getProductCatBadge(p.category)}">${p.category}</span></td>
            <td><strong style="color:${isLow ? '#b91c1c' : '#1e293b'}">${p.currentStock} ${p.unitOfMeasure}</strong></td>
            <td>${p.minStockLevel} ${p.unitOfMeasure}</td>
            <td>${this.formatCurrency(p.unitCost)}</td>
            <td>${p.sellingPrice > 0 ? this.formatCurrency(p.sellingPrice) : '—'}</td>
            <td><strong>${this.formatCurrency(totalVal)}</strong></td>
            <td>${p.warehouseLocation || 'Warehouse 1'}</td>
            <td>
              ${isLow ? '<span class="badge badge-danger">Low Stock</span>' : '<span class="badge badge-success">Optimal</span>'}
            </td>
          </tr>
        `;
      }).join('');

      // Stock Movements Table
      const moveTbody = document.getElementById('stockMovementsTable');
      moveTbody.innerHTML = movements.map(m => {
        const isPositive = m.quantity > 0;
        return `
          <tr>
            <td><small>${this.formatDateTime(m.timestamp)}</small></td>
            <td><strong>${m.product ? m.product.name : 'Product'}</strong> [${m.product ? m.product.sku : ''}]</td>
            <td><span class="badge ${this.getMovementBadge(m.movementType)}">${m.movementType}</span></td>
            <td><strong style="color:${isPositive ? '#059669' : '#b91c1c'}">${isPositive ? '+' : ''}${m.quantity}</strong></td>
            <td><strong>${m.balanceAfter}</strong></td>
            <td><code>${m.referenceNumber || 'N/A'}</code></td>
            <td><small>${m.notes || ''}</small></td>
          </tr>
        `;
      }).join('');

    } catch (e) {
      console.error(e);
      this.showToast('Failed to load inventory data', 'error');
    }
  },

  async handleNewProduct(e) {
    e.preventDefault();
    const form = e.target;
    const prod = {
      sku: form.sku.value,
      name: form.name.value,
      category: form.category.value,
      unitOfMeasure: form.unitOfMeasure.value,
      unitCost: parseFloat(form.unitCost.value),
      sellingPrice: parseFloat(form.sellingPrice.value || 0),
      currentStock: parseInt(form.currentStock.value),
      minStockLevel: parseInt(form.minStockLevel.value),
      warehouseLocation: form.warehouseLocation.value
    };

    try {
      const res = await fetch('/api/inventory/products', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(prod)
      });
      if (!res.ok) throw new Error(await res.text());
      this.closeModal('modalNewProduct');
      this.showToast(`Product ${prod.name} saved!`, 'success');
      form.reset();
      this.loadInventory();
    } catch (err) {
      this.showToast(err.message || 'Error saving product', 'error');
    }
  },

  // ===============================================
  // 8. AUDIT LOGS
  // ===============================================
  async loadAudit() {
    try {
      const res = await fetch('/api/audit');
      const audits = await res.json();
      const tbody = document.getElementById('fullAuditTable');
      tbody.innerHTML = audits.map(a => `
        <tr>
          <td><small>${this.formatDateTime(a.timestamp)}</small></td>
          <td><span class="badge ${this.getModuleBadge(a.module)}">${a.module}</span></td>
          <td><strong>${a.action}</strong></td>
          <td><code>${a.entityId || 'N/A'}</code></td>
          <td>${a.performedBy || 'System'}</td>
          <td>${a.details || ''}</td>
        </tr>
      `).join('');
    } catch (e) {
      console.error(e);
    }
  },

  // ===============================================
  // HELPERS
  // ===============================================
  formatCurrency(val) {
    if (val === null || val === undefined) return '$0.00';
    return '$' + Number(val).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  },

  formatDateTime(iso) {
    if (!iso) return '';
    const d = new Date(iso);
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }) + ' ' + d.toLocaleDateString();
  },

  getModuleBadge(mod) {
    switch (mod) {
      case 'FINANCE': return 'badge-success';
      case 'HR': return 'badge-info';
      case 'PROCUREMENT': return 'badge-warning';
      case 'SALES': return 'badge-primary';
      case 'MANUFACTURING': return 'badge-secondary';
      case 'INVENTORY': return 'badge-info';
      default: return 'badge-secondary';
    }
  },

  getAccountBadge(type) {
    switch (type) {
      case 'ASSET': return 'badge-info';
      case 'LIABILITY': return 'badge-warning';
      case 'EQUITY': return 'badge-secondary';
      case 'REVENUE': return 'badge-success';
      case 'EXPENSE': return 'badge-danger';
      default: return 'badge-secondary';
    }
  },

  getPoBadge(status) {
    switch (status) {
      case 'APPROVED': return 'badge-info';
      case 'RECEIVED': return 'badge-success';
      case 'CANCELLED': return 'badge-danger';
      default: return 'badge-warning';
    }
  },

  getSoBadge(status) {
    switch (status) {
      case 'CONFIRMED': return 'badge-info';
      case 'SHIPPED': return 'badge-success';
      case 'CANCELLED': return 'badge-danger';
      default: return 'badge-warning';
    }
  },

  getWoBadge(status) {
    switch (status) {
      case 'IN_PROGRESS': return 'badge-warning';
      case 'COMPLETED': return 'badge-success';
      case 'CANCELLED': return 'badge-danger';
      default: return 'badge-secondary';
    }
  },

  getProductCatBadge(cat) {
    switch (cat) {
      case 'FINISHED_GOOD': return 'badge-success';
      case 'RAW_MATERIAL': return 'badge-info';
      default: return 'badge-secondary';
    }
  },

  getMovementBadge(type) {
    switch (type) {
      case 'INWARD_PURCHASE':
      case 'PRODUCTION_OUTPUT':
        return 'badge-success';
      case 'OUTWARD_SALES':
      case 'PRODUCTION_CONSUMPTION':
        return 'badge-danger';
      default:
        return 'badge-info';
    }
  },

  // ===============================================
  // 10. AUTHENTICATION & SESSION MANAGEMENT
  // ===============================================
  async verifySession(token) {
    try {
      const res = await fetch(`/api/auth/me?token=${encodeURIComponent(token)}`);
      if (res.ok) {
        const user = await res.json();
        this.currentUser = user;
        this.authToken = token;
        this.updateHeaderUserProfile();
        this.enforceRbac();
        return true;
      }
    } catch (e) {
      console.error('Session verify failed', e);
    }
    this.clearSession();
    return false;
  },

  async handleLoginSubmit(e) {
    if (e) e.preventDefault();
    const uInput = document.getElementById('loginUsername');
    const pInput = document.getElementById('loginPassword');
    const rememberBox = document.getElementById('loginRememberMe');
    const errorBox = document.getElementById('loginErrorMessage');
    if (errorBox) errorBox.style.display = 'none';

    const username = uInput ? uInput.value.trim() : '';
    const password = pInput ? pInput.value.trim() : '';
    const rememberMe = rememberBox ? rememberBox.checked : true;

    if (!username || !password) {
      if (errorBox) {
        errorBox.textContent = 'Please provide both username and password.';
        errorBox.style.display = 'block';
      }
      return;
    }

    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.error || 'Authentication failed. Please check credentials.');
      }

      this.currentUser = data.user;
      this.authToken = data.token;
      if (rememberMe) {
        localStorage.setItem('omni_token', data.token);
      } else {
        sessionStorage.setItem('omni_token', data.token);
      }

      this.hideLogin();
      this.updateHeaderUserProfile();
      this.enforceRbac();
      this.showToast(`Welcome back, ${data.user.fullName} (${data.user.roleName})!`, 'success');
      this.refreshCurrentTab();
    } catch (err) {
      if (errorBox) {
        errorBox.textContent = err.message || 'Authentication error';
        errorBox.style.display = 'block';
      } else {
        this.showToast(err.message, 'error');
      }
    }
  },

  quickLogin(username, password) {
    const uInput = document.getElementById('loginUsername');
    const pInput = document.getElementById('loginPassword');
    if (uInput) uInput.value = username;
    if (pInput) pInput.value = password;
    return this.handleLoginSubmit();
  },

  toggleLoginPasswordVisibility() {
    const input = document.getElementById('loginPassword');
    const btn = document.getElementById('btnTogglePassword');
    if (!input) return;
    if (input.type === 'password') {
      input.type = 'text';
      if (btn) btn.textContent = 'Hide';
    } else {
      input.type = 'password';
      if (btn) btn.textContent = 'Show';
    }
  },

  async handleLogout(e) {
    if (e) e.stopPropagation();
    if (confirm('Are you sure you want to sign out of PHANTOM-ERP?')) {
      try {
        if (this.authToken) {
          await fetch('/api/auth/logout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ token: this.authToken })
          });
        }
      } catch (err) {
        console.warn('Logout API call failed', err);
      }
      this.clearSession();
      this.showLogin();
      this.showToast('You have been safely signed out.', 'info');
    }
  },

  clearSession() {
    this.currentUser = null;
    this.authToken = null;
    localStorage.removeItem('omni_token');
    sessionStorage.removeItem('omni_token');
    const uInput = document.getElementById('loginUsername');
    const pInput = document.getElementById('loginPassword');
    if (uInput) uInput.value = '';
    if (pInput) pInput.value = '';
  },

  updateHeaderUserProfile() {
    if (!this.currentUser) return;
    const nameEl = document.getElementById('headerUserName');
    const roleEl = document.getElementById('headerUserRole');
    const avatarEl = document.getElementById('headerUserAvatar');
    if (nameEl) nameEl.textContent = this.currentUser.fullName;
    if (roleEl) roleEl.textContent = this.currentUser.roleName;
    if (avatarEl) avatarEl.textContent = this.currentUser.avatar || '👨‍💼';
  },

  enforceRbac() {
    if (!this.currentUser) return;
    const navAdmin = document.getElementById('navItemAdmin');
    const isSuperOrAdmin = this.currentUser.roleCode === 'ROLE_SUPER_ADMIN' ||
                           (this.currentUser.permissions && this.currentUser.permissions.includes('MODULE_ADMIN'));
    if (navAdmin) {
      navAdmin.style.display = isSuperOrAdmin ? '' : 'none';
    }
  },

  showLogin() {
    const overlay = document.getElementById('loginOverlay');
    if (overlay) overlay.style.display = 'flex';
  },

  hideLogin() {
    const overlay = document.getElementById('loginOverlay');
    if (overlay) overlay.style.display = 'none';
  },

  // ===============================================
  // 11. ROLES & ADMINISTRATION MODULE
  // ===============================================
  currentAdminSubTab: 'users',
  adminUsers: [],
  adminRoles: [],

  async loadAdmin() {
    try {
      const [usersRes, rolesRes, statsRes, auditRes] = await Promise.all([
        fetch('/api/admin/users'),
        fetch('/api/admin/roles'),
        fetch('/api/admin/security-overview'),
        fetch('/api/admin/audit-logs')
      ]);

      this.adminUsers = await usersRes.json();
      this.adminRoles = await rolesRes.json();
      const stats = await statsRes.json();
      const auditLogs = await auditRes.json();

      // Render KPIs
      const kpiTotalUsersEl = document.getElementById('kpiAdminTotalUsers');
      if (kpiTotalUsersEl) kpiTotalUsersEl.textContent = stats.totalUsers || this.adminUsers.length;

      const kpiRolesEl = document.getElementById('kpiAdminRoles');
      if (kpiRolesEl) kpiRolesEl.textContent = stats.totalRoles || this.adminRoles.length;

      const kpiPrivilegedEl = document.getElementById('kpiAdminPrivileged');
      if (kpiPrivilegedEl) kpiPrivilegedEl.textContent = stats.privilegedAdmins || 1;

      const kpiScoreEl = document.getElementById('kpiAdminSecurityScore');
      if (kpiScoreEl) kpiScoreEl.textContent = `${stats.securityScore || 98}%`;

      const kpiSessionsEl = document.getElementById('kpiAdminSessions');
      if (kpiSessionsEl) kpiSessionsEl.textContent = stats.activeSessionCount || 1;

      const pillUserCount = document.getElementById('pillAdminUserCount');
      if (pillUserCount) pillUserCount.textContent = this.adminUsers.length;

      const pillRoleCount = document.getElementById('pillAdminRoleCount');
      if (pillRoleCount) pillRoleCount.textContent = this.adminRoles.length;

      const badgeAdmin = document.getElementById('badgeAdminUsers');
      if (badgeAdmin) badgeAdmin.textContent = this.adminUsers.length;

      // Populate Role Filter dropdown
      const filterRole = document.getElementById('adminFilterRole');
      if (filterRole) {
        const currentVal = filterRole.value;
        filterRole.innerHTML = '<option value="">All Roles</option>' +
          this.adminRoles.map(r => `<option value="${r.roleCode}">${r.name}</option>`).join('');
        filterRole.value = currentVal;
      }

      this.renderAdminUsers();
      this.renderAdminRoles();
      this.renderAdminAudit(auditLogs);
    } catch (err) {
      console.error('Error loading Admin module', err);
      this.showToast('Failed to load administration data', 'error');
    }
  },

  switchAdminSubTab(subTab) {
    this.currentAdminSubTab = subTab;
    document.querySelectorAll('#tab-admin .sub-tab-btn').forEach(btn => {
      btn.classList.toggle('active', btn.id === `adminSubTab-${subTab}`);
    });

    const panels = ['users', 'roles', 'audit', 'policies'];
    panels.forEach(p => {
      const el = document.getElementById(`adminCard-${p}`);
      if (el) el.style.display = (p === subTab) ? '' : 'none';
    });
  },

  filterAdminUsers() {
    const search = (document.getElementById('adminSearchUsers')?.value || '').toLowerCase();
    const roleFilter = document.getElementById('adminFilterRole')?.value || '';

    const filtered = (this.adminUsers || []).filter(u => {
      const matchSearch = !search ||
        (u.fullName && u.fullName.toLowerCase().includes(search)) ||
        (u.username && u.username.toLowerCase().includes(search)) ||
        (u.email && u.email.toLowerCase().includes(search));
      const matchRole = !roleFilter || (u.role && u.role.roleCode === roleFilter);
      return matchSearch && matchRole;
    });

    this.renderAdminUsers(filtered);
  },

  renderAdminUsers(list = this.adminUsers) {
    const tbody = document.getElementById('adminUsersTableBody');
    if (!tbody) return;

    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:#64748b; padding:24px;">No user accounts match filter.</td></tr>';
      return;
    }

    tbody.innerHTML = list.map(u => {
      const deptName = u.department ? u.department.name : 'Executive / Central';
      const roleName = u.role ? u.role.name : 'Staff';
      const roleCode = u.role ? u.role.roleCode : '';
      const isSuper = roleCode === 'ROLE_SUPER_ADMIN';
      const roleBadgeClass = isSuper ? 'badge-primary' : 'badge-secondary';
      const statusBadge = u.status === 'ACTIVE'
        ? '<span class="badge badge-success">ACTIVE</span>'
        : '<span class="badge badge-danger">SUSPENDED</span>';

      const lastLoginStr = u.lastLogin ? new Date(u.lastLogin).toLocaleString() : 'Never logged in';

      return `
        <tr>
          <td>
            <div style="display:flex; align-items:center; gap:10px;">
              <span style="font-size:1.4rem;">${u.avatar || '👤'}</span>
              <div>
                <strong style="color:#0f172a; font-size:0.92rem;">${u.fullName}</strong>
                <div style="font-size:0.72rem; color:#64748b;">ID: #${u.id}</div>
              </div>
            </div>
          </td>
          <td>
            <div><code style="font-weight:700; color:#0284c7;">${u.username}</code></div>
            <div style="font-size:0.74rem; color:#64748b;">${u.email}</div>
          </td>
          <td>
            <div><span class="badge badge-info">${deptName}</span></div>
            <div style="font-size:0.75rem; color:#64748b; margin-top:2px;">${u.jobTitle || 'Associate'}</div>
          </td>
          <td>
            <span class="badge ${roleBadgeClass}" style="font-weight:600;">${roleName}</span>
          </td>
          <td>${statusBadge}</td>
          <td><span style="font-size:0.75rem; color:#64748b;">${lastLoginStr}</span></td>
          <td>
            <div style="display:flex; gap:6px;">
              <button class="btn btn-sm btn-secondary" title="Edit User" onclick="app.openEditUserModal(${u.id})">✏️</button>
              ${u.username !== 'admin' ? `
                <button class="btn btn-sm ${u.status === 'ACTIVE' ? 'btn-danger' : 'btn-success'}"
                        title="${u.status === 'ACTIVE' ? 'Suspend Account' : 'Activate Account'}"
                        onclick="app.toggleUserStatus(${u.id})">
                  ${u.status === 'ACTIVE' ? '🔒' : '🔓'}
                </button>
                <button class="btn btn-sm btn-danger" title="Delete Account" onclick="app.deleteUser(${u.id}, '${u.username}')">🗑️</button>
              ` : `
                <span class="badge badge-secondary" style="font-size:0.7rem;">Protected</span>
              `}
            </div>
          </td>
        </tr>
      `;
    }).join('');
  },

  renderAdminRoles() {
    const grid = document.getElementById('adminRolesGrid');
    if (!grid || !this.adminRoles) return;

    grid.innerHTML = this.adminRoles.map(r => {
      const perms = Array.from(r.permissions || []);
      const userCount = (this.adminUsers || []).filter(u => u.role && u.role.id === r.id).length;

      return `
        <div class="role-card">
          <div>
            <div class="role-card-header">
              <div>
                <div class="role-card-title">${r.name}</div>
                <div class="role-card-code"><code>${r.roleCode}</code></div>
              </div>
              <span class="badge ${r.isSystemRole ? 'badge-primary' : 'badge-secondary'}">
                ${r.isSystemRole ? 'System Built-in' : 'Custom'}
              </span>
            </div>
            <p style="font-size:0.8rem; color:#64748b; line-height:1.4; margin-bottom:10px;">
              ${r.description || 'Enterprise role specification.'}
            </p>
            <div style="font-size:0.72rem; font-weight:700; color:#475569; text-transform:uppercase; letter-spacing:0.04em;">
              Assigned Permissions (${perms.length}):
            </div>
            <div class="permissions-tag-wrap">
              ${perms.slice(0, 8).map(p => `<span class="permission-tag active">${p}</span>`).join('')}
              ${perms.length > 8 ? `<span class="permission-tag">+${perms.length - 8} more</span>` : ''}
            </div>
          </div>
          <div style="display:flex; justify-content:space-between; align-items:center; border-top:1px solid #f1f5f9; padding-top:12px; margin-top:10px;">
            <span style="font-size:0.78rem; color:#64748b;"><strong>${userCount}</strong> assigned users</span>
            <span class="badge badge-success" style="font-size:0.7rem;">● Active Policy</span>
          </div>
        </div>
      `;
    }).join('');
  },

  renderAdminAudit(logs) {
    const tbody = document.getElementById('adminAuditTableBody');
    if (!tbody) return;

    if (!logs || logs.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:#64748b; padding:20px;">No recent audit records.</td></tr>';
      return;
    }

    tbody.innerHTML = logs.map(l => {
      const timeStr = l.timestamp ? new Date(l.timestamp).toLocaleString() : 'N/A';
      return `
        <tr>
          <td><span style="font-size:0.75rem; color:#64748b;">${timeStr}</span></td>
          <td><span class="badge badge-info" style="font-weight:700;">${l.action}</span></td>
          <td><strong style="color:#0f172a;">${l.performedBy || 'System'}</strong></td>
          <td><code>${l.entityId || 'N/A'}</code></td>
          <td><span style="font-size:0.82rem; color:#334155;">${l.details || ''}</span></td>
        </tr>
      `;
    }).join('');
  },

  // Modals & Handlers for User & Role Admin
  openNewUserModal() {
    const roleSelect = document.getElementById('newUserRoleSelect');
    if (roleSelect && this.adminRoles) {
      roleSelect.innerHTML = this.adminRoles.map(r => `<option value="${r.id}">${r.name} (${r.roleCode})</option>`).join('');
    }
    const deptSelect = document.getElementById('newUserDeptSelect');
    if (deptSelect && this.departments) {
      deptSelect.innerHTML = this.departments.map(d => `<option value="${d.id}">${d.name}</option>`).join('');
    }
    this.openModal('modalNewUser');
  },

  async handleNewUser(e) {
    e.preventDefault();
    const form = e.target;
    const userData = {
      username: form.username.value,
      password: form.password.value,
      fullName: form.fullName.value,
      email: form.email.value,
      jobTitle: form.jobTitle.value,
      avatar: form.avatar.value,
      roleId: parseInt(form.roleId.value),
      departmentId: form.departmentId.value ? parseInt(form.departmentId.value) : null
    };

    try {
      const res = await fetch('/api/admin/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userData)
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Failed to create user');

      this.closeModal('modalNewUser');
      form.reset();
      this.showToast(`User account '${data.fullName}' created successfully!`, 'success');
      this.loadAdmin();
    } catch (err) {
      this.showToast(err.message, 'error');
    }
  },

  openEditUserModal(id) {
    const user = (this.adminUsers || []).find(u => u.id === id);
    if (!user) return;

    document.getElementById('editUserId').value = user.id;
    document.getElementById('editUserUsername').value = user.username;
    document.getElementById('editUserFullName').value = user.fullName;
    document.getElementById('editUserEmail').value = user.email;
    document.getElementById('editUserJobTitle').value = user.jobTitle || '';
    document.getElementById('editUserStatus').value = user.status;

    const roleSelect = document.getElementById('editUserRoleSelect');
    if (roleSelect && this.adminRoles) {
      roleSelect.innerHTML = this.adminRoles.map(r => `<option value="${r.id}" ${user.role && user.role.id === r.id ? 'selected' : ''}>${r.name} (${r.roleCode})</option>`).join('');
    }
    const deptSelect = document.getElementById('editUserDeptSelect');
    if (deptSelect && this.departments) {
      deptSelect.innerHTML = this.departments.map(d => `<option value="${d.id}" ${user.department && user.department.id === d.id ? 'selected' : ''}>${d.name}</option>`).join('');
    }

    this.openModal('modalEditUser');
  },

  async handleUpdateUser(e) {
    e.preventDefault();
    const form = e.target;
    const userId = form.userId.value;
    const updateData = {
      fullName: form.fullName.value,
      email: form.email.value,
      jobTitle: form.jobTitle.value,
      status: form.status.value,
      roleId: parseInt(form.roleId.value),
      departmentId: form.departmentId.value ? parseInt(form.departmentId.value) : null
    };
    if (form.password.value && form.password.value.trim().length > 0) {
      updateData.password = form.password.value.trim();
    }

    try {
      const res = await fetch(`/api/admin/users/${userId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updateData)
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Failed to update user');

      this.closeModal('modalEditUser');
      this.showToast(`User '${data.fullName}' updated successfully!`, 'success');
      this.loadAdmin();
    } catch (err) {
      this.showToast(err.message, 'error');
    }
  },

  async toggleUserStatus(id) {
    try {
      const res = await fetch(`/api/admin/users/${id}/toggle-status`, { method: 'POST' });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Failed to toggle status');
      this.showToast(`Account status updated to ${data.status}!`, 'success');
      this.loadAdmin();
    } catch (err) {
      this.showToast(err.message, 'error');
    }
  },

  async deleteUser(id, username) {
    if (!confirm(`Are you sure you want to permanently delete user account '${username}'?`)) return;
    try {
      const res = await fetch(`/api/admin/users/${id}`, { method: 'DELETE' });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Failed to delete user');
      this.showToast(`User '${username}' deleted.`, 'info');
      this.loadAdmin();
    } catch (err) {
      this.showToast(err.message, 'error');
    }
  },

  openNewRoleModal() {
    this.openModal('modalNewRole');
  },

  async handleNewRole(e) {
    e.preventDefault();
    const form = e.target;
    const selectedPerms = Array.from(form.querySelectorAll('input[name="rolePerms"]:checked'))
      .map(cb => cb.value);

    const roleData = {
      roleCode: form.roleCode.value.trim().toUpperCase(),
      name: form.name.value.trim(),
      description: form.description.value.trim(),
      isSystemRole: false,
      permissions: selectedPerms
    };

    try {
      const res = await fetch('/api/admin/roles', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(roleData)
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Failed to create role');

      this.closeModal('modalNewRole');
      form.reset();
      this.showToast(`Role '${data.name}' defined successfully!`, 'success');
      this.loadAdmin();
    } catch (err) {
      this.showToast(err.message, 'error');
    }
  },

  openModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.add('active');
  },

  closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.remove('active');
  },

  openQuickActionModal() {
    this.openModal('modalQuickAction');
  },

  showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    const icon = type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️';
    toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  }
};

window.addEventListener('DOMContentLoaded', () => {
  app.init();
});
