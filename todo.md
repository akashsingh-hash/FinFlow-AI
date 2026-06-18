# Finflow AI: Complete Manual Testing Checklist

Use the password **`password123`** for all preconfigured accounts when running these tests.

---

## 1. 🏢 Multi-Tenant Registration & Signup
* [ ] **Test Case: Setup a Brand New Company**
  * Go to the **Register** page.
  * Register with a new email (e.g., `owner@google.com`) and input a new company name (e.g., `Google LLC`) and Tax ID.
  * Log in with `owner@google.com`. Verify that you are redirected to a blank dashboard for your new company (confirming data isolation from the demo company `Acme Corporation`).

---

## 2. 👥 Team Directory & User Management
*Log in as Admin (`admin@acme.com` / `password123`)*
* [ ] **Test Case: Invite a New User**
  * Go to the **Team Directory** page.
  * Click **Invite Team Member** and create a user (e.g. `engineer@acme.com` in the **Engineering** department with the **Employee** role).
  * Verify they appear in the team list as **`PENDING`**.
* [ ] **Test Case: Change a User's Role**
  * Find `employee@acme.com` in the directory.
  * Use the dropdown in their row to change their role from **Employee** to **Manager**. Verify the change updates successfully.
* [ ] **Test Case: Deactivate / Reactivate a User**
  * Find `auditor@acme.com` in the list.
  * Click the red **Deactivate** button (user status changes to **`INACTIVE`**).
  * Try logging in as `auditor@acme.com` / `password123`. Verify the system blocks the login with an "INACTIVE" warning.
  * Log back in as `admin@acme.com` and click **Activate** (status returns to **`ACTIVE`**).

---

## 3. 💰 Departmental Budgets
*Log in as Admin (`admin@acme.com` / `password123`)*
* [ ] **Test Case: Set Up a Budget**
  * Go to the **Budgets** page.
  * Click **Create Allocation** and allocate a budget of **2,000 INR** to the **Sales** department.
  * Verify it creates a card showing **0% Consumed**.

---

## 4. 💸 Expense Routing & Approval Rules
* [ ] **Test Case 1: Budget Overrun Blocker**
  * Log in as the new user `owner@google.com` (which has no budgets allocated yet).
  * Go to **Expenses** -> **File Expense Claim** (create a draft claim for 100 INR).
  * Click the green **Send** icon to submit it. Verify the application **blocks** the submission because there is no active budget for your department.
* [ ] **Test Case 2: Auto-Approval Rule (< 5,000 INR)**
  * Log in as **`employee@acme.com`** / `password123`.
  * File a claim for **3,000 INR** (Meals & Entertainment).
  * Click the green **Send** icon to submit it.
  * Verify the status immediately becomes **`APPROVED`** (No manager action needed).
* [ ] **Test Case 3: Manager Approval Rule (5,000 - 25,000 INR)**
  * Log in as **`employee@acme.com`** / `password123`.
  * File a claim for **15,000 INR** (Software & Tool Licensing). Submit it.
  * Verify the claim status changes to **`UNDER_REVIEW`**.
  * Log in as the manager **`manager@acme.com`** / `password123`.
  * Go to the **Approvals** page. Verify the claim appears under "Assigned to You". Click **Perform Review** -> **Approve**.
* [ ] **Test Case 4: Finance Pool Approval Rule (> 25,000 INR)**
  * Log in as **`employee@acme.com`** / `password123`.
  * File a claim for **35,000 INR** (IT Hardware). Submit it.
  * Log in as the Finance Manager **`finance@acme.com`** / `password123`.
  * Go to the **Approvals** page. Verify the claim appears under "Unassigned Finance Pool". Click **Claim Claim**, then click **Perform Review** -> **Approve**.

---

## 5. 🤝 Vendor Directory
*Log in as Finance Manager (`finance@acme.com` / `password123`)*
* [ ] **Test Case: Add and Delete a Vendor**
  * Go to the **Vendors** page.
  * Click **Add Vendor** (e.g. `Microsoft`, `billing@microsoft.com`).
  * Verify Microsoft is added to your supplier cards.
  * Click the trash icon on Microsoft and click **Confirm** in the custom modal to delete it.

---

## 6. 🧾 Invoices
*Log in as Manager (`manager@acme.com` / `password123`)*
* [ ] **Test Case: Register and Pay a Vendor Invoice**
  * Go to the **Invoices** page.
  * Click **Upload Invoice** (Invoice Number: `INV-2026-99`, Amount: `5000`, Vendor: `Amazon Web Services`).
  * Log in as Finance Manager **`finance@acme.com`**.
  * Go to the **Invoices** page. Find `INV-2026-99`, click **Mark Paid**, and verify the invoice status changes to **`PAID`**.

---

## 7. 💵 Payout Settlements
*Log in as Finance Manager (`finance@acme.com` / `password123`)*
* [ ] **Test Case: Disburse Reimbursements**
  * Go to the **Reimbursements** page.
  * Locate any approved claim from the earlier approval test cases (status: **`PENDING`**).
  * Click **Disburse Payout**.
  * Choose **Bank Transfer**, enter a reference code (e.g., `UTR-987654`), and click **Record Settlement**. Verify the status changes to **`PAID`**.

---

## 8. 🛡️ Audit Trail Logs
*Log in as Admin (`admin@acme.com` / `password123`)*
* [ ] **Test Case: Inspect Audits**
  * Go to the **Audit Logs** page.
  * Verify that every action you performed (e.g. user toggles, role assignments, vendor creation/deletions) has been logged.
  * Click the **Eye icon** on a role change or status toggle log. Inspect the JSON panel to see the exact old value vs the new updated value.

---

## 9. 📈 Dynamic Dashboard Analytics
*Log in as Admin (`admin@acme.com` / `password123`)*
* [ ] **Test Case: Verify Graphs and Statistics**
  * Observe the Dashboard after approving/disbursing expenses.
  * Confirm that **Approved Expenses** and **Monthly Spending** have increased.
  * Confirm that the **Departmental Budget Consumption** bar graph has updated its height reflecting the money spent.
  * Confirm that the **Overall Budget Utilization** percentage bar has progressed (e.g. from 1% consumed to a higher percentage).
  * Confirm that **Pending Approvals** count matches exactly what is outstanding.
