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

package org.yardstickframework.impl;

import org.reflections.*;
import org.yardstickframework.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import static org.yardstickframework.BenchmarkUtils.*;

/**
 * Benchmarks loader.
 */
public class BenchmarkLoader {
    /** Benchmark configuration. */
    private BenchmarkConfiguration cfg;

    /** */
    private Reflections refs;

    /**
     * @param cfg Config.
     * @throws Exception If failed.
     */
    public void initialize(BenchmarkConfiguration cfg) throws Exception {
        this.cfg = cfg;

        if (cfg.output() == null)
            cfg.output(System.out);

        if (cfg.error() == null)
            cfg.error(System.err);

        Properties props = new Properties();

        try (FileInputStream is = new FileInputStream(cfg.propertiesFileName())) {
            props.load(is);
        }
        catch (FileNotFoundException ignore) {
            println(cfg, "Framework configuration file was not found: " + cfg.propertiesFileName());
        }

        // Try init packages first.
        if (props.getProperty("BENCHMARK_PACKAGES") != null) {
            if (cfg.packages().isEmpty()) {
                String[] packagesArr = props.getProperty("BENCHMARK_PACKAGES").split(",");

                List<String> packagesLst = new ArrayList<>(packagesArr.length);

                for (String pck : packagesArr) {
                    pck = pck.trim();

                    if (!pck.isEmpty())
                        packagesLst.add(pck);
                }

                cfg.packages(packagesLst);
            }
        }

        Collection<String> allPackages = new LinkedHashSet<>();

        if (cfg.packages() != null)
            allPackages.addAll(cfg.packages());

        refs = new Reflections(allPackages.toArray(new String[allPackages.size()]));

        Map<String, String> customProps = new HashMap<>(props.size());

        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = (String)entry.getKey();
            String val = (String)entry.getValue();

            switch (key) {
                case "BENCHMARK_DEFAULT_PROBES":
                    if (cfg.defaultProbeClassNames().isEmpty()) {
                        String[] clsArr = val.split(",");

                        List<String> probes = new ArrayList<>(clsArr.length);

                        for (String clsName : clsArr) {
                            clsName = clsName.trim();

                            if (!clsName.isEmpty())
                                probes.add(clsName);
                        }

                        cfg.defaultProbeClassNames(probes);
                    }
                    break;

                case "BENCHMARK_WRITER":
                    if (cfg.probeWriterClassName() == null) {
                        String writerClsName = val.trim();

                        if (!writerClsName.isEmpty())
                            cfg.probeWriterClassName(writerClsName);
                    }

                    break;

                default:
                    customProps.put(key, val);

                    break;
            }
        }

        cfg.customProperties(customProps);
    }

    /**
     * Loads benchmark probes.
     *
     * @return Loaded probes.
     * @throws Exception If failed.
     */
    public Collection<BenchmarkProbe> loadProbes() throws Exception {
        // Init probes.
        List<BenchmarkProbe> probes = new ArrayList<>(cfg.defaultProbeClassNames().size());

        for (String probeClsName : cfg.defaultProbeClassNames()) {
            BenchmarkProbe probe = loadClass(BenchmarkProbe.class, probeClsName);

            if (probe != null)
                probes.add(probe);
            else
                println(cfg, "Failed to load probe: " + probeClsName);
        }

        return probes;
    }

    /**
     * Loads specified class.
     *
     * @param cls Class to load.
     * @param name Simple or fully-qualified class name to load.
     * @return Loaded class.
     * @throws Exception If class could not be loaded.
     */
    public <T> T loadClass(Class<T> cls, String name) throws Exception {
        Collection<Class<? extends T>> classes = refs.getSubTypesOf(cls);

        Map<String, String> simpleNames = new HashMap<>(classes.size());

        Map<String, Class<? extends T>> fqNames = new HashMap<>(classes.size());

        List<String> duplicates = null;

        for (Class<? extends T> c : classes) {
            if (!Modifier.isAbstract(c.getModifiers())) {
                if (!simpleNames.containsKey(c.getSimpleName()))
                    simpleNames.put(c.getSimpleName(), c.getName());
                else {
                    if (duplicates == null)
                        duplicates = new ArrayList<>();

                    duplicates.add(c.getName());
                }

                fqNames.put(c.getName(), c);
            }
        }

        if (duplicates != null) {
            println(cfg, "Duplicate simple class names detected (use fully-qualified names for execution): ");

            for (String duplicate : duplicates)
                println(cfg, "\t" + duplicate);
        }

        String fqName = simpleNames.get(name);

        if (fqName == null)
            fqName = name;

        Class<? extends T> implCls = fqNames.get(fqName);

        if (implCls == null)
            return null;

        return implCls.newInstance();
    }
}
