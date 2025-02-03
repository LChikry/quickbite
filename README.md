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

<br />
<br />
<br />
<strong><h2 align="center"> DISCLAIMER:</h2></strong>
<p align="center">
  THIS PROJECT IS CURRENTLY <strong>UNDER DEVELOPMENT</strong> AND IS IN <strong>ALPHA</strong> STATE/VERSION. SOME FEATURES MAY NOT BE FULLY IMPLEMENTED, AND BUGS MAY STILL BE PRESENT. PLEASE USE WITH CAUTION AND CHECK FOR UPDATES REGULARLY.
</p>
<br />
<br />
<br />

## ✨ Features

- **Menu Browsing** – Customers can view the restaurant's menu directly within Telegram.
- **Pre-Ordering** – Allows customers to place orders in advance, reducing wait times.
- **Pickup or Dine-In** – Customers can select their preferred dining option.
- **Extensible** – Restaurants can modify the bot's source code to fit their specific business logic.

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK) 21 or higher**
- **Postgres Database**
- **A Telegram bot token** (obtain from [@BotFather](https://core.telegram.org/bots#botfather))
- **Optional: Docker**

### Installation
1. **Clone the Repository**:

   ```bash
   git clone https://github.com/LChikry/quickbite.git
   cd quickbite
   ```

2. **Configure the Bot**:
    - create a `.env` file at the root directory and add the following information to it:

      ```properties
       BOT_TOKEN=YOUR_TELEGRAM_TOKEN_HERE
       DB_PASSWORD=YOUR_POSTGRES_DATABASE_PASSWORD_HERE
       DB_USER=YOUR_POSTGRES_DATABASE_USERNAME_HERE
       DB_URL=YOUR_POSTGRES_DATABASE_URL_HERE
       PEPPER=YOUR_CUSTOM_PASSWORD_PEPPER_FOR_HASHING_HERE
      ```

3. **Build & Run the Project**:
    - Without Docker: just use your idea building mechanism, which also will run the bot for you. It's easier.
    - With Docker: run the commands below. The first one for building the project and second to run it.

   ```bash
   docker build -t quickbitejar .
   ```
   ```bash
   docker run quickbitejar:latest
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