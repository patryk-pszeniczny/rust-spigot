# Rust-Style Building Core (WIP)

[![Status: Work in Progress](https://img.shields.io/badge/status-WIP-orange.svg)]()
[![Classes](https://img.shields.io/badge/classes-500%2B-blue.svg)]()
[![License](https://img.shields.io/badge/license-TBD-lightgrey.svg)]()

A **Rust-style building system**: the foundational core for a survival/construction game server.  
It implements building blocks (foundations, walls, ceilings), placement rules, stability checks, upgrade paths, permissions, and backend services (persistence, events).  
> **Note:** The core is **not 100% finished**. Some modules are actively evolving.

---

## 📦 What’s Inside (High Level)
- **Building Blocks**: foundations, walls, ceilings, sockets, snapping & grid alignment  
- **Placement & Validation**: terrain checks, collisions, adjacency rules, zoning  
- **Stability & Decay**: structure graph, load propagation, conditional decay  
- **Upgrades & Tiers**: material progression, durability, costs, repair logic  
- **Permissions**: tool cupboard / TC-like control, roles, team visibility  
- **Server Core**: events bus, persistence, auth, config, telemetry hooks

> Current scale: **500+ classes** (domain, services, validation, utilities).

---

## 🚀 Release Cadence (Auction-Driven)
To keep valuation transparent, **one class is released every day**.  
Daily releases will **continue until potential buyers stop bidding up the price**.

Each daily drop includes:
- The class source code
- A brief docstring or README note
- Change highlights / context

---

## 💸 Bidding Board

| # | Buyer   | Amount | Date |
|---|---------|--------|------|
| 7 | Server B.....pl     | 7200 PLN  | 22.10.2025 |
| 6 | Server T.....pl     | 7000 PLN  | 19.10.2025 |
| 5 | Server C.....pl     | 6500 PLN  | 17.10.2025 |
| 4 | Anonymous participant A     | 4305 PLN  | 13.10.2025 |
| 3 | Anonymous participant B     | 4300 PLN  | 12.10.2025 |
| 2 | Anonymous participant A     | 3900 PLN  | 12.10.2025 |
| 1 | stevko     | 5 PLN  | 11.10.2025 |

**Rules:**  
- Highest standing bid sets the current valuation trend  
- Daily releases continue while the price is being bid up

---

## 🗺️ Roadmap (Short)
- [ ] Finalize stability algorithms & edge cases  
- [ ] Expand upgrade tiers & material balance  
- [ ] Permission edge cases (raid/overlap scenarios)  
- [ ] Persistence: snapshot + migration tooling  
- [ ] Public API draft & examples

---

## 🛠️ Tech Notes
- Clean architecture: domain → application → infrastructure layers
- Event-driven core (publish/subscribe)
- Config-first, DI-friendly design
- Designed for server performance & testability

---

## 🤝 Contributing / Access
This repository releases **one class per day**. If you’re evaluating or bidding:
- Open an issue with your **bid** (buyer, amount, date) or contact maintainers
- Request a **private preview** of upcoming modules if needed
- Use Discussions for feature questions

---

## 📄 License
TBD — license will be finalized before the full public release.

---

## ✉️ Contact
For bids, previews, or partnership inquiries: **pszeniczny.patryk1@gmail.com**
