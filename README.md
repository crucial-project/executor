# Crucial Executor

Executor abstractions for the [CRUCIAL](http://github.com/crucial-project/crucial)
project.
This project includes the abstraction of `CloudThread` that allows
running Java `Runnable`s in the cloud on FaaS platforms (currently AWS Lambda).

The `CloudThread` class requires some configuration depending on your
AWS Lambda deployment.
See the [examples](https://github.com/crucial-project/examples) for more
information.

The basic usage is that of Java threads:

```java
Thread t = new CloudThread(new MyRunnable());
t.start();
t.join();
```

You can simulate the execution with a local thread with `t.setLocal(true)`
before starting the `CloudThread`.

# ServerlessExecutorService
