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
@SuppressWarnings("UnusedDeclaration")
public class JFreeChartGraphPlotterArguments {
    /** */
    @Parameter(names = {"-h", "--help"}, description = "Print help message", help = true, hidden = true)
    private boolean help;

    /** */
    @Parameter(names = {"-i", "--inputFolders"},
        description = "Comma-separated list of input folders which contains folders with probe results files (required)")
    private String inputFolders;

    /** */
    @SuppressWarnings("FieldCanBeLocal")
    @Parameter(names = {"-cc", "--chartColumns"},
        description = "Number of columns that the charts are displayed in on the resulted page")
    private int chartCols = 3;

    @Parameter(names = {"-gm", "--generationMode"},
        description = "Mode that defines the way how different benchmark runs are compared with each other")
    private JFreeChartGenerationMode genMode;

    /**
     * @return List of input folders.
     */
    public String inputFolders() {
        return inputFolders;
    }

    /**
     * @return Chart columns number.
     */
    public int chartColumns() {
        return chartCols;
    }

    /**
     * @return Help.
     */
    public boolean help() {
        return help;
    }

    /**
     * @return Generation mode.
     */
    public JFreeChartGenerationMode generationMode() {
        return genMode;
    }
}
