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

package org.yardstickframework.report.jfreechart;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Tests plot values correction.
 */
public class JFreeChartGraphCorrectValuesTest {
    /** */
    private List<double[]> input;

    /** */
    private List<double[]> expected;

    /**
     *
     */
    @After
    public void check() {
        JFreeChartGraphPlotter.PlotSeries series = new JFreeChartGraphPlotter.PlotSeries("test");

        series.rawData().addAll(input);

        series.correctValues();

        List<double[]> actual = series.rawData();

        assertEquals("Size", expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++)
            assertArrayEquals("Arrays", expected.get(i), actual.get(i), 0.001);
    }

    /**
     *
     */
    @Test
    public void testEmpty() {
        input = Collections.emptyList();

        expected = Collections.emptyList();
    }

    /**
     *
     */
    @Test
    public void testCorrectOne() {
        input = Arrays.asList(
            new double[] {0, 10}
        );

        expected = input;
    }

    /**
     *
     */
    @Test
    public void testCorrectMany() {
        int i = 0;

        input = Arrays.asList(
            new double[] {i++, 10},
            new double[] {i++, 15},
            new double[] {i++, 9},
            new double[] {i++, 13},
            new double[] {i++, 3},
            new double[] {i++, 5},
            new double[] {i++, 8},
            new double[] {i, 17}
        );

        expected = input;
    }

    /**
     *
     */
    @Test
    public void testUncorrectedNaN1() {
        input = Arrays.asList(
            new double[] {0, Double.NaN}
        );

        expected = input;
    }

    /**
     *
     */
    @Test
    public void testUncorrectedNaN2() {
        int i = 0;

        input = Arrays.asList(
            new double[] {i++, Double.NaN},
            new double[] {i, Double.NaN}
        );

        int j = 0;

        expected = Arrays.asList(
            new double[] {j++, Double.NaN},
            new double[] {j, Double.NaN}
        );
    }

    /**
     *
     */
    @Test
    public void testNaN1() {
        int i = 0;

        input = Arrays.asList(
            new double[] {i++, Double.NaN},
            new double[] {i++, 8},
            new double[] {i, 9}
        );

        int j = 0;

        expected = Arrays.asList(
            new double[] {j++, 8},
            new double[] {j++, 8},
            new double[] {j, 9}
        );
    }

    /**
     *
     */
    @Test
    public void testNaN2() {
        int i = 0;

        input = Arrays.asList(
            new double[] {i++, Double.NaN},
            new double[] {i++, Double.NaN},
            new double[] {i++, 8},
            new double[] {i, 9}
        );

        int j = 0;

        expected = Arrays.asList(
            new double[] {j++, 8},
            new double[] {j++, 8},
            new double[] {j++, 8},
            new double[] {j, 9}
        );
    }

    /**
     *
     */
    @Test
    public void testNaN3() {
        int i = 0;

        input = Arrays.asList(
            new double[] {i++, 6},
            new double[] {i++, Double.NaN},
            new double[] {i++, Double.NaN},
            new double[] {i++, 8},
            new double[] {i, 9}
        );

        int j = 0;

        expected = Arrays.asList(
            new double[] {j++, 6},
            new double[] {j++, 7},
            new double[] {j++, 8},
            new double[] {j++, 8},
            new double[] {j, 9}
        );
    }

    /**
     *
     */
    @Test
    public void testNaN4() {
        int i = 0;

        input = Arrays.asList(
            new double[] {i++, 6},
            new double[] {i++, Double.NaN},
            new double[] {i++, 8},
            new double[] {i++, Double.NaN},
            new double[] {i, 10}
        );

        int j = 0;

        expected = Arrays.asList(
            new double[] {j++, 6},
            new double[] {j++, 7},
            new double[] {j++, 8},
            new double[] {j++, 9},
            new double[] {j, 10}
        );
    }

    /**
     *
     */
    @Test
    public void testNaN5() {
        int i = 0;

        input = Arrays.asList(
            new double[] {i++, 6},
            new double[] {i, Double.NaN}
        );

        int j = 0;

        expected = Arrays.asList(
            new double[] {j++, 6},
            new double[] {j, 6}
        );
    }

    /**
     *
     */
    @Test
    public void testNaN6() {
        int i = 0;

        input = Arrays.asList(
            new double[] {i++, 6},
            new double[] {i++, Double.NaN},
            new double[] {i, Double.NaN}
        );

        int j = 0;

        expected = Arrays.asList(
            new double[] {j++, 6},
            new double[] {j++, 6},
            new double[] {j, 6}
        );
    }

    /**
     *
     */
    @Test
    public void testPositiveInf() {
        int i = 0;

        input = Arrays.asList(
            new double[] {i++, Double.POSITIVE_INFINITY},
            new double[] {i++, 8},
            new double[] {i, 9}
        );

        int j = 0;

        expected = Arrays.asList(
            new double[] {j++, 8},
            new double[] {j++, 8},
            new double[] {j, 9}
        );
    }

    /**
     *
     */
    @Test
    public void testNegativeInf() {
        int i = 0;

        input = Arrays.asList(
            new double[] {i++, Double.NEGATIVE_INFINITY},
            new double[] {i++, 8},
            new double[] {i, 9}
        );

        int j = 0;

        expected = Arrays.asList(
            new double[] {j++, 8},
            new double[] {j++, 8},
            new double[] {j, 9}
        );
    }
}
