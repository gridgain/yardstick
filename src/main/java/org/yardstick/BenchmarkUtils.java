/*
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.yardstick;

import com.beust.jcommander.*;

/**
 * Benchmark utility methods.
 */
public class BenchmarkUtils {
    /**
     * Initializes instance of {@code JCommander}.
     *
     * @param a Arguments.
     * @param args Custom arguments that should be filled with parsed arguments.
     * @param programName Program name.
     * @return Parses input method parameters and returns {@link JCommander} instance.
     */
    public static JCommander jcommander(String[] a, Object args, String programName) {
        JCommander jCommander = new JCommander();

        jCommander.setAcceptUnknownOptions(true);
        jCommander.setProgramName(programName);
        jCommander.addObject(args);

        jCommander.parse(a);

        return jCommander;
    }

    /**
     * Prints usage string to output.
     *
     * @param args JCommander arguments.
     * @return Usage string.
     */
    public static String usage(Object args) {
        CompositeParameters cp = new CompositeParameters();

        cp.benchmarkArgs = args == null ? new Object() : args;

        JCommander jCommander = new JCommander();

        jCommander.setAcceptUnknownOptions(true);
        jCommander.addObject(cp);

        StringBuilder sb = new StringBuilder();

        jCommander.usage(sb);

        return sb.toString();
    }

    /** */
    private static class CompositeParameters {
        @ParametersDelegate
        private BenchmarkConfiguration cfg = new BenchmarkConfiguration();

        @ParametersDelegate
        private Object benchmarkArgs;
    }

    /**
     * Ensure static class.
     */
    private BenchmarkUtils() {
        // No-op.
    }
}
