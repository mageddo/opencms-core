/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/Attic/CmsContainerpageHandler.java,v $
 * Date   : $Date: 2010/05/18 14:09:26 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.containerpage.client;

import org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement;
import org.opencms.ade.containerpage.client.draganddrop.CmsDragSubcontainer;
import org.opencms.ade.containerpage.client.draganddrop.I_CmsDragContainerElement;
import org.opencms.ade.containerpage.client.draganddrop.I_CmsDragTargetContainer;
import org.opencms.ade.containerpage.client.ui.CmsContentEditorDialog;
import org.opencms.ade.containerpage.client.ui.CmsLeavePageDialog;
import org.opencms.ade.containerpage.client.ui.CmsSubcontainerEditor;
import org.opencms.ade.containerpage.client.ui.I_CmsToolbarButton;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.publish.client.CmsPublishDialog;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.I_CmsFormHandler;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsMapUtil;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The container-page handler.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.9 $
 * 
 * @since 8.0.0
 */
public class CmsContainerpageHandler {

    /** The container-page controller. */
    protected CmsContainerpageController m_controller;

    /** The container-page editor. */
    protected CmsContainerpageEditor m_editor;

    /** The currently active tool-bar button. */
    private I_CmsToolbarButton m_activeButton;

    /**
     * Constructor.<p>
     * 
     * @param controller the container-page controller
     * @param editor the container-page editor
     */
    public CmsContainerpageHandler(CmsContainerpageController controller, CmsContainerpageEditor editor) {

        m_controller = controller;

        m_editor = editor;
    }

    /**
     * Activates the selection button.<p>
     */
    public void activateSelection() {

        m_editor.getSelection().setActive(true);
    }

    /**
     * Adds the given list item widget to the favorite list widget.<p>
     * 
     * @param listItem the list item
     */
    public void addToFavorites(CmsListItem listItem) {

        m_editor.getClipboard().addToFavorites(listItem);
    }

    /**
     * Adds the given list item widget to the recent list widget.<p>
     * 
     * @param listItem the list item
     */
    public void addToRecent(CmsListItem listItem) {

        m_editor.getClipboard().addToRecent(listItem);
    }

    /**
     * De-activates the current button.<p> 
     */
    public void deactivateCurrentButton() {

        if (m_activeButton != null) {
            m_activeButton.setActive(false);
            m_activeButton = null;
        }

    }

    /**
     * Starts the property editor for the given container element.<p>
     * 
     * @param elementWidget the container element widget for which the properties should be edited 
     */
    public void editProperties(final CmsDragContainerElement elementWidget) {

        final String id = elementWidget.getClientId();

        m_controller.getElement(id, new I_CmsSimpleCallback<CmsContainerElement>() {

            public void execute(final CmsContainerElement elementBean) {

                Map<String, String> properties = elementBean.getProperties();
                Map<String, CmsXmlContentProperty> propertyConfig = elementBean.getPropertyConfig();
                if (propertyConfig.size() == 0) {
                    String message = Messages.get().key(Messages.GUI_NO_PROPERTIES_0);
                    String title = Messages.get().key(Messages.GUI_NO_PROPERTIES_TITLE_0);
                    (new CmsAlertDialog(title, message)).center();
                    return;
                }
                I_CmsFormHandler formHandler = new I_CmsFormHandler() {

                    public void onSubmitForm(Map<String, String> fieldValues) {

                        m_controller.reloadElementWithProperties(
                            elementWidget,
                            elementBean.getClientId(),
                            CmsMapUtil.removeNullEntries(fieldValues));
                    }
                };
                String title = Messages.get().key(Messages.GUI_PROPERTY_DIALOG_TITLE_0);
                CmsFormDialog dialog = new CmsFormDialog(title);
                dialog.setWidth("450px");
                CmsForm form = dialog.getForm();
                Map<String, I_CmsFormField> formFields = CmsBasicFormField.createFields(propertyConfig.values());
                for (I_CmsFormField field : formFields.values()) {
                    form.addField(field, properties.get(field.getId()));
                }
                dialog.setFormHandler(formHandler);
                dialog.center();
            }

            public void onError(String message) {

                // can never happen 

            }

        });

    }

    /**
     * Enables the drag handler on the given element.<p>
     * 
     * @param element the element
     */
    public void enableDragHandler(I_CmsDragContainerElement<I_CmsDragTargetContainer> element) {

        m_controller.getContainerpageUtil().enableDragHandler(element);
    }

    /**
     * Enables the save and reset button of the tool-bar.<p>
     * 
     * @param enable <code>true</code> to enable
     */
    public void enableSaveReset(boolean enable) {

        if (enable) {
            m_editor.getSave().enable();
            m_editor.getReset().enable();
        } else {
            m_editor.getSave().disable(Messages.get().key(Messages.GUI_BUTTON_SAVE_DISABLED_0));
            m_editor.getReset().disable(Messages.get().key(Messages.GUI_BUTTON_RESET_DISABLED_0));
        }
    }

    /**
     * Leaves the current page and opens the sitemap.<p>�
     */
    public void gotoSitemap() {

        if (m_controller.hasPageChanged()) {
            CmsLeavePageDialog dialog = new CmsLeavePageDialog(
                m_controller.getData().getSitemapUri(),
                m_controller,
                this);
            dialog.center();
        } else {
            m_controller.leaveUnsaved(m_controller.getData().getSitemapUri());
        }
    }

    /**
     * Loads the favorite list from the server and adds it's items to the clip-board.<p>
     */
    public void loadFavorites() {

        m_controller.loadFavorites(new I_CmsSimpleCallback<List<CmsContainerElement>>() {

            /**
             * Generating the list item widgets and inserting them into the favorite list.<p> 
             * 
             * @param arg the element data
             */
            public void execute(List<CmsContainerElement> arg) {

                m_editor.getClipboard().clearFavorites();
                Iterator<CmsContainerElement> it = arg.iterator();
                while (it.hasNext()) {
                    addToFavorites(m_controller.getContainerpageUtil().createListItem(it.next(), null));
                }
            }

            /**
             * @see org.opencms.gwt.client.util.I_CmsSimpleCallback#onError(java.lang.String)
             */
            public void onError(String message) {

                // TODO: Auto-generated method stub

            }
        });
    }

    /**
     * Loads the recent list from the server and adds it's items to the clip-board.<p>
     */
    public void loadRecent() {

        m_controller.loadRecent(new I_CmsSimpleCallback<List<CmsContainerElement>>() {

            /**
             * Generating the list item widgets and inserting them into the recent list.<p> 
             * 
             * @param arg the element data
             */
            public void execute(List<CmsContainerElement> arg) {

                m_editor.getClipboard().clearRecent();
                Iterator<CmsContainerElement> it = arg.iterator();
                while (it.hasNext()) {
                    addToRecent(m_controller.getContainerpageUtil().createListItem(it.next(), null));
                }
            }

            /**
             * @see org.opencms.gwt.client.util.I_CmsSimpleCallback#onError(java.lang.String)
             */
            public void onError(String message) {

                // TODO: Auto-generated method stub

            }
        });
    }

    /**
     * Opens the edit dialog for the specified element.<p>
     * 
     * @param element the element to edit
     */
    public void openEditorForElement(CmsDragContainerElement element) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(element.getNoEditReason())) {
            if (CmsDomUtil.hasClass(CmsContainerpageUtil.CLASS_SUB_CONTAINER_ELEMENTS, element.getElement())) {
                openSubcontainerEditor((CmsDragSubcontainer)element);
            } else {
                CmsContentEditorDialog.get().openEditDialog(element.getClientId(), element.getSitePath());
            }
        } else {
            //should be deactivated
            CmsDebugLog.getInstance().printLine("should be deactivated: " + element.getNoEditReason());
        }
    }

    /**
     * Reloads the content for the given element and all related elements.<p>
     * 
     * @param elementId the element id
     */
    public void reloadElement(String elementId) {

        m_controller.reloadElement(elementId);
    }

    /**
     * Removes the given container-page element.<p>
     * 
     * @param element the element
     */
    public void removeElement(CmsDragContainerElement element) {

        m_controller.removeElement(element);
    }

    /**
     * Resets the container-page to it's previous state.<p>
     */
    public void resetPage() {

        CmsConfirmDialog dialog = new CmsConfirmDialog(org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_DIALOG_RESET_TITLE_0), org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_DIALOG_RESET_TEXT_0));
        dialog.setHandler(new I_CmsConfirmDialogHandler() {

            /**
             * @see org.opencms.gwt.client.ui.I_CmsCloseDialogHandler#onClose()
             */
            public void onClose() {

                deactivateCurrentButton();
                activateSelection();
            }

            /**
             * @see org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler#onOk()
             */
            public void onOk() {

                m_controller.resetPage();
            }
        });
        dialog.center();
    }

    /**
     * Saves the favorite list.<p>
     * 
     * @param clientIds the client id's of the list's elements
     * 
     * @see org.opencms.ade.containerpage.client.CmsContainerpageController#saveFavoriteList(java.util.List)
     */
    public void saveFavoriteList(List<String> clientIds) {

        m_controller.saveFavoriteList(clientIds);
    }

    /**
     * Saves the current state of the container-page.<p>
     */
    public void savePage() {

        m_controller.saveContainerpage();

        deactivateCurrentButton();
        activateSelection();
    }

    /**
     * Sets the currently active tool-bar button.<p>
     * 
     * @param button the button
     */
    public void setActiveButton(I_CmsToolbarButton button) {

        m_activeButton = button;
    }

    /**
     * Shows the publish dialog.<p>
     */
    public void showPublishDialog() {

        CmsPublishDialog.showPublishDialog(new CloseHandler<PopupPanel>() {

            /**
             * @see com.google.gwt.event.logical.shared.CloseHandler#onClose(com.google.gwt.event.logical.shared.CloseEvent)
             */
            public void onClose(CloseEvent<PopupPanel> event) {

                deactivateCurrentButton();
                activateSelection();

            }
        });
    }

    /**
     * Toggles the tool-bars visibility.<p>
     */
    public void toggleToolbar() {

        if (m_editor.toolbarVisible()) {
            deactivateCurrentButton();
            m_editor.showToolbar(false);
            m_controller.setToolbarVisible(false);
        } else {
            activateSelection();
            m_editor.showToolbar(true);
            m_controller.setToolbarVisible(true);
        }
    }

    /**
     * Opens the sub-container element editor.<p>
     * 
     * @param subContainer the sub-container element
     */
    private void openSubcontainerEditor(CmsDragSubcontainer subContainer) {

        CmsSubcontainerEditor.openSubcontainerEditor(subContainer, m_controller, this);
    }
}
