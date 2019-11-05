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

package org.yardstickframework.runners.context;

/**
 * Node docker context.
 */
public class NodeDockerContext {
    /** */
    private String dockerfilePath;

    /** */
    private String imageName;

    /** Container name prefix. */
    private String contPref;

    /**
     *
     * @return Dockerfile path.
     */
    public String getDockerfilePath() {
        return dockerfilePath;
    }

    /**
     *
     * @param dockerfilePath Dockerfile path.
     */
    public void setDockerfilePath(String dockerfilePath) {
        this.dockerfilePath = dockerfilePath;
    }

    /**
     *
     * @return Image name.
     */
    public String getImageName() {
        return imageName;
    }

    /**
     *
     * @return Container name prefix.
     */
    public String getContPref() {
        return contPref;
    }

    /**
     *
     * @param contPref Container name prefix.
     */
    public void setContPref(String contPref) {
        this.contPref = contPref;
    }

    /**
     *
     * @param imageName Image name.
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
