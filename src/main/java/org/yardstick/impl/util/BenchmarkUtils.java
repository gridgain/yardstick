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

package org.yardstick.impl.util;

import com.beust.jcommander.*;
import org.yardstick.*;

import java.lang.annotation.*;
import java.lang.reflect.*;

/**
 * Utils.
 */
public class BenchmarkUtils {
    /**
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
     * @param cfg Benchmark configuration.
     * @throws Exception If failed.
     */
    public static void showUsage(BenchmarkConfiguration cfg) throws Exception {
        CompositeParameters cp = new CompositeParameters();

        Object args = cfg.benchmark() == null ? null : arguments(cfg.benchmark(), true);

        cp.benchmarkArgs = args == null ? new Object() : args;

        JCommander jCommander = new JCommander();

        jCommander.setAcceptUnknownOptions(true);
        jCommander.addObject(cp);

        StringBuilder sb = new StringBuilder();

        jCommander.usage(sb);

        cfg.output().println(sb.toString());
    }

    /**
     * Finds the first field that is annotated to be included to usage string.
     *
     * @param target Object to be scanned for arguments.
     * @param newInstance Flag indicating whether to return a new instance of arguments or the current value.
     * @return Object to be included to usage string.
     */
    public static Object arguments(Object target, boolean newInstance) {
        Class c = target.getClass();

        while (c != null) {
            for (Field field : c.getDeclaredFields()) {
                field.setAccessible(true);

                Annotation ann = field.getAnnotation(BenchmarkIncludeToUsage.class);

                if (ann != null) {
                    try {
                        return newInstance ? field.getType().newInstance() : field.get(target);
                    }
                    catch (Exception ignore) {
                        // No-op.
                    }
                }
            }

            c = c.getSuperclass();
        }

        return null;
    }

    /**
     * Returns short description of arguments.
     *
     * @param target Object to be scanned for the method that returns short string.
     * @return Returns short description of arguments.
     */
    public static String toShortString(Object target) {
        if (target == null)
            return "";

        Method[] methods = target.getClass().getMethods();

        for (Method method : methods) {
            BenchmarkToShortString ann = method.getAnnotation(BenchmarkToShortString.class);
            if (ann != null) {
                try {
                    Object res = method.invoke(target);

                    return res instanceof String ? (String) res : "";
                } catch (Exception ignore) {
                    // No-op.
                }
            }
        }

        return "";
    }

    /** */
    @SuppressWarnings("UnusedDeclaration")
    private static class CompositeParameters {
        @ParametersDelegate
        /** */
        private BenchmarkConfiguration cfg = new BenchmarkConfiguration();

        @ParametersDelegate
        /** */
        private Object benchmarkArgs;
    }

    /** */
    private BenchmarkUtils() {
        // No-op.
    }
}
