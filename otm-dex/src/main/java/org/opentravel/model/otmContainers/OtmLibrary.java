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

package org.opentravel.model.otmContainers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.common.ValidationUtils;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmServiceObject;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * OTM Object for libraries. Does <b>NOT</b> provide access to members.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmLibrary implements Comparable<OtmLibrary> {
    private static Log log = LogFactory.getLog( OtmLibrary.class );

    protected OtmModelManager mgr;
    protected List<ProjectItem> projectItems = new ArrayList<>();
    protected AbstractLibrary tlLib;
    protected OtmVersionChain versionChain = null;
    protected ValidationFindings findings;

    /**
     * Should only be called by OtmModelManager. See {@link OtmModelManager#add(AbstractLibrary)}
     * 
     * @param tl
     * @param mgr
     */
    public OtmLibrary(AbstractLibrary tl, OtmModelManager mgr) {
        this.mgr = mgr;
        tlLib = tl;
    }

    protected OtmLibrary(OtmModelManager mgr) {
        this.mgr = mgr;
    }

    public OtmLibrary(ProjectItem pi, OtmModelManager mgr) {
        this.mgr = mgr;
        projectItems.add( pi );
        tlLib = pi.getContent();
    }

    @Override
    public int compareTo(OtmLibrary o) {
        return getName().compareTo( o.getName() );
    }

    /**
     * Add the TL member to the Tl library and model manager.
     * <p>
     * Note: will not add second member if one with the same name already is in TL library
     * <p>
     * Note: will not add second service and return null if attempted.
     * <p>
     * <b>Note:</b> adds member to the model manager. See {@link OtmModelManager#add(OtmLibraryMember)}
     * 
     * @param a library member
     * @return the member if added OK
     */
    public OtmLibraryMember add(OtmLibraryMember member) {
        // Libraries can only have 1 service
        if (member instanceof OtmServiceObject && hasService())
            return null;

        if (member != null && member.getTL() instanceof LibraryMember)
            try {
                // make sure not already a member
                if (getTL().getNamedMember( ((LibraryMember) member.getTL()).getLocalName() ) == null)
                    getTL().addNamedMember( (LibraryMember) member.getTL() );
                else {
                    log.warn( "Did not add member " + member + " to library because it was already a member." );
                    return null;
                }
                // Add to model manager
                if (getModelManager() != null)
                    getModelManager().add( member );
                // Sanity check
                if (member.getTlLM().getOwningLibrary() != getTL())
                    log.warn( "Member does not have correct owning library." );
                return member;
            } catch (IllegalArgumentException e) {
                log.warn( "Exception: " + e.getLocalizedMessage() );
            }
        else
            log.warn( "Tried to add a non-library member " + member + " to library." );
        return null;
    }


    /**
     * Add the project item to the list maintained by the library. Libraries can be members of multiple, open projects.
     * 
     * @param pi
     */
    public void add(ProjectItem pi) {
        if (pi.getContent() == null
            || (!(pi.getNamespace().equals( getTL().getNamespace() ) && pi.getContent().getName().equals( getName() ))))

            // if (pi.getContent() != tlLib)
            throw new IllegalArgumentException( "Can not add project item with wrong library." );
        projectItems.add( pi );
        // log.debug( "Added project item to " + this.getName() + ". Now has " + projectItems.size() + " items." );
    }

    /**
     * @return
     */
    public boolean canBeLocked() {
        if (getStatus() == TLLibraryStatus.DRAFT && getState() == RepositoryItemState.MANAGED_UNLOCKED
            && getManagingProject() != null && getManagingProject().getPermission() != null)
            return getManagingProject().getPermission().equals( RepositoryPermission.WRITE );
        return false;
    }

    public boolean canBeUnlocked() {
        // TODO - check to see if this is the user that locked it
        return getState() == RepositoryItemState.MANAGED_LOCKED || getState() == RepositoryItemState.MANAGED_WIP;
    }

    public boolean contains(AbstractLibrary aLib) {
        if (tlLib == aLib)
            return true;
        for (ProjectItem pi : projectItems)
            if (pi.getContent() == aLib)
                return true;
        return false;
    }

    /**
     * Test the TL library to see if it has a named member with the same name.
     * 
     * @param member
     * @return
     */
    public boolean contains(OtmLibraryMember member) {
        return getTL().getNamedMember( member.getName() ) != null;
    }

    /**
     * Delete member. Remove from this library and underlying TL library and from the model manager.
     */
    public void delete(OtmLibraryMember member) {
        if (member != null) {
            if (member.getModelManager() != null)
                member.getModelManager().remove( member );
            // assert (member.getModelManager().getMember( member.getTL() ) == null);
            if (getTL() != null)
                getTL().removeNamedMember( member.getTlLM() );
            // Contextual facets are the only library members that also are children of other members via the
            // contributed facet.
            if (member instanceof OtmContextualFacet && ((OtmContextualFacet) member).getContributedObject() != null)
                ((OtmContextualFacet) member).getContributedObject().delete( member );
            // TODO - what about base types?
            // Leave where used. It is needed for un-delete.
        }
    }

    /**
     * Get Read-only, Minor or Full action manager. To determine which manager to return, consider
     * <ul>
     * <li>library version
     * <li>library status
     * </ul>
     * See also {@link #getActionManager(OtmLibraryMember)}
     * 
     * @return action manager
     */
    public DexActionManager getActionManager() {
        if (isMinorVersion())
            return getModelManager().getMinorActionManager( isEditable() );
        return getModelManager().getActionManager( isEditable() );
    }

    /**
     * Get Read-only, Minor or Full action manager. To determine which manager to return, consider
     * <ul>
     * <li>managed or unmanaged - when unmanaged always return the full action manager from model manager
     * <li>library version
     * <li>library status
     * <li>if the member is new to the chain and editable library
     * <li>if the member is the latest in the version chain.
     * </ul>
     *
     * @return action manager
     */
    public DexActionManager getActionManager(OtmLibraryMember member) {
        if (isUnmanaged())
            return getModelManager().getActionManager( true );
        if (isMajorVersion() && isEditable())
            return getModelManager().getActionManager( true );
        if (isChainEditable()) {
            if (isEditable() && getVersionChain().isNewToChain( member ))
                return getModelManager().getActionManager( true );
            return getModelManager().getMinorActionManager( getVersionChain().isLatestVersion( member ) );
        }
        return getModelManager().getActionManager( false );
    }

    /**
     * Get the base namespace from the first project item
     * 
     * @return
     */
    public String getBaseNamespace() {
        if (getTL() instanceof TLLibrary)
            return ((TLLibrary) getTL()).getBaseNamespace();
        // Fail-safe: if fails the instance test, try the PI
        return projectItems.isEmpty() ? "" : projectItems.get( 0 ).getBaseNamespace();
    }

    /**
     * 
     * @return namespace / name
     */
    public String getFullName() {
        return getTL() != null ? getTL().getNamespace() + "/" + getTL().getName() : null;
    }

    public Icons getIconType() {
        return ImageManager.Icons.LIBRARY;
    }

    public List<OtmLibrary> getIncludes() {
        List<OtmLibrary> libs = new ArrayList<>();
        for (TLInclude include : tlLib.getIncludes()) {
            if (include.getOwningLibrary() != null)
                libs.add( mgr.get( include.getOwningLibrary() ) );
        }
        return libs;
    }

    public String getLockedBy() {
        for (ProjectItem pi : projectItems)
            if (pi.getLockedByUser() != null)
                return pi.getLockedByUser();
        return "";
    }

    /**
     * @param namespace
     * @return the major version number
     * @throws VersionSchemeException
     */
    public int getMajorVersion() throws VersionSchemeException {
        int vn = 0;
        if (!isBuiltIn() && getTL().getNamespace() != null) {
            try {
                String versionScheme = getTL().getVersionScheme();
                VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme( versionScheme );
                String versionId = vScheme.getVersionIdentifier( getTL().getNamespace() );
                vn = Integer.valueOf( vScheme.getMajorVersion( versionId ) );
            } catch (NumberFormatException e) {
                log.debug( "Error converting version string." + e.getCause() );
            } catch (VersionSchemeException e) {
                log.debug( "Error determining version. " + e.getCause() );
            }
        }
        return vn;
    }

    public OtmProject getManagingProject() {
        return mgr.getManagingProject( this );
    }

    /**
     * Get the members of this library from the model manager.
     * 
     * @return
     */
    public List<OtmLibraryMember> getMembers() {
        return getModelManager().getMembers( this );
    }

    /**
     * @return the minor version number
     * @throws VersionSchemeException
     */
    public int getMinorVersion() {
        int vn = 0;
        if (!isBuiltIn() && getTL().getNamespace() != null) {
            try {
                String versionScheme = getTL().getVersionScheme();
                VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme( versionScheme );
                String versionId = vScheme.getVersionIdentifier( getTL().getNamespace() );
                vn = Integer.valueOf( vScheme.getMinorVersion( versionId ) );
            } catch (NumberFormatException e) {
                log.debug( "Error converting version string." + e.getCause() );
            } catch (VersionSchemeException e) {
                log.debug( "Error determining version. " + e.getCause() );
            }
        }
        return vn;
    }

    /**
     * @return the minor version number
     * @throws VersionSchemeException
     */
    public int getPatchVersion() {
        int vn = 0;
        if (!isBuiltIn() && getTL().getNamespace() != null) {
            try {
                String versionScheme = getTL().getVersionScheme();
                VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme( versionScheme );
                String versionId = vScheme.getVersionIdentifier( getTL().getNamespace() );
                vn = Integer.valueOf( vScheme.getPatchLevel( versionId ) );
            } catch (NumberFormatException e) {
                log.debug( "Error converting version string." + e.getCause() );
            } catch (VersionSchemeException e) {
                log.debug( "Error determining version. " + e.getCause() );
            }
        }
        return vn;
    }

    public OtmModelManager getModelManager() {
        return mgr;
    }

    public String getName() {
        return getTL() != null ? getTL().getName() : "";
    }

    public String getNameWithBasenamespace() {
        return getBaseNamespace() + "/" + getName();
    }

    public String getPrefix() {
        return getTL().getPrefix();
    }

    /**
     * @see #getManagingProject()
     * @return the project item for this library in the managing project
     */
    public ProjectItem getProjectItem() {
        ProjectItem pi = null;
        OtmProject project = getManagingProject();
        if (project != null)
            for (ProjectItem candidate : project.getTL().getProjectItems())
                if (getProjectItems().contains( candidate ))
                    pi = candidate;
        return pi;
    }

    public List<ProjectItem> getProjectItems() {
        return projectItems;
    }

    /**
     * Get the name(s) of the project(s) that contain this library.
     * 
     * @return new array of string containing the project names
     */
    public List<String> getProjectNames() {
        List<String> names = new ArrayList<>();
        getModelManager().getProjects( getTL() ).forEach( p -> names.add( p.getName() ) );
        names.sort( null );
        return names;
    }

    public List<OtmProject> getProjects() {
        List<OtmProject> projects = new ArrayList<>();
        if (projectItems != null)
            getProjectNames().forEach( pn -> projects.add( getModelManager().getProject( pn ) ) );
        return projects;
    }

    /**
     * Examine all project items and return the state that grants the user the most rights.
     * 
     * @return
     */
    public RepositoryItemState getState() {
        RepositoryItemState state = RepositoryItemState.MANAGED_UNLOCKED; // the weakest state
        if (projectItems != null) {
            // If not in a project, it must be unmanaged.
            if (projectItems.isEmpty())
                return RepositoryItemState.UNMANAGED;

            for (ProjectItem pi : projectItems) {
                // log.debug("state = " + pi.getState());
                switch (pi.getState()) {
                    case MANAGED_UNLOCKED:
                        break;
                    case BUILT_IN:
                    case UNMANAGED:
                        // These are true regardless of user or user actions
                        return pi.getState();

                    case MANAGED_LOCKED:
                        if (state != RepositoryItemState.MANAGED_WIP)
                            state = pi.getState();
                        break;

                    case MANAGED_WIP:
                        // This gives user most rights and is therefore always used as state
                        return pi.getState();
                }
            }
        }
        return state;
    }

    /**
     * @return actual status of TL Libraries otherwise DRAFT
     */
    public TLLibraryStatus getStatus() {
        if (tlLib instanceof TLLibrary)
            return ((TLLibrary) tlLib).getStatus();
        else
            return TLLibraryStatus.FINAL;
    }

    public AbstractLibrary getTL() {
        return tlLib;
    }

    /**
     * @return
     */
    public String getVersion() {
        return getTL().getVersion();
    }

    public boolean hasService() {
        for (OtmLibraryMember m : getMembers())
            if (m instanceof OtmServiceObject)
                return true;
        return false;
    }

    public boolean isBuiltIn() {
        return getTL() instanceof BuiltInLibrary;
    }

    /**
     * A library is editable if any associated project item state is Managed_WIP -OR- unmanaged. Regardless of action
     * manager.
     * 
     * @return
     */
    public boolean isEditable() {
        // log.debug( getName() + " State = " + getState().toString() + " Status = " + getStatus() );
        if (getStatus() != TLLibraryStatus.DRAFT)
            return false;
        return getState() == RepositoryItemState.MANAGED_WIP || getState() == RepositoryItemState.UNMANAGED;
    }

    /**
     * Are any of the libraries in the version chain (same major version number) editable? Always true if this library
     * is editable.
     * 
     * @return
     */
    public boolean isChainEditable() {
        if (isEditable())
            return true;
        return getVersionChain().isChainEditable();
        // boolean editableLibFound = false;
        // for (OtmLibrary lib : mgr.getVersionChain( this ))
        // if (lib.isEditable())
        // editableLibFound = true;
        // return editableLibFound;
    }

    // FIXME - clear version chain when new library added
    public OtmVersionChain getVersionChain() {
        if (versionChain == null)
            versionChain = new OtmVersionChain( this );
        return versionChain;
    }

    /**
     * Ask the model manager if this is the latest version of the library
     * 
     * @return
     */
    public boolean isLatestVersion() {
        return mgr.isLatest( this );
    }

    /**
     * @return true if minor version number is > 0 and managed in repository
     */
    public boolean isMinorVersion() {
        if (getPatchVersion() > 0)
            return false;
        return (getMinorVersion() > 0 && getState() != RepositoryItemState.UNMANAGED);
    }

    /**
     * @return true if minor version number is = 0 and managed in repository
     */
    public boolean isMajorVersion() {
        if (getPatchVersion() > 0)
            return false;
        return (getMinorVersion() == 0 && getState() != RepositoryItemState.UNMANAGED);
    }

    /**
     * @return
     */
    public boolean isUnmanaged() {
        return getState() == RepositoryItemState.UNMANAGED;
    }

    public ValidationFindings getFindings() {
        if (findings == null)
            isValid();
        return findings;
    }

    public boolean isValid() {
        findings = OtmModelElement.isValid( getTL() );
        return !ValidationUtils.hasErrors( findings );
        // return findings == null || findings.isEmpty();
    }

    /**
     * No-operation. (see {@link #delete(OtmLibraryMember)}
     * <p>
     * Note: This is not implemented because OtmLibrary does not maintain children list. Method is here just to make
     * finding the right delete method easier
     * 
     * @param a library member
     */
    public void remove(OtmLibraryMember member) {
        // No-op
    }

    public void remove(ProjectItem item) {
        projectItems.remove( item );
    }

    /**
     * 
     */
    public void save() {
        save( new LibraryModelSaver() );
    }

    /**
     * Use the passed saver. Intended for use in saving multiple libraries in one action.
     * 
     * @param lms
     */
    public void save(LibraryModelSaver lms) {
        if (getTL() instanceof TLLibrary) {
            // final ValidationFindings findings = new ValidationFindings();
            try {
                // log.debug("Saving library: " + libraryName + " " + libraryUrl);
                // findings.addAll( lms.saveLibrary( (TLLibrary) getTL() ) );
                lms.saveLibrary( (TLLibrary) getTL() );
            } catch (final LibrarySaveException e) {
                final Throwable t = e.getCause();
                if (t != null && t.getMessage() != null)
                    log.error( "Save error" + t.getMessage() );
            }
        }
    }

    public String toString() {
        return getNameWithBasenamespace() + " " + getVersion();
    }

    public void validate() {
        findings = TLModelCompileValidator.validateModelElement( getTL(), true );
    }

    /**
     * Get a provider map.
     * <p>
     * The keys are each provider library -- libraries containing types assigned to type-users in this library.
     * <p>
     * The values are an array of this library's members that use the provided the types. Each value is a member that
     * uses types from the provider library.
     * 
     * @return new map.
     */
    public Map<OtmLibrary,List<OtmLibraryMember>> getProviderMap(boolean sort) {
        Map<OtmLibrary,List<OtmLibraryMember>> providerMap = new TreeMap<>();
        for (OtmLibraryMember m : getMembers()) {
            if (m instanceof OtmTypeUser)
                addToMap( (OtmTypeUser) m, providerMap );
            for (OtmTypeUser u : m.getDescendantsTypeUsers()) {
                // For each user, get the provider's owner and add to map
                addToMap( u, providerMap );
            }
        }
        if (sort)
            providerMap.values().forEach( l -> l.sort( null ) );
        return providerMap;
    }

    // Entry: user's assignedType's library : user's owning member
    private void addToMap(OtmTypeUser user, Map<OtmLibrary,List<OtmLibraryMember>> map) {
        if (user != null && map != null && user.getAssignedType() != null) {
            OtmLibraryMember owner = user.getAssignedType().getOwningMember();
            if (owner.getLibrary() == this)
                return;

            List<OtmLibraryMember> mList = map.get( owner.getLibrary() );
            if (mList != null) {
                if (!mList.contains( user.getOwningMember() ))
                    mList.add( user.getOwningMember() );
            } else {
                mList = new ArrayList<>();
                mList.add( user.getOwningMember() );
                map.put( owner.getLibrary(), mList );
            }
        }
    }

    /**
     * Get a users map.
     * <p>
     * The keys are each library that uses types from this library.
     * <p>
     * The values are an array of library members that use the types.
     * 
     * @return new map.
     */
    public Map<OtmLibrary,List<OtmLibraryMember>> getUsersMap(boolean sort) {
        Map<OtmLibrary,List<OtmLibraryMember>> usersMap = new HashMap<>();
        getMembers().forEach( m -> {
            m.getDescendantsTypeProviders().forEach( p -> {
                if (p != null) {
                    List<OtmLibraryMember> users = p.getOwningMember().getWhereUsed();
                    users.forEach( u -> {
                        if (u.getLibrary() != this) {
                            List<OtmLibraryMember> mList = usersMap.get( u.getLibrary() );
                            if (mList != null) {
                                if (!mList.contains( u.getOwningMember() ))
                                    usersMap.get( u.getLibrary() ).add( u.getOwningMember() );
                            } else {
                                mList = new ArrayList<>();
                                mList.add( u.getOwningMember() );
                                usersMap.put( u.getLibrary(), mList );
                            }
                        }
                    } );
                }
            } );
        } );
        return usersMap;
    }
}
