# PHANTOM-ERP — Enterprise Resource Planning (ERP) Web Application

A full-featured, enterprise-grade **Enterprise Resource Planning (ERP)** web application built in **Java 21** using **Spring Boot 3.3.4**, **Spring Data JPA**, a **Central Unified Relational Database (Single Source of Truth)**, and a modern, responsive Single Page Application (SPA) web portal.

---

## 🌟 Key Architecture & Capabilities

### 1. Central Database (Single Source of Truth)
Connects all organizational departments through a single relational database schema:
* Eliminates operational data silos between Finance, HR, Supply Chain, Sales, and Factory Floor.
* Maintains relational integrity with foreign keys, transactional boundaries, and double-entry reconciliation.
* Development: Built-in in-memory / persistent H2 database engine with web console at `/h2-console`.
* Production: Instantly switchable to PostgreSQL, MySQL, Oracle, or Microsoft SQL Server.

### 2. Core Integrated Modules

| Module | Core Features & Business Workflows |
|---|---|
| **💰 Finance & General Ledger** | Chart of Accounts (Assets, Liabilities, Equity, Revenue, Expense), Double-Entry Journal Entries (automatically balanced), Accounts Receivable (AR) & Accounts Payable (AP) Invoicing, Payment Collection, Real-time P&L / Balance Sheet. |
| **👥 Human Resources (HR)** | Employee directory, Corporate department structures & budgets, Leave Request management with approval workflows, One-Click Monthly Payroll Engine with tax withholding and automated salary GL journal entries. |
| **🛒 Procurement & Supply Chain** | Vendor management & rating system, Purchase Orders (PO), One-Click Goods Receipt Note (GRN) that automatically replenishes inventory stock, updates vendor balances, and posts AP bills. |
| **💼 Sales & Customer CRM** | Customer accounts & credit limits, Sales Orders (SO), One-Click Order Fulfillment that automatically deducts warehouse stock, generates AR invoices, and recognizes revenue & COGS. |
| **⚙️ Manufacturing & Production** | Bill of Materials (BOM) multi-component recipes with labor/overhead costing, Plant Work Orders (WO), One-Click Production Runs that automatically consume raw materials and output finished goods to stock. |
| **📦 Inventory & Warehousing** | Item Master catalog (Raw Materials, Components, Finished Goods), Real-time stock levels, Min-stock reorder alerts, Total inventory valuation, Immutable stock movement audit ledger. |
| **⚡ Real-Time Automation** | Event-driven cross-module pipelines: e.g., shipping a sales order dispatches stock, generates AR invoice, updates customer balance, and posts debits & credits in Finance. |
| **🌐 Deployment Architecture** | Designed for Cloud-Based (SaaS), On-Premises, or Hybrid deployments with edge-to-cloud synchronization. |

---

## 🚀 Quick Start Guide

### Prerequisites
* Windows, Linux, or macOS.
* Portable **Java 21 LTS** and **Apache Maven 3.9.9** are already included in `tools/` with zero system installation needed!

### Running the Application

Double-click or run from PowerShell / Command Prompt:
```cmd
.\run.bat
```
Or via the included Maven wrapper:
```powershell
.\mvnw.cmd spring-boot:run
```

Once started:
* **ERP Web Portal:** [http://localhost:8080](http://localhost:8080)
* **Central Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  * *JDBC URL:* `jdbc:h2:mem:erpdb`
  * *User Name:* `sa`
  * *Password:* (leave blank)

---

## 🌐 Deployment Topologies

### ☁️ Cloud-Based (SaaS)
* Hosted on AWS, Google Cloud Platform (GCP), or Microsoft Azure.
* Containerized with Docker / Kubernetes (EKS / GKE / AKS).
* Automated zero-downtime rolling upgrades and auto-scaling.
* Managed relational databases (e.g. AWS Aurora PostgreSQL, Google Cloud SQL).

### 🏢 On-Premises
* Deployed locally on bare metal servers or VMware/Proxmox clusters inside the enterprise private datacenter.
* Complete data sovereignty for strict compliance (defense, healthcare, banking).
* Zero external internet dependencies; direct connectivity with plant floor SCADA/PLC machinery.

### ⚡ Hybrid Architecture
* Combines local edge appliances (running on manufacturing plant floors and logistics warehouse scanners for sub-millisecond offline continuity) with central Cloud ERP instances.
* Real-time bidirectional replication syncing local production batches and goods receipts with corporate finance and executive reporting.

---

## 🔌 Unified REST API Endpoints

### Executive Dashboard & Auditing
* `GET /api/dashboard` — Live KPI metrics, inventory valuation, active headcount, operational order split, and deployment status.
* `GET /api/audit` — Immutable chronological audit stream from all departments.

### Finance & Accounting
* `GET /api/finance/accounts` — Chart of Accounts listing.
* `POST /api/finance/accounts` — Create an account.
* `GET /api/finance/journal-entries` — General ledger entries.
* `POST /api/finance/journal-entries` — Post manual balanced journal entry.
* `GET /api/finance/invoices` — Accounts Receivable & Accounts Payable invoices.
* `POST /api/finance/invoices/{id}/pay` — Record customer payment or vendor disbursement.

### Human Resources
* `GET /api/hr/employees` — Employee directory.
* `POST /api/hr/employees` — Add employee.
* `GET /api/hr/departments` — Department structures and budgets.
* `GET /api/hr/payrolls` — Historical payroll distributions.
* `POST /api/hr/payrolls/run` — Trigger monthly payroll calculation and automated GL posting.
* `GET /api/hr/leaves` — Leave requests.
* `POST /api/hr/leaves/{id}/status` — Approve or reject employee leave.

### Procurement & Supply Chain
* `GET /api/procurement/vendors` — Approved suppliers.
* `GET /api/procurement/orders` — Purchase orders.
* `POST /api/procurement/orders` — Create new PO.
* `POST /api/procurement/orders/{id}/approve` — Approve PO.
* `POST /api/procurement/orders/{id}/receive` — Receive goods (auto-replenishes stock, posts AP bill and GL entry).

### Sales & CRM
* `GET /api/sales/customers` — Commercial clients and AR balances.
* `GET /api/sales/orders` — Commercial sales orders.
* `POST /api/sales/orders` — Create new sales order.
* `POST /api/sales/orders/{id}/confirm` — Confirm sales order.
* `POST /api/sales/orders/{id}/fulfill` — Ship & invoice order (dispatches inventory, posts AR invoice and revenue/COGS GL entry).

### Manufacturing & Production
* `GET /api/manufacturing/boms` — Bills of Materials recipes and component ratios.
* `POST /api/manufacturing/boms` — Define BOM recipe.
* `GET /api/manufacturing/work-orders` — Plant production runs.
* `POST /api/manufacturing/work-orders` — Launch new work order.
* `POST /api/manufacturing/work-orders/{id}/start` — Start production run.
* `POST /api/manufacturing/work-orders/{id}/complete` — Complete production run (consumes raw materials, outputs finished goods, and posts mfg GL entries).

### Inventory & Warehousing
* `GET /api/inventory/products` — Item master catalog and stock levels.
* `POST /api/inventory/products` — Create new product SKU.
* `GET /api/inventory/low-stock` — Products below reorder threshold.
* `GET /api/inventory/movements` — Detailed stock movement ledger.
