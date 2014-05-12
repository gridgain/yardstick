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
To run remote servers the following should be done:

1. Prepare benchmark properties for the benchmark execution. These properties will contain list of remote servers
to run benchmark on, optional username to log in on remote servers, class names for benchmark server and driver.
2. Run `bin/benchmark-servers-start.sh` script with specified benchmark properties file.

For example:
`bin/benchmark-servers-start.sh config/benchmark.properties`

### Starting benchmark
After remote servers are started, the benchmark should be run. Procedure is almost the same - the only
difference is that the `bin/benchmark-run.sh` script should be used.

For example:
`bin/benchmark-run.sh config/benchmark.properties`

### Stopping remote servers
To stop remote servers after the benchmark finished his job `bin/benchmark-servers-stop.sh` script should be run.

For example:
`bin/benchmark-servers-stop.sh config/benchmark.properties`

### Properties and command line arguments

The following properties can be defined in benchmark properties file:

* `BENCHMARK_DEFAULT_PROBES` - list of default probes
* `BENCHMARK_PACKAGES` - packages where the specified benchmark is searched by reflection mechanism
* `BENCHMARK_WRITER` - probe point writer class name
* `HOSTS` - comma-separated list of hosts where to start servers, one server per host
* `SERVER` - class name of the server to be started
* `DRIVER` - class name of the benchmark to be started
* `CONFIG` - benchmark configuration to be passed to the servers and to the benchmarks

The following properties can be defined in the benchmark configuration:

* `--config <path>` - framework configuration file path
* `--name <name>` - benchmark name (required)
* `--packages <list>` - comma separated list of packages for benchmarks
* `--probes <list>` - comma separated list of probes for benchmarks
* `--writer <name>` - Probe point writer class name
* `--threads <num>` - thread count (set to 'cpus * 2')
* `--duration <time>` - test duration, in seconds
* `--warmup <time>` - warmup time, in seconds
* `--shutdown` - flag indicating whether to invoke shutdown hook or not

## JFreeChart graphs
Yardstick goes with the script `jfreechart-graph-plotter-run.sh` that builds JFreeChart graphs using probe points.

`jfreechart-graph-plotter-run.sh` script accepts the following arguments:

* `--inputFolders` - comma-separated list of Input folders which contains folders with probe results files (required)
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

Copy and paste one more code snippet to add the scripts that run Yardstick benchmarks to your project:

```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-dependency-plugin</artifactId>
            <executions>
                <execution>
                    <id>unpack</id>
                    <phase>package</phase>
                    <goals>
                        <goal>unpack</goal>
                    </goals>
                    <configuration>
                        <artifactItems>
                            <artifactItem>
                                <groupId>org.yardstick</groupId>
                                <artifactId>yardstick</artifactId>
                                <version>${yardstick.version}</version>
                                <type>zip</type>
                                <classifier>resources</classifier>
                                <outputDirectory>${basedir}</outputDirectory>
                            </artifactItem>
                        </artifactItems>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

The scripts will be unpacked to `bin` folder by command `mvn package`.

## Issues
Use GitHub [issues](https://github.com/gridgain/yardstick/issues) to file bugs.

## License
Yardstick is available under [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) license.
