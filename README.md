# emersim
Simulating emergency services for our 3rd year group project in Java.

# Running the build
### Building the Jar executable
```shell
$ ant create_jar
```

### Running Tests
```shell
$ ant run_all_tests
```

Alternatively tests may be run in package groups:
e.g.
```shell
$ ant engine_tests
$ ant gui_tests
$ ant jmva_tests
$ ant spatial_queue_tests
$ ant spatial_queue_commit_tests
```

#Setting up git hooks

To install our version-controlled git hooks, please run the following script:

`./.init-hooks`

