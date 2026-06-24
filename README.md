# PalletFlow 📦

> Inventory & profit tracking for liquidation pallet buyers in the USA.

PalletFlow is a native Android application built for small business owners who purchase liquidation pallets, break them down, and sell individual items on eBay, Facebook Marketplace, bin stores, and flea markets.

---

## The Problem

Most pallet resellers track their business in Excel spreadsheets or a mix of QuickBooks and handwritten notes. There is no easy way to know the real profit of a single pallet after factoring in logistics, shipping, labor, and other costs — until now.

---

## What PalletFlow Does

- **Lot tracking** — Create a purchase record for every pallet or shipment. Add the purchase price, logistics costs, and any extra expenses. The app automatically calculates the true cost per item.
- **Cost distribution** — When multiple pallets arrive in one truck, split shared costs (freight, fuel, labor) across pallets proportionally or manually.
- **Profit per item** — Know exactly how much you made on every single item sold, traced back to its original lot.
- **Multi-warehouse support** — Manage inventory across multiple storage locations and move items between them.
- **Sales tracking** — Record sales by platform (eBay, Facebook Marketplace, Cash, Bin Store) including shipping costs.
- **Reports** — Sales reports, profit by lot, stock levels, and top-performing items. Filter by day, week, month, or custom date range.
- **Offline first** — Works completely without internet. Your data stays on your device.
- **QuickBooks export** — Export transactions to CSV for QuickBooks at tax time.

---

## Screenshots

> Coming soon

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| Navigation | Jetpack Navigation Component |
| Database | Room |
| Storage | DataStore |
| DI | Hilt |
| Async | Coroutines + Flow |
| Images | Coil |
| Min SDK | 30 (Android 11) |

---

## Project Structure

```
com.palletflow
├── data
│   ├── local (Room entities, DAOs, Database)
│   ├── repository (implementations)
│   └── datastore
├── domain
│   ├── model
│   ├── repository (interfaces)
│   └── usecase
├── presentation
│   ├── navigation
│   ├── common
│   └── screens
│       ├── auth / onboarding
│       ├── dashboard
│       ├── warehouse
│       ├── supplier
│       ├── lot
│       ├── inventory
│       ├── sell / move
│       ├── transactions
│       ├── reports
│       └── settings
└── di
```

---

## Features Roadmap

### MVP (v1.0)
- [x] Project structure
- [ ] Local authentication (PIN + Biometrics)
- [ ] Onboarding flow
- [ ] Warehouse management
- [ ] Supplier management
- [ ] Receive new lot with cost distribution
- [ ] Inventory tracking
- [ ] Sell item flow
- [ ] Move inventory between warehouses
- [ ] Transaction history
- [ ] Basic reports
- [ ] QuickBooks CSV export

### v2.0
- [ ] Cloud sync (Supabase)
- [ ] eBay integration
- [ ] Facebook Marketplace integration
- [ ] Barcode / Bluetooth scanner support
- [ ] Photo documentation
- [ ] Multi-user support

---

## Getting Started

### Requirements
- Android Studio Hedgehog or newer
- JDK 17
- Android device or emulator running API 30+

### Setup

```bash
git clone https://github.com/yourusername/palletflow.git
cd palletflow
```

Open in Android Studio and let Gradle sync. No API keys required for local-only MVP.

---

## Target Market

Small pallet liquidation businesses in the USA — typically 1 to 5 person operations buying from Amazon, Walmart, and other retailers through liquidation marketplaces like BULQ, Direct Liquidation, and B-Stock.

---

## License

MIT License — see [LICENSE](LICENSE) for details.

---

## Author

Built by a pallet business owner, for pallet business owners.
