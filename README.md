# Java REST API Aggregator

A multithreaded Java console application for collecting and aggregating data from multiple REST APIs.

The project was developed as part of a course project on **multithreaded data aggregation from REST APIs**.

## Features

* Integration with multiple REST APIs
* Parallel API requests using multiple threads
* Configurable number of simultaneous tasks
* Configurable request interval
* Automatic and interactive application modes
* JSON and CSV output
* Saving aggregated data to files
* Reading saved data
* Unit testing of application logic
* Extensible architecture for adding new APIs

## Currently implemented APIs

The following APIs are currently functional:

* Weather
* Anime
* Books

Additional API implementations are present in the project but are currently unfinished:

* Alliexpress
* Crypt

## Technologies

* Java 24
* Maven
* Java `java.util.concurrent`
* Jackson Databind
* Apache Commons CSV
* JUnit 5
* Mockito

## Project Structure

```text
src/
├── main/
│   └── java/
│       └── org/example/
│           ├── Main.java
│           ├── config/
│           ├── convertor/
│           ├── workWithAPI/
│           │   ├── listAPI/
│           │   └── workWithAPIThread/
│           └── workWithData/
│
└── test/
    └── java/
        └── ...
```

### Main components

* **API clients** — responsible for sending requests to external APIs and receiving data.
* **API registry** — stores available APIs and allows them to be selected by name.
* **Converters** — transform API responses into the application's internal data representation.
* **Data processing** — processes and prepares received data.
* **Data storage** — saves data in JSON or CSV format and reads previously saved results.
* **Thread management** — controls parallel API requests and their execution intervals.
* **Console interaction** — handles user input and application configuration.

## Requirements

To run the project, you need:

* Java 24 or compatible JDK
* Maven
* IntelliJ IDEA or another Java IDE
* Internet connection for accessing external APIs
* An API key for the Weather API

### API key configuration

The API key is stored locally and is **not included in the repository**.

Configuration files containing API keys are excluded from Git using `.gitignore`:

```gitignore
config.properties
*.key
*.secret
```

Before running the application, configure your own API key locally.

## Running the Application

The project is normally launched from **IntelliJ IDEA**.

1. Open the project in IntelliJ IDEA.
2. Open the **Run Configuration** for the application.
3. Configure the required environment variables.
4. Specify program arguments if required by the selected mode.
5. Run the application using **Shift + F10**.

The API key is provided through the local run configuration and is not committed to GitHub.

## Application Modes

The application supports two modes:

* `inter` — interactive mode
* `auto` — automatic mode

### Interactive mode

In interactive mode, the application asks the user for the required parameters directly in the console.

The user can configure:

* number of simultaneous tasks;
* interval between requests;
* APIs to use;
* output format;
* file mode;
* APIs whose results should be displayed.

The application can then be started and stopped interactively.

### Automatic mode

In automatic mode, all parameters are passed as program arguments.

The general syntax is:

```text
auto <number_of_tasks> <interval> <session_timeout> <API1> [API2 ...] <output_format>
```

For example:

```text
auto 3 1000 10000 Weather Books Anime json
```

The exact API names available for selection correspond to the APIs registered in the application.

### Automatic mode parameters

| Parameter           | Description                                       |
| ------------------- | ------------------------------------------------- |
| `auto`              | Starts automatic mode                             |
| `number_of_tasks`   | Maximum number of simultaneous tasks              |
| `interval`          | Minimum interval between requests to the same API |
| `session_timeout`   | Total duration of the automatic session           |
| `API1`, `API2`, ... | APIs selected for data collection                 |
| `output_format`     | Output format: `json` or `csv`                    |

The application validates the input parameters before starting the requests.

## Multithreading

The application uses Java concurrency utilities to perform API requests in parallel.

The number of simultaneously running tasks can be configured by the user.

Each API is periodically queried according to the configured interval. After the configured session time expires, the application stops the running tasks.

The thread management logic is separated from the API and data-processing logic.

## Output

Collected data can be saved in two formats:

* JSON
* CSV

The application also supports reading saved results and displaying either:

* data from all APIs;
* data from a selected API.

## Testing

The project contains unit tests using:

* JUnit 5
* Mockito

The tests cover the main application components and business logic.

Unit tests do not require network access to external APIs.

Run tests with Maven:

```bash
mvn test
```

Build the project with:

```bash
mvn clean package
```

## Course Project Requirements

The project was developed according to the following requirements:

* aggregation of data from at least three open REST APIs;
* JSON data processing;
* console application;
* automatic and interactive modes;
* parallel API requests;
* configurable number of simultaneous tasks;
* configurable request intervals;
* saving aggregated data to files;
* JSON and CSV output;
* object-oriented design;
* separation of API access, data processing, storage, and user interaction;
* multithreading using Java concurrency utilities;
* unit testing.

## Limitations

At the current stage, three APIs are fully functional:

* Weather
* Anime
* Books

The Alliexpress and Crypt API implementations are present in the project but are not currently complete.

## Author

**Sofya Pozneeva**
