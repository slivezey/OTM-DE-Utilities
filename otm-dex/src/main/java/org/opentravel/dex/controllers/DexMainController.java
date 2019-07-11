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

package org.opentravel.dex.controllers;

import org.opentravel.model.OtmModelManager;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.repository.RepositoryManager;

import javafx.stage.Stage;

/**
 * Abstract interface for main Otm-DE FX controllers. Main controllers:
 * <ul>
 * <li>may contain included controllers injected by FXML
 * <li>do <b>not</b> implement business logic or user interaction controls
 * <li>provide access to menu bars, status controllers and system resources such as model managers and image managers.
 * </ul>
 * These <i>may not</i> be the top level controller; for example Tab controllers are main controllers.
 * 
 * @author dmh
 *
 */
public interface DexMainController extends DexController {

    /**
     * Add the included controller to the list of controllers. The list is used for iterative processes such as clear
     * and refresh. The controller is tested to assure not null and configured with this controller as a parameter.
     * 
     * @see DexIncludedController#configure(DexMainController)
     * 
     * @param controller
     */
    void addIncludedController(DexIncludedController<?> controller);

    // /**
    // * @return the image manager used by this controller
    // */
    // public ImageManager getImageManager();

    /**
     * @return the model manager used by this controller or null
     */
    public OtmModelManager getModelManager();

    /**
     * @return repository manager or null
     */
    public RepositoryManager getRepositoryManager();

    /**
     * 
     */
    public Stage getStage();

    /**
     * @return status controller used by this application
     */
    public DexStatusController getStatusController();

    /**
     * Inform the user of an exception.
     * 
     * @param e localized message and possible cause
     * @param title applied to dialog box
     */
    void postError(Exception e, String title);

    /**
     * Update the progress indicator displayed value.
     * 
     * @param percentDone
     */
    public void postProgress(double percentDone);

    /**
     * Put the status string into the status label.
     * 
     * @param string
     */
    public void postStatus(String string);

    /**
     * @param queueSize
     */
    void updateActionQueueSize(int queueSize);

    /**
     * @return
     */
    UserSettings getUserSettings();

}