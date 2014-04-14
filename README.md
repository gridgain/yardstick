## Yardstick
Yardstick is a framework for benchmarks writing. The framework goes with a default set of probes that collect some
metrics during benchmark execution, for example the probe that measures throughput and latency, the probe that gathers
'vmstat' statistics and etc. As a result Yardstick produces files with probe points.

## How to write your own benchmark using Yardstick
There are two main interfaces: BenchmarkServer and BenchmarkDriver that should be implemented.
BenchmarkDriver is a instance of the benchmark that performs some operation that needs to be benchmarked.
BenchmarkServer is a server that the BenchmarkDriver communicates with.

For example if we want to measure the time of the message delivery then BenchmarkDriver should establish
connection with BenchmarkServer and implement ping command. BenchmarkServer should receive ping command
from BenchmarkDriver and send the message back.
That's it, Yardstick benchmark will measure latency and other metrics for you.

## How to run Yardstick benchmarks

### Starting remote servers
To run a remote server the following should be done:

1. Some configuration parameters can be defined in `benchmark.properties`.
The file is specified via `-c` program argument.
2. Run `bin/benchmark-run.sh` script with specified benchmark server name. For example:
`bin/benchmark-run.sh -c benchmark.properties -n EchoServer`

Additional parameters of the script are listed below.

### Starting benchmark
After remote servers are started, the benchmark should be run. Procedure is almost the same - the only
difference is that the benchmark class name should be specified instead of server clsss name.

For example:
`bin/benchmark-run.sh -c benchmark.properties -n EchoServerBenchmark`

The following properties can be defined in `benchmark.properties` file:

* `benchmark.default.probes` - the list of default probes
* `benchmark.packages` - packages where the specified benchmark is searched by reflection mechanism.

`benchmark-run.sh` script accepts the following arguments:

* `-c <path>` - framework configuration file path
* `-n <name>` - benchmark name (required)
* `-p <list>` - comma separated list of packages for benchmarks
* `--probes <list>` - comma separated list of probes for benchmarks
* `--writer <name>` - Probe point writer class name
* `-t <num>` - thread count (set to 'cpus * 2')
* `-d <time>` - test duration, in seconds
* `-w <time>` - warmup time, in seconds
* `-sh` - flag indicating whether to invoke shutdown hook or not

## JFreeChart graphs
Yardstick goes with the script `jfreechart-graph-plotter-run.sh` that builds JFreeChart graphs using probe points.

`jfreechart-graph-plotter-run.sh` script accepts the following arguments:

* `-i` - input folder where files with probe points are located (required)
* `-o` - output folder, if it's not defined then the input folder is used

As a result the script produces 'png' images with graphs and html pages where all graphs belonging to the one test run
are located.

## Maven Install
The easiest way to get started with Yardstick in your project is to use Maven dependency management:

```xml
<dependency>
    <groupId>org.yardstick</groupId>
    <artifactId>yardstick</artifactId>
    <version>${yardstick.version}</version>
</dependency>
```

You can copy and paste this snippet into your Maven POM file. Make sure to replace version with the one you need.

## Issues
Use GitHub [issues](https://github.com/gridgain/yardstick/issues) to file bugs.

## License
Yardstick is available under [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) license.