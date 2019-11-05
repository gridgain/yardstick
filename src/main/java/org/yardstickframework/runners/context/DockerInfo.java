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
 * Docker info.
 */
public class DockerInfo {
    /** */
    private String imageName;

    /** */
    private String imageVer;

    /** */
    private String contName;

    /** */
    private String contId;

    /**
     * Constructor.
     *
     * @param imageName Image name.
     * @param contName Container name.
     */
    public DockerInfo(String imageName, String contName) {
        this.imageName = imageName;
        this.contName = contName;
    }

    /**
     * @return Image name.
     */
    public String imageName() {
        return imageName;
    }

    /**
     * @param imageName New image name.
     */
    public void imageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * @return Image version.
     */
    public String imageVersion() {
        return imageVer;
    }

    /**
     * @param imageVer New image version.
     */
    public void imageVersion(String imageVer) {
        this.imageVer = imageVer;
    }

    /**
     * @return Cont name.
     */
    public String contName() {
        return contName;
    }

    /**
     * @param contName New cont name.
     */
    public void contName(String contName) {
        this.contName = contName;
    }

    /**
     * @return Cont id.
     */
    public String contId() {
        return contId;
    }

    /**
     * @param contId New cont id.
     */
    public void contId(String contId) {
        this.contId = contId;
    }
}
