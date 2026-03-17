# Gambling App - Distributed Systems summer semester 2025-2026

A distributed system for an online gambling platform developed as part of the Distributed Systems course (Spring 2025-2026) at Athens University of Economics and Business. The system consists of a multithreaded Java Master node that routes requests to Worker nodes using consistent hashing, a MapReduce-based Reducer for aggregation queries, a Secure Random Number Generator server with a producer-consumer buffer, a Manager console app, and an Android frontend. All communication is handled exclusively via TCP sockets, with in-memory data structures and synchronization using synchronized / wait-notify.

_______________________________________________________________________________________________________(optional plan)

### **Deliverable A: Backend Core & Infrastructure**
**Deadline: April 3, 2026** [cite: 163]
The objective is to establish a functional distributed environment where managers can manage games and players can interact via a console-based dummy client[cite: 164].

#### **Member 1: Master Node & Network Protocol**
* Develop the **Master TCP Server** using Java `ServerSocket`[cite: 95].
* Implement the **Hashing function** $H(\text{GameName}) \pmod n$ for worker node selection[cite: 64, 65].
* Define the common **Message Protocol** (TCP Sockets) for system-wide communication[cite: 107].

#### **Member 2: Worker Nodes & Game Logic**
* Develop the **Worker Nodes** to manage game data exclusively in-memory[cite: 66, 100].
* Implement the **Betting Engine** based on Risk Level tables (Low, Medium, High) and Jackpot logic[cite: 74, 75, 76, 77].
* Apply thread synchronization using `synchronized`, `wait`, and `notify` to handle concurrent bets safely[cite: 89, 108].

#### **Member 3: Secure Random Generator (SRG) & Concurrency**
* Develop the **SRG TCP Server** as a standalone multithreaded process[cite: 98, 99].
* Implement the **Producer-Consumer model** using a Queue buffer for random number generation[cite: 79, 80].
* Implement the **SHA256 verification** (number + secret S) for secure communication between Worker and SRG[cite: 82, 83, 84].

#### **Member 4: Manager Console & Dummy Testing Client**
* Develop the **Manager Console Application** for JSON/Logo management[cite: 12, 38].
* Create the **Dummy Player Client** to simulate game filtering and betting during Phase A[cite: 164].
* Ensure the manager can perform add/remove/edit operations dynamically[cite: 60, 62].

---

### **Deliverable B: Android App & MapReduce Analytics**
**Deadline: May 10, 2026** [cite: 167]
The objective is to replace the dummy client with a professional Android UI and implement complex data aggregation queries[cite: 168].

#### **Member 1: Reducer & Final Aggregation**
* Develop the **Reducer Node** to merge intermediate values from Workers[cite: 105].
* Finalize the **MapReduce workflow** for total profit/loss analytics per Provider and Player[cite: 90, 91].

#### **Member 2: Map Function & Worker Integration**
* Implement the **Map function** logic within Workers to process local game/player data[cite: 29, 31].
* Establish the TCP link to forward local results to the Reducer[cite: 106].

#### **Member 3: Active Replication (Bonus/Mandatory)**
* Implement **Active Replication** to ensure data redundancy across multiple nodes[cite: 156, 157].
* Develop **Fault Tolerance** logic to reroute requests to replicas in case of node failure[cite: 158, 159].

#### **Member 4: Android Application Development**
* Build the **Android UI** featuring `search()`, `play()`, and `addBalance()`[cite: 22, 23, 24].
* Implement **Asynchronous Tasks** (Threads) for non-blocking TCP communication with the Master[cite: 154].

---

## 🛠 Tech Stack
* **Language:** Java [cite: 171]
* **Network:** TCP Sockets (No external HTTP libraries) [cite: 96, 150]
* **Storage:** In-memory Data Structures (No Databases) [cite: 109]
* **Frontend:** Android SDK / Android Studio [cite: 173, 174]