# SensorServer
SensorServer is a basic server that is designed to act as a data receiver for
debugging purposes. When open, multiple devices can connect to it using basic
TCP sockets and transmit sensor data in realtime to this server. The data is
then saved in CSV files and can be analyzed or further processed easily.

## Installation
To obtain SensorServer, simply download a built release from the releases
page, and place it in a directory you know. To run these releases, make sure
you have a JRE of version 17 or higher installed and in your path.

Additionally, you can build a JAR yourself by cloning the repository and
building the `jar` target with Gradle.

## Usage
To run the Server, open a terminal in the directory where your JAR is located
and run it with Java. If needed, you can provide a few options to customize the
server.

```bash
java -jar SensorServer-v0.2.jar [OPTIONS]
```

The following options are supported:
- **--help** - Display help information and exit
- **--version** - Show version info and exit
- **--output** <PATH> - Specify the directory where the data should be saved
- **--port** <PORT> - Specify the port the server should run on

By default, the server runs on port `55555` and saves the data to the working
directory.

## Data Storage
As already mentioned, this application stores the received data in CSV files.
This makes it easy to analyze or further process the received data. Data
received by a connection is broken up into single transmissions by the client.

So there are two types of files used:
- **Index File**: Each data directory contains an `index.csv` file. This file
  contains a row for every transmission recorded. It keeps track of given IDs,
  devices and formats of the different transmissions.
- **Data File**: Every transmission is stored in its own file. On starting a
  transmission, it is assigned a unique 4-letter transmission ID. The data will
  then be stored in a file with the ID in its name (e.g. `data-a1b2.csv`). Also
  be aware, that a data file contains a header, indicating which columns are what.
  These names are set by the client.

## Protocol
This server uses a very basic protocol on top of usual TCP sockets to receive its
data. This communication is one-way. This means that the server does not respond
to any data received by the client. This makes it very simple. To learn more
about this protocol, head to the *file what will document the protocol but hasn't
been crated yet*.

## License
This application is licensed under the MIT license. Take a look at the LICENSE file
for more information.
