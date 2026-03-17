# Gambling App - Distributed Systems summer semester 2025-2026

A distributed system for an online gambling platform developed as part of the Distributed Systems course (Spring 2025-2026). The system consists of a multithreaded Java Master node that routes requests to Worker nodes using a hash-based selection function, a MapReduce-based Reducer for aggregation queries, a Secure Random Number Generator server with a producer-consumer buffer, a Manager console app, and an Android frontend (implemented in Deliverable B). All communication is handled exclusively via TCP sockets, with in-memory data structures and synchronization using synchronized / wait-notify.

_______________________________________________________________________________________________________

### **Deliverable A: Backend Core & Infrastructure**
**Deadline: April 3, 2026**

The objective is to establish a functional distributed environment where managers can manage games and players can interact via a console-based dummy client.

The system includes an initial implementation of the Reducer to support MapReduce-based search operations. Full aggregation queries (e.g., total profit/loss per Provider and Player) will be completed in Deliverable B.

#### **Member 1: Master Node & Network Protocol**
* Develop the **Master TCP Server** using Java `ServerSocket`.
* Implement the **Hashing function** $H(\text{GameName}) \mod n$ for worker node selection.
* Define the common **Message Protocol** (TCP Sockets) for system-wide communication.

#### **Member 2: Worker Nodes & Game Logic**
* Develop the **Worker Nodes** to manage game data exclusively in-memory.
* Implement the **Betting Engine** based on Risk Level tables (Low, Medium, High) and Jackpot logic.
* Apply thread synchronization using `synchronized`, `wait`, and `notify` to handle concurrent bets safely.
* Ensure thread-safe updates of game statistics under concurrent betting.

#### **Member 3: Secure Random Generator (SRG) & Concurrency**
* Develop the **SRG TCP Server** as a standalone multithreaded process.
* Implement the **Producer-Consumer model** using a Queue buffer for random number generation.
* Implement the **SHA256 verification** (number + secret S) for secure communication between Worker and SRG.

#### **Member 4: Manager Console & Dummy Testing Client**
* Develop the **Manager Console Application** for JSON/Logo management.
* Create the **Dummy Player Client** to simulate game filtering and betting during Phase A.
* Ensure the manager can perform add/remove/edit operations dynamically.
* Implement basic player actions: `search`, `play`, and `addBalance`.

---

### **Deliverable B: Android App & MapReduce Analytics**
**Deadline: May 10, 2026**

The objective is to replace the dummy client with an Android UI and implement full data aggregation queries.

#### **Member 1: Reducer & Final Aggregation**
* Develop the **Reducer Node** to merge intermediate values from Workers.
* Finalize the **MapReduce workflow** for total profit/loss analytics per Provider and Player.

#### **Member 2: Map Function & Worker Integration**
* Implement the **Map function** logic within Workers to process local game/player data.
* Establish the TCP link to forward local results to the Reducer.

#### **Member 3: Active Replication (Bonus - Mandatory for 4 members)**
* Implement **Active Replication** to ensure data redundancy across multiple nodes.
* Develop **Fault Tolerance** logic to reroute requests to replicas in case of node failure.

#### **Member 4: Android Application Development**
* Build the **Android UI** featuring `search()`, `play()`, and `addBalance()`.
* Implement **asynchronous communication** (Threads) for non-blocking TCP interaction with the Master.

---

## 📂 Project Structure

```text
/
├── backend/
|    ├── dummy-client/       # Phase A testing app (Member 4)
|    ├── manager-app/        # Java Console for Managers (Member 4)
|    ├── master/             # Master TCP Server (Member 1)
|    ├── random-generator/   # Secure Random Generator (Member 3)
|    ├── reducer/            # Reducer Aggregation Logic (Member 1)
|    ├── shared/             # Common Classes (Game.java, Message.java)
|    ├── worker/             # Worker Node Logic (Member 2)
|
├── android-app/        # Android Studio Project (Member 4 - Phase B)
├── assets/             # Game JSONs and Logos (.png)
├── config/             # IPs, Ports, and Worker Nodes list
└── README.md           # Project Documentationv