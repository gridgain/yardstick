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

package org.yardstick.report.jfreechart;

import com.beust.jcommander.*;

/**
 * Graph plotter arguments.
 */
public class JFreeChartGraphPlotterArguments {
    /** Help message flag. */
    @Parameter(names = {"-h", "--help"}, description = "Print help message")
    private boolean help;

    /** Input file name. */
    @Parameter(names = {"-i", "--input"}, description = "Input file name")
    private String inputFileName;

    /** Output file name. */
    @Parameter(names = {"-o", "--output"}, description = "Output file name")
    private String outputFileName;

    /** Split graphs flag. */
    @Parameter(names = {"-s", "--split"}, description = "Split graphs flag. Set to render each series in separate graph.")
    private boolean splitGraphs;

    /**
     * @return Input file name.
     */
    public String inputFileName() {
        return inputFileName;
    }

    /**
     * @param inputFileName Input file name.
     */
    public void inputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    /**
     * @return Output file name.
     */
    public String outputFileName() {
        return outputFileName;
    }

    /**
     * @param outputFileName Output file name.
     */
    public void outputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    /**
     * @return Split graphs.
     */
    public boolean splitGraphs() {
        return splitGraphs;
    }

    /**
     * @param splitGraphs Split graphs.
     */
    public void splitGraphs(boolean splitGraphs) {
        this.splitGraphs = splitGraphs;
    }

    /**
     * @return Help.
     */
    public boolean help() {
        return help;
    }

    /**
     * @param help Help.
     */
    public void help(boolean help) {
        this.help = help;
    }
}
