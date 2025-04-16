# LoadTestingMetricsPlugin

This repository contains the source code of the load testing metrics collection plugin for the Dronce CI/CD ecosystem.

The project is completed during the preparation of Andrey Petrov work under Testing of software at SPbPU Institute of Computer Science and Cybersecurity (SPbPU ICSC).

Authors and contributors The main contributor Andrey Petrov student of SPbPU ICSC. The advisor and contributor Vladimir A. Parkhomenko Senior Lecturer of SPbPU ICSC.

Warranty The contributors give no warranty for the using of the software.

License This program is open to use anywhere and is licensed under the GNU General Public License v3.0.

---
---
---

Рядом с файлом, находится Dockerfile и папка проекта плагина. Пойдём по порядку.

---

## Dockerfile

Этот Dockerfile описывает многоэтапную сборку Docker-образа для проекта LoadTestingMetricsPlugin — Java-плагина для сбора и экспорта метрик во время нагрузочного тестирования (k6).

Sh скрипт параллельно запускает приложение и k6 в общем Docker контейнер, дожидаясь полного завершения работы плагина.

---
---

## LoadTestingMetricsPlugin

Этот проект представляет собой плагин для сбора метрик во время нагрузочного тестирования. Основная задача плагина — уменьшить число шагов в pipeline для проведения нагрузочного тестирования в экосистеме Drone CI/CD.

В древе ниже видна структура проекта плагина.

```text
.
├── pom.xml
└── src
    └── main
        ├── java
        │   └── plugin
        │       ├── Abstractions
        │       │   ├── DataShell.java
        │       │   └── Follower.java
        │       ├── DTO
        │       │   └── Metrics.java
        │       ├── DataShells
        │       │   ├── MarkdownShell.java
        │       │   ├── PdfFileShell.java
        │       │   └── TextFileShell.java
        │       ├── Followers
        │       │   ├── EmailFollower.java
        │       │   ├── FilesystemFollower.java
        │       │   └── TelegramFollower.java
        │       ├── Main.java
        │       └── Services
        │           ├── FilesManager.java
        │           └── Transporter.java
        └── resources
            └── application.conf
```

---

## Запуск плагина

Чтобы собрать и запустить плагин, нужно выполнить команды:

```bash
docker build -t plugin_image .
docker run docker run \
 --rm \
 --network=host \
 -v C:\main\Apps\k6:/app/k6 \
 -v /var/run/docker.sock:/var/run/docker.sock \
 --privileged \
 -e SUBSCRIBE_TELEGRAM="true" \
 -e TELEGRAM_TOKEN="..." \
 -e TELEGRAM_CHAT_ID="..." \
 -e SENDING_FORMAT="TEXT_FORMAT" \
 -e ONLY_CURRENT_TEST="true" \
 plugin_image
```

---

## Структура проекта

- src/main/java: Исходный код проекта.
- pom.xml: Файл конфигурации Maven, включая зависимости и настройки плагинов.
- application.conf: файл конфигурации, в который записываются переменные окружения

---

## Переменные окружения

Общие переменные:
- SENDING_FORMAT - формат итогового отчёта. Возможные значения: TEXT_FORMAT, PDF_FORMAT, MARKDOWN.
- ONLY_CURRENT_TEST - при значении true отправляется отчёт только по текущему тестированию, при false - агрегированный файл со статистикой за всё время.

Переменные для Telegram:
- SUBSCRIBE_TELEGRAM - true или false. Определяет, нужно ли отправлять отчёт в Telegram.
- TELEGRAM_TOKEN - токен Telegram-бота.
- TELEGRAM_CHAT_ID - идентификатор чата, в который будет отправлен отчёт.

Переменные для e-mail:
- SUBSCRIBE_EMAIL - флаг активации отправки по e-mail (true или false).
- SMTP_HOST, SMTP_PORT - адрес и порт SMTP-сервера.
- USERNAME, PASSWORD - учётные данные отправителя.
- RECIPIENTS - список получателей (e-mail адреса через заптую).

Переменная для файловой системы:
- SUBSCRIBE_FILESYSTEM - логическое значение, определяющее, нужно ли сохранять отчёт локально.

---

## Пример использования

Пример конфигурации Drone CI/CD pipeline:

```yaml
kind: pipeline
type: docker
name: simple-pipeline

steps:
  #build app
  #run API

  - name: run load testing metrics plugin
    image: swezeg/loadtestingmetricsplugin:latest
    privileged: true
    volumes:
      - name: dockersock
        path: /var/run/docker.sock
    environment:
      SUBSCRIBE_TELEGRAM: true
      TELEGRAM_TOKEN: 8060387975:AAGTxAHHqHEo7LKpD4z71LKx7LEZSngh8k8
      TELEGRAM_CHAT_ID: 978107538
      SENDING_FORMAT: TEXT_FORMAT
      ONLY_CURRENT_TEST: true
   
volumes:
  - name: dockersock
    host:
      path: /var/run/docker.sock
```



