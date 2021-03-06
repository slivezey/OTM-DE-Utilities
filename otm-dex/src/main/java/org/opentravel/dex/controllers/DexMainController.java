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

import org.opentravel.application.common.events.OtmEventSubscriptionManager;
import org.opentravel.common.DexStyleSheetHandler;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.repository.Repository;
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
     * @param viewGroupId
     */
    void addIncludedController(DexIncludedController<?> controller, int viewGroupId);

    /**
     * Use this method for controllers that are not part of a tab or window group.
     * 
     * {@link #addIncludedController(DexIncludedController, int)}
     * 
     * @param eventManager
     */
    void addIncludedController(DexIncludedController<?> controller, OtmEventSubscriptionManager eventManager,
        int viewGroupId);

    /**
     * @return
     */
    public OtmEventSubscriptionManager getEventSubscriptionManager();

    /**
     * @return the primary member filter controller
     */
    public DexFilter<OtmLibraryMember> getMemberFilter();

    /**
     * @return the model manager used by this controller or null
     */
    public OtmModelManager getModelManager();

    /**
     * @return repository manager or null
     */
    public RepositoryManager getRepositoryManager();

    /**
     * Attempt to get the selected repository from the RepositorySelectionController
     * 
     * @return repository or null
     */
    public Repository getSelectedRepository();

    /**
     * 
     */
    public Stage getStage();

    /**
     * @return status controller used by this application
     */
    public DexStatusController getStatusController();

    /**
     * Get the DEX user settings. Load from properties file if needed.
     * 
     * @return user settings or null
     */
    public UserSettings getUserSettings();

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
     * Inform application that a model change event has occurred. Limited to model change events. Other events should be
     * thrown by their included controller.
     * 
     * @param event DexChangeEvent to publish
     */
    public void publishEvent(DexChangeEvent event);

    /**
     * Update any displays of the action manager status or queue
     * 
     * @param actionManager
     */
    public void updateActionManagerDisplay(DexActionManager actionManager);

    /**
     * Apply the currently selected style sheet to the passed stage.
     * 
     * @param stage
     */
    public void applyStyleSheet(Stage stage);

    /**
     * Set the user selected application style sheet. Saves selection in user settings and applies to stage.
     * 
     * @param selector {@link DexStyleSheetHandler.Selector#labels()}
     */
    public void setStyleSheet(String selector);

    /**
     * Get the user selected application style sheet selector label.
     * 
     * @returns selector label {@link DexStyleSheetHandler.Selector#labels()}
     */
    public String getStyleSheet();

    // /**
    // * @deprecated - use updateActionManagerDisplay(actionManager)
    // * @param queueSize
    // */
    // public void updateActionQueueSize(int queueSize);

}
