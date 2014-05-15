## Yardstick
Yardstick is a framework for benchmarks writing. The framework goes with a default set of probes that collect some
metrics during benchmark execution, for example the probe that measures throughput and latency, the probe that gathers
'vmstat' statistics and etc. As a result Yardstick produces files with probe points.
See [Yardstick GridGain](https://github.com/gridgain/yardstick-gridgain) as an example of Yardstick framework usage.

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
to run benchmark on, optional username to log in on remote servers, comma-separated list of run configurations.
2. Run `bin/benchmark-servers-start.sh` script with specified benchmark properties file. The script will take the first
configuration from the comma-separated list of configurations.

For example:
`bin/benchmark-servers-start.sh config/benchmark.properties`

Remote servers logs can be found in `logs` folder.

### Starting benchmark
After remote servers are started, the benchmark driver should be run. Procedure is almost the same - the only
difference is that the `bin/benchmark-run.sh` script should be used. The script will take the first
configuration from the comma-separated list of configurations.

For example:
`bin/benchmark-run.sh config/benchmark.properties`

### Stopping remote servers
To stop remote servers after the benchmark finished his job `bin/benchmark-servers-stop.sh` script should be run.

For example:
`bin/benchmark-servers-stop.sh config/benchmark.properties`

### Starting remote servers and a benchmark at once
If there's need to run many benchmarks or some benchmark with different configurations then this can be done
by using `bin/benchmark-run-all.sh` script. This script starts benchmark servers on remote machines, 
runs benchmark driver and stops the servers on remote machines after the driver finished his job. 
This procedure is performed for all configurations defined in run properties file.

### Properties and command line arguments

The following properties can be defined in benchmark properties file:

* `BENCHMARK_DEFAULT_PROBES` - list of default probes
* `BENCHMARK_PACKAGES` - packages where the specified benchmark is searched by reflection mechanism
* `BENCHMARK_WRITER` - probe point writer class name
* `HOSTS` - comma-separated list of hosts where to start servers, one server per host
* `CONFIGS` - comma-separated list of benchmark run configurations which are passed to the servers and to the benchmarks

The following properties can be defined in the benchmark configuration:

* `-cfg <path>` or `--config <path>` - framework configuration file path
* `-dn <name>` or `--driverName <name>` - driver name (required for the driver)
* `-sn <name>` or `--serverName <name>` - server name (required for the server)
* `-p <list>` or `--packages <list>` - comma separated list of packages for benchmarks
* `-pr <list>` or `--probes <list>` - comma separated list of probes for benchmarks
* `-wr <name>` or `--writer <name>` - probe point writer class name
* `-t <num>` or `--threads <num>` - thread count (set to 'cpus * 2')
* `-d <time>` or `--duration <time>` - test duration, in seconds
* `-w <time>` or `--warmup <time>` - warmup time, in seconds
* `-sh` or `--shutdown` - flag indicating whether to invoke shutdown hook or not
* `-of <path>` or `--outputFolder <path>` - output folder for benchmark results, current folder is used by default

For example if we need to run EchoServer server on localhost and EchoServerBenchmark benchmark on localhost, 
the test should be 20 seconds then the following configuration should be specified in run properties file:

* `HOSTS=localhost`
* `CONFIGS="--duration 20 -sn EchoServer -dn EchoServerBenchmark"`

## JFreeChart graphs
Yardstick goes with the script `jfreechart-graph-gen.sh` that builds JFreeChart graphs using probe points.

`jfreechart-graph-gen.sh` script accepts the following arguments:

* `-i <list>` or `--inputFolders <list>` - comma-separated list of input folders which contains folders 
with probe results files (required)
* `-cc <num>` or `--chartColumns <num>` - number of columns that the charts are displayed in on the resulted page
* `-gm <mode>` or `--generationMode <mode>` - mode that defines the way how different benchmark runs are compared 
with each other

Generation modes:

* `STANDARD` - all benchmark runs are separate. Every chart contains one graph. It's default mode
* `COMPARISON` - in this mode benchmark runs from one folder are compared with benchmark runs from another folder, 
first with first, second with second etc. Many graphs are displayed on one chart
* `COMPOUND` - in this mode all benchmark runs are compared with each other. Many graphs are displayed on one chart

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
