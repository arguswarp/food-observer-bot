# Food observer bot

## Description
This is a pet project for my wife to use (and maybe somebody else). 
It's purpose to save records of food that mother have eaten and add ratings to stats that represent the state of her baby's allergy. 
In simple words this bot gathers data to find out what was the cause of allergy.

## Installation
First clone this repository to your local machine:
```
git clone https://github.com/arguswarp/food-observer-bot.git
```
Next add **.env** file to the root folder of the project. It must contain environment variables listed below:
```
DB_URL=<database url>
DB_USERNAME=<database username>
DB_PASSWORD=<password for database user>
DB_PORT=<database external port>
PG_VERSION=<PostgreSql version to use>

TIME_ZONE=<time zone for app container>

BOT_TOKEN=<your bot token>
BOT_NAME=<your bot name>

EXCEL_PATH=<path to store generated excel files>
```
You must have docker and docker compose installed. 
Also, to get bot token and bot name you must create your bot using [BotFather](https://t.me/BotFather).

Finally, run: 
```bash
docker compose up --build app -d
```
This command will build new image from the [Dockerfile](/Dockerfile) for the app and starts containers with app and database.

## Usage
Use like all other bots - `/start` to begin, `/help` to see available commands.

Bot has main menu on the left side with all commands, menu on the bottom for most common commands.

Other commands can be accessed with inline buttons in an interactive way.

Command `/record` contains all the commands to add and show records.

As a user you can add food records and make notes for today or yesterday.
Mode changes  with `/mode`.