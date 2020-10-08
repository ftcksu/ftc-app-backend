# FTC Application Backend

FTC KSU Mobile Application Backend Built Using **Spring Boot**.

## Setup:

### Environment Variables

Please, set the needed env variables in `application.yml`. 

You can ignore the firebase admin sdk file location or create a new project if you're trying to test something with cloud messaging.

You can then start the application with the `bootRun` task in gradle.

### Admin User:

You can create an admin user by running the application with the command line args username and password, you can use gradlew if you don't have gradle installed.

Example:
```
gradle bootRun --args='--username=442100000 --password=geronimo'
```

Please, make sure that the username is an integer.

### Using the API:

You can import the api docs to any rest client such as Postman, or Insomnia from `/api/v2/api-docs`, or you can use the api directly from `/api/swagger-ui/` (Make sure to have a trailing slash after the url or it will lead to a 404 page).

## Contributing:

If you plan to contribute new features, utility functions, or extensions to the system, please first fork the project, checkout a new branch to work on, and please follow the [Conventional Commits](https://www.conventionalcommits.org/) specification. 

After you finish please send a PR and wait for our review.

## Authors:

- **Badr Alnassar** - [GitHub](https://github.com/BadrAlnassar)
- **Fahd Alshalhoub** - [GitHub](https://github.com/FahdAlShalhoub)
- **Faris Alissa** - [GitHub](https://github.com/FarisAlissa)
- **Feras Aloudah** - [GitHub](https://github.com/FerasAloudah)
- **Mohammed Alohaydab** - [GitHub](https://github.com/mohammedib)
