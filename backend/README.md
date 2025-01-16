# Currency Converter Service

## Development 

Prerequisites:
* Scala 3.3.4 or higher
* Sbt 1.10.7 or higher

### Essentials

#### Configure the application

Make `.env` file in the root
```
touch .env
```
Add the following environment variables to the `.env` file:
```
SWOP_API_KEY=your_api_key_here
SWOP_HOST=https://swop.cx/rest
```

#### Run redis
```
docker run -d --name redis-stack-server -p 6379:6379 redis/redis-stack-server:latest
```

#### Run the application
```
sbt run
```
After this you can access the Swagger docs at: http://localhost:8080/docs/

### Testing
You can run the Scalatest test suite with:
```
sbt test
```

### Linting
We are using scalafix sbt plugin for linting
```
sbt scalafixAll
```

### Formatting
Simply run the following command to format everything
```
sbt scalafmtAll
```

### Code coverage
You can run the Scalatest test suite with code coverage:
```
sbt clean coverage test coverageReport
```
Code coverage report can be found at `target/scala-3.3.4/scoverage-report/index.html`

### Publish Docker image
```
sbt docker:publishLocal
```