/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.application.common;

import javafx.scene.image.Image;

/**
 * Enumeration representing the type of message displayed in the status BAR of an application.
 */
@SuppressWarnings("squid:UnusedPrivateMethod") // Invalid finding
public enum StatusType {

    INFO(Images.infoIcon), WARNING(Images.warningIcon), ERROR(Images.errorIcon);

    private Image icon;

    /**
     * Constructor that specifies the icon for this status type.
     * 
     * @param icon the image to display for messages of this type
     */
    private StatusType(Image icon) {
        this.icon = icon;
    }

    /**
     * Returns the icon image associated with this status type.
     * 
     * @return Image
     */
    public Image getIcon() {
        return icon;
    }

}
