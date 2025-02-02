<h1 align="center">QuickBite</h1>

<p align="center">
  <a href="https://github.com/LChikry/quickbite/releases">
    <img src="https://img.shields.io/github/v/release/LChikry/quickbite" alt="Latest Release">
  </a>
  <a href="https://github.com/LChikry/quickbite/issues">
    <img src="https://img.shields.io/github/issues/LChikry/quickbite" alt="Issues">
  </a>
  <a href="https://github.com/LChikry/quickbite/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/LChikry/quickbite" alt="License">
  </a>
    <a href="https://x.com/LChikry" target="_blank" style="margin: 2px;">
    <img src="https://img.shields.io/badge/Follow%20Me%20On%20X-8A2BE2" alt="FollowX">
  </a>
</p>

<p align="center">
  <strong>QuickBite</strong> is an extensible Telegram bot template for restaurants, enabling customers to browse menus, pre-order meals, and choose pickup or dine-in for a fast, seamless, <strong>and most importantly, line-free</strong> experience.
</p>

---

## ✨ Features

- **Menu Browsing** – Customers can view the restaurant's menu directly within Telegram.
- **Pre-Ordering** – Allows customers to place orders in advance, reducing wait times.
- **Pickup or Dine-In** – Customers can select their preferred dining option.
- **Extensible** – Restaurants can modify the bot's source code to fit their specific business logic.

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK) 17 or higher**
- **Maven**
- **A Telegram bot token** (obtain from [@BotFather](https://core.telegram.org/bots#botfather))

### Installation

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/LChikry/quickbite.git
   cd quickbite
   ```

2. **Configure the Bot**:
   - create a `.env` file at the root directory and add your Telegram bot token and database connection information. For example,

     ```properties
     BOT_TOKEN=YOUR_TELEGRAM_BOT_TOKEN
     DB_PASSWORD=YOUR_DATABASE_PASSWORD
     ```

3. **Build the Project**:

   ```bash
   mvn clean install
   ```

4. **Run the Bot**:

   ```bash
   java -jar target/quickbite-1.0.jar
   ```

## 📌 Usage

Once the bot is running, customers can interact with it on Telegram to:

- **Browse the menu**
- **Place pre-orders**
- **Choose between pickup or dine-in options**

Restaurants can customize the bot's behavior by modifying the source code to add specific business logic or restrictions. Also, they will need to create a telegram bot and add it to the bot in order to receive customers order on that bot then react to it (accept, reject, notify, etc.)

## 🤝 Contributing

Contributions are welcome! Fork this repository and submit a pull request for any enhancements or bug fixes.

## 📜 License

This project is licensed under the **MIT License**. See the [LICENSE](https://github.com/LChikry/quickbite/blob/main/LICENSE.md) file for details.

## 🙌 Origins of the Idea
to be told another day :)