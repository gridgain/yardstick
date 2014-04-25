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
The file is specified via `--config` program argument.
2. Run `bin/benchmark-run.sh` script with specified benchmark server name.

For example:
`bin/benchmark-run.sh --config benchmark.properties -n EchoServer`

Additional parameters of the script are listed below.

### Starting benchmark
After remote servers are started, the benchmark should be run. Procedure is almost the same - the only
difference is that the benchmark class name should be specified instead of server class name.

For example:
`bin/benchmark-run.sh --config benchmark.properties -n EchoServerBenchmark`

The following properties can be defined in `benchmark.properties` file:

* `benchmark.default.probes` - the list of default probes
* `benchmark.packages` - packages where the specified benchmark is searched by reflection mechanism
* `benchmark.writer` - probe point writer class name

`benchmark-run.sh` script accepts the following arguments:

* `--config <path>` - framework configuration file path
* `--name <name>` - benchmark name (required)
* `--packages <list>` - comma separated list of packages for benchmarks
* `--probes <list>` - comma separated list of probes for benchmarks
* `--writer <name>` - Probe point writer class name
* `--threads <num>` - thread count (set to 'cpus * 2')
* `--duration <time>` - test duration, in seconds
* `--warmup <time>` - warmup time, in seconds
* `--shutdown` - flag indicating whether to invoke shutdown hook or not

### Starting remote servers and benchmark by one script
Several benchmarks with different configurations can be run with remote servers by using `bin\benchmark-remote-run.sh`
script. To run the script some environment variables should be defined:

* `BHOSTS` - comma-separated list of hosts where to start servers, one server per host
* `BSERVER` - class name of the server to be started
* `BDRIVER` - class name of the benchmark to be started
* `BCONFIG` - benchmark configuration to be passed to the servers and to the benchmarks

Yardstick is shipped with `bin\benchmark-remote-run-example.sh` script as an example of the script
that defines the set of benchmark configurations and needed environment variables.

## JFreeChart graphs
Yardstick goes with the script `jfreechart-graph-plotter-run.sh` that builds JFreeChart graphs using probe points.

`jfreechart-graph-plotter-run.sh` script accepts the following arguments:

* `--input` - input folder where files with probe points are located (required)
* `--chartColumns` - number of columns that the charts are displayed in on the resulted page
* `--compoundChart` - mode in which the same probe metrics are displayed on one chart. For example,
throughput plots from different benchmark runs are displayed on one chart.

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