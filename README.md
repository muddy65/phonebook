# 📒 PhoneBook

### Your people, always close.

A colorful desktop address book built with **Java Swing** and **MySQL**. Create a private account, keep your contacts organized, and find them quickly from a searchable table.

![Java](https://img.shields.io/badge/Java-Swing-176B87?style=for-the-badge&logo=openjdk&logoColor=white)
![Database](https://img.shields.io/badge/Database-MySQL-147D92?style=for-the-badge&logo=mysql&logoColor=white)
![Status](https://img.shields.io/badge/Status-In%20development-E6A23C?style=for-the-badge)

---

## ⬇️ Download PhoneBook

> **Release download:** [https://github.com/muddy65/phonebook/releases/download/v1.0.2/Phone.Book-1.0.2.exe](https://github.com/muddy65/phonebook/releases/download/v1.0.2/Phone.Book-1.0.2.exe)
>
> 

---

## ✨ Features

- 🔐 Sign up and log in with salted, hashed passwords.
- 👤 View your name, username, address, email, and phone number.
- 📇 Add, search, sort, and delete contacts in your personal list.
- 🧹 Delete your account after confirming your password; its contacts are removed with it.
- 🛠️ Sign in with the reserved username `muddy` to open the admin dashboard and view registered account details.
- 🎨 A navy and teal interface with a custom phonebook image.

Each contact is associated with its owner's account in the database, so users see their own contact lists.

## 🧰 Requirements

- JDK 11 or newer
- MySQL Server
- MySQL Connector/J added to the project's runtime classpath

## ⚙️ Configure MySQL

1. Open `DatabaseHelper.java` and set `URL`, `SERVER_URL`, `USERNAME`, and `PASSWORD` to match your local MySQL server.
2. Use a MySQL account with permission to create the `contacts` database and its tables. The app initializes the schema from `schema.sql` on startup.
3. Keep database passwords private. Do not publish them in source code or commit them to a public repository.

The schema creates a `users` table and a `contacts` table. Passwords are stored as salted PBKDF2 hashes; the admin screen shows account details and password protection status, never the password hashes.

## ▶️ Compile and run (PowerShell)

Run these commands from the `javaLoginPage` folder. The Connector/J JAR below matches the filename currently in this folder; adjust it if yours has a different name.

```powershell
New-Item -ItemType Directory -Force bin | Out-Null
javac -d bin *.java
java -cp "bin;.;mysql-connector-j-26.10.0.jar" MainDashboard
```

The `.` entry makes `schema.sql` and `phonebook.png` available at runtime.

## 🔑 Account notes

- The username `muddy` is reserved for the administrator and cannot be used for a new standard signup.
- New passwords must contain at least **6 characters**.
- Password recovery checks the account's username and email address. No email verification service is configured yet.

## 🗂️ Project files

| File | Purpose |
| --- | --- |
| `MainDashboard.java` | Connects login, signup, recovery, user, and admin screens |
| `MainPage.java` | User profile and contact book |
| `AdminPage.java` | Registered users directory |
| `LoginPage.java`, `SignupPage.java`, `PasswordResetPage.java` | Account screens |
| `DatabaseHelper.java` | MySQL access |
| `Validation.java` | Form checks and password hashing |
| `schema.sql` | MySQL database and table definitions |
| `phonebook.png` | Login/signup branding and window icon |

---

💙 **PhoneBook — a little more organized, a lot more connected.**
